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
package com.jagornet.dhcpv6.db;

import java.net.InetAddress;
import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * The Class TestJdbcIdentityAssocDAO.
 * 
 * @author agrabil
 */
public class TestJdbcIaAddressDAO extends BaseTestCase
{
	protected IdentityAssocDAO iaDao;
	protected IaAddressDAO iaAddrDao;
	protected IdentityAssoc ia;
	
	public TestJdbcIaAddressDAO() throws Exception 
	{
		super("jdbc-derby", 1);	
		iaDao = (IdentityAssocDAO) ctx.getBean("identityAssocDAO");		
		iaAddrDao = (IaAddressDAO) ctx.getBean("iaAddressDAO");
	}
	
	@Override
	public void setUp() throws Exception
	{
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
	
	@Override
	protected void tearDown() throws Exception
	{
		iaDao.deleteById(ia.getId());
	}
	

}
