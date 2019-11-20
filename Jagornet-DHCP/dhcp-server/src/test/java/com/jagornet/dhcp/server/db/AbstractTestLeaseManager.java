package com.jagornet.dhcp.server.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;

public class AbstractTestLeaseManager extends BaseTestCase {

	private static Logger log = LoggerFactory.getLogger(AbstractTestLeaseManager.class);

	protected static InetAddress startIp = null;
	protected static InetAddress endIp = null;
	protected static LeaseManager leaseManager = null;
	
	protected static int leaseTime;
	
	public static void oneTimeSetUp() throws Exception
	{
		log.info("oneTimeSetUp starting");
		DhcpServerPolicies.setProperty(Property.BINDING_MANAGER_REAPER_STARTUP_DELAY, "1000");
		DhcpServerPolicies.setProperty(Property.BINDING_MANAGER_REAPER_RUN_PERIOD, "10000");
		DhcpServerPolicies.setProperty(Property.BINDING_MANAGER_OFFER_EXPIRATION, "2000");
		log.info("oneTimeSetUp complete");
	}
	
	public static void oneTimeTearDown() throws Exception
	{
		log.info("oneTimeTearDown starting");
		closeContext();
		log.info("oneTimeTearDown complete");
	}
	
	public void setUp() throws Exception {
		super.setUp();
		Date now = new Date();
		// set expiration to ten seconds from now
		leaseTime = 1000*10;
		Date end = new Date(now.getTime() + leaseTime);
		startIp = InetAddress.getByName("10.0.0.0");
		DhcpLease lease = null;
		for (int i=0; i<10; i++) {
			lease = new DhcpLease();
			BigInteger ip = new BigInteger(startIp.getAddress()).add(BigInteger.valueOf(i));
			try {
				InetAddress ipaddr = InetAddress.getByAddress(ip.toByteArray());
				lease.setIpAddress(ipaddr);
				lease.setDuid(BigInteger.valueOf(i).toByteArray());
				lease.setIaid(1);
				lease.setIatype((byte)IdentityAssoc.V4_TYPE);
				lease.setStartTime(now);
				lease.setPreferredEndTime(end);
				lease.setValidEndTime(end);
				if (i < 5) {
					lease.setState(IaAddress.ADVERTISED);
				}
				else {
					lease.setState(IaAddress.COMMITTED);
				}
				// TODO: add options
				IdentityAssoc ia = LeaseManager.toIdentityAssoc(lease);
				leaseManager.createIA(ia);
				log.debug("Inserted: " + ia);
			}
			catch (UnknownHostException ex) {
				
			}
		}
		endIp = lease.getIpAddress(); 
	}

	public void tearDown() throws Exception {
		super.tearDown();
		leaseManager.deleteAllLeases();
	}

	public static void findUnusedLeases() throws Exception {
		DhcpLease lease = leaseManager.findDhcpLeaseForInetAddr(startIp);
		log.info("Lease: " + lease.toString());
		List<IaAddress> unused = leaseManager.findUnusedIaAddresses(startIp, endIp);
		assertTrue(unused.isEmpty());
		long sleep = leaseManager.getOfferExpireMillis();
		log.info("Waiting " + sleep + "ms for offer expiration...");
		Thread.sleep(sleep);
		log.info("Finding unused IaAddresses as of: " + new Date());
		unused = leaseManager.findUnusedIaAddresses(startIp, endIp);
		assertNotNull(unused);
		// should find 5 advertised leases that were never requested
		// meaning that the offers have expired
		assertEquals(5, unused.size());
		sleep = leaseTime;
		log.info("Waiting " + sleep + "ms for lease expiration...");
		Thread.sleep(sleep);
		log.info("Finding unused IaAddresses as of: " + new Date());
		unused = leaseManager.findUnusedIaAddresses(startIp, endIp);
		assertNotNull(unused);
		// should find 5 advertised leases that were never requested
		// meaning that the offers have expired + 5 expired leases
		assertEquals(10, unused.size());
	}

	public static void findExpiredLeases() throws Exception {
		DhcpLease lease = leaseManager.findDhcpLeaseForInetAddr(endIp);
		lease.setStartTime(null);
		lease.setPreferredEndTime(null);
		lease.setValidEndTime(null);
		lease.setState(IaAddress.RELEASED);
		leaseManager.updateDhcpLease(lease);
		List<DhcpLease> expired = leaseManager.findExpiredLeases(IdentityAssoc.V4_TYPE);
		if (expired != null) {
			log.debug("Found " + expired.size() + " expired leases, first one: " + expired.get(0));
		}
		else {
			log.debug("No expired leases found");
		}
		assertTrue(expired.isEmpty());
	}
}
