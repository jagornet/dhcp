/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestDhcpRebindProcessor.java is part of DHCPv6.
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

import java.net.InetAddress;
import java.util.Collection;

import com.jagornet.dhcp.message.DhcpV6Message;
import com.jagornet.dhcp.option.base.DhcpOption;
import com.jagornet.dhcp.option.v6.DhcpV6ClientIdOption;
import com.jagornet.dhcp.option.v6.DhcpV6IaAddrOption;
import com.jagornet.dhcp.option.v6.DhcpV6IaNaOption;
import com.jagornet.dhcp.option.v6.DhcpV6ServerIdOption;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.request.DhcpV6RebindProcessor;
import com.jagornet.dhcp.server.request.DhcpV6RequestProcessor;
import com.jagornet.dhcp.server.request.DhcpV6SolicitProcessor;
import com.jagornet.dhcp.util.DhcpConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class TestDhcpV6RebindProcessor.
 */
public class TestDhcpV6RebindProcessor extends BaseTestDhcpV6Processor
{
	
	/**
	 * Test solicit and request and rebind.
	 * 
	 * @throws Exception the exception
	 */
	public void testSolicitAndRequestAndRebind() throws Exception
	{
		DhcpV6Message requestMsg = buildRequestMessage(firstPoolAddr);
		requestMsg.setMessageType(DhcpConstants.SOLICIT);

		DhcpV6SolicitProcessor sProc = 
			new DhcpV6SolicitProcessor(requestMsg, requestMsg.getRemoteAddress().getAddress());

		DhcpV6Message advertiseMsg = sProc.processMessage();
		
		assertNotNull(advertiseMsg);
		
		// use the ADVERTISE message to create the REQUEST message
		advertiseMsg.setMessageType(DhcpConstants.REQUEST);
		DhcpV6RequestProcessor rProc = 
			new DhcpV6RequestProcessor(advertiseMsg, advertiseMsg.getRemoteAddress().getAddress());

		DhcpV6Message replyMsg = rProc.processMessage();
		
		assertNotNull(replyMsg);
		
		System.out.println("Sleeping before renew...");
		Thread.sleep(2000);
		
		// convert the reply into a rebind request
		replyMsg.setMessageType(DhcpConstants.REBIND);
		// null out the ServerId option for a rebind
		replyMsg.getDhcpOptionMap().remove(DhcpConstants.OPTION_SERVERID);
		
		DhcpV6RebindProcessor nProc =
			new DhcpV6RebindProcessor(replyMsg, replyMsg.getRemoteAddress().getAddress());
		
		replyMsg = nProc.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.REPLY, replyMsg.getMessageType());
		
		checkReply(replyMsg, 
				InetAddress.getByName("2001:DB8:1::A"),
				InetAddress.getByName("2001:DB8:1::FF"));
	}
	
	/**
	 * Test rebind no binding.
	 * 
	 * @throws Exception the exception
	 */
	public void testRebindNoBinding() throws Exception
	{
		// set the default server policy
		DhcpServerPolicies.getProperties().put(Property.VERIFY_UNKNOWN_REBIND.key(), "false");
		
		DhcpV6Message requestMsg = buildRequestMessage(firstPoolAddr);
		requestMsg.setMessageType(DhcpConstants.REBIND);
		
		DhcpV6RebindProcessor processor =
			new DhcpV6RebindProcessor(requestMsg, requestMsg.getRemoteAddress().getAddress());
		
		DhcpV6Message replyMsg = processor.processMessage();
		
		assertNull(replyMsg);
	}
	
	/**
	 * Test rebind no binding with verify.
	 * 
	 * @throws Exception the exception
	 */
	public void testRebindNoBindingWithVerify() throws Exception
	{
		// set the default server policy
		DhcpServerPolicies.getProperties().put(Property.VERIFY_UNKNOWN_REBIND.key(), "true");

		DhcpV6Message requestMsg = buildRequestMessage(firstPoolAddr);
		requestMsg.setMessageType(DhcpConstants.REBIND);
		DhcpV6IaAddrOption dhcpIaAddr = new DhcpV6IaAddrOption();
		dhcpIaAddr.setIpAddress("2001:DB8:2::1");
		requestMsg.getIaNaOptions().iterator().next().getIaAddrOptions().add(dhcpIaAddr);
		
		DhcpV6RebindProcessor processor =
			new DhcpV6RebindProcessor(requestMsg, requestMsg.getRemoteAddress().getAddress());
		
		DhcpV6Message replyMsg = processor.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.REPLY, replyMsg.getMessageType());
		
		Collection<DhcpOption> dhcpOptions = replyMsg.getDhcpOptions();
		assertNotNull(dhcpOptions);
		assertEquals(2, dhcpOptions.size());
		
		DhcpV6ClientIdOption _clientIdOption = 
			(DhcpV6ClientIdOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_CLIENTID);	
		assertNotNull(_clientIdOption);
		
		DhcpV6ServerIdOption _serverIdOption = 
			(DhcpV6ServerIdOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_SERVERID);	
		assertNotNull(_serverIdOption);
		
		DhcpV6IaNaOption _iaNaOption = replyMsg.getIaNaOptions().get(0);
		assertNotNull(_iaNaOption);

		DhcpV6IaAddrOption _iaAddrOption = _iaNaOption.getIaAddrOptions().get(0);
		assertNotNull(_iaAddrOption);
		assertNotNull(_iaAddrOption.getInetAddress());
		assertEquals(InetAddress.getByName("2001:DB8:2::1"), _iaAddrOption.getInetAddress());
		assertEquals(0, _iaAddrOption.getPreferredLifetime());
		assertEquals(0, _iaAddrOption.getValidLifetime());
	}
	
	/**
	 * Test zero lifetimes.
	 * 
	 * @throws Exception the exception
	 */
	public void testZeroLifetimes() throws Exception
	{
		DhcpV6Message requestMsg = buildRequestMessage(firstPoolAddr);
		requestMsg.setMessageType(DhcpConstants.SOLICIT);

		DhcpV6SolicitProcessor sProc = 
			new DhcpV6SolicitProcessor(requestMsg, requestMsg.getRemoteAddress().getAddress());

		DhcpV6Message advertiseMsg = sProc.processMessage();
		
		assertNotNull(advertiseMsg);
		
		// use the ADVERTISE message to create the REQUEST message
		advertiseMsg.setMessageType(DhcpConstants.REQUEST);
		DhcpV6RequestProcessor rProc = 
			new DhcpV6RequestProcessor(advertiseMsg, advertiseMsg.getRemoteAddress().getAddress());

		DhcpV6Message replyMsg = rProc.processMessage();
		
		assertNotNull(replyMsg);
		
		System.out.println("Sleeping before rebind...");
		Thread.sleep(2000);

		// convert the reply into a rebind request
		replyMsg.setMessageType(DhcpConstants.REBIND);
		// null out the ServerId option for a rebind
		replyMsg.getDhcpOptionMap().remove(DhcpConstants.OPTION_SERVERID);
		// hack the returned reply to request an off-link address
		replyMsg.getIaNaOptions().iterator().next().
				getIaAddrOptions().iterator().next().
						setIpAddress("2001:DB8:2::1");
		
		DhcpV6RebindProcessor nProc =
			new DhcpV6RebindProcessor(replyMsg, replyMsg.getRemoteAddress().getAddress());
		
		// set the server policy to allow it to verify this unknown rebind
		DhcpServerPolicies.setProperty(Property.VERIFY_UNKNOWN_REBIND, "true");
		
		replyMsg = nProc.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.REPLY, replyMsg.getMessageType());
		
//		checkReply(replyMsg, null, null, 0);
		Collection<DhcpOption> dhcpOptions = replyMsg.getDhcpOptions();
		assertNotNull(dhcpOptions);
		assertEquals(2, dhcpOptions.size());
		
		DhcpV6ClientIdOption _clientIdOption = 
			(DhcpV6ClientIdOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_CLIENTID);	
		assertNotNull(_clientIdOption);
		
		DhcpV6ServerIdOption _serverIdOption = 
			(DhcpV6ServerIdOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_SERVERID);	
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
