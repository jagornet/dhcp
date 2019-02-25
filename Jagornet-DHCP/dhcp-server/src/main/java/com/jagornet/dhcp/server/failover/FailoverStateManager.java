package com.jagornet.dhcp.server.failover;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;

import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.DatagramChannel;

public class FailoverStateManager implements Runnable {

	private static Logger log = LoggerFactory.getLogger(FailoverStateManager.class);
	
	public enum Role { NOT_CONFIGURED, PRIMARY, BACKUP };
	
	public enum State { INIT,
						PRIMARY_RUNNING,
						PRIMARY_AWAITING_BNDACK,
						PRIMARY_REQUESTING_CONTROL,
						PRIMARY_RECEIVING_UPDATES,
						BACKUP_POLLING,
						BACKUP_AWAITING_POLL_REPLY,
						BACKUP_POLLING_FAILURE,
						BACKUP_RUNNING,
						BACKUP_RETURNING_CONTROL,
						BACKUP_BULK_BINDINGS_SENT };
						
	/*
	public enum PrimaryStates { INIT, 
								RUNNING, 
								REQUESTING_CONTROL, 
								RECEIVING_UPDATES };
								
	public enum BackupStates { INIT,
							   POLLING,
							   POLLING_FAILURE, 
							   RUNNING,
							   DHCP_BULK_BINDINGS_SENT };
	*/
						 
	private Role role = Role.NOT_CONFIGURED;
	private State state = State.INIT;
	private DatagramChannel channel = null;
	private InetSocketAddress peerAddress = null;

	private ControlTask controlTask = null;
	private int ctlRetTimeoutCnt = 0;
	
	private ScheduledExecutorService pollingTaskExecutor = null;
	private ScheduledFuture<?> scheduledPollingFuture = null;
	private PollingTask pollingTask = null;
	private int pollReplyTimeoutCnt = 0;
	
    public FailoverStateManager(Role role) {
    	this.role = role;
    	this.state = State.INIT;
    	controlTask = new ControlTask();
    	pollingTaskExecutor = Executors.newSingleThreadScheduledExecutor();
    	pollingTask = new PollingTask();
    }
    
    public Role getRole() {
    	return role;
    }
    
    public void setRole(Role role) {
    	this.role = role;
    }

	public State getState() {
		return state;
	}

	public void setState(State state) {
		log.info("State change from=" + this.state + " to" + state);
		this.state = state;
	}

	public DatagramChannel getChannel() {
		return channel;
	}

	public void setChannel(DatagramChannel channel) {
		this.channel = channel;
	}

	public InetSocketAddress getPeerAddress() {
		return peerAddress;
	}

	public void setPeerAddress(InetSocketAddress peerAddress) {
		this.peerAddress = peerAddress;
	}

	@Override
	public void run() {
		if (role == Role.PRIMARY) {
			requestControl();
		}
		else if (role == Role.BACKUP) {
			startPollingTask();
		}
		else {
			log.error("Unknown Failover Role!");
		}	
	}
	
	public void requestControl() {
		// synchronous task
		controlTask.run();
	}
	
	public void receivedCtlRet() {
		controlTask.notifyAll();
	}
	
	class ControlTask implements Runnable {

		private int timeout;
		private int failLimit;
		
		public ControlTask() {
			timeout = DhcpServerPolicies.globalPolicyAsInt(
					Property.FAILOVER_CONTROL_RETURN_TIMEOUT);			
			failLimit = DhcpServerPolicies.globalPolicyAsInt(
					Property.FAILOVER_CONTROL_RETURN_FAILURE_COUNT);
		}

		@Override
		public void run() {
			FailoverMessage ctlreqMessage = 
					new FailoverMessage(channel.localAddress(), peerAddress);
			ctlreqMessage.setMessageType(FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPCTLREQ);
			try {
				ChannelFuture future = channel.writeAndFlush(ctlreqMessage);
				future.await();
				if (future.isSuccess()) {
					waitForCtlRet();
				}
				else {
					log.error("Failed to send ctlreq message");
				}
			}
			catch (Exception ex) {
				log.error("Exception sending ctlreq message: " + ex);
			}
		}
		
		private synchronized void waitForCtlRet() throws InterruptedException {
			setState(State.PRIMARY_REQUESTING_CONTROL);
			log.info("Waiting " + timeout + " seconds for control return...");
			this.wait(timeout*1000);	// milliseconds
			// wait expired, increment failed counter
			ctlRetTimeoutCnt++;
			log.warn("Control return timeout exceeded " + ctlRetTimeoutCnt + " times");
			if (ctlRetTimeoutCnt >= failLimit) {
				log.warn("Control return failure count limit reached: " + failLimit);
				setState(State.PRIMARY_RUNNING);
			}
			else {
				run();	// try again
			}
		}
	}
	
	public void startPollingTask() {
		int pollSeconds = DhcpServerPolicies.globalPolicyAsInt(
				Property.FAILOVER_POLL_SECONDS);
		// TODO: consider scheduleWithFixedDelay?
		scheduledPollingFuture = pollingTaskExecutor.scheduleAtFixedRate(
				pollingTask, 0, pollSeconds, TimeUnit.SECONDS);
	}
	
	public void stopPollingTask() {
		scheduledPollingFuture.cancel(true);
	}
	
	public void receivedPollReply() {
		pollingTask.notifyAll();
	}
	
	class PollingTask implements Runnable {

		private int timeout;
		private int failLimit;
		
		public PollingTask() {
			timeout = DhcpServerPolicies.globalPolicyAsInt(
					Property.FAILOVER_POLL_REPLY_TIMEOUT);			
			failLimit = DhcpServerPolicies.globalPolicyAsInt(
					Property.FAILOVER_POLL_REPLY_FAILURE_COUNT);
		}
		
		@Override
		public void run() {
			FailoverMessage pollMessage = 
					new FailoverMessage(channel.localAddress(), peerAddress);
			pollMessage.setMessageType(FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPPOLL);
			try {
				ChannelFuture future = channel.writeAndFlush(pollMessage);
				future.await();
				if (future.isSuccess()) {
					waitForPollReply();
				}
				else {
					log.error("Failed to send poll message");
				}
			}
			catch (Exception ex) {
				log.error("Exception running polling task: " + ex);
			}
		}
		
		private synchronized void waitForPollReply() throws InterruptedException {
			setState(State.BACKUP_AWAITING_POLL_REPLY);
			log.info("Waiting " + timeout + " seconds for poll reply...");
			this.wait(timeout*1000);	// milliseconds
			// wait expired, increment failed counter
			pollReplyTimeoutCnt++;
			log.warn("Poll reply timeout exceeded " + pollReplyTimeoutCnt + " times");
			if (pollReplyTimeoutCnt >= failLimit) {
				log.warn("Poll reply failure count limit reached: " + failLimit);
				setState(State.BACKUP_RUNNING);
			}
		}
	}
}
