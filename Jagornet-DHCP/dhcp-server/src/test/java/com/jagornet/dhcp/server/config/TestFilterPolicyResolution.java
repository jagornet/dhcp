/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.config.xml.ClientClassExpression;
import com.jagornet.dhcp.server.config.xml.Filter;
import com.jagornet.dhcp.server.config.xml.FilterExpression;
import com.jagornet.dhcp.server.config.xml.FilterExpressionsType;
import com.jagornet.dhcp.server.config.xml.FiltersType;
import com.jagornet.dhcp.server.config.xml.Link;
import com.jagornet.dhcp.server.config.xml.LinkFilter;
import com.jagornet.dhcp.server.config.xml.LinkFiltersType;
import com.jagornet.dhcp.server.config.xml.OpaqueData;
import com.jagornet.dhcp.server.config.xml.Operator;
import com.jagornet.dhcp.server.config.xml.PoliciesType;
import com.jagornet.dhcp.server.config.xml.Policy;
import com.jagornet.dhcp.server.config.xml.V4VendorClassOption;
import com.jagornet.dhcp.server.testutil.DhcpConfigTestContext;
import com.jagornet.dhcp.server.testutil.DhcpV4MessageBuilder;

/**
 * Unit tests verifying the 4-tier policy resolution hierarchy:
 * Pool Filter -> Pool -> Link Filter -> Link -> Global
 * in {@link DhcpServerPolicies}.
 * 
 * @author A. Gregory Rabil
 */
public class TestFilterPolicyResolution {

	private DhcpConfigTestContext context;

	@Before
	public void setUp() {
		context = new DhcpConfigTestContext();
	}

	@After
	public void tearDown() {
		if (context != null) {
			context.close();
		}
	}

	@Test
	public void testGlobalPolicyFallback() {
		// No link or filter policies defined -> falls back to global default
		Link link = new Link();
		DhcpV4Message msg = DhcpV4MessageBuilder.discover().build();

		assertEquals("false", DhcpServerPolicies.effectivePolicy(msg, link, Property.DHCP_SEND_REQUESTED_OPTIONS_ONLY));
		assertFalse(DhcpServerPolicies.effectivePolicyAsBoolean(msg, link, Property.DHCP_SEND_REQUESTED_OPTIONS_ONLY));
	}

	@Test
	public void testLinkPolicyOverridesGlobal() {
		Link link = new Link();
		PoliciesType linkPolicies = new PoliciesType();
		Policy pol = new Policy();
		pol.setName(Property.DHCP_SEND_REQUESTED_OPTIONS_ONLY.key());
		pol.setValue("true");
		linkPolicies.getPolicyList().add(pol);
		link.setPolicies(linkPolicies);

		DhcpV4Message msg = DhcpV4MessageBuilder.discover().build();

		assertEquals("true", DhcpServerPolicies.effectivePolicy(msg, link, Property.DHCP_SEND_REQUESTED_OPTIONS_ONLY));
		assertTrue(DhcpServerPolicies.effectivePolicyAsBoolean(msg, link, Property.DHCP_SEND_REQUESTED_OPTIONS_ONLY));
	}

	@Test
	public void testLinkFilterOverridesLinkAndGlobal() {
		Link link = new Link();

		// Link policy sets DHCP_SEND_REQUESTED_OPTIONS_ONLY = "false"
		PoliciesType linkPolicies = new PoliciesType();
		Policy linkPol = new Policy();
		linkPol.setName(Property.DHCP_SEND_REQUESTED_OPTIONS_ONLY.key());
		linkPol.setValue("false");
		linkPolicies.getPolicyList().add(linkPol);
		link.setPolicies(linkPolicies);

		// LinkFilter sets DHCP_SEND_REQUESTED_OPTIONS_ONLY = "true" for Cisco phones
		LinkFilter linkFilter = new LinkFilter();
		linkFilter.setName("Cisco Filter");
		linkFilter.setFilterExpressions(createV4VendorClassExpressions("Cisco", Operator.STARTS_WITH));

		PoliciesType filterPolicies = new PoliciesType();
		Policy filterPol = new Policy();
		filterPol.setName(Property.DHCP_SEND_REQUESTED_OPTIONS_ONLY.key());
		filterPol.setValue("true");
		filterPolicies.getPolicyList().add(filterPol);
		linkFilter.setPolicies(filterPolicies);

		LinkFiltersType linkFiltersType = new LinkFiltersType();
		linkFiltersType.getLinkFilterList().add(linkFilter);
		link.setLinkFilters(linkFiltersType);

		// Matching client receives filter policy ("true")
		DhcpV4Message ciscoClient = DhcpV4MessageBuilder.discover()
				.withVendorClass("Cisco-Phone")
				.build();
		assertEquals("true", DhcpServerPolicies.effectivePolicy(ciscoClient, link, Property.DHCP_SEND_REQUESTED_OPTIONS_ONLY));

		// Non-matching client falls back to link policy ("false")
		DhcpV4Message otherClient = DhcpV4MessageBuilder.discover()
				.withVendorClass("Generic-PC")
				.build();
		assertEquals("false", DhcpServerPolicies.effectivePolicy(otherClient, link, Property.DHCP_SEND_REQUESTED_OPTIONS_ONLY));
	}

	@Test
	public void testPoolAndPoolFilterHierarchy() {
		Link link = new Link();

		// Set default link lease time = 3600
		PoliciesType linkPolicies = new PoliciesType();
		Policy linkLeasePol = new Policy();
		linkLeasePol.setName(Property.V4_DEFAULT_LEASETIME.key());
		linkLeasePol.setValue("3600");
		linkPolicies.getPolicyList().add(linkLeasePol);
		link.setPolicies(linkPolicies);

		// Create a mock pool config object
		MockPoolConfig pool = new MockPoolConfig();

		// Pool policy sets lease time = 7200
		PoliciesType poolPolicies = new PoliciesType();
		Policy poolLeasePol = new Policy();
		poolLeasePol.setName(Property.V4_DEFAULT_LEASETIME.key());
		poolLeasePol.setValue("7200");
		poolPolicies.getPolicyList().add(poolLeasePol);
		pool.setPolicies(poolPolicies);

		// Pool filter sets lease time = 14400 for VIP clients
		Filter vipFilter = new Filter();
		vipFilter.setName("VIP Filter");
		vipFilter.setFilterExpressions(createV4VendorClassExpressions("VIP-Client", Operator.EQUALS));
		PoliciesType vipPolicies = new PoliciesType();
		Policy vipPol = new Policy();
		vipPol.setName(Property.V4_DEFAULT_LEASETIME.key());
		vipPol.setValue("14400");
		vipPolicies.getPolicyList().add(vipPol);
		vipFilter.setPolicies(vipPolicies);

		FiltersType poolFilters = new FiltersType();
		poolFilters.getFilterList().add(vipFilter);
		pool.setFilters(poolFilters);

		DhcpV4Message vipMsg = DhcpV4MessageBuilder.discover().withVendorClass("VIP-Client").build();
		DhcpV4Message regMsg = DhcpV4MessageBuilder.discover().withVendorClass("Standard-Client").build();

		// VIP client matches Pool Filter -> 14400
		assertEquals(14400, DhcpServerPolicies.effectivePolicyAsInt(vipMsg, pool, link, Property.V4_DEFAULT_LEASETIME));

		// Regular client does not match Pool Filter -> gets Pool policy (7200)
		assertEquals(7200, DhcpServerPolicies.effectivePolicyAsInt(regMsg, pool, link, Property.V4_DEFAULT_LEASETIME));

		// Client with null pool falls back to Link policy (3600)
		assertEquals(3600, DhcpServerPolicies.effectivePolicyAsInt(regMsg, null, link, Property.V4_DEFAULT_LEASETIME));
	}

	@Test
	public void testMultipleLinkFilters_LastMatchWins() {
		Link link = new Link();
		LinkFiltersType linkFiltersType = new LinkFiltersType();

		// Filter 1: matches "Cisco" -> sets lease time 1000
		LinkFilter lf1 = new LinkFilter();
		lf1.setName("Cisco Generic");
		lf1.setFilterExpressions(createV4VendorClassExpressions("Cisco", Operator.STARTS_WITH));
		PoliciesType p1 = new PoliciesType();
		Policy pol1 = new Policy();
		pol1.setName(Property.V4_DEFAULT_LEASETIME.key());
		pol1.setValue("1000");
		p1.getPolicyList().add(pol1);
		lf1.setPolicies(p1);
		linkFiltersType.getLinkFilterList().add(lf1);

		// Filter 2: also matches "Cisco-Specific" -> sets lease time 2000
		LinkFilter lf2 = new LinkFilter();
		lf2.setName("Cisco Specific");
		lf2.setFilterExpressions(createV4VendorClassExpressions("Cisco-Specific", Operator.STARTS_WITH));
		PoliciesType p2 = new PoliciesType();
		Policy pol2 = new Policy();
		pol2.setName(Property.V4_DEFAULT_LEASETIME.key());
		pol2.setValue("2000");
		p2.getPolicyList().add(pol2);
		lf2.setPolicies(p2);
		linkFiltersType.getLinkFilterList().add(lf2);

		link.setLinkFilters(linkFiltersType);

		DhcpV4Message msg = DhcpV4MessageBuilder.discover().withVendorClass("Cisco-Specific-Device").build();

		// Documented behavior: last matching filter on the link wins
		assertEquals(2000, DhcpServerPolicies.effectivePolicyAsInt(msg, link, Property.V4_DEFAULT_LEASETIME));
	}

	// =========================================================================
	// Helpers
	// =========================================================================

	private FilterExpressionsType createV4VendorClassExpressions(String ascii, Operator op) {
		FilterExpressionsType exprsType = new FilterExpressionsType();
		FilterExpression expr = new FilterExpression();
		ClientClassExpression ccexpr = new ClientClassExpression();
		ccexpr.setOperator(op);

		V4VendorClassOption v4vc = new V4VendorClassOption();
		OpaqueData od = new OpaqueData();
		od.setAsciiValue(ascii);
		v4vc.setOpaqueData(od);

		ccexpr.setV4VendorClassOption(v4vc);
		expr.setClientClassExpression(ccexpr);
		exprsType.getFilterExpressionList().add(expr);
		return exprsType;
	}

	private static class MockPoolConfig implements DhcpConfigObject {
		private FiltersType filters;
		private PoliciesType policies;

		@Override public FiltersType getFilters() { return filters; }
		public void setFilters(FiltersType filters) { this.filters = filters; }

		@Override public PoliciesType getPolicies() { return policies; }
		public void setPolicies(PoliciesType policies) { this.policies = policies; }

		@Override public long getPreferredLifetime() { return 0; }
		@Override public long getValidLifetime() { return 0; }
		@Override public long getPreferredLifetimeMs() { return 0; }
		@Override public long getValidLifetimeMs() { return 0; }
	}
}
