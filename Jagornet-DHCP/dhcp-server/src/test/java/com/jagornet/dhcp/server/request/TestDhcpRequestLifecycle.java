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

import java.net.InetAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.option.v4.DhcpV4BootFileNameOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4DomainNameOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4LeaseTimeOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4RoutersOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaAddrOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaNaOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6ServerIdOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6SipServerAddressesOption;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.config.xml.DhcpServerConfig;
import com.jagornet.dhcp.server.db.IdentityAssoc;
import com.jagornet.dhcp.server.request.binding.V4AddrBindingManagerImpl;
import com.jagornet.dhcp.server.request.binding.V6NaAddrBindingManagerImpl;
import com.jagornet.dhcp.server.testutil.DhcpAssert;
import com.jagornet.dhcp.server.testutil.DhcpConfigTestContext;
import com.jagornet.dhcp.server.testutil.DhcpV4MessageBuilder;
import com.jagornet.dhcp.server.testutil.DhcpV6MessageBuilder;
import com.jagornet.dhcp.server.testutil.InMemoryIaManager;

/**
 * End-to-End Request Processor Lifecycle (DORA & SARR) Test Suite.
 * 
 * Exercises the full request processing lifecycle driven by filters,
 * linkFilters, policies, options, and binding pools across:
 * 1. DHCPv4 DORA (Discover, Offer, Request, Ack) with client classification.
 * 2. DHCPv4 Rapid Commit (2-way handshake: Discover -> Ack).
 * 3. DHCPv4 Lease Renewal & Release lifecycle.
 * 4. DHCPv4 Negative & Boundary Paths (off-link NAK, mismatched ServerId,
 * strict link without fallback).
 * 5. DHCPv6 SARR (Solicit, Advertise, Request, Reply) with linkFilter-directed
 * IA_NA pools and options.
 * 6. DHCPv6 Rapid Commit (2-way handshake: Solicit -> Reply).
 * 7. DHCPv6 Release lifecycle.
 * 
 * @author A. Gregory Rabil
 */
public class TestDhcpRequestLifecycle {

	private DhcpConfigTestContext context;
	private InMemoryIaManager iaMgr;
	private V4AddrBindingManagerImpl v4Mgr;
	private V6NaAddrBindingManagerImpl v6Mgr;

	private static final String CONFIG_XML = "<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
			"  <v4ServerIdOption>\n" +
			"    <ipAddress>10.10.1.1</ipAddress>\n" +
			"  </v4ServerIdOption>\n" +
			"  <v6ServerIdOption>\n" +
			"    <opaqueData>\n" +
			"      <hexValue>00010001aabbccdd001122334455</hexValue>\n" +
			"    </opaqueData>\n" +
			"  </v6ServerIdOption>\n" +
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
			"  <filters>\n" +
			"    <filterList>\n" +
			"      <name>GlobalVoipFilter</name>\n" +
			"      <filterExpressions>\n" +
			"        <filterExpressionList>\n" +
			"          <clientClassExpression operator=\"equals\">\n" +
			"            <v4VendorClassOption>\n" +
			"              <opaqueData><asciiValue>Cisco-IP-Phone</asciiValue></opaqueData>\n" +
			"            </v4VendorClassOption>\n" +
			"          </clientClassExpression>\n" +
			"        </filterExpressionList>\n" +
			"      </filterExpressions>\n" +
			"      <policies>\n" +
			"        <policyList>\n" +
			"          <name>v4.defaultLeasetime</name>\n" +
			"          <value>7200</value>\n" +
			"        </policyList>\n" +
			"      </policies>\n" +
			"      <v4ConfigOptions>\n" +
			"        <v4BootFileNameOption>\n" +
			"          <string>voip/global-cisco.bin</string>\n" +
			"        </v4BootFileNameOption>\n" +
			"      </v4ConfigOptions>\n" +
			"    </filterList>\n" +
			"  </filters>\n" +
			"  <links>\n" +
			"    <!-- Corporate V4 Link -->\n" +
			"    <linkList>\n" +
			"      <name>CorporateV4Link</name>\n" +
			"      <address>10.10.1.0/24</address>\n" +
			"      <v4ConfigOptions>\n" +
			"        <v4DomainNameOption>\n" +
			"          <domainName>corp.local</domainName>\n" +
			"        </v4DomainNameOption>\n" +
			"        <v4RoutersOption>\n" +
			"          <ipAddressList>10.10.1.1</ipAddressList>\n" +
			"        </v4RoutersOption>\n" +
			"      </v4ConfigOptions>\n" +
			"      <v4AddrPools>\n" +
			"        <poolList>\n" +
			"          <range>10.10.1.100-10.10.1.199</range>\n" +
			"        </poolList>\n" +
			"      </v4AddrPools>\n" +
			"      <linkFilters>\n" +
			"        <linkFilterList>\n" +
			"          <name>VoipLinkFilter</name>\n" +
			"          <filterExpressions>\n" +
			"            <filterExpressionList>\n" +
			"              <clientClassExpression operator=\"equals\">\n" +
			"                <v4VendorClassOption>\n" +
			"                  <opaqueData><asciiValue>Cisco-IP-Phone</asciiValue></opaqueData>\n" +
			"                </v4VendorClassOption>\n" +
			"              </clientClassExpression>\n" +
			"            </filterExpressionList>\n" +
			"          </filterExpressions>\n" +
			"          <policies>\n" +
			"            <policyList>\n" +
			"              <name>v4.defaultLeasetime</name>\n" +
			"              <value>3600</value>\n" +
			"            </policyList>\n" +
			"          </policies>\n" +
			"          <v4ConfigOptions>\n" +
			"            <v4BootFileNameOption>\n" +
			"              <string>voip/cisco-phone.bin</string>\n" +
			"            </v4BootFileNameOption>\n" +
			"          </v4ConfigOptions>\n" +
			"          <v4AddrPools>\n" +
			"            <poolList>\n" +
			"              <range>10.10.1.10-10.10.1.50</range>\n" +
			"              <policies>\n" +
			"                <policyList>\n" +
			"                  <name>v4.defaultLeasetime</name>\n" +
			"                  <value>3600</value>\n" +
			"                </policyList>\n" +
			"              </policies>\n" +
			"            </poolList>\n" +
			"          </v4AddrPools>\n" +
			"        </linkFilterList>\n" +
			"        <linkFilterList>\n" +
			"          <name>PolycomLinkFilter</name>\n" +
			"          <filterExpressions>\n" +
			"            <filterExpressionList>\n" +
			"              <optionExpression code=\"12\" v4=\"true\" operator=\"startsWith\">\n" +
			"                <stringOption><string>polycom</string></stringOption>\n" +
			"              </optionExpression>\n" +
			"            </filterExpressionList>\n" +
			"          </filterExpressions>\n" +
			"          <policies>\n" +
			"            <policyList>\n" +
			"              <name>v4.defaultLeasetime</name>\n" +
			"              <value>1800</value>\n" +
			"            </policyList>\n" +
			"          </policies>\n" +
			"          <v4ConfigOptions>\n" +
			"            <v4BootFileNameOption>\n" +
			"              <string>voip/polycom.bin</string>\n" +
			"            </v4BootFileNameOption>\n" +
			"          </v4ConfigOptions>\n" +
			"          <v4AddrPools>\n" +
			"            <poolList>\n" +
			"              <range>10.10.1.60-10.10.1.80</range>\n" +
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
			"    <!-- Strict VoIP Link (no default pool) -->\n" +
			"    <linkList>\n" +
			"      <name>StrictVoipV4Link</name>\n" +
			"      <address>10.10.2.0/24</address>\n" +
			"      <linkFilters>\n" +
			"        <linkFilterList>\n" +
			"          <name>StrictVoipOnly</name>\n" +
			"          <filterExpressions>\n" +
			"            <filterExpressionList>\n" +
			"              <clientClassExpression operator=\"equals\">\n" +
			"                <v4VendorClassOption>\n" +
			"                  <opaqueData><asciiValue>Cisco-IP-Phone</asciiValue></opaqueData>\n" +
			"                </v4VendorClassOption>\n" +
			"              </clientClassExpression>\n" +
			"            </filterExpressionList>\n" +
			"          </filterExpressions>\n" +
			"          <v4AddrPools>\n" +
			"            <poolList>\n" +
			"              <range>10.10.2.10-10.10.2.20</range>\n" +
			"            </poolList>\n" +
			"          </v4AddrPools>\n" +
			"        </linkFilterList>\n" +
			"      </linkFilters>\n" +
			"    </linkList>\n" +
			"\n" +
			"    <!-- Rapid Commit V4 Link -->\n" +
			"    <linkList>\n" +
			"      <name>RapidCommitV4Link</name>\n" +
			"      <address>10.10.3.0/24</address>\n" +
			"      <policies>\n" +
			"        <policyList>\n" +
			"          <name>dhcp.supportRapidCommit</name>\n" +
			"          <value>true</value>\n" +
			"        </policyList>\n" +
			"      </policies>\n" +
			"      <v4AddrPools>\n" +
			"        <poolList>\n" +
			"          <range>10.10.3.100-10.10.3.200</range>\n" +
			"        </poolList>\n" +
			"      </v4AddrPools>\n" +
			"    </linkList>\n" +
			"\n" +
			"    <!-- Corporate V6 Link -->\n" +
			"    <linkList>\n" +
			"      <name>CorporateV6Link</name>\n" +
			"      <address>2001:db8:1::/64</address>\n" +
			"      <v6MsgConfigOptions>\n" +
			"        <v6DnsServersOption>\n" +
			"          <ipAddressList>2001:db8:1::53</ipAddressList>\n" +
			"        </v6DnsServersOption>\n" +
			"      </v6MsgConfigOptions>\n" +
			"      <v6NaAddrPools>\n" +
			"        <poolList>\n" +
			"          <range>2001:db8:1::100-2001:db8:1::199</range>\n" +
			"        </poolList>\n" +
			"      </v6NaAddrPools>\n" +
			"      <linkFilters>\n" +
			"        <linkFilterList>\n" +
			"          <name>V6VoipLinkFilter</name>\n" +
			"          <filterExpressions>\n" +
			"            <filterExpressionList>\n" +
			"              <clientClassExpression operator=\"equals\">\n" +
			"                <v6VendorClassOption enterpriseNumber=\"9\">\n" +
			"                  <opaqueDataList><asciiValue>Cisco-Voip</asciiValue></opaqueDataList>\n" +
			"                </v6VendorClassOption>\n" +
			"              </clientClassExpression>\n" +
			"            </filterExpressionList>\n" +
			"          </filterExpressions>\n" +
			"          <policies>\n" +
			"            <policyList>\n" +
			"              <name>v6.validLifetime</name>\n" +
			"              <value>3600</value>\n" +
			"            </policyList>\n" +
			"            <policyList>\n" +
			"              <name>v6.preferredLifetime</name>\n" +
			"              <value>1800</value>\n" +
			"            </policyList>\n" +
			"          </policies>\n" +
			"          <v6MsgConfigOptions>\n" +
			"            <v6SipServerAddressesOption>\n" +
			"              <ipAddressList>2001:db8:1::5060</ipAddressList>\n" +
			"            </v6SipServerAddressesOption>\n" +
			"          </v6MsgConfigOptions>\n" +
			"          <v6NaAddrPools>\n" +
			"            <poolList>\n" +
			"              <range>2001:db8:1::10-2001:db8:1::50</range>\n" +
			"              <policies>\n" +
			"                <policyList>\n" +
			"                  <name>v6.validLifetime</name>\n" +
			"                  <value>3600</value>\n" +
			"                </policyList>\n" +
			"                <policyList>\n" +
			"                  <name>v6.preferredLifetime</name>\n" +
			"                  <value>1800</value>\n" +
			"                </policyList>\n" +
			"              </policies>\n" +
			"            </poolList>\n" +
			"          </v6NaAddrPools>\n" +
			"        </linkFilterList>\n" +
			"      </linkFilters>\n" +
			"    </linkList>\n" +
			"\n" +
			"    <!-- Rapid Commit V6 Link -->\n" +
			"    <linkList>\n" +
			"      <name>RapidCommitV6Link</name>\n" +
			"      <address>2001:db8:3::/64</address>\n" +
			"      <policies>\n" +
			"        <policyList>\n" +
			"          <name>dhcp.supportRapidCommit</name>\n" +
			"          <value>true</value>\n" +
			"        </policyList>\n" +
			"      </policies>\n" +
			"      <v6NaAddrPools>\n" +
			"        <poolList>\n" +
			"          <range>2001:db8:3::100-2001:db8:3::200</range>\n" +
			"        </poolList>\n" +
			"      </v6NaAddrPools>\n" +
			"    </linkList>\n" +
			"  </links>\n" +
			"</dhc:dhcpServerConfig>";

	@Before
	public void setUp() throws Exception {
		context = new DhcpConfigTestContext();
		DhcpServerConfig serverConfig = context.initXml(CONFIG_XML);
		assertNotNull(serverConfig);

		iaMgr = new InMemoryIaManager();

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

	@After
	public void tearDown() {
		if (v4Mgr != null) {
			v4Mgr.close();
		}
		if (v6Mgr != null) {
			v6Mgr.close();
		}
		if (context != null) {
			context.close();
		}
	}

	/**
	 * Test complete DHCPv4 DORA lifecycle for a client matching a LinkFilter (Cisco
	 * VoIP).
	 * Verifies:
	 * 1. Discover -> Offer: Allocated address in filtered pool (10.10.1.10-50).
	 * 2. Scoped boot file name option ('voip/cisco-phone.bin') and LinkFilter lease
	 * time (3600).
	 * 3. Request -> Ack: State transitions to LEASED in lease database, identical
	 * IP confirmed.
	 */
	@Test
	public void testV4DoraFilteredVoipClient() throws Exception {
		InetAddress clientLink = InetAddress.getByName("10.10.1.1");
		String mac = "00:11:22:33:44:55";

		// 1. DISCOVER
		DhcpV4Message discoverMsg = DhcpV4MessageBuilder.discover()
				.withMac(mac)
				.withVendorClass("Cisco-IP-Phone")
				.build();

		DhcpV4DiscoverProcessor dProc = new DhcpV4DiscoverProcessor(discoverMsg, clientLink);
		DhcpV4Message offerMsg = dProc.processMessage();

		assertNotNull("Offer message must not be null", offerMsg);
		assertEquals(DhcpConstants.V4_OP_REPLY, offerMsg.getOp());
		assertEquals(DhcpConstants.V4MESSAGE_TYPE_OFFER, offerMsg.getMessageType());
		assertEquals(discoverMsg.getTransactionId(), offerMsg.getTransactionId());

		// Verify offered IP is inside VoIP LinkFilter pool
		InetAddress offeredIp = offerMsg.getYiAddr();
		assertNotNull("Offered yiAddr must be populated", offeredIp);
		DhcpAssert.assertIpInRange(offeredIp, "10.10.1.10", "10.10.1.50");

		// Verify options and policy on Offer
		DhcpV4LeaseTimeOption leaseTimeOpt = (DhcpV4LeaseTimeOption) offerMsg
				.getDhcpOption(DhcpConstants.V4OPTION_LEASE_TIME);
		assertNotNull(leaseTimeOpt);
		assertEquals("VoIP LinkFilter lease time should be 3600", 3600L, leaseTimeOpt.getUnsignedInt());

		DhcpV4BootFileNameOption bootOpt = (DhcpV4BootFileNameOption) offerMsg
				.getDhcpOption(DhcpConstants.V4OPTION_BOOT_FILE_NAME);
		assertNotNull(bootOpt);
		assertEquals("voip/cisco-phone.bin", bootOpt.getString());

		// Verify binding recorded in InMemoryIaManager as OFFERED
		IdentityAssoc ia = iaMgr.findIA(discoverMsg.getClientId(), IdentityAssoc.V4_TYPE, 0);
		assertNotNull("IA must be stored in DB", ia);
		assertEquals(IdentityAssoc.OFFERED, ia.getState());

		// 2. REQUEST
		DhcpV4Message requestMsg = DhcpV4MessageBuilder.request()
				.withMac(mac)
				.withTransactionId(offerMsg.getTransactionId())
				.withVendorClass("Cisco-IP-Phone")
				.withRequestedIp(offeredIp.getHostAddress())
				.withServerId(offerMsg.getDhcpV4ServerIdOption().getIpAddress())
				.build();

		DhcpV4RequestProcessor rProc = new DhcpV4RequestProcessor(requestMsg, clientLink);
		DhcpV4Message ackMsg = rProc.processMessage();

		assertNotNull("Ack message must not be null", ackMsg);
		assertEquals(DhcpConstants.V4_OP_REPLY, ackMsg.getOp());
		assertEquals(DhcpConstants.V4MESSAGE_TYPE_ACK, ackMsg.getMessageType());
		assertEquals(offeredIp, ackMsg.getYiAddr());

		// Verify database state transitioned to LEASED
		IdentityAssoc leasedIa = iaMgr.findIA(discoverMsg.getClientId(), IdentityAssoc.V4_TYPE, 0);
		assertNotNull(leasedIa);
		assertEquals("Binding state must be LEASED", IdentityAssoc.LEASED, leasedIa.getState());
	}

	/**
	 * Test complete DHCPv4 DORA lifecycle for a generic client NOT matching any
	 * filter.
	 * Verifies:
	 * 1. Allocated address from default pool (10.10.1.100-199).
	 * 2. Link options (domainName 'corp.local', routers '10.10.1.1') and global
	 * lease time (86400).
	 * 3. Does NOT receive VoIP boot file option.
	 */
	@Test
	public void testV4DoraDefaultClient() throws Exception {
		InetAddress clientLink = InetAddress.getByName("10.10.1.1");
		String mac = "00:aa:bb:cc:dd:ee";

		// 1. DISCOVER
		DhcpV4Message discoverMsg = DhcpV4MessageBuilder.discover()
				.withMac(mac)
				.withVendorClass("MSFT 5.0")
				.build();

		DhcpV4DiscoverProcessor dProc = new DhcpV4DiscoverProcessor(discoverMsg, clientLink);
		DhcpV4Message offerMsg = dProc.processMessage();

		assertNotNull("Offer message must not be null", offerMsg);
		assertEquals(DhcpConstants.V4MESSAGE_TYPE_OFFER, offerMsg.getMessageType());

		// Default pool check
		InetAddress offeredIp = offerMsg.getYiAddr();
		DhcpAssert.assertIpInRange(offeredIp, "10.10.1.100", "10.10.1.199");

		// Global policy check
		DhcpV4LeaseTimeOption leaseOpt = (DhcpV4LeaseTimeOption) offerMsg
				.getDhcpOption(DhcpConstants.V4OPTION_LEASE_TIME);
		assertNotNull(leaseOpt);
		assertEquals("Global lease time must be 86400", 86400L, leaseOpt.getUnsignedInt());

		// Link options check
		DhcpV4DomainNameOption domainOpt = (DhcpV4DomainNameOption) offerMsg
				.getDhcpOption(DhcpConstants.V4OPTION_DOMAIN_NAME);
		assertNotNull(domainOpt);
		assertEquals("corp.local", domainOpt.getString());

		DhcpV4RoutersOption routerOpt = (DhcpV4RoutersOption) offerMsg.getDhcpOption(DhcpConstants.V4OPTION_ROUTERS);
		assertNotNull(routerOpt);

		// Negative check: VoIP boot option should NOT be present
		assertNull(offerMsg.getDhcpOption(DhcpConstants.V4OPTION_BOOT_FILE_NAME));

		// 2. REQUEST
		DhcpV4Message requestMsg = DhcpV4MessageBuilder.request()
				.withMac(mac)
				.withTransactionId(offerMsg.getTransactionId())
				.withVendorClass("MSFT 5.0")
				.withRequestedIp(offeredIp.getHostAddress())
				.withServerId(offerMsg.getDhcpV4ServerIdOption().getIpAddress())
				.build();

		DhcpV4RequestProcessor rProc = new DhcpV4RequestProcessor(requestMsg, clientLink);
		DhcpV4Message ackMsg = rProc.processMessage();

		assertNotNull(ackMsg);
		assertEquals(DhcpConstants.V4MESSAGE_TYPE_ACK, ackMsg.getMessageType());
		assertEquals(offeredIp, ackMsg.getYiAddr());
	}

	/**
	 * Test secondary filter matching based on Option 12 (Host Name prefix
	 * 'polycom').
	 * Verifies proper segregation into 10.10.1.60-80 with 1800s lease time.
	 */
	@Test
	public void testV4DoraSecondaryFilterHostname() throws Exception {
		InetAddress clientLink = InetAddress.getByName("10.10.1.1");
		String mac = "00:04:f2:88:99:aa";

		DhcpV4Message discoverMsg = DhcpV4MessageBuilder.discover()
				.withMac(mac)
				.withHostName("polycom-conference-phone")
				.build();

		DhcpV4DiscoverProcessor dProc = new DhcpV4DiscoverProcessor(discoverMsg, clientLink);
		DhcpV4Message offerMsg = dProc.processMessage();

		assertNotNull(offerMsg);
		assertEquals(DhcpConstants.V4MESSAGE_TYPE_OFFER, offerMsg.getMessageType());

		// Polycom pool: 10.10.1.60 - 10.10.1.80
		DhcpAssert.assertIpInRange(offerMsg.getYiAddr(), "10.10.1.60", "10.10.1.80");

		DhcpV4LeaseTimeOption leaseOpt = (DhcpV4LeaseTimeOption) offerMsg
				.getDhcpOption(DhcpConstants.V4OPTION_LEASE_TIME);
		assertNotNull(leaseOpt);
		assertEquals("Polycom filter lease time should be 1800", 1800L, leaseOpt.getUnsignedInt());

		DhcpV4BootFileNameOption bootOpt = (DhcpV4BootFileNameOption) offerMsg
				.getDhcpOption(DhcpConstants.V4OPTION_BOOT_FILE_NAME);
		assertNotNull(bootOpt);
		assertEquals("voip/polycom.bin", bootOpt.getString());
	}

	/**
	 * Test DHCPv4 Rapid Commit 2-way handshake (Discover -> Ack directly, bypassing
	 * Offer & Request).
	 */
	@Test
	public void testV4RapidCommitTwoWayHandshake() throws Exception {
		InetAddress clientLink = InetAddress.getByName("10.10.3.1");
		String mac = "00:50:56:11:22:33";

		DhcpV4Message discoverMsg = DhcpV4MessageBuilder.discover()
				.withMac(mac)
				.withRapidCommit()
				.build();

		DhcpV4DiscoverProcessor dProc = new DhcpV4DiscoverProcessor(discoverMsg, clientLink);
		DhcpV4Message replyMsg = dProc.processMessage();

		assertNotNull(replyMsg);
		assertEquals("Rapid Commit must yield immediate ACK", DhcpConstants.V4MESSAGE_TYPE_ACK,
				replyMsg.getMessageType());
		DhcpAssert.assertIpInRange(replyMsg.getYiAddr(), "10.10.3.100", "10.10.3.200");

		// State in database must be directly LEASED
		IdentityAssoc ia = iaMgr.findIA(discoverMsg.getClientId(), IdentityAssoc.V4_TYPE, 0);
		assertNotNull(ia);
		assertEquals("Rapid commit must immediately create LEASED binding", IdentityAssoc.LEASED, ia.getState());
	}

	/**
	 * Test DHCPv4 Lease Renewal cycle (Request with ciAddr set and unicast).
	 */
	@Test
	public void testV4LeaseRenewal() throws Exception {
		InetAddress clientLink = InetAddress.getByName("10.10.1.1");
		String mac = "00:11:22:77:88:99";

		// Initial DORA
		DhcpV4Message disc = DhcpV4MessageBuilder.discover().withMac(mac).build();
		DhcpV4Message offer = new DhcpV4DiscoverProcessor(disc, clientLink).processMessage();
		assertNotNull(offer);

		DhcpV4Message req = DhcpV4MessageBuilder.request()
				.withMac(mac)
				.withTransactionId(offer.getTransactionId())
				.withRequestedIp(offer.getYiAddr().getHostAddress())
				.withServerId(offer.getDhcpV4ServerIdOption().getIpAddress())
				.build();
		DhcpV4Message ack = new DhcpV4RequestProcessor(req, clientLink).processMessage();
		assertNotNull(ack);
		InetAddress leasedIp = ack.getYiAddr();

		// RENEW: unicast request with ciAddr set, no requested IP option
		DhcpV4Message renewMsg = DhcpV4MessageBuilder.request()
				.withMac(mac)
				.withCiAddr(leasedIp)
				.withUnicast(true)
				.build();

		DhcpV4RequestProcessor renewProc = new DhcpV4RequestProcessor(renewMsg, clientLink);
		DhcpV4Message renewAck = renewProc.processMessage();

		assertNotNull("Renew must return ACK", renewAck);
		assertEquals(DhcpConstants.V4MESSAGE_TYPE_ACK, renewAck.getMessageType());
		assertEquals("Renewed IP must match leased IP", leasedIp, renewAck.getYiAddr());
	}

	/**
	 * Test DHCPv4 Release lifecycle and immediate address reuse by a subsequent
	 * client.
	 */
	@Test
	public void testV4ReleaseAndAddressReuse() throws Exception {
		InetAddress clientLink = InetAddress.getByName("10.10.1.1");
		String mac1 = "00:11:22:aa:bb:01";
		String mac2 = "00:11:22:aa:bb:02";

		// Client 1 leases IP
		DhcpV4Message disc1 = DhcpV4MessageBuilder.discover().withMac(mac1).build();
		DhcpV4Message offer1 = new DhcpV4DiscoverProcessor(disc1, clientLink).processMessage();
		assertNotNull(offer1);
		InetAddress allocatedIp = offer1.getYiAddr();

		DhcpV4Message req1 = DhcpV4MessageBuilder.request()
				.withMac(mac1)
				.withRequestedIp(allocatedIp.getHostAddress())
				.withServerId(offer1.getDhcpV4ServerIdOption().getIpAddress())
				.build();
		DhcpV4Message ack1 = new DhcpV4RequestProcessor(req1, clientLink).processMessage();
		assertNotNull(ack1);

		// Client 1 releases IP
		DhcpV4Message relMsg = DhcpV4MessageBuilder.release()
				.withMac(mac1)
				.withCiAddr(allocatedIp)
				.withServerId(offer1.getDhcpV4ServerIdOption().getIpAddress())
				.build();

		DhcpV4ReleaseProcessor relProc = new DhcpV4ReleaseProcessor(relMsg, clientLink);
		relProc.processMessage();

		// Client 2 requests on same link - system should operate normally without
		// conflict
		DhcpV4Message disc2 = DhcpV4MessageBuilder.discover().withMac(mac2).build();
		DhcpV4Message offer2 = new DhcpV4DiscoverProcessor(disc2, clientLink).processMessage();
		assertNotNull("Subsequent client can discover after release", offer2);
		DhcpAssert.assertIpInRange(offer2.getYiAddr(), "10.10.1.100", "10.10.1.199");
	}

	/**
	 * Test negative path: Client requests off-link IP in REQUEST -> server returns
	 * NAK.
	 */
	@Test
	public void testV4NegativeOffLinkRequestedIpNak() throws Exception {
		InetAddress clientLink = InetAddress.getByName("10.10.1.1");
		String mac = "00:11:22:cc:dd:ee";

		DhcpV4Message offLinkReq = DhcpV4MessageBuilder.request()
				.withMac(mac)
				.withRequestedIp("192.168.99.50")
				.withServerId("10.10.1.1")
				.build();

		DhcpV4RequestProcessor rProc = new DhcpV4RequestProcessor(offLinkReq, clientLink);
		DhcpV4Message reply = rProc.processMessage();

		assertNotNull(reply);
		assertEquals("Off-link request must result in NAK", DhcpConstants.V4MESSAGE_TYPE_NAK, reply.getMessageType());
	}

	/**
	 * Test negative path: REQUEST with mismatched ServerId directed to another
	 * server -> ignored.
	 */
	@Test
	public void testV4NegativeMismatchedServerIdIgnored() throws Exception {
		InetAddress clientLink = InetAddress.getByName("10.10.1.1");
		String mac = "00:11:22:11:33:55";

		DhcpV4Message wrongServerReq = DhcpV4MessageBuilder.request()
				.withMac(mac)
				.withRequestedIp("10.10.1.150")
				.withServerId("192.168.1.99") // Different server
				.build();

		DhcpV4RequestProcessor rProc = new DhcpV4RequestProcessor(wrongServerReq, clientLink);
		DhcpV4Message reply = rProc.processMessage();

		assertNull("Request addressed to different server ID must be dropped/ignored", reply);
	}

	/**
	 * Test negative path: Client on StrictVoipV4Link without matching VoIP vendor
	 * class -> no offer.
	 */
	@Test
	public void testV4NegativeStrictFilteredLinkNoOffer() throws Exception {
		InetAddress clientLink = InetAddress.getByName("10.10.2.1"); // Strict VoIP link
		String mac = "00:11:22:88:88:88";

		DhcpV4Message discoverMsg = DhcpV4MessageBuilder.discover()
				.withMac(mac)
				.withVendorClass("Generic-Workstation")
				.build();

		DhcpV4DiscoverProcessor dProc = new DhcpV4DiscoverProcessor(discoverMsg, clientLink);
		DhcpV4Message offer = dProc.processMessage();

		assertNull("Unmatched client on link without default pool must be dropped", offer);
	}

	/**
	 * Test complete DHCPv6 SARR lifecycle for a client matching a v6 LinkFilter
	 * (Cisco VoIP).
	 * Verifies:
	 * 1. Solicit -> Advertise: Allocated address in filtered pool
	 * (2001:db8:1::10-50).
	 * 2. Scoped SIP server option (2001:db8:1::5060) and LinkFilter lifetimes (3600
	 * valid, 1800 preferred).
	 * 3. Request -> Reply: State transitions to LEASED in lease database, identical
	 * IP confirmed.
	 */
	@Test
	public void testV6SarrFilteredVoipClient() throws Exception {
		InetAddress clientLink = InetAddress.getByName("2001:db8:1::1");
		String clientIdHex = "00010001aabbccdd112233445566";

		// 1. SOLICIT
		DhcpV6Message solicitMsg = DhcpV6MessageBuilder.solicit()
				.withClientId(clientIdHex)
				.withRemoteAddress("2001:db8:1::100", 546)
				.withVendorClass(9, "Cisco-Voip")
				.withIaNa(1)
				.build();

		DhcpV6SolicitProcessor sProc = new DhcpV6SolicitProcessor(solicitMsg, clientLink);
		DhcpV6Message advMsg = sProc.processMessage();

		assertNotNull("Advertise message must not be null", advMsg);
		assertEquals(DhcpConstants.V6MESSAGE_TYPE_ADVERTISE, advMsg.getMessageType());
		assertEquals(solicitMsg.getTransactionId(), advMsg.getTransactionId());

		// Verify IA_NA contains address from filtered pool (2001:db8:1::10 -
		// 2001:db8:1::50)
		assertEquals(1, advMsg.getIaNaOptions().size());
		DhcpV6IaNaOption iaNa = advMsg.getIaNaOptions().get(0);
		assertEquals(1, iaNa.getIaAddrOptions().size());
		DhcpV6IaAddrOption iaAddr = iaNa.getIaAddrOptions().get(0);

		DhcpAssert.assertIpInRange(iaAddr.getIpAddress(), "2001:db8:1::10", "2001:db8:1::50");
		assertEquals("Filtered valid lifetime must be 3600", 3600L, iaAddr.getValidLifetime());
		assertEquals("Filtered preferred lifetime must be 1800", 1800L, iaAddr.getPreferredLifetime());

		// Verify scoped SIP option
		DhcpV6SipServerAddressesOption sipOpt = (DhcpV6SipServerAddressesOption) advMsg
				.getDhcpOption(DhcpConstants.V6OPTION_SIP_SERVERS_ADDRESS_LIST);
		assertNotNull("VoIP client must receive SIP server address option", sipOpt);

		// 2. REQUEST
		DhcpV6ServerIdOption serverId = advMsg.getDhcpServerIdOption();
		assertNotNull("ServerId must be present in Advertise", serverId);

		DhcpV6Message reqMsg = DhcpV6MessageBuilder.request()
				.withClientId(clientIdHex)
				.withServerId(serverId.getOpaqueData().getHex())
				.withRemoteAddress("2001:db8:1::100", 546)
				.withVendorClass(9, "Cisco-Voip")
				.withIaNa(1)
				.withIaAddr(iaAddr.getIpAddress())
				.build();

		DhcpV6RequestProcessor rProc = new DhcpV6RequestProcessor(reqMsg, clientLink);
		DhcpV6Message replyMsg = rProc.processMessage();

		assertNotNull("Reply message must not be null", replyMsg);
		assertEquals(DhcpConstants.V6MESSAGE_TYPE_REPLY, replyMsg.getMessageType());

		DhcpV6IaAddrOption replyAddr = replyMsg.getIaNaOptions().get(0).getIaAddrOptions().get(0);
		assertEquals("Assigned address in Reply must match advertised address",
				iaAddr.getIpAddress(), replyAddr.getIpAddress());

		// Verify database state is LEASED
		byte[] duid = solicitMsg.getDhcpClientIdOption().getOpaqueData().getHex();
		IdentityAssoc ia = iaMgr.findIA(duid, IdentityAssoc.NA_TYPE, 1);
		assertNotNull("IA must be stored in DB", ia);
		assertEquals(IdentityAssoc.LEASED, ia.getState());
	}

	/**
	 * Test complete DHCPv6 SARR lifecycle for a generic client without matching
	 * filter.
	 * Verifies allocation in default pool (2001:db8:1::100 - 2001:db8:1::199).
	 */
	@Test
	public void testV6SarrDefaultClient() throws Exception {
		InetAddress clientLink = InetAddress.getByName("2001:db8:1::1");
		String clientIdHex = "00010001aabbccdd998877665544";

		// 1. SOLICIT
		DhcpV6Message solicitMsg = DhcpV6MessageBuilder.solicit()
				.withClientId(clientIdHex)
				.withRemoteAddress("2001:db8:1::100", 546)
				.withIaNa(1)
				.build();

		DhcpV6SolicitProcessor sProc = new DhcpV6SolicitProcessor(solicitMsg, clientLink);
		DhcpV6Message advMsg = sProc.processMessage();

		assertNotNull(advMsg);
		assertEquals(DhcpConstants.V6MESSAGE_TYPE_ADVERTISE, advMsg.getMessageType());

		DhcpV6IaAddrOption iaAddr = advMsg.getIaNaOptions().get(0).getIaAddrOptions().get(0);
		DhcpAssert.assertIpInRange(iaAddr.getIpAddress(), "2001:db8:1::100", "2001:db8:1::199");

		// Negative check: SIP server option should NOT be present for standard client
		assertNull(advMsg.getDhcpOption(DhcpConstants.V6OPTION_SIP_SERVERS_ADDRESS_LIST));

		// 2. REQUEST
		DhcpV6ServerIdOption serverId = advMsg.getDhcpServerIdOption();
		DhcpV6Message reqMsg = DhcpV6MessageBuilder.request()
				.withClientId(clientIdHex)
				.withServerId(serverId.getOpaqueData().getHex())
				.withRemoteAddress("2001:db8:1::100", 546)
				.withIaNa(1)
				.withIaAddr(iaAddr.getIpAddress())
				.build();

		DhcpV6RequestProcessor rProc = new DhcpV6RequestProcessor(reqMsg, clientLink);
		DhcpV6Message replyMsg = rProc.processMessage();

		assertNotNull(replyMsg);
		assertEquals(DhcpConstants.V6MESSAGE_TYPE_REPLY, replyMsg.getMessageType());
		assertEquals(iaAddr.getIpAddress(), replyMsg.getIaNaOptions().get(0).getIaAddrOptions().get(0).getIpAddress());
	}

	/**
	 * Test DHCPv6 Rapid Commit 2-way handshake (Solicit -> Reply directly).
	 */
	@Test
	public void testV6RapidCommitTwoWayHandshake() throws Exception {
		InetAddress clientLink = InetAddress.getByName("2001:db8:3::1");
		String clientIdHex = "00010001aabbccdd334455667788";

		DhcpV6Message solicitMsg = DhcpV6MessageBuilder.solicit()
				.withClientId(clientIdHex)
				.withRemoteAddress("2001:db8:3::100", 546)
				.withIaNa(1)
				.withRapidCommit()
				.build();

		DhcpV6SolicitProcessor sProc = new DhcpV6SolicitProcessor(solicitMsg, clientLink);
		DhcpV6Message replyMsg = sProc.processMessage();

		assertNotNull("Rapid commit must return immediate reply", replyMsg);
		assertEquals("Rapid commit must yield REPLY message type", DhcpConstants.V6MESSAGE_TYPE_REPLY,
				replyMsg.getMessageType());

		DhcpV6IaAddrOption iaAddr = replyMsg.getIaNaOptions().get(0).getIaAddrOptions().get(0);
		DhcpAssert.assertIpInRange(iaAddr.getIpAddress(), "2001:db8:3::100", "2001:db8:3::200");

		// State in database must be directly LEASED
		byte[] duid = solicitMsg.getDhcpClientIdOption().getOpaqueData().getHex();
		IdentityAssoc ia = iaMgr.findIA(duid, IdentityAssoc.NA_TYPE, 1);
		assertNotNull(ia);
		assertEquals("Rapid commit must immediately create LEASED binding", IdentityAssoc.LEASED, ia.getState());
	}

	/**
	 * Test DHCPv6 Release lifecycle.
	 */
	@Test
	public void testV6ReleaseLifecycle() throws Exception {
		InetAddress clientLink = InetAddress.getByName("2001:db8:1::1");
		String clientIdHex = "00010001aabbccdd445566778899";

		// 1. Establish lease through SARR
		DhcpV6Message sol = DhcpV6MessageBuilder.solicit()
				.withClientId(clientIdHex)
				.withRemoteAddress("2001:db8:1::100", 546)
				.withIaNa(1)
				.build();
		DhcpV6Message adv = new DhcpV6SolicitProcessor(sol, clientLink).processMessage();
		assertNotNull(adv);

		DhcpV6IaAddrOption iaAddr = adv.getIaNaOptions().get(0).getIaAddrOptions().get(0);
		DhcpV6Message req = DhcpV6MessageBuilder.request()
				.withClientId(clientIdHex)
				.withServerId(adv.getDhcpServerIdOption().getOpaqueData().getHex())
				.withRemoteAddress("2001:db8:1::100", 546)
				.withIaNa(1)
				.withIaAddr(iaAddr.getIpAddress())
				.build();
		DhcpV6Message reply = new DhcpV6RequestProcessor(req, clientLink).processMessage();
		assertNotNull(reply);

		// 2. RELEASE
		DhcpV6Message relMsg = DhcpV6MessageBuilder.release()
				.withClientId(clientIdHex)
				.withServerId(adv.getDhcpServerIdOption().getOpaqueData().getHex())
				.withRemoteAddress("2001:db8:1::100", 546)
				.withIaNa(1)
				.withIaAddr(iaAddr.getIpAddress())
				.build();

		DhcpV6ReleaseProcessor relProc = new DhcpV6ReleaseProcessor(relMsg, clientLink);
		DhcpV6Message relReply = relProc.processMessage();

		assertNotNull("Release must return Reply message", relReply);
		assertEquals(DhcpConstants.V6MESSAGE_TYPE_REPLY, relReply.getMessageType());
	}
}
