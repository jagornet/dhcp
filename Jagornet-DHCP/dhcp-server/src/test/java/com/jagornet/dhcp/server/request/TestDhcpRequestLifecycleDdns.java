/*
 * Copyright 2026 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */
package com.jagornet.dhcp.server.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.DHCIDRecord;
import org.xbill.DNS.Message;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.option.base.BaseDhcpOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4ClientFqdnOption;
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
		context.initXml(buildConfigXml(updatePolicy, domainPolicy));

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
}
