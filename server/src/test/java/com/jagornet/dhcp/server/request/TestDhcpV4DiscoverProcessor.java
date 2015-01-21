/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestDhcpV4DiscoverProcessor.java is part of Jagornet DHCP.
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
import com.jagornet.dhcp.server.request.DhcpV4DiscoverProcessor;
import com.jagornet.dhcp.util.DhcpConstants;

/**
 * The Class TestDhcpV4DiscoverProcessor.
 */
public class TestDhcpV4DiscoverProcessor extends BaseTestDhcpV4Processor
{
	
	/**
	 * Test discover.
	 * 
	 * @throws Exception the exception
	 */
	public void testDiscover() throws Exception
	{
		DhcpV4Message requestMsg = buildRequestMessage(firstPoolAddr);
		requestMsg.setMessageType((short)DhcpConstants.V4MESSAGE_TYPE_DISCOVER);

		DhcpV4DiscoverProcessor processor = 
			new DhcpV4DiscoverProcessor(requestMsg, requestMsg.getRemoteAddress().getAddress());
		
		DhcpV4Message replyMsg = processor.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(DhcpConstants.V4_OP_REPLY, replyMsg.getOp());
		assertEquals(requestMsg.getChAddr(), replyMsg.getChAddr());
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.V4MESSAGE_TYPE_OFFER, replyMsg.getMessageType());
		
		checkReply(replyMsg,
				InetAddress.getByName("192.168.0.100"),
				InetAddress.getByName("192.168.0.199"));		
	}
	
}
