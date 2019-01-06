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
package com.jagornet.dhcp.server.netty4;

import java.net.InetSocketAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.server.JagornetDhcpServer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * Title: DhcpChannelDecoder
 * Description: The protocol decoder used by the NETTY-based DHCP server
 * when receiving packets.
 * 
 * @author A. Gregory Rabil
 */
@ChannelHandler.Sharable
public class DhcpV6ChannelDecoder extends MessageToMessageDecoder<ByteBuf>
{
    private static Logger log = LoggerFactory.getLogger(DhcpV6ChannelDecoder.class);

    /** The local socket address. */
    protected InetSocketAddress localSocketAddress = null;
    
    /** The remote socket address. */
    protected InetSocketAddress remoteSocketAddress = null;
    
    protected boolean ignoreSelfPackets;
    
    public DhcpV6ChannelDecoder(InetSocketAddress localSocketAddress, boolean ignoreSelfPackets)
    {
    	this.localSocketAddress = localSocketAddress;
    	this.ignoreSelfPackets = ignoreSelfPackets;
    }
    
    @Override
    public boolean acceptInboundMessage(Object buf) throws Exception {
    	if (ignoreSelfPackets) {
	    	if (JagornetDhcpServer.getAllIPv6Addrs().contains(remoteSocketAddress.getAddress())) {
	    		log.debug("Ignoring packet from self: address=" + 
	    					remoteSocketAddress.getAddress());
	    		return false;
	    	}
    	}
    	return true;
    }
    
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        DhcpV6Message dhcpMessage = 
        	DhcpV6Message.decode(buf.nioBuffer(), localSocketAddress,
        						 (InetSocketAddress) ctx.channel().remoteAddress());
        out.add(dhcpMessage);
    }

	public InetSocketAddress getLocalSocketAddress() {
		return localSocketAddress;
	}

	public void setLocalSocketAddress(InetSocketAddress localSocketAddress) {
		this.localSocketAddress = localSocketAddress;
	}

	public InetSocketAddress getRemoteSocketAddress() {
		return remoteSocketAddress;
	}

	public void setRemoteSocketAddress(InetSocketAddress remoteSocketAddress) {
		this.remoteSocketAddress = remoteSocketAddress;
	}

	public boolean isIgnoreSelfPackets() {
		return ignoreSelfPackets;
	}

	public void setIgnoreSelfPackets(boolean ignoreSelfPackets) {
		this.ignoreSelfPackets = ignoreSelfPackets;
	}
}
