package com.jagornet.dhcp.server.failover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.failover.FailoverStateManager.Role;
import com.jagornet.dhcp.server.failover.FailoverStateManager.State;

public class FailoverCtlReqProcessor extends BaseFailoverProcessor {
	
	private static Logger log = LoggerFactory.getLogger(FailoverCtlReqProcessor.class);

	protected FailoverCtlReqProcessor(FailoverMessage requestMsg) {
		super(requestMsg);
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
		//TODO: what if the message can't be sent for some reason, need better state management
		fsm.setState(State.BACKUP_RETURNING_CONTROL);
		replyMsg.setMessageType(FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPCTLRET);
		return true;	// send reply message
	}

}
