/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.request.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.db.IaManager;
import com.jagornet.dhcp.server.testutil.DhcpAssert;
import com.jagornet.dhcp.server.testutil.DhcpConfigTestContext;
import com.jagornet.dhcp.server.testutil.DhcpV4MessageBuilder;
import com.jagornet.dhcp.server.testutil.DhcpV6MessageBuilder;

/**
 * Unit and integration tests for address and prefix pool isolation via
 * LinkFilter.
 * Verifies that:
 * 1. Binding managers order LinkFilter pools before unfiltered default pools.
 * 2. Clients matching a LinkFilter allocate addresses/prefixes from the
 * filtered pool.
 * 3. Clients not matching a LinkFilter allocate from the link's default pool.
 * 4. Non-matching clients cannot request or obtain addresses inside filtered
 * pools.
 * 5. Pool exhaustion in filtered pools is isolated and does not break default
 * pool allocations.
 * 
 * @author A. Gregory Rabil
 */
public class TestLinkFilterPoolAllocation {

	private DhcpConfigTestContext context;
	private IaManager mockIaMgr;

	@Before
	public void setUp() {
		context = new DhcpConfigTestContext();
		mockIaMgr = mock(IaManager.class);
		when(mockIaMgr.findExistingIPs(any(), any())).thenReturn(Collections.emptyList());
	}

	@After
	public void tearDown() {
		if (context != null) {
			context.close();
		}
	}

	@Test
	public void testV4PoolOrderingOnLink() throws Exception {
		String xml = "<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
				"  <links>\n" +
				"    <linkList>\n" +
				"      <name>OfficeLink</name>\n" +
				"      <address>192.168.1.0/24</address>\n" +
				"      <v4AddrPools>\n" +
				"        <poolList>\n" +
				"          <range>192.168.1.100-192.168.1.199</range>\n" +
				"        </poolList>\n" +
				"      </v4AddrPools>\n" +
				"      <linkFilters>\n" +
				"        <linkFilterList>\n" +
				"          <name>VoipPoolFilter</name>\n" +
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
				"              <range>192.168.1.10-192.168.1.50</range>\n" +
				"            </poolList>\n" +
				"          </v4AddrPools>\n" +
				"        </linkFilterList>\n" +
				"      </linkFilters>\n" +
				"    </linkList>\n" +
				"  </links>\n" +
				"</dhc:dhcpServerConfig>";

		context.initXml(xml);

		V4AddrBindingManagerImpl v4Manager = new V4AddrBindingManagerImpl();
		try {
			v4Manager.setIaMgr(mockIaMgr);
			v4Manager.init();

			List<? extends BindingPool> pools = v4Manager.getBindingPoolMap().get("192.168.1.0/24");
			assertNotNull("Pools must be registered for link", pools);
			assertEquals(2, pools.size());

			// Filtered pool must be first in list
			BindingPool pool0 = pools.get(0);
			assertNotNull("First pool must have link filter", pool0.getLinkFilter());
			assertEquals("VoipPoolFilter", pool0.getLinkFilter().getName());
			assertEquals("192.168.1.10", pool0.getStartAddress().getHostAddress());
			assertEquals("192.168.1.50", pool0.getEndAddress().getHostAddress());

			// Unfiltered default pool must be second in list
			BindingPool pool1 = pools.get(1);
			assertNull("Second pool must be unfiltered default", pool1.getLinkFilter());
			assertEquals("192.168.1.100", pool1.getStartAddress().getHostAddress());
			assertEquals("192.168.1.199", pool1.getEndAddress().getHostAddress());
		} finally {
			v4Manager.close();
		}
	}

	@Test
	public void testV4DynamicAllocationAndPoolIsolation() throws Exception {
		String xml = "<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
				"  <links>\n" +
				"    <linkList>\n" +
				"      <name>OfficeLink</name>\n" +
				"      <address>192.168.1.0/24</address>\n" +
				"      <v4AddrPools>\n" +
				"        <poolList>\n" +
				"          <range>192.168.1.100-192.168.1.199</range>\n" +
				"        </poolList>\n" +
				"      </v4AddrPools>\n" +
				"      <linkFilters>\n" +
				"        <linkFilterList>\n" +
				"          <name>VoipPoolFilter</name>\n" +
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
				"              <range>192.168.1.10-192.168.1.50</range>\n" +
				"            </poolList>\n" +
				"          </v4AddrPools>\n" +
				"        </linkFilterList>\n" +
				"      </linkFilters>\n" +
				"    </linkList>\n" +
				"  </links>\n" +
				"</dhc:dhcpServerConfig>";

		context.initXml(xml);
		DhcpLink link = DhcpServerConfiguration.getInstance().getLinkMap().values().iterator().next();
		assertNotNull(link);

		V4AddrBindingManagerImpl v4Manager = new V4AddrBindingManagerImpl();
		try {
			v4Manager.setIaMgr(mockIaMgr);
			v4Manager.init();

			// 1. Matching VoIP Client
			DhcpV4Message voipMsg = DhcpV4MessageBuilder.discover()
					.withMac("00:11:22:33:44:55")
					.withVendorClass("Cisco-IP-Phone")
					.build();

			InetAddress voipIp = v4Manager.getNextFreeAddress(link, voipMsg);
			assertNotNull("Must allocate IP for matching VoIP client", voipIp);
			DhcpAssert.assertIpInRange(voipIp, "192.168.1.10", "192.168.1.50");

			// 2. Non-matching PC Client
			DhcpV4Message pcMsg = DhcpV4MessageBuilder.discover()
					.withMac("00:aa:bb:cc:dd:ee")
					.withVendorClass("Standard-PC")
					.build();

			InetAddress pcIp = v4Manager.getNextFreeAddress(link, pcMsg);
			assertNotNull("Must allocate IP for non-matching PC client", pcIp);
			DhcpAssert.assertIpInRange(pcIp, "192.168.1.100", "192.168.1.199");

			// 3. Client with no Vendor Class
			DhcpV4Message plainMsg = DhcpV4MessageBuilder.discover()
					.withMac("00:aa:bb:11:22:33")
					.build();

			InetAddress plainIp = v4Manager.getNextFreeAddress(link, plainMsg);
			assertNotNull("Must allocate IP for plain client", plainIp);
			DhcpAssert.assertIpInRange(plainIp, "192.168.1.100", "192.168.1.199");
		} finally {
			v4Manager.close();
		}
	}

	@Test
	public void testV4RequestedAddressStrictIsolation() throws Exception {
		String xml = "<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
				"  <links>\n" +
				"    <linkList>\n" +
				"      <name>OfficeLink</name>\n" +
				"      <address>192.168.1.0/24</address>\n" +
				"      <v4AddrPools>\n" +
				"        <poolList>\n" +
				"          <range>192.168.1.100-192.168.1.199</range>\n" +
				"        </poolList>\n" +
				"      </v4AddrPools>\n" +
				"      <linkFilters>\n" +
				"        <linkFilterList>\n" +
				"          <name>VoipPoolFilter</name>\n" +
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
				"              <range>192.168.1.10-192.168.1.50</range>\n" +
				"            </poolList>\n" +
				"          </v4AddrPools>\n" +
				"        </linkFilterList>\n" +
				"      </linkFilters>\n" +
				"    </linkList>\n" +
				"  </links>\n" +
				"</dhc:dhcpServerConfig>";

		context.initXml(xml);
		DhcpLink link = DhcpServerConfiguration.getInstance().getLinkMap().values().iterator().next();
		assertNotNull(link);

		V4AddrBindingManagerImpl v4Manager = new V4AddrBindingManagerImpl();
		try {
			v4Manager.setIaMgr(mockIaMgr);
			v4Manager.init();

			InetAddress voipPoolAddr = InetAddress.getByName("192.168.1.25");

			// Non-matching client explicitly requesting filtered VoIP IP
			DhcpV4Message pcRequestMsg = DhcpV4MessageBuilder.request()
					.withMac("00:aa:bb:cc:dd:ee")
					.withVendorClass("Standard-PC")
					.withRequestedIp("192.168.1.25")
					.build();

			BindingPool pcPool = v4Manager.findBindingPool(link.getLink(), voipPoolAddr, pcRequestMsg);
			assertNull("Non-matching client must NOT be allowed to find/use the filtered pool", pcPool);

			// Matching VoIP client requesting filtered VoIP IP
			DhcpV4Message voipRequestMsg = DhcpV4MessageBuilder.request()
					.withMac("00:11:22:33:44:55")
					.withVendorClass("Cisco-IP-Phone")
					.withRequestedIp("192.168.1.25")
					.build();

			BindingPool voipPool = v4Manager.findBindingPool(link.getLink(), voipPoolAddr, voipRequestMsg);
			assertNotNull("Matching VoIP client must find the filtered pool", voipPool);
			assertEquals("VoipPoolFilter", voipPool.getLinkFilter().getName());
		} finally {
			v4Manager.close();
		}
	}

	@Test
	public void testV4FilterPoolExhaustionFallbackAndIsolation() throws Exception {
		// Small filtered pool with only 2 addresses: 192.168.1.10 to 192.168.1.11
		String xml = "<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
				"  <links>\n" +
				"    <linkList>\n" +
				"      <name>OfficeLink</name>\n" +
				"      <address>192.168.1.0/24</address>\n" +
				"      <v4AddrPools>\n" +
				"        <poolList>\n" +
				"          <range>192.168.1.100-192.168.1.199</range>\n" +
				"        </poolList>\n" +
				"      </v4AddrPools>\n" +
				"      <linkFilters>\n" +
				"        <linkFilterList>\n" +
				"          <name>SmallVoipFilter</name>\n" +
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
				"              <range>192.168.1.10-192.168.1.11</range>\n" +
				"            </poolList>\n" +
				"          </v4AddrPools>\n" +
				"        </linkFilterList>\n" +
				"      </linkFilters>\n" +
				"    </linkList>\n" +
				"  </links>\n" +
				"</dhc:dhcpServerConfig>";

		context.initXml(xml);
		DhcpLink link = DhcpServerConfiguration.getInstance().getLinkMap().values().iterator().next();
		assertNotNull(link);

		V4AddrBindingManagerImpl v4Manager = new V4AddrBindingManagerImpl();
		try {
			v4Manager.setIaMgr(mockIaMgr);
			v4Manager.init();

			// Allocate phone 1
			DhcpV4Message voip1 = DhcpV4MessageBuilder.discover().withVendorClass("Cisco-IP-Phone").build();
			InetAddress ip1 = v4Manager.getNextFreeAddress(link, voip1);
			assertNotNull(ip1);
			DhcpAssert.assertIpInRange(ip1, "192.168.1.10", "192.168.1.11");

			// Allocate phone 2
			DhcpV4Message voip2 = DhcpV4MessageBuilder.discover().withVendorClass("Cisco-IP-Phone").build();
			InetAddress ip2 = v4Manager.getNextFreeAddress(link, voip2);
			assertNotNull(ip2);
			DhcpAssert.assertIpInRange(ip2, "192.168.1.10", "192.168.1.11");
			assertTrue(!ip1.equals(ip2));

			// Allocate phone 3 (filter pool exhausted -> falls through to default link
			// pool)
			DhcpV4Message voip3 = DhcpV4MessageBuilder.discover().withVendorClass("Cisco-IP-Phone").build();
			InetAddress ip3 = v4Manager.getNextFreeAddress(link, voip3);
			assertNotNull("Matching client falls through to link default pool when filter pool is exhausted", ip3);
			DhcpAssert.assertIpInRange(ip3, "192.168.1.100", "192.168.1.199");

			// Allocate standard PC (ensuring standard allocation is unaffected)
			DhcpV4Message pc = DhcpV4MessageBuilder.discover().withVendorClass("Standard-PC").build();
			InetAddress pcIp = v4Manager.getNextFreeAddress(link, pc);
			assertNotNull(pcIp);
			DhcpAssert.assertIpInRange(pcIp, "192.168.1.100", "192.168.1.199");
			assertTrue(!pcIp.equals(ip3));
		} finally {
			v4Manager.close();
		}
	}

	@Test
	public void testV6NaAddrDynamicAllocationAndIsolation() throws Exception {
		String xml = "<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
				"  <links>\n" +
				"    <linkList>\n" +
				"      <name>V6OfficeLink</name>\n" +
				"      <address>2001:db8:1::/64</address>\n" +
				"      <v6NaAddrPools>\n" +
				"        <poolList>\n" +
				"          <range>2001:db8:1::100-2001:db8:1::199</range>\n" +
				"        </poolList>\n" +
				"      </v6NaAddrPools>\n" +
				"      <linkFilters>\n" +
				"        <linkFilterList>\n" +
				"          <name>VoipV6Filter</name>\n" +
				"          <filterExpressions>\n" +
				"            <filterExpressionList>\n" +
				"              <clientClassExpression operator=\"equals\">\n" +
				"                <v6VendorClassOption>\n" +
				"                  <opaqueDataList><asciiValue>Cisco</asciiValue></opaqueDataList>\n" +
				"                  <enterpriseNumber>9</enterpriseNumber>\n" +
				"                </v6VendorClassOption>\n" +
				"              </clientClassExpression>\n" +
				"            </filterExpressionList>\n" +
				"          </filterExpressions>\n" +
				"          <v6NaAddrPools>\n" +
				"            <poolList>\n" +
				"              <range>2001:db8:1::10-2001:db8:1::50</range>\n" +
				"            </poolList>\n" +
				"          </v6NaAddrPools>\n" +
				"        </linkFilterList>\n" +
				"      </linkFilters>\n" +
				"    </linkList>\n" +
				"  </links>\n" +
				"</dhc:dhcpServerConfig>";

		context.initXml(xml);
		DhcpLink link = DhcpServerConfiguration.getInstance().getLinkMap().values().iterator().next();
		assertNotNull(link);

		V6NaAddrBindingManagerImpl v6Manager = new V6NaAddrBindingManagerImpl();
		try {
			v6Manager.setIaMgr(mockIaMgr);
			v6Manager.init();

			// Matching client (Vendor Class Cisco)
			DhcpV6Message voipMsg = DhcpV6MessageBuilder.solicit()
					.withDuid("00010001aabbccdd001122334455")
					.withVendorClass(9, "Cisco")
					.build();

			InetAddress voipIp = v6Manager.getNextFreeAddress(link, voipMsg);
			assertNotNull("Must allocate IPv6 address for matching client", voipIp);
			DhcpAssert.assertIpInRange(voipIp, "2001:db8:1::10", "2001:db8:1::50");

			// Non-matching client
			DhcpV6Message pcMsg = DhcpV6MessageBuilder.solicit()
					.withDuid("00010001aabbccdd009988776655")
					.build();

			InetAddress pcIp = v6Manager.getNextFreeAddress(link, pcMsg);
			assertNotNull("Must allocate IPv6 address for non-matching client", pcIp);
			DhcpAssert.assertIpInRange(pcIp, "2001:db8:1::100", "2001:db8:1::199");

			// Strict pool lookup check
			InetAddress voipPoolAddr = InetAddress.getByName("2001:db8:1::20");
			assertNull("Non-matching client must not access filtered pool",
					v6Manager.findBindingPool(link.getLink(), voipPoolAddr, pcMsg));
			assertNotNull("Matching client must access filtered pool",
					v6Manager.findBindingPool(link.getLink(), voipPoolAddr, voipMsg));
		} finally {
			v6Manager.close();
		}
	}

	@Test
	public void testV6PrefixDelegationPoolIsolation() throws Exception {
		String xml = "<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
				"  <links>\n" +
				"    <linkList>\n" +
				"      <name>V6PdLink</name>\n" +
				"      <address>2001:db8:10::/48</address>\n" +
				"      <v6PrefixPools>\n" +
				"        <poolList>\n" +
				"          <range>2001:db8:10:1000::/52</range>\n" +
				"          <prefixLength>64</prefixLength>\n" +
				"        </poolList>\n" +
				"      </v6PrefixPools>\n" +
				"      <linkFilters>\n" +
				"        <linkFilterList>\n" +
				"          <name>EnterpriseRouterFilter</name>\n" +
				"          <filterExpressions>\n" +
				"            <filterExpressionList>\n" +
				"              <clientClassExpression operator=\"equals\">\n" +
				"                <v6UserClassOption>\n" +
				"                  <opaqueDataList><asciiValue>Enterprise</asciiValue></opaqueDataList>\n" +
				"                </v6UserClassOption>\n" +
				"              </clientClassExpression>\n" +
				"            </filterExpressionList>\n" +
				"          </filterExpressions>\n" +
				"          <v6PrefixPools>\n" +
				"            <poolList>\n" +
				"              <range>2001:db8:10:2000::/52</range>\n" +
				"              <prefixLength>56</prefixLength>\n" +
				"            </poolList>\n" +
				"          </v6PrefixPools>\n" +
				"        </linkFilterList>\n" +
				"      </linkFilters>\n" +
				"    </linkList>\n" +
				"  </links>\n" +
				"</dhc:dhcpServerConfig>";

		context.initXml(xml);
		DhcpLink link = DhcpServerConfiguration.getInstance().getLinkMap().values().iterator().next();
		assertNotNull(link);

		V6PrefixBindingManagerImpl v6PdManager = new V6PrefixBindingManagerImpl();
		try {
			v6PdManager.setIaMgr(mockIaMgr);
			v6PdManager.init();

			List<? extends BindingPool> pools = v6PdManager.getBindingPoolMap().get("2001:db8:10::/48");
			assertNotNull(pools);
			assertEquals(2, pools.size());

			// Matching enterprise router
			DhcpV6Message routerMsg = DhcpV6MessageBuilder.solicit()
					.withDuid("00010001aabbccdd001122334455")
					.withUserClass("Enterprise")
					.build();

			InetAddress routerPrefix = v6PdManager.getNextFreeAddress(link, routerMsg);
			assertNotNull("Must allocate prefix for enterprise router", routerPrefix);
			// Enterprise pool range is 2001:db8:10:2000:: through
			// 2001:db8:10:2fff:ffff:ffff:ffff:ffff
			DhcpAssert.assertIpInRange(routerPrefix, "2001:db8:10:2000::", "2001:db8:10:2fff:ffff:ffff:ffff:ffff");

			// Non-matching client
			DhcpV6Message standardMsg = DhcpV6MessageBuilder.solicit()
					.withDuid("00010001aabbccdd009988776655")
					.build();

			InetAddress standardPrefix = v6PdManager.getNextFreeAddress(link, standardMsg);
			assertNotNull("Must allocate prefix for standard client", standardPrefix);
			// Standard pool range is 2001:db8:10:1000:: through
			// 2001:db8:10:1fff:ffff:ffff:ffff:ffff
			DhcpAssert.assertIpInRange(standardPrefix, "2001:db8:10:1000::", "2001:db8:10:1fff:ffff:ffff:ffff:ffff");
		} finally {
			v6PdManager.close();
		}
	}
}
