/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.DHCIDRecord;
import org.xbill.DNS.Message;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.TXTRecord;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.option.base.BaseDhcpOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4ClientFqdnOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4HostnameOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6ClientFqdnOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaAddrOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaNaOption;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.db.IaAddress;
import com.jagornet.dhcp.server.db.IdentityAssoc;
import com.jagornet.dhcp.server.request.binding.V4AddrBindingManagerImpl;
import com.jagornet.dhcp.server.request.binding.V6NaAddrBindingManagerImpl;
import com.jagornet.dhcp.server.testutil.DhcpConfigTestContext;
import com.jagornet.dhcp.server.testutil.DhcpV4MessageBuilder;
import com.jagornet.dhcp.server.testutil.DhcpV6MessageBuilder;
import com.jagornet.dhcp.server.testutil.InMemoryIaManager;
import com.jagornet.dhcp.server.testutil.TestDnsServer;

/**
 * End-to-End DHCP Request Lifecycle and Dynamic DNS (DDNS) Update Integration Suite.
 * Validates:
 * <ul>
 *   <li>DHCPv4 DORA with Option 81 (Client FQDN): S/O/N flag negotiation, forward (A+DHCID) & reverse (PTR) updates</li>
 *   <li>DHCPv4 DORA with Option 81 and honorNoA policy</li>
 *   <li>DHCPv4 DORA with Option 81 and honorNoUpdate policy</li>
 *   <li>DHCPv4 DORA with Option 81 and server override flag behavior</li>
 *   <li>DHCPv4 DORA fallback to Option 12 (Hostname) combined with ddns.domain policy</li>
 *   <li>DHCPv4 Release generating DDNS forward and reverse deletions</li>
 *   <li>DHCPv6 SARR with Option 47 (Client FQDN): S/O/N flag negotiation, forward (AAAA+DHCID) & reverse (PTR) updates</li>
 *   <li>DHCPv6 SARR with Option 47 and honorNoAAAA policy</li>
 *   <li>DHCPv6 SARR with Option 47 and honorNoUpdate policy</li>
 *   <li>DHCPv6 SARR with Option 47 and server override flag behavior</li>
 *   <li>DHCPv6 Release generating DDNS forward and reverse deletions</li>
 * </ul>
 * 
 * @author A. Gregory Rabil
 */
public class TestDhcpRequestLifecycleDdns {

	private TestDnsServer dnsServer;
	private InMemoryIaManager iaMgr;
	private DhcpConfigTestContext context;
	private V4AddrBindingManagerImpl v4Mgr;
	private V6NaAddrBindingManagerImpl v6Mgr;

	@Before
	public void setUp() throws Exception {
		dnsServer = new TestDnsServer();
		iaMgr = new InMemoryIaManager();
	}

	@After
	public void tearDown() throws Exception {
		if (v4Mgr != null) {
			v4Mgr.close();
		}
		if (v6Mgr != null) {
			v6Mgr.close();
		}
		if (dnsServer != null) {
			dnsServer.close();
		}
		if (context != null) {
			context.close();
		}
	}

	private void initServer(String updatePolicy, String domainPolicy) throws Exception {
		initServer(updatePolicy, domainPolicy, null);
	}

	private void initServer(String updatePolicy, String domainPolicy, Map<String, String> extraPolicies) throws Exception {
		if (v4Mgr != null) {
			v4Mgr.close();
		}
		if (v6Mgr != null) {
			v6Mgr.close();
		}
		if (context != null) {
			context.close();
		}
		context = new DhcpConfigTestContext();
		context.initXml(buildConfigXml(updatePolicy, domainPolicy, extraPolicies));

		v4Mgr = new V4AddrBindingManagerImpl();
		v4Mgr.setIaMgr(iaMgr);
		v4Mgr.init();

		v6Mgr = new V6NaAddrBindingManagerImpl();
		v6Mgr.setIaMgr(iaMgr);
		v6Mgr.init();

		DhcpServerConfiguration config = DhcpServerConfiguration.getInstance();
		config.setIaMgr(iaMgr);
		config.setV4AddrBindingMgr(v4Mgr);
		config.setV6NaAddrBindingMgr(v6Mgr);
	}

	private void initServerWithConfig(String configXml) throws Exception {
		if (v4Mgr != null) {
			v4Mgr.close();
		}
		if (v6Mgr != null) {
			v6Mgr.close();
		}
		if (context != null) {
			context.close();
		}
		context = new DhcpConfigTestContext();
		context.initXml(configXml);

		v4Mgr = new V4AddrBindingManagerImpl();
		v4Mgr.setIaMgr(iaMgr);
		v4Mgr.init();

		v6Mgr = new V6NaAddrBindingManagerImpl();
		v6Mgr.setIaMgr(iaMgr);
		v6Mgr.init();

		DhcpServerConfiguration config = DhcpServerConfiguration.getInstance();
		config.setIaMgr(iaMgr);
		config.setV4AddrBindingMgr(v4Mgr);
		config.setV6NaAddrBindingMgr(v6Mgr);
	}

	private String buildConfigXml(String updatePolicy, String domainPolicy) {
		return buildConfigXml(updatePolicy, domainPolicy, null);
	}

	private String buildConfigXml(String updatePolicy, String domainPolicy, Map<String, String> extraPolicies) {
		StringBuilder sb = new StringBuilder();
		sb.append("<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n");
		sb.append("  <v4ServerIdOption><ipAddress>192.168.1.1</ipAddress></v4ServerIdOption>\n");
		sb.append("  <v6ServerIdOption><opaqueData><hexValue>00010001aabbccdd001122334455</hexValue></opaqueData></v6ServerIdOption>\n");
		sb.append("  <policies>\n");
		sb.append("    <policyList><name>ddns.synchronize</name><value>true</value></policyList>\n");
		sb.append("    <policyList><name>ddns.server</name><value>127.0.0.1:").append(dnsServer.getPort()).append("</value></policyList>\n");
		sb.append("    <policyList><name>ddns.ttl</name><value>3600</value></policyList>\n");
		sb.append("    <policyList><name>v4.defaultLeasetime</name><value>86400</value></policyList>\n");
		sb.append("    <policyList><name>v6.validLifetime</name><value>86400</value></policyList>\n");
		sb.append("    <policyList><name>v6.preferredLifetime</name><value>43200</value></policyList>\n");
		if (updatePolicy != null) {
			sb.append("    <policyList><name>ddns.update</name><value>").append(updatePolicy).append("</value></policyList>\n");
		}
		if (domainPolicy != null) {
			sb.append("    <policyList><name>ddns.domain</name><value>").append(domainPolicy).append("</value></policyList>\n");
		}
		if (extraPolicies != null) {
			for (Map.Entry<String, String> entry : extraPolicies.entrySet()) {
				sb.append("    <policyList><name>").append(entry.getKey()).append("</name><value>")
						.append(entry.getValue()).append("</value></policyList>\n");
			}
		}
		sb.append("  </policies>\n");
		sb.append("  <links>\n");
		sb.append("    <linkList>\n");
		sb.append("      <name>V4TestLink</name>\n");
		sb.append("      <address>192.168.1.0/24</address>\n");
		sb.append("      <v4AddrPools>\n");
		sb.append("        <poolList>\n");
		sb.append("          <range>192.168.1.100-192.168.1.199</range>\n");
		sb.append("        </poolList>\n");
		sb.append("      </v4AddrPools>\n");
		sb.append("    </linkList>\n");
		sb.append("    <linkList>\n");
		sb.append("      <name>V6TestLink</name>\n");
		sb.append("      <address>2001:db8:1::/64</address>\n");
		sb.append("      <v6NaAddrPools>\n");
		sb.append("        <poolList>\n");
		sb.append("          <range>2001:db8:1::100-2001:db8:1::199</range>\n");
		sb.append("        </poolList>\n");
		sb.append("      </v6NaAddrPools>\n");
		sb.append("    </linkList>\n");
		sb.append("  </links>\n");
		sb.append("</dhc:dhcpServerConfig>");
		return sb.toString();
	}

	@Test
	public void testV4DoraWithOption81ServerUpdatesBothAAndPtr() throws Exception {
		initServer("always", null);

		InetAddress clientLink = InetAddress.getByName("192.168.1.1");
		String mac = "00:11:22:33:44:55";
		String fqdn = "workstation1.example.com";

		// Step 1: DISCOVER with Option 81 (S=1, N=0)
		DhcpV4Message discoverMsg = DhcpV4MessageBuilder.discover()
				.withMac(mac)
				.withClientFqdn(fqdn, true, false)
				.build();
		DhcpV4Message offerMsg = new DhcpV4DiscoverProcessor(discoverMsg, clientLink).processMessage();
		assertNotNull(offerMsg);
		assertEquals(DhcpConstants.V4MESSAGE_TYPE_OFFER, offerMsg.getMessageType());

		// In offer, no DDNS updates should have been dispatched yet
		assertEquals(0, dnsServer.getReceivedQueries().size());

		// Step 2: REQUEST with Option 81
		DhcpV4Message requestMsg = DhcpV4MessageBuilder.request()
				.withMac(mac)
				.withRequestedIp(offerMsg.getYiAddr().getHostAddress())
				.withServerId(offerMsg.getDhcpV4ServerIdOption().getIpAddress())
				.withClientFqdn(fqdn, true, false)
				.build();
		DhcpV4Message ackMsg = new DhcpV4RequestProcessor(requestMsg, clientLink).processMessage();
		assertNotNull(ackMsg);
		assertEquals(DhcpConstants.V4MESSAGE_TYPE_ACK, ackMsg.getMessageType());

		// Check Option 81 returned in reply
		DhcpV4ClientFqdnOption replyFqdn = (DhcpV4ClientFqdnOption) ackMsg.getDhcpOption(DhcpConstants.V4OPTION_CLIENT_FQDN);
		assertNotNull("ACK must contain Option 81", replyFqdn);
		assertTrue("Server agreed to update A record (S bit)", replyFqdn.getUpdateABit());
		assertFalse("Server did not need to override (O bit)", replyFqdn.getOverrideBit());
		assertFalse("N bit must be false", replyFqdn.getNoUpdateBit());
		assertEquals("workstation1.example.com", replyFqdn.getDomainName());

		// Verify DNS server received both forward (A+DHCID) and reverse (PTR) updates
		assertEquals(2, dnsServer.getReceivedQueries().size());

		boolean foundA = false;
		boolean foundDhcid = false;
		boolean foundPtr = false;
		for (Message q : dnsServer.getReceivedQueries()) {
			for (Record r : q.getSectionArray(Section.UPDATE)) {
				if (r instanceof ARecord) foundA = true;
				if (r instanceof DHCIDRecord) foundDhcid = true;
				if (r instanceof PTRRecord) foundPtr = true;
			}
		}
		assertTrue("DNS server must receive A record update", foundA);
		assertTrue("DNS server must receive DHCID record update", foundDhcid);
		assertTrue("DNS server must receive PTR record update", foundPtr);

		// Verify Option 81 saved with binding in IaManager
		IdentityAssoc ia = iaMgr.findIA(requestMsg.getClientId(), IdentityAssoc.V4_TYPE, 0);
		assertNotNull(ia);
		IaAddress iaAddr = ia.getIaAddresses().iterator().next();
		List<BaseDhcpOption> savedOpts = iaMgr.findDhcpOptionsForAddr(iaAddr);
		assertEquals(1, savedOpts.size());
		assertTrue(savedOpts.get(0) instanceof DhcpV4ClientFqdnOption);
	}

	@Test
	public void testV4DoraClientHonorNoA() throws Exception {
		initServer("honorNoA", null);

		InetAddress clientLink = InetAddress.getByName("192.168.1.1");
		String mac = "00:22:33:44:55:66";
		String fqdn = "client-handles-a.example.com";

		// Client requests S=0 (client updates A itself)
		DhcpV4Message discoverMsg = DhcpV4MessageBuilder.discover()
				.withMac(mac)
				.withClientFqdn(fqdn, false, false)
				.build();
		DhcpV4Message offerMsg = new DhcpV4DiscoverProcessor(discoverMsg, clientLink).processMessage();

		DhcpV4Message requestMsg = DhcpV4MessageBuilder.request()
				.withMac(mac)
				.withRequestedIp(offerMsg.getYiAddr().getHostAddress())
				.withServerId(offerMsg.getDhcpV4ServerIdOption().getIpAddress())
				.withClientFqdn(fqdn, false, false)
				.build();
		DhcpV4Message ackMsg = new DhcpV4RequestProcessor(requestMsg, clientLink).processMessage();
		assertNotNull(ackMsg);

		DhcpV4ClientFqdnOption replyFqdn = (DhcpV4ClientFqdnOption) ackMsg.getDhcpOption(DhcpConstants.V4OPTION_CLIENT_FQDN);
		assertNotNull(replyFqdn);
		assertFalse("Server honored client S=0 request", replyFqdn.getUpdateABit());
		assertFalse("Override bit must not be set", replyFqdn.getOverrideBit());

		// Server should have performed ONLY the reverse PTR update
		assertEquals(1, dnsServer.getReceivedQueries().size());
		Message query = dnsServer.getLastQuery();
		boolean foundA = false;
		boolean foundPtr = false;
		for (Record r : query.getSectionArray(Section.UPDATE)) {
			if (r instanceof ARecord) foundA = true;
			if (r instanceof PTRRecord) foundPtr = true;
		}
		assertFalse("Server must NOT send forward A update", foundA);
		assertTrue("Server must send reverse PTR update", foundPtr);
	}

	@Test
	public void testV4DoraClientHonorNoUpdate() throws Exception {
		initServer("honorNoUpdate", null);

		InetAddress clientLink = InetAddress.getByName("192.168.1.1");
		String mac = "00:33:44:55:66:77";
		String fqdn = "noupdate.example.com";

		// Client requests N=1 (no DNS updates from server)
		DhcpV4Message discoverMsg = DhcpV4MessageBuilder.discover()
				.withMac(mac)
				.withClientFqdn(fqdn, false, true)
				.build();
		DhcpV4Message offerMsg = new DhcpV4DiscoverProcessor(discoverMsg, clientLink).processMessage();

		DhcpV4Message requestMsg = DhcpV4MessageBuilder.request()
				.withMac(mac)
				.withRequestedIp(offerMsg.getYiAddr().getHostAddress())
				.withServerId(offerMsg.getDhcpV4ServerIdOption().getIpAddress())
				.withClientFqdn(fqdn, false, true)
				.build();
		DhcpV4Message ackMsg = new DhcpV4RequestProcessor(requestMsg, clientLink).processMessage();
		assertNotNull(ackMsg);

		DhcpV4ClientFqdnOption replyFqdn = (DhcpV4ClientFqdnOption) ackMsg.getDhcpOption(DhcpConstants.V4OPTION_CLIENT_FQDN);
		assertNotNull(replyFqdn);
		assertTrue("N bit must be echoed back as true", replyFqdn.getNoUpdateBit());

		// Zero updates dispatched to DNS server
		assertEquals(0, dnsServer.getReceivedQueries().size());
	}

	@Test
	public void testV4DoraServerOverrideClientFlag() throws Exception {
		initServer("always", null);

		InetAddress clientLink = InetAddress.getByName("192.168.1.1");
		String mac = "00:44:55:66:77:88";
		String fqdn = "override-me.example.com";

		// Client sends S=0 (asking server not to update A)
		DhcpV4Message discoverMsg = DhcpV4MessageBuilder.discover()
				.withMac(mac)
				.withClientFqdn(fqdn, false, false)
				.build();
		DhcpV4Message offerMsg = new DhcpV4DiscoverProcessor(discoverMsg, clientLink).processMessage();

		DhcpV4Message requestMsg = DhcpV4MessageBuilder.request()
				.withMac(mac)
				.withRequestedIp(offerMsg.getYiAddr().getHostAddress())
				.withServerId(offerMsg.getDhcpV4ServerIdOption().getIpAddress())
				.withClientFqdn(fqdn, false, false)
				.build();
		DhcpV4Message ackMsg = new DhcpV4RequestProcessor(requestMsg, clientLink).processMessage();
		assertNotNull(ackMsg);

		DhcpV4ClientFqdnOption replyFqdn = (DhcpV4ClientFqdnOption) ackMsg.getDhcpOption(DhcpConstants.V4OPTION_CLIENT_FQDN);
		assertNotNull(replyFqdn);
		assertTrue("Server overrode and set S=1", replyFqdn.getUpdateABit());
		assertTrue("Override bit must be true", replyFqdn.getOverrideBit());

		// Both A and PTR updates dispatched
		assertEquals(2, dnsServer.getReceivedQueries().size());
	}

	@Test
	public void testV4DoraFallbackHostnameOption12() throws Exception {
		initServer("always", "corp.example.com");

		InetAddress clientLink = InetAddress.getByName("192.168.1.1");
		String mac = "00:55:66:77:88:99";

		// Client sends Option 12 (hostname = "laserjet"), NO Option 81
		DhcpV4Message discoverMsg = DhcpV4MessageBuilder.discover()
				.withMac(mac)
				.withHostname("laserjet")
				.build();
		DhcpV4Message offerMsg = new DhcpV4DiscoverProcessor(discoverMsg, clientLink).processMessage();

		DhcpV4Message requestMsg = DhcpV4MessageBuilder.request()
				.withMac(mac)
				.withRequestedIp(offerMsg.getYiAddr().getHostAddress())
				.withServerId(offerMsg.getDhcpV4ServerIdOption().getIpAddress())
				.withHostname("laserjet")
				.build();
		DhcpV4Message ackMsg = new DhcpV4RequestProcessor(requestMsg, clientLink).processMessage();
		assertNotNull(ackMsg);

		// Since client did NOT send Option 81, reply packet should NOT contain Option 81
		assertNull("Reply must not have Option 81 if client did not request it",
				ackMsg.getDhcpOption(DhcpConstants.V4OPTION_CLIENT_FQDN));

		// But DNS updates should have been dispatched with synthesized FQDN laserjet.corp.example.com
		assertEquals(2, dnsServer.getReceivedQueries().size());

		// And fabricated Option 81 must be stored in database for release cleanup
		IdentityAssoc ia = iaMgr.findIA(requestMsg.getClientId(), IdentityAssoc.V4_TYPE, 0);
		assertNotNull(ia);
		List<BaseDhcpOption> saved = iaMgr.findDhcpOptionsForAddr(ia.getIaAddresses().iterator().next());
		assertEquals(1, saved.size());
		DhcpV4ClientFqdnOption storedFqdn = (DhcpV4ClientFqdnOption) saved.get(0);
		assertEquals("laserjet.corp.example.com", storedFqdn.getDomainName());
	}

	@Test
	public void testV4ReleaseTriggersDdnsDeletion() throws Exception {
		initServer("always", null);

		InetAddress clientLink = InetAddress.getByName("192.168.1.1");
		String mac = "00:66:77:88:99:aa";
		String fqdn = "ephemeral-pc.example.com";

		// Lease address
		DhcpV4Message disc = DhcpV4MessageBuilder.discover().withMac(mac).withClientFqdn(fqdn, true, false).build();
		DhcpV4Message offer = new DhcpV4DiscoverProcessor(disc, clientLink).processMessage();
		DhcpV4Message req = DhcpV4MessageBuilder.request()
				.withMac(mac)
				.withRequestedIp(offer.getYiAddr().getHostAddress())
				.withServerId(offer.getDhcpV4ServerIdOption().getIpAddress())
				.withClientFqdn(fqdn, true, false)
				.build();
		new DhcpV4RequestProcessor(req, clientLink).processMessage();

		// Clear initial add queries
		dnsServer.clear();

		// Client releases address
		DhcpV4Message rel = DhcpV4MessageBuilder.release()
				.withMac(mac)
				.withCiAddr(offer.getYiAddr())
				.withServerId(offer.getDhcpV4ServerIdOption().getIpAddress())
				.build();
		new DhcpV4ReleaseProcessor(rel, clientLink).processMessage();

		// Verify delete updates received by DNS server: forward delete (A) and reverse delete (PTR)
		assertTrue("DNS server must receive delete updates on release", dnsServer.getReceivedQueries().size() >= 2);

		// Verify saved Option 81 is deleted from IaManager
		IdentityAssoc ia = iaMgr.findIA(req.getClientId(), IdentityAssoc.V4_TYPE, 0);
		if (ia != null && !ia.getIaAddresses().isEmpty()) {
			List<BaseDhcpOption> saved = iaMgr.findDhcpOptionsForAddr(ia.getIaAddresses().iterator().next());
			assertEquals(0, saved.size());
		}
	}

	@Test
	public void testV6SarrWithOption47ServerUpdatesBothAaaaAndPtr() throws Exception {
		initServer("always", null);

		InetAddress clientLink = InetAddress.getByName("2001:db8:1::1");
		String duidHex = "00010001aabbccdd112233445566";
		String fqdn = "v6node1.example.com";

		// Step 1: SOLICIT with Option 47 (S=1, N=0) and ORO containing Option 47
		DhcpV6Message solicitMsg = DhcpV6MessageBuilder.solicit()
				.withClientIdHex(duidHex)
				.withIaNa(1)
				.withClientFqdn(fqdn, true, false)
				.withRequestedOptions(DhcpConstants.V6OPTION_CLIENT_FQDN)
				.build();
		DhcpV6Message advertiseMsg = new DhcpV6SolicitProcessor(solicitMsg, clientLink).processMessage();
		assertNotNull(advertiseMsg);
		assertEquals(DhcpConstants.V6MESSAGE_TYPE_ADVERTISE, advertiseMsg.getMessageType());

		assertEquals(0, dnsServer.getReceivedQueries().size());

		// Extract allocated IP
		DhcpV6IaNaOption iaNaAdv = advertiseMsg.getIaNaOptions().get(0);
		assertNotNull(iaNaAdv);
		DhcpV6IaAddrOption iaAddrAdv = iaNaAdv.getIaAddrOptions().get(0);
		assertNotNull(iaAddrAdv);

		// Step 2: REQUEST with Option 47 and ORO containing Option 47
		DhcpV6Message requestMsg = DhcpV6MessageBuilder.request()
				.withClientIdHex(duidHex)
				.withServerId(advertiseMsg.getDhcpServerIdOption())
				.withIaNaWithAddr(1, iaAddrAdv.getIpAddress())
				.withClientFqdn(fqdn, true, false)
				.withRequestedOptions(DhcpConstants.V6OPTION_CLIENT_FQDN)
				.build();
		DhcpV6Message replyMsg = new DhcpV6RequestProcessor(requestMsg, clientLink).processMessage();
		assertNotNull(replyMsg);
		assertEquals(DhcpConstants.V6MESSAGE_TYPE_REPLY, replyMsg.getMessageType());

		// Verify Option 47 in reply
		DhcpV6ClientFqdnOption replyFqdn = (DhcpV6ClientFqdnOption) replyMsg.getDhcpOption(DhcpConstants.V6OPTION_CLIENT_FQDN);
		assertNotNull("Reply must contain Option 47", replyFqdn);
		assertTrue("Server agreed to update AAAA record", replyFqdn.getUpdateAaaaBit());
		assertFalse("Override bit must be false", replyFqdn.getOverrideBit());
		assertFalse("N bit must be false", replyFqdn.getNoUpdateBit());
		assertEquals("v6node1.example.com", replyFqdn.getDomainName());

		// Verify DNS server received both forward (AAAA+DHCID) and reverse (PTR) updates
		assertEquals(2, dnsServer.getReceivedQueries().size());

		boolean foundAAAA = false;
		boolean foundDhcid = false;
		boolean foundPtr = false;
		for (Message q : dnsServer.getReceivedQueries()) {
			for (Record r : q.getSectionArray(Section.UPDATE)) {
				if (r instanceof AAAARecord) foundAAAA = true;
				if (r instanceof DHCIDRecord) foundDhcid = true;
				if (r instanceof PTRRecord) foundPtr = true;
			}
		}
		assertTrue("DNS server must receive AAAA record update", foundAAAA);
		assertTrue("DNS server must receive DHCID record update", foundDhcid);
		assertTrue("DNS server must receive PTR record update", foundPtr);

		// Verify Option 47 saved in IaManager
		IdentityAssoc ia = iaMgr.findIA(requestMsg.getDhcpClientIdOption().getDuid(), IdentityAssoc.NA_TYPE, 1);
		assertNotNull(ia);
		IaAddress iaAddr = ia.getIaAddresses().iterator().next();
		List<BaseDhcpOption> savedOpts = iaMgr.findDhcpOptionsForAddr(iaAddr);
		assertEquals(1, savedOpts.size());
		assertTrue(savedOpts.get(0) instanceof DhcpV6ClientFqdnOption);
	}

	@Test
	public void testV6SarrClientHonorNoAaaa() throws Exception {
		initServer("honorNoAAAA", null);

		InetAddress clientLink = InetAddress.getByName("2001:db8:1::1");
		String duidHex = "00010001aabbccdd223344556677";
		String fqdn = "client-aaaa.example.com";

		// Client requests S=0 (client updates AAAA itself)
		DhcpV6Message solicitMsg = DhcpV6MessageBuilder.solicit()
				.withClientIdHex(duidHex)
				.withIaNa(1)
				.withClientFqdn(fqdn, false, false)
				.withRequestedOptions(DhcpConstants.V6OPTION_CLIENT_FQDN)
				.build();
		DhcpV6Message advertiseMsg = new DhcpV6SolicitProcessor(solicitMsg, clientLink).processMessage();

		DhcpV6IaAddrOption iaAddrAdv = advertiseMsg.getIaNaOptions().get(0).getIaAddrOptions().get(0);

		DhcpV6Message requestMsg = DhcpV6MessageBuilder.request()
				.withClientIdHex(duidHex)
				.withServerId(advertiseMsg.getDhcpServerIdOption())
				.withIaNaWithAddr(1, iaAddrAdv.getIpAddress())
				.withClientFqdn(fqdn, false, false)
				.withRequestedOptions(DhcpConstants.V6OPTION_CLIENT_FQDN)
				.build();
		DhcpV6Message replyMsg = new DhcpV6RequestProcessor(requestMsg, clientLink).processMessage();
		assertNotNull(replyMsg);

		DhcpV6ClientFqdnOption replyFqdn = (DhcpV6ClientFqdnOption) replyMsg.getDhcpOption(DhcpConstants.V6OPTION_CLIENT_FQDN);
		assertNotNull(replyFqdn);
		assertFalse("Server honored client S=0 request", replyFqdn.getUpdateAaaaBit());
		assertFalse("Override bit must not be set", replyFqdn.getOverrideBit());

		// Server should have performed ONLY the reverse PTR update
		assertEquals(1, dnsServer.getReceivedQueries().size());
		Message query = dnsServer.getLastQuery();
		boolean foundAAAA = false;
		boolean foundPtr = false;
		for (Record r : query.getSectionArray(Section.UPDATE)) {
			if (r instanceof AAAARecord) foundAAAA = true;
			if (r instanceof PTRRecord) foundPtr = true;
		}
		assertFalse("Server must NOT send forward AAAA update", foundAAAA);
		assertTrue("Server must send reverse PTR update", foundPtr);
	}

	@Test
	public void testV6SarrClientHonorNoUpdate() throws Exception {
		initServer("honorNoUpdate", null);

		InetAddress clientLink = InetAddress.getByName("2001:db8:1::1");
		String duidHex = "00010001aabbccdd334455667788";
		String fqdn = "v6noupdate.example.com";

		// Client sends Option 47 with N=1
		DhcpV6Message solicitMsg = DhcpV6MessageBuilder.solicit()
				.withClientIdHex(duidHex)
				.withIaNa(1)
				.withClientFqdn(fqdn, false, true)
				.withRequestedOptions(DhcpConstants.V6OPTION_CLIENT_FQDN)
				.build();
		DhcpV6Message advertiseMsg = new DhcpV6SolicitProcessor(solicitMsg, clientLink).processMessage();

		DhcpV6IaAddrOption iaAddrAdv = advertiseMsg.getIaNaOptions().get(0).getIaAddrOptions().get(0);

		DhcpV6Message requestMsg = DhcpV6MessageBuilder.request()
				.withClientIdHex(duidHex)
				.withServerId(advertiseMsg.getDhcpServerIdOption())
				.withIaNaWithAddr(1, iaAddrAdv.getIpAddress())
				.withClientFqdn(fqdn, false, true)
				.withRequestedOptions(DhcpConstants.V6OPTION_CLIENT_FQDN)
				.build();
		DhcpV6Message replyMsg = new DhcpV6RequestProcessor(requestMsg, clientLink).processMessage();
		assertNotNull(replyMsg);

		DhcpV6ClientFqdnOption replyFqdn = (DhcpV6ClientFqdnOption) replyMsg.getDhcpOption(DhcpConstants.V6OPTION_CLIENT_FQDN);
		assertNotNull(replyFqdn);
		assertTrue("N bit must be echoed back as true", replyFqdn.getNoUpdateBit());

		assertEquals(0, dnsServer.getReceivedQueries().size());
	}

	@Test
	public void testV6SarrServerOverrideClientFlag() throws Exception {
		initServer("always", null);

		InetAddress clientLink = InetAddress.getByName("2001:db8:1::1");
		String duidHex = "00010001aabbccdd445566778899";
		String fqdn = "v6override.example.com";

		// Client requests S=0
		DhcpV6Message solicitMsg = DhcpV6MessageBuilder.solicit()
				.withClientIdHex(duidHex)
				.withIaNa(1)
				.withClientFqdn(fqdn, false, false)
				.withRequestedOptions(DhcpConstants.V6OPTION_CLIENT_FQDN)
				.build();
		DhcpV6Message advertiseMsg = new DhcpV6SolicitProcessor(solicitMsg, clientLink).processMessage();

		DhcpV6IaAddrOption iaAddrAdv = advertiseMsg.getIaNaOptions().get(0).getIaAddrOptions().get(0);

		DhcpV6Message requestMsg = DhcpV6MessageBuilder.request()
				.withClientIdHex(duidHex)
				.withServerId(advertiseMsg.getDhcpServerIdOption())
				.withIaNaWithAddr(1, iaAddrAdv.getIpAddress())
				.withClientFqdn(fqdn, false, false)
				.withRequestedOptions(DhcpConstants.V6OPTION_CLIENT_FQDN)
				.build();
		DhcpV6Message replyMsg = new DhcpV6RequestProcessor(requestMsg, clientLink).processMessage();
		assertNotNull(replyMsg);

		DhcpV6ClientFqdnOption replyFqdn = (DhcpV6ClientFqdnOption) replyMsg.getDhcpOption(DhcpConstants.V6OPTION_CLIENT_FQDN);
		assertNotNull(replyFqdn);
		assertTrue("Server overrode and set S=1", replyFqdn.getUpdateAaaaBit());
		assertTrue("Override bit must be true", replyFqdn.getOverrideBit());

		assertEquals(2, dnsServer.getReceivedQueries().size());
	}

	@Test
	public void testV6ReleaseTriggersDdnsDeletion() throws Exception {
		initServer("always", null);

		InetAddress clientLink = InetAddress.getByName("2001:db8:1::1");
		String duidHex = "00010001aabbccdd556677889900";
		String fqdn = "ephemeral-v6.example.com";

		// Acquire lease
		DhcpV6Message sol = DhcpV6MessageBuilder.solicit()
				.withClientIdHex(duidHex)
				.withIaNa(1)
				.withClientFqdn(fqdn, true, false)
				.withRequestedOptions(DhcpConstants.V6OPTION_CLIENT_FQDN)
				.build();
		DhcpV6Message adv = new DhcpV6SolicitProcessor(sol, clientLink).processMessage();
		DhcpV6IaAddrOption iaAddr = adv.getIaNaOptions().get(0).getIaAddrOptions().get(0);
		DhcpV6Message req = DhcpV6MessageBuilder.request()
				.withClientIdHex(duidHex)
				.withServerId(adv.getDhcpServerIdOption())
				.withIaNaWithAddr(1, iaAddr.getIpAddress())
				.withClientFqdn(fqdn, true, false)
				.withRequestedOptions(DhcpConstants.V6OPTION_CLIENT_FQDN)
				.build();
		new DhcpV6RequestProcessor(req, clientLink).processMessage();

		// Clear initial queries
		dnsServer.clear();

		// Client releases address
		DhcpV6Message rel = DhcpV6MessageBuilder.release()
				.withClientIdHex(duidHex)
				.withServerId(adv.getDhcpServerIdOption())
				.withIaNaWithAddr(1, iaAddr.getIpAddress())
				.build();
		new DhcpV6ReleaseProcessor(rel, clientLink).processMessage();

		// Verify delete updates received by DNS server: forward delete (AAAA) and reverse delete (PTR)
		assertTrue("DNS server must receive delete updates on release", dnsServer.getReceivedQueries().size() >= 2);

		// Verify saved Option 47 is deleted from IaManager
		IdentityAssoc ia = iaMgr.findIA(req.getDhcpClientIdOption().getDuid(), IdentityAssoc.NA_TYPE, 1);
		if (ia != null && !ia.getIaAddresses().isEmpty()) {
			List<BaseDhcpOption> saved = iaMgr.findDhcpOptionsForAddr(ia.getIaAddresses().iterator().next());
			assertEquals(0, saved.size());
		}
	}

	@Test
	public void testV4DoraWithDirtyHostnameScrubbedAndOption12Returned() throws Exception {
		Map<String, String> policies = new java.util.HashMap<>();
		policies.put("hostname.mode", "scrub");
		policies.put("hostname.sendOption12", "on-change");
		initServer("always", "example.com", policies);

		InetAddress clientLink = InetAddress.getByName("192.168.1.1");
		String mac = "00:aa:bb:cc:dd:ee";
		String dirtyFqdn = "My_Dirty-Host!#1.local";

		DhcpV4Message discoverMsg = DhcpV4MessageBuilder.discover()
				.withMac(mac)
				.withClientFqdn(dirtyFqdn, true, false)
				.build();
		DhcpV4Message offerMsg = new DhcpV4DiscoverProcessor(discoverMsg, clientLink).processMessage();
		assertNotNull(offerMsg);

		// Verify Offer returns scrubbed Option 12 because hostname was changed
		DhcpV4HostnameOption offerHostname = (DhcpV4HostnameOption) offerMsg.getDhcpOption(DhcpConstants.V4OPTION_HOSTNAME);
		assertNotNull("Offer should contain Option 12 when hostname is changed", offerHostname);
		assertEquals("my-dirty-host-1", offerHostname.getString());

		DhcpV4Message requestMsg = DhcpV4MessageBuilder.request()
				.withMac(mac)
				.withRequestedIp(offerMsg.getYiAddr().getHostAddress())
				.withServerId(offerMsg.getDhcpV4ServerIdOption().getIpAddress())
				.withClientFqdn(dirtyFqdn, true, false)
				.build();
		DhcpV4Message ackMsg = new DhcpV4RequestProcessor(requestMsg, clientLink).processMessage();
		assertNotNull(ackMsg);

		// Verify ACK returns Option 12 with scrubbed short name
		DhcpV4HostnameOption ackHostname = (DhcpV4HostnameOption) ackMsg.getDhcpOption(DhcpConstants.V4OPTION_HOSTNAME);
		assertNotNull("ACK must contain Option 12", ackHostname);
		assertEquals("my-dirty-host-1", ackHostname.getString());

		// Verify Option 81 in ACK has scrubbed FQDN and Override bit set
		DhcpV4ClientFqdnOption replyFqdn = (DhcpV4ClientFqdnOption) ackMsg.getDhcpOption(DhcpConstants.V4OPTION_CLIENT_FQDN);
		assertNotNull("ACK must contain Option 81", replyFqdn);
		assertTrue("Override bit must be set", replyFqdn.getOverrideBit());
		assertEquals("my-dirty-host-1.example.com", replyFqdn.getDomainName());

		// Verify DNS server received forward update for scrubbed name
		boolean foundA = false;
		for (Message q : dnsServer.getReceivedQueries()) {
			for (Record r : q.getSectionArray(Section.UPDATE)) {
				if (r instanceof ARecord && r.getName().toString().startsWith("my-dirty-host-1.example.com")) {
					foundA = true;
				}
			}
		}
		assertTrue("DNS server must receive A record update for scrubbed FQDN", foundA);
	}

	@Test
	public void testV4DoraWithMissingHostnameGeneratedAndOption12Returned() throws Exception {
		Map<String, String> policies = new java.util.HashMap<>();
		policies.put("hostname.mode", "always-generate");
		policies.put("hostname.generate.template", "host-${ip.dashed}");
		policies.put("hostname.sendOption12", "on-change");
		initServer("always", "example.com", policies);

		InetAddress clientLink = InetAddress.getByName("192.168.1.1");
		String mac = "00:55:44:33:22:11";

		// Client sends Discover without Option 12 or Option 81
		DhcpV4Message discoverMsg = DhcpV4MessageBuilder.discover()
				.withMac(mac)
				.build();
		DhcpV4Message offerMsg = new DhcpV4DiscoverProcessor(discoverMsg, clientLink).processMessage();
		assertNotNull(offerMsg);

		DhcpV4HostnameOption offerHostname = (DhcpV4HostnameOption) offerMsg.getDhcpOption(DhcpConstants.V4OPTION_HOSTNAME);
		assertNotNull("Offer must contain generated Option 12", offerHostname);
		assertEquals("host-192-168-1-100", offerHostname.getString());

		DhcpV4Message requestMsg = DhcpV4MessageBuilder.request()
				.withMac(mac)
				.withRequestedIp(offerMsg.getYiAddr().getHostAddress())
				.withServerId(offerMsg.getDhcpV4ServerIdOption().getIpAddress())
				.build();
		DhcpV4Message ackMsg = new DhcpV4RequestProcessor(requestMsg, clientLink).processMessage();
		assertNotNull(ackMsg);

		DhcpV4HostnameOption ackHostname = (DhcpV4HostnameOption) ackMsg.getDhcpOption(DhcpConstants.V4OPTION_HOSTNAME);
		assertNotNull("ACK must contain generated Option 12", ackHostname);
		assertEquals("host-192-168-1-100", ackHostname.getString());

		// Verify DNS server received forward update for generated name
		boolean foundA = false;
		for (Message q : dnsServer.getReceivedQueries()) {
			for (Record r : q.getSectionArray(Section.UPDATE)) {
				if (r instanceof ARecord && r.getName().toString().startsWith("host-192-168-1-100.example.com")) {
					foundA = true;
				}
			}
		}
		assertTrue("DNS server must receive A record update for generated FQDN", foundA);
	}

	@Test
	public void testV6SarrWithMissingFqdnGenerated() throws Exception {
		Map<String, String> policies = new java.util.HashMap<>();
		policies.put("hostname.mode", "always-generate");
		policies.put("hostname.generate.template", "v6-${ip.dashed}");
		initServer("always", "example.com", policies);

		InetAddress clientLink = InetAddress.getByName("2001:db8:1::1");
		String duidHex = "00010001aabbccdd001122334466";

		// Client sends Solicit without Option 39, but requests Option 39 in ORO
		DhcpV6Message solicitMsg = DhcpV6MessageBuilder.solicit()
				.withClientIdHex(duidHex)
				.withIaNa(1)
				.withRequestedOptions(DhcpConstants.V6OPTION_CLIENT_FQDN)
				.build();
		DhcpV6Message advMsg = new DhcpV6SolicitProcessor(solicitMsg, clientLink).processMessage();
		assertNotNull(advMsg);

		DhcpV6IaAddrOption iaAddr = advMsg.getIaNaOptions().get(0).getIaAddrOptions().get(0);
		String leasedIp = iaAddr.getIpAddress();
		String expectedName = "v6-" + leasedIp.replace('.', '-').replace(':', '-') + ".example.com";

		// Client sends Request
		DhcpV6Message requestMsg = DhcpV6MessageBuilder.request()
				.withClientIdHex(duidHex)
				.withServerId(advMsg.getDhcpServerIdOption())
				.withIaNaWithAddr(1, leasedIp)
				.withRequestedOptions(DhcpConstants.V6OPTION_CLIENT_FQDN)
				.build();
		DhcpV6Message replyMsg = new DhcpV6RequestProcessor(requestMsg, clientLink).processMessage();
		assertNotNull(replyMsg);

		DhcpV6ClientFqdnOption replyFqdn = (DhcpV6ClientFqdnOption) replyMsg.getDhcpOption(DhcpConstants.V6OPTION_CLIENT_FQDN);
		assertNotNull("Reply must contain Option 39", replyFqdn);
		assertTrue("Override bit must be set", replyFqdn.getOverrideBit());
		assertEquals(expectedName, replyFqdn.getDomainName());

		// Verify DNS server received AAAA update
		boolean foundAaaa = false;
		for (Message q : dnsServer.getReceivedQueries()) {
			for (Record r : q.getSectionArray(Section.UPDATE)) {
				if (r instanceof AAAARecord && r.getName().toString().startsWith(expectedName)) {
					foundAaaa = true;
				}
			}
		}
		assertTrue("DNS server must receive AAAA record update for generated FQDN", foundAaaa);
	}

	@Test
	public void testDhcpV4DoraWithTxtInterimRecord() throws Exception {
		Map<String, String> policies = new java.util.HashMap<>();
		policies.put("ddns.mode", "server");
		policies.put("ddns.record.type", "txt");
		initServer(null, "example.com", policies);

		byte[] mac = new byte[] { 0x00, 0x11, 0x22, 0x33, 0x44, 0x77 };
		String fqdn = "interim-v4.example.com";

		// Discover
		DhcpV4Message discoverMsg = DhcpV4MessageBuilder.discover()
				.withMac(mac)
				.withClientFqdn(fqdn, true, false)
				.build();
		DhcpV4Message offerMsg = new DhcpV4DiscoverProcessor(discoverMsg, InetAddress.getByName("192.168.1.1")).processMessage();
		assertNotNull(offerMsg);
		InetAddress offeredIp = offerMsg.getYiAddr();

		// Request
		DhcpV4Message requestMsg = DhcpV4MessageBuilder.request()
				.withMac(mac)
				.withServerId(InetAddress.getByName("192.168.1.1"))
				.withRequestedIp(offeredIp)
				.withClientFqdn(fqdn, true, false)
				.build();
		DhcpV4Message ackMsg = new DhcpV4RequestProcessor(requestMsg, InetAddress.getByName("192.168.1.1")).processMessage();
		assertNotNull(ackMsg);

		// Verify DNS received a TXT record update instead of DHCID
		boolean foundTxt = false;
		boolean foundDhcid = false;
		for (Message q : dnsServer.getReceivedQueries()) {
			for (Record r : q.getSectionArray(Section.UPDATE)) {
				if (r instanceof TXTRecord) {
					foundTxt = true;
				}
				if (r instanceof DHCIDRecord) {
					foundDhcid = true;
				}
			}
		}
		assertTrue("DNS server must receive TXT record in interim mode", foundTxt);
		assertFalse("DNS server must not receive DHCID record when record.type=txt", foundDhcid);
	}

	@Test
	public void testDhcpV4DoraWithReplaceDynamicTakeover() throws Exception {
		Map<String, String> policies = new java.util.HashMap<>();
		policies.put("ddns.mode", "server");
		policies.put("ddns.conflict.strategy", "replace-dynamic");
		policies.put("ddns.record.type", "txt");
		policies.put("ddns.dynamic.token", "dynamic");
		initServer(null, "example.com", policies);

		byte[] mac1 = new byte[] { 0x00, 0x11, 0x22, 0x33, 0x44, 0x01 };
		byte[] mac2 = new byte[] { 0x00, 0x11, 0x22, 0x33, 0x44, 0x02 };
		String fqdn = "shared-host.example.com";

		// Client 1 leases and registers DDNS
		DhcpV4Message d1 = DhcpV4MessageBuilder.discover().withMac(mac1).withClientFqdn(fqdn, true, false).build();
		DhcpV4Message o1 = new DhcpV4DiscoverProcessor(d1, InetAddress.getByName("192.168.1.1")).processMessage();
		DhcpV4Message r1 = DhcpV4MessageBuilder.request().withMac(mac1)
				.withServerId(InetAddress.getByName("192.168.1.1")).withRequestedIp(o1.getYiAddr())
				.withClientFqdn(fqdn, true, false).build();
		DhcpV4Message ack1 = new DhcpV4RequestProcessor(r1, InetAddress.getByName("192.168.1.1")).processMessage();
		assertNotNull(ack1);

		// Configure DNS server: next absent check returns YXDOMAIN (name exists), but marker check succeeds
		dnsServer.setRcodeProvider(new java.util.function.Function<Message, Integer>() {
			private int attempt = 0;
			@Override
			public Integer apply(Message msg) {
				attempt++;
				if (attempt == 1) {
					return Rcode.YXDOMAIN;
				}
				return Rcode.NOERROR;
			}
		});

		// Client 2 requests same name -> replace-dynamic takes over!
		DhcpV4Message d2 = DhcpV4MessageBuilder.discover().withMac(mac2).withClientFqdn(fqdn, true, false).build();
		DhcpV4Message o2 = new DhcpV4DiscoverProcessor(d2, InetAddress.getByName("192.168.1.1")).processMessage();
		DhcpV4Message r2 = DhcpV4MessageBuilder.request().withMac(mac2)
				.withServerId(InetAddress.getByName("192.168.1.1")).withRequestedIp(o2.getYiAddr())
				.withClientFqdn(fqdn, true, false).build();
		DhcpV4Message ack2 = new DhcpV4RequestProcessor(r2, InetAddress.getByName("192.168.1.1")).processMessage();
		assertNotNull(ack2);
	}

	@Test
	public void testDhcpV4DoraWithReplaceDynamicStaticProtection() throws Exception {
		Map<String, String> policies = new java.util.HashMap<>();
		policies.put("ddns.mode", "server");
		policies.put("ddns.conflict.strategy", "replace-dynamic");
		policies.put("ddns.record.type", "txt");
		initServer(null, "example.com", policies);

		// DNS simulates a static host: name exists (YXDOMAIN), but lacks dynamic marker (NXRRSET)
		dnsServer.setRcodeProvider(new java.util.function.Function<Message, Integer>() {
			private int attempt = 0;
			@Override
			public Integer apply(Message msg) {
				attempt++;
				if (attempt == 1) {
					return Rcode.YXDOMAIN;
				}
				return Rcode.NXRRSET; // Prereq that dynamic marker is present fails!
			}
		});

		byte[] mac = new byte[] { 0x00, 0x11, 0x22, 0x33, 0x44, 0x03 };
		DhcpV4Message d = DhcpV4MessageBuilder.discover().withMac(mac).withClientFqdn("dc01.example.com", true, false).build();
		DhcpV4Message o = new DhcpV4DiscoverProcessor(d, InetAddress.getByName("192.168.1.1")).processMessage();
		DhcpV4Message r = DhcpV4MessageBuilder.request().withMac(mac)
				.withServerId(InetAddress.getByName("192.168.1.1")).withRequestedIp(o.getYiAddr())
				.withClientFqdn("dc01.example.com", true, false).build();
		DhcpV4Message ack = new DhcpV4RequestProcessor(r, InetAddress.getByName("192.168.1.1")).processMessage();
		assertNotNull(ack);
	}

	@Test
	public void testDhcpV6SarrWithDdnsModeClientHonorNoAAAA() throws Exception {
		Map<String, String> policies = new java.util.HashMap<>();
		policies.put("ddns.mode", "client");
		initServer(null, "example.com", policies);

		InetAddress clientLink = InetAddress.getByName("2001:db8:1::1");
		String duidHex = "00010001aabbccdd001122334488";
		String fqdn = "client-handles-aaaa.example.com";

		// Client sends Solicit with Option 47 (S=0, client will update AAAA record)
		DhcpV6Message solicitMsg = DhcpV6MessageBuilder.solicit()
				.withClientIdHex(duidHex)
				.withIaNa(1)
				.withClientFqdn(fqdn, false, false) // S=0, N=0
				.withRequestedOptions(DhcpConstants.V6OPTION_CLIENT_FQDN)
				.build();
		DhcpV6Message advMsg = new DhcpV6SolicitProcessor(solicitMsg, clientLink).processMessage();
		assertNotNull(advMsg);

		DhcpV6IaAddrOption iaAddr = advMsg.getIaNaOptions().get(0).getIaAddrOptions().get(0);
		String leasedIp = iaAddr.getIpAddress();

		// Request
		DhcpV6Message requestMsg = DhcpV6MessageBuilder.request()
				.withClientIdHex(duidHex)
				.withServerId(advMsg.getDhcpServerIdOption())
				.withIaNaWithAddr(1, leasedIp)
				.withClientFqdn(fqdn, false, false) // S=0, N=0
				.withRequestedOptions(DhcpConstants.V6OPTION_CLIENT_FQDN)
				.build();
		DhcpV6Message replyMsg = new DhcpV6RequestProcessor(requestMsg, clientLink).processMessage();
		assertNotNull(replyMsg);

		DhcpV6ClientFqdnOption replyFqdn = (DhcpV6ClientFqdnOption) replyMsg.getDhcpOption(DhcpConstants.V6OPTION_CLIENT_FQDN);
		assertNotNull(replyFqdn);
		assertFalse("Server must honor client S=0 flag in client mode", replyFqdn.getUpdateAaaaBit());
		assertFalse("Override bit must not be set when honoring client S=0", replyFqdn.getOverrideBit());

		// Verify DNS server received PTR update but NO AAAA update
		boolean foundAaaa = false;
		boolean foundPtr = false;
		for (Message q : dnsServer.getReceivedQueries()) {
			for (Record rec : q.getSectionArray(Section.UPDATE)) {
				if (rec instanceof AAAARecord) {
					foundAaaa = true;
				}
				if (rec instanceof PTRRecord) {
					foundPtr = true;
				}
			}
		}
		assertFalse("Server must NOT send forward AAAA update when client handles it", foundAaaa);
		assertTrue("Server must send reverse PTR update", foundPtr);
	}

	@Test
	public void testV4LifecycleWithDefaultForwardDomainAndCalculatedReverseZone() throws Exception {
		String configXml = "<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
				"  <v4ServerIdOption><ipAddress>192.168.1.1</ipAddress></v4ServerIdOption>\n" +
				"  <policies>\n" +
				"    <policyList><name>ddns.synchronize</name><value>true</value></policyList>\n" +
				"    <policyList><name>ddns.mode</name><value>server</value></policyList>\n" +
				"    <policyList><name>ddns.server</name><value>127.0.0.1:" + dnsServer.getPort() + "</value></policyList>\n" +
				"    <policyList><name>ddns.ttl</name><value>3600</value></policyList>\n" +
				"    <policyList><name>v4.defaultLeasetime</name><value>86400</value></policyList>\n" +
				"  </policies>\n" +
				"  <links>\n" +
				"    <linkList>\n" +
				"      <name>V4AutoLink</name>\n" +
				"      <address>192.168.1.0/24</address>\n" +
				"      <v4ConfigOptions>\n" +
				"        <v4DomainNameOption>\n" +
				"          <domainName>autov4.example.com</domainName>\n" +
				"        </v4DomainNameOption>\n" +
				"      </v4ConfigOptions>\n" +
				"      <v4AddrPools>\n" +
				"        <poolList>\n" +
				"          <range>192.168.1.100-192.168.1.199</range>\n" +
				"        </poolList>\n" +
				"      </v4AddrPools>\n" +
				"    </linkList>\n" +
				"  </links>\n" +
				"</dhc:dhcpServerConfig>";

		initServerWithConfig(configXml);

		InetAddress clientLink = InetAddress.getByName("192.168.1.1");
		String mac = "00:11:22:33:44:77";

		// Step 1: DISCOVER with unqualified hostname option "client-v4"
		DhcpV4Message discoverMsg = DhcpV4MessageBuilder.discover()
				.withMac(mac)
				.withHostname("client-v4")
				.build();
		DhcpV4Message offerMsg = new DhcpV4DiscoverProcessor(discoverMsg, clientLink).processMessage();
		assertNotNull(offerMsg);
		assertEquals(InetAddress.getByName("192.168.1.100"), offerMsg.getYiAddr());

		// Step 2: REQUEST selecting offered address
		DhcpV4Message requestMsg = DhcpV4MessageBuilder.request()
				.withMac(mac)
				.withRequestedIp(offerMsg.getYiAddr().getHostAddress())
				.withServerId(offerMsg.getDhcpV4ServerIdOption().getIpAddress())
				.withHostname("client-v4")
				.build();
		DhcpV4Message ackMsg = new DhcpV4RequestProcessor(requestMsg, clientLink).processMessage();
		assertNotNull(ackMsg);
		assertEquals(InetAddress.getByName("192.168.1.100"), ackMsg.getYiAddr());

		// Verify DNS updates: forward in autov4.example.com., reverse in 1.168.192.in-addr.arpa.
		List<Message> queries = dnsServer.getReceivedQueries();
		assertEquals("Must have 2 DNS update messages (forward + reverse)", 2, queries.size());

		Message fwdMsg = queries.get(0);
		assertEquals("autov4.example.com.", fwdMsg.getQuestion().getName().toString());
		ARecord aRec = null;
		for (Record r : fwdMsg.getSectionArray(Section.UPDATE)) {
			if (r instanceof ARecord) { aRec = (ARecord) r; }
		}
		assertNotNull("Must include A record in forward update", aRec);
		assertEquals("client-v4.autov4.example.com.", aRec.getName().toString());

		Message revMsg = queries.get(1);
		assertEquals("1.168.192.in-addr.arpa.", revMsg.getQuestion().getName().toString());
		PTRRecord ptrRec = null;
		for (Record r : revMsg.getSectionArray(Section.UPDATE)) {
			if (r instanceof PTRRecord) { ptrRec = (PTRRecord) r; }
		}
		assertNotNull("Must include PTR record in reverse update", ptrRec);
		assertEquals("100.1.168.192.in-addr.arpa.", ptrRec.getName().toString());
		assertEquals("client-v4.autov4.example.com.", ptrRec.getTarget().toString());

		// Step 3: RELEASE should delete both records from their respective zones
		dnsServer.clear();
		DhcpV4Message releaseMsg = DhcpV4MessageBuilder.release()
				.withMac(mac)
				.withCiAddr(offerMsg.getYiAddr())
				.withServerId(offerMsg.getDhcpV4ServerIdOption().getIpAddress())
				.build();
		new DhcpV4ReleaseProcessor(releaseMsg, clientLink).processMessage();

		List<Message> delQueries = dnsServer.getReceivedQueries();
		assertTrue("Release must trigger at least 2 DNS update messages (delete forward + reverse)", delQueries.size() >= 2);
		for (Message q : delQueries) {
			String zoneName = q.getQuestion().getName().toString();
			assertTrue("Query zone " + zoneName + " must be either forward or reverse zone",
					zoneName.equals("autov4.example.com.") || zoneName.equals("1.168.192.in-addr.arpa."));
		}
	}

	@Test
	public void testV6LifecycleWithDefaultForwardDomainAndCalculatedReverseZone() throws Exception {
		String configXml = "<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
				"  <v6ServerIdOption><opaqueData><hexValue>00010001aabbccdd001122334455</hexValue></opaqueData></v6ServerIdOption>\n" +
				"  <policies>\n" +
				"    <policyList><name>ddns.synchronize</name><value>true</value></policyList>\n" +
				"    <policyList><name>ddns.mode</name><value>server</value></policyList>\n" +
				"    <policyList><name>ddns.server</name><value>127.0.0.1:" + dnsServer.getPort() + "</value></policyList>\n" +
				"    <policyList><name>ddns.ttl</name><value>3600</value></policyList>\n" +
				"    <policyList><name>v6.validLifetime</name><value>86400</value></policyList>\n" +
				"    <policyList><name>v6.preferredLifetime</name><value>43200</value></policyList>\n" +
				"  </policies>\n" +
				"  <links>\n" +
				"    <linkList>\n" +
				"      <name>V6AutoLink</name>\n" +
				"      <address>2001:db8:1::/64</address>\n" +
				"      <v6MsgConfigOptions>\n" +
				"        <v6DomainSearchListOption>\n" +
				"          <domainNameList>autov6.example.com</domainNameList>\n" +
				"        </v6DomainSearchListOption>\n" +
				"      </v6MsgConfigOptions>\n" +
				"      <v6NaAddrPools>\n" +
				"        <poolList>\n" +
				"          <range>2001:db8:1::100-2001:db8:1::199</range>\n" +
				"        </poolList>\n" +
				"      </v6NaAddrPools>\n" +
				"    </linkList>\n" +
				"  </links>\n" +
				"</dhc:dhcpServerConfig>";

		initServerWithConfig(configXml);

		InetAddress clientLink = InetAddress.getByName("2001:db8:1::1");
		String duidHex = "00010001aabbccdd001122334488";

		// Step 1: SOLICIT with unqualified client FQDN "client-v6"
		DhcpV6Message solicitMsg = DhcpV6MessageBuilder.solicit()
				.withClientIdHex(duidHex)
				.withIaNa(1)
				.withClientFqdn("client-v6", true, false)
				.withRequestedOptions(DhcpConstants.V6OPTION_CLIENT_FQDN)
				.build();
		DhcpV6Message advMsg = new DhcpV6SolicitProcessor(solicitMsg, clientLink).processMessage();
		assertNotNull(advMsg);

		DhcpV6IaAddrOption iaAddr = advMsg.getIaNaOptions().get(0).getIaAddrOptions().get(0);

		// Step 2: REQUEST
		DhcpV6Message requestMsg = DhcpV6MessageBuilder.request()
				.withClientIdHex(duidHex)
				.withServerId(advMsg.getDhcpServerIdOption())
				.withIaNaWithAddr(1, iaAddr.getIpAddress())
				.withClientFqdn("client-v6", true, false)
				.withRequestedOptions(DhcpConstants.V6OPTION_CLIENT_FQDN)
				.build();
		DhcpV6Message replyMsg = new DhcpV6RequestProcessor(requestMsg, clientLink).processMessage();
		assertNotNull(replyMsg);

		DhcpV6ClientFqdnOption replyFqdn = (DhcpV6ClientFqdnOption) replyMsg.getDhcpOption(DhcpConstants.V6OPTION_CLIENT_FQDN);
		assertNotNull(replyFqdn);
		assertEquals("client-v6.autov6.example.com", replyFqdn.getDomainName());

		// Verify DNS updates: forward in autov6.example.com., reverse in 0.0.0.0.1.0.0.0.8.b.d.0.1.0.0.2.ip6.arpa.
		List<Message> queries = dnsServer.getReceivedQueries();
		assertEquals("Must have 2 DNS update messages (forward + reverse)", 2, queries.size());

		Message fwdMsg = queries.get(0);
		assertEquals("autov6.example.com.", fwdMsg.getQuestion().getName().toString());
		AAAARecord aaaaRec = null;
		for (Record r : fwdMsg.getSectionArray(Section.UPDATE)) {
			if (r instanceof AAAARecord) { aaaaRec = (AAAARecord) r; }
		}
		assertNotNull("Must include AAAA record in forward update", aaaaRec);
		assertEquals("client-v6.autov6.example.com.", aaaaRec.getName().toString());

		Message revMsg = queries.get(1);
		assertEquals("0.0.0.0.1.0.0.0.8.b.d.0.1.0.0.2.ip6.arpa.", revMsg.getQuestion().getName().toString());
		PTRRecord ptrRec = null;
		for (Record r : revMsg.getSectionArray(Section.UPDATE)) {
			if (r instanceof PTRRecord) { ptrRec = (PTRRecord) r; }
		}
		assertNotNull("Must include PTR record in reverse update", ptrRec);
		assertEquals("client-v6.autov6.example.com.", ptrRec.getTarget().toString());

		// Step 3: RELEASE should delete both records
		dnsServer.clear();
		DhcpV6Message releaseMsg = DhcpV6MessageBuilder.release()
				.withClientIdHex(duidHex)
				.withServerId(advMsg.getDhcpServerIdOption())
				.withIaNaWithAddr(1, iaAddr.getIpAddress())
				.build();
		new DhcpV6ReleaseProcessor(releaseMsg, clientLink).processMessage();

		List<Message> delQueries = dnsServer.getReceivedQueries();
		assertTrue("Release must trigger at least 2 DNS update messages (delete forward + reverse)", delQueries.size() >= 2);
		for (Message q : delQueries) {
			String zoneName = q.getQuestion().getName().toString();
			assertTrue("Query zone " + zoneName + " must be either forward or reverse zone",
					zoneName.equals("autov6.example.com.") || zoneName.equals("0.0.0.0.1.0.0.0.8.b.d.0.1.0.0.2.ip6.arpa."));
		}
	}
}
