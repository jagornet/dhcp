/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV4MessageHandler.java is part of Jagornet DHCP.
 *
 *   Jagornet DHCP is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Jagornet DHCP is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Jagornet DHCP.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcp.server.failover;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Title: FailoverMessageHandler
 * Description: The main failover message handler class.
 * 
 * @author A. Gregory Rabil
 */
public class FailoverMessageHandler
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(FailoverMessageHandler.class);
	
    public static FailoverMessage handleMessage(InetSocketAddress remoteAddress,
    		InetAddress localAddress, FailoverMessage failoverMessage)
    {
    	FailoverMessageProcessor processor = null;
		switch (failoverMessage.getMessageType()) {
			case FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPBNDADD:
				processor = new FailoverBndAddProcessor(failoverMessage);
				break;
			case FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPBNDDEL:
				processor = new FailoverBndDelProcessor(failoverMessage);
				break;
			case FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPBNDUPD:
				processor = new FailoverBndUpdProcessor(failoverMessage);
				break;
			case FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPBNDACK:
				processor = new FailoverBndAckProcessor(failoverMessage);
				break;
			case FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPBNDNAK:
				processor = new FailoverBndNakProcessor(failoverMessage);
				break;
			case FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPCTLREQ:
				processor = new FailoverCtlReqProcessor(failoverMessage);
				break;
			case FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPCTLRET:
				processor = new FailoverCtlRetProcessor(failoverMessage);
				break;
			case FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPCTLACK:
				processor = new FailoverCtlAckProcessor(failoverMessage);
				break;
			case FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPPOLL:
				processor = new FailoverPollProcessor(failoverMessage);
				break;
			case FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPPRPL:
				processor = new FailoverPollReplyProcessor(failoverMessage);
				break;
			case FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCP_BULK_BINDING_CHANGES:
				processor = new FailoverBulkBindingChangesProcessor(failoverMessage);
				break;
			case FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCP_BULK_BINDING_ACKS:
				processor = new FailoverBulkBindingAcksProcessor(failoverMessage);
				break;
	        default:
	            log.error("Unknown message type.");
	            break;
		}
    	if (processor != null) {
    		return processor.processMessage();
    	}
    	else {
    		log.error("No processor found for message type: " + failoverMessage.getMessageType());
    	}
		return null;
	}
    
}
