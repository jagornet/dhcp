/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6UnicastChannelDecoder.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.server.netty4;

import java.net.InetSocketAddress;
import java.util.List;

import com.jagornet.dhcp.core.message.DhcpV6Message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * The Class DhcpUnicastChannelDecoder.
 */
public class DhcpV6UnicastChannelDecoder extends DhcpV6ChannelDecoder 
{
	
	/**
	 * Instantiates a new dhcp unicast channel decoder.
	 *
	 * @param localSocketAddress the local socket address
	 */
	public DhcpV6UnicastChannelDecoder(InetSocketAddress localSocketAddress, boolean ignoreSelfPackets)
	{
		super(localSocketAddress, ignoreSelfPackets);
	}
    
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        DhcpV6Message dhcpMessage = 
        	DhcpV6Message.decode(buf.nioBuffer(), localSocketAddress, remoteSocketAddress);
        dhcpMessage.setUnicast(true);
        out.add(dhcpMessage);
    }
}
