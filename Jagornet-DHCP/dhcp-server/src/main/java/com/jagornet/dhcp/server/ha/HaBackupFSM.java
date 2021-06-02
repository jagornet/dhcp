package com.jagornet.dhcp.server.ha;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.rest.api.DhcpLeasesService;
import com.jagornet.dhcp.server.rest.api.DhcpServerStatusResource;
import com.jagornet.dhcp.server.rest.api.DhcpServerStatusService;
import com.jagornet.dhcp.server.rest.cli.JerseyRestClient;

public class HaBackupFSM implements Runnable {

	private static Logger log = LoggerFactory.getLogger(HaBackupFSM.class);

	/*
	public enum State { BACKUP_INIT,
						BACKUP_POLLING,
						BACKUP_AWAITING_POLL_REPLY,
						BACKUP_POLLING_FAILURE,
						BACKUP_RUNNING,
						BACKUP_RETURNING_CONTROL,
						BACKUP_BULK_BINDINGS_SENT };
	*/
	
	public enum State { BACKUP_INIT,
						BACKUP_SYNCING_FROM_PRIMARY,
						// don't care BACKUP_SYNCING_TO_PRIMARY,
						BACKUP_POLLING,
						BACKUP_RUNNING,
						BACKUP_CONFLICT };

	private String primaryHost;
	private int primaryPort;
	private boolean requestAllLeasesOnRestart;
	private State state;
	private HaPrimaryFSM.State primaryState;
	private HaStateDbManager haStateDbManager;
	private Collection<DhcpLink> dhcpLinks;
	private CountDownLatch linkSyncLatch;

	// REST client for communicating to primary
	private JerseyRestClient restClient;
	// REST service for handling requests from primary
	private DhcpLeasesService dhcpLeasesService;
	
	private ScheduledExecutorService pollingTaskExecutor = null;
	private ScheduledFuture<?> scheduledPollingFuture = null;
	private PollingTask pollingTask = null;
	private int pollReplyTimeoutCnt = 0;
	
	public HaBackupFSM(String primaryHost, int primaryPort) {
		this.primaryHost = primaryHost;
		this.primaryPort = primaryPort;
		requestAllLeasesOnRestart = 
				DhcpServerPolicies.globalPolicyAsBoolean(Property.HA_CONTROL_REQUEST_ALL_LEASES_ON_RESTART);
    	pollingTaskExecutor = Executors.newSingleThreadScheduledExecutor();
    	pollingTask = new PollingTask();
	}

	public void init() throws Exception {
		
		dhcpLinks = DhcpServerConfiguration.getInstance().getLinkMap().values();
		
		// service implementation for processing leases synced from backup server
		dhcpLeasesService = new DhcpLeasesService();
		
		String haUsername = DhcpServerPolicies.globalPolicy(Property.HA_USERNAME);
		String haPassword = DhcpServerPolicies.globalPolicy(Property.HA_PASSWORD);

		// client implementation for getting updates from primary server
		restClient = new JerseyRestClient(primaryHost, primaryPort,
										  haUsername, haPassword);

		// get the last stored state and take the appropriate action for startup
		haStateDbManager = new HaStateDbManager();
    	HaState haState = haStateDbManager.init(DhcpServerConfiguration.HaRole.BACKUP);
    	//since we store state changes to either server, and we don't really
    	//care what the last state was, not going to set a startup state here
		//state = State.valueOf(haState.state);
    	setState(State.valueOf(haState.state));
    	
		Thread haBackupThread = new Thread(this, "HA-Backup");
		haBackupThread.start();
	}

	@Override
	public void run() {
		String primaryHaState = 
				restClient.doGetString(DhcpServerStatusResource.PATH +
										DhcpServerStatusResource.HASTATE);
		if (primaryHaState == null) {
			log.info("Null response from HA Primary server");
			primaryState = null;
			// we are backup, so start polling and
			// if polling fails, then take over
			startPollingTask();
			return;
		}
		log.info("HA Primary state: " + primaryHaState);
		primaryState = HaPrimaryFSM.State.valueOf(primaryHaState);
		
		// primary is available, start link sync from primary
		try {
			startLinkSync();
		}
		catch (Exception ex) {
			log.error("Failed to start Link Sync");
		}
		startPollingTask();
	}

	public void startPollingTask() {
		int pollSeconds = DhcpServerPolicies.globalPolicyAsInt(
				Property.HA_POLL_SECONDS);
		// TODO: consider scheduleWithFixedDelay?
		setState(State.BACKUP_POLLING);
		scheduledPollingFuture = pollingTaskExecutor.scheduleAtFixedRate(
				pollingTask, 0, pollSeconds, TimeUnit.SECONDS);
	}
	
	protected void startLinkSync() throws Exception {
		if (!dhcpLinks.isEmpty()) {
	    	linkSyncLatch = new CountDownLatch(dhcpLinks.size());
	    	boolean unsyncedLeasesOnly =  requestAllLeasesOnRestart ||
										  getState().equals(State.BACKUP_INIT) ?
												  false : true;
			setState(State.BACKUP_SYNCING_FROM_PRIMARY);
			Instant start = Instant.now();
			// check the state of the primary
			// set links unavailable
			for (DhcpLink dhcpLink : dhcpLinks) {
				dhcpLink.setState(DhcpLink.State.NOT_SYNCED);
				Thread linkSyncThread = new Thread(
						new LinkSyncThread(dhcpLink, 
						linkSyncLatch, restClient, 
						dhcpLeasesService, unsyncedLeasesOnly), 
						"BackupLinkSyncFromPrimary-" + dhcpLink.getLinkAddress()
						);
				linkSyncThread.start();
			}

	    	try {
//TODO	    		
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
	
	public String getPrimaryHost() {
		return primaryHost;
	}

	public void setPrimaryHost(String primaryHost) {
		this.primaryHost = primaryHost;
	}

	public int getPrimaryPort() {
		return primaryPort;
	}

	public void setPrimaryPort(int primaryPort) {
		this.primaryPort = primaryPort;
	}
	
	public State getState() {
		return state;
	}

	public synchronized void setState(State state) {
		if (this.state != state) {
			log.info("Backup HA State change: " + this.state + " -> " + state);
			this.state = state;
			try {
				haStateDbManager.updateBackupState(this.state);
			}
			catch (Exception ex) {
				log.error("Failed to update HA backup state DB", ex);
			}
		}
	}
	
	public HaPrimaryFSM.State getPrimaryState() {
		return primaryState;
	}

	public synchronized void setPrimaryState(HaPrimaryFSM.State primaryState) {
		if (this.primaryState != primaryState) {
			log.info("Primary HA State change: " + this.primaryState + " -> " + primaryState);
			this.primaryState = primaryState;
			try {
				haStateDbManager.updatePrimaryState(this.primaryState);
			}
			catch (Exception ex) {
				log.error("Failed to update HA backup state DB", ex);
			}
		}
	}

	public void stopPollingTask() {
		scheduledPollingFuture.cancel(true);
	}
	
	class PollingTask implements Runnable {

// timeout is configured in JerseyRestClient
//		private int timeout;
		private int failLimit;
		
		public PollingTask() {
//			timeout = DhcpServerPolicies.globalPolicyAsInt(
//					Property.HA_POLL_REPLY_TIMEOUT);			
			failLimit = DhcpServerPolicies.globalPolicyAsInt(
					Property.HA_POLL_REPLY_FAILURE_COUNT);
		}
		
		@Override
		public void run() {
			String status = restClient.doGetString(DhcpServerStatusResource.PATH);
			if (status == null) {
				log.error("No response to status request");
				pollReplyTimeoutCnt++;
				log.warn("Poll reply timeout exceeded " + pollReplyTimeoutCnt + 
						 " of limit " + failLimit + " times");
				if (pollReplyTimeoutCnt >= failLimit) {
					log.warn("Poll reply failure count limit reached: " + failLimit);
					setState(State.BACKUP_RUNNING);
				}
			}
			else {
				log.info("Status request response: " + status);
				if (status.equals(DhcpServerStatusService.STATUS_OK)) {
					setState(State.BACKUP_POLLING);
					log.debug("Resetting pollReplyTimeoutCount=0");
					pollReplyTimeoutCnt = 0;
				}
				else {
					//TODO
				}
			}
		}
	}
}
