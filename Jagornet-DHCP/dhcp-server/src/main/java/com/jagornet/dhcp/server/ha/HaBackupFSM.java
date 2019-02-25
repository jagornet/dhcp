package com.jagornet.dhcp.server.ha;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.rest.api.DhcpServerStatusResource;
import com.jagornet.dhcp.server.rest.api.DhcpServerStatusService;
import com.jagornet.dhcp.server.rest.cli.JerseyRestClient;

public class HaBackupFSM implements Runnable {

	private static Logger log = LoggerFactory.getLogger(HaBackupFSM.class);
		
	public enum State { BACKUP_INIT,
						BACKUP_POLLING,
						BACKUP_AWAITING_POLL_REPLY,
						BACKUP_POLLING_FAILURE,
						BACKUP_RUNNING,
						BACKUP_RETURNING_CONTROL,
						BACKUP_BULK_BINDINGS_SENT };

	private String primaryHost;
	private int primaryPort;
	private State state;
	private JerseyRestClient restClient;
	
	private ScheduledExecutorService pollingTaskExecutor = null;
	private ScheduledFuture<?> scheduledPollingFuture = null;
	private PollingTask pollingTask = null;
	private int pollReplyTimeoutCnt = 0;
	
	public HaBackupFSM(String primaryHost, int primaryPort) {
		this.primaryHost = primaryHost;
		this.primaryPort = primaryPort;
		setState(State.BACKUP_INIT);
    	pollingTaskExecutor = Executors.newSingleThreadScheduledExecutor();
    	pollingTask = new PollingTask();
	}

	public void init(State state) throws Exception {
		restClient = new JerseyRestClient(primaryHost, primaryPort);
		// start polling the primary server regardless of the initial
		// state, because we always want to know if primary is running
		Thread haBackupThread = new Thread(this, "HA-Backup");
		haBackupThread.start();
		setState(state);
	}

	@Override
	public void run() {
		int pollSeconds = DhcpServerPolicies.globalPolicyAsInt(
				Property.HA_POLL_SECONDS);
		// TODO: consider scheduleWithFixedDelay?
		scheduledPollingFuture = pollingTaskExecutor.scheduleAtFixedRate(
				pollingTask, 0, pollSeconds, TimeUnit.SECONDS);
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

	public void setState(State state) {
		if (this.state != state) {
			//TODO: write the state to a file
			log.info("State change: " + this.state + " -> " + state);
			this.state = state;
		}
	}

	public void stopPollingTask() {
		scheduledPollingFuture.cancel(true);
	}
	
	class PollingTask implements Runnable {

		private int timeout;
		private int failLimit;
		
		public PollingTask() {
			//TODO: integrate timeout with JerseyRestClient
			timeout = DhcpServerPolicies.globalPolicyAsInt(
					Property.HA_POLL_REPLY_TIMEOUT);			
			failLimit = DhcpServerPolicies.globalPolicyAsInt(
					Property.HA_POLL_REPLY_FAILURE_COUNT);
		}
		
		@Override
		public void run() {
			String status = restClient.doGet(DhcpServerStatusResource.PATH);
			if (status == null) {
				log.error("No response to status request");
				pollReplyTimeoutCnt++;
				log.warn("Poll reply timeout exceeded " + pollReplyTimeoutCnt + " times");
				if (pollReplyTimeoutCnt >= failLimit) {
					log.warn("Poll reply failure count limit reached: " + failLimit);
					setState(State.BACKUP_RUNNING);
				}
			}
			else {
				log.info("Status request response: " + status);
				if (status.equals(DhcpServerStatusService.STATUS_OK)) {
					setState(State.BACKUP_POLLING);
				}
				else {
					//TODO
				}
			}
		}
	}
}
