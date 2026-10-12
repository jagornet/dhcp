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
 * The Class TestDhcpV6ReleaseProcessor.
 */
public class TestDhcpV6ReleaseProcessor extends BaseTestDhcpV6Processor
{
	/**
	 * Test solicit and request and release.
	 * 
	 * @throws Exception the exception
	 */
	@Test
	public void testSolicitAndRequestAndRelease() throws Exception
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
		
		System.out.println("Sleeping before release...");
		Thread.sleep(2000);

		// convert the reply into a release request
		replyMsg.setMessageType(DhcpConstants.V6MESSAGE_TYPE_RELEASE);
		
		DhcpV6ReleaseProcessor lProc =
			new DhcpV6ReleaseProcessor(replyMsg, replyMsg.getRemoteAddress().getAddress());
		
		replyMsg = lProc.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.V6MESSAGE_TYPE_REPLY, replyMsg.getMessageType());
		
		checkReplyMsgStatus(replyMsg, DhcpConstants.V6STATUS_CODE_SUCCESS);
	}
	
	/**
	 * Test release no binding.
	 * 
	 * @throws Exception the exception
	 */
	@Test
	public void testReleaseNoBinding() throws Exception
	{
		DhcpV6Message requestMsg = buildNaRequestMessage(firstPoolAddr);
		requestMsg.setMessageType(DhcpConstants.V6MESSAGE_TYPE_RELEASE);
		DhcpV6ServerIdOption dhcpServerId = config.getDhcpV6ServerIdOption();
		requestMsg.putDhcpOption(dhcpServerId);
		
		DhcpV6ReleaseProcessor processor =
			new DhcpV6ReleaseProcessor(requestMsg, requestMsg.getRemoteAddress().getAddress());
		
		DhcpV6Message replyMsg = processor.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.V6MESSAGE_TYPE_REPLY, replyMsg.getMessageType());
		
		checkReplyMsgStatus(replyMsg, DhcpConstants.V6STATUS_CODE_SUCCESS);
		
		checkReplyIaNaStatus(replyMsg, DhcpConstants.V6STATUS_CODE_NOBINDING);
	}
}
