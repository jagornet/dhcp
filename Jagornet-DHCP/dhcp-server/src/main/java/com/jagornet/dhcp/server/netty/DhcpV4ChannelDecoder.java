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
import java.net.NetworkInterface;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.JagornetDhcpServer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * Title: DhcpV4ChannelDecoder
 * Description: The DHCPv4 message decoder used by the NETTY-based DHCPv4 server
 * when receiving packets.  This is the second handler in the inbound pipeline.
 * This class does the work of creating the request DhcpV4Message and then
 * sends it on to the channel handler, which is next in the pipeline. 
 * 
 * @author A. Gregory Rabil
 */
@ChannelHandler.Sharable
public class DhcpV4ChannelDecoder extends MessageToMessageDecoder<ByteBuf>
{
    private static Logger log = LoggerFactory.getLogger(DhcpV4ChannelDecoder.class);

    protected InetSocketAddress localSocketAddress = null;
    
    protected InetSocketAddress remoteSocketAddress = null;
    
    protected boolean ignoreSelfPackets;
    protected NetworkInterface v4BroadcastNetIf;
    
    public DhcpV4ChannelDecoder(InetSocketAddress localSocketAddress, boolean ignoreSelfPackets, NetworkInterface v4BroadcastNetIf)
    {
    	this.localSocketAddress = localSocketAddress;
    	this.ignoreSelfPackets = ignoreSelfPackets;
    	this.v4BroadcastNetIf = v4BroadcastNetIf;
    }
    
    @Override
    public boolean acceptInboundMessage(Object obj) throws Exception {
    	boolean accept = super.acceptInboundMessage(obj);
    	if (accept) {
    	    // TODO: the DhcpV4UnicastChannelDecoder calls this superclass
    		// implementation, but maybe should just refactor appropriately
    		if (!(this instanceof DhcpV4UnicastChannelDecoder)) {
    			// being called from Netty as the broadcast decoder, as opposed
    			// to being called as the super implementation from the subclass
		    	if (DhcpConstants.IS_WINDOWS &&
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
		    	// v4BroadcastNetIf should only be set in the event that
		    	// there are multiple V4 interfaces, but keep the double-check
		    	if  (JagornetDhcpServer.hasMultipleV4Interfaces() &&
		    			(v4BroadcastNetIf != null)) {
					log.warn("Multiple active IPv4 interfaces found." +
							 " Assuming this packet was received on the" +
							 " configured broadcast network interface: " +
							v4BroadcastNetIf);
				}
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
		log.info("Decoding broadcast message:" +
				 " local=" + Util.socketAddressAsString(localSocketAddress) + 
				 " remote=" + Util.socketAddressAsString(remoteSocketAddress));
		// Netty's ByteBuf.nioBuffer() returns the underlying ByteBuffer, 
		// without additional allocations, so should not be necessary 
		// to have a NettyDhcpV4Message.decode() for Netty's ByteBuf
        DhcpV4Message dhcpMessage = 
        	DhcpV4Message.decode(buf.nioBuffer(), localSocketAddress, remoteSocketAddress);
        log.info("Broadcast message decoded: msg=" + dhcpMessage);
		// send the message to the next handler in the pipeline, which is
		// the DhcpV4ChannelHandler
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
