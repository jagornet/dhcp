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

import com.jagornet.dhcpv6.db.BaseTestCase;
import com.jagornet.dhcpv6.message.DhcpV4Message;
import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.option.v4.DhcpV4DomainNameOption;
import com.jagornet.dhcpv6.option.v4.DhcpV4DomainServersOption;
import com.jagornet.dhcpv6.option.v4.DhcpV4LeaseTimeOption;
import com.jagornet.dhcpv6.option.v4.DhcpV4MsgTypeOption;
import com.jagornet.dhcpv6.option.v4.DhcpV4RequestedIpAddressOption;
import com.jagornet.dhcpv6.option.v4.DhcpV4RoutersOption;
import com.jagornet.dhcpv6.option.v4.DhcpV4ServerIdOption;
import com.jagornet.dhcpv6.option.v4.DhcpV4SubnetMaskOption;
import com.jagornet.dhcpv6.server.request.binding.V4AddrBindingManager;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.util.Util;

// TODO: Auto-generated Javadoc
/**
 * The Class BaseTestDhcpProcessor.
 */
public class BaseTestDhcpV4Processor extends BaseTestCase
{
	/** The manager. */
	protected V4AddrBindingManager manager;
	
	/** The client mac address. */
	protected byte[] clientMacAddr;
	
	protected InetAddress firstPoolAddr;

	public BaseTestDhcpV4Processor() {
		super();
		manager = config.getV4AddrBindingMgr();
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.BaseDbTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		clientMacAddr = new byte[] { (byte)0xde, (byte)0xbb, (byte)0x1e,
				(byte)0xde, (byte)0xbb, (byte)0x1e };
		firstPoolAddr = InetAddress.getByName("192.168.0.1");
	}
	
	/* (non-Javadoc)
	 * @see org.dbunit.DatabaseTestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		clientMacAddr = null;
		firstPoolAddr = null;
	}

	/**
	 * Builds the request message.
	 * 
	 * @param linkAddress the link address
	 * 
	 * @return the dhcp message
	 */
	protected DhcpV4Message buildRequestMessage(InetAddress linkAddress)
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
	protected DhcpV4Message buildRequestMessage(InetAddress linkAddress, String requestedAddr)
	{
		InetSocketAddress remoteSocketAddr = 
			new InetSocketAddress(linkAddress, DhcpConstants.V4_CLIENT_PORT);
		InetSocketAddress localSocketAddr = 
			new InetSocketAddress(DhcpConstants.LOCALHOST, DhcpConstants.V4_SERVER_PORT);
		
		DhcpV4Message requestMsg = new DhcpV4Message(localSocketAddr, remoteSocketAddr);
		requestMsg.setHtype((short)1);
		requestMsg.setHlen((short)clientMacAddr.length);
		requestMsg.setChAddr(clientMacAddr);
		if (requestedAddr != null) {
			DhcpV4RequestedIpAddressOption dhcpIaAddr = new DhcpV4RequestedIpAddressOption();
			dhcpIaAddr.setIpAddress(requestedAddr);
			requestMsg.getDhcpOptions().add(dhcpIaAddr);
		}
		return requestMsg;
	}
	
	/**
	 * Builds the reply message.
	 * 
	 * @param requestMsg the request msg
	 * 
	 * @return the dhcp message
	 */
	protected DhcpV4Message buildReplyMessage(DhcpV4Message requestMsg)
	{
		// build a reply message using the local and remote sockets from the request
        DhcpV4Message replyMsg = 
        	new DhcpV4Message(requestMsg.getLocalAddress(), requestMsg.getRemoteAddress());
        // copy the transaction ID into the reply
        replyMsg.setTransactionId(requestMsg.getTransactionId());
        replyMsg.setHtype(requestMsg.getHtype());
        replyMsg.setHlen(requestMsg.getHlen());
        replyMsg.setChAddr(requestMsg.getChAddr());
        replyMsg.setFlags(requestMsg.getFlags());
        replyMsg.setGiAddr(requestMsg.getGiAddr());

        // MUST put Server Identifier DUID in ADVERTISE or REPLY message
        DhcpV4ServerIdOption serverIdOption = 
        	new DhcpV4ServerIdOption(config.getDhcpV6ServerConfig().getV4ServerIdOption());
        replyMsg.putDhcpOption(serverIdOption);

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
	protected void checkReply(DhcpV4Message replyMsg,
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
	protected void checkReply(DhcpV4Message replyMsg,
			InetAddress poolStart, InetAddress poolEnd, long lifetime) throws Exception
	{
		assertNotNull(replyMsg.getYiAddr());
		if (poolStart != null)
			assertTrue(Util.compareInetAddrs(poolStart, replyMsg.getYiAddr()) <= 0);
		if (poolEnd != null)
			assertTrue(Util.compareInetAddrs(poolEnd, replyMsg.getYiAddr()) >= 0);
		
		Collection<DhcpOption> dhcpOptions = replyMsg.getDhcpOptions();
		assertNotNull(dhcpOptions);
		assertEquals(7, dhcpOptions.size());
		
		DhcpV4MsgTypeOption _msgTypeOption = 
			(DhcpV4MsgTypeOption) replyMsg.getDhcpOption(DhcpConstants.V4OPTION_MESSAGE_TYPE);	
		assertNotNull(_msgTypeOption);
		
		DhcpV4ServerIdOption _serverIdOption = 
			(DhcpV4ServerIdOption) replyMsg.getDhcpOption(DhcpConstants.V4OPTION_SERVERID);	
		assertNotNull(_serverIdOption);
		
		DhcpV4SubnetMaskOption _subnetMaskOption =
			(DhcpV4SubnetMaskOption) replyMsg.getDhcpOption(DhcpConstants.V4OPTION_SUBNET_MASK);
		assertNotNull(_subnetMaskOption);
		
		DhcpV4RoutersOption _routersOption =
			(DhcpV4RoutersOption) replyMsg.getDhcpOption(DhcpConstants.V4OPTION_ROUTERS);
		assertNotNull(_routersOption);
		
		DhcpV4DomainServersOption _domainServersOption = 
			(DhcpV4DomainServersOption) replyMsg.getDhcpOption(DhcpConstants.V4OPTION_DOMAIN_SERVERS);	
		assertNotNull(_domainServersOption);
		
		DhcpV4DomainNameOption _domainNameOption = 
			(DhcpV4DomainNameOption) replyMsg.getDhcpOption(DhcpConstants.V4OPTION_DOMAIN_NAME);	
		assertNotNull(_domainNameOption);
		
		DhcpV4LeaseTimeOption _leaseTimeOption = 
			(DhcpV4LeaseTimeOption) replyMsg.getDhcpOption(DhcpConstants.V4OPTION_LEASE_TIME);	
		assertNotNull(_leaseTimeOption);
		assertEquals(lifetime, _leaseTimeOption.getUnsignedInt());		
	}	
}
