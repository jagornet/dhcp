/*
 * Copyright 2026 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */
package com.jagornet.dhcp.server.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.option.base.DhcpOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4BootFileNameOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4DomainNameOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4DomainServersOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4RoutersOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4SubnetMaskOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4VendorSpecificOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6DnsServersOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6DomainSearchListOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6PreferenceOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6SipServerAddressesOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6StatusCodeOption;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.server.config.xml.V4AddressPool;
import com.jagornet.dhcp.server.request.binding.V4AddressBindingPool;
import com.jagornet.dhcp.server.testutil.DhcpAssert;
import com.jagornet.dhcp.server.testutil.DhcpConfigTestContext;
import com.jagornet.dhcp.server.testutil.DhcpV4MessageBuilder;
import com.jagornet.dhcp.server.testutil.DhcpV6MessageBuilder;

/**
 * Unit and integration tests for DhcpServerConfiguration scoped option resolution,
 * inheritance, overriding, and first-matching-filter semantics.
 * 
 * Verifies option resolution across:
 *   Global Options -> Global Filters -> Link Options -> Link Filters -> Pool Options -> Pool Filters
 * 
 * @author A. Gregory Rabil
 */
public class TestFilterOptionResolution {

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
	public void testGlobalV4OptionsNoFilterMatch() throws Exception {
		String xml =
			"<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
			"  <v4ConfigOptions>\n" +
			"    <v4SubnetMaskOption><ipAddress>255.255.255.0</ipAddress></v4SubnetMaskOption>\n" +
			"    <v4DomainNameOption><domainName>global.example.com</domainName></v4DomainNameOption>\n" +
			"  </v4ConfigOptions>\n" +
			"  <filters>\n" +
			"    <filterList>\n" +
			"      <name>VoipFilter</name>\n" +
			"      <filterExpressions>\n" +
			"        <filterExpressionList>\n" +
			"          <clientClassExpression operator=\"equals\">\n" +
			"            <v4VendorClassOption>\n" +
			"              <opaqueData><asciiValue>Cisco-IP-Phone</asciiValue></opaqueData>\n" +
			"            </v4VendorClassOption>\n" +
			"          </clientClassExpression>\n" +
			"        </filterExpressionList>\n" +
			"      </filterExpressions>\n" +
			"      <v4ConfigOptions>\n" +
			"        <v4DomainNameOption><domainName>voip.example.com</domainName></v4DomainNameOption>\n" +
			"        <v4RoutersOption><ipAddressList>10.0.0.1</ipAddressList></v4RoutersOption>\n" +
			"      </v4ConfigOptions>\n" +
			"    </filterList>\n" +
			"  </filters>\n" +
			"</dhc:dhcpServerConfig>";

		context.initXml(xml);

		// Non-matching client (PC)
		DhcpV4Message clientMsg = DhcpV4MessageBuilder.discover()
				.withMac("00:11:22:33:44:55")
				.withVendorClass("Standard-PC")
				.build();

		Map<Integer, DhcpOption> options = DhcpServerConfiguration.getInstance().effectiveV4AddrOptions(clientMsg);
		assertNotNull(options);

		DhcpAssert.assertThat(options)
				.hasOption(DhcpConstants.V4OPTION_SUBNET_MASK)
				.hasOption(DhcpConstants.V4OPTION_DOMAIN_NAME)
				.doesNotHaveOption(DhcpConstants.V4OPTION_ROUTERS);

		DhcpV4SubnetMaskOption maskOpt = (DhcpV4SubnetMaskOption) options.get((int) DhcpConstants.V4OPTION_SUBNET_MASK);
		assertEquals("255.255.255.0", maskOpt.getIpAddress());

		DhcpV4DomainNameOption domainOpt = (DhcpV4DomainNameOption) options.get((int) DhcpConstants.V4OPTION_DOMAIN_NAME);
		assertEquals("global.example.com", domainOpt.getString());
	}

	@Test
	public void testGlobalV4OptionsFilterOverrideAndInjection() throws Exception {
		String xml =
			"<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
			"  <v4ConfigOptions>\n" +
			"    <v4SubnetMaskOption><ipAddress>255.255.255.0</ipAddress></v4SubnetMaskOption>\n" +
			"    <v4DomainNameOption><domainName>global.example.com</domainName></v4DomainNameOption>\n" +
			"  </v4ConfigOptions>\n" +
			"  <filters>\n" +
			"    <filterList>\n" +
			"      <name>VoipFilter</name>\n" +
			"      <filterExpressions>\n" +
			"        <filterExpressionList>\n" +
			"          <clientClassExpression operator=\"equals\">\n" +
			"            <v4VendorClassOption>\n" +
			"              <opaqueData><asciiValue>Cisco-IP-Phone</asciiValue></opaqueData>\n" +
			"            </v4VendorClassOption>\n" +
			"          </clientClassExpression>\n" +
			"        </filterExpressionList>\n" +
			"      </filterExpressions>\n" +
			"      <v4ConfigOptions>\n" +
			"        <v4DomainNameOption><domainName>voip.example.com</domainName></v4DomainNameOption>\n" +
			"        <v4RoutersOption><ipAddressList>10.0.0.1</ipAddressList></v4RoutersOption>\n" +
			"      </v4ConfigOptions>\n" +
			"    </filterList>\n" +
			"  </filters>\n" +
			"</dhc:dhcpServerConfig>";

		context.initXml(xml);

		// Matching client (VoIP phone)
		DhcpV4Message clientMsg = DhcpV4MessageBuilder.discover()
				.withMac("00:11:22:33:44:55")
				.withVendorClass("Cisco-IP-Phone")
				.build();

		Map<Integer, DhcpOption> options = DhcpServerConfiguration.getInstance().effectiveV4AddrOptions(clientMsg);
		assertNotNull(options);

		DhcpAssert.assertThat(options)
				.hasOption(DhcpConstants.V4OPTION_SUBNET_MASK)
				.hasOption(DhcpConstants.V4OPTION_DOMAIN_NAME)
				.hasOption(DhcpConstants.V4OPTION_ROUTERS);

		// Subnet mask inherited from global base
		DhcpV4SubnetMaskOption maskOpt = (DhcpV4SubnetMaskOption) options.get((int) DhcpConstants.V4OPTION_SUBNET_MASK);
		assertEquals("255.255.255.0", maskOpt.getIpAddress());

		// Domain name overridden by VoipFilter
		DhcpV4DomainNameOption domainOpt = (DhcpV4DomainNameOption) options.get((int) DhcpConstants.V4OPTION_DOMAIN_NAME);
		assertEquals("voip.example.com", domainOpt.getString());

		// Routers injected by VoipFilter
		DhcpV4RoutersOption routersOpt = (DhcpV4RoutersOption) options.get((int) DhcpConstants.V4OPTION_ROUTERS);
		assertEquals("10.0.0.1", routersOpt.getIpAddressList().get(0));
	}

	@Test
	public void testGlobalV6OptionsFilterOverrideAndInjection() throws Exception {
		String xml =
			"<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
			"  <v6MsgConfigOptions>\n" +
			"    <v6DnsServersOption><ipAddressList>2001:db8::1</ipAddressList></v6DnsServersOption>\n" +
			"    <v6DomainSearchListOption><domainNameList>example.com</domainNameList></v6DomainSearchListOption>\n" +
			"  </v6MsgConfigOptions>\n" +
			"  <filters>\n" +
			"    <filterList>\n" +
			"      <name>VoipV6Filter</name>\n" +
			"      <filterExpressions>\n" +
			"        <filterExpressionList>\n" +
			"          <clientClassExpression operator=\"equals\">\n" +
			"            <v6VendorClassOption>\n" +
			"              <opaqueDataList><asciiValue>Cisco</asciiValue></opaqueDataList>\n" +
			"              <enterpriseNumber>9</enterpriseNumber>\n" +
			"            </v6VendorClassOption>\n" +
			"          </clientClassExpression>\n" +
			"        </filterExpressionList>\n" +
			"      </filterExpressions>\n" +
			"      <v6MsgConfigOptions>\n" +
			"        <v6DnsServersOption><ipAddressList>2001:db8::99</ipAddressList></v6DnsServersOption>\n" +
			"        <v6SipServerAddressesOption><ipAddressList>2001:db8::5060</ipAddressList></v6SipServerAddressesOption>\n" +
			"      </v6MsgConfigOptions>\n" +
			"    </filterList>\n" +
			"  </filters>\n" +
			"</dhc:dhcpServerConfig>";

		context.initXml(xml);

		// Non-matching client
		DhcpV6Message standardClient = DhcpV6MessageBuilder.solicit()
				.withDuid("00010001aabbccdd001122334455")
				.build();

		Map<Integer, DhcpOption> standardOpts = DhcpServerConfiguration.getInstance().effectiveMsgOptions(standardClient);
		DhcpAssert.assertThat(standardOpts)
				.hasOption(DhcpConstants.V6OPTION_DNS_SERVERS)
				.hasOption(DhcpConstants.V6OPTION_DOMAIN_SEARCH_LIST)
				.doesNotHaveOption(DhcpConstants.V6OPTION_SIP_SERVERS_ADDRESS_LIST);

		DhcpV6DnsServersOption stdDns = (DhcpV6DnsServersOption) standardOpts.get(DhcpConstants.V6OPTION_DNS_SERVERS);
		assertEquals("2001:db8::1", stdDns.getIpAddressList().get(0));

		// Matching client
		DhcpV6Message voipClient = DhcpV6MessageBuilder.solicit()
				.withDuid("00010001aabbccdd001122334455")
				.withVendorClass(9, "Cisco")
				.build();

		Map<Integer, DhcpOption> voipOpts = DhcpServerConfiguration.getInstance().effectiveMsgOptions(voipClient);
		DhcpAssert.assertThat(voipOpts)
				.hasOption(DhcpConstants.V6OPTION_DNS_SERVERS)
				.hasOption(DhcpConstants.V6OPTION_DOMAIN_SEARCH_LIST)
				.hasOption(DhcpConstants.V6OPTION_SIP_SERVERS_ADDRESS_LIST);

		// DNS servers overridden
		DhcpV6DnsServersOption voipDns = (DhcpV6DnsServersOption) voipOpts.get(DhcpConstants.V6OPTION_DNS_SERVERS);
		assertEquals("2001:db8::99", voipDns.getIpAddressList().get(0));

		// Domain search list inherited
		DhcpV6DomainSearchListOption voipDomain = (DhcpV6DomainSearchListOption) voipOpts.get(DhcpConstants.V6OPTION_DOMAIN_SEARCH_LIST);
		assertEquals("example.com", voipDomain.getDomainNameList().get(0));

		// SIP server injected
		DhcpV6SipServerAddressesOption sipOpt = (DhcpV6SipServerAddressesOption) voipOpts.get(DhcpConstants.V6OPTION_SIP_SERVERS_ADDRESS_LIST);
		assertEquals("2001:db8::5060", sipOpt.getIpAddressList().get(0));
	}

	@Test
	public void testLinkLevelV4OptionInheritanceAndFilterOverride() throws Exception {
		String xml =
			"<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
			"  <v4ConfigOptions>\n" +
			"    <v4SubnetMaskOption><ipAddress>255.255.0.0</ipAddress></v4SubnetMaskOption>\n" +
			"    <v4DomainNameOption><domainName>global.example.com</domainName></v4DomainNameOption>\n" +
			"  </v4ConfigOptions>\n" +
			"  <links>\n" +
			"    <linkList>\n" +
			"      <name>OfficeLink</name>\n" +
			"      <address>192.168.1.0/24</address>\n" +
			"      <v4ConfigOptions>\n" +
			"        <v4DomainNameOption><domainName>office.example.com</domainName></v4DomainNameOption>\n" +
			"        <v4RoutersOption><ipAddressList>192.168.1.1</ipAddressList></v4RoutersOption>\n" +
			"      </v4ConfigOptions>\n" +
			"      <linkFilters>\n" +
			"        <linkFilterList>\n" +
			"          <name>PrintersFilter</name>\n" +
			"          <filterExpressions>\n" +
			"            <filterExpressionList>\n" +
			"              <clientClassExpression operator=\"startsWith\">\n" +
			"                <v4VendorClassOption>\n" +
			"                  <opaqueData><asciiValue>HP-Printer</asciiValue></opaqueData>\n" +
			"                </v4VendorClassOption>\n" +
			"              </clientClassExpression>\n" +
			"            </filterExpressionList>\n" +
			"          </filterExpressions>\n" +
			"          <v4ConfigOptions>\n" +
			"            <v4DomainNameOption><domainName>printers.office.example.com</domainName></v4DomainNameOption>\n" +
			"            <v4DomainServersOption><ipAddressList>192.168.1.254</ipAddressList></v4DomainServersOption>\n" +
			"          </v4ConfigOptions>\n" +
			"        </linkFilterList>\n" +
			"      </linkFilters>\n" +
			"    </linkList>\n" +
			"  </links>\n" +
			"</dhc:dhcpServerConfig>";

		context.initXml(xml);

		DhcpLink link = DhcpServerConfiguration.getInstance().findLinkForAddress(InetAddress.getByName("192.168.1.50"));
		if (link == null && !DhcpServerConfiguration.getInstance().getLinkMap().isEmpty()) {
			link = DhcpServerConfiguration.getInstance().getLinkMap().values().iterator().next();
		}
		assertNotNull("DhcpLink must be found", link);

		// Non-matching client on link
		DhcpV4Message clientMsg = DhcpV4MessageBuilder.discover()
				.withMac("00:11:22:33:44:55")
				.withVendorClass("Standard-PC")
				.build();

		Map<Integer, DhcpOption> opts = DhcpServerConfiguration.getInstance().effectiveV4AddrOptions(clientMsg, link);
		DhcpAssert.assertThat(opts)
				.hasOption(DhcpConstants.V4OPTION_SUBNET_MASK)
				.hasOption(DhcpConstants.V4OPTION_DOMAIN_NAME)
				.hasOption(DhcpConstants.V4OPTION_ROUTERS)
				.doesNotHaveOption(DhcpConstants.V4OPTION_DOMAIN_SERVERS);

		// Subnet mask from Global
		DhcpV4SubnetMaskOption maskOpt = (DhcpV4SubnetMaskOption) opts.get((int) DhcpConstants.V4OPTION_SUBNET_MASK);
		assertEquals("255.255.0.0", maskOpt.getIpAddress());

		// Domain name overridden by Link level (office.example.com beats global.example.com)
		DhcpV4DomainNameOption domOpt = (DhcpV4DomainNameOption) opts.get((int) DhcpConstants.V4OPTION_DOMAIN_NAME);
		assertEquals("office.example.com", domOpt.getString());

		// Routers provided by Link
		DhcpV4RoutersOption rtrOpt = (DhcpV4RoutersOption) opts.get((int) DhcpConstants.V4OPTION_ROUTERS);
		assertEquals("192.168.1.1", rtrOpt.getIpAddressList().get(0));

		// Matching client on link
		DhcpV4Message printerMsg = DhcpV4MessageBuilder.discover()
				.withMac("00:11:22:33:44:55")
				.withVendorClass("HP-Printer-LaserJet-4000")
				.build();

		Map<Integer, DhcpOption> printerOpts = DhcpServerConfiguration.getInstance().effectiveV4AddrOptions(printerMsg, link);
		DhcpAssert.assertThat(printerOpts)
				.hasOption(DhcpConstants.V4OPTION_SUBNET_MASK)
				.hasOption(DhcpConstants.V4OPTION_DOMAIN_NAME)
				.hasOption(DhcpConstants.V4OPTION_ROUTERS)
				.hasOption(DhcpConstants.V4OPTION_DOMAIN_SERVERS);

		// LinkFilter domain name beats Link and Global!
		DhcpV4DomainNameOption prtDom = (DhcpV4DomainNameOption) printerOpts.get((int) DhcpConstants.V4OPTION_DOMAIN_NAME);
		assertEquals("printers.office.example.com", prtDom.getString());

		// LinkFilter domain servers injected
		DhcpV4DomainServersOption prtDns = (DhcpV4DomainServersOption) printerOpts.get((int) DhcpConstants.V4OPTION_DOMAIN_SERVERS);
		assertEquals("192.168.1.254", prtDns.getIpAddressList().get(0));
	}

	@Test
	public void testPoolLevelV4OptionHierarchyAndOverride() throws Exception {
		// 4-tier hierarchy: Global -> Link -> LinkFilter -> Pool -> PoolFilter
		String xml =
			"<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
			"  <v4ConfigOptions>\n" +
			"    <v4SubnetMaskOption><ipAddress>255.255.0.0</ipAddress></v4SubnetMaskOption>\n" +
			"    <v4DomainNameOption><domainName>global.com</domainName></v4DomainNameOption>\n" +
			"  </v4ConfigOptions>\n" +
			"  <links>\n" +
			"    <linkList>\n" +
			"      <name>LabLink</name>\n" +
			"      <address>192.168.2.0/24</address>\n" +
			"      <v4ConfigOptions>\n" +
			"        <v4RoutersOption><ipAddressList>192.168.2.1</ipAddressList></v4RoutersOption>\n" +
			"      </v4ConfigOptions>\n" +
			"      <v4AddrPools>\n" +
			"        <poolList>\n" +
			"          <range>192.168.2.100-192.168.2.150</range>\n" +
			"          <configOptions>\n" +
			"            <v4DomainNameOption><domainName>pool.lab.com</domainName></v4DomainNameOption>\n" +
			"          </configOptions>\n" +
			"          <filters>\n" +
			"            <filterList>\n" +
			"              <name>SpecialPoolFilter</name>\n" +
			"              <filterExpressions>\n" +
			"                <filterExpressionList>\n" +
			"                  <clientClassExpression operator=\"equals\">\n" +
			"                    <v4VendorClassOption>\n" +
			"                      <opaqueData><asciiValue>Special-Device</asciiValue></opaqueData>\n" +
			"                    </v4VendorClassOption>\n" +
			"                  </clientClassExpression>\n" +
			"                </filterExpressionList>\n" +
			"              </filterExpressions>\n" +
			"              <v4ConfigOptions>\n" +
			"                <v4DomainNameOption><domainName>special.pool.lab.com</domainName></v4DomainNameOption>\n" +
			"                <v4BootFileNameOption><string>pxelinux.0</string></v4BootFileNameOption>\n" +
			"              </v4ConfigOptions>\n" +
			"            </filterList>\n" +
			"          </filters>\n" +
			"        </poolList>\n" +
			"      </v4AddrPools>\n" +
			"      <linkFilters>\n" +
			"        <linkFilterList>\n" +
			"          <name>GeneralLabFilter</name>\n" +
			"          <filterExpressions>\n" +
			"            <filterExpressionList>\n" +
			"              <clientClassExpression operator=\"startsWith\">\n" +
			"                <v4VendorClassOption>\n" +
			"                  <opaqueData><asciiValue>Special</asciiValue></opaqueData>\n" +
			"                </v4VendorClassOption>\n" +
			"              </clientClassExpression>\n" +
			"            </filterExpressionList>\n" +
			"          </filterExpressions>\n" +
			"          <v4ConfigOptions>\n" +
			"            <v4DomainNameOption><domainName>linkfilter.lab.com</domainName></v4DomainNameOption>\n" +
			"          </v4ConfigOptions>\n" +
			"        </linkFilterList>\n" +
			"      </linkFilters>\n" +
			"    </linkList>\n" +
			"  </links>\n" +
			"</dhc:dhcpServerConfig>";

		context.initXml(xml);

		DhcpLink link = DhcpServerConfiguration.getInstance().getLinkMap().values().iterator().next();
		assertNotNull(link);

		V4AddressPool rawPool = link.getLink().getV4AddrPools().getPoolList().get(0);
		V4AddressBindingPool poolObj = new V4AddressBindingPool(rawPool);

		// Case 1: Client matching SpecialPoolFilter
		// Hierarchy: PoolFilter beats Pool, LinkFilter, Link, and Global!
		DhcpV4Message specialClient = DhcpV4MessageBuilder.discover()
				.withMac("00:11:22:33:44:55")
				.withVendorClass("Special-Device")
				.build();

		Map<Integer, DhcpOption> specialOpts = 
				DhcpServerConfiguration.getInstance().effectiveV4AddrOptions(specialClient, link, poolObj);

		DhcpAssert.assertThat(specialOpts)
				.hasOption(DhcpConstants.V4OPTION_SUBNET_MASK)
				.hasOption(DhcpConstants.V4OPTION_ROUTERS)
				.hasOption(DhcpConstants.V4OPTION_DOMAIN_NAME)
				.hasOption(DhcpConstants.V4OPTION_BOOT_FILE_NAME);

		DhcpV4DomainNameOption specialDom = (DhcpV4DomainNameOption) specialOpts.get((int) DhcpConstants.V4OPTION_DOMAIN_NAME);
		assertEquals("special.pool.lab.com", specialDom.getString());

		DhcpV4BootFileNameOption bootOpt = (DhcpV4BootFileNameOption) specialOpts.get((int) DhcpConstants.V4OPTION_BOOT_FILE_NAME);
		assertEquals("pxelinux.0", bootOpt.getString());

		// Case 2: Client matching LinkFilter (Special-Other) but NOT PoolFilter (Special-Device)
		// Pool base options beat LinkFilter!
		DhcpV4Message otherSpecialClient = DhcpV4MessageBuilder.discover()
				.withMac("00:11:22:33:44:55")
				.withVendorClass("Special-Other")
				.build();

		Map<Integer, DhcpOption> otherOpts = 
				DhcpServerConfiguration.getInstance().effectiveV4AddrOptions(otherSpecialClient, link, poolObj);

		DhcpAssert.assertThat(otherOpts)
				.hasOption(DhcpConstants.V4OPTION_SUBNET_MASK)
				.hasOption(DhcpConstants.V4OPTION_ROUTERS)
				.hasOption(DhcpConstants.V4OPTION_DOMAIN_NAME)
				.doesNotHaveOption(DhcpConstants.V4OPTION_BOOT_FILE_NAME);

		// Pool option beats LinkFilter
		DhcpV4DomainNameOption otherDom = (DhcpV4DomainNameOption) otherOpts.get((int) DhcpConstants.V4OPTION_DOMAIN_NAME);
		assertEquals("pool.lab.com", otherDom.getString());
	}

	@Test
	public void testFirstMatchingFilterWinsForOptions() throws Exception {
		// Unlike policies where the last matching filter wins, for options
		// DhcpServerConfiguration returns the options of the FIRST matching filter.
		String xml =
			"<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
			"  <filters>\n" +
			"    <filterList>\n" +
			"      <name>FilterFirst</name>\n" +
			"      <filterExpressions>\n" +
			"        <filterExpressionList>\n" +
			"          <clientClassExpression operator=\"startsWith\">\n" +
			"            <v4VendorClassOption>\n" +
			"              <opaqueData><asciiValue>DualMatch</asciiValue></opaqueData>\n" +
			"            </v4VendorClassOption>\n" +
			"          </clientClassExpression>\n" +
			"        </filterExpressionList>\n" +
			"      </filterExpressions>\n" +
			"      <v4ConfigOptions>\n" +
			"        <v4DomainNameOption><domainName>first.example.com</domainName></v4DomainNameOption>\n" +
			"      </v4ConfigOptions>\n" +
			"    </filterList>\n" +
			"    <filterList>\n" +
			"      <name>FilterSecond</name>\n" +
			"      <filterExpressions>\n" +
			"        <filterExpressionList>\n" +
			"          <clientClassExpression operator=\"startsWith\">\n" +
			"            <v4VendorClassOption>\n" +
			"              <opaqueData><asciiValue>DualMatch</asciiValue></opaqueData>\n" +
			"            </v4VendorClassOption>\n" +
			"          </clientClassExpression>\n" +
			"        </filterExpressionList>\n" +
			"      </filterExpressions>\n" +
			"      <v4ConfigOptions>\n" +
			"        <v4DomainNameOption><domainName>second.example.com</domainName></v4DomainNameOption>\n" +
			"      </v4ConfigOptions>\n" +
			"    </filterList>\n" +
			"  </filters>\n" +
			"</dhc:dhcpServerConfig>";

		context.initXml(xml);

		DhcpV4Message clientMsg = DhcpV4MessageBuilder.discover()
				.withMac("00:11:22:33:44:55")
				.withVendorClass("DualMatch-Client")
				.build();

		Map<Integer, DhcpOption> opts = DhcpServerConfiguration.getInstance().effectiveV4AddrOptions(clientMsg);
		assertNotNull(opts);

		DhcpV4DomainNameOption dom = (DhcpV4DomainNameOption) opts.get((int) DhcpConstants.V4OPTION_DOMAIN_NAME);
		assertNotNull(dom);
		assertEquals("First matching filter in document order must win for options",
				"first.example.com", dom.getString());
	}

	@Test
	public void testScopedDhcpV6OptionsInLinkFilter() throws Exception {
		String xml =
			"<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
			"  <links>\n" +
			"    <linkList>\n" +
			"      <name>V6ScopedLink</name>\n" +
			"      <address>2001:db8:1::/64</address>\n" +
			"      <linkFilters>\n" +
			"        <linkFilterList>\n" +
			"          <name>SpecialV6Filter</name>\n" +
			"          <filterExpressions>\n" +
			"            <filterExpressionList>\n" +
			"              <clientClassExpression operator=\"equals\">\n" +
			"                <v6VendorClassOption>\n" +
			"                  <opaqueDataList><asciiValue>Enterprise-Device</asciiValue></opaqueDataList>\n" +
			"                  <enterpriseNumber>12345</enterpriseNumber>\n" +
			"                </v6VendorClassOption>\n" +
			"              </clientClassExpression>\n" +
			"            </filterExpressionList>\n" +
			"          </filterExpressions>\n" +
			"          <v6IaNaConfigOptions>\n" +
			"            <v6PreferenceOption><unsignedByte>200</unsignedByte></v6PreferenceOption>\n" +
			"          </v6IaNaConfigOptions>\n" +
			"          <v6NaAddrConfigOptions>\n" +
			"            <v6StatusCodeOption><statusCode>0</statusCode><message>Success</message></v6StatusCodeOption>\n" +
			"          </v6NaAddrConfigOptions>\n" +
			"          <v6IaPdConfigOptions>\n" +
			"            <v6PreferenceOption><unsignedByte>220</unsignedByte></v6PreferenceOption>\n" +
			"          </v6IaPdConfigOptions>\n" +
			"        </linkFilterList>\n" +
			"      </linkFilters>\n" +
			"    </linkList>\n" +
			"  </links>\n" +
			"</dhc:dhcpServerConfig>";

		context.initXml(xml);

		DhcpLink link = DhcpServerConfiguration.getInstance().getLinkMap().values().iterator().next();
		assertNotNull(link);

		// Non-matching client
		DhcpV6Message nonMatchingMsg = DhcpV6MessageBuilder.solicit()
				.withDuid("00010001aabbccdd001122334455")
				.build();

		Map<Integer, DhcpOption> naOptsNonMatch = 
				DhcpServerConfiguration.getInstance().effectiveIaNaOptions(nonMatchingMsg, link);
		assertTrue("Non-matching client should not have IA_NA options from filter",
				naOptsNonMatch == null || naOptsNonMatch.isEmpty());

		// Matching client
		DhcpV6Message matchingMsg = DhcpV6MessageBuilder.solicit()
				.withDuid("00010001aabbccdd001122334455")
				.withVendorClass(12345, "Enterprise-Device")
				.build();

		// Check IA_NA scoped options
		Map<Integer, DhcpOption> iaNaOpts = 
				DhcpServerConfiguration.getInstance().effectiveIaNaOptions(matchingMsg, link);
		DhcpAssert.assertThat(iaNaOpts).hasOption(DhcpConstants.V6OPTION_PREFERENCE);
		DhcpV6PreferenceOption prefNa = (DhcpV6PreferenceOption) iaNaOpts.get(DhcpConstants.V6OPTION_PREFERENCE);
		assertEquals(200, prefNa.getUnsignedByte());

		// Check IA_ADDR scoped options
		Map<Integer, DhcpOption> naAddrOpts = 
				DhcpServerConfiguration.getInstance().effectiveNaAddrOptions(matchingMsg, link);
		DhcpAssert.assertThat(naAddrOpts).hasOption(DhcpConstants.V6OPTION_STATUS_CODE);
		DhcpV6StatusCodeOption statusOpt = (DhcpV6StatusCodeOption) naAddrOpts.get(DhcpConstants.V6OPTION_STATUS_CODE);
		assertEquals(0, statusOpt.getStatusCode());
		assertEquals("Success", statusOpt.getMessage());

		// Check IA_PD scoped options
		Map<Integer, DhcpOption> iaPdOpts = 
				DhcpServerConfiguration.getInstance().effectiveIaPdOptions(matchingMsg, link);
		DhcpAssert.assertThat(iaPdOpts).hasOption(DhcpConstants.V6OPTION_PREFERENCE);
		DhcpV6PreferenceOption prefPd = (DhcpV6PreferenceOption) iaPdOpts.get(DhcpConstants.V6OPTION_PREFERENCE);
		assertEquals(220, prefPd.getUnsignedByte());
	}

	@Test
	public void testVendorSpecificOption43Injection() throws Exception {
		String xml =
			"<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
			"  <filters>\n" +
			"    <filterList>\n" +
			"      <name>VendorSpecificFilter</name>\n" +
			"      <filterExpressions>\n" +
			"        <filterExpressionList>\n" +
			"          <clientClassExpression operator=\"equals\">\n" +
			"            <v4VendorClassOption>\n" +
			"              <opaqueData><asciiValue>Enterprise-AP</asciiValue></opaqueData>\n" +
			"            </v4VendorClassOption>\n" +
			"          </clientClassExpression>\n" +
			"        </filterExpressionList>\n" +
			"      </filterExpressions>\n" +
			"      <v4ConfigOptions>\n" +
			"        <v4VendorSpecificOption>\n" +
			"          <opaqueData><asciiValue>WLC-IP=10.10.10.1</asciiValue></opaqueData>\n" +
			"        </v4VendorSpecificOption>\n" +
			"      </v4ConfigOptions>\n" +
			"    </filterList>\n" +
			"  </filters>\n" +
			"</dhc:dhcpServerConfig>";

		context.initXml(xml);

		// Non-matching client
		DhcpV4Message clientMsg = DhcpV4MessageBuilder.discover()
				.withMac("00:11:22:33:44:55")
				.withVendorClass("Normal-Client")
				.build();

		Map<Integer, DhcpOption> nonMatchOpts = 
				DhcpServerConfiguration.getInstance().effectiveV4AddrOptions(clientMsg);
		DhcpAssert.assertThat(nonMatchOpts).doesNotHaveOption(DhcpConstants.V4OPTION_VENDOR_INFO);

		// Matching Access Point
		DhcpV4Message apMsg = DhcpV4MessageBuilder.discover()
				.withMac("00:11:22:33:44:55")
				.withVendorClass("Enterprise-AP")
				.build();

		Map<Integer, DhcpOption> apOpts = 
				DhcpServerConfiguration.getInstance().effectiveV4AddrOptions(apMsg);
		DhcpAssert.assertThat(apOpts).hasOption(DhcpConstants.V4OPTION_VENDOR_INFO);

		DhcpV4VendorSpecificOption vso = (DhcpV4VendorSpecificOption) apOpts.get((int) DhcpConstants.V4OPTION_VENDOR_INFO);
		assertNotNull(vso);
		assertNotNull(vso.getOpaqueData());
		assertEquals("WLC-IP=10.10.10.1", vso.getOpaqueData().getAscii());
	}
}
