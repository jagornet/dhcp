/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestJdbcIdentityAssocDAO.java is part of Jagornet DHCP.
 *
 *   Jagornet DHCP is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Jagornet DHCP is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Jagornet DHCP.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcp.db;

import java.util.Arrays;

import com.jagornet.dhcp.db.IdentityAssoc;
import com.jagornet.dhcp.db.IdentityAssocDAO;

// TODO: Auto-generated Javadoc
/**
 * The Class TestJdbcIdentityAssocDAO.
 * 
 * @author agrabil
 */
public class TestJdbcIdentityAssocDAO extends BaseTestCase
{
	protected IdentityAssocDAO iaDao;
	
	public TestJdbcIdentityAssocDAO() throws Exception 
	{
		super("jdbc-derby", 1);
		iaDao = (IdentityAssocDAO) ctx.getBean("identityAssocDAO");
	}
	
	/**
	 * Test create read delete.
	 */
	public void testCreateReadDelete()
	{
		
		byte[] duid = new byte[] { (byte)0xde, (byte)0xbb, (byte)0x1e, (byte)0xde, (byte)0xbb, (byte)0x1e };
		byte iatype = 1;
		long iaid = (long)0xdebdeb00;
		
		// create
		IdentityAssoc ia = new IdentityAssoc();
		ia.setDuid(duid);
		ia.setIatype(iatype);
		ia.setIaid(iaid);
		ia.setState((byte)1);
		
		iaDao.create(ia);
		
		IdentityAssoc ia2 = iaDao.getByKey(duid, iatype, iaid);
		assertNotNull(ia2);
		
		Long newId = ia2.getId();
		
		assertTrue(Arrays.equals(duid, ia2.getDuid()));
		assertEquals(iatype, ia2.getIatype());
		assertEquals(iaid, ia2.getIaid());
		assertEquals(1, ia2.getState());
		
		iaDao.deleteById(newId);
		
		IdentityAssoc ia3 = iaDao.getById(newId);
		assertNull(ia3);
		
	}

}
