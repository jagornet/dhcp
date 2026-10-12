/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.request.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Iterator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jagornet.dhcp.core.message.DhcpMessage;
import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.option.v6.DhcpV6ClientIdOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaNaOption;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.config.OpaqueDataUtil;
import com.jagornet.dhcp.server.config.xml.OpaqueData;
import com.jagornet.dhcp.server.db.BaseTestCase;
import com.jagornet.dhcp.server.db.IdentityAssoc;

/**
 * The Class TestBindingManager.
 */
public class TestV6NaAddrBindingManager extends BaseTestCase
{
	/** The manager. */
	private static V6NaAddrBindingManager manager;
	
	/** The client id option. */
	private DhcpV6ClientIdOption clientIdOption;

	@BeforeClass
	public static void oneTimeSetUp() throws Exception
	{
		initializeContext();
		manager = config.getV6NaAddrBindingMgr();
	}

	@AfterClass
	public static void oneTimeTearDown() throws Exception
	{
		closeContext();
	}
	
	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		config.getIaMgr().deleteAllIAs();
		OpaqueData opaque = new OpaqueData();
		opaque.setHexValue(new byte[] { (byte)0xde, (byte)0xbb, (byte)0x1e,
				(byte)0xde, (byte)0xbb, (byte)0x1e });
		clientIdOption = new DhcpV6ClientIdOption(OpaqueDataUtil.toBaseOpaqueData(opaque));
	}
	
	@After
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		clientIdOption = null;
	}
	
	/**
	 * Test find no current binding.
	 * 
	 * @throws Exception the exception
	 */
	@Test
	public void testFindNoCurrentBinding() throws Exception {
		DhcpV6IaNaOption dhcpIaNa = new DhcpV6IaNaOption();
		dhcpIaNa.setIaId(1);
		dhcpIaNa.setT1(0);	// client SHOULD set to zero RFC3315 - 18.1.2
		dhcpIaNa.setT2(0);		
		DhcpLink clientLink = config.findLinkForAddress(InetAddress.getByName("2001:DB8:1::a"));
		assertNotNull(clientLink);
		Binding binding = manager.findCurrentBinding(clientLink, clientIdOption, 
				dhcpIaNa, null);
		assertNull(binding);
	}
	
	/**
	 * Test create solicit binding.
	 * 
	 * @throws Exception the exception
	 */
	@Test
	public void testCreateSolicitBinding() throws Exception {
		DhcpV6IaNaOption dhcpIaNa = new DhcpV6IaNaOption();
		dhcpIaNa.setIaId(1);
		dhcpIaNa.setT1(0);	// client SHOULD set to zero RFC3315 - 18.1.2
		dhcpIaNa.setT2(0);
		DhcpLink clientLink = config.findLinkForAddress(InetAddress.getByName("2001:DB8:1::a"));
		assertNotNull(clientLink);
		// create a mock message - local address is server port, remote address is client port
		DhcpMessage dhcpMessage = 
				new DhcpV6Message(new InetSocketAddress(DhcpConstants.V6_SERVER_PORT),
						new InetSocketAddress(DhcpConstants.V6_CLIENT_PORT));
		Binding binding = manager.createSolicitBinding(clientLink, clientIdOption, 
				dhcpIaNa, dhcpMessage, IdentityAssoc.OFFERED);
		assertNotNull(binding);
		Binding binding2 = manager.findCurrentBinding(clientLink, clientIdOption, 
				dhcpIaNa, dhcpMessage);
		assertNotNull(binding2);
		assertEquals(binding.getDuid().length, binding2.getDuid().length);
		for (int i=0; i<binding.getDuid().length; i++) {
			assertEquals(binding.getDuid()[i], binding.getDuid()[i]);
		}
		assertEquals(binding.getIaid(), binding2.getIaid());
		assertEquals(binding.getIatype(), binding2.getIatype());
		assertEquals(binding.getState(), binding2.getState());
		assertEquals(binding.getBindingObjects().size(), binding2.getBindingObjects().size());
		Iterator<BindingObject> binding1Addrs = binding.getBindingObjects().iterator();
		Iterator<BindingObject> binding2Addrs = binding2.getBindingObjects().iterator();
		while (binding1Addrs.hasNext()) {
			V6BindingAddress ba1 = (V6BindingAddress) binding1Addrs.next();
			V6BindingAddress ba2 = (V6BindingAddress) binding2Addrs.next();
			assertEquals(ba1.getId(), ba2.getId());
			assertEquals(ba1.getIpAddress(), ba2.getIpAddress());
			assertEquals(ba1.getStartTime().getTime(), ba2.getStartTime().getTime());
			assertEquals(ba1.getPreferredEndTime().getTime(), ba2.getPreferredEndTime().getTime());
			assertEquals(ba1.getValidEndTime().getTime(), ba2.getValidEndTime().getTime());
		}
	}
}
