package com.jagornet.dhcp.server.failover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.failover.FailoverStateManager.Role;

public class FailoverCtlAckProcessor extends BaseFailoverProcessor {
	
	private static Logger log = LoggerFactory.getLogger(FailoverCtlAckProcessor.class);

	protected FailoverCtlAckProcessor(FailoverMessage requestMsg) {
		super(requestMsg);
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
		// TODO Auto-generated method stub
		return false;
	}

}
