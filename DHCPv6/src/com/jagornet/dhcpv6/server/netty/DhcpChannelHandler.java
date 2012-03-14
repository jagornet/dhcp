/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpChannelHandler.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.server.netty;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.server.request.DhcpMessageHandler;

/**
 * Title: DhcpChannelHandler
 * Description: The handler used by the NETTY-based DHCP server
 * for handling DhcpMessages.
 * 
 * @author A. Gregory Rabil
 */
@ChannelHandler.Sharable
public class DhcpChannelHandler extends SimpleChannelHandler
{
	private static Logger log = LoggerFactory.getLogger(DhcpChannelHandler.class);

	/*
	 * (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
	 */
	@Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
    {
    	Object message = e.getMessage();
        if (message instanceof DhcpMessage) {
            
            DhcpMessage dhcpMessage = (DhcpMessage) message;
            if (log.isDebugEnabled())
            	log.debug("Received: " + dhcpMessage.toStringWithOptions());
            else
            	log.info("Received: " + dhcpMessage.toString());
            
            SocketAddress remoteAddress = e.getRemoteAddress();
            InetAddress localAddr = ((InetSocketAddress)e.getChannel().getLocalAddress()).getAddress(); 
            DhcpMessage replyMessage = 
            	DhcpMessageHandler.handleMessage(localAddr, dhcpMessage);
            
            if (replyMessage != null) {
            	e.getChannel().write(replyMessage, remoteAddress);
            }
            else {
                log.warn("Null DHCP reply message returned from handler");
            }
            
        }
        else {
            // Note: in theory, we can't get here, because the
            // codec would have thrown an exception beforehand
            log.error("Received unknown message object: " + message.getClass());
        }
    }
    
}
