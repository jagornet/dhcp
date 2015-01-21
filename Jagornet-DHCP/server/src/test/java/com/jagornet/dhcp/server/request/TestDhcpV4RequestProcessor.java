/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestDhcpV4RequestProcessor.java is part of Jagornet DHCP.
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

import java.net.InetAddress;

import com.jagornet.dhcp.message.DhcpV4Message;
import com.jagornet.dhcp.option.v4.DhcpV4RequestedIpAddressOption;
import com.jagornet.dhcp.server.request.DhcpV4DiscoverProcessor;
import com.jagornet.dhcp.server.request.DhcpV4RequestProcessor;
import com.jagornet.dhcp.util.DhcpConstants;

/**
 * The Class TestDhcpV4RequestProcessor.
 */
public class TestDhcpV4RequestProcessor extends BaseTestDhcpV4Processor
{
	
	/**
	 * Test solicit and request.
	 * 
	 * @throws Exception the exception
	 */
	public void testDiscoverAndRequest() throws Exception
	{
		DhcpV4Message requestMsg = buildRequestMessage(firstPoolAddr);
		requestMsg.setMessageType(DhcpConstants.V6MESSAGE_TYPE_SOLICIT);

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
