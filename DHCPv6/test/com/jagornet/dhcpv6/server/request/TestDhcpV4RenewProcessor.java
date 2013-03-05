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

import com.jagornet.dhcpv6.message.DhcpV4Message;
import com.jagornet.dhcpv6.option.v4.DhcpV4RequestedIpAddressOption;
import com.jagornet.dhcpv6.util.DhcpConstants;

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
	public void testDiscoverAndRequestAndRenew() throws Exception
	{
		DhcpV4Message requestMsg = buildRequestMessage(firstPoolAddr);
		requestMsg.setMessageType(DhcpConstants.SOLICIT);

		DhcpV4DiscoverProcessor dProc = 
			new DhcpV4DiscoverProcessor(requestMsg, requestMsg.getRemoteAddress().getAddress());

		DhcpV4Message offerMsg = dProc.processMessage();
		
		assertNotNull(offerMsg);
		
		// use the OFFER message to create the REQUEST message
		offerMsg.setMessageType((short)DhcpConstants.V4MESSAGE_TYPE_REQUEST);
		DhcpV4RequestedIpAddressOption requestedIpOption = new DhcpV4RequestedIpAddressOption();
		requestedIpOption.setIpAddress(offerMsg.getYiAddr().getHostAddress());
		offerMsg.putDhcpOption(requestedIpOption);
		offerMsg.setYiAddr(DhcpConstants.ZEROADDR);
		DhcpV4RequestProcessor rProc = 
			new DhcpV4RequestProcessor(offerMsg, offerMsg.getRemoteAddress().getAddress());

		DhcpV4Message replyMsg = rProc.processMessage();
		
		assertNotNull(replyMsg);
		
		System.out.println("Sleeping before renew...");
		Thread.sleep(2000);

		// convert the reply into a renew request
		replyMsg.setMessageType((short)DhcpConstants.V4MESSAGE_TYPE_REQUEST);
		replyMsg.setCiAddr(replyMsg.getYiAddr());
		replyMsg.setYiAddr(DhcpConstants.ZEROADDR);
		
		DhcpV4RequestProcessor nProc =
			new DhcpV4RequestProcessor(replyMsg, replyMsg.getRemoteAddress().getAddress());
		
		replyMsg = nProc.processMessage();

		assertEquals(DhcpConstants.OP_REPLY, replyMsg.getOp());
		assertEquals(requestMsg.getChAddr(), replyMsg.getChAddr());
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.V4MESSAGE_TYPE_ACK, replyMsg.getMessageType());
		
		checkReply(replyMsg, 
				InetAddress.getByName("192.168.0.100"),
				InetAddress.getByName("192.168.0.199"));
	}

}
