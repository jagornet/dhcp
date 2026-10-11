/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6ChannelDecoder.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.client;

import java.net.InetSocketAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.message.DhcpV6Message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * Title: DhcpV6ChannelDecoder
 * Description: The protocol decoder used by the NETTY-based DHCPv6 client
 * when receiving packets.
 * 
 * @author A. Gregory Rabil
 */
@ChannelHandler.Sharable
public class DhcpV6ChannelDecoder extends MessageToMessageDecoder<DatagramPacket>
{
    private static Logger log = LoggerFactory.getLogger(DhcpV6ChannelDecoder.class);

    /** The local socket address. */
    protected InetSocketAddress localSocketAddress = null;
    
    protected boolean ignoreSelfPackets = false;
    
    public DhcpV6ChannelDecoder(InetSocketAddress localSocketAddress)
    {
        this(localSocketAddress, false);
    }

    public DhcpV6ChannelDecoder(InetSocketAddress localSocketAddress, boolean ignoreSelfPackets)
    {
        this.localSocketAddress = localSocketAddress;
        this.ignoreSelfPackets = ignoreSelfPackets;
    }
    
    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket packet, List<Object> out) throws Exception
    {
        ByteBuf buf = packet.content();
        InetSocketAddress remoteSocketAddress = packet.sender();
        InetSocketAddress localAddress = (localSocketAddress != null) ? 
                localSocketAddress : (InetSocketAddress) ctx.channel().localAddress();

        DhcpV6Message dhcpMessage = 
                DhcpV6Message.decode(buf.nioBuffer(), localAddress, remoteSocketAddress);
        if (dhcpMessage != null) {
            out.add(dhcpMessage);
        }
        else {
            log.warn("Failed to decode DHCPv6 message from: " + remoteSocketAddress);
        }
    }
}