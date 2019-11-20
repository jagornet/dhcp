package com.jagornet.dhcp.server.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTestLeaseManagerPerformance extends BaseTestCase {

	private static Logger log = LoggerFactory.getLogger(AbstractTestLeaseManagerPerformance.class);

	protected static InetAddress startIp = null;
	protected static InetAddress endIp = null;
	protected static LeaseManager leaseManager = null;
	protected static int LEASE_COUNT = 1000;
	protected static int LEASE_TIME = 60*1000;	// one minute
	protected static List<DhcpLease> mockLeases = null;
	protected static Random rand = new Random();

	public static void oneTimeSetUp() throws Exception
	{
		startIp = InetAddress.getByName("10.0.0.0");
		mockLeases = createMockLeases(startIp, LEASE_COUNT);
		endIp = mockLeases.get(LEASE_COUNT-1).getIpAddress();
	}
	
	public static void oneTimeTearDown() throws Exception
	{
		closeContext();
	}
	
	public void setUp() throws Exception {
		super.setUp();
	}

	public void tearDown() throws Exception {
		super.tearDown();
	}
	
	public static Duration createIAs() throws Exception {
		Instant t1 = Instant.now();
		for (DhcpLease dhcpLease : mockLeases) {
			leaseManager.createIA(LeaseManager.toIdentityAssoc(dhcpLease));
		}
		Instant t2 = Instant.now();
		return Duration.between(t1, t2);
	}
	
	public static Duration findIA() {
		DhcpLease dhcpLease = mockLeases.get(rand.nextInt(LEASE_COUNT));
		assertNotNull(dhcpLease);
		Instant t1 = Instant.now();
		IdentityAssoc found = leaseManager.findIA(dhcpLease.getDuid(),
												  dhcpLease.getIatype(),
												  dhcpLease.getIaid());
		Instant t2 = Instant.now();
		assertNotNull(found);
		log.info("Found IA: " + found);
		List<DhcpLease> leases = LeaseManager.toDhcpLeases(found);
		assertEquals(1, leases.size());
		assertEquals(dhcpLease, leases.get(0));
		return Duration.between(t1, t2);
	}

	public static Duration findIAs() {
		Duration totalDuration = Duration.ZERO;
		for (int i=0; i<LEASE_COUNT; i++) {
			DhcpLease dhcpLease = mockLeases.get(rand.nextInt(LEASE_COUNT));
			assertNotNull(dhcpLease);
			Instant t1 = Instant.now();
			IdentityAssoc found = leaseManager.findIA(dhcpLease.getDuid(), 
													  dhcpLease.getIatype(), 
													  dhcpLease.getIaid());
			Instant t2 = Instant.now();
			assertNotNull(found);
			List<DhcpLease> leases = LeaseManager.toDhcpLeases(found);
			assertEquals(1, leases.size());
			assertEquals(dhcpLease, leases.get(0));
			Duration duration = Duration.between(t1, t2);
			totalDuration = totalDuration.plus(duration);
		}
		return totalDuration;
	}
	
	public static Duration updateIA() throws Exception {
		int index = rand.nextInt(LEASE_COUNT);
		DhcpLease dhcpLease = mockLeases.get(index);
		assertNotNull(dhcpLease);
		Date now = new Date();
		Date tomorrow = new Date(now.getTime() + (1000 * 60 * 60 * 24));
		DhcpLease origDhcpLease = dhcpLease.clone();
		dhcpLease.setStartTime(now);
		dhcpLease.setPreferredEndTime(tomorrow);
		dhcpLease.setValidEndTime(tomorrow);
		IdentityAssoc ia = LeaseManager.toIdentityAssoc(dhcpLease);
		IaAddress iaAddr = LeaseManager.toIaAddress(dhcpLease);
		Instant t1 = Instant.now();
		leaseManager.updateIA(ia, null, Arrays.asList(iaAddr), null);
		Instant t2 = Instant.now();
		log.debug("Replacing: " + origDhcpLease +
				" with: " + dhcpLease);
		mockLeases.set(index, dhcpLease);
		return Duration.between(t1, t2);
	}

	public static Duration updateIAs() throws Exception {
		Duration totalDuration = Duration.ZERO;
		Date now = new Date();
		Date tomorrow = new Date(now.getTime() + (1000 * 60 * 60 * 24));
		for (int i=0; i<LEASE_COUNT; i++) {
			int index = rand.nextInt(LEASE_COUNT);
			DhcpLease dhcpLease = mockLeases.get(index);
			assertNotNull(dhcpLease);
			DhcpLease origDhcpLease = dhcpLease.clone();
			dhcpLease.setStartTime(now);
			dhcpLease.setPreferredEndTime(tomorrow);
			dhcpLease.setValidEndTime(tomorrow);
			IdentityAssoc ia = LeaseManager.toIdentityAssoc(dhcpLease);
			IaAddress iaAddr = LeaseManager.toIaAddress(dhcpLease);
			Instant t1 = Instant.now();
			leaseManager.updateIA(ia, null, Arrays.asList(iaAddr), null);
			Instant t2 = Instant.now();
			Duration duration = Duration.between(t1, t2);
			totalDuration = totalDuration.plus(duration);
			log.debug("Replacing: " + origDhcpLease +
					" with: " + dhcpLease);
			mockLeases.set(index, dhcpLease);
		}
		return totalDuration;
	}
	
	protected static List<DhcpLease> createMockLeases(InetAddress startIp, int count) {
		List<DhcpLease> leases = new ArrayList<DhcpLease>();
		Date now = new Date();
		Date end = new Date(now.getTime() + LEASE_TIME);
		for (int i=0; i<count; i++) {
			DhcpLease lease = new DhcpLease();
			BigInteger ip = new BigInteger(startIp.getAddress()).add(BigInteger.valueOf(i));
			try {
				InetAddress ipaddr = InetAddress.getByAddress(ip.toByteArray());
				lease.setIpAddress(ipaddr);
				lease.setDuid(BigInteger.valueOf(i).toByteArray());
				lease.setIaid(1);
				lease.setIatype((byte)1);
				lease.setStartTime(now);
				lease.setPreferredEndTime(end);
				lease.setValidEndTime(end);
				// TODO: add options
				leases.add(lease);
				log.debug("Created Lease: " + lease);
			}
			catch (UnknownHostException ex) {
				
			}
		}
		return leases;
	}
}
