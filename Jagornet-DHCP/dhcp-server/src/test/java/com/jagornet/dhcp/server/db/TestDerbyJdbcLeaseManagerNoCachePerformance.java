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

public class TestDerbyJdbcLeaseManagerNoCachePerformance extends AbstractTestLeaseManagerPerformance {

	private static Logger log = LoggerFactory.getLogger(TestDerbyJdbcLeaseManagerNoCachePerformance.class);

	@BeforeClass
	public static void oneTimeSetUp() throws Exception
	{
		log.info("oneTimeSetUp starting");
		DhcpServerPolicies.setProperty(Property.BINDING_MANAGER_IA_CACHE_SIZE, "0");
		DhcpServerPolicies.setProperty(Property.BINDING_MANAGER_LEASE_CACHE_SIZE, "0");
		initializeContext(DbSchemaManager.SCHEMATYPE_JDBC_DERBY, BaseTestCase.DEFAULT_SCHEMA_VERSION);
		leaseManager =  (LeaseManager)config.getIaMgr();
		AbstractTestLeaseManagerPerformance.oneTimeSetUp();
		Duration duration = AbstractTestLeaseManagerPerformance.createIAs();
		log.info("createIAs duration=" + duration);
		log.info("oneTimeSetUp complete");
	}
	
	@AfterClass
	public static void oneTimeTearDown() throws Exception
	{
		log.info("oneTimeTearDown starting");
		AbstractTestLeaseManagerPerformance.oneTimeTearDown();
		log.info("oneTimeTearDown starting");
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

	/*
	@Test
	public void testFindUnusedIaAddresses() throws Exception {
		List<IaAddress> iaAddrs = leaseManager.findUnusedIaAddresses(startIp, endIp);
		assertTrue(iaAddrs.isEmpty());
		log.info("Waiting for lease expiration...");
		Thread.sleep(LEASE_TIME);
		iaAddrs = leaseManager.findUnusedIaAddresses(startIp, endIp);
		assertNotNull(iaAddrs);
		assertEquals(LEASE_COUNT, iaAddrs.size());
	}
	*/
}
