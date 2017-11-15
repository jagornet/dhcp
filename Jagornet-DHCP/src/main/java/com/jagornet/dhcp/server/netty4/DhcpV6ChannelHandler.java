/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6ChannelHandler.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.server.netty4;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.server.request.DhcpV6MessageHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

/**
 * Title: DhcpChannelHandler
 * Description: The handler used by the NETTY-based DHCP server
 * for handling DhcpMessages.
 * 
 * @author A. Gregory Rabil
 */
@ChannelHandler.Sharable
public class DhcpV6ChannelHandler extends SimpleChannelInboundHandler<DhcpV6Message>
{
	private static Logger log = LoggerFactory.getLogger(DhcpV6ChannelHandler.class);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DhcpV6Message dhcpMessage) throws Exception {
        if (log.isDebugEnabled())
        	log.debug("Received: " + dhcpMessage.toStringWithOptions());
        else
        	log.info("Received: " + dhcpMessage.toString());
        
        InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        InetAddress localAddr = ((InetSocketAddress)ctx.channel().localAddress()).getAddress();
        if (localAddr.equals(DhcpConstants.ZEROADDR_V4)) {
        	localAddr = DhcpConstants.ZEROADDR_V6;
        }
        DhcpV6Message replyMessage = 
        	DhcpV6MessageHandler.handleMessage(localAddr, dhcpMessage);
        
        if (replyMessage != null) {
        	ByteBuf buf = Unpooled.wrappedBuffer(replyMessage.encode());
        	ctx.writeAndFlush(new DatagramPacket(buf, remoteAddress));
        }
        else {
            log.warn("Null DHCP reply message returned from handler");
        }
	}    
}
