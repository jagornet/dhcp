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
import com.jagornet.dhcp.core.option.v4.DhcpV4RequestedIpAddressOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * The Class TestDhcpV4RequestProcessor.
 */
public class TestDhcpV4RenewProcessor extends BaseTestDhcpV4Processor
{
	
	/**
	 * Test solicit and request.
	 * 
	 * @throws Exception the exception
	 */
	@Test
	public void testDiscoverAndRequestAndRenew() throws Exception
	{
		DhcpV4Message requestMsg = buildRequestMessage(firstPoolAddr);
		requestMsg.setMessageType((short)DhcpConstants.V4MESSAGE_TYPE_DISCOVER);

		DhcpV4DiscoverProcessor dProc = 
			new DhcpV4DiscoverProcessor(requestMsg, requestMsg.getRemoteAddress().getAddress());

		DhcpV4Message offerMsg = dProc.processMessage();
		
		assertNotNull(offerMsg);
		
		// use the OFFER message to create the REQUEST message
		offerMsg.setMessageType((short)DhcpConstants.V4MESSAGE_TYPE_REQUEST);
		DhcpV4RequestedIpAddressOption requestedIpOption = new DhcpV4RequestedIpAddressOption();
		requestedIpOption.setIpAddress(offerMsg.getYiAddr().getHostAddress());
		offerMsg.putDhcpOption(requestedIpOption);
		offerMsg.setYiAddr(DhcpConstants.ZEROADDR_V4);
		DhcpV4RequestProcessor rProc = 
			new DhcpV4RequestProcessor(offerMsg, offerMsg.getRemoteAddress().getAddress());

		DhcpV4Message replyMsg = rProc.processMessage();
		
		assertNotNull(replyMsg);
		
		System.out.println("Sleeping before renew...");
		Thread.sleep(2000);

		// convert the reply into a renew request
		replyMsg.setMessageType((short)DhcpConstants.V4MESSAGE_TYPE_REQUEST);
		replyMsg.setCiAddr(replyMsg.getYiAddr());
		replyMsg.setYiAddr(DhcpConstants.ZEROADDR_V4);
		
		DhcpV4RequestProcessor nProc =
			new DhcpV4RequestProcessor(replyMsg, replyMsg.getRemoteAddress().getAddress());
		
		replyMsg = nProc.processMessage();

		assertEquals(DhcpConstants.V4_OP_REPLY, replyMsg.getOp());
		assertEquals(requestMsg.getChAddr(), replyMsg.getChAddr());
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.V4MESSAGE_TYPE_ACK, replyMsg.getMessageType());
		
		checkReply(replyMsg, 
				InetAddress.getByName("192.168.0.100"),
				InetAddress.getByName("192.168.0.199"));
	}

}
