/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV4ChannelHandler.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.server.netty;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.message.DhcpV4Message;
import com.jagornet.dhcp.server.request.DhcpV4MessageHandler;
import com.jagornet.dhcp.util.DhcpConstants;

/**
 * Title: DhcpV4ChannelHandler
 * Description: The handler used by the NETTY-based DHCPv4 server
 * for handling DhcpV4Messages.
 * 
 * @author A. Gregory Rabil
 */
@ChannelHandler.Sharable
public class DhcpV4ChannelHandler extends SimpleChannelHandler
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpV4ChannelHandler.class);

	private Channel broadcastSendChannel;
	
	
	public DhcpV4ChannelHandler(Channel broadcastSendChannel)
	{
		this.broadcastSendChannel = broadcastSendChannel;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
	 */
	@Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
    {
    	Object message = e.getMessage();
        if (message instanceof DhcpV4Message) {
            
            DhcpV4Message dhcpMessage = (DhcpV4Message) message;
            if (log.isDebugEnabled()) {
            	log.debug("Received: " + dhcpMessage.toStringWithOptions());
            }
            else {
            	log.info("Received: " + dhcpMessage.toString());
            }
            
            DhcpV4Message replyMessage = 
            	DhcpV4MessageHandler.handleMessage(dhcpMessage.getLocalAddress().getAddress(), 
            										dhcpMessage);
            
            if (replyMessage != null) {
            	if ((broadcastSendChannel != null) &&
            		(replyMessage.getRemoteAddress().getAddress().equals(DhcpConstants.ZEROADDR_V4))) {
        			if (log.isDebugEnabled())
        				log.debug("Client request received from zero address," +
        							" replying to broadcast address.");
        			replyMessage.setRemoteAddress(new InetSocketAddress(DhcpConstants.BROADCAST,
        											replyMessage.getRemoteAddress().getPort()));
        			// ensure the packet is sent on the proper broadcast interface
        			broadcastSendChannel.write(replyMessage, replyMessage.getRemoteAddress());
        		}
        		else {
        			// client request was unicast, so reply via receive channel
        			e.getChannel().write(replyMessage, replyMessage.getRemoteAddress());
        		}
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
