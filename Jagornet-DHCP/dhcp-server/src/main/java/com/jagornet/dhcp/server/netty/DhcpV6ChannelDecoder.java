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
package com.jagornet.dhcp.server.netty;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.JagornetDhcpServer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * Description: The DHCPv6 message decoder used by the NETTY-based DHCPv6 server
 * when receiving packets.  This is the second handler in the inbound pipeline.
 * This class does the work of creating the request DhcpV6Message and then
 * sends it on to the channel handler, which is next in the pipeline. 
 * 
 * @author A. Gregory Rabil
 */
@ChannelHandler.Sharable
public class DhcpV6ChannelDecoder extends MessageToMessageDecoder<ByteBuf>
{
    private static Logger log = LoggerFactory.getLogger(DhcpV6ChannelDecoder.class);

    protected InetSocketAddress localSocketAddress = null;
    
    protected InetSocketAddress remoteSocketAddress = null;
    
    protected boolean ignoreSelfPackets;
    
    public DhcpV6ChannelDecoder(InetSocketAddress localSocketAddress, boolean ignoreSelfPackets)
    {
    	this.localSocketAddress = localSocketAddress;
    	this.ignoreSelfPackets = ignoreSelfPackets;
    }
    
    @Override
    public boolean acceptInboundMessage(Object obj) throws Exception {
    	boolean accept = super.acceptInboundMessage(obj);
    	if (accept) {
    		InetAddress localAddr = localSocketAddress.getAddress();
    		InetAddress remoteAddr = remoteSocketAddress.getAddress();
    		if (localAddr.isLinkLocalAddress() !=  remoteAddr.isLinkLocalAddress()) {
	        	log.debug("Ignoring packet from " + 
        				remoteAddr.getHostAddress() +
        				" (linkLocal=" + remoteAddr.isLinkLocalAddress() + ")" +
        				" received on " +
        				localAddr.getHostAddress() +
        				" (linkLocal=" + localAddr.isLinkLocalAddress() + ")");
	        	accept = false;
    		}
    		
	    	if (accept && ignoreSelfPackets) {
		    	if (JagornetDhcpServer.getAllIPv6Addrs().contains(remoteSocketAddress.getAddress())) {
		    		log.debug("Ignoring packet from self: address=" + 
		    					remoteSocketAddress.getAddress());
		    		accept = false;
		    	}
	    	}
    	}
    	return accept;
    }
    
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
		log.info("Decoding multicast message:" +
				 " local=" + Util.socketAddressAsString(localSocketAddress) + 
				 " remote=" + Util.socketAddressAsString(remoteSocketAddress));
		// Netty's ByteBuf.nioBuffer() returns the underlying ByteBuffer, 
		// without additional allocations, so should not be necessary 
		// to have a NettyDhcpV4Message.decode() for Netty's ByteBuf
        DhcpV6Message dhcpMessage = 
        	DhcpV6Message.decode(buf.nioBuffer(), localSocketAddress, remoteSocketAddress);
        log.info("Multicast message decoded: msg=" + dhcpMessage);
		// send the message to the next handler in the pipeline, which is
		// the DhcpV6ChannelHandler
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
