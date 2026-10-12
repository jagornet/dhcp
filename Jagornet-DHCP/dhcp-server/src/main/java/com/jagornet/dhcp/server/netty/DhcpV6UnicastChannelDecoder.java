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

import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.util.Util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Title: DhcpV6UnicastChannelDecoder
 * Description: A specialized DHCPv6 message decoder used by the NETTY-based 
 * DHCPv6 server when receiving packets in order to set the unicast flag in
 * the DhcpV6Message for proper protocol processing.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6UnicastChannelDecoder extends DhcpV6ChannelDecoder 
{
    private static Logger log = LoggerFactory.getLogger(DhcpV6UnicastChannelDecoder.class);

    public DhcpV6UnicastChannelDecoder(InetSocketAddress localSocketAddress, boolean ignoreSelfPackets)
	{
		super(localSocketAddress, ignoreSelfPackets);
	}
    
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
		log.info("Decoding unicast message:" +
				 " local=" + Util.socketAddressAsString(localSocketAddress) + 
				 " remote=" + Util.socketAddressAsString(remoteSocketAddress));
		// Netty's ByteBuf.nioBuffer() returns the underlying ByteBuffer, 
		// without additional allocations, so should not be necessary 
		// to have a NettyDhcpV4Message.decode() for Netty's ByteBuf
        DhcpV6Message dhcpMessage = 
        	DhcpV6Message.decode(buf.nioBuffer(), localSocketAddress, remoteSocketAddress);
        // DHCP message received via unicast
        dhcpMessage.setUnicast(true);
        log.info("Unicast message decoded: msg=" + dhcpMessage);
        out.add(dhcpMessage);
    }
}
