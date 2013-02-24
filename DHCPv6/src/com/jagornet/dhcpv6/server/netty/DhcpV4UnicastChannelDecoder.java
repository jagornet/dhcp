/*
 * Copyright 2011 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpUnicastChannelDecoder.java is part of DHCPv6.
 *
 *   DHCPv6 is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   DHCPv6 is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with DHCPv6.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcpv6.server.netty;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpV4Message;
import com.jagornet.dhcpv6.util.DhcpConstants;

/**
 * The Class DhcpUnicastChannelDecoder.
 */
public class DhcpV4UnicastChannelDecoder extends DhcpV4ChannelDecoder 
{
	   
    /** The log. */
    private static Logger log = LoggerFactory.getLogger(DhcpV4UnicastChannelDecoder.class);
	
	/**
	 * Instantiates a new dhcp unicast channel decoder.
	 *
	 * @param localSocketAddress the local socket address
	 */
	public DhcpV4UnicastChannelDecoder(InetSocketAddress localSocketAddress, boolean ignoreSelfPackets)
	{
		super(localSocketAddress, ignoreSelfPackets);
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.netty.DhcpChannelDecoder#decode(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.Channel, java.lang.Object)
	 */
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception
	{
    	if (remoteSocketAddress.getAddress().equals(DhcpConstants.ZEROADDR)) {
        	// can't unicast to 0.0.0.0, so a broadcast channel is needed
        	// this is a workaround for Windows implementation which will
        	// see duplicate packets on the unicast and broadcast channels
        	log.debug("Ignoring packet from 0.0.0.0 received on unicast channel");
        	return null;
    	}
    	
		Object obj = super.decode(ctx, channel, msg);
		if (obj instanceof DhcpV4Message) {
			// this decoder is in the pipeline for unicast
			// channels only, so this must be a unicast packet
			((DhcpV4Message)obj).setUnicast(true);
		}
		return obj;
	}
}
