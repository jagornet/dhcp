/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestDhcpConfirmProcessor.java is part of DHCPv6.
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
import com.jagornet.dhcpv6.util.DhcpConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class TestDhcpConfirmProcessor.
 */
public class TestDhcpConfirmProcessor extends BaseTestDhcpProcessor
{
	
	/**
	 * Test solicit and request and confirm.
	 * 
	 * @throws Exception the exception
	 */
	public void testSolicitAndRequestAndConfirm() throws Exception
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
		
		System.out.println("Sleeping before confirm...");
		Thread.sleep(2000);

		// convert the reply into a confirm request
		replyMsg.setMessageType(DhcpConstants.CONFIRM);
		// null out the ServerId option for a confirm
		replyMsg.getDhcpOptionMap().remove(DhcpConstants.OPTION_SERVERID);
		
		DhcpConfirmProcessor cProc =
			new DhcpConfirmProcessor(replyMsg, replyMsg.getRemoteAddress().getAddress());
		
		replyMsg = cProc.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.REPLY, replyMsg.getMessageType());
		
//	TAHI tests want the status at the message level
//		checkReplyIaNaStatus(replyMsg, DhcpConstants.STATUS_CODE_SUCCESS);
		checkReplyMsgStatus(replyMsg, DhcpConstants.STATUS_CODE_SUCCESS);
	}
	
	/**
	 * Test request not on link.
	 * 
	 * @throws Exception the exception
	 */
	public void testRequestNotOnLink() throws Exception
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
		
		System.out.println("Sleeping before confirm...");
		Thread.sleep(2000);

		// convert the reply into a confirm request
		replyMsg.setMessageType(DhcpConstants.CONFIRM);
		// null out the ServerId option for a confirm
		replyMsg.getDhcpOptionMap().remove(DhcpConstants.OPTION_SERVERID);
		// hack the returned reply to request an off-link address
		replyMsg.getIaNaOptions().iterator().next().
				getIaAddrOptions().iterator().next().
						getIaAddrOption().setIpv6Address("2001:DB8:2::1");
		
		DhcpConfirmProcessor cProc =
			new DhcpConfirmProcessor(replyMsg, replyMsg.getRemoteAddress().getAddress());
		
		replyMsg = cProc.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.REPLY, replyMsg.getMessageType());
		
//	TAHI tests want the status at the message level
//		checkReplyIaNaStatus(replyMsg, DhcpConstants.STATUS_CODE_NOTONLINK);
		checkReplyMsgStatus(replyMsg, DhcpConstants.STATUS_CODE_NOTONLINK);
	}
	
	/**
	 * Test confirm no addrs.
	 * 
	 * @throws Exception the exception
	 */
	public void testConfirmNoAddrs() throws Exception
	{
		DhcpMessage requestMsg = buildRequestMessage(firstPoolAddr);
		requestMsg.setMessageType(DhcpConstants.CONFIRM);
		// null out the ServerId option for a confirm
		requestMsg.getDhcpOptionMap().remove(DhcpConstants.OPTION_SERVERID);
		
		DhcpConfirmProcessor cProc =
			new DhcpConfirmProcessor(requestMsg, requestMsg.getRemoteAddress().getAddress());
		
		DhcpMessage replyMsg = cProc.processMessage();
		
		assertNull(replyMsg);		
	}
}
