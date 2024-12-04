package com.jagornet.dhcp.server.ha;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.ws.rs.client.InvocationCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.option.base.DhcpOption;
import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.db.DhcpLease;
import com.jagornet.dhcp.server.db.LeaseManager;
import com.jagornet.dhcp.server.request.binding.Binding;
import com.jagornet.dhcp.server.rest.api.DhcpLeasesService;

public class HaPrimaryFSM implements Runnable {

	private static Logger log = LoggerFactory.getLogger(HaPrimaryFSM.class);

	/*
	public enum State { PRIMARY_INIT,
						PRIMARY_RUNNING,
						PRIMARY_AWAITING_BNDACK,
						PRIMARY_REQUESTING_CONTROL,
						PRIMARY_RECEIVING_UPDATES };
	*/
	
	public enum State { PRIMARY_INIT,
						// don't care PRIMARY_REQUESTING_CONTROL,
						PRIMARY_SYNCING_FROM_BACKUP,
						// don't care PRIMARY_SYNCING_TO_BACKUP,
						PRIMARY_RUNNING,
						PRIMARY_CONFLICT };

	public enum UpdateMode { SYNC, ASYNC, DATABASE };
	
	private String backupHost;
	private int backupPort;
	private boolean requestAllLeasesOnRestart;
	private State state;
	private HaBackupFSM.State backupState;
	private UpdateMode updateMode;
	private HaStateDbManager haStateDbManager;
	private Collection<DhcpLink> dhcpLinks;
	private CountDownLatch linkSyncLatch;

	private boolean asyncUpdate;

	// HA client for communicating to backup
	private HaClient haClient;
	// service for handling requests from backup
	private DhcpLeasesService dhcpLeasesService;

	public HaPrimaryFSM(String backupHost, int backupPort, HaClient haClient, boolean asyncUpdate) {
		this.backupHost = backupHost;
		this.backupPort = backupPort;
		this.haClient = haClient;
		this.asyncUpdate = asyncUpdate;
		requestAllLeasesOnRestart = 
				DhcpServerPolicies.globalPolicyAsBoolean(Property.HA_CONTROL_REQUEST_ALL_LEASES_ON_RESTART);
	}

	public void startFSM() throws Exception {
		
		dhcpLinks = DhcpServerConfiguration.getInstance().getLinkMap().values();
		
		dhcpLeasesService = new DhcpLeasesService();

		// get the last stored state and take the appropriate action for startup
		haStateDbManager = new HaStateDbManager();
    	HaState haState = haStateDbManager.init(DhcpServerConfiguration.HaRole.PRIMARY);
    	//since we store state changes to either server, and we don't really
    	//care what the last state was, not going to set a startup state here
		//state = State.valueOf(haState.state);
    	setState(State.valueOf(haState.state));
		
		Thread haPrimaryThread = new Thread(this, "HA-Primary");
		haPrimaryThread.start();
	}
	
	@Override
	public void run() {

		backupState = haClient.getBackupHaState();
		if (backupState == null) {
			log.info("Null response from HA Backup server");
			// backup unavailable, so just get going
			setState(State.PRIMARY_RUNNING);
			return;
		}
		log.info("HA Backup state: " + backupState);

		// backup is available, start link sync from backup
		try {
			startLinkSync();
		}
		catch (Exception ex) {
			log.error("Failed to start Link Sync");
		}
		setState(State.PRIMARY_RUNNING);
	}
	
	protected void startLinkSync() throws Exception {
		if (!dhcpLinks.isEmpty()) {
	    	linkSyncLatch = new CountDownLatch(dhcpLinks.size());
	    	// request all leases from the backup if configured to do so,
	    	// or if this primary is initializing for the first time
	    	boolean unsyncedLeasesOnly =  requestAllLeasesOnRestart ||
	    								  getState().equals(State.PRIMARY_INIT) ?
	    										  false : true;
			setState(State.PRIMARY_SYNCING_FROM_BACKUP);
			Instant start = Instant.now();
			// check the state of the backup
			// set links unavailable
			for (DhcpLink dhcpLink : dhcpLinks) {
				dhcpLink.setState(DhcpLink.State.NOT_SYNCED);

				Thread linkSyncThread = new Thread(
						haClient.buildLinkSyncThread(dhcpLink, linkSyncLatch, unsyncedLeasesOnly), 
						"PrimaryLinkSyncFromBackup-" + dhcpLink.getLinkAddress()
						);
				linkSyncThread.start();
			}
	    	try {
// TODO
//	    		int timeout = 60;
//	    		log.info("Waiting total of " + timeout + " seconds for " + 
//	    				dhcpLinks.size() + " links to sync");
//	    		linkSyncLatch.await(timeout, TimeUnit.SECONDS);
	    		log.info("Waiting for " +  dhcpLinks.size() + " links to sync");
	    		linkSyncLatch.await();
				Instant finish = Instant.now();
			    long timeElapsed = Duration.between(start, finish).toMillis();
			    log.info("Link sync threads completed: timeElapsed=" + 
			    		 timeElapsed + "ms");
			} 
	    	catch (InterruptedException e) {
				log.error("Link sync interrupted: ", e);
			}
		}
		else {
			log.error("No links to sync!");
		}
	}
	
	public String getBackupHost() {
		return backupHost;
	}

	public void setBackupHost(String backupHost) {
		this.backupHost = backupHost;
	}

	public int getBackupPort() {
		return backupPort;
	}

	public void setBackupPort(int backupPort) {
		this.backupPort = backupPort;
	}
	
	public State getState() {
		return state;
	}

	public synchronized void setState(State state) {
		if (this.state != state) {
			log.info("Primary State change: " + this.state + " -> " + state);
			this.state = state;
			try {
				haStateDbManager.updatePrimaryState(this.state);
			}
			catch (Exception ex) {
				log.error("Failed to update HA primary state DB", ex);
			}
		}
	}

	public HaBackupFSM.State getBackupState() {
		return backupState;
	}

	public synchronized void setBackupState(HaBackupFSM.State backupState) {
		if (this.backupState != backupState) {
			log.info("Backup State change: " + this.backupState + " -> " + backupState);
			this.backupState = backupState;
			try {
				haStateDbManager.updateBackupState(this.backupState);
			}
			catch (Exception ex) {
				log.error("Failed to update HA primary state DB", ex);
			}
		}
	}
	
	public void updateBindings(List<Binding> bindings, Map<Integer, DhcpOption> dhcpOptionMap) {
//		if (getState().equals(State.PRIMARY_RUNNING)) {
		if (getBackupState() != null) {
			// backup is available, so send it updates
			if (updateMode == UpdateMode.DATABASE) {
				log.info("HA binding update delegated to database replication");
			}
			else {
				log.info("Sending binding updates to backup server");
				//TODO: consider posting the whole binding rather than one DhcpLease
				// at a time, even though Binding probably only contains one DhcpLease?
//				Collection<com.jagornet.dhcp.server.db.DbDhcpOption> dhcpOptions = 
//						BaseBindingManager.convertDhcpOptions(dhcpOptionMap);				
				for (Binding binding : bindings) {
					List<DhcpLease> dhcpLeases = LeaseManager.toDhcpLeases(binding, dhcpOptionMap.values());
					for (DhcpLease dhcpLease : dhcpLeases) {
						// Expected response is that the haPeerState is
						// set to the value of the state...
						// TODO: consider an alternative, architected return value?
						DhcpLease expectedDhcpLease = dhcpLease.clone();
						expectedDhcpLease.setHaPeerState(expectedDhcpLease.getState());
						if (!asyncUpdate) {
							DhcpLease responseDhcpLease = haClient.updateDhcpLease(dhcpLease);
							log.info("Binding update response: " + responseDhcpLease);
							if (expectedDhcpLease.equals(responseDhcpLease)) {
								// if response matches what we sent, then success
								// so update the HA peer state of the lease as synced
								dhcpLease.setHaPeerState(dhcpLease.getState());
								if (dhcpLeasesService.updateDhcpLease(dhcpLease.getIpAddress(), dhcpLease)) {
									log.info("HA peer state updated (sync) successfully");
								}
								else {
									log.error("HA peer state update (sync) failed");
								}
							}
							else {
								log.warn("Response (sync) does not match expected DhcpLease: " +
											expectedDhcpLease);
								// if the response doesn't match what we sent, then failure
								// so update the HA peer state of the lease as unknown
// not necessary, since we set haPeerState=UNKNOWN when creating/updating the lease
//								dhcpLease.setHaPeerState(IaAddress.UNKNOWN);
//								dhcpLeasesService.updateDhcpLease(dhcpLease.getIpAddress(), dhcpLease);
							}
						}
						else { 
							haClient.updateDhcpLeaseAsync(dhcpLease, expectedDhcpLease);
						}
					}
				}
			}
		}
		else {
			log.warn("HA Backup unvailable, not sending binding update");
		}
	}
	
	@Deprecated
	public class HaDhcpLeaseCallbackString implements InvocationCallback<String> {
		
		private DhcpLease dhcpLease;
		private String expectedLeaseJson;
		
		public HaDhcpLeaseCallbackString(DhcpLease dhcpLease, String expectedLeaseJson) {
			this.dhcpLease = dhcpLease;
			this.expectedLeaseJson = expectedLeaseJson;
		}

		@Override
		public void completed(String response) {
			log.info("DhcpLease update (async) completed for: " + dhcpLease);
			if (expectedLeaseJson.equals(response)) {
				// if response matches what we sent, then success
				// so update the HA peer state of the lease as synced
				dhcpLease.setHaPeerState(dhcpLease.getState());
				if (dhcpLeasesService.updateDhcpLease(dhcpLease.getIpAddress(), dhcpLease)) {
					log.info("HA peer state updated (async) successfully");
				}
				else {
					log.error("HA peer state update (async) failed");
				}
			}
			else {
				log.warn("Response (async) does not match posted JSON data");
				// if the response doesn't match what we sent, then failure
				// so update the HA peer state of the lease as unknown
// not necessary, since we set haPeerState=UNKNOWN when creating/updating the lease
//				dhcpLease.setHaPeerState(IaAddress.UNKNOWN);
//				dhcpLeasesService.updateDhcpLease(dhcpLease.getIpAddress(), dhcpLease);
			}
			
		}

		@Override
		public void failed(Throwable throwable) {
			log.error("DhcpLease update (async) failed for: " + dhcpLease + ": " + throwable);
// not necessary, since we set haPeerState=UNKNOWN when creating/updating the lease
//			dhcpLease.setHaPeerState(IaAddress.UNKNOWN);
//			dhcpLeasesService.updateDhcpLease(dhcpLease.getIpAddress(), dhcpLease);
		}
	}
	
	public class HaDhcpLeaseCallback implements InvocationCallback<DhcpLease> {
		
		private DhcpLease dhcpLease;
		private DhcpLease expectedDhcpLease;
		
		public HaDhcpLeaseCallback(DhcpLease dhcpLease, DhcpLease expectedDhcpLease) {
			this.dhcpLease = dhcpLease;
			this.expectedDhcpLease = expectedDhcpLease;
		}

		@Override
		public void completed(DhcpLease responseDhcpLease) {
			log.info("DhcpLease update (async) completed for: " + dhcpLease);
			if (expectedDhcpLease.equals(responseDhcpLease)) {
				// if response matches what we sent, then success
				// so update the HA peer state of the lease as synced
				dhcpLease.setHaPeerState(dhcpLease.getState());
				if (dhcpLeasesService.updateDhcpLease(dhcpLease.getIpAddress(), dhcpLease)) {
					log.info("HA peer state updated (async) successfully");
				}
				else {
					log.error("HA peer state update (async) failed");
				}
			}
			else {
				log.warn("Response (async) does not match posted DhcpLease");
				// if the response doesn't match what we sent, then failure
				// so update the HA peer state of the lease as unknown
// not necessary, since we set haPeerState=UNKNOWN when creating/updating the lease
//				dhcpLease.setHaPeerState(IaAddress.UNKNOWN);
//				dhcpLeasesService.updateDhcpLease(dhcpLease.getIpAddress(), dhcpLease);
			}
			
		}

		@Override
		public void failed(Throwable throwable) {
			log.error("DhcpLease update (async) failed for: " + dhcpLease + ": " + throwable);
// not necessary, since we set haPeerState=UNKNOWN when creating/updating the lease
//			dhcpLease.setHaPeerState(IaAddress.UNKNOWN);
//			dhcpLeasesService.updateDhcpLease(dhcpLease.getIpAddress(), dhcpLease);
		}
	}
}
