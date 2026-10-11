/*
 * Copyright 2026 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */
package com.jagornet.dhcp.server.request.ddns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.DHCIDRecord;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

import com.jagornet.dhcp.core.option.v4.DhcpV4ClientFqdnOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6ClientFqdnOption;
import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.db.IaAddress;
import com.jagornet.dhcp.server.request.binding.V4BindingAddress;
import com.jagornet.dhcp.server.request.binding.V6BindingAddress;
import com.jagornet.dhcp.server.testutil.DhcpConfigTestContext;
import com.jagornet.dhcp.server.testutil.InMemoryIaManager;
import com.jagornet.dhcp.server.testutil.TestDnsServer;

/**
 * Comprehensive Dynamic DNS (DDNS) Update Engine Test Suite.
 * Validates:
 * <ul>
 * <li>RFC 4701/4703 DHCID generation for IPv4 MAC/duid and IPv6 DUID</li>
 * <li>Reverse IPv4 (in-addr.arpa.) and IPv6 (ip6.arpa.) string generation</li>
 * <li>Reverse zone derivation across various prefix bit lengths</li>
 * <li>Forward DNS (A/AAAA and DHCID) additions, deletions, and conflict
 * resolution via TestDnsServer</li>
 * <li>Reverse DNS (PTR) updates and deletions via TestDnsServer</li>
 * <li>TSIG HMAC transaction signature generation and verification</li>
 * <li>DdnsUpdater configuration policy resolution (zones, servers, TTL
 * calculation, TSIG)</li>
 * <li>DhcpV4DdnsComplete and DhcpV6DdnsComplete database callback
 * persistence</li>
 * </ul>
 * 
 * @author A. Gregory Rabil
 */
public class TestDdnsEngine {

	private TestDnsServer dnsServer;
	private InMemoryIaManager iaMgr;

	@Before
	public void setUp() throws Exception {
		dnsServer = new TestDnsServer();
		iaMgr = new InMemoryIaManager();
		DhcpServerConfiguration.getInstance().setIaMgr(iaMgr);
	}

	@After
	public void tearDown() throws Exception {
		if (dnsServer != null) {
			dnsServer.close();
		}
	}

	@Test
	public void testDhcidGenerationV4() throws Exception {
		String fqdn = "client1.example.com";
		InetAddress ip = InetAddress.getByName("192.168.1.100");
		byte[] mac = new byte[] { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55 };

		ForwardDdnsUpdate fwd = new ForwardDdnsUpdate(fqdn, ip, mac);

		Field dataField = DdnsUpdate.class.getDeclaredField("data");
		dataField.setAccessible(true);
		byte[] dhcidData = (byte[]) dataField.get(fwd);

		assertNotNull("DHCID data must be generated", dhcidData);
		// RFC 4701 format: 2 bytes identifier type (0x0002) + 1 byte digest type (0x01)
		// + 32 bytes SHA-256 = 35 bytes
		assertEquals(35, dhcidData.length);
		assertEquals(0x00, dhcidData[0]);
		assertEquals(0x02, dhcidData[1]);
		assertEquals(0x01, dhcidData[2]);

		// Verify SHA-256 calculation over duid + fqdn.getBytes()
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] expectedInput = new byte[mac.length + fqdn.getBytes().length];
		System.arraycopy(mac, 0, expectedInput, 0, mac.length);
		System.arraycopy(fqdn.getBytes(), 0, expectedInput, mac.length, fqdn.getBytes().length);
		byte[] expectedHash = md.digest(expectedInput);

		byte[] actualHash = Arrays.copyOfRange(dhcidData, 3, dhcidData.length);
		assertTrue("SHA-256 digest in DHCID must match RFC 4701 calculation",
				Arrays.equals(expectedHash, actualHash));
	}

	@Test
	public void testDhcidGenerationV6() throws Exception {
		String fqdn = "v6host.corp.internal";
		InetAddress ip = InetAddress.getByName("2001:db8:1::50");
		byte[] duid = new byte[] { 0x00, 0x01, 0x00, 0x01, 0x12, 0x34, 0x56, 0x78, (byte) 0xaa, (byte) 0xbb,
				(byte) 0xcc, (byte) 0xdd, (byte) 0xee, (byte) 0xff };

		ForwardDdnsUpdate fwd = new ForwardDdnsUpdate(fqdn, ip, duid);

		Field dataField = DdnsUpdate.class.getDeclaredField("data");
		dataField.setAccessible(true);
		byte[] dhcidData = (byte[]) dataField.get(fwd);

		assertNotNull(dhcidData);
		assertEquals(35, dhcidData.length);
		assertEquals(0x00, dhcidData[0]);
		assertEquals(0x02, dhcidData[1]);
		assertEquals(0x01, dhcidData[2]);
	}

	@Test
	public void testDhcidDistinctForDifferentFqdns() throws Exception {
		InetAddress ip = InetAddress.getByName("192.168.1.100");
		byte[] mac = new byte[] { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55 };

		ForwardDdnsUpdate fwd1 = new ForwardDdnsUpdate("host1.example.com", ip, mac);
		ForwardDdnsUpdate fwd2 = new ForwardDdnsUpdate("host2.example.com", ip, mac);

		Field dataField = DdnsUpdate.class.getDeclaredField("data");
		dataField.setAccessible(true);
		byte[] data1 = (byte[]) dataField.get(fwd1);
		byte[] data2 = (byte[]) dataField.get(fwd2);

		assertFalse("Different FQDNs must generate distinct DHCID hashes",
				Arrays.equals(data1, data2));
	}

	@Test
	public void testReverseIpStringV4() throws Exception {
		InetAddress ip = InetAddress.getByName("192.168.10.25");
		ReverseDdnsUpdate rev = new ReverseDdnsUpdate("host.test", ip, new byte[] { 1 });

		Method m = ReverseDdnsUpdate.class.getDeclaredMethod("buildReverseIpString");
		m.setAccessible(true);
		String revIp = (String) m.invoke(rev);

		assertEquals("25.10.168.192.in-addr.arpa.", revIp);
	}

	@Test
	public void testReverseIpStringV6() throws Exception {
		InetAddress ip = InetAddress.getByName("2001:db8:0:1::abcd");
		ReverseDdnsUpdate rev = new ReverseDdnsUpdate("host.test", ip, new byte[] { 1 });

		Method m = ReverseDdnsUpdate.class.getDeclaredMethod("buildReverseIpString");
		m.setAccessible(true);
		String revIp = (String) m.invoke(rev);

		// 2001:0db8:0000:0001:0000:0000:0000:abcd reversed nibble by nibble:
		// d.c.b.a.0.0.0.0.0.0.0.0.0.0.0.0.1.0.0.0.0.0.0.0.8.b.d.0.1.0.0.2.ip6.arpa.
		assertEquals("d.c.b.a.0.0.0.0.0.0.0.0.0.0.0.0.1.0.0.0.0.0.0.0.8.b.d.0.1.0.0.2.ip6.arpa.", revIp);
	}

	@Test
	public void testReverseZoneNameV4BitLengths() throws Exception {
		InetAddress ip = InetAddress.getByName("10.20.30.40");
		ReverseDdnsUpdate rev = new ReverseDdnsUpdate("host.test", ip, new byte[] { 1 });

		Method buildZone = ReverseDdnsUpdate.class.getDeclaredMethod("buildZoneName", String.class);
		buildZone.setAccessible(true);
		Method buildRevIp = ReverseDdnsUpdate.class.getDeclaredMethod("buildReverseIpString");
		buildRevIp.setAccessible(true);
		String revIp = (String) buildRevIp.invoke(rev);

		// Default bit length 24 (/24) -> 30.20.10.in-addr.arpa.
		rev.setV4RevZoneBitLength(24);
		Name zone24 = (Name) buildZone.invoke(rev, revIp);
		assertEquals("30.20.10.in-addr.arpa.", zone24.toString());

		// Bit length 16 (/16) -> 20.10.in-addr.arpa.
		rev.setV4RevZoneBitLength(16);
		Name zone16 = (Name) buildZone.invoke(rev, revIp);
		assertEquals("20.10.in-addr.arpa.", zone16.toString());

		// Bit length 8 (/8) -> 10.in-addr.arpa.
		rev.setV4RevZoneBitLength(8);
		Name zone8 = (Name) buildZone.invoke(rev, revIp);
		assertEquals("10.in-addr.arpa.", zone8.toString());
	}

	@Test
	public void testReverseZoneNameV6BitLengths() throws Exception {
		InetAddress ip = InetAddress.getByName("2001:db8:1::10");
		ReverseDdnsUpdate rev = new ReverseDdnsUpdate("host.test", ip, new byte[] { 1 });

		Method buildZone = ReverseDdnsUpdate.class.getDeclaredMethod("buildZoneName", String.class);
		buildZone.setAccessible(true);
		Method buildRevIp = ReverseDdnsUpdate.class.getDeclaredMethod("buildReverseIpString");
		buildRevIp.setAccessible(true);
		String revIp = (String) buildRevIp.invoke(rev);

		// Bit length 64 (/64)
		rev.setRevZoneBitLength(64);
		Name zone64 = (Name) buildZone.invoke(rev, revIp);
		assertEquals("0.0.0.0.1.0.0.0.8.b.d.0.1.0.0.2.ip6.arpa.", zone64.toString());

		// Bit length 48 (/48)
		rev.setRevZoneBitLength(48);
		Name zone48 = (Name) buildZone.invoke(rev, revIp);
		assertEquals("1.0.0.0.8.b.d.0.1.0.0.2.ip6.arpa.", zone48.toString());
	}

	@Test
	public void testForwardDdnsAddV4WithMockDns() throws Exception {
		String fqdn = "pc1.example.com";
		InetAddress ip = InetAddress.getByName("192.168.1.100");
		byte[] mac = new byte[] { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55 };

		ForwardDdnsUpdate fwd = new ForwardDdnsUpdate(fqdn, ip, mac);
		fwd.setServer(dnsServer.getHostPort());
		fwd.setTtl(3600);

		boolean success = fwd.sendAdd();
		assertTrue("sendAdd() must succeed with NOERROR response", success);

		assertEquals(1, dnsServer.getReceivedQueries().size());
		Message query = dnsServer.getLastQuery();

		// Check Prerequisites: absent(pc1.example.com.)
		Record[] prereqs = query.getSectionArray(Section.PREREQ);
		assertEquals(1, prereqs.length);
		assertEquals("pc1.example.com.", prereqs[0].getName().toString());
		assertEquals(Type.ANY, prereqs[0].getType()); // absent RRset has type ANY

		// Check Updates: ARecord and DHCIDRecord
		Record[] updates = query.getSectionArray(Section.UPDATE);
		assertEquals(2, updates.length);

		ARecord aRec = null;
		DHCIDRecord dhcidRec = null;
		for (Record r : updates) {
			if (r instanceof ARecord)
				aRec = (ARecord) r;
			if (r instanceof DHCIDRecord)
				dhcidRec = (DHCIDRecord) r;
		}
		assertNotNull("Must contain ARecord in update", aRec);
		assertNotNull("Must contain DHCIDRecord in update", dhcidRec);
		assertEquals("192.168.1.100", aRec.getAddress().getHostAddress());
		assertEquals(3600, aRec.getTTL());
	}

	@Test
	public void testForwardDdnsAddV6WithMockDns() throws Exception {
		String fqdn = "server1.corp.net";
		InetAddress ip = InetAddress.getByName("2001:db8:2::20");
		byte[] duid = new byte[] { 0x00, 0x01, 0x00, 0x01, 0x55, 0x66, 0x77, (byte) 0x88 };

		ForwardDdnsUpdate fwd = new ForwardDdnsUpdate(fqdn, ip, duid);
		fwd.setServer(dnsServer.getHostPort());
		fwd.setTtl(7200);

		boolean success = fwd.sendAdd();
		assertTrue(success);

		Message query = dnsServer.getLastQuery();
		Record[] updates = query.getSectionArray(Section.UPDATE);
		AAAARecord aaaaRec = null;
		for (Record r : updates) {
			if (r instanceof AAAARecord)
				aaaaRec = (AAAARecord) r;
		}
		assertNotNull("Must contain AAAARecord in update", aaaaRec);
		assertEquals("2001:db8:2:0:0:0:0:20", aaaaRec.getAddress().getHostAddress());
		assertEquals(7200, aaaaRec.getTTL());
	}

	@Test
	public void testForwardDdnsConflictResolutionOnYxdomain() throws Exception {
		String fqdn = "pc1.example.com";
		InetAddress ip = InetAddress.getByName("192.168.1.100");
		byte[] mac = new byte[] { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55 };

		// Configure DNS server: first query gets YXDOMAIN (name exists), second query
		// gets NOERROR
		final AtomicInteger callCount = new AtomicInteger(0);
		dnsServer.setRcodeProvider(new java.util.function.Function<Message, Integer>() {
			public Integer apply(Message msg) {
				if (callCount.incrementAndGet() == 1) {
					return Rcode.YXDOMAIN;
				}
				return Rcode.NOERROR;
			}
		});

		ForwardDdnsUpdate fwd = new ForwardDdnsUpdate(fqdn, ip, mac);
		fwd.setServer(dnsServer.getHostPort());
		fwd.setTtl(3600);

		boolean success = fwd.sendAdd();
		assertTrue("sendAdd() must succeed after conflict resolution", success);
		assertEquals(2, dnsServer.getReceivedQueries().size());

		// The second query is the conflict resolution update:
		// Prerequisites must be: present(owner) and present(dhcid)
		Message secondQuery = dnsServer.getReceivedQueries().get(1);
		Record[] prereqs = secondQuery.getSectionArray(Section.PREREQ);
		assertEquals(2, prereqs.length);

		boolean hasOwnerPresent = false;
		boolean hasDhcidPresent = false;
		for (Record r : prereqs) {
			if (r.getType() == Type.ANY)
				hasOwnerPresent = true;
			if (r.getType() == Type.DHCID)
				hasDhcidPresent = true;
		}
		assertTrue("Second query must include present(owner) prerequisite", hasOwnerPresent);
		assertTrue("Second query must include present(dhcid) prerequisite", hasDhcidPresent);
	}

	@Test
	public void testForwardDdnsDeleteWithMockDns() throws Exception {
		String fqdn = "host-to-remove.example.com";
		InetAddress ip = InetAddress.getByName("192.168.1.150");
		byte[] mac = new byte[] { 0x00, 0x55, 0x44, 0x33, 0x22, 0x11 };

		ForwardDdnsUpdate fwd = new ForwardDdnsUpdate(fqdn, ip, mac);
		fwd.setServer(dnsServer.getHostPort());

		boolean success = fwd.sendDelete();
		assertTrue(success);

		// sendDelete() does two passes when NOERROR is returned:
		// 1) present(dhcid) -> delete(A)
		// 2) present(dhcid) -> absent(A), absent(AAAA) -> delete(owner)
		assertEquals(2, dnsServer.getReceivedQueries().size());
		Message first = dnsServer.getReceivedQueries().get(0);
		Record[] firstUpdates = first.getSectionArray(Section.UPDATE);
		assertEquals(1, firstUpdates.length);
		assertTrue(firstUpdates[0] instanceof ARecord);

		Message second = dnsServer.getReceivedQueries().get(1);
		Record[] secondUpdates = second.getSectionArray(Section.UPDATE);
		assertEquals(1, secondUpdates.length);
		assertEquals("host-to-remove.example.com.", secondUpdates[0].getName().toString());
	}

	@Test
	public void testReverseDdnsAddAndReplaceWithMockDns() throws Exception {
		String fqdn = "node1.example.com";
		InetAddress ip = InetAddress.getByName("10.0.0.5");
		byte[] duid = new byte[] { 1, 2, 3 };

		ReverseDdnsUpdate rev = new ReverseDdnsUpdate(fqdn, ip, duid);
		rev.setServer(dnsServer.getHostPort());
		rev.setTtl(1800);

		boolean success = rev.sendAdd();
		assertTrue(success);

		assertEquals(1, dnsServer.getReceivedQueries().size());
		Message query = dnsServer.getLastQuery();

		// Reverse add should delete old PTR and add new PTR
		Record[] updates = query.getSectionArray(Section.UPDATE);
		assertEquals(2, updates.length);

		PTRRecord addedPtr = null;
		for (Record r : updates) {
			if (r instanceof PTRRecord && r.getTTL() == 1800) {
				addedPtr = (PTRRecord) r;
			}
		}
		assertNotNull("Must contain added PTRRecord with TTL", addedPtr);
		assertEquals("5.0.0.10.in-addr.arpa.", addedPtr.getName().toString());
		assertEquals("node1.example.com.", addedPtr.getTarget().toString());
	}

	@Test
	public void testReverseDdnsDeleteWithMockDns() throws Exception {
		String fqdn = "node1.example.com";
		InetAddress ip = InetAddress.getByName("10.0.0.5");
		byte[] duid = new byte[] { 1, 2, 3 };

		ReverseDdnsUpdate rev = new ReverseDdnsUpdate(fqdn, ip, duid);
		rev.setServer(dnsServer.getHostPort());

		boolean success = rev.sendDelete();
		assertTrue(success);

		Message query = dnsServer.getLastQuery();
		Record[] updates = query.getSectionArray(Section.UPDATE);
		assertEquals(1, updates.length);
		assertTrue(updates[0] instanceof PTRRecord);
		assertEquals(0, updates[0].getTTL()); // delete has TTL 0
	}

	@Test
	public void testDdnsWithTsigAuthentication() throws Exception {
		String keyName = "dhcp-key.example.com.";
		String keyData = "c2VjcmV0a2V5ZGF0YTEyMzQ1Njc4OQ=="; // base64 secret

		dnsServer.setTsig(keyName, keyData);

		ForwardDdnsUpdate fwd = new ForwardDdnsUpdate("secure.example.com",
				InetAddress.getByName("192.168.1.77"), new byte[] { 1, 2, 3 });
		fwd.setServer(dnsServer.getHostPort());
		fwd.setTsigKeyName(keyName);
		fwd.setTsigKeyData(keyData);
		fwd.setTtl(600);

		boolean success = fwd.sendAdd();
		assertTrue("sendAdd() with TSIG must succeed", success);

		Message query = dnsServer.getLastQuery();
		assertNotNull("Query must have TSIG record attached", query.getTSIG());
		assertEquals(keyName, query.getTSIG().getName().toString());
	}

	@Test
	public void testDdnsUpdaterPolicyResolution() throws Exception {
		String configXml = "<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
				"  <policies>\n" +
				"    <policyList><name>ddns.synchronize</name><value>true</value></policyList>\n" +
				"    <policyList><name>ddns.forward.zone.name</name><value>custom.fwd.zone.</value></policyList>\n" +
				"    <policyList><name>ddns.reverse.zone.name</name><value>custom.rev.zone.</value></policyList>\n" +
				"    <policyList><name>ddns.server</name><value>127.0.0.1:" + dnsServer.getPort()
				+ "</value></policyList>\n" +
				"    <policyList><name>ddns.ttl</name><value>0.5</value></policyList>\n" + // 50% of lifetime
				"    <policyList><name>ddns.tsig.keyName</name><value>test-key.</value></policyList>\n" +
				"    <policyList><name>ddns.tsig.keyData</name><value>YmFzZTY0a2V5ZGF0YQ==</value></policyList>\n" +
				"  </policies>\n" +
				"  <links>\n" +
				"    <linkList>\n" +
				"      <name>TestLink</name>\n" +
				"      <address>192.168.1.0/24</address>\n" +
				"      <v4AddrPools>\n" +
				"        <poolList>\n" +
				"          <range>192.168.1.10-192.168.1.50</range>\n" +
				"        </poolList>\n" +
				"      </v4AddrPools>\n" +
				"    </linkList>\n" +
				"  </links>\n" +
				"</dhc:dhcpServerConfig>";

		DhcpConfigTestContext context = new DhcpConfigTestContext();
		context.initXml(configXml);

		dnsServer.setTsig("test-key.", "YmFzZTY0a2V5ZGF0YQ==");

		DhcpLink link = DhcpServerConfiguration.getInstance().getLinkMap().values().iterator().next();
		assertNotNull(link);

		final AtomicBoolean fwdDone = new AtomicBoolean(false);
		final AtomicBoolean revDone = new AtomicBoolean(false);

		DdnsCallback callback = new DdnsCallback() {
			public void fwdAddComplete(boolean success) {
				fwdDone.set(success);
			}

			public void fwdDeleteComplete(boolean success) {
			}

			public void revAddComplete(boolean success) {
				revDone.set(success);
			}

			public void revDeleteComplete(boolean success) {
			}
		};

		DdnsUpdater updater = new DdnsUpdater(link.getLink(), null,
				InetAddress.getByName("192.168.1.20"), "client-policy.custom.fwd.zone",
				new byte[] { 1, 2, 3, 4, 5, 6 }, 7200, true, false, callback);

		updater.processUpdates(); // sync=true, executes immediately on current thread

		assertTrue("Forward add must succeed via DdnsUpdater", fwdDone.get());
		assertTrue("Reverse add must succeed via DdnsUpdater", revDone.get());
		assertEquals(2, dnsServer.getReceivedQueries().size());
	}

	@Test
	public void testDhcpV4DdnsCompleteCallbacks() throws Exception {
		IaAddress iaAddr = new IaAddress();
		iaAddr.setIpAddress(InetAddress.getByName("192.168.1.88"));
		V4BindingAddress bindingAddr = new V4BindingAddress(iaAddr, null);

		DhcpV4ClientFqdnOption fqdnOpt = new DhcpV4ClientFqdnOption("device.corp.net");
		DhcpV4DdnsComplete complete = new DhcpV4DdnsComplete(bindingAddr, fqdnOpt);

		complete.fwdAddComplete(true);
		List<com.jagornet.dhcp.core.option.base.BaseDhcpOption> saved = iaMgr.findDhcpOptionsForAddr(bindingAddr);
		assertEquals(1, saved.size());
		assertEquals("device.corp.net", ((DhcpV4ClientFqdnOption) saved.get(0)).getDomainName());

		complete.fwdDeleteComplete(true);
		List<com.jagornet.dhcp.core.option.base.BaseDhcpOption> remaining = iaMgr.findDhcpOptionsForAddr(bindingAddr);
		assertEquals(0, remaining.size());
	}

	@Test
	public void testDhcpV6DdnsCompleteCallbacks() throws Exception {
		IaAddress iaAddr = new IaAddress();
		iaAddr.setIpAddress(InetAddress.getByName("2001:db8:1::88"));
		V6BindingAddress bindingAddr = new V6BindingAddress(iaAddr, null);

		DhcpV6ClientFqdnOption fqdnOpt = new DhcpV6ClientFqdnOption("device6.corp.net");
		DhcpV6DdnsComplete complete = new DhcpV6DdnsComplete(bindingAddr, fqdnOpt);

		complete.fwdAddComplete(true);
		List<com.jagornet.dhcp.core.option.base.BaseDhcpOption> saved = iaMgr.findDhcpOptionsForAddr(bindingAddr);
		assertEquals(1, saved.size());
		assertEquals("device6.corp.net", ((DhcpV6ClientFqdnOption) saved.get(0)).getDomainName());

		complete.fwdDeleteComplete(true);
		List<com.jagornet.dhcp.core.option.base.BaseDhcpOption> remaining = iaMgr.findDhcpOptionsForAddr(bindingAddr);
		assertEquals(0, remaining.size());
	}
}
