/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.netty;

import java.net.InetSocketAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.core.util.Util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Title: DhcpV4UnicastChannelDecoder
 * Description: A specialized DHCPv4 message decoder used by the NETTY-based 
 * DHCPv4 server when receiving packets in order to set the unicast flag in
 * the DhcpV4Message for proper protocol processing.  This class is also used 
 * distinguish the unicast channel in order to workaround duplicate messages 
 * received when running on Windows.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4UnicastChannelDecoder extends DhcpV4ChannelDecoder 
{	   
    private static Logger log = LoggerFactory.getLogger(DhcpV4UnicastChannelDecoder.class);
	
	public DhcpV4UnicastChannelDecoder(InetSocketAddress localSocketAddress, boolean ignoreSelfPackets)
	{
		super(localSocketAddress, ignoreSelfPackets, null);
	}

	@Override
	public boolean acceptInboundMessage(Object obj) throws Exception {
		boolean accept = super.acceptInboundMessage(obj);
		if (accept) {
	    	if (remoteSocketAddress.getAddress().equals(DhcpConstants.ZEROADDR_V4)) {
	        	// can't unicast to 0.0.0.0, so a broadcast channel is needed
	        	// this is a workaround for Windows implementation which will
	        	// see duplicate packets on the unicast and broadcast channels
	        	log.debug("Ignoring packet from 0.0.0.0 received on unicast channel");
	        	accept = false;
	    	}
		}
		return accept;
	}
    
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
		// remoteSocketAddress is set by DhcpV4PacketDecoder.channelRead
		log.info("Decoding unicast message:" +
				 " local=" + Util.socketAddressAsString(localSocketAddress) + 
				 " remote=" + Util.socketAddressAsString(remoteSocketAddress));
		// Netty's ByteBuf.nioBuffer() returns the underlying ByteBuffer, 
		// without additional allocations, so should not be necessary 
		// to have a NettyDhcpV4Message.decode() for Netty's ByteBuf
        DhcpV4Message dhcpMessage = 
        	DhcpV4Message.decode(buf.nioBuffer(), localSocketAddress, remoteSocketAddress);
        // DHCP message received via unicast
        dhcpMessage.setUnicast(true);
        log.info("Unicast message decoded: msg=" + dhcpMessage);
        out.add(dhcpMessage);
    }
}
