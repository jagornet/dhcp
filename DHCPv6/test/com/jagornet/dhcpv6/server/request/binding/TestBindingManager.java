/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestBindingManager.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.server.request.binding;

import java.net.InetAddress;
import java.util.Iterator;

import com.jagornet.dhcpv6.db.BaseDbTestCase;
import com.jagornet.dhcpv6.db.IdentityAssoc;
import com.jagornet.dhcpv6.option.DhcpClientIdOption;
import com.jagornet.dhcpv6.option.DhcpIaNaOption;
import com.jagornet.dhcpv6.server.config.DhcpLink;
import com.jagornet.dhcpv6.xml.ClientIdOption;
import com.jagornet.dhcpv6.xml.IaNaOption;
import com.jagornet.dhcpv6.xml.OpaqueData;

// TODO: Auto-generated Javadoc
/**
 * The Class TestBindingManager.
 */
public class TestBindingManager extends BaseDbTestCase
{
	/** The manager. */
	private NaAddrBindingManager manager;
	
	/** The client id option. */
	private DhcpClientIdOption clientIdOption;
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.BaseDbTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		manager = (NaAddrBindingManager) ctx.getBean("naAddrBindingManager");
		manager.init();
		OpaqueData opaque = OpaqueData.Factory.newInstance();
		opaque.setHexValue(new byte[] { (byte)0xde, (byte)0xbb, (byte)0x1e,
				(byte)0xde, (byte)0xbb, (byte)0x1e });
		ClientIdOption clientId = ClientIdOption.Factory.newInstance();
		clientId.setOpaqueData(opaque);
		clientIdOption = new DhcpClientIdOption(clientId);
	}
	
	/* (non-Javadoc)
	 * @see org.dbunit.DatabaseTestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		manager = null;
		clientIdOption = null;
	}
	
	/**
	 * Test find no current binding.
	 * 
	 * @throws Exception the exception
	 */
	public void testFindNoCurrentBinding() throws Exception {
		DhcpIaNaOption dhcpIaNa = new DhcpIaNaOption();
		IaNaOption iaNa = dhcpIaNa.getIaNaOption();
		iaNa.setIaId(1);
		iaNa.setT1(0);	// client SHOULD set to zero RFC3315 - 18.1.2
		iaNa.setT2(0);		
		DhcpLink clientLink = config.findLinkForAddress(InetAddress.getByName("2001:DB8:1::a"));
		assertNotNull(clientLink);
		Binding binding = manager.findCurrentBinding(clientLink.getLink(), clientIdOption, 
				dhcpIaNa, null);
		assertNull(binding);
	}
	
	/**
	 * Test create solicit binding.
	 * 
	 * @throws Exception the exception
	 */
	public void testCreateSolicitBinding() throws Exception {
		DhcpIaNaOption dhcpIaNa = new DhcpIaNaOption();
		IaNaOption iaNa = dhcpIaNa.getIaNaOption();
		iaNa.setIaId(1);
		iaNa.setT1(0);	// client SHOULD set to zero RFC3315 - 18.1.2
		iaNa.setT2(0);
		DhcpLink clientLink = config.findLinkForAddress(InetAddress.getByName("2001:DB8:1::a"));
		assertNotNull(clientLink);
		Binding binding = manager.createSolicitBinding(clientLink.getLink(), clientIdOption, 
				dhcpIaNa, null, IdentityAssoc.ADVERTISED);
		assertNotNull(binding);
		Binding binding2 = manager.findCurrentBinding(clientLink.getLink(), clientIdOption, 
				dhcpIaNa, null);
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
			BindingAddress ba1 = (BindingAddress) binding1Addrs.next();
			BindingAddress ba2 = (BindingAddress) binding2Addrs.next();
			assertEquals(ba1.getId(), ba2.getId());
			assertEquals(ba1.getIpAddress(), ba2.getIpAddress());
			assertEquals(ba1.getStartTime().getTime(), ba2.getStartTime().getTime());
			assertEquals(ba1.getPreferredEndTime().getTime(), ba2.getPreferredEndTime().getTime());
			assertEquals(ba1.getValidEndTime().getTime(), ba2.getValidEndTime().getTime());
		}
	}
}
