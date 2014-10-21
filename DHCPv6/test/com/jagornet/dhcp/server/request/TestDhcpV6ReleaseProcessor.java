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
package com.jagornet.dhcp.server.request;

import com.jagornet.dhcp.message.DhcpV6Message;
import com.jagornet.dhcp.option.v6.DhcpV6ServerIdOption;
import com.jagornet.dhcp.server.request.DhcpV6ReleaseProcessor;
import com.jagornet.dhcp.server.request.DhcpV6RequestProcessor;
import com.jagornet.dhcp.server.request.DhcpV6SolicitProcessor;
import com.jagornet.dhcp.util.DhcpConstants;

// TODO: Auto-generated Javadoc
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
	public void testSolicitAndRequestAndRelease() throws Exception
	{
		DhcpV6Message requestMsg = buildRequestMessage(firstPoolAddr);
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
	public void testReleaseNoBinding() throws Exception
	{
		DhcpV6Message requestMsg = buildRequestMessage(firstPoolAddr);
		requestMsg.setMessageType(DhcpConstants.V6MESSAGE_TYPE_RELEASE);
		DhcpV6ServerIdOption dhcpServerId = 
			new DhcpV6ServerIdOption(config.getDhcpServerConfig().getV6ServerIdOption());
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
