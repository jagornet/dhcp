/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV4ChannelDecoder.java is part of Jagornet DHCP.
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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.server.JagornetDhcpServer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * Title: DhcpV4ChannelDecoder
 * Description: The protocol decoder used by the NETTY-based DHCPv4 server
 * when receiving packets.
 * 
 * @author A. Gregory Rabil
 */
@ChannelHandler.Sharable
public class DhcpV4ChannelDecoder extends MessageToMessageDecoder<ByteBuf>
{
    /** The log. */
    private static Logger log = LoggerFactory.getLogger(DhcpV4ChannelDecoder.class);

    /** The local socket address. */
    protected InetSocketAddress localSocketAddress = null;
    
    /** The remote socket address. */
    protected InetSocketAddress remoteSocketAddress = null;
    
    protected boolean ignoreSelfPackets;
    
    public DhcpV4ChannelDecoder(InetSocketAddress localSocketAddress, boolean ignoreSelfPackets)
    {
    	this.localSocketAddress = localSocketAddress;
    	this.ignoreSelfPackets = ignoreSelfPackets;
    }
    
    @Override
    public boolean acceptInboundMessage(Object obj) throws Exception {
    	boolean accept = super.acceptInboundMessage(obj);
    	if (accept) {
	    	if (DhcpConstants.IS_WINDOWS &&
	        		!(this instanceof DhcpV4UnicastChannelDecoder) &&
	        		!remoteSocketAddress.getAddress().equals(DhcpConstants.ZEROADDR_V4)) {
	        	// we ignore packets from 0.0.0.0 in the DhcpV4UnicastChannelDecoder, so if this is NOT
	        	// that decoder, then this is the broadcast decoder, in which case we want to ignore
	        	// packets that are NOT from 0.0.0.0.  This is to workaround Windows implementation which
	        	// will see duplicate packets on the unicast and broadcast channels
	        	log.debug("Ignoring packet from " + 
	        				remoteSocketAddress.getAddress().getHostAddress() + 
	        				" received on broadcast channel");
	        	accept = false;
	    	}
	    	
	    	if (accept && ignoreSelfPackets) {
		    	if (JagornetDhcpServer.getAllIPv4Addrs().contains(remoteSocketAddress.getAddress())) {
		    		log.debug("Ignoring packet from self: address=" + 
		    					remoteSocketAddress.getAddress());
		    		accept = false;
		    	}
	    	}
    	}
    	else {
    		log.debug("Packet was not accepted by super class: " + this.getClass().getGenericSuperclass());
    	}
    	return accept;
    }
    
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
		// remoteSocketAddress is set by DhcpV4PacketDecoder.channelRead
        DhcpV4Message dhcpMessage = 
        	DhcpV4Message.decode(buf.nioBuffer(), localSocketAddress, remoteSocketAddress);
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
