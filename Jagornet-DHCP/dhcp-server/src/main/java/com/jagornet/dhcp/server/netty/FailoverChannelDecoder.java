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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.failover.FailoverMessage;

/**
 * Title: DhcpV4ChannelDecoder
 * Description: The protocol decoder used by the NETTY-based DHCPv4 server
 * when receiving packets.
 * 
 * @author A. Gregory Rabil
 */
@ChannelHandler.Sharable
public class FailoverChannelDecoder extends OneToOneDecoder
{
    
    /** The log. */
    private static Logger log = LoggerFactory.getLogger(FailoverChannelDecoder.class);

    /** The local socket address. */
    protected InetSocketAddress localSocketAddress = null;
    
    /** The remote socket address. */
    protected InetSocketAddress remoteSocketAddress = null;
    
    protected boolean ignoreSelfPackets;
    
    public FailoverChannelDecoder(InetSocketAddress localSocketAddress, boolean ignoreSelfPackets)
    {
    	this.localSocketAddress = localSocketAddress;
    	this.ignoreSelfPackets = ignoreSelfPackets;
    }
    
    /*
     * Decodes a received ChannelBuffer into a DhcpMessage.
     * (non-Javadoc)
     * @see org.jboss.netty.handler.codec.oneone.OneToOneDecoder#decode(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.Channel, java.lang.Object)
     */
    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception
    {
    	
        if (msg instanceof ChannelBuffer) {
        	ChannelBuffer buf = (ChannelBuffer) msg;
            FailoverMessage failoverMessage =  
            	FailoverMessage.decode(buf.toByteBuffer(), localSocketAddress, remoteSocketAddress);
            return failoverMessage;
        }
        else {
            String errmsg = "Unknown message object class: " + msg.getClass();
            log.error(errmsg);
            return msg;
        }
    }
    
    /* (non-Javadoc)
     * @see org.jboss.netty.handler.codec.oneone.OneToOneDecoder#handleUpstream(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelEvent)
     */
    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent evt) throws Exception
    {
    	if (evt instanceof MessageEvent) {
    		remoteSocketAddress = (InetSocketAddress) ((MessageEvent)evt).getRemoteAddress();
    	}
    	super.handleUpstream(ctx, evt);
    }
}
