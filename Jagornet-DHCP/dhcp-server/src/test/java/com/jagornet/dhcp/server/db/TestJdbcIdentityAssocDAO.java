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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * The Class TestJdbcIdentityAssocDAO.
 * 
 * @author agrabil
 */
@Ignore
public class TestJdbcIdentityAssocDAO extends BaseTestCase
{
	protected static IdentityAssocDAO iaDao;
	
	@BeforeClass
	public static void oneTimeSetUp() throws Exception 
	{
		initializeContext(DbSchemaManager.SCHEMATYPE_JDBC_DERBY, BaseTestCase.DEFAULT_SCHEMA_VERSION);
		iaDao = (IdentityAssocDAO) ctx.getBean("identityAssocDAO");
	}

	@AfterClass
	public static void oneTimeTearDown() throws Exception
	{
		closeContext();
	}
	
	/**
	 * Test create read delete.
	 */
	@Test
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
