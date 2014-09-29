/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestDhcpSolicitProcessor.java is part of DHCPv6.
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

import java.net.InetAddress;

import com.jagornet.dhcpv6.message.DhcpV6Message;
import com.jagornet.dhcpv6.option.v6.DhcpV6ReconfigureAcceptOption;
import com.jagornet.dhcpv6.util.DhcpConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class TestDhcpV6SolicitProcessor.
 */
public class TestDhcpV6SolicitProcessor extends BaseTestDhcpV6Processor
{
	
	/**
	 * Test solicit.
	 * 
	 * @throws Exception the exception
	 */
	public void testSolicit() throws Exception
	{
		DhcpV6Message requestMsg = buildRequestMessage(firstPoolAddr);
		requestMsg.setMessageType(DhcpConstants.SOLICIT);

		DhcpV6SolicitProcessor processor = 
			new DhcpV6SolicitProcessor(requestMsg, requestMsg.getRemoteAddress().getAddress());
		
		DhcpV6Message replyMsg = processor.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.ADVERTISE, replyMsg.getMessageType());
		
		checkReply(replyMsg,
				InetAddress.getByName("2001:DB8:1::A"),
				InetAddress.getByName("2001:DB8:1::FF"));		
	}
	
	/**
	 * Test solicit request off link address.
	 * 
	 * @throws Exception the exception
	 */
	public void testSolicitRequestOffLinkAddress() throws Exception
	{
		DhcpV6Message requestMsg = buildRequestMessage(firstPoolAddr,
													"2001:DB8:2::1");
		requestMsg.setMessageType(DhcpConstants.SOLICIT);

		DhcpV6SolicitProcessor processor = 
			new DhcpV6SolicitProcessor(requestMsg, requestMsg.getRemoteAddress().getAddress());
		
		DhcpV6Message replyMsg = processor.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.ADVERTISE, replyMsg.getMessageType());
		
		checkReply(replyMsg,
				InetAddress.getByName("2001:DB8:1::A"),
				InetAddress.getByName("2001:DB8:1::FF"));		
	}
	
	/**
	 * Test solicit reconfigure accept.
	 * 
	 * @throws Exception the exception
	 */
	public void testSolicitReconfigureAccept() throws Exception
	{
		DhcpV6Message requestMsg = buildRequestMessage(firstPoolAddr);
		requestMsg.setMessageType(DhcpConstants.SOLICIT);
		
		DhcpV6ReconfigureAcceptOption reconfigAcceptOption = new DhcpV6ReconfigureAcceptOption();
		requestMsg.putDhcpOption(reconfigAcceptOption);

		DhcpV6SolicitProcessor processor = 
			new DhcpV6SolicitProcessor(requestMsg, requestMsg.getRemoteAddress().getAddress());
		
		DhcpV6Message replyMsg = processor.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.ADVERTISE, replyMsg.getMessageType());
		
		checkReply(replyMsg,
				InetAddress.getByName("2001:DB8:1::10A"),
				InetAddress.getByName("2001:DB8:1::1FF"));		
	}
	
	/**
	 * Test solicit no reconfigure accept.
	 * 
	 * @throws Exception the exception
	 */
	public void testSolicitNoReconfigureAccept() throws Exception
	{
		DhcpV6Message requestMsg = buildRequestMessage(InetAddress.getByName("2001:DB8:2::1"));
		requestMsg.setMessageType(DhcpConstants.SOLICIT);

		DhcpV6SolicitProcessor processor = 
			new DhcpV6SolicitProcessor(requestMsg, requestMsg.getRemoteAddress().getAddress());
		
		DhcpV6Message replyMsg = processor.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.ADVERTISE, replyMsg.getMessageType());
		
//	TAHI tests want this at the message level
//		checkReplyIaNaStatus(replyMsg, DhcpConstants.STATUS_CODE_NOADDRSAVAIL);
		checkReplyMsgStatus(replyMsg, DhcpConstants.STATUS_CODE_NOADDRSAVAIL);
	}
	
	
}
