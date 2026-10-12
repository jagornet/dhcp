/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.db;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;

public class TestDerbyJdbcLeaseManagerWithCache extends AbstractTestLeaseManager {

	private static Logger log = LoggerFactory.getLogger(TestDerbyJdbcLeaseManagerWithCache.class);
	
	@BeforeClass
	public static void oneTimeSetUp() throws Exception
	{
		AbstractTestLeaseManager.oneTimeSetUp();
		DhcpServerPolicies.setProperty(Property.BINDING_MANAGER_IA_CACHE_SIZE, "1000");
		DhcpServerPolicies.setProperty(Property.BINDING_MANAGER_LEASE_CACHE_SIZE, "1000");
		initializeContext(DbSchemaManager.SCHEMATYPE_JDBC_DERBY, BaseTestCase.DEFAULT_SCHEMA_VERSION);
		leaseManager =  (LeaseManager)config.getIaMgr();
		log.info("oneTimeSetUp complete");
	}
	
	@AfterClass
	public static void oneTimeTearDown() throws Exception
	{
		AbstractTestLeaseManager.oneTimeTearDown();
		log.info("oneTimeTearDown complete");
	}
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void testFindUnusedLeases() throws Exception {
		AbstractTestLeaseManager.findUnusedLeases();
	}
	
	@Test
	public void testFindExpiredLeases() throws Exception {
		AbstractTestLeaseManager.findExpiredLeases();
	}
}
