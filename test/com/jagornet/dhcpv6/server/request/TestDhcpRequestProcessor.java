/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestDhcpRequestProcessor.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.util.DhcpConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class TestDhcpRequestProcessor.
 */
public class TestDhcpRequestProcessor extends BaseTestDhcpProcessor
{
	
	/**
	 * Test solicit and request.
	 * 
	 * @throws Exception the exception
	 */
	public void testSolicitAndRequest() throws Exception
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
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.REPLY, replyMsg.getMessageType());
		
		checkReply(replyMsg, 
				InetAddress.getByName("2001:DB8:1::A"),
				InetAddress.getByName("2001:DB8:1::FF"));
	}
	
	/**
	 * Test should multicast.
	 * 
	 * @throws Exception the exception
	 */
	public void testShouldMulticast() throws Exception
	{
		DhcpMessage requestMsg = buildRequestMessage(firstPoolAddr);
		requestMsg.setMessageType(DhcpConstants.REQUEST);

		DhcpSolicitProcessor sProc = 
			new DhcpSolicitProcessor(requestMsg, requestMsg.getRemoteAddress().getAddress());

		DhcpMessage advertiseMsg = sProc.processMessage();
		
		assertNotNull(advertiseMsg);

		// hack the returned advertise to set unicast
		advertiseMsg.setUnicast(true);
		
		// use the ADVERTISE message to create the REQUEST message
		DhcpRequestProcessor rProc = 
			new DhcpRequestProcessor(advertiseMsg, advertiseMsg.getRemoteAddress().getAddress());

		DhcpMessage replyMsg = rProc.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.REPLY, replyMsg.getMessageType());
		
		checkReplyMsgStatus(replyMsg, DhcpConstants.STATUS_CODE_USEMULTICAST);
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

		// hack the returned advertise to request an off-link address
		advertiseMsg.getIaNaOptions().iterator().next().
				getIaAddrOptions().iterator().next().
						getIaAddrOption().setIpv6Address("2001:DB8:2::1");
		
		// use the ADVERTISE message to create the REQUEST message
		advertiseMsg.setMessageType(DhcpConstants.REQUEST);
		DhcpRequestProcessor rProc = 
			new DhcpRequestProcessor(advertiseMsg, advertiseMsg.getRemoteAddress().getAddress());

		DhcpMessage replyMsg = rProc.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.REPLY, replyMsg.getMessageType());
		
		checkReplyIaNaStatus(replyMsg, DhcpConstants.STATUS_CODE_NOTONLINK);
	}

}
