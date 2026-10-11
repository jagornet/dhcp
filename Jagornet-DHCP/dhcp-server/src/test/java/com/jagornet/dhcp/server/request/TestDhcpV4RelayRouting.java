/*
 * Copyright 2026 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */
package com.jagornet.dhcp.server.request;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.option.base.BaseOpaqueData;
import com.jagornet.dhcp.core.option.base.DhcpOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4LeaseTimeOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4RelayAgentInfoOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4RoutersOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4ServerIdOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4SubnetSelectionOption;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.db.IaAddress;
import com.jagornet.dhcp.server.db.IdentityAssoc;
import com.jagornet.dhcp.server.request.binding.V4AddrBindingManagerImpl;
import com.jagornet.dhcp.server.testutil.DhcpAssert;
import com.jagornet.dhcp.server.testutil.DhcpConfigTestContext;
import com.jagornet.dhcp.server.testutil.DhcpV4MessageBuilder;
import com.jagornet.dhcp.server.testutil.InMemoryIaManager;

/**
 * DHCPv4 Relay Agent Information (RFC 3046 Option 82) and Routing Test Suite.
 * 
 * Tests include:
 * 1. Option 82 Sub-option parsing & decoding:
 * - Suboption 1: Circuit-ID (RFC 3046)
 * - Suboption 2: Remote-ID (RFC 3046)
 * - Suboption 5: Link Selection (RFC 3527)
 * - Suboption 11: Server ID Override (RFC 5107)
 * - Combined multi-suboption decoding
 * - Malformed payload resilience (truncated, unknown suboptions, empty/null)
 * 2. Routing Precedence:
 * - Standard giaddr routing (remote link selection)
 * - Option 82 Suboption 5 (Link Selection) overriding giaddr
 * - Option 118 (Subnet Selection) overriding Suboption 5 and giaddr
 * - Link Selection ignored when v4.subnetSelection policy is disabled
 * 3. Option 82 Relay Echo-back & Hop Count:
 * - Echoing Option 82 in DHCPOFFER and DHCPACK
 * - Preserving relayed message processing with incremented hops
 * 4. Server ID Override (RFC 5107 Suboption 11):
 * - Overridden server ID in OFFER/ACK when policy enabled
 * - Standard server ID when policy disabled
 * 5. Option 82 Filter Matching:
 * - Matching Option 82 payload in LinkFilter (directed pool and scoped options)
 * - Non-matching request falling back to default pool
 * 6. End-to-End DORA Lifecycle via Relay Agent:
 * - Discover -> Offer -> Request -> Ack -> Renew -> Release with in-memory
 * lease tracking
 * 
 * @author A. Gregory Rabil
 */
public class TestDhcpV4RelayRouting {

	private DhcpConfigTestContext context;
	private InMemoryIaManager iaMgr;
	private V4AddrBindingManagerImpl v4Mgr;

	private static final String LOCAL_IP = "10.0.0.1";
	private static final String RELAY_GIADDR = "192.168.10.1";

	private static final String CONFIG_XML = "<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
			"  <v4ServerIdOption>\n" +
			"    <ipAddress>" + LOCAL_IP + "</ipAddress>\n" +
			"  </v4ServerIdOption>\n" +
			"  <policies>\n" +
			"    <policyList>\n" +
			"      <name>dhcp.sendRequestedOptionsOnly</name>\n" +
			"      <value>false</value>\n" +
			"    </policyList>\n" +
			"    <policyList>\n" +
			"      <name>v4.defaultLeasetime</name>\n" +
			"      <value>86400</value>\n" +
			"    </policyList>\n" +
			"  </policies>\n" +
			"  <links>\n" +
			"    <!-- Local Interface Link -->\n" +
			"    <linkList>\n" +
			"      <name>LocalLink</name>\n" +
			"      <address>10.0.0.0/24</address>\n" +
			"      <v4AddrPools>\n" +
			"        <poolList>\n" +
			"          <range>10.0.0.100-10.0.0.199</range>\n" +
			"        </poolList>\n" +
			"      </v4AddrPools>\n" +
			"    </linkList>\n" +
			"\n" +
			"    <!-- Relayed Link (giaddr routing) -->\n" +
			"    <linkList>\n" +
			"      <name>RelayGiaddrLink</name>\n" +
			"      <address>192.168.10.0/24</address>\n" +
			"      <v4ConfigOptions>\n" +
			"        <v4RoutersOption>\n" +
			"          <ipAddressList>192.168.10.1</ipAddressList>\n" +
			"        </v4RoutersOption>\n" +
			"      </v4ConfigOptions>\n" +
			"      <v4AddrPools>\n" +
			"        <poolList>\n" +
			"          <range>192.168.10.100-192.168.10.199</range>\n" +
			"        </poolList>\n" +
			"      </v4AddrPools>\n" +
			"      <linkFilters>\n" +
			"        <linkFilterList>\n" +
			"          <name>CircuitVipFilter</name>\n" +
			"          <filterExpressions>\n" +
			"            <filterExpressionList>\n" +
			"              <optionExpression code=\"82\" v4=\"true\" operator=\"contains\">\n" +
			"                <opaqueDataOption>\n" +
			"                  <opaqueData>\n" +
			"                    <hexValue>0104beefcafe</hexValue>\n" +
			"                  </opaqueData>\n" +
			"                </opaqueDataOption>\n" +
			"              </optionExpression>\n" +
			"            </filterExpressionList>\n" +
			"          </filterExpressions>\n" +
			"          <policies>\n" +
			"            <policyList>\n" +
			"              <name>v4.defaultLeasetime</name>\n" +
			"              <value>1800</value>\n" +
			"            </policyList>\n" +
			"          </policies>\n" +
			"          <v4AddrPools>\n" +
			"            <poolList>\n" +
			"              <range>192.168.10.200-192.168.10.250</range>\n" +
			"              <policies>\n" +
			"                <policyList>\n" +
			"                  <name>v4.defaultLeasetime</name>\n" +
			"                  <value>1800</value>\n" +
			"                </policyList>\n" +
			"              </policies>\n" +
			"            </poolList>\n" +
			"          </v4AddrPools>\n" +
			"        </linkFilterList>\n" +
			"      </linkFilters>\n" +
			"    </linkList>\n" +
			"\n" +
			"    <!-- Link Selection Link (Suboption 5) -->\n" +
			"    <linkList>\n" +
			"      <name>LinkSelectionLink</name>\n" +
			"      <address>192.168.20.0/24</address>\n" +
			"      <v4ConfigOptions>\n" +
			"        <v4RoutersOption>\n" +
			"          <ipAddressList>192.168.20.1</ipAddressList>\n" +
			"        </v4RoutersOption>\n" +
			"      </v4ConfigOptions>\n" +
			"      <v4AddrPools>\n" +
			"        <poolList>\n" +
			"          <range>192.168.20.100-192.168.20.199</range>\n" +
			"        </poolList>\n" +
			"      </v4AddrPools>\n" +
			"    </linkList>\n" +
			"\n" +
			"    <!-- Subnet Selection Option 118 Link -->\n" +
			"    <linkList>\n" +
			"      <name>SubnetSelection118Link</name>\n" +
			"      <address>192.168.30.0/24</address>\n" +
			"      <v4ConfigOptions>\n" +
			"        <v4RoutersOption>\n" +
			"          <ipAddressList>192.168.30.1</ipAddressList>\n" +
			"        </v4RoutersOption>\n" +
			"      </v4ConfigOptions>\n" +
			"      <v4AddrPools>\n" +
			"        <poolList>\n" +
			"          <range>192.168.30.100-192.168.30.199</range>\n" +
			"        </poolList>\n" +
			"      </v4AddrPools>\n" +
			"    </linkList>\n" +
			"  </links>\n" +
			"</dhc:dhcpServerConfig>";

	@Before
	public void setUp() throws Exception {
		context = new DhcpConfigTestContext();
		context.initXml(CONFIG_XML);

		DhcpServerPolicies.setProperty(Property.V4_SUBNET_SELECTION, "true");
		DhcpServerPolicies.setProperty(Property.V4_SERVER_ID_OVERRIDE, "true");

		iaMgr = new InMemoryIaManager();

		v4Mgr = new V4AddrBindingManagerImpl();
		v4Mgr.setIaMgr(iaMgr);
		v4Mgr.init();

		DhcpServerConfiguration config = DhcpServerConfiguration.getInstance();
		config.setIaMgr(iaMgr);
		config.setV4AddrBindingMgr(v4Mgr);
	}

	@After
	public void tearDown() {
		if (v4Mgr != null) {
			v4Mgr.close();
		}
		if (context != null) {
			context.close();
		}
	}

	// -------------------------------------------------------------------------
	// Option 82 Helper Builders
	// -------------------------------------------------------------------------

	private static DhcpV4RelayAgentInfoOption createOption82(byte[] payload) {
		DhcpV4RelayAgentInfoOption opt82 = new DhcpV4RelayAgentInfoOption();
		opt82.setOpaqueData(new BaseOpaqueData(payload));
		return opt82;
	}

	private static byte[] buildSuboption(int code, byte[] data) {
		byte[] sub = new byte[2 + data.length];
		sub[0] = (byte) code;
		sub[1] = (byte) data.length;
		System.arraycopy(data, 0, sub, 2, data.length);
		return sub;
	}

	private static byte[] buildIpSuboption(int code, String ip) throws Exception {
		return buildSuboption(code, InetAddress.getByName(ip).getAddress());
	}

	private static byte[] concat(byte[]... chunks) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (byte[] chunk : chunks) {
			if (chunk != null) {
				baos.write(chunk);
			}
		}
		return baos.toByteArray();
	}

	// =========================================================================
	// 1. Sub-option Parsing & Decoding Tests
	// =========================================================================

	@Test
	public void testSuboption1CircuitIdParsing() throws Exception {
		byte[] circuitData = "Slot1/Port2/Vlan100".getBytes(StandardCharsets.UTF_8);
		byte[] payload = buildSuboption(1, circuitData);

		DhcpV4RelayAgentInfoOption opt82 = createOption82(payload);
		byte[] decoded = opt82.getSuboptionData(1);
		assertNotNull("Suboption 1 must be parsed", decoded);
		assertArrayEquals("Suboption 1 data must match exactly", circuitData, decoded);
		assertEquals("Slot1/Port2/Vlan100", new String(decoded, StandardCharsets.UTF_8));
	}

	@Test
	public void testSuboption2RemoteIdParsing() throws Exception {
		byte[] remoteData = "Switch-Rack-42-Core".getBytes(StandardCharsets.UTF_8);
		byte[] payload = buildSuboption(2, remoteData);

		DhcpV4RelayAgentInfoOption opt82 = createOption82(payload);
		byte[] decoded = opt82.getSuboptionData(2);
		assertNotNull("Suboption 2 must be parsed", decoded);
		assertArrayEquals("Suboption 2 data must match exactly", remoteData, decoded);
		assertEquals("Switch-Rack-42-Core", new String(decoded, StandardCharsets.UTF_8));
	}

	@Test
	public void testSuboption5LinkSelectionParsing() throws Exception {
		byte[] payload = buildIpSuboption(DhcpConstants.V4OPTION_RELAY_INFO_LINK_SELECTION, "192.168.20.1");

		DhcpV4RelayAgentInfoOption opt82 = createOption82(payload);
		InetAddress linkAddr = opt82.getLinkSelectionAddress();
		assertNotNull("Link selection address must be parsed", linkAddr);
		assertEquals("192.168.20.1", linkAddr.getHostAddress());
	}

	@Test
	public void testSuboption11ServerIdOverrideParsing() throws Exception {
		byte[] payload = buildIpSuboption(DhcpConstants.V4OPTION_RELAY_INFO_SERVER_ID_OVERRIDE, "192.168.100.1");

		DhcpV4RelayAgentInfoOption opt82 = createOption82(payload);
		InetAddress overrideAddr = opt82.getServerIdOverrideAddress();
		assertNotNull("Server ID override address must be parsed", overrideAddr);
		assertEquals("192.168.100.1", overrideAddr.getHostAddress());
	}

	@Test
	public void testMultipleSuboptionsCombined() throws Exception {
		byte[] sub1 = buildSuboption(1, "eth0/1".getBytes(StandardCharsets.UTF_8));
		byte[] sub2 = buildSuboption(2, "dslam-alpha".getBytes(StandardCharsets.UTF_8));
		byte[] sub5 = buildIpSuboption(5, "192.168.20.1");
		byte[] sub11 = buildIpSuboption(11, "192.168.100.1");

		byte[] combinedPayload = concat(sub1, sub2, sub5, sub11);
		DhcpV4RelayAgentInfoOption opt82 = createOption82(combinedPayload);

		assertEquals("eth0/1", new String(opt82.getSuboptionData(1), StandardCharsets.UTF_8));
		assertEquals("dslam-alpha", new String(opt82.getSuboptionData(2), StandardCharsets.UTF_8));
		assertEquals("192.168.20.1", opt82.getLinkSelectionAddress().getHostAddress());
		assertEquals("192.168.100.1", opt82.getServerIdOverrideAddress().getHostAddress());
	}

	@Test
	public void testMalformedOption82Payloads() throws Exception {
		// Empty payload
		DhcpV4RelayAgentInfoOption emptyOpt = createOption82(new byte[0]);
		assertNull(emptyOpt.getSuboptionData(1));
		assertNull(emptyOpt.getLinkSelectionAddress());

		// Null payload
		DhcpV4RelayAgentInfoOption nullOpt = new DhcpV4RelayAgentInfoOption();
		assertNull(nullOpt.getSuboptionData(1));
		assertNull(nullOpt.getLinkSelectionAddress());

		// Truncated payload: suboption header says len 10, but only 2 bytes present
		byte[] truncated = new byte[] { 1, 10, 0x01, 0x02 };
		DhcpV4RelayAgentInfoOption truncOpt = createOption82(truncated);
		assertNull("Truncated suboption must return null gracefully", truncOpt.getSuboptionData(1));

		// Suboption IP with incorrect length: 3 bytes instead of 4
		byte[] badIp = new byte[] { 5, 3, 10, 0, 1 };
		DhcpV4RelayAgentInfoOption badIpOpt = createOption82(badIp);
		assertNull("Malformed IP suboption must return null", badIpOpt.getLinkSelectionAddress());

		// Unknown suboption code followed by valid suboption
		byte[] unknownFollowedByValid = concat(
				new byte[] { 99, 4, 1, 2, 3, 4 }, // unknown subopt 99
				buildIpSuboption(5, "192.168.20.1") // valid subopt 5
		);
		DhcpV4RelayAgentInfoOption mixedOpt = createOption82(unknownFollowedByValid);
		assertNull("Unknown suboption 1 should not match", mixedOpt.getSuboptionData(1));
		assertEquals("192.168.20.1", mixedOpt.getLinkSelectionAddress().getHostAddress());
	}

	// =========================================================================
	// 2. Routing Precedence Tests
	// =========================================================================

	@Test
	public void testGiaddrRouting() throws Exception {
		DhcpV4Message discover = DhcpV4MessageBuilder.discover()
				.withMac("00:11:22:33:44:55")
				.withLocalAddress(LOCAL_IP, 67)
				.withGiAddr(RELAY_GIADDR)
				.withOption(createOption82(buildSuboption(1, "port-1".getBytes(StandardCharsets.UTF_8))))
				.build();

		DhcpV4Message offer = DhcpV4MessageHandler.handleMessage(InetAddress.getByName(LOCAL_IP), discover);
		assertNotNull("Server must respond with OFFER", offer);
		DhcpAssert.assertThat(offer).isOffer();

		// Address must belong to RelayGiaddrLink (192.168.10.100 - 192.168.10.199)
		String offeredIp = offer.getYiAddr().getHostAddress();
		assertTrue("Offered IP " + offeredIp + " must be in 192.168.10.0/24", offeredIp.startsWith("192.168.10."));
		assertEquals("giaddr must be preserved in response", RELAY_GIADDR, offer.getGiAddr().getHostAddress());

		// Verify routers option scoped to RelayGiaddrLink
		DhcpV4RoutersOption routersOpt = (DhcpV4RoutersOption) offer.getDhcpOption(DhcpConstants.V4OPTION_ROUTERS);
		assertNotNull("Routers option must be present", routersOpt);
		assertEquals("Router must match relay subnet gateway", "192.168.10.1", routersOpt.getIpAddressList().get(0));
	}

	@Test
	public void testLinkSelectionSuboptionOverridesGiaddr() throws Exception {
		// giaddr is 192.168.10.1, but Subopt 5 link selection is 192.168.20.1
		byte[] payload = concat(
				buildSuboption(1, "port-2".getBytes(StandardCharsets.UTF_8)),
				buildIpSuboption(DhcpConstants.V4OPTION_RELAY_INFO_LINK_SELECTION, "192.168.20.1"));

		DhcpV4Message discover = DhcpV4MessageBuilder.discover()
				.withMac("00:22:33:44:55:66")
				.withLocalAddress(LOCAL_IP, 67)
				.withGiAddr(RELAY_GIADDR)
				.withOption(createOption82(payload))
				.build();

		DhcpV4Message offer = DhcpV4MessageHandler.handleMessage(InetAddress.getByName(LOCAL_IP), discover);
		assertNotNull("Server must respond with OFFER", offer);

		// Link selection override takes precedence: allocated from 192.168.20.0/24
		String offeredIp = offer.getYiAddr().getHostAddress();
		assertTrue("Offered IP " + offeredIp + " must be in 192.168.20.0/24 (LinkSelectionLink)",
				offeredIp.startsWith("192.168.20."));

		// giaddr remains unchanged for router packet forwarding
		assertEquals(RELAY_GIADDR, offer.getGiAddr().getHostAddress());
	}

	@Test
	public void testOption118SubnetSelectionOverridesSuboption5AndGiaddr() throws Exception {
		// Option 118 (192.168.30.1) + Option 82 Subopt 5 (192.168.20.1) + giaddr
		// (192.168.10.1)
		byte[] payload = buildIpSuboption(DhcpConstants.V4OPTION_RELAY_INFO_LINK_SELECTION, "192.168.20.1");

		DhcpV4SubnetSelectionOption opt118 = new DhcpV4SubnetSelectionOption();
		opt118.setIpAddress("192.168.30.1");

		DhcpV4Message discover = DhcpV4MessageBuilder.discover()
				.withMac("00:33:44:55:66:77")
				.withLocalAddress(LOCAL_IP, 67)
				.withGiAddr(RELAY_GIADDR)
				.withOption(createOption82(payload))
				.withOption(opt118)
				.build();

		DhcpV4Message offer = DhcpV4MessageHandler.handleMessage(InetAddress.getByName(LOCAL_IP), discover);
		assertNotNull("Server must respond with OFFER", offer);

		// RFC 3527 & RFC 3011: Option 118 takes precedence over Option 82 Subopt 5
		String offeredIp = offer.getYiAddr().getHostAddress();
		assertTrue("Offered IP " + offeredIp + " must be in 192.168.30.0/24 (Option 118 link)",
				offeredIp.startsWith("192.168.30."));
	}

	@Test
	public void testLinkSelectionIgnoredWhenPolicyDisabled() throws Exception {
		DhcpServerPolicies.setProperty(Property.V4_SUBNET_SELECTION, "false");

		byte[] payload = buildIpSuboption(DhcpConstants.V4OPTION_RELAY_INFO_LINK_SELECTION, "192.168.20.1");

		DhcpV4Message discover = DhcpV4MessageBuilder.discover()
				.withMac("00:44:55:66:77:88")
				.withLocalAddress(LOCAL_IP, 67)
				.withGiAddr(RELAY_GIADDR)
				.withOption(createOption82(payload))
				.build();

		DhcpV4Message offer = DhcpV4MessageHandler.handleMessage(InetAddress.getByName(LOCAL_IP), discover);
		assertNotNull("Server must respond with OFFER", offer);

		// With policy disabled, Subopt 5 is ignored; giaddr (192.168.10.1) is used
		String offeredIp = offer.getYiAddr().getHostAddress();
		assertTrue("Offered IP " + offeredIp + " must be in 192.168.10.0/24 (giaddr link)",
				offeredIp.startsWith("192.168.10."));
	}

	// =========================================================================
	// 3. Option 82 Echo-back & Hop Count Tests
	// =========================================================================

	@Test
	public void testOption82EchoBackInOfferAndAck() throws Exception {
		byte[] sub1 = buildSuboption(1, "vlan100-ge0".getBytes(StandardCharsets.UTF_8));
		byte[] sub2 = buildSuboption(2, "edge-switch-01".getBytes(StandardCharsets.UTF_8));
		byte[] payload = concat(sub1, sub2);

		DhcpV4Message discover = DhcpV4MessageBuilder.discover()
				.withMac("00:55:66:77:88:99")
				.withLocalAddress(LOCAL_IP, 67)
				.withGiAddr(RELAY_GIADDR)
				.withOption(createOption82(payload))
				.build();

		DhcpV4Message offer = DhcpV4MessageHandler.handleMessage(InetAddress.getByName(LOCAL_IP), discover);
		assertNotNull("OFFER must be produced", offer);

		// RFC 3046: Relay agent info option MUST be echoed back in OFFER
		DhcpOption echoedOpt82 = offer.getDhcpOption(DhcpConstants.V4OPTION_RELAY_INFO);
		assertNotNull("Option 82 must be echoed back in OFFER", echoedOpt82);
		assertTrue(echoedOpt82 instanceof DhcpV4RelayAgentInfoOption);
		assertArrayEquals("Echoed Option 82 payload must match original",
				payload, ((DhcpV4RelayAgentInfoOption) echoedOpt82).getRelayData());

		// Complete DORA: client requests the offered IP
		DhcpV4Message request = DhcpV4MessageBuilder.request()
				.withMac("00:55:66:77:88:99")
				.withLocalAddress(LOCAL_IP, 67)
				.withGiAddr(RELAY_GIADDR)
				.withRequestedIp(offer.getYiAddr().getHostAddress())
				.withServerId(LOCAL_IP)
				.withOption(createOption82(payload))
				.build();

		DhcpV4Message ack = DhcpV4MessageHandler.handleMessage(InetAddress.getByName(LOCAL_IP), request);
		assertNotNull("ACK must be produced", ack);
		DhcpAssert.assertThat(ack).isAck();

		// Option 82 MUST be echoed back in ACK
		DhcpOption ackEchoedOpt82 = ack.getDhcpOption(DhcpConstants.V4OPTION_RELAY_INFO);
		assertNotNull("Option 82 must be echoed back in ACK", ackEchoedOpt82);
		assertArrayEquals("Echoed Option 82 payload in ACK must match",
				payload, ((DhcpV4RelayAgentInfoOption) ackEchoedOpt82).getRelayData());
	}

	@Test
	public void testHopCountPreservationAndProcessing() throws Exception {
		// Relay agents increment hops field (e.g. 2 router hops)
		DhcpV4Message discover = DhcpV4MessageBuilder.discover()
				.withMac("00:66:77:88:99:aa")
				.withLocalAddress(LOCAL_IP, 67)
				.withGiAddr(RELAY_GIADDR)
				.withHops((short) 2)
				.withOption(createOption82(buildSuboption(1, "hop2".getBytes(StandardCharsets.UTF_8))))
				.build();

		assertEquals("Discover hop count should be 2", 2, discover.getHops());

		DhcpV4Message offer = DhcpV4MessageHandler.handleMessage(InetAddress.getByName(LOCAL_IP), discover);
		assertNotNull("Server must process relayed packet with multiple hops", offer);
		DhcpAssert.assertThat(offer).isOffer();
	}

	// =========================================================================
	// 4. Server ID Override (RFC 5107 Suboption 11) Tests
	// =========================================================================

	@Test
	public void testServerIdOverrideEnabled() throws Exception {
		DhcpServerPolicies.setProperty(Property.V4_SERVER_ID_OVERRIDE, "true");

		byte[] payload = concat(
				buildSuboption(1, "circuit-a".getBytes(StandardCharsets.UTF_8)),
				buildIpSuboption(DhcpConstants.V4OPTION_RELAY_INFO_SERVER_ID_OVERRIDE, "192.168.100.1"));

		DhcpV4Message discover = DhcpV4MessageBuilder.discover()
				.withMac("00:77:88:99:aa:bb")
				.withLocalAddress(LOCAL_IP, 67)
				.withGiAddr(RELAY_GIADDR)
				.withOption(createOption82(payload))
				.build();

		DhcpV4Message offer = DhcpV4MessageHandler.handleMessage(InetAddress.getByName(LOCAL_IP), discover);
		assertNotNull(offer);

		// Server Identifier Option in OFFER must reflect Suboption 11
		DhcpV4ServerIdOption serverIdOpt = (DhcpV4ServerIdOption) offer.getDhcpOption(DhcpConstants.V4OPTION_SERVERID);
		assertNotNull("Server ID option must be present", serverIdOpt);
		assertEquals("Server ID must be overridden to Suboption 11 IP",
				"192.168.100.1", serverIdOpt.getIpAddress());

		// In REQUEST, client directs request to overridden Server ID
		DhcpV4Message request = DhcpV4MessageBuilder.request()
				.withMac("00:77:88:99:aa:bb")
				.withLocalAddress(LOCAL_IP, 67)
				.withGiAddr(RELAY_GIADDR)
				.withRequestedIp(offer.getYiAddr().getHostAddress())
				.withServerId("192.168.100.1")
				.withOption(createOption82(payload))
				.build();

		DhcpV4Message ack = DhcpV4MessageHandler.handleMessage(InetAddress.getByName(LOCAL_IP), request);
		assertNotNull("ACK must be produced", ack);

		DhcpV4ServerIdOption ackServerIdOpt = (DhcpV4ServerIdOption) ack.getDhcpOption(DhcpConstants.V4OPTION_SERVERID);
		assertNotNull(ackServerIdOpt);
		assertEquals("ACK Server ID must also reflect Suboption 11 IP",
				"192.168.100.1", ackServerIdOpt.getIpAddress());
	}

	@Test
	public void testServerIdOverrideDisabled() throws Exception {
		DhcpServerPolicies.setProperty(Property.V4_SERVER_ID_OVERRIDE, "false");

		byte[] payload = buildIpSuboption(DhcpConstants.V4OPTION_RELAY_INFO_SERVER_ID_OVERRIDE, "192.168.100.1");

		DhcpV4Message discover = DhcpV4MessageBuilder.discover()
				.withMac("00:88:99:aa:bb:cc")
				.withLocalAddress(LOCAL_IP, 67)
				.withGiAddr(RELAY_GIADDR)
				.withOption(createOption82(payload))
				.build();

		DhcpV4Message offer = DhcpV4MessageHandler.handleMessage(InetAddress.getByName(LOCAL_IP), discover);
		assertNotNull(offer);

		// With policy disabled, standard server ID must be used
		DhcpV4ServerIdOption serverIdOpt = (DhcpV4ServerIdOption) offer.getDhcpOption(DhcpConstants.V4OPTION_SERVERID);
		assertNotNull(serverIdOpt);
		assertEquals("Server ID must NOT be overridden when policy is false",
				LOCAL_IP, serverIdOpt.getIpAddress());
	}

	// =========================================================================
	// 5. Option 82 Filter Matching Tests
	// =========================================================================

	@Test
	public void testOption82FilterMatchingDirectsPoolAndPolicy() throws Exception {
		// Filter CircuitVipFilter matches hex "0104beefcafe" (Subopt 1 with 4 bytes:
		// 0xbe, 0xef, 0xca, 0xfe)
		byte[] vipPayload = new byte[] { 0x01, 0x04, (byte) 0xbe, (byte) 0xef, (byte) 0xca, (byte) 0xfe };

		DhcpV4Message vipDiscover = DhcpV4MessageBuilder.discover()
				.withMac("00:aa:bb:cc:dd:01")
				.withLocalAddress(LOCAL_IP, 67)
				.withGiAddr(RELAY_GIADDR)
				.withOption(createOption82(vipPayload))
				.build();

		DhcpV4Message vipOffer = DhcpV4MessageHandler.handleMessage(InetAddress.getByName(LOCAL_IP), vipDiscover);
		assertNotNull("VIP client must receive OFFER", vipOffer);

		String vipIp = vipOffer.getYiAddr().getHostAddress();
		int vipHostPart = Integer.parseInt(vipIp.substring(vipIp.lastIndexOf('.') + 1));
		assertTrue("VIP IP " + vipIp + " must be allocated from CircuitVipFilter pool (200-250)",
				vipHostPart >= 200 && vipHostPart <= 250);

		DhcpV4LeaseTimeOption vipLeaseOpt = (DhcpV4LeaseTimeOption) vipOffer
				.getDhcpOption(DhcpConstants.V4OPTION_LEASE_TIME);
		assertNotNull(vipLeaseOpt);
		assertEquals("VIP client must receive filter-scoped lease time (1800)", 1800L, vipLeaseOpt.getUnsignedInt());

		// Non-matching client gets default pool (100-199) and default lease time
		// (86400)
		byte[] regularPayload = new byte[] { 0x01, 0x04, 0x11, 0x22, 0x33, 0x44 };

		DhcpV4Message regDiscover = DhcpV4MessageBuilder.discover()
				.withMac("00:aa:bb:cc:dd:02")
				.withLocalAddress(LOCAL_IP, 67)
				.withGiAddr(RELAY_GIADDR)
				.withOption(createOption82(regularPayload))
				.build();

		DhcpV4Message regOffer = DhcpV4MessageHandler.handleMessage(InetAddress.getByName(LOCAL_IP), regDiscover);
		assertNotNull("Regular client must receive OFFER", regOffer);

		String regIp = regOffer.getYiAddr().getHostAddress();
		int regHostPart = Integer.parseInt(regIp.substring(regIp.lastIndexOf('.') + 1));
		assertTrue("Regular IP " + regIp + " must be allocated from default pool (100-199)",
				regHostPart >= 100 && regHostPart <= 199);

		DhcpV4LeaseTimeOption regLeaseOpt = (DhcpV4LeaseTimeOption) regOffer
				.getDhcpOption(DhcpConstants.V4OPTION_LEASE_TIME);
		assertNotNull(regLeaseOpt);
		assertEquals("Regular client must receive default link lease time (86400)", 86400L,
				regLeaseOpt.getUnsignedInt());
	}

	// =========================================================================
	// 6. End-to-End DORA Lifecycle via Relay Agent
	// =========================================================================

	@Test
	public void testEndToEndDoraViaRelayWithRenewalAndRelease() throws Exception {
		byte[] opt82Data = concat(
				buildSuboption(1, "corp-access-sw1-p10".getBytes(StandardCharsets.UTF_8)),
				buildSuboption(2, "bldg-b-floor-3".getBytes(StandardCharsets.UTF_8)));

		String clientMac = "00:bb:cc:dd:ee:ff";

		// 1. DISCOVER
		DhcpV4Message discover = DhcpV4MessageBuilder.discover()
				.withMac(clientMac)
				.withLocalAddress(LOCAL_IP, 67)
				.withGiAddr(RELAY_GIADDR)
				.withOption(createOption82(opt82Data))
				.build();

		DhcpV4Message offer = DhcpV4MessageHandler.handleMessage(InetAddress.getByName(LOCAL_IP), discover);
		assertNotNull(offer);
		DhcpAssert.assertThat(offer).isOffer();
		String leasedIp = offer.getYiAddr().getHostAddress();
		assertTrue(leasedIp.startsWith("192.168.10."));

		// 2. REQUEST
		DhcpV4Message request = DhcpV4MessageBuilder.request()
				.withMac(clientMac)
				.withLocalAddress(LOCAL_IP, 67)
				.withGiAddr(RELAY_GIADDR)
				.withRequestedIp(leasedIp)
				.withServerId(LOCAL_IP)
				.withOption(createOption82(opt82Data))
				.build();

		DhcpV4Message ack = DhcpV4MessageHandler.handleMessage(InetAddress.getByName(LOCAL_IP), request);
		assertNotNull(ack);
		DhcpAssert.assertThat(ack).isAck();
		assertEquals(leasedIp, ack.getYiAddr().getHostAddress());

		// Verify binding recorded in InMemoryIaManager
		IdentityAssoc ia = iaMgr.findIA(InetAddress.getByName(leasedIp));
		assertNotNull("IA binding must be recorded in memory", ia);
		IaAddress iaAddr = ia.getIaAddresses().iterator().next();
		assertEquals("Leased IP must match IA record", leasedIp, iaAddr.getIpAddress().getHostAddress());
		assertEquals("Binding must be in LEASED state", IaAddress.LEASED, iaAddr.getState());

		// 3. RENEW via relay
		DhcpV4Message renewRequest = DhcpV4MessageBuilder.request()
				.withMac(clientMac)
				.withLocalAddress(LOCAL_IP, 67)
				.withGiAddr(RELAY_GIADDR)
				.withCiAddr(leasedIp)
				.withOption(createOption82(opt82Data))
				.build();

		DhcpV4Message renewAck = DhcpV4MessageHandler.handleMessage(InetAddress.getByName(LOCAL_IP), renewRequest);
		assertNotNull(renewAck);
		DhcpAssert.assertThat(renewAck).isAck();
		assertEquals(leasedIp, renewAck.getYiAddr().getHostAddress());

		// 4. RELEASE via relay
		DhcpV4Message release = DhcpV4MessageBuilder.release()
				.withMac(clientMac)
				.withLocalAddress(LOCAL_IP, 67)
				.withGiAddr(RELAY_GIADDR)
				.withCiAddr(leasedIp)
				.withServerId(LOCAL_IP)
				.withOption(createOption82(opt82Data))
				.build();

		DhcpV4MessageHandler.handleMessage(InetAddress.getByName(LOCAL_IP), release);

		// Verify binding state released / available
		IdentityAssoc releasedIa = iaMgr.findIA(InetAddress.getByName(leasedIp));
		if (releasedIa != null && !releasedIa.getIaAddresses().isEmpty()) {
			IaAddress addr = releasedIa.getIaAddresses().iterator().next();
			assertEquals("Binding must be in AVAILABLE state after release", IaAddress.AVAILABLE, addr.getState());
		}
	}
}
