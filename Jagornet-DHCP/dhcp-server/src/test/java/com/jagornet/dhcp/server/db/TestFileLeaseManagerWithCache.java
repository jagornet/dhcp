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

public class TestFileLeaseManagerWithCache extends AbstractTestLeaseManager {

	private static Logger log = LoggerFactory.getLogger(TestFileLeaseManagerWithCache.class);
	
	@BeforeClass
	public static void oneTimeSetUp() throws Exception
	{
		AbstractTestLeaseManager.oneTimeSetUp();
		DhcpServerPolicies.setProperty(Property.BINDING_MANAGER_IA_CACHE_SIZE, "1000");
		DhcpServerPolicies.setProperty(Property.BINDING_MANAGER_LEASE_CACHE_SIZE, "1000");
		initializeContext("file", 2);
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
}
