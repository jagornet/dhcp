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

import com.jagornet.dhcp.xml.OpaqueData;
import com.jagornet.dhcp.xml.V6ClientIdOption;
import com.jagornet.dhcpv6.db.BaseTestCase;
import com.jagornet.dhcpv6.message.DhcpV6Message;
import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6ClientIdOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6DnsServersOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6DomainSearchListOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6IaAddrOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6IaNaOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6ServerIdOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6SipServerAddressesOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6StatusCodeOption;
import com.jagornet.dhcpv6.server.request.binding.V6NaAddrBindingManager;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.util.Util;

// TODO: Auto-generated Javadoc
/**
 * The Class BaseTestDhcpV6Processor.
 */
public class BaseTestDhcpV6Processor extends BaseTestCase
{
	/** The manager. */
	protected V6NaAddrBindingManager manager;
	
	/** The client id option. */
	protected DhcpV6ClientIdOption clientIdOption;
	
	protected InetAddress firstPoolAddr;
	
	public BaseTestDhcpV6Processor() {
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
		V6ClientIdOption clientId = V6ClientIdOption.Factory.newInstance();
		clientId.setOpaqueData(opaque);
		clientIdOption = new DhcpV6ClientIdOption(clientId);
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
	protected DhcpV6Message buildRequestMessage(InetAddress linkAddress)
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
	protected DhcpV6Message buildRequestMessage(InetAddress linkAddress, String requestedAddr)
	{
		InetSocketAddress remoteSocketAddr = 
			new InetSocketAddress(linkAddress, DhcpConstants.CLIENT_PORT);
		InetSocketAddress localSocketAddr = 
			new InetSocketAddress(DhcpConstants.LOCALHOST_V6, DhcpConstants.SERVER_PORT);
		
		DhcpV6Message requestMsg = new DhcpV6Message(localSocketAddr, remoteSocketAddr);
		requestMsg.putDhcpOption(clientIdOption);
		DhcpV6IaNaOption dhcpIaNa = new DhcpV6IaNaOption();
		dhcpIaNa.setIaId(1);
		dhcpIaNa.setT1(0);	// client SHOULD set to zero RFC3315 - 18.1.2
		dhcpIaNa.setT2(0);
		if (requestedAddr != null) {
			DhcpV6IaAddrOption dhcpIaAddr = new DhcpV6IaAddrOption();
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
	protected DhcpV6Message buildReplyMessage(DhcpV6Message requestMsg)
	{
		// build a reply message using the local and remote sockets from the request
        DhcpV6Message replyMsg = new DhcpV6Message(requestMsg.getLocalAddress(), requestMsg.getRemoteAddress());
        // copy the transaction ID into the reply
        replyMsg.setTransactionId(requestMsg.getTransactionId());

        // MUST put Server Identifier DUID in ADVERTISE or REPLY message
        DhcpV6ServerIdOption serverIdOption = 
        	new DhcpV6ServerIdOption(config.getDhcpServerConfig().getV6ServerIdOption());
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
	protected void checkReply(DhcpV6Message replyMsg,
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
	protected void checkReply(DhcpV6Message replyMsg,
			InetAddress poolStart, InetAddress poolEnd, long lifetime) throws Exception
	{
		Collection<DhcpOption> dhcpOptions = replyMsg.getDhcpOptions();
		assertNotNull(dhcpOptions);
		assertEquals(4, dhcpOptions.size());
		
		DhcpV6ClientIdOption _clientIdOption = 
			(DhcpV6ClientIdOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_CLIENTID);	
		assertNotNull(_clientIdOption);
		
		DhcpV6ServerIdOption _serverIdOption = 
			(DhcpV6ServerIdOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_SERVERID);	
		assertNotNull(_serverIdOption);
		
		DhcpV6DnsServersOption _dnsServersOption = 
			(DhcpV6DnsServersOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_DNS_SERVERS);	
		assertNotNull(_dnsServersOption);
		
		DhcpV6DomainSearchListOption _domainSearchListOption = 
			(DhcpV6DomainSearchListOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_DOMAIN_SEARCH_LIST);	
		assertNotNull(_domainSearchListOption);
		
		DhcpV6IaNaOption _iaNaOption = replyMsg.getIaNaOptions().get(0);
		assertNotNull(_iaNaOption);

		DhcpV6IaAddrOption _iaAddrOption = _iaNaOption.getIaAddrOptions().get(0);
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
		
		DhcpV6SipServerAddressesOption _sipServersOption = 
			(DhcpV6SipServerAddressesOption) optMap.get(DhcpConstants.OPTION_SIP_SERVERS_ADDRESS_LIST);
		assertNotNull(_sipServersOption);
		
		optMap = _iaNaOption.getDhcpOptionMap();
		assertNotNull(optMap);
		
		_dnsServersOption = 
			(DhcpV6DnsServersOption) optMap.get(DhcpConstants.OPTION_DNS_SERVERS);
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
	protected void checkReplyMsgStatus(DhcpV6Message replyMsg, int status) throws Exception
	{
		Collection<DhcpOption> dhcpOptions = replyMsg.getDhcpOptions();
		assertNotNull(dhcpOptions);
		assertEquals(3, dhcpOptions.size());
		
		DhcpV6ClientIdOption _clientIdOption = 
			(DhcpV6ClientIdOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_CLIENTID);	
		assertNotNull(_clientIdOption);
		
		DhcpV6ServerIdOption _serverIdOption = 
			(DhcpV6ServerIdOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_SERVERID);	
		assertNotNull(_serverIdOption);
				
		DhcpV6StatusCodeOption _statusCodeOption = 
			(DhcpV6StatusCodeOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_STATUS_CODE);
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
	protected void checkReplyIaNaStatus(DhcpV6Message replyMsg, int status) throws Exception
	{
		Collection<DhcpOption> dhcpOptions = replyMsg.getDhcpOptions();
		assertNotNull(dhcpOptions);
//		assertEquals(2, dhcpOptions.size());
		
		DhcpV6ClientIdOption _clientIdOption = 
			(DhcpV6ClientIdOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_CLIENTID);	
		assertNotNull(_clientIdOption);
		
		DhcpV6ServerIdOption _serverIdOption = 
			(DhcpV6ServerIdOption) replyMsg.getDhcpOption(DhcpConstants.OPTION_SERVERID);	
		assertNotNull(_serverIdOption);
		
		DhcpV6IaNaOption _iaNaOption = replyMsg.getIaNaOptions().get(0);
		assertNotNull(_iaNaOption);

		Map<Integer, DhcpOption> optMap = _iaNaOption.getDhcpOptionMap();
		assertNotNull(optMap);
		assertEquals(1, optMap.size());
		
		DhcpV6StatusCodeOption _statusCodeOption = 
			(DhcpV6StatusCodeOption) optMap.get(DhcpConstants.OPTION_STATUS_CODE);
		assertNotNull(_statusCodeOption);
		assertEquals(status, _statusCodeOption.getStatusCode());
	}	
}
