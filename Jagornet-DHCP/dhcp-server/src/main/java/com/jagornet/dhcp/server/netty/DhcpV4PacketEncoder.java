/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV4PacketDecoder.java is part of Jagornet DHCP.
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
