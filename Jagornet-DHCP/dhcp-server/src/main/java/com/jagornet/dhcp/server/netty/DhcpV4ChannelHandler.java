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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.server.request.DhcpV4MessageHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

/**
 * Title: DhcpV4ChannelHandler
 * Description: The handler used by the NETTY-based DHCPv4 server
 * for handling DhcpV4Messages.
 * 
 * @author A. Gregory Rabil
 */
@ChannelHandler.Sharable
public class DhcpV4ChannelHandler extends SimpleChannelInboundHandler<DhcpV4Message>
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpV4ChannelHandler.class);

	private Channel broadcastSendChannel;
	
	
	public DhcpV4ChannelHandler(Channel broadcastSendChannel)
	{
		this.broadcastSendChannel = broadcastSendChannel;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DhcpV4Message msg) throws Exception {
        if (log.isDebugEnabled()) {
        	log.debug("Received: " + msg.toStringWithOptions());
        }
        else {
        	log.info("Received: " + msg.toString());
        }
        
        DhcpV4Message replyMessage = 
        	DhcpV4MessageHandler.handleMessage(msg.getLocalAddress().getAddress(), msg);
        
        if (replyMessage != null) {
        	if ((broadcastSendChannel != null) &&
        		(replyMessage.getRemoteAddress().getAddress().equals(DhcpConstants.ZEROADDR_V4))) {
    			if (log.isDebugEnabled())
    				log.debug("Client request received from zero address," +
    							" replying to broadcast address.");
    			replyMessage.setRemoteAddress(new InetSocketAddress(DhcpConstants.BROADCAST,
    											replyMessage.getRemoteAddress().getPort()));
    			// ensure the packet is sent on the proper broadcast interface
    			ByteBuf buf = Unpooled.wrappedBuffer(replyMessage.encode());
    			broadcastSendChannel.writeAndFlush(new DatagramPacket(buf, replyMessage.getRemoteAddress()));
    		}
    		else {
    			// client request was unicast, so reply via receive channel
    			ByteBuf buf = Unpooled.wrappedBuffer(replyMessage.encode());
    			ctx.writeAndFlush(new DatagramPacket(buf, replyMessage.getRemoteAddress()));
    		}
        }
        else {
        	// don't log a warning for release, which has no reply message
        	if (!(msg.getMessageType() == DhcpConstants.V4MESSAGE_TYPE_RELEASE)) {
        		log.warn("Null DHCP reply message returned from handler");
        	}
        }
	}
    
}
