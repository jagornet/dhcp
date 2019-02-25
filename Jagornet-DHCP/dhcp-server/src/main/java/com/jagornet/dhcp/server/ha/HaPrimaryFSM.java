package com.jagornet.dhcp.server.ha;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.config.DhcpServerConfigException;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.db.DhcpLease;
import com.jagornet.dhcp.server.db.LeaseManager;
import com.jagornet.dhcp.server.request.binding.Binding;
import com.jagornet.dhcp.server.rest.api.DhcpLeasesResource;
import com.jagornet.dhcp.server.rest.cli.JerseyRestClient;

public class HaPrimaryFSM implements Runnable {

	private static Logger log = LoggerFactory.getLogger(HaPrimaryFSM.class);
	
	public enum State { PRIMARY_INIT,
						PRIMARY_RUNNING,
						PRIMARY_AWAITING_BNDACK,
						PRIMARY_REQUESTING_CONTROL,
						PRIMARY_RECEIVING_UPDATES };

	public enum UpdateMode { SYNC, ASYNC, DATABASE };
	
	private String backupHost;
	private int backupPort;
	private State state;
	private UpdateMode updateMode;
	private JerseyRestClient restClient;
	
	public HaPrimaryFSM(String backupHost, int backupPort) {
		this.backupHost = backupHost;
		this.backupPort = backupPort;
		setState(State.PRIMARY_INIT);
	}

	public void init(State state) throws Exception {
		this.state = state;
		String mode = DhcpServerPolicies.globalPolicy(Property.HA_BINDING_UPDATE_MODE);
		if (mode.equalsIgnoreCase("sync")) {
			updateMode = UpdateMode.SYNC;
		}
		else if (mode.equalsIgnoreCase("async")) {
			updateMode = UpdateMode.ASYNC;
		}
		else if (mode.equalsIgnoreCase("database")) {
			updateMode = UpdateMode.DATABASE;
		}
		else {
			throw new DhcpServerConfigException("Unknown " + Property.HA_BINDING_UPDATE_MODE.key() +
												": " + mode);
		}
		restClient = new JerseyRestClient(backupHost, backupPort);
		Thread haPrimaryThread = new Thread(this, "HA-Primary");
		haPrimaryThread.start();
		setState(state);
	}
	
	@Override
	public void run() {
		// TODO: complete FSM
		if (state == State.PRIMARY_INIT) {
			state = State.PRIMARY_RUNNING;
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

	public void setState(State state) {
		if (this.state != state) {
			//TODO: write the state to a file
			log.info("State change: " + this.state + " -> " + state);
			this.state = state;
		}
	}

	private String buildPath(String ip) {
//		return DhcpLeasesResource.PATH_IPS + "/" + ip;
		return DhcpLeasesResource.PATH;
	}
	
	public void updateBindings(List<Binding> bindings) {
		if (getState().equals(State.PRIMARY_RUNNING)) {
			if (updateMode == UpdateMode.DATABASE) {
				log.info("HA binding update delegated to database replication");
			}
			else {
				for (Binding binding : bindings) {
					List<DhcpLease> dhcpLeases = LeaseManager.toDhcpLeases(binding);
					for (DhcpLease dhcpLease : dhcpLeases) {
						if (updateMode == UpdateMode.SYNC) {
							restClient.doPost(
									buildPath(dhcpLease.getIpAddress().getHostAddress()), 
									dhcpLease.toJson());
						}
						else { 
							//TODO: something with the callback!
//							HaBindingCallbackString callback = new HaBindingCallbackString(binding);
							HaBindingCallbackResponse callback = new HaBindingCallbackResponse(binding);
							restClient.doPostAsync(
									buildPath(dhcpLease.getIpAddress().getHostAddress()),
									dhcpLease.toJson(), callback);
						}
					}
				}
			}
		}
		else {
			log.warn("Unable to process failover binding updates: " +
					 "failover state=" + getState());
		}
	}
	
}
