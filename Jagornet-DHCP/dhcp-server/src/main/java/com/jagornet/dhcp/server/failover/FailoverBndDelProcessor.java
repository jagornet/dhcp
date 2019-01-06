package com.jagornet.dhcp.server.failover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.failover.FailoverStateManager.Role;
import com.jagornet.dhcp.server.request.binding.Binding;

public class FailoverBndDelProcessor extends BaseFailoverProcessor {
	
	private static Logger log = LoggerFactory.getLogger(FailoverBndDelProcessor.class);

	protected FailoverBndDelProcessor(FailoverMessage requestMsg) {
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
		//TODO: check failover state here or in preProcess?
		Binding binding = requestMsg.getBinding();
		if (binding != null) {
			//TODO: handle errors
			serverConfig.getIaMgr().deleteIA(binding);
			replyMsg.setMessageType(FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPBNDACK);
		}
		else {
			log.error("Binding is null");
			replyMsg.setMessageType(FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPBNDNAK);
		}
		return true;	// send reply message
	}

}
