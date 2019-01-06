package com.jagornet.dhcp.server.failover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.failover.FailoverStateManager.Role;

public class FailoverPollProcessor extends BaseFailoverProcessor {

	private static Logger log = LoggerFactory.getLogger(FailoverPollProcessor.class);
	
	public FailoverPollProcessor(FailoverMessage failoverMessage) {
		super(failoverMessage);
	}

	@Override
	public boolean preProcess() {
		if (!super.preProcess()) {
			return false;
		}
		if (!fsm.getRole().equals(Role.PRIMARY)) {
			log.error("Failover role is not " + Role.PRIMARY);
			return false;
		}
		return true;
	}
	
	@Override
	public boolean process() {
		// just reply to the poll for now
		// TODO: integrate with FSM?
		replyMsg.setMessageType(FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPPRPL);
		return true;	// send reply message
	}
}
