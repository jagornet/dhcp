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

import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.util.Util;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.DatagramPacketEncoder;

/**
 * Title: DhcpV6PacketEncoder
 * Description: The protocol encode used by the NETTY-based DHCPv6 server
 * when receiving packets.
 * 
 * @author A. Gregory Rabil
 */
@ChannelHandler.Sharable
public class DhcpV6PacketEncoder extends DatagramPacketEncoder<DhcpV6Message>
{
    private static Logger log = LoggerFactory.getLogger(DhcpV6PacketEncoder.class);

    DhcpV6ChannelEncoder dhcpV6ChannelEncoder;

    public DhcpV6PacketEncoder(DhcpV6ChannelEncoder dhcpV6ChannelEncoder)
    {
    	super(dhcpV6ChannelEncoder);
    	this.dhcpV6ChannelEncoder = dhcpV6ChannelEncoder;
    }
    
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    	log.debug("Sending datagram packet on localChannel: " + 
  			  Util.socketAddressAsString((InetSocketAddress)ctx.channel().localAddress()));
		// send the message to the next handler in the outbound pipeline, which invokes 
    	// the encode method of the dhcpV6ChannelEncoder supplied in the constructor
    	super.write(ctx, msg, promise);
    }
}
