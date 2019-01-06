package com.jagornet.dhcp.server.failover;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;

public class BackupFSM {
	
	public enum States { NONE,
						 POLLING,
						 POLLING_FAILURE, 
						 RUNNING,
						 DHCP_BULK_BINDINGS_SENT };

	private static States state = States.NONE;
	
	private static int unansweredPolls = 0;
	
	public static void start() {
		unansweredPolls = 0;
		startPollTimer(0);
	}
	
	public static FailoverMessage handleBackupFailoverMessage(short msgType,
															   FailoverMessage failoverMessage) {
		FailoverMessage replyMessage = null;
		switch (msgType) {
			case FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPPRPL:
				if (state == States.POLLING) {
					unansweredPolls = 0;
//					startPollTimer();
				}
				else {
					
				}
				break;
				
			case FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPBNDADD:
			case FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPBNDUPD:
			case FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPBNDDEL:
				unansweredPolls = 0;
//				handleBindingChange();
				break;
				
			case FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPCTLREQ:
//				initiateControlHandover();
				break;
				
			default:
				//TODO: error
		}
		return replyMessage;
	}
	
	public static void startPollTimer(int delay) {
	    TimerTask pollTask = new TimerTask() {
	        public void run() {
	            
	        }
	    };
	    
	    Timer timer = new Timer("Polling Timer");	     
	    timer.schedule(pollTask, delay);
		
	}

}
