package com.jagornet.dhcp.server.db;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MariaDBContainer;

import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;

public class TestMysqlJdbcLeaseManagerWithCache extends AbstractTestLeaseManager {

	private static Logger log = LoggerFactory.getLogger(TestMysqlJdbcLeaseManagerWithCache.class);

	private static MariaDBContainer<?> mariadbContainer;

	@BeforeClass
	public static void oneTimeSetUp() throws Exception
	{
		boolean dockerAvailable = false;
		try {
			dockerAvailable = DockerClientFactory.instance().isDockerAvailable();
		} catch (Throwable t) {
			log.warn("Docker not available for TestMysqlJdbcLeaseManagerWithCache: " + t.getMessage());
		}
		Assume.assumeTrue("Docker is not available - skipping MySQL/MariaDB container test", dockerAvailable);

		mariadbContainer = new MariaDBContainer<>("mariadb:10.11")
				.withDatabaseName("jagornet")
				.withUsername("jagornet")
				.withPassword("jagornet");
		mariadbContainer.start();

		System.setProperty("jdbc.driver", mariadbContainer.getDriverClassName());
		System.setProperty("jdbc.url", mariadbContainer.getJdbcUrl());
		System.setProperty("jdbc.user", mariadbContainer.getUsername());
		System.setProperty("jdbc.password", mariadbContainer.getPassword());

		AbstractTestLeaseManager.oneTimeSetUp();
		DhcpServerPolicies.setProperty(Property.BINDING_MANAGER_IA_CACHE_SIZE, "1000");
		DhcpServerPolicies.setProperty(Property.BINDING_MANAGER_LEASE_CACHE_SIZE, "1000");
		initializeContext(DbSchemaManager.SCHEMATYPE_JDBC_MYSQL, BaseTestCase.DEFAULT_SCHEMA_VERSION);
		leaseManager = (LeaseManager) config.getIaMgr();
		log.info("oneTimeSetUp complete");
	}

	@AfterClass
	public static void oneTimeTearDown() throws Exception
	{
		try {
			if (leaseManager != null) {
				AbstractTestLeaseManager.oneTimeTearDown();
			}
		} finally {
			System.clearProperty("jdbc.driver");
			System.clearProperty("jdbc.url");
			System.clearProperty("jdbc.user");
			System.clearProperty("jdbc.password");
			if (mariadbContainer != null) {
				mariadbContainer.stop();
			}
		}
		log.info("oneTimeTearDown complete");
	}

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@org.junit.After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@org.junit.Test
	public void testFindUnusedLeases() throws Exception {
		AbstractTestLeaseManager.findUnusedLeases();
	}

	@org.junit.Test
	public void testFindExpiredLeases() throws Exception {
		AbstractTestLeaseManager.findExpiredLeases();
	}
}
