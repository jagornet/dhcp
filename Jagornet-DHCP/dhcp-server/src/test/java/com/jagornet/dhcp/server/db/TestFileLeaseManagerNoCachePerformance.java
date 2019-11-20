package com.jagornet.dhcp.server.db;

import java.time.Duration;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;

public class TestFileLeaseManagerNoCachePerformance extends AbstractTestLeaseManagerPerformance {

	private static Logger log = LoggerFactory.getLogger(TestFileLeaseManagerNoCachePerformance.class);

	@BeforeClass
	public static void oneTimeSetUp() throws Exception
	{
		DhcpServerPolicies.setProperty(Property.BINDING_MANAGER_IA_CACHE_SIZE, "0");
		DhcpServerPolicies.setProperty(Property.BINDING_MANAGER_LEASE_CACHE_SIZE, "0");
		leaseManager = new FileLeaseManager();
		leaseManager.init();
		AbstractTestLeaseManagerPerformance.oneTimeSetUp();
		Duration duration = AbstractTestLeaseManagerPerformance.createIAs();
		log.info("createIAs duration=" + duration);
	}
	
	@AfterClass
	public static void oneTimeTearDown() throws Exception
	{
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
	public void testFindIA() {
		Duration duration = super.findIA();
		log.info("findIA duration=" + duration);
	}

	@Test
	public void testFindIAs() {
		log.info("testFindIAs...");
		Duration duration = super.findIAs();
		log.info("findIAs duration=" + duration);
	}
	
	@Test
	public void testUpdateIA() throws Exception {
		Duration duration = super.updateIA();
		log.info("updateIA duration=" + duration);
	}

	@Test
	public void testUpdateIAs() throws Exception {
		log.info("testUpdateIAs...");
		Duration duration = super.updateIAs();
		log.info("updateIAs duration=" + duration);
	}
}
