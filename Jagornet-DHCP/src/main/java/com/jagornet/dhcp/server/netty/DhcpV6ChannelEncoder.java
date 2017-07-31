/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6ChannelEncoder.java is part of Jagornet DHCP.
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

import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ByteBufferBackedChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.message.DhcpV6Message;

/**
 * Title: DhcpChannelEncoder
 * Description: The protocol encoder used by the NETTY-based DHCP server
 * when sending packets.
 * 
 * @author A. Gregory Rabil
 */
@ChannelHandler.Sharable
public class DhcpV6ChannelEncoder extends OneToOneEncoder
{
    private static Logger log = LoggerFactory.getLogger(DhcpV6ChannelEncoder.class);

    /*
     * Encode the requested DhcpMessage into a ChannelBuffer.
     * (non-Javadoc)
     * @see org.jboss.netty.handler.codec.oneone.OneToOneEncoder#encode(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.Channel, java.lang.Object)
     */
    @Override
    public Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception
    {
        if (msg instanceof DhcpV6Message) {
            DhcpV6Message dhcpMessage = (DhcpV6Message) msg;
            ByteBuffer buf = dhcpMessage.encode();
            return new ByteBufferBackedChannelBuffer(buf);
        }
        else {
            String errmsg = "Unknown message object class: " + msg.getClass();
            log.error(errmsg);
            return msg;
        }
    }
    
}
