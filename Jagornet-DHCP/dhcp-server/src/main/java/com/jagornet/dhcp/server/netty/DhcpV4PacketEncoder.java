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

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.util.Util;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.DatagramPacketEncoder;

/**
 * Title: DhcpV4PacketEncoder
 * Description: The protocol encoder used by the NETTY-based DHCPv4 server
 * when sending packets.
 * 
 * @author A. Gregory Rabil
 */
@ChannelHandler.Sharable
public class DhcpV4PacketEncoder extends DatagramPacketEncoder<DhcpV4Message>
{
    private static Logger log = LoggerFactory.getLogger(DhcpV4PacketEncoder.class);

    DhcpV4ChannelEncoder dhcpV4ChannelEncoder;

    public DhcpV4PacketEncoder(DhcpV4ChannelEncoder dhcpV4ChannelEncoder)
    {
    	super(dhcpV4ChannelEncoder);
    	this.dhcpV4ChannelEncoder = dhcpV4ChannelEncoder;
    }
    
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    	log.debug("Sending datagram packet on localChannel: " + 
    			  Util.socketAddressAsString((InetSocketAddress)ctx.channel().localAddress()));
    	// send the message to the next handler in the outbound pipeline, which invokes 
    	// the encode method of the dhcpV4ChannelEncoder supplied in the constructor
    	super.write(ctx, msg, promise);
    }
}
