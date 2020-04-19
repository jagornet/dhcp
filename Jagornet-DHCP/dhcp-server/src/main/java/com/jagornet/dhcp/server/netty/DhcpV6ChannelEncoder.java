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
import java.util.List;

import com.jagornet.dhcp.core.message.DhcpV6Message;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

/**
 * Title: DhcpChannelEncoder
 * Description: The protocol encoder used by the NETTY-based DHCP server
 * when sending packets.
 * 
 * @author A. Gregory Rabil
 */
@ChannelHandler.Sharable
public class DhcpV6ChannelEncoder extends MessageToMessageEncoder<DhcpV6Message>
{
	@Override
	protected void encode(ChannelHandlerContext ctx, DhcpV6Message msg, List<Object> out) throws Exception {
        ByteBuffer buf = msg.encode();
        out.add(Unpooled.wrappedBuffer(buf));
	}   
}
