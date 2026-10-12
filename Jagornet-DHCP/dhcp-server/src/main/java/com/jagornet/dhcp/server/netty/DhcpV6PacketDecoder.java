/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.netty;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.util.Util;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.DatagramPacketDecoder;

/**
 * Title: DhcpV6PacketDecoder
 * Description: The datagram packet decoder used by the NETTY-based DHCPv6 server
 * when receiving packets.  This is the first handler in the inbound pipeline.
 * This class does not really decode the packet, but is used to get information about
 * the datagram packet (notably the remote socket address) that is useful when decoding
 * the message in the channel decoder, which is next in the inbound pipeline.
 * 
 * @author A. Gregory Rabil
 */
@ChannelHandler.Sharable
public class DhcpV6PacketDecoder extends DatagramPacketDecoder
{
    private static Logger log = LoggerFactory.getLogger(DhcpV6PacketDecoder.class);

    DhcpV6ChannelDecoder dhcpV6ChannelDecoder;

    public DhcpV6PacketDecoder(DhcpV6ChannelDecoder dhcpV6ChannelDecoder)
    {
    	super(dhcpV6ChannelDecoder);
    	this.dhcpV6ChannelDecoder = dhcpV6ChannelDecoder;
    }
    
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
		if (obj instanceof DatagramPacket) {
			InetSocketAddress localChannel = (InetSocketAddress)ctx.channel().localAddress();
			InetSocketAddress remoteSender = ((DatagramPacket) obj).sender();
			log.debug("Received datagram packet:" +
					  " localChannel=" + Util.socketAddressAsString(localChannel) + 
					  " remoteSender=" + Util.socketAddressAsString(remoteSender));
			// set the remote sender's socket address into the channel decoder
			dhcpV6ChannelDecoder.setRemoteSocketAddress(remoteSender);
			// send the packet to the next handler in the inbound pipeline, which invokes
			// the acceptInboundMessage, and if accepted, the decode method of the 
			// dhcpV6[Unicast]ChannelDecoder supplied in the constructor
			super.channelRead(ctx, obj);
		}
	}
	
}
