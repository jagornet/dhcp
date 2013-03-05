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
package com.jagornet.dhcpv6.server.request;

import java.net.InetAddress;
import java.util.Collection;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpClientIdOption;
import com.jagornet.dhcpv6.option.DhcpIaAddrOption;
import com.jagornet.dhcpv6.option.DhcpIaNaOption;
import com.jagornet.dhcpv6.option.DhcpServerIdOption;
import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcpv6.util.DhcpConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class TestDhcpRebindProcessor.
 */
public class TestDhcpRebindProcessor extends BaseTestDhcpProcessor
{
	
	/**
	 * Test solicit and request and rebind.
	 * 
	 * @throws Exception the exception
	 */
	public void testSolicitAndRequestAndRebind() throws Exception
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
		
		System.out.println("Sleeping before renew...");
		Thread.sleep(2000);
		
		// convert the reply into a rebind request
		replyMsg.setMessageType(DhcpConstants.REBIND);
		// null out the ServerId option for a rebind
		replyMsg.getDhcpOptionMap().remove(DhcpConstants.OPTION_SERVERID);
		
		DhcpRebindProcessor nProc =
			new DhcpRebindProcessor(replyMsg, replyMsg.getRemoteAddress().getAddress());
		
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
		DhcpMessage requestMsg = buildRequestMessage(firstPoolAddr);
		requestMsg.setMessageType(DhcpConstants.REBIND);
		
		DhcpRebindProcessor processor =
			new DhcpRebindProcessor(requestMsg, requestMsg.getRemoteAddress().getAddress());
		
		DhcpMessage replyMsg = processor.processMessage();
		
		// the verify policy is off by default, so silently ignore request
		assertNull(replyMsg);
	}
	
	/**
	 * Test rebind no binding with verify.
	 * 
	 * @throws Exception the exception
	 */
	public void testRebindNoBindingWithVerify() throws Exception
	{
		// override the default server policy
		DhcpServerPolicies.getProperties().put(Property.VERIFY_UNKNOWN_REBIND.key(), "true");
		DhcpMessage requestMsg = buildRequestMessage(firstPoolAddr);
		requestMsg.setMessageType(DhcpConstants.REBIND);
		DhcpIaAddrOption dhcpIaAddr = new DhcpIaAddrOption();
		dhcpIaAddr.setIpAddress("2001:DB8:2::1");
		requestMsg.getIaNaOptions().iterator().next().getIaAddrOptions().add(dhcpIaAddr);
		
		DhcpRebindProcessor processor =
			new DhcpRebindProcessor(requestMsg, requestMsg.getRemoteAddress().getAddress());
		
		DhcpMessage replyMsg = processor.processMessage();
		
		assertNotNull(replyMsg);
		assertEquals(requestMsg.getTransactionId(), replyMsg.getTransactionId());
		assertEquals(DhcpConstants.REPLY, replyMsg.getMessageType());
		
		Collection<DhcpOption> dhcpOptions = replyMsg.getDhcpOptions();
		assertNotNull(dhcpOptions);
		assertEquals(2, dhcpOptions.size());
		
		DhcpClientIdOption _clientIdOption = 
			(DhcpClientIdOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_CLIENTID);	
		assertNotNull(_clientIdOption);
		
		DhcpServerIdOption _serverIdOption = 
			(DhcpServerIdOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_SERVERID);	
		assertNotNull(_serverIdOption);
		
		DhcpIaNaOption _iaNaOption = replyMsg.getIaNaOptions().get(0);
		assertNotNull(_iaNaOption);

		DhcpIaAddrOption _iaAddrOption = _iaNaOption.getIaAddrOptions().get(0);
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
		
		DhcpRebindProcessor nProc =
			new DhcpRebindProcessor(replyMsg, replyMsg.getRemoteAddress().getAddress());
		
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
		
		DhcpClientIdOption _clientIdOption = 
			(DhcpClientIdOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_CLIENTID);	
		assertNotNull(_clientIdOption);
		
		DhcpServerIdOption _serverIdOption = 
			(DhcpServerIdOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_SERVERID);	
		assertNotNull(_serverIdOption);
		
		DhcpIaNaOption _iaNaOption = replyMsg.getIaNaOptions().get(0);
		assertNotNull(_iaNaOption);

		DhcpIaAddrOption _iaAddrOption = _iaNaOption.getIaAddrOptions().get(0);
		assertNotNull(_iaAddrOption);
		assertNotNull(_iaAddrOption.getInetAddress());
		assertEquals(0, _iaAddrOption.getPreferredLifetime());
		assertEquals(0, _iaAddrOption.getValidLifetime());
	}

}
