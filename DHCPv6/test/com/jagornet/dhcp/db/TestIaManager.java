/*
 * $Id$
 * 
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 *   This file TestJdbcIdentityAssocDAO.java is part of DHCPv6.
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
package com.jagornet.dhcp.db;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.db.DhcpOption;
import com.jagornet.dhcp.db.IaAddress;
import com.jagornet.dhcp.db.IaManager;
import com.jagornet.dhcp.db.IdentityAssoc;

// TODO: Auto-generated Javadoc
/**
 * The Class TestJdbcIaManager.
 * 
 * @author agrabil
 */
public class TestIaManager extends BaseTestCase
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(TestIaManager.class);
	
	/** The ia mgr. */
	protected IaManager iaMgr;
	
	/** The ia. */
	protected IdentityAssoc ia;

	/** The duid. */
	byte[] duid;
	
	/** The iatype. */
	byte iatype;
	
	/** The iaid. */
	long iaid;
	
	/** The onehour. */
	long onehour;
	
	/** The twohours. */
	long twohours;
	
	/** The ip1. */
	IaAddress ip1;
	
	/** The now1. */
	Date now1;
	
	/** The p1. */
	Date p1;
	
	/** The v1. */
	Date v1;
	
	/** The ip2. */
	IaAddress ip2;
	
	/** The now2. */
	Date now2;
	
	/** The p2. */
	Date p2;
	
	/** The v2. */
	Date v2;
	
	/**
	 * Instantiates a new test jdbc ia manager.
	 */
	public TestIaManager() throws Exception
	{
		super("jdbc-derby", 2);
		iaMgr = (IaManager) ctx.getBean("iaManager");
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.BaseDbTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		
		iaMgr.deleteAllIAs();	// clean the database first
		
		duid = new byte[] { (byte)0xde, (byte)0xbb, (byte)0x1e, (byte)0xde, (byte)0xbb, (byte)0x1e };
		iatype = 1;
		iaid = (long)0xdebdeb00;
		onehour = 3600*1000;
		twohours = 7200*1000;
		
		ia = new IdentityAssoc();
		ia.setDuid(duid);
		ia.setIatype(iatype);
		ia.setIaid(iaid);
		ia.setState(IdentityAssoc.ADVERTISED);
		
		Collection<IaAddress> ips = new ArrayList<IaAddress>();
		
		ip1 = new IaAddress();
		ip1.setIpAddress(InetAddress.getByName("3ffe::1"));
		now1 = new Date();
		ip1.setStartTime(now1);
		p1 = new Date(now1.getTime() + onehour);
		ip1.setPreferredEndTime(p1);
		v1 = new Date(now1.getTime() + twohours);
		ip1.setValidEndTime(v1);
		ip1.setState(IaAddress.ADVERTISED);
		
		Collection<DhcpOption> ip1opts = new ArrayList<DhcpOption>();
		
		DhcpOption ip1opt1 = new DhcpOption();
		ip1opt1.setCode(11);
		ip1opt1.setValue(new byte[] { (byte)0x11 } );
		ip1opts.add(ip1opt1);
		
		DhcpOption ip1opt2 = new DhcpOption();
		ip1opt2.setCode(12);
		ip1opt2.setValue(new byte[] { (byte)0x12 } );
		ip1opts.add(ip1opt2);
		
		ip1.setDhcpOptions(ip1opts);
		ips.add(ip1);
		
		ip2 = new IaAddress();
		ip2.setIpAddress(InetAddress.getByName("3ffe::2"));
		now2 = new Date();
		ip2.setStartTime(now2);
		p2 = new Date(now2.getTime() + onehour);
		ip2.setPreferredEndTime(p2);
		v2 = new Date(now2.getTime() + twohours);
		ip2.setValidEndTime(v2);
		ip2.setState(IaAddress.ADVERTISED);
		
		Collection<DhcpOption> ip2opts = new ArrayList<DhcpOption>();
		
		DhcpOption ip2opt1 = new DhcpOption();
		ip2opt1.setCode(21);
		ip2opt1.setValue(new byte[] { (byte)0x21 } );
		ip2opts.add(ip2opt1);
		
		DhcpOption ip2opt2 = new DhcpOption();
		ip2opt2.setCode(22);
		ip2opt2.setValue(new byte[] { (byte)0x22 } );
		ip2opts.add(ip1opt2);
		
		ip2.setDhcpOptions(ip2opts);
		ips.add(ip2);
		
		Collection<DhcpOption> iaopts = new ArrayList<DhcpOption>();
		
		DhcpOption iaopt1 = new DhcpOption();
		iaopt1.setCode(1);
		iaopt1.setValue(new byte[] { (byte)0x01 } );
		iaopts.add(iaopt1);
		
		DhcpOption iaopt2 = new DhcpOption();
		iaopt2.setCode(2);
		iaopt2.setValue(new byte[] { (byte)0x02 } );
		iaopts.add(iaopt2);
		
		ia.setIaAddresses(ips);
		ia.setDhcpOptions(iaopts);		
	}

	/**
	 * Check ia.
	 * 
	 * @param ia2 the ia2
	 */
	private void checkIA(IdentityAssoc ia2)
	{
		assertTrue(Arrays.equals(duid, ia2.getDuid()));
		assertEquals(iatype, ia2.getIatype());
		assertEquals(iaid, ia2.getIaid());
		assertEquals(IdentityAssoc.ADVERTISED, ia2.getState());
		
		Collection<DhcpOption> opts2 = ia2.getDhcpOptions();
		assertNotNull(opts2);
		assertEquals(2, opts2.size());		
	}

	/**
	 * Check ia addr1.
	 * 
	 * @param ia2ip1 the ia2ip1
	 */
	private void checkIaAddr1(IaAddress ia2ip1)
	{
		assertEquals(ip1.getIpAddress(), ia2ip1.getIpAddress());
		assertEquals(now1.getTime(), ia2ip1.getStartTime().getTime());
		assertEquals(p1.getTime(), ia2ip1.getPreferredEndTime().getTime());
		assertEquals(v1.getTime(), ia2ip1.getValidEndTime().getTime());
		assertEquals(IaAddress.ADVERTISED, ip1.getState());
		
		Collection<DhcpOption> ia2ip1opts = ia2ip1.getDhcpOptions();
		assertNotNull(ia2ip1opts);
		assertEquals(2, ia2ip1opts.size());
	}

	/**
	 * Check ia addr2.
	 * 
	 * @param ia2ip2 the ia2ip2
	 */
	private void checkIaAddr2(IaAddress ia2ip2)
	{
		assertEquals(ip2.getIpAddress(), ia2ip2.getIpAddress());
		assertEquals(now2.getTime(), ia2ip2.getStartTime().getTime());
		assertEquals(p2.getTime(), ia2ip2.getPreferredEndTime().getTime());
		assertEquals(v2.getTime(), ia2ip2.getValidEndTime().getTime());
		assertEquals(IaAddress.ADVERTISED, ip2.getState());
		
		Collection<DhcpOption> ia2ip2opts = ia2ip2.getDhcpOptions();
		assertNotNull(ia2ip2opts);
		assertEquals(2, ia2ip2opts.size());
	}
	
	/**
	 * Test create read delete ia.
	 * 
	 * @throws Exception the exception
	 */
	public void testCreateReadDeleteIA() throws Exception
	{	
		log.info("Creating IA");
		iaMgr.createIA(ia);

		log.info("Locating IA");
		IdentityAssoc ia2 = iaMgr.findIA(duid, iatype, iaid);
		assertNotNull(ia2);
		
		checkIA(ia2);
		
		Collection<? extends IaAddress> ips2 = ia2.getIaAddresses();
		assertNotNull(ips2);
		assertEquals(2, ips2.size());
		Iterator<? extends IaAddress> ips2i = ips2.iterator();
		
		IaAddress ia2ip1 = ips2i.next();
		checkIaAddr1(ia2ip1);
		
		IaAddress ia2ip2 = ips2i.next();
		checkIaAddr2(ia2ip2);
		
		log.info("Deleting IA");
		iaMgr.deleteIA(ia2);
		
		IdentityAssoc ia3 = iaMgr.findIA(duid, iatype, iaid);
		assertNull(ia3);
	}

	/**
	 * Test update ia addr.
	 * 
	 * @throws Exception the exception
	 */
	public void testUpdateIaAddr() throws Exception
	{
		log.info("Creating IA");
		iaMgr.createIA(ia);

		// "expire" the first address
		IaAddress addr1 = ia.getIaAddresses().iterator().next();
		addr1.setStartTime(null);
		addr1.setPreferredEndTime(null);
		addr1.setValidEndTime(null);
		iaMgr.updateIaAddr(addr1);
		
		log.info("Locating IA");
		IdentityAssoc ia2 = iaMgr.findIA(duid, iatype, iaid);
		assertNotNull(ia2);
		checkIA(ia2);
		
		Collection<? extends IaAddress> ips2 = ia2.getIaAddresses();
		assertNotNull(ips2);
		assertEquals(2, ips2.size());
		Iterator<? extends IaAddress> ips2i = ips2.iterator();
		
		IaAddress ia2ip1 = ips2i.next();
		assertEquals(ip1.getIpAddress(), ia2ip1.getIpAddress());
		assertNull(ia2ip1.getStartTime());
		assertNull(ia2ip1.getPreferredEndTime());
		assertNull(ia2ip1.getValidEndTime());
		
		Collection<DhcpOption> ia2ip1opts = ia2ip1.getDhcpOptions();
		assertNotNull(ia2ip1opts);
		assertEquals(2, ia2ip1opts.size());
		
		IaAddress ia2ip2 = ips2i.next();
		checkIaAddr2(ia2ip2);
		
		log.info("Deleting IA");
		iaMgr.deleteIA(ia2);
	}

	/**
	 * Test update all ia addrs.
	 * 
	 * @throws Exception the exception
	 */
	public void testUpdateAllIaAddrs() throws Exception
	{
		log.info("Creating IA");
		iaMgr.createIA(ia);

		Iterator<? extends IaAddress> iaAddrIter = ia.getIaAddresses().iterator();
		
		// "expire" the first address
		IaAddress addr1 = iaAddrIter.next();
		addr1.setStartTime(null);
		addr1.setPreferredEndTime(null);
		addr1.setValidEndTime(null);
		iaMgr.updateIaAddr(addr1);

		// "expire" the second address, which should trigger
		// a change to the IdentityAssoc.state
		IaAddress addr2 = iaAddrIter.next();
		addr2.setStartTime(null);
		addr2.setPreferredEndTime(null);
		addr2.setValidEndTime(null);
		iaMgr.updateIaAddr(addr2);
		
		log.info("Locating IA");
		IdentityAssoc ia2 = iaMgr.findIA(duid, iatype, iaid);
		assertNotNull(ia2);
		assertTrue(Arrays.equals(duid, ia2.getDuid()));
		assertEquals(iatype, ia2.getIatype());
		assertEquals(iaid, ia2.getIaid());
//		assertEquals(IdentityAssoc.EXPIRED, ia2.getState());
		
		Collection<DhcpOption> opts2 = ia2.getDhcpOptions();
		assertNotNull(opts2);
		assertEquals(2, opts2.size());		
		
		Collection<? extends IaAddress> ips2 = ia2.getIaAddresses();
		assertNotNull(ips2);
		assertEquals(2, ips2.size());
		Iterator<? extends IaAddress> ips2i = ips2.iterator();
		
		IaAddress ia2ip1 = ips2i.next();
		assertEquals(ip1.getIpAddress(), ia2ip1.getIpAddress());
		assertNull(ia2ip1.getStartTime());
		assertNull(ia2ip1.getPreferredEndTime());
		assertNull(ia2ip1.getValidEndTime());
		
		Collection<DhcpOption> ia2ip1opts = ia2ip1.getDhcpOptions();
		assertNotNull(ia2ip1opts);
		assertEquals(2, ia2ip1opts.size());
		
		IaAddress ia2ip2 = ips2i.next();
		assertEquals(ip2.getIpAddress(), ia2ip2.getIpAddress());
		assertNull(ia2ip2.getStartTime());
		assertNull(ia2ip2.getPreferredEndTime());
		assertNull(ia2ip2.getValidEndTime());
		
		log.info("Deleting IA");
		iaMgr.deleteIA(ia2);
	}

	/**
	 * Test delete ia addr.
	 * 
	 * @throws Exception the exception
	 */
	public void testDeleteIaAddr() throws Exception
	{
		log.info("Creating IA");
		iaMgr.createIA(ia);

		// delete the first address
		IaAddress addr1 = ia.getIaAddresses().iterator().next();
		iaMgr.deleteIaAddr(addr1);
		
		log.info("Locating IA");
		IdentityAssoc ia2 = iaMgr.findIA(duid, iatype, iaid);
		assertNotNull(ia2);
		checkIA(ia2);
		
		Collection<? extends IaAddress> ips2 = ia2.getIaAddresses();
		assertNotNull(ips2);
		assertEquals(1, ips2.size());
		Iterator<? extends IaAddress> ips2i = ips2.iterator();
		
		IaAddress ia2ip2 = ips2i.next();
		checkIaAddr2(ia2ip2);
		
		log.info("Deleting IA");
		iaMgr.deleteIA(ia2);
	}

	/**
	 * Test delete all ia addrs.
	 * 
	 * @throws Exception the exception
	 */
	public void testDeleteAllIaAddrs() throws Exception
	{
		log.info("Creating IA");
		iaMgr.createIA(ia);

		Iterator<? extends IaAddress> iaAddrIter = ia.getIaAddresses().iterator();
		
		// delete the first address
		IaAddress addr1 = iaAddrIter.next();
		iaMgr.deleteIaAddr(addr1);
		
		// delete the second address, which should trigger
		// a delete of the IdentityAssoc
		IaAddress addr2 = iaAddrIter.next();
		iaMgr.deleteIaAddr(addr2);
		
		log.info("Locating IA");
		IdentityAssoc ia2 = iaMgr.findIA(duid, iatype, iaid);
		assertNull(ia2);
	}
}
