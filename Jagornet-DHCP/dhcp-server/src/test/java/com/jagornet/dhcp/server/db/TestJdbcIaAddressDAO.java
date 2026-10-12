/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.InetAddress;
import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


// TODO: Auto-generated Javadoc
/**
 * The Class TestJdbcIdentityAssocDAO.
 * 
 * @author agrabil
 */
@Ignore
public class TestJdbcIaAddressDAO extends BaseTestCase
{
    protected static IdentityAssocDAO iaDao;
	protected static IaAddressDAO iaAddrDao;
	protected IdentityAssoc ia;
	
	@BeforeClass
	public static void oneTimeSetUp() 
	{
		initializeContext("jdbc-derby", 1);	
		iaDao = (IdentityAssocDAO) ctx.getBean("identityAssocDAO");		
		iaAddrDao = (IaAddressDAO) ctx.getBean("iaAddressDAO");
	}

	@AfterClass
	public static void oneTimeTearDown() throws Exception
	{
		closeContext();
	}
	
	@Before
	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		byte[] duid = new byte[] { (byte)0xde, (byte)0xbb, (byte)0x1e, (byte)0xde, (byte)0xbb, (byte)0x1e };
		byte iatype = 1;
		long iaid = (long)0xdebdeb00;
		
		ia = new IdentityAssoc();
		ia.setDuid(duid);
		ia.setIatype(iatype);
		ia.setIaid(iaid);
		ia.setState((byte)1);
		
		iaDao.create(ia);
	}
	
	@After
	@Override
	public void tearDown() throws Exception
	{
		super.tearDown();
		iaDao.deleteById(ia.getId());
	}	
	
	@Test
	public void testCreateIPv6Address() throws Exception
	{
		InetAddress ipv6Addr = InetAddress.getByName("db80::1");
		IaAddress iaaddr = new IaAddress();
		iaaddr.setIdentityAssocId(ia.getId());
		iaaddr.setIpAddress(ipv6Addr);
		Date now = new Date();
		iaaddr.setStartTime(now);
		now.setTime(now.getTime()+10000);
		iaaddr.setPreferredEndTime(now);
		iaaddr.setValidEndTime(now);
		
		iaAddrDao.create(iaaddr);
		
		IaAddress iaaddr2 = iaAddrDao.getByInetAddress(ipv6Addr);
		assertNotNull(iaaddr2);
		assertEquals(ipv6Addr.getHostAddress(), iaaddr2.getIpAddress().getHostAddress());
	}
	
	@Test
	public void testCreateIPv4Address() throws Exception
	{
		InetAddress ipv4Addr = InetAddress.getByName("10.0.0.1");
		IaAddress iaaddr = new IaAddress();
		iaaddr.setIdentityAssocId(ia.getId());
		iaaddr.setIpAddress(ipv4Addr);
		Date now = new Date();
		iaaddr.setStartTime(now);
		now.setTime(now.getTime()+10000);
		iaaddr.setPreferredEndTime(now);
		iaaddr.setValidEndTime(now);
		
		iaAddrDao.create(iaaddr);
		
		IaAddress iaaddr2 = iaAddrDao.getByInetAddress(ipv4Addr);
		assertNotNull(iaaddr2);
		assertEquals(ipv4Addr.getHostAddress(), iaaddr2.getIpAddress().getHostAddress());
	}

}
