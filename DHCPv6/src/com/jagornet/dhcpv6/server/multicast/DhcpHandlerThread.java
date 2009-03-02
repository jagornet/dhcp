/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpHandlerThread.java is part of DHCPv6.
 *
 *   DHCPv6 is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   DHCPv6 is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with DHCPv6.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcpv6.server.multicast;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.server.request.DhcpMessageHandler;

/**
 * Title: DhcpInfoRequest
 * Description: The handler thread for INFO_REQUEST messages.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpHandlerThread implements Runnable
{
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpHandlerThread.class);

	/** The request message. */
	private final DhcpMessage requestMessage;
 
    /**
     * Construct an DhcpInfoRequest handler.
     * 
     * @param reqMsg the Info-Request DhcpMessage
     */
    public DhcpHandlerThread(DhcpMessage reqMsg)
    {
    	this.requestMessage = reqMsg;
    }

    /**
     * Handle the client request.  Find appropriate configuration based on any
     * criteria in the request message that can be matched against the server's
     * configuration, then formulate a response message containing the options
     * to be sent to the client.
     */
    public void run()
    {
    	InetSocketAddress local = requestMessage.getLocalAddress();
    	InetSocketAddress remote = requestMessage.getRemoteAddress();
    	
    	Thread.currentThread().setName(Thread.currentThread().getName() + 
    			" (" + remote.getAddress().getHostAddress() + ")");
    	
       	DhcpServerSocket dhcpServerSocket = MulticastDhcpServer.getDhcpServerSocket(local);
       	if (dhcpServerSocket != null) {       		
	        DhcpMessage replyMessage = DhcpMessageHandler.handleMessage(
	        			dhcpServerSocket.getLocalAddress().getAddress(),
	        			requestMessage);
	        if (replyMessage != null) {
	            log.info("Sending DHCP reply message");
	            dhcpServerSocket.sendMessage(replyMessage);
	        }
	        else {
	            log.warn("Null DHCP reply message returned from processor");
	        }
       	}
       	else {
       		log.error("Failed to locate DhcpServerSocket for localAddr=" +
       				  requestMessage.getLocalAddress());
       	}
    }
}
