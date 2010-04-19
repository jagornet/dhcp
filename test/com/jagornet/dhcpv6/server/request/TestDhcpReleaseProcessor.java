/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestDhcpReleaseProcessor.java is part of DHCPv6.
 *
 *   DHCPv6 is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   DHCPv6 is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with DHCPv6.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcpv6.server.request;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpServerIdOption;
import com.jagornet.dhcpv6.util.DhcpConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class TestDhcpReleaseProcessor.
 */
public class TestDhcpReleaseProcessor extends BaseTestDhcpProcessor
{
	
	/**
	 * Test solicit and request and release.
	 * 
	 * @throws Exception the exception
	 */
	public void testSolicitAndRequestAndRelease() throws Exception
	{
		DhcpMessage requestMsg = buildRequestMessage(firstPoolAddr);
		requestMsg.setMessageType(DhcpConstants.SOLICIT);

		DhcpSolicitProcessor sProc = 
			new DhcpSolicitProcessor(requestMsg, requestMsg.getRemoteAddress().getAddress());

		DhcpMessage advertiseMsg = sProc.processMessage();
		
		assertNotNull(advertiseMsg);
		
		// use the ADVERTISE message to create the REQUEST message
		advertiseMsg.setMessageType(DhcpConstants.REQUEST);
		DhcpRequestProcessor rProc = 
			new DhcpRequestProcessor(advertiseMsg, advertiseMsg.getRemoteAddress().getAddress());

		DhcpMessage replyMsg = rProc.processMessage();
		
		assertNotNull(replyMsg);
		
		System.out.println("Sleeping before release...");
		Thread.sleep(2000);

		// convert the reply into a release request
		replyMsg.setMessageType(DhcpConstants.RELEASE);
		
		DhcpReleaseProcessor lProc =
			new DhcpReleaseProcessor(replyMsg, replyMsg.getRemoteAddress().getAddress());
		
		replyMsg = lProc.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.REPLY, replyMsg.getMessageType());
		
		checkReplyMsgStatus(replyMsg, DhcpConstants.STATUS_CODE_SUCCESS);
	}
	
	/**
	 * Test release no binding.
	 * 
	 * @throws Exception the exception
	 */
	public void testReleaseNoBinding() throws Exception
	{
		DhcpMessage requestMsg = buildRequestMessage(firstPoolAddr);
		requestMsg.setMessageType(DhcpConstants.RELEASE);
		DhcpServerIdOption dhcpServerId = 
			new DhcpServerIdOption(config.getDhcpV6ServerConfig().getServerIdOption());
		requestMsg.putDhcpOption(dhcpServerId);
		
		DhcpReleaseProcessor processor =
			new DhcpReleaseProcessor(requestMsg, requestMsg.getRemoteAddress().getAddress());
		
		DhcpMessage replyMsg = processor.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.REPLY, replyMsg.getMessageType());
		
		checkReplyMsgStatus(replyMsg, DhcpConstants.STATUS_CODE_SUCCESS);
		
		checkReplyIaNaStatus(replyMsg, DhcpConstants.STATUS_CODE_NOBINDING);
	}
}
