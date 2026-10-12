/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.netty;

import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.message.DhcpV4Message;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

/**
 * Title: DhcpV4ChannelEncoder
 * Description: The protocol encoder used by the NETTY-based DHCPv4 server
 * when sending packets.
 * 
 * @author A. Gregory Rabil
 */
@ChannelHandler.Sharable
public class DhcpV4ChannelEncoder extends MessageToMessageEncoder<DhcpV4Message>
{
    private static Logger log = LoggerFactory.getLogger(DhcpV4ChannelEncoder.class);

    @Override
	protected void encode(ChannelHandlerContext ctx, DhcpV4Message msg, List<Object> out) throws Exception {
		log.info("Encoding message: msg=" + msg);
        ByteBuffer buf = msg.encode();
        log.info("Message encoded: limit=" + buf.limit());
        out.add(Unpooled.wrappedBuffer(buf));
	}   
}
