/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.junit.Test;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.option.v4.DhcpV4MsgTypeOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6ClientIdOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.DatagramPacketEncoder;

public class TestDhcpClientCodecs {

    @Test
    public void testDhcpV4CodecRoundTrip() throws Exception {
        InetSocketAddress clientAddr = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 67);
        InetSocketAddress serverAddr = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 67);

        EmbeddedChannel channel = new EmbeddedChannel(
                new DatagramPacketEncoder<DhcpV4Message>(new DhcpV4ChannelEncoder()),
                new DhcpV4ChannelDecoder(clientAddr)
        );

        // Build sample Discover message
        DhcpV4Message discover = new DhcpV4Message(clientAddr, serverAddr);
        discover.setOp((short) DhcpConstants.V4_OP_REQUEST);
        discover.setTransactionId(12345678L);
        discover.setHtype((short) 1);
        discover.setHlen((byte) 6);
        discover.setChAddr(new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 });
        DhcpV4MsgTypeOption msgType = new DhcpV4MsgTypeOption();
        msgType.setUnsignedByte((short) DhcpConstants.V4MESSAGE_TYPE_DISCOVER);
        discover.putDhcpOption(msgType);

        // Outbound write: DhcpV4Message -> DatagramPacket
        boolean written = channel.writeOutbound(
                new DefaultAddressedEnvelope<DhcpV4Message, InetSocketAddress>(discover, serverAddr));
        assertTrue(written);

        DatagramPacket packet = channel.readOutbound();
        assertNotNull(packet);
        assertEquals(serverAddr, packet.recipient());
        assertTrue(packet.content().readableBytes() > 0);

        // Inbound read: DatagramPacket -> DhcpV4Message
        boolean inboundProcessed = channel.writeInbound(packet);
        assertTrue(inboundProcessed);

        DhcpV4Message decoded = channel.readInbound();
        assertNotNull(decoded);
        assertEquals(discover.getTransactionId(), decoded.getTransactionId());
        assertEquals(DhcpConstants.V4MESSAGE_TYPE_DISCOVER, decoded.getMessageType());
        channel.finish();
    }

    @Test
    public void testDhcpV6CodecRoundTrip() throws Exception {
        InetSocketAddress clientAddr = new InetSocketAddress(InetAddress.getByName("::1"), 546);
        InetSocketAddress serverAddr = new InetSocketAddress(InetAddress.getByName("::1"), 547);

        EmbeddedChannel channel = new EmbeddedChannel(
                new DatagramPacketEncoder<DhcpV6Message>(new DhcpV6ChannelEncoder()),
                new DhcpV6ChannelDecoder(clientAddr)
        );

        // Build sample Solicit message
        DhcpV6Message solicit = new DhcpV6Message(clientAddr, serverAddr);
        solicit.setMessageType(DhcpConstants.V6MESSAGE_TYPE_SOLICIT);
        solicit.setTransactionId(987654);
        DhcpV6ClientIdOption clientId = new DhcpV6ClientIdOption();
        clientId.getOpaqueData().setAscii("test-client-duid");
        solicit.putDhcpOption(clientId);

        // Outbound write: DhcpV6Message -> DatagramPacket
        boolean written = channel.writeOutbound(
                new DefaultAddressedEnvelope<DhcpV6Message, InetSocketAddress>(solicit, serverAddr));
        assertTrue(written);

        DatagramPacket packet = channel.readOutbound();
        assertNotNull(packet);
        assertEquals(serverAddr, packet.recipient());
        assertTrue(packet.content().readableBytes() > 0);

        // Inbound read: DatagramPacket -> DhcpV6Message
        boolean inboundProcessed = channel.writeInbound(packet);
        assertTrue(inboundProcessed);

        DhcpV6Message decoded = channel.readInbound();
        assertNotNull(decoded);
        assertEquals(solicit.getTransactionId(), decoded.getTransactionId());
        assertEquals(DhcpConstants.V6MESSAGE_TYPE_SOLICIT, decoded.getMessageType());
        channel.finish();
    }
}
