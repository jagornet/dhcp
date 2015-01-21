/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestDhcpV6RenewProcessor.java is part of Jagornet DHCP.
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
import java.util.Collection;

import com.jagornet.dhcp.message.DhcpV6Message;
import com.jagornet.dhcp.option.base.DhcpOption;
import com.jagornet.dhcp.option.v6.DhcpV6ClientIdOption;
import com.jagornet.dhcp.option.v6.DhcpV6IaAddrOption;
import com.jagornet.dhcp.option.v6.DhcpV6IaNaOption;
import com.jagornet.dhcp.option.v6.DhcpV6ServerIdOption;
import com.jagornet.dhcp.server.request.DhcpV6RenewProcessor;
import com.jagornet.dhcp.server.request.DhcpV6RequestProcessor;
import com.jagornet.dhcp.server.request.DhcpV6SolicitProcessor;
import com.jagornet.dhcp.util.DhcpConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class TestDhcpV6RenewProcessor.
 */
public class TestDhcpV6RenewProcessor extends BaseTestDhcpV6Processor
{
	
	/**
	 * Test solicit and request and renew.
	 * 
	 * @throws Exception the exception
	 */
	public void testSolicitAndRequestAndRenew() throws Exception
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
		
		System.out.println("Sleeping before renew...");
		Thread.sleep(2000);

		// convert the reply into a renew request
		replyMsg.setMessageType(DhcpConstants.V6MESSAGE_TYPE_RENEW);
		
		DhcpV6RenewProcessor nProc =
			new DhcpV6RenewProcessor(replyMsg, replyMsg.getRemoteAddress().getAddress());
		
		replyMsg = nProc.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.V6MESSAGE_TYPE_REPLY, replyMsg.getMessageType());
		
		checkReply(replyMsg, 
				InetAddress.getByName("2001:DB8:1::A"),
				InetAddress.getByName("2001:DB8:1::FF"));
	}
	
	/**
	 * Test renew no binding.
	 * 
	 * @throws Exception the exception
	 */
	public void testRenewNoBinding() throws Exception
	{
		DhcpV6Message requestMsg = buildRequestMessage(firstPoolAddr);
		requestMsg.setMessageType(DhcpConstants.V6MESSAGE_TYPE_RENEW);
		DhcpV6ServerIdOption dhcpServerId = 
			new DhcpV6ServerIdOption(config.getDhcpServerConfig().getV6ServerIdOption());
		requestMsg.putDhcpOption(dhcpServerId);
		
		DhcpV6RenewProcessor processor =
			new DhcpV6RenewProcessor(requestMsg, requestMsg.getRemoteAddress().getAddress());
		
		DhcpV6Message replyMsg = processor.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.V6MESSAGE_TYPE_REPLY, replyMsg.getMessageType());
		
		checkReplyIaNaStatus(replyMsg, DhcpConstants.V6STATUS_CODE_NOBINDING);
	}
	
	/**
	 * Test zero lifetimes.
	 * 
	 * @throws Exception the exception
	 */
	public void testZeroLifetimes() throws Exception
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
		
		System.out.println("Sleeping before renew...");
		Thread.sleep(2000);

		// convert the reply into a renew request
		replyMsg.setMessageType(DhcpConstants.V6MESSAGE_TYPE_RENEW);
		// hack the returned reply to request an off-link address
		replyMsg.getIaNaOptions().iterator().next().
				getIaAddrOptions().iterator().next().
						setIpAddress("2001:DB8:2::1");
		
		DhcpV6RenewProcessor nProc =
			new DhcpV6RenewProcessor(replyMsg, replyMsg.getRemoteAddress().getAddress());
		
		replyMsg = nProc.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.V6MESSAGE_TYPE_REPLY, replyMsg.getMessageType());
		
//		checkReply(replyMsg, null, null, 0);
		Collection<DhcpOption> dhcpOptions = replyMsg.getDhcpOptions();
		assertNotNull(dhcpOptions);
		assertEquals(2, dhcpOptions.size());
		
		DhcpV6ClientIdOption _clientIdOption = 
			(DhcpV6ClientIdOption) replyMsg.getDhcpOption(DhcpConstants.V6OPTION_CLIENTID);	
		assertNotNull(_clientIdOption);
		
		DhcpV6ServerIdOption _serverIdOption = 
			(DhcpV6ServerIdOption) replyMsg.getDhcpOption(DhcpConstants.V6OPTION_SERVERID);	
		assertNotNull(_serverIdOption);
		
		DhcpV6IaNaOption _iaNaOption = replyMsg.getIaNaOptions().get(0);
		assertNotNull(_iaNaOption);

		DhcpV6IaAddrOption _iaAddrOption = _iaNaOption.getIaAddrOptions().get(0);
		assertNotNull(_iaAddrOption);
		assertNotNull(_iaAddrOption.getInetAddress());
		assertEquals(0, _iaAddrOption.getPreferredLifetime());
		assertEquals(0, _iaAddrOption.getValidLifetime());
	}

}
