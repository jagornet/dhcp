/*
 * Copyright 2026 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */
package com.jagornet.dhcp.server.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.option.v4.DhcpV4DomainServersOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4HostnameOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4LeaseTimeOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4RequestedIpAddressOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6DomainSearchListOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6ElapsedTimeOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6PreferenceOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6RapidCommitOption;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.config.xml.ClientClassExpression;
import com.jagornet.dhcp.server.config.xml.DomainNameListOptionType;
import com.jagornet.dhcp.server.config.xml.DomainNameOptionType;
import com.jagornet.dhcp.server.config.xml.Filter;
import com.jagornet.dhcp.server.config.xml.FilterExpression;
import com.jagornet.dhcp.server.config.xml.FilterExpressionsType;
import com.jagornet.dhcp.server.config.xml.IpAddressListOptionType;
import com.jagornet.dhcp.server.config.xml.IpAddressOptionType;
import com.jagornet.dhcp.server.config.xml.OpaqueData;
import com.jagornet.dhcp.server.config.xml.Operator;
import com.jagornet.dhcp.server.config.xml.OptionExpression;
import com.jagornet.dhcp.server.config.xml.StringOptionType;
import com.jagornet.dhcp.server.config.xml.UnsignedByteOptionType;
import com.jagornet.dhcp.server.config.xml.UnsignedIntOptionType;
import com.jagornet.dhcp.server.config.xml.UnsignedShortOptionType;
import com.jagornet.dhcp.server.config.xml.V4VendorClassOption;
import com.jagornet.dhcp.server.config.xml.V6UserClassOption;
import com.jagornet.dhcp.server.config.xml.V6VendorClassOption;
import com.jagornet.dhcp.server.testutil.DhcpV4MessageBuilder;
import com.jagornet.dhcp.server.testutil.DhcpV6MessageBuilder;

/**
 * Unit tests for {@link DhcpServerConfiguration#msgMatchesFilter} and
 * associated
 * expression evaluation logic (client class matching, generic option matching,
 * operators, compound AND expressions, and boundary conditions).
 * 
 * @author A. Gregory Rabil
 */
public class TestDhcpFilterEngine {

	// =========================================================================
	// DHCPv4 Client Class (Vendor Class) Tests
	// =========================================================================

	@Test
	public void testV4VendorClass_Equals_Match() {
		DhcpV4Message msg = DhcpV4MessageBuilder.discover()
				.withVendorClass("Cisco-IP-Phone")
				.build();

		Filter filter = createV4VendorClassFilter("Cisco-IP-Phone", Operator.EQUALS);
		assertTrue(DhcpServerConfiguration.msgMatchesFilter(msg, filter));
	}

	@Test
	public void testV4VendorClass_Equals_Mismatch() {
		DhcpV4Message msg = DhcpV4MessageBuilder.discover()
				.withVendorClass("Polycom-SoundPoint")
				.build();

		Filter filter = createV4VendorClassFilter("Cisco-IP-Phone", Operator.EQUALS);
		assertFalse(DhcpServerConfiguration.msgMatchesFilter(msg, filter));
	}

	@Test
	public void testV4VendorClass_StartsWith() {
		DhcpV4Message msg = DhcpV4MessageBuilder.discover()
				.withVendorClass("Cisco-IP-Phone-7960")
				.build();

		Filter filter = createV4VendorClassFilter("Cisco", Operator.STARTS_WITH);
		assertTrue(DhcpServerConfiguration.msgMatchesFilter(msg, filter));

		Filter nonMatchingFilter = createV4VendorClassFilter("Polycom", Operator.STARTS_WITH);
		assertFalse(DhcpServerConfiguration.msgMatchesFilter(msg, nonMatchingFilter));
	}

	@Test
	public void testV4VendorClass_EndsWith() {
		DhcpV4Message msg = DhcpV4MessageBuilder.discover()
				.withVendorClass("Cisco-IP-Phone-7960")
				.build();

		Filter filter = createV4VendorClassFilter("7960", Operator.ENDS_WITH);
		assertTrue(DhcpServerConfiguration.msgMatchesFilter(msg, filter));

		Filter nonMatchingFilter = createV4VendorClassFilter("7940", Operator.ENDS_WITH);
		assertFalse(DhcpServerConfiguration.msgMatchesFilter(msg, nonMatchingFilter));
	}

	@Test
	public void testV4VendorClass_Contains() {
		DhcpV4Message msg = DhcpV4MessageBuilder.discover()
				.withVendorClass("Cisco-IP-Phone-7960")
				.build();

		Filter filter = createV4VendorClassFilter("IP-Phone", Operator.CONTAINS);
		assertTrue(DhcpServerConfiguration.msgMatchesFilter(msg, filter));

		Filter nonMatchingFilter = createV4VendorClassFilter("Printer", Operator.CONTAINS);
		assertFalse(DhcpServerConfiguration.msgMatchesFilter(msg, nonMatchingFilter));
	}

	@Test
	public void testV4VendorClass_RegExp() {
		DhcpV4Message msg = DhcpV4MessageBuilder.discover()
				.withVendorClass("Cisco-IP-Phone-7960")
				.build();

		Filter filter = createV4VendorClassFilter("^Cisco.*[0-9]+$", Operator.REG_EXP);
		assertTrue(DhcpServerConfiguration.msgMatchesFilter(msg, filter));

		Filter nonMatchingFilter = createV4VendorClassFilter("^Polycom.*", Operator.REG_EXP);
		assertFalse(DhcpServerConfiguration.msgMatchesFilter(msg, nonMatchingFilter));
	}

	@Test
	public void testV4VendorClass_HexBytes() {
		DhcpV4Message msg = DhcpV4MessageBuilder.discover()
				.withVendorClassHex("AABBCCDDEEFF")
				.build();

		Filter filter = createFilterWithV4VendorClassHex("AABBCCDDEEFF", Operator.EQUALS);
		assertTrue(DhcpServerConfiguration.msgMatchesFilter(msg, filter));

		Filter prefixFilter = createFilterWithV4VendorClassHex("AABBCC", Operator.STARTS_WITH);
		assertTrue(DhcpServerConfiguration.msgMatchesFilter(msg, prefixFilter));

		Filter nonMatchingFilter = createFilterWithV4VendorClassHex("112233", Operator.EQUALS);
		assertFalse(DhcpServerConfiguration.msgMatchesFilter(msg, nonMatchingFilter));
	}

	// =========================================================================
	// DHCPv6 Client Class (User Class & Vendor Class) Tests
	// =========================================================================

	@Test
	public void testV6UserClass_Equals() {
		DhcpV6Message msg = DhcpV6MessageBuilder.solicit()
				.withUserClass("Engineering")
				.build();

		Filter filter = createV6UserClassFilter("Engineering", Operator.EQUALS);
		assertTrue(DhcpServerConfiguration.msgMatchesFilter(msg, filter));

		Filter mismatchFilter = createV6UserClassFilter("Marketing", Operator.EQUALS);
		assertFalse(DhcpServerConfiguration.msgMatchesFilter(msg, mismatchFilter));
	}

	@Test
	public void testV6VendorClass_Equals() {
		DhcpV6Message msg = DhcpV6MessageBuilder.solicit()
				.withVendorClass(9, "EnterpriseSwitch")
				.build();

		Filter filter = createV6VendorClassFilter("EnterpriseSwitch", Operator.EQUALS);
		assertTrue(DhcpServerConfiguration.msgMatchesFilter(msg, filter));

		Filter mismatchFilter = createV6VendorClassFilter("UnknownVendor", Operator.EQUALS);
		assertFalse(DhcpServerConfiguration.msgMatchesFilter(msg, mismatchFilter));
	}

	// =========================================================================
	// OptionExpression: String & Domain Options
	// =========================================================================

	@Test
	public void testStringOption_Hostname() {
		DhcpV4HostnameOption hostnameOpt = new DhcpV4HostnameOption();
		hostnameOpt.setString("workstation-42.corp.local");

		DhcpV4Message msg = DhcpV4MessageBuilder.discover()
				.withOption(hostnameOpt)
				.build();

		OptionExpression expr = createOptionExpr(DhcpConstants.V4OPTION_HOSTNAME, true, Operator.STARTS_WITH);
		StringOptionType strType = new StringOptionType();
		strType.setString("workstation");
		expr.setStringOption(strType);

		assertTrue(DhcpServerConfiguration.msgMatchesFilter(msg, wrapExpression(expr)));

		// Non-matching prefix
		strType.setString("server");
		assertFalse(DhcpServerConfiguration.msgMatchesFilter(msg, wrapExpression(expr)));
	}

	@Test
	public void testDomainNameOption() {
		com.jagornet.dhcp.core.option.v4.DhcpV4ClientFqdnOption fqdnOpt = new com.jagornet.dhcp.core.option.v4.DhcpV4ClientFqdnOption();
		fqdnOpt.setDomainName("host.example.com.");

		DhcpV4Message msg = DhcpV4MessageBuilder.discover()
				.withOption(fqdnOpt)
				.build();

		OptionExpression expr = createOptionExpr(DhcpConstants.V4OPTION_CLIENT_FQDN, true, Operator.EQUALS);
		DomainNameOptionType domainType = new DomainNameOptionType();
		domainType.setDomainName("host.example.com.");
		expr.setDomainNameOption(domainType);

		assertTrue(DhcpServerConfiguration.msgMatchesFilter(msg, wrapExpression(expr)));

		domainType.setDomainName("other.example.com.");
		assertFalse(DhcpServerConfiguration.msgMatchesFilter(msg, wrapExpression(expr)));
	}

	@Test
	public void testDomainNameListOption_Contains() {
		DhcpV6DomainSearchListOption searchList = new DhcpV6DomainSearchListOption();
		searchList.setDomainNameList(Arrays.asList("corp.example.com", "eng.example.com"));

		DhcpV6Message msg = DhcpV6MessageBuilder.solicit()
				.withOption(searchList)
				.build();

		OptionExpression expr = createOptionExpr(DhcpConstants.V6OPTION_DOMAIN_SEARCH_LIST, false, Operator.CONTAINS);
		DomainNameListOptionType listType = new DomainNameListOptionType();
		listType.getDomainNameList().add("corp.example.com");
		expr.setDomainNameListOption(listType);

		assertTrue(DhcpServerConfiguration.msgMatchesFilter(msg, wrapExpression(expr)));

		listType.getDomainNameList().clear();
		listType.getDomainNameList().add("sales.example.com");
		assertFalse(DhcpServerConfiguration.msgMatchesFilter(msg, wrapExpression(expr)));
	}

	// =========================================================================
	// OptionExpression: IP Address Options
	// =========================================================================

	@Test
	public void testIpAddressOption_RequestedIp() {
		DhcpV4RequestedIpAddressOption reqIpOpt = new DhcpV4RequestedIpAddressOption();
		reqIpOpt.setIpAddress("192.168.1.50");

		DhcpV4Message msg = DhcpV4MessageBuilder.request()
				.withOption(reqIpOpt)
				.build();

		OptionExpression expr = createOptionExpr(DhcpConstants.V4OPTION_REQUESTED_IP, true, Operator.EQUALS);
		IpAddressOptionType ipType = new IpAddressOptionType();
		ipType.setIpAddress("192.168.1.50");
		expr.setIpAddressOption(ipType);

		assertTrue(DhcpServerConfiguration.msgMatchesFilter(msg, wrapExpression(expr)));

		ipType.setIpAddress("192.168.1.99");
		assertFalse(DhcpServerConfiguration.msgMatchesFilter(msg, wrapExpression(expr)));
	}

	@Test
	public void testIpAddressListOption_DnsServers() throws Exception {
		DhcpV4DomainServersOption dnsOpt = new DhcpV4DomainServersOption();
		dnsOpt.setIpAddressList(Arrays.asList("8.8.8.8", "8.8.4.4"));

		DhcpV4Message msg = DhcpV4MessageBuilder.discover()
				.withOption(dnsOpt)
				.build();

		OptionExpression expr = createOptionExpr(DhcpConstants.V4OPTION_DOMAIN_SERVERS, true, Operator.CONTAINS);
		IpAddressListOptionType listType = new IpAddressListOptionType();
		listType.getIpAddressList().add("8.8.8.8");
		expr.setIpAddressListOption(listType);

		assertTrue(DhcpServerConfiguration.msgMatchesFilter(msg, wrapExpression(expr)));

		listType.getIpAddressList().clear();
		listType.getIpAddressList().add("1.1.1.1");
		assertFalse(DhcpServerConfiguration.msgMatchesFilter(msg, wrapExpression(expr)));
	}

	// =========================================================================
	// OptionExpression: Numeric Comparators (Unsigned Byte, Short, Int)
	// =========================================================================

	@Test
	public void testUnsignedByte_Comparisons() {
		DhcpV6PreferenceOption prefOpt = new DhcpV6PreferenceOption();
		prefOpt.setUnsignedByte((short) 100);

		DhcpV6Message msg = DhcpV6MessageBuilder.solicit()
				.withOption(prefOpt)
				.build();

		// Equals 100
		OptionExpression eqExpr = createOptionExpr(DhcpConstants.V6OPTION_PREFERENCE, false, Operator.EQUALS);
		UnsignedByteOptionType ubyte = new UnsignedByteOptionType();
		ubyte.setUnsignedByte((short) 100);
		eqExpr.setUByteOption(ubyte);
		assertTrue(DhcpServerConfiguration.msgMatchesFilter(msg, wrapExpression(eqExpr)));

		// GreaterThan 50
		OptionExpression gtExpr = createOptionExpr(DhcpConstants.V6OPTION_PREFERENCE, false, Operator.GREATER_THAN);
		UnsignedByteOptionType ubyte50 = new UnsignedByteOptionType();
		ubyte50.setUnsignedByte((short) 50);
		gtExpr.setUByteOption(ubyte50);
		assertTrue(DhcpServerConfiguration.msgMatchesFilter(msg, wrapExpression(gtExpr)));

		// LessThan 50 -> should be false
		OptionExpression ltExpr = createOptionExpr(DhcpConstants.V6OPTION_PREFERENCE, false, Operator.LESS_THAN);
		ltExpr.setUByteOption(ubyte50);
		assertFalse(DhcpServerConfiguration.msgMatchesFilter(msg, wrapExpression(ltExpr)));
	}

	@Test
	public void testUnsignedShort_Comparisons() {
		DhcpV6ElapsedTimeOption timeOpt = new DhcpV6ElapsedTimeOption();
		timeOpt.setUnsignedShort(500);

		DhcpV6Message msg = DhcpV6MessageBuilder.solicit()
				.withOption(timeOpt)
				.build();

		OptionExpression expr = createOptionExpr(DhcpConstants.V6OPTION_ELAPSED_TIME, false,
				Operator.GREATER_THAN_OR_EQUAL);
		UnsignedShortOptionType ushort = new UnsignedShortOptionType();
		ushort.setUnsignedShort(500);
		expr.setUShortOption(ushort);

		assertTrue(DhcpServerConfiguration.msgMatchesFilter(msg, wrapExpression(expr)));

		ushort.setUnsignedShort(501);
		assertFalse(DhcpServerConfiguration.msgMatchesFilter(msg, wrapExpression(expr)));
	}

	@Test
	public void testUnsignedInt_Comparisons() {
		DhcpV4LeaseTimeOption leaseOpt = new DhcpV4LeaseTimeOption();
		leaseOpt.setUnsignedInt(86400L);

		DhcpV4Message msg = DhcpV4MessageBuilder.request()
				.withOption(leaseOpt)
				.build();

		OptionExpression expr = createOptionExpr(DhcpConstants.V4OPTION_LEASE_TIME, true, Operator.EQUALS);
		UnsignedIntOptionType uint = new UnsignedIntOptionType();
		uint.setUnsignedInt(86400L);
		expr.setUIntOption(uint);

		assertTrue(DhcpServerConfiguration.msgMatchesFilter(msg, wrapExpression(expr)));

		uint.setUnsignedInt(3600L);
		assertFalse(DhcpServerConfiguration.msgMatchesFilter(msg, wrapExpression(expr)));
	}

	// =========================================================================
	// OptionExpression: BaseEmptyOption (e.g. Rapid Commit)
	// =========================================================================

	@Test
	public void testEmptyOption_RapidCommit() {
		DhcpV6Message msg = DhcpV6MessageBuilder.solicit()
				.withOption(new DhcpV6RapidCommitOption())
				.build();

		OptionExpression expr = createOptionExpr(DhcpConstants.V6OPTION_RAPID_COMMIT, false, Operator.EQUALS);
		assertTrue(DhcpServerConfiguration.msgMatchesFilter(msg, wrapExpression(expr)));
	}

	// =========================================================================
	// Compound Expressions & AND Logic
	// =========================================================================

	@Test
	public void testCompoundExpressions_AllMatch_ReturnsTrue() {
		DhcpV4HostnameOption hostnameOpt = new DhcpV4HostnameOption();
		hostnameOpt.setString("finance-laptop-01");

		DhcpV4Message msg = DhcpV4MessageBuilder.discover()
				.withVendorClass("Corporate-Device")
				.withOption(hostnameOpt)
				.build();

		Filter filter = new Filter();
		filter.setName("Finance Laptops");
		FilterExpressionsType exprsType = new FilterExpressionsType();

		// Expr 1: VendorClass equals "Corporate-Device"
		FilterExpression expr1 = new FilterExpression();
		ClientClassExpression ccexpr = new ClientClassExpression();
		ccexpr.setOperator(Operator.EQUALS);
		V4VendorClassOption v4vc = new V4VendorClassOption();
		OpaqueData od = new OpaqueData();
		od.setAsciiValue("Corporate-Device");
		v4vc.setOpaqueData(od);
		ccexpr.setV4VendorClassOption(v4vc);
		expr1.setClientClassExpression(ccexpr);
		exprsType.getFilterExpressionList().add(expr1);

		// Expr 2: Hostname starts with "finance-"
		FilterExpression expr2 = new FilterExpression();
		OptionExpression optExpr = createOptionExpr(DhcpConstants.V4OPTION_HOSTNAME, true, Operator.STARTS_WITH);
		StringOptionType strType = new StringOptionType();
		strType.setString("finance-");
		optExpr.setStringOption(strType);
		expr2.setOptionExpression(optExpr);
		exprsType.getFilterExpressionList().add(expr2);

		filter.setFilterExpressions(exprsType);

		assertTrue("Both expressions match -> filter should match",
				DhcpServerConfiguration.msgMatchesFilter(msg, filter));
	}

	@Test
	public void testCompoundExpressions_OneMismatch_ReturnsFalse() {
		DhcpV4HostnameOption hostnameOpt = new DhcpV4HostnameOption();
		hostnameOpt.setString("sales-laptop-01"); // does NOT match "finance-"

		DhcpV4Message msg = DhcpV4MessageBuilder.discover()
				.withVendorClass("Corporate-Device")
				.withOption(hostnameOpt)
				.build();

		Filter filter = new Filter();
		FilterExpressionsType exprsType = new FilterExpressionsType();

		// Expr 1: VendorClass equals "Corporate-Device" (matches)
		FilterExpression expr1 = new FilterExpression();
		ClientClassExpression ccexpr = new ClientClassExpression();
		ccexpr.setOperator(Operator.EQUALS);
		V4VendorClassOption v4vc = new V4VendorClassOption();
		OpaqueData od = new OpaqueData();
		od.setAsciiValue("Corporate-Device");
		v4vc.setOpaqueData(od);
		ccexpr.setV4VendorClassOption(v4vc);
		expr1.setClientClassExpression(ccexpr);
		exprsType.getFilterExpressionList().add(expr1);

		// Expr 2: Hostname starts with "finance-" (fails)
		FilterExpression expr2 = new FilterExpression();
		OptionExpression optExpr = createOptionExpr(DhcpConstants.V4OPTION_HOSTNAME, true, Operator.STARTS_WITH);
		StringOptionType strType = new StringOptionType();
		strType.setString("finance-");
		optExpr.setStringOption(strType);
		expr2.setOptionExpression(optExpr);
		exprsType.getFilterExpressionList().add(expr2);

		filter.setFilterExpressions(exprsType);

		assertFalse("One expression fails in AND group -> filter must return false",
				DhcpServerConfiguration.msgMatchesFilter(msg, filter));
	}

	// =========================================================================
	// Negative & Edge Cases
	// =========================================================================

	@Test
	public void testMissingOptionInMessage_ReturnsFalse() {
		DhcpV4Message msg = DhcpV4MessageBuilder.discover().build();

		// Filter requires Hostname (code 12), but packet does not contain it
		OptionExpression expr = createOptionExpr(DhcpConstants.V4OPTION_HOSTNAME, true, Operator.EQUALS);
		StringOptionType str = new StringOptionType();
		str.setString("myhost");
		expr.setStringOption(str);

		assertFalse("Missing option must evaluate to false without throwing exception",
				DhcpServerConfiguration.msgMatchesFilter(msg, wrapExpression(expr)));
	}

	@Test
	public void testV4OptionTestedAgainstV6Message_ReturnsFalse() {
		DhcpV6Message v6Msg = DhcpV6MessageBuilder.solicit().build();

		// Expression specifies v4="true"
		OptionExpression expr = createOptionExpr(DhcpConstants.V4OPTION_HOSTNAME, true, Operator.EQUALS);
		StringOptionType str = new StringOptionType();
		str.setString("myhost");
		expr.setStringOption(str);

		assertFalse(DhcpServerConfiguration.msgMatchesFilter(v6Msg, wrapExpression(expr)));
	}

	@Test
	public void testEmptyFilterExpressions_ReturnsTrueByDefault() {
		DhcpV4Message msg = DhcpV4MessageBuilder.discover().build();
		Filter filter = new Filter();
		filter.setName("No-Expressions-Filter");

		assertTrue("Filter with null expressions should match by default",
				DhcpServerConfiguration.msgMatchesFilter(msg, filter));

		filter.setFilterExpressions(new FilterExpressionsType());
		assertTrue("Filter with empty expressions list should match by default",
				DhcpServerConfiguration.msgMatchesFilter(msg, filter));
	}

	// =========================================================================
	// Helper Methods
	// =========================================================================

	private Filter createV4VendorClassFilter(String asciiValue, Operator op) {
		Filter filter = new Filter();
		filter.setName("V4 Vendor Class Filter");
		FilterExpressionsType exprsType = new FilterExpressionsType();
		FilterExpression expr = new FilterExpression();
		ClientClassExpression ccexpr = new ClientClassExpression();
		ccexpr.setOperator(op);

		V4VendorClassOption v4vc = new V4VendorClassOption();
		OpaqueData od = new OpaqueData();
		od.setAsciiValue(asciiValue);
		v4vc.setOpaqueData(od);

		ccexpr.setV4VendorClassOption(v4vc);
		expr.setClientClassExpression(ccexpr);
		exprsType.getFilterExpressionList().add(expr);
		filter.setFilterExpressions(exprsType);
		return filter;
	}

	private Filter createFilterWithV4VendorClassHex(String hexValue, Operator op) {
		Filter filter = new Filter();
		filter.setName("V4 Vendor Class Hex Filter");
		FilterExpressionsType exprsType = new FilterExpressionsType();
		FilterExpression expr = new FilterExpression();
		ClientClassExpression ccexpr = new ClientClassExpression();
		ccexpr.setOperator(op);

		V4VendorClassOption v4vc = new V4VendorClassOption();
		OpaqueData od = new OpaqueData();
		od.setHexValue(Util.fromHexString(hexValue));
		v4vc.setOpaqueData(od);

		ccexpr.setV4VendorClassOption(v4vc);
		expr.setClientClassExpression(ccexpr);
		exprsType.getFilterExpressionList().add(expr);
		filter.setFilterExpressions(exprsType);
		return filter;
	}

	private Filter createV6UserClassFilter(String asciiValue, Operator op) {
		Filter filter = new Filter();
		filter.setName("V6 User Class Filter");
		FilterExpressionsType exprsType = new FilterExpressionsType();
		FilterExpression expr = new FilterExpression();
		ClientClassExpression ccexpr = new ClientClassExpression();
		ccexpr.setOperator(op);

		V6UserClassOption v6uc = new V6UserClassOption();
		OpaqueData od = new OpaqueData();
		od.setAsciiValue(asciiValue);
		v6uc.getOpaqueDataList().add(od);

		ccexpr.setV6UserClassOption(v6uc);
		expr.setClientClassExpression(ccexpr);
		exprsType.getFilterExpressionList().add(expr);
		filter.setFilterExpressions(exprsType);
		return filter;
	}

	private Filter createV6VendorClassFilter(String asciiValue, Operator op) {
		Filter filter = new Filter();
		filter.setName("V6 Vendor Class Filter");
		FilterExpressionsType exprsType = new FilterExpressionsType();
		FilterExpression expr = new FilterExpression();
		ClientClassExpression ccexpr = new ClientClassExpression();
		ccexpr.setOperator(op);

		V6VendorClassOption v6vc = new V6VendorClassOption();
		OpaqueData od = new OpaqueData();
		od.setAsciiValue(asciiValue);
		v6vc.getOpaqueDataList().add(od);

		ccexpr.setV6VendorClassOption(v6vc);
		expr.setClientClassExpression(ccexpr);
		exprsType.getFilterExpressionList().add(expr);
		filter.setFilterExpressions(exprsType);
		return filter;
	}

	private OptionExpression createOptionExpr(int code, boolean isV4, Operator op) {
		OptionExpression expr = new OptionExpression();
		expr.setCode(code);
		expr.setV4(isV4);
		expr.setOperator(op);
		return expr;
	}

	private Filter wrapExpression(OptionExpression optExpr) {
		Filter filter = new Filter();
		FilterExpressionsType exprsType = new FilterExpressionsType();
		FilterExpression expr = new FilterExpression();
		expr.setOptionExpression(optExpr);
		exprsType.getFilterExpressionList().add(expr);
		filter.setFilterExpressions(exprsType);
		return filter;
	}
}
