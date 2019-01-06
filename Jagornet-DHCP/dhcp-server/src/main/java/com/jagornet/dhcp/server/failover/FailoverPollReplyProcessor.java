package com.jagornet.dhcp.server.failover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.failover.FailoverStateManager.Role;

public class FailoverPollReplyProcessor extends BaseFailoverProcessor {
	
	private static Logger log = LoggerFactory.getLogger(FailoverPollReplyProcessor.class);

	public FailoverPollReplyProcessor(FailoverMessage failoverMessage) {
		super(failoverMessage);
	}

	@Override
	public boolean preProcess() {
		if (!super.preProcess()) {
			return false;
		}
		if (!fsm.getRole().equals(Role.BACKUP)) {
			log.error("Failover role is not " + Role.BACKUP);
			return false;
		}
		return true;
	}

	@Override
	public boolean process() {
		fsm.receivedPollReply();
		return false;	// nothing to return to peer, just continue polling
	}

}
