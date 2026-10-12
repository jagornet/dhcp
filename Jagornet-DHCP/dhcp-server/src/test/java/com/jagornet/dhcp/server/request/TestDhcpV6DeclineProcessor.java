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

import org.junit.Test;

import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.option.v6.DhcpV6ServerIdOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * The Class TestDhcpV6DeclineProcessor.
 */
public class TestDhcpV6DeclineProcessor extends BaseTestDhcpV6Processor
{
	
	/**
	 * Test solicit and request and decline.
	 * 
	 * @throws Exception the exception
	 */
	@Test
	public void testSolicitAndRequestAndDecline() throws Exception
	{
		DhcpV6Message requestMsg = buildNaRequestMessage(firstPoolAddr);
		requestMsg.setMessageType(DhcpConstants.V6MESSAGE_TYPE_SOLICIT);

		DhcpV6SolicitProcessor sProc = 
			new DhcpV6SolicitProcessor(requestMsg, requestMsg.getRemoteAddress().getAddress());

		DhcpV6Message advertiseMsg = sProc.processMessage();
		
		assertNotNull(advertiseMsg);
		
		// use the ADVERTISE message to create the REQUEST message
		advertiseMsg.setMessageType(DhcpConstants.V6MESSAGE_TYPE_REQUEST);
		DhcpV6RequestProcessor rProc = 
			new DhcpV6RequestProcessor(advertiseMsg, advertiseMsg.getRemoteAddress().getAddress());

		DhcpV6Message replyMsg = rProc.processMessage();
		
		assertNotNull(replyMsg);
		
		System.out.println("Sleeping before decline...");
		Thread.sleep(2000);

		// convert the reply into a decline request
		replyMsg.setMessageType(DhcpConstants.V6MESSAGE_TYPE_DECLINE);
		
		DhcpV6DeclineProcessor dProc =
			new DhcpV6DeclineProcessor(replyMsg, replyMsg.getRemoteAddress().getAddress());
		
		replyMsg = dProc.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.V6MESSAGE_TYPE_REPLY, replyMsg.getMessageType());
		
		checkReplyMsgStatus(replyMsg, DhcpConstants.V6STATUS_CODE_SUCCESS);
	}
	
	/**
	 * Test decline no binding.
	 * 
	 * @throws Exception the exception
	 */
	@Test
	public void testDeclineNoBinding() throws Exception
	{
		DhcpV6Message requestMsg = buildNaRequestMessage(firstPoolAddr);
		requestMsg.setMessageType(DhcpConstants.V6MESSAGE_TYPE_DECLINE);
		DhcpV6ServerIdOption dhcpServerId = config.getDhcpV6ServerIdOption();
		requestMsg.putDhcpOption(dhcpServerId);
		
		DhcpV6DeclineProcessor processor =
			new DhcpV6DeclineProcessor(requestMsg, requestMsg.getRemoteAddress().getAddress());
		
		DhcpV6Message replyMsg = processor.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.V6MESSAGE_TYPE_REPLY, replyMsg.getMessageType());
		
		checkReplyMsgStatus(replyMsg, DhcpConstants.V6STATUS_CODE_SUCCESS);
		
		checkReplyIaNaStatus(replyMsg, DhcpConstants.V6STATUS_CODE_NOBINDING);
	}
}
