/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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