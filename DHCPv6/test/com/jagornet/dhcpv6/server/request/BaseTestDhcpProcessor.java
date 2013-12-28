/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseTestDhcpProcessor.java is part of DHCPv6.
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
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;

import com.jagornet.dhcpv6.db.BaseTestCase;
import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpClientIdOption;
import com.jagornet.dhcpv6.option.DhcpDnsServersOption;
import com.jagornet.dhcpv6.option.DhcpDomainSearchListOption;
import com.jagornet.dhcpv6.option.DhcpIaAddrOption;
import com.jagornet.dhcpv6.option.DhcpIaNaOption;
import com.jagornet.dhcpv6.option.DhcpServerIdOption;
import com.jagornet.dhcpv6.option.DhcpSipServerAddressesOption;
import com.jagornet.dhcpv6.option.DhcpStatusCodeOption;
import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.server.request.binding.NaAddrBindingManager;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.ClientIdOption;
import com.jagornet.dhcpv6.xml.OpaqueData;

// TODO: Auto-generated Javadoc
/**
 * The Class BaseTestDhcpProcessor.
 */
public class BaseTestDhcpProcessor extends BaseTestCase
{
	/** The manager. */
	protected NaAddrBindingManager manager;
	
	/** The client id option. */
	protected DhcpClientIdOption clientIdOption;
	
	protected InetAddress firstPoolAddr;
	
	public BaseTestDhcpProcessor() {
		super();
		manager = config.getNaAddrBindingMgr();
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.BaseDbTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		OpaqueData opaque = OpaqueData.Factory.newInstance();
		opaque.setHexValue(new byte[] { (byte)0xde, (byte)0xbb, (byte)0x1e,
				(byte)0xde, (byte)0xbb, (byte)0x1e });
		ClientIdOption clientId = ClientIdOption.Factory.newInstance();
		clientId.setOpaqueData(opaque);
		clientIdOption = new DhcpClientIdOption(clientId);
		firstPoolAddr = InetAddress.getByName("2001:DB8:1::a");
	}
	
	/* (non-Javadoc)
	 * @see org.dbunit.DatabaseTestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		clientIdOption = null;
		firstPoolAddr = null;
	}

	/**
	 * Builds the request message.
	 * 
	 * @param linkAddress the link address
	 * 
	 * @return the dhcp message
	 */
	protected DhcpMessage buildRequestMessage(InetAddress linkAddress)
	{
		return buildRequestMessage(linkAddress, null);
	}
	
	/**
	 * Builds the request message.
	 * 
	 * @param linkAddress the link address
	 * @param requestedAddr the requested addr
	 * 
	 * @return the dhcp message
	 */
	protected DhcpMessage buildRequestMessage(InetAddress linkAddress, String requestedAddr)
	{
		InetSocketAddress remoteSocketAddr = 
			new InetSocketAddress(linkAddress, DhcpConstants.CLIENT_PORT);
		InetSocketAddress localSocketAddr = 
			new InetSocketAddress(DhcpConstants.LOCALHOST_V6, DhcpConstants.SERVER_PORT);
		
		DhcpMessage requestMsg = new DhcpMessage(localSocketAddr, remoteSocketAddr);
		requestMsg.putDhcpOption(clientIdOption);
		DhcpIaNaOption dhcpIaNa = new DhcpIaNaOption();
		dhcpIaNa.setIaId(1);
		dhcpIaNa.setT1(0);	// client SHOULD set to zero RFC3315 - 18.1.2
		dhcpIaNa.setT2(0);
		if (requestedAddr != null) {
			DhcpIaAddrOption dhcpIaAddr = new DhcpIaAddrOption();
			dhcpIaAddr.setIpAddress(requestedAddr);
			dhcpIaNa.getIaAddrOptions().add(dhcpIaAddr);
		}
		requestMsg.addIaNaOption(dhcpIaNa);
		return requestMsg;
	}
	
	/**
	 * Builds the reply message.
	 * 
	 * @param requestMsg the request msg
	 * 
	 * @return the dhcp message
	 */
	protected DhcpMessage buildReplyMessage(DhcpMessage requestMsg)
	{
		// build a reply message using the local and remote sockets from the request
        DhcpMessage replyMsg = new DhcpMessage(requestMsg.getLocalAddress(), requestMsg.getRemoteAddress());
        // copy the transaction ID into the reply
        replyMsg.setTransactionId(requestMsg.getTransactionId());

        // MUST put Server Identifier DUID in ADVERTISE or REPLY message
        DhcpServerIdOption serverIdOption = 
        	new DhcpServerIdOption(config.getDhcpV6ServerConfig().getServerIdOption());
        replyMsg.putDhcpOption(serverIdOption);

        // copy Client Identifier DUID if given in client request message
        replyMsg.putDhcpOption(requestMsg.getDhcpClientIdOption());
        return replyMsg;
	}

	/**
	 * Check reply.
	 * 
	 * @param replyMsg the reply msg
	 * @param poolStart the pool start
	 * @param poolEnd the pool end
	 * 
	 * @throws Exception the exception
	 */
	protected void checkReply(DhcpMessage replyMsg,
			InetAddress poolStart, InetAddress poolEnd) throws Exception
	{
		checkReply(replyMsg, poolStart, poolEnd, 3600);
	}
	
	/**
	 * Check reply.
	 * 
	 * @param replyMsg the reply msg
	 * @param poolStart the pool start
	 * @param poolEnd the pool end
	 * @param lifetime the lifetime
	 * 
	 * @throws Exception the exception
	 */
	protected void checkReply(DhcpMessage replyMsg,
			InetAddress poolStart, InetAddress poolEnd, long lifetime) throws Exception
	{
		Collection<DhcpOption> dhcpOptions = replyMsg.getDhcpOptions();
		assertNotNull(dhcpOptions);
		assertEquals(4, dhcpOptions.size());
		
		DhcpClientIdOption _clientIdOption = 
			(DhcpClientIdOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_CLIENTID);	
		assertNotNull(_clientIdOption);
		
		DhcpServerIdOption _serverIdOption = 
			(DhcpServerIdOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_SERVERID);	
		assertNotNull(_serverIdOption);
		
		DhcpDnsServersOption _dnsServersOption = 
			(DhcpDnsServersOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_DNS_SERVERS);	
		assertNotNull(_dnsServersOption);
		
		DhcpDomainSearchListOption _domainSearchListOption = 
			(DhcpDomainSearchListOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_DOMAIN_SEARCH_LIST);	
		assertNotNull(_domainSearchListOption);
		
		DhcpIaNaOption _iaNaOption = replyMsg.getIaNaOptions().get(0);
		assertNotNull(_iaNaOption);

		DhcpIaAddrOption _iaAddrOption = _iaNaOption.getIaAddrOptions().get(0);
		assertNotNull(_iaAddrOption);
		assertNotNull(_iaAddrOption.getInetAddress());
		if (poolStart != null)
			assertTrue(Util.compareInetAddrs(poolStart, _iaAddrOption.getInetAddress()) <= 0);
		if (poolEnd != null)
			assertTrue(Util.compareInetAddrs(poolEnd, _iaAddrOption.getInetAddress()) >= 0);
		assertEquals(lifetime, _iaAddrOption.getPreferredLifetime());
		assertEquals(lifetime, _iaAddrOption.getValidLifetime());
		
		Map<Integer, DhcpOption> optMap = _iaAddrOption.getDhcpOptionMap();
		assertNotNull(optMap);
		
		DhcpSipServerAddressesOption _sipServersOption = 
			(DhcpSipServerAddressesOption) optMap.get(DhcpConstants.OPTION_SIP_SERVERS_ADDRESS_LIST);
		assertNotNull(_sipServersOption);
		
		optMap = _iaNaOption.getDhcpOptionMap();
		assertNotNull(optMap);
		
		_dnsServersOption = 
			(DhcpDnsServersOption) optMap.get(DhcpConstants.OPTION_DNS_SERVERS);
		assertNotNull(_dnsServersOption);
	}	

	/**
	 * Check reply msg status.
	 * 
	 * @param replyMsg the reply msg
	 * @param status the status
	 * 
	 * @throws Exception the exception
	 */
	protected void checkReplyMsgStatus(DhcpMessage replyMsg, int status) throws Exception
	{
		Collection<DhcpOption> dhcpOptions = replyMsg.getDhcpOptions();
		assertNotNull(dhcpOptions);
		assertEquals(3, dhcpOptions.size());
		
		DhcpClientIdOption _clientIdOption = 
			(DhcpClientIdOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_CLIENTID);	
		assertNotNull(_clientIdOption);
		
		DhcpServerIdOption _serverIdOption = 
			(DhcpServerIdOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_SERVERID);	
		assertNotNull(_serverIdOption);
				
		DhcpStatusCodeOption _statusCodeOption = 
			(DhcpStatusCodeOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_STATUS_CODE);
		assertNotNull(_statusCodeOption);
		assertEquals(status, _statusCodeOption.getStatusCode());
	}	

	/**
	 * Check reply ia na status.
	 * 
	 * @param replyMsg the reply msg
	 * @param status the status
	 * 
	 * @throws Exception the exception
	 */
	protected void checkReplyIaNaStatus(DhcpMessage replyMsg, int status) throws Exception
	{
		Collection<DhcpOption> dhcpOptions = replyMsg.getDhcpOptions();
		assertNotNull(dhcpOptions);
//		assertEquals(2, dhcpOptions.size());
		
		DhcpClientIdOption _clientIdOption = 
			(DhcpClientIdOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_CLIENTID);	
		assertNotNull(_clientIdOption);
		
		DhcpServerIdOption _serverIdOption = 
			(DhcpServerIdOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_SERVERID);	
		assertNotNull(_serverIdOption);
		
		DhcpIaNaOption _iaNaOption = replyMsg.getIaNaOptions().get(0);
		assertNotNull(_iaNaOption);

		Map<Integer, DhcpOption> optMap = _iaNaOption.getDhcpOptionMap();
		assertNotNull(optMap);
		assertEquals(1, optMap.size());
		
		DhcpStatusCodeOption _statusCodeOption = 
			(DhcpStatusCodeOption) optMap.get(DhcpConstants.OPTION_STATUS_CODE);
		assertNotNull(_statusCodeOption);
		assertEquals(status, _statusCodeOption.getStatusCode());
	}	
}
