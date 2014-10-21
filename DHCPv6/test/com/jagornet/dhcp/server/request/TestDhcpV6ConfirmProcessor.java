/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestDhcpV6ConfirmProcessor.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.server.request;

import com.jagornet.dhcp.message.DhcpV6Message;
import com.jagornet.dhcp.server.request.DhcpV6ConfirmProcessor;
import com.jagornet.dhcp.server.request.DhcpV6RequestProcessor;
import com.jagornet.dhcp.server.request.DhcpV6SolicitProcessor;
import com.jagornet.dhcp.util.DhcpConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class TestDhcpV6ConfirmProcessor.
 */
public class TestDhcpV6ConfirmProcessor extends BaseTestDhcpV6Processor
{
	
	/**
	 * Test solicit and request and confirm.
	 * 
	 * @throws Exception the exception
	 */
	public void testSolicitAndRequestAndConfirm() throws Exception
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
		
		System.out.println("Sleeping before confirm...");
		Thread.sleep(2000);

		// convert the reply into a confirm request
		replyMsg.setMessageType(DhcpConstants.V6MESSAGE_TYPE_CONFIRM);
		// null out the ServerId option for a confirm
		replyMsg.getDhcpOptionMap().remove(DhcpConstants.V6OPTION_SERVERID);
		
		DhcpV6ConfirmProcessor cProc =
			new DhcpV6ConfirmProcessor(replyMsg, replyMsg.getRemoteAddress().getAddress());
		
		replyMsg = cProc.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.V6MESSAGE_TYPE_REPLY, replyMsg.getMessageType());
		
//	TAHI tests want the status at the message level
//		checkReplyIaNaStatus(replyMsg, DhcpConstants.STATUS_CODE_SUCCESS);
		checkReplyMsgStatus(replyMsg, DhcpConstants.V6STATUS_CODE_SUCCESS);
	}
	
	/**
	 * Test request not on link.
	 * 
	 * @throws Exception the exception
	 */
	public void testRequestNotOnLink() throws Exception
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
		
		System.out.println("Sleeping before confirm...");
		Thread.sleep(2000);

		// convert the reply into a confirm request
		replyMsg.setMessageType(DhcpConstants.V6MESSAGE_TYPE_CONFIRM);
		// null out the ServerId option for a confirm
		replyMsg.getDhcpOptionMap().remove(DhcpConstants.V6OPTION_SERVERID);
		// hack the returned reply to request an off-link address
		replyMsg.getIaNaOptions().iterator().next().
				getIaAddrOptions().iterator().next().
						setIpAddress("2001:DB8:2::1");
		
		DhcpV6ConfirmProcessor cProc =
			new DhcpV6ConfirmProcessor(replyMsg, replyMsg.getRemoteAddress().getAddress());
		
		replyMsg = cProc.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.V6MESSAGE_TYPE_REPLY, replyMsg.getMessageType());
		
//	TAHI tests want the status at the message level
//		checkReplyIaNaStatus(replyMsg, DhcpConstants.STATUS_CODE_NOTONLINK);
		checkReplyMsgStatus(replyMsg, DhcpConstants.V6STATUS_CODE_NOTONLINK);
	}
	
	/**
	 * Test confirm no addrs.
	 * 
	 * @throws Exception the exception
	 */
	public void testConfirmNoAddrs() throws Exception
	{
		DhcpV6Message requestMsg = buildRequestMessage(firstPoolAddr);
		requestMsg.setMessageType(DhcpConstants.V6MESSAGE_TYPE_CONFIRM);
		// null out the ServerId option for a confirm
		requestMsg.getDhcpOptionMap().remove(DhcpConstants.V6OPTION_SERVERID);
		
		DhcpV6ConfirmProcessor cProc =
			new DhcpV6ConfirmProcessor(requestMsg, requestMsg.getRemoteAddress().getAddress());
		
		DhcpV6Message replyMsg = cProc.processMessage();
		
		assertNull(replyMsg);		
	}
}
