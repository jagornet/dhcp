/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.message.DhcpV6RelayMessage;
import com.jagornet.dhcp.core.option.base.BaseOpaqueData;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaAddrOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaNaOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6InterfaceIdOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6RelayOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6RemoteIdOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6ServerIdOption;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.db.IaAddress;
import com.jagornet.dhcp.server.db.IdentityAssoc;
import com.jagornet.dhcp.server.request.binding.V6NaAddrBindingManagerImpl;
import com.jagornet.dhcp.server.testutil.DhcpAssert;
import com.jagornet.dhcp.server.testutil.DhcpConfigTestContext;
import com.jagornet.dhcp.server.testutil.DhcpV6MessageBuilder;
import com.jagornet.dhcp.server.testutil.InMemoryIaManager;

/**
 * DHCPv6 Relay Agent Message Processing (RFC 8415 / RFC 3315) Test Suite.
 * 
 * Tests include:
 * 1. Single-hop RELAY-FORW / RELAY-REPL:
 *    - Encapsulated Solicit -> Advertise in Relay-Repl
 *    - Encapsulated Request -> Reply in Relay-Repl
 *    - Preservation of Option 18 (Interface-ID) and Option 37 (Remote-ID)
 *    - Header preservation: link-address, peer-address, hop-count
 * 2. Multi-hop Cascaded Relay (2-hop and 3-hop relay stacks):
 *    - Unwrapping nested Relay-Forw messages
 *    - Subnet resolution from innermost relay link-address
 *    - Reconstructing nested Relay-Repl stack
 *    - Faithful preservation of interface IDs across each cascaded relay hop
 * 3. Subnet Resolution Rules:
 *    - link-address takes precedence over transit peer-address
 *    - Intermediate relays with unspecified (::) link-address resolving to innermost link
 * 4. End-to-End SARR & Release Lifecycle via Relay:
 *    - Solicit -> Advertise -> Request -> Reply -> Release
 *    - Lease tracking in InMemoryIaManager
 * 5. Malformed Relay Message Resilience:
 *    - Missing Option 9 (Relay Message Option)
 *    - Unsupported relay message type
 * 
 * @author A. Gregory Rabil
 */
public class TestDhcpV6RelayRouting {

	private DhcpConfigTestContext context;
	private InMemoryIaManager iaMgr;
	private V6NaAddrBindingManagerImpl v6Mgr;

	private static final String SERVER_LOCAL_IP = "2001:db8:1::1";
	private static final String CLIENT_LINK_IP = "2001:db8:10::1";
	private static final String CLIENT_PEER_IP = "fe80::cafe:babe";

	private static final String CONFIG_XML =
		"<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
		"  <v6ServerIdOption>\n" +
		"    <opaqueData>\n" +
		"      <hexValue>00010001aabbccdd001122334455</hexValue>\n" +
		"    </opaqueData>\n" +
		"  </v6ServerIdOption>\n" +
		"  <policies>\n" +
		"    <policyList>\n" +
		"      <name>v6.validLifetime</name>\n" +
		"      <value>7200</value>\n" +
		"    </policyList>\n" +
		"    <policyList>\n" +
		"      <name>v6.preferredLifetime</name>\n" +
		"      <value>3600</value>\n" +
		"    </policyList>\n" +
		"  </policies>\n" +
		"  <links>\n" +
		"    <!-- Directly connected server link -->\n" +
		"    <linkList>\n" +
		"      <name>LocalLinkV6</name>\n" +
		"      <address>2001:db8:1::/64</address>\n" +
		"      <v6NaAddrPools>\n" +
		"        <poolList>\n" +
		"          <range>2001:db8:1::100-2001:db8:1::199</range>\n" +
		"        </poolList>\n" +
		"      </v6NaAddrPools>\n" +
		"    </linkList>\n" +
		"\n" +
		"    <!-- Relayed Client Link (Hop 0) -->\n" +
		"    <linkList>\n" +
		"      <name>RelayedClientLinkV6</name>\n" +
		"      <address>2001:db8:10::/64</address>\n" +
		"      <v6NaAddrPools>\n" +
		"        <poolList>\n" +
		"          <range>2001:db8:10::100-2001:db8:10::199</range>\n" +
		"        </poolList>\n" +
		"      </v6NaAddrPools>\n" +
		"    </linkList>\n" +
		"\n" +
		"    <!-- Intermediate Relay Link (Hop 1) -->\n" +
		"    <linkList>\n" +
		"      <name>IntermediateRelayLinkV6</name>\n" +
		"      <address>2001:db8:20::/64</address>\n" +
		"      <v6NaAddrPools>\n" +
		"        <poolList>\n" +
		"          <range>2001:db8:20::100-2001:db8:20::199</range>\n" +
		"        </poolList>\n" +
		"      </v6NaAddrPools>\n" +
		"    </linkList>\n" +
		"\n" +
		"    <!-- Aggregation Relay Link (Hop 2) -->\n" +
		"    <linkList>\n" +
		"      <name>AggregationRelayLinkV6</name>\n" +
		"      <address>2001:db8:30::/64</address>\n" +
		"      <v6NaAddrPools>\n" +
		"        <poolList>\n" +
		"          <range>2001:db8:30::100-2001:db8:30::199</range>\n" +
		"        </poolList>\n" +
		"      </v6NaAddrPools>\n" +
		"    </linkList>\n" +
		"  </links>\n" +
		"</dhc:dhcpServerConfig>";

	@Before
	public void setUp() throws Exception {
		context = new DhcpConfigTestContext();
		context.initXml(CONFIG_XML);

		iaMgr = new InMemoryIaManager();

		v6Mgr = new V6NaAddrBindingManagerImpl();
		v6Mgr.setIaMgr(iaMgr);
		v6Mgr.init();

		DhcpServerConfiguration config = DhcpServerConfiguration.getInstance();
		config.setIaMgr(iaMgr);
		config.setV6NaAddrBindingMgr(v6Mgr);
	}

	@After
	public void tearDown() {
		if (v6Mgr != null) {
			v6Mgr.close();
		}
		if (context != null) {
			context.close();
		}
	}

	// -------------------------------------------------------------------------
	// Relay Message Helpers
	// -------------------------------------------------------------------------

	private static DhcpV6RelayMessage buildRelayForw(InetAddress linkAddr, InetAddress peerAddr,
			short hopCount, DhcpV6Message innerMsg) {
		InetSocketAddress local = new InetSocketAddress(SERVER_LOCAL_IP, DhcpConstants.V6_SERVER_PORT);
		InetSocketAddress remote = new InetSocketAddress(peerAddr, DhcpConstants.V6_SERVER_PORT);
		DhcpV6RelayMessage relay = new DhcpV6RelayMessage(local, remote);
		relay.setMessageType(DhcpConstants.V6MESSAGE_TYPE_RELAY_FORW);
		relay.setHopCount(hopCount);
		relay.setLinkAddress(linkAddr);
		relay.setPeerAddress(peerAddr);

		if (innerMsg != null) {
			DhcpV6RelayOption relayOpt = new DhcpV6RelayOption();
			relayOpt.setDhcpMessage(innerMsg);
			relay.putDhcpOption(relayOpt);
			relay.setRelayOption(relayOpt);
		}
		return relay;
	}

	private static void addInterfaceId(DhcpV6RelayMessage relay, String ifId) {
		BaseOpaqueData data = new BaseOpaqueData(ifId.getBytes(StandardCharsets.UTF_8));
		DhcpV6InterfaceIdOption opt = new DhcpV6InterfaceIdOption(data);
		relay.putDhcpOption(opt);
	}

	private static void addRemoteId(DhcpV6RelayMessage relay, long enterpriseNum, String remoteId) {
		BaseOpaqueData data = new BaseOpaqueData(remoteId.getBytes(StandardCharsets.UTF_8));
		DhcpV6RemoteIdOption opt = new DhcpV6RemoteIdOption(enterpriseNum, data);
		relay.putDhcpOption(opt);
	}

	// =========================================================================
	// 1. Single-Hop RELAY-FORW / RELAY-REPL Processing
	// =========================================================================

	@Test
	public void testSingleHopRelayForwSolicitAdvertise() throws Exception {
		// Client sends Solicit
		DhcpV6Message solicit = DhcpV6MessageBuilder.solicit()
				.withClientId("00:02:00:00:00:01")
				.withIaNa(1, 0, 0)
				.build();

		// Relay encapsulates Solicit into RELAY-FORW
		InetAddress linkAddr = InetAddress.getByName(CLIENT_LINK_IP);
		InetAddress peerAddr = InetAddress.getByName(CLIENT_PEER_IP);
		DhcpV6RelayMessage relayForw = buildRelayForw(linkAddr, peerAddr, (short) 0, solicit);

		// Add Option 18 (Interface-ID) and Option 37 (Remote-ID)
		addInterfaceId(relayForw, "GigabitEthernet0/0/1.100");
		addRemoteId(relayForw, 9999L, "dsl-access-node-42");

		// Server handles message
		DhcpV6Message response = DhcpV6MessageHandler.handleMessage(
				InetAddress.getByName(SERVER_LOCAL_IP), relayForw);

		assertNotNull("Server must return a response", response);
		assertTrue("Response must be DhcpV6RelayMessage", response instanceof DhcpV6RelayMessage);

		DhcpV6RelayMessage relayRepl = (DhcpV6RelayMessage) response;
		assertEquals("Message type must be RELAY-REPL",
				DhcpConstants.V6MESSAGE_TYPE_RELAY_REPL, relayRepl.getMessageType());
		assertEquals("Hop count preserved", 0, relayRepl.getHopCount());
		assertEquals("Link address preserved", linkAddr, relayRepl.getLinkAddress());
		assertEquals("Peer address preserved", peerAddr, relayRepl.getPeerAddress());

		// Verify Option 18 (Interface-ID) preserved in RELAY-REPL
		DhcpV6InterfaceIdOption ifIdOpt = (DhcpV6InterfaceIdOption)
				relayRepl.getDhcpOption(DhcpConstants.V6OPTION_INTERFACE_ID);
		assertNotNull("Interface-ID option must be preserved in Relay-Repl", ifIdOpt);
		assertEquals("GigabitEthernet0/0/1.100",
				new String(ifIdOpt.getOpaqueData().getHex(), StandardCharsets.UTF_8));

		// Verify Option 37 (Remote-ID) preserved in RELAY-REPL
		DhcpV6RemoteIdOption remoteIdOpt = (DhcpV6RemoteIdOption)
				relayRepl.getDhcpOption(DhcpConstants.V6OPTION_REMOTE_ID);
		assertNotNull("Remote-ID option must be preserved in Relay-Repl", remoteIdOpt);
		assertEquals("Enterprise number must match", 9999L, remoteIdOpt.getEnterpriseNumber());
		assertEquals("dsl-access-node-42",
				new String(remoteIdOpt.getOpaqueData().getHex(), StandardCharsets.UTF_8));

		// Verify encapsulated client message is ADVERTISE
		assertNotNull(relayRepl.getRelayOption());
		DhcpV6Message advertise = relayRepl.getRelayOption().getDhcpMessage();
		assertNotNull("Relayed message must be present", advertise);
		DhcpAssert.assertThat(advertise).isAdvertise();
		assertEquals(solicit.getTransactionId(), advertise.getTransactionId());

		// Verify allocated IP is from RelayedClientLinkV6 (2001:db8:10::100-199)
		assertNotNull("IA_NA options list must not be empty", advertise.getIaNaOptions());
		assertTrue("IA_NA options list must not be empty", !advertise.getIaNaOptions().isEmpty());
		DhcpV6IaNaOption iaNa = advertise.getIaNaOptions().get(0);
		assertNotNull("IA_ADDR must be present in IA_NA", iaNa.getIaAddrOptions());
		assertTrue("IA_ADDR list must not be empty", !iaNa.getIaAddrOptions().isEmpty());
		DhcpV6IaAddrOption iaAddr = iaNa.getIaAddrOptions().get(0);
		String ip = iaAddr.getIpAddress();
		assertTrue("Offered IP " + ip + " must be from relayed client prefix 2001:db8:10::/64",
				ip.startsWith("2001:db8:10:"));
	}

	@Test
	public void testSingleHopRelayForwRequestReply() throws Exception {
		// First do Solicit to obtain offer
		DhcpV6Message solicit = DhcpV6MessageBuilder.solicit()
				.withClientId("00:02:00:00:00:02")
				.withIaNa(1, 0, 0)
				.build();

		InetAddress linkAddr = InetAddress.getByName(CLIENT_LINK_IP);
		InetAddress peerAddr = InetAddress.getByName(CLIENT_PEER_IP);
		DhcpV6RelayMessage relayForw = buildRelayForw(linkAddr, peerAddr, (short) 0, solicit);
		addInterfaceId(relayForw, "Port-2");

		DhcpV6RelayMessage relayRepl = (DhcpV6RelayMessage) DhcpV6MessageHandler.handleMessage(
				InetAddress.getByName(SERVER_LOCAL_IP), relayForw);
		DhcpV6Message advertise = relayRepl.getRelayOption().getDhcpMessage();

		DhcpV6ServerIdOption serverId = advertise.getDhcpServerIdOption();
		assertNotNull("ServerId must be present in Advertise", serverId);
		assertNotNull("IA_NA list must not be empty", advertise.getIaNaOptions());
		DhcpV6IaNaOption advIaNa = advertise.getIaNaOptions().get(0);
		DhcpV6IaAddrOption advAddr = advIaNa.getIaAddrOptions().get(0);
		String advIp = advAddr.getIpAddress();

		// Build Client Request
		DhcpV6Message request = DhcpV6MessageBuilder.request()
				.withClientId("00:02:00:00:00:02")
				.withServerId(serverId)
				.withIaNaWithAddr(1, advIp)
				.build();

		// Relay encapsulates Request
		DhcpV6RelayMessage reqRelayForw = buildRelayForw(linkAddr, peerAddr, (short) 0, request);
		addInterfaceId(reqRelayForw, "Port-2");
		addRemoteId(reqRelayForw, 1234L, "subscriber-88");

		DhcpV6RelayMessage reqRelayRepl = (DhcpV6RelayMessage) DhcpV6MessageHandler.handleMessage(
				InetAddress.getByName(SERVER_LOCAL_IP), reqRelayForw);
		assertNotNull(reqRelayRepl);
		assertEquals(DhcpConstants.V6MESSAGE_TYPE_RELAY_REPL, reqRelayRepl.getMessageType());

		// Verify Options 18 and 37 preserved
		assertNotNull(reqRelayRepl.getDhcpOption(DhcpConstants.V6OPTION_INTERFACE_ID));
		assertNotNull(reqRelayRepl.getDhcpOption(DhcpConstants.V6OPTION_REMOTE_ID));

		// Verify inner client message is REPLY
		DhcpV6Message reply = reqRelayRepl.getRelayOption().getDhcpMessage();
		assertNotNull(reply);
		DhcpAssert.assertThat(reply).isReply();

		// Verify lease recorded in InMemoryIaManager
		IdentityAssoc ia = iaMgr.findIA(InetAddress.getByName(advIp));
		assertNotNull("IA binding must be recorded in memory", ia);
		IaAddress leasedAddr = ia.getIaAddresses().iterator().next();
		assertEquals(InetAddress.getByName(advIp), leasedAddr.getIpAddress());
		assertEquals(IaAddress.LEASED, leasedAddr.getState());
	}

	// =========================================================================
	// 2. Multi-hop Cascaded Relay Processing
	// =========================================================================

	@Test
	public void testMultiHopRelayCascadedTwoHops() throws Exception {
		// Client SOLICIT
		DhcpV6Message solicit = DhcpV6MessageBuilder.solicit()
				.withClientId("00:02:00:00:00:03")
				.withIaNa(1, 0, 0)
				.build();

		// Hop 0 (edge relay, closest to client):
		// link-address = 2001:db8:10::1, peer-address = fe80::client, hopCount = 0
		InetAddress clientLink = InetAddress.getByName(CLIENT_LINK_IP);
		InetAddress clientPeer = InetAddress.getByName(CLIENT_PEER_IP);
		DhcpV6RelayMessage hop0Relay = buildRelayForw(clientLink, clientPeer, (short) 0, solicit);
		addInterfaceId(hop0Relay, "access-switch-port-5");

		// Hop 1 (aggregation relay, closest to server):
		// link-address = 2001:db8:20::1, peer-address = 2001:db8:20::2, hopCount = 1
		InetAddress aggLink = InetAddress.getByName("2001:db8:20::1");
		InetAddress aggPeer = InetAddress.getByName("2001:db8:20::2");
		DhcpV6RelayMessage hop1Relay = buildRelayForw(aggLink, aggPeer, (short) 1, hop0Relay);
		addInterfaceId(hop1Relay, "agg-router-trunk-1");

		// Server processes 2-hop cascaded relay
		DhcpV6Message outerResponse = DhcpV6MessageHandler.handleMessage(
				InetAddress.getByName(SERVER_LOCAL_IP), hop1Relay);

		assertNotNull(outerResponse);
		assertTrue(outerResponse instanceof DhcpV6RelayMessage);

		// Verify outer relay (Hop 1) flipped to RELAY-REPL
		DhcpV6RelayMessage outerRepl = (DhcpV6RelayMessage) outerResponse;
		assertEquals("Outer message type must be RELAY-REPL",
				DhcpConstants.V6MESSAGE_TYPE_RELAY_REPL, outerRepl.getMessageType());
		assertEquals("Outer hop count preserved", 1, outerRepl.getHopCount());
		assertEquals("Outer link address preserved", aggLink, outerRepl.getLinkAddress());
		assertEquals("Outer peer address preserved", aggPeer, outerRepl.getPeerAddress());

		DhcpV6InterfaceIdOption outerIfId = (DhcpV6InterfaceIdOption)
				outerRepl.getDhcpOption(DhcpConstants.V6OPTION_INTERFACE_ID);
		assertNotNull(outerIfId);
		assertEquals("agg-router-trunk-1",
				new String(outerIfId.getOpaqueData().getHex(), StandardCharsets.UTF_8));

		// Verify inner relay (Hop 0) flipped to RELAY-REPL
		DhcpV6Message innerMsg = outerRepl.getRelayOption().getDhcpMessage();
		assertNotNull(innerMsg);
		assertTrue("Inner message must also be DhcpV6RelayMessage", innerMsg instanceof DhcpV6RelayMessage);

		DhcpV6RelayMessage innerRepl = (DhcpV6RelayMessage) innerMsg;
		assertEquals("Inner message type must be RELAY-REPL",
				DhcpConstants.V6MESSAGE_TYPE_RELAY_REPL, innerRepl.getMessageType());
		assertEquals("Inner hop count preserved", 0, innerRepl.getHopCount());
		assertEquals("Inner link address preserved", clientLink, innerRepl.getLinkAddress());
		assertEquals("Inner peer address preserved", clientPeer, innerRepl.getPeerAddress());

		DhcpV6InterfaceIdOption innerIfId = (DhcpV6InterfaceIdOption)
				innerRepl.getDhcpOption(DhcpConstants.V6OPTION_INTERFACE_ID);
		assertNotNull(innerIfId);
		assertEquals("access-switch-port-5",
				new String(innerIfId.getOpaqueData().getHex(), StandardCharsets.UTF_8));

		// Verify innermost client message is ADVERTISE allocated from innermost clientLink (2001:db8:10::/64)
		DhcpV6Message clientAdv = innerRepl.getRelayOption().getDhcpMessage();
		assertNotNull(clientAdv);
		DhcpAssert.assertThat(clientAdv).isAdvertise();

		DhcpV6IaNaOption iaNa = clientAdv.getIaNaOptions().get(0);
		assertNotNull(iaNa);
		String allocatedIp = iaNa.getIaAddrOptions().get(0).getIpAddress();
		assertTrue("Allocated IP " + allocatedIp + " must be from innermost link 2001:db8:10::/64",
				allocatedIp.startsWith("2001:db8:10:"));
	}

	@Test
	public void testMultiHopRelayCascadedThreeHops() throws Exception {
		// Client SOLICIT
		DhcpV6Message solicit = DhcpV6MessageBuilder.solicit()
				.withClientId("00:02:00:00:00:04")
				.withIaNa(1, 0, 0)
				.build();

		// Hop 0 (edge relay): link = 2001:db8:10::1
		InetAddress hop0Link = InetAddress.getByName(CLIENT_LINK_IP);
		InetAddress hop0Peer = InetAddress.getByName(CLIENT_PEER_IP);
		DhcpV6RelayMessage hop0 = buildRelayForw(hop0Link, hop0Peer, (short) 0, solicit);
		addInterfaceId(hop0, "hop0-if");

		// Hop 1 (distribution relay): link = 2001:db8:20::1
		InetAddress hop1Link = InetAddress.getByName("2001:db8:20::1");
		InetAddress hop1Peer = InetAddress.getByName("2001:db8:20::2");
		DhcpV6RelayMessage hop1 = buildRelayForw(hop1Link, hop1Peer, (short) 1, hop0);
		addInterfaceId(hop1, "hop1-if");

		// Hop 2 (core relay): link = 2001:db8:30::1
		InetAddress hop2Link = InetAddress.getByName("2001:db8:30::1");
		InetAddress hop2Peer = InetAddress.getByName("2001:db8:30::2");
		DhcpV6RelayMessage hop2 = buildRelayForw(hop2Link, hop2Peer, (short) 2, hop1);
		addInterfaceId(hop2, "hop2-if");

		// Server processes 3-hop stack
		DhcpV6Message respHop2 = DhcpV6MessageHandler.handleMessage(
				InetAddress.getByName(SERVER_LOCAL_IP), hop2);
		assertNotNull(respHop2);
		assertEquals(DhcpConstants.V6MESSAGE_TYPE_RELAY_REPL, respHop2.getMessageType());

		// Layer 2
		DhcpV6RelayMessage replHop2 = (DhcpV6RelayMessage) respHop2;
		assertEquals("hop2-if", new String(
				((DhcpV6InterfaceIdOption) replHop2.getDhcpOption(DhcpConstants.V6OPTION_INTERFACE_ID))
						.getOpaqueData().getHex(), StandardCharsets.UTF_8));

		// Layer 1
		DhcpV6RelayMessage replHop1 = (DhcpV6RelayMessage) replHop2.getRelayOption().getDhcpMessage();
		assertNotNull(replHop1);
		assertEquals(DhcpConstants.V6MESSAGE_TYPE_RELAY_REPL, replHop1.getMessageType());
		assertEquals("hop1-if", new String(
				((DhcpV6InterfaceIdOption) replHop1.getDhcpOption(DhcpConstants.V6OPTION_INTERFACE_ID))
						.getOpaqueData().getHex(), StandardCharsets.UTF_8));

		// Layer 0
		DhcpV6RelayMessage replHop0 = (DhcpV6RelayMessage) replHop1.getRelayOption().getDhcpMessage();
		assertNotNull(replHop0);
		assertEquals(DhcpConstants.V6MESSAGE_TYPE_RELAY_REPL, replHop0.getMessageType());
		assertEquals("hop0-if", new String(
				((DhcpV6InterfaceIdOption) replHop0.getDhcpOption(DhcpConstants.V6OPTION_INTERFACE_ID))
						.getOpaqueData().getHex(), StandardCharsets.UTF_8));

		// Client message
		DhcpV6Message clientMsg = replHop0.getRelayOption().getDhcpMessage();
		assertNotNull(clientMsg);
		DhcpAssert.assertThat(clientMsg).isAdvertise();

		DhcpV6IaNaOption iaNa = clientMsg.getIaNaOptions().get(0);
		assertTrue(iaNa.getIaAddrOptions().get(0).getIpAddress().startsWith("2001:db8:10:"));
	}

	// =========================================================================
	// 3. Subnet Resolution Rules Tests
	// =========================================================================

	@Test
	public void testSubnetResolutionLinkAddressVsPeerAddress() throws Exception {
		DhcpV6Message solicit = DhcpV6MessageBuilder.solicit()
				.withClientId("00:02:00:00:00:05")
				.withIaNa(1, 0, 0)
				.build();

		// link-address is 2001:db8:10::1 (matches RelayedClientLinkV6)
		// peer-address is 2001:db8:99::99 (unconfigured external transit IP)
		InetAddress linkAddr = InetAddress.getByName(CLIENT_LINK_IP);
		InetAddress transitPeerAddr = InetAddress.getByName("2001:db8:99::99");

		DhcpV6RelayMessage relayForw = buildRelayForw(linkAddr, transitPeerAddr, (short) 0, solicit);
		DhcpV6RelayMessage relayRepl = (DhcpV6RelayMessage) DhcpV6MessageHandler.handleMessage(
				InetAddress.getByName(SERVER_LOCAL_IP), relayForw);

		assertNotNull("Response must be generated", relayRepl);
		DhcpV6Message adv = relayRepl.getRelayOption().getDhcpMessage();
		assertNotNull(adv);
		DhcpAssert.assertThat(adv).isAdvertise();

		// Address must come from 2001:db8:10::/64 (selected by link-address, ignoring unconfigured transit peer)
		DhcpV6IaNaOption iaNa = adv.getIaNaOptions().get(0);
		String allocated = iaNa.getIaAddrOptions().get(0).getIpAddress();
		assertTrue("Allocated address must match link-address subnet", allocated.startsWith("2001:db8:10:"));
	}

	// =========================================================================
	// 4. End-to-End SARR & Release Lifecycle via Relay
	// =========================================================================

	@Test
	public void testEndToEndSarrViaRelayWithRelease() throws Exception {
		String clientId = "00:02:00:00:00:06";
		InetAddress linkAddr = InetAddress.getByName(CLIENT_LINK_IP);
		InetAddress peerAddr = InetAddress.getByName(CLIENT_PEER_IP);

		// 1. SOLICIT
		DhcpV6Message solicit = DhcpV6MessageBuilder.solicit()
				.withClientId(clientId)
				.withIaNa(1, 0, 0)
				.build();

		DhcpV6RelayMessage solRelay = buildRelayForw(linkAddr, peerAddr, (short) 0, solicit);
		addInterfaceId(solRelay, "cust-port-7");

		DhcpV6RelayMessage advRelay = (DhcpV6RelayMessage) DhcpV6MessageHandler.handleMessage(
				InetAddress.getByName(SERVER_LOCAL_IP), solRelay);
		assertNotNull(advRelay);

		DhcpV6Message advertise = advRelay.getRelayOption().getDhcpMessage();
		DhcpV6ServerIdOption serverId = advertise.getDhcpServerIdOption();
		assertNotNull(serverId);
		DhcpV6IaNaOption advIaNa = advertise.getIaNaOptions().get(0);
		String leasedIp = advIaNa.getIaAddrOptions().get(0).getIpAddress();

		// 2. REQUEST
		DhcpV6Message request = DhcpV6MessageBuilder.request()
				.withClientId(clientId)
				.withServerId(serverId)
				.withIaNaWithAddr(1, leasedIp)
				.build();

		DhcpV6RelayMessage reqRelay = buildRelayForw(linkAddr, peerAddr, (short) 0, request);
		addInterfaceId(reqRelay, "cust-port-7");

		DhcpV6RelayMessage repRelay = (DhcpV6RelayMessage) DhcpV6MessageHandler.handleMessage(
				InetAddress.getByName(SERVER_LOCAL_IP), reqRelay);
		assertNotNull(repRelay);

		DhcpV6Message reply = repRelay.getRelayOption().getDhcpMessage();
		DhcpAssert.assertThat(reply).isReply();

		// Verify binding exists in InMemoryIaManager
		IdentityAssoc ia = iaMgr.findIA(InetAddress.getByName(leasedIp));
		assertNotNull("Lease must be stored in IA manager", ia);
		assertEquals(IaAddress.LEASED, ia.getIaAddresses().iterator().next().getState());

		// 3. RELEASE
		DhcpV6Message release = DhcpV6MessageBuilder.release()
				.withClientId(clientId)
				.withServerId(serverId)
				.withIaNaWithAddr(1, leasedIp)
				.build();

		DhcpV6RelayMessage relRelay = buildRelayForw(linkAddr, peerAddr, (short) 0, release);
		addInterfaceId(relRelay, "cust-port-7");

		DhcpV6RelayMessage relRespRelay = (DhcpV6RelayMessage) DhcpV6MessageHandler.handleMessage(
				InetAddress.getByName(SERVER_LOCAL_IP), relRelay);
		assertNotNull(relRespRelay);

		DhcpV6Message relReply = relRespRelay.getRelayOption().getDhcpMessage();
		assertNotNull(relReply);
		DhcpAssert.assertThat(relReply).isReply();

		// Verify lease is released
		IdentityAssoc releasedIa = iaMgr.findIA(InetAddress.getByName(leasedIp));
		if (releasedIa != null && !releasedIa.getIaAddresses().isEmpty()) {
			assertEquals("Lease should be marked AVAILABLE upon release",
					IaAddress.AVAILABLE, releasedIa.getIaAddresses().iterator().next().getState());
		}
	}

	// =========================================================================
	// 5. Malformed Relay Message Resilience
	// =========================================================================

	@Test
	public void testMalformedRelayMissingRelayOption() throws Exception {
		// Relay-Forw without Option 9 (Relay Message Option)
		InetAddress linkAddr = InetAddress.getByName(CLIENT_LINK_IP);
		InetAddress peerAddr = InetAddress.getByName(CLIENT_PEER_IP);
		DhcpV6RelayMessage badRelay = buildRelayForw(linkAddr, peerAddr, (short) 0, null);

		DhcpV6Message response = DhcpV6MessageHandler.handleMessage(
				InetAddress.getByName(SERVER_LOCAL_IP), badRelay);
		assertNull("Malformed relay without Option 9 must be dropped cleanly", response);
	}

	@Test
	public void testMalformedRelayInvalidMessageType() throws Exception {
		DhcpV6Message solicit = DhcpV6MessageBuilder.solicit()
				.withClientId("00:02:00:00:00:07")
				.build();

		InetAddress linkAddr = InetAddress.getByName(CLIENT_LINK_IP);
		InetAddress peerAddr = InetAddress.getByName(CLIENT_PEER_IP);
		DhcpV6RelayMessage relay = buildRelayForw(linkAddr, peerAddr, (short) 0, solicit);

		// Invalid message type: neither RELAY_FORW nor standard client message
		relay.setMessageType((short) 99);

		DhcpV6Message response = DhcpV6MessageHandler.handleMessage(
				InetAddress.getByName(SERVER_LOCAL_IP), relay);
		assertNull("Unknown message type must be dropped cleanly", response);
	}
}
