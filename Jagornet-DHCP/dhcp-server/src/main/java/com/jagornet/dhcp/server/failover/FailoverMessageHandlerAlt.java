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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Title: DhcpV4MessageHandler
 * Description: The main DHCPv4 message handler class.
 * 
 * @author A. Gregory Rabil
 */
public class FailoverMessageHandlerAlt
{
	private static Logger log = LoggerFactory.getLogger(FailoverMessageHandlerAlt.class);
	
	//TODO
	private static String role = FailoverConstants.ROLE_PRIMARY;
	private static BackupFSM.States backupState = BackupFSM.States.NONE;
	
    public static FailoverMessage handleMessage(InetAddress localAddress, FailoverMessage failoverMessage)
    {
		FailoverMessage replyMessage = null;
		short msgType = failoverMessage.getMessageType();
		if (FailoverConstants.ROLE_PRIMARY.equals(role)) {
			replyMessage = handlePrimaryFailoverMessage(msgType, failoverMessage);
		}
		else if (FailoverConstants.ROLE_BACKUP.equals(role)) {
			replyMessage = handleBackupFailoverMessage(msgType, failoverMessage);
		}
		else {
			if ((role == null) || role.isEmpty()) {
				log.warn("Received failover message, but not configured for a failover role");
			}
			else {
				log.error("Unknown failover role: " + role);
			}
		}
		return replyMessage;
	}

	private static FailoverMessage handleBackupFailoverMessage(short msgType,
															   FailoverMessage failoverMessage) {
		return BackupFSM.handleBackupFailoverMessage(msgType, failoverMessage);
	}

	private static FailoverMessage handlePrimaryFailoverMessage(short msgType,
																FailoverMessage failoverMessage) {
		// TODO Auto-generated method stub
		return null;
	}
    
}
