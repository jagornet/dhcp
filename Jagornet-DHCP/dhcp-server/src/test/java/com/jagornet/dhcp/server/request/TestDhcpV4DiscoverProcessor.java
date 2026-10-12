/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.InetAddress;

import org.junit.Test;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * The Class TestDhcpV4DiscoverProcessor.
 */
public class TestDhcpV4DiscoverProcessor extends BaseTestDhcpV4Processor
{
	
	/**
	 * Test discover.
	 * 
	 * @throws Exception the exception
	 */
	@Test
	public void testDiscover() throws Exception
	{
		DhcpV4Message requestMsg = buildRequestMessage(firstPoolAddr);
		requestMsg.setMessageType((short)DhcpConstants.V4MESSAGE_TYPE_DISCOVER);

		DhcpV4DiscoverProcessor processor = 
			new DhcpV4DiscoverProcessor(requestMsg, requestMsg.getRemoteAddress().getAddress());
		
		DhcpV4Message replyMsg = processor.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(DhcpConstants.V4_OP_REPLY, replyMsg.getOp());
		assertEquals(requestMsg.getChAddr(), replyMsg.getChAddr());
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.V4MESSAGE_TYPE_OFFER, replyMsg.getMessageType());
		
		checkReply(replyMsg,
				InetAddress.getByName("192.168.0.100"),
				InetAddress.getByName("192.168.0.199"));		
	}
	
}
