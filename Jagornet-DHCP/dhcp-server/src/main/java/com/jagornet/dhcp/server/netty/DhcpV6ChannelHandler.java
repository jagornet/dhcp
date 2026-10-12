/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.netty;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.request.DhcpV6MessageHandler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.SimpleChannelInboundHandler;

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

	private Channel outboundChannel;
	
	public DhcpV6ChannelHandler(Channel outboundChannel)
	{
		this.outboundChannel = outboundChannel;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DhcpV6Message dhcpMessage) throws Exception {
        if (log.isDebugEnabled()) {
        	log.debug("Received: " + dhcpMessage.toStringWithOptions());
        }
        else {
        	log.info("Received: " + dhcpMessage.toString());
        }
        
//        InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        InetSocketAddress remoteAddress = dhcpMessage.getRemoteAddress();
//        InetAddress localAddr = ((InetSocketAddress)ctx.channel().localAddress()).getAddress();
        InetAddress localAddr = dhcpMessage.getLocalAddress().getAddress();
        if (localAddr.equals(DhcpConstants.ZEROADDR_V4)) {
        	localAddr = DhcpConstants.ZEROADDR_V6;
        }
        
        DhcpV6Message replyMessage = 
        	DhcpV6MessageHandler.handleMessage(localAddr, dhcpMessage);
        
        if (replyMessage != null) {
        	if (log.isDebugEnabled()) {
        		log.debug("Sending: " + replyMessage.toStringWithOptions());
        	}
        	else {
        		log.info("Sending: " + replyMessage.toString());
        	}
			ChannelFuture future = outboundChannel.writeAndFlush(
					new DefaultAddressedEnvelope<DhcpV6Message, SocketAddress>(
							replyMessage, replyMessage.getRemoteAddress()));
			future.addListener(new ChannelFutureListener() {
				public void operationComplete(ChannelFuture future) {
					if (log.isDebugEnabled()) {
						log.debug("Channel: " +
								Util.socketAddressAsString(
										(InetSocketAddress)future.channel().localAddress()) +
								" write/flush operation complete: success=" + future.isSuccess());
					}
					if (replyMessage instanceof NettyDhcpV6Message) {
			        	// release message resources, which basically means 
			        	// releasing the byte buffer back to the buffer pool
			        	((NettyDhcpV6Message)replyMessage).release();
					}
				}
			});
        }
        else {
            log.warn("Null DHCP reply message returned from handler");
        }
	}    
}
