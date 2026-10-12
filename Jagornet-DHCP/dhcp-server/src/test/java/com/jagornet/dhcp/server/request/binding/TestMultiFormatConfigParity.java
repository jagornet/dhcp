/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.request.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.option.base.DhcpOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4BootFileNameOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4DomainNameOption;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.config.xml.DhcpServerConfig;
import com.jagornet.dhcp.server.config.xml.Filter;
import com.jagornet.dhcp.server.config.xml.LinkFilter;
import com.jagornet.dhcp.server.db.IaManager;
import com.jagornet.dhcp.server.testutil.DhcpAssert;
import com.jagornet.dhcp.server.testutil.DhcpConfigTestContext;
import com.jagornet.dhcp.server.testutil.DhcpV4MessageBuilder;
import com.jagornet.dhcp.server.testutil.DhcpV6MessageBuilder;

/**
 * Comprehensive configuration format parity test suite.
 * Verifies that XML, YAML, and JSON configurations produce 100% equivalent:
 * 1. Deserialized in-memory object graphs (filters, linkFilters, pools, policies, options).
 * 2. Filter matching evaluations (DhcpServerConfiguration#msgMatchesFilter).
 * 3. Scoped policy resolutions (DhcpServerPolicies).
 * 4. Scoped DHCP option resolutions (DhcpServerConfiguration).
 * 5. Address binding pool ordering and runtime pool allocations.
 * 6. Native human-authored YAML and JSON snippets vs XML equivalents.
 * 
 * @author A. Gregory Rabil
 */
public class TestMultiFormatConfigParity {

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

	/**
	 * Canonical reference XML snippet containing global policies, global filters,
	 * link-level linkFilters, pools, options, and policies.
	 */
	private static final String REFERENCE_XML =
		"<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
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
		"          <string>voip/cisco.bin</string>\n" +
		"        </v4BootFileNameOption>\n" +
		"      </v4ConfigOptions>\n" +
		"    </filterList>\n" +
		"  </filters>\n" +
		"  <links>\n" +
		"    <linkList>\n" +
		"      <name>CorporateLink</name>\n" +
		"      <address>10.10.1.0/24</address>\n" +
		"      <v4ConfigOptions>\n" +
		"        <v4DomainNameOption>\n" +
		"          <domainName>corp.local</domainName>\n" +
		"        </v4DomainNameOption>\n" +
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
		"              <string>voip/link-specific.bin</string>\n" +
		"            </v4BootFileNameOption>\n" +
		"          </v4ConfigOptions>\n" +
		"          <v4AddrPools>\n" +
		"            <poolList>\n" +
		"              <range>10.10.1.10-10.10.1.50</range>\n" +
		"            </poolList>\n" +
		"          </v4AddrPools>\n" +
		"        </linkFilterList>\n" +
		"      </linkFilters>\n" +
		"    </linkList>\n" +
		"  </links>\n" +
		"</dhc:dhcpServerConfig>";

	/**
	 * Verify that converting the reference XML configuration to YAML and JSON,
	 * then loading each format into the server context, yields identical
	 * filter matching, policy resolution, option scoping, and pool allocation behavior.
	 */
	@Test
	public void testXmlJsonYamlRoundTripParity() throws Exception {
		// 1. Load reference XML
		DhcpServerConfig xmlConfig = context.initXml(REFERENCE_XML);
		assertNotNull(xmlConfig);

		// 2. Export to YAML and JSON
		ByteArrayOutputStream yamlBaos = new ByteArrayOutputStream();
		DhcpServerConfiguration.saveYamlConfig(xmlConfig, yamlBaos);
		String yamlString = yamlBaos.toString(StandardCharsets.UTF_8.name());
		assertNotNull("YAML string must not be null", yamlString);
		assertFalse("YAML string must not be empty", yamlString.trim().isEmpty());

		ByteArrayOutputStream jsonBaos = new ByteArrayOutputStream();
		DhcpServerConfiguration.saveJsonConfig(xmlConfig, jsonBaos);
		String jsonString = jsonBaos.toString(StandardCharsets.UTF_8.name());
		assertNotNull("JSON string must not be null", jsonString);
		assertFalse("JSON string must not be empty", jsonString.trim().isEmpty());

		// 3. Verify XML behavior
		verifyFormatBehavior("XML");

		// 4. Reload as YAML and verify identical behavior
		context.initYaml(yamlString);
		verifyFormatBehavior("YAML");

		// 5. Reload as JSON and verify identical behavior
		context.initJson(jsonString);
		verifyFormatBehavior("JSON");
	}

	/**
	 * Comprehensive behavioral assertions executed against any active configuration format.
	 */
	private void verifyFormatBehavior(String formatLabel) throws Exception {
		DhcpServerConfiguration serverConfig = DhcpServerConfiguration.getInstance();
		assertNotNull("[" + formatLabel + "] Server configuration instance must exist", serverConfig);

		// Packets for matching
		DhcpV4Message voipMsg = DhcpV4MessageBuilder.discover()
				.withMac("00:11:22:33:44:55")
				.withVendorClass("Cisco-IP-Phone")
				.build();

		DhcpV4Message pcMsg = DhcpV4MessageBuilder.discover()
				.withMac("00:aa:bb:cc:dd:ee")
				.withVendorClass("MSFT 5.0")
				.build();

		// Verify Global Policies
		assertEquals("[" + formatLabel + "] Global sendRequestedOptionsOnly must match",
				"false", DhcpServerPolicies.effectivePolicy(pcMsg, null, Property.DHCP_SEND_REQUESTED_OPTIONS_ONLY));
		assertEquals("[" + formatLabel + "] Global defaultLeaseTime for generic client",
				86400, DhcpServerPolicies.effectivePolicyAsInt(pcMsg, null, Property.V4_DEFAULT_LEASETIME));

		// Verify Filters exist
		assertNotNull("[" + formatLabel + "] Global filters must exist", serverConfig.getGlobalFilters());
		assertEquals("[" + formatLabel + "] Must have 1 global filter", 1, serverConfig.getGlobalFilters().getFilterList().size());
		Filter globalFilter = serverConfig.getGlobalFilters().getFilterList().get(0);
		assertEquals("GlobalVoipFilter", globalFilter.getName());

		// Verify filter matching
		assertTrue("[" + formatLabel + "] VoIP packet must match GlobalVoipFilter",
				DhcpServerConfiguration.msgMatchesFilter(voipMsg, globalFilter));
		assertFalse("[" + formatLabel + "] PC packet must NOT match GlobalVoipFilter",
				DhcpServerConfiguration.msgMatchesFilter(pcMsg, globalFilter));

		// Verify Links and LinkFilters
		DhcpLink link = serverConfig.findLinkForAddress(InetAddress.getByName("10.10.1.150"));
		assertNotNull("[" + formatLabel + "] CorporateLink must exist", link);
		assertEquals("CorporateLink", link.getLink().getName());
		assertNotNull("[" + formatLabel + "] Link must have linkFilters", link.getLink().getLinkFilters());
		assertEquals(1, link.getLink().getLinkFilters().getLinkFilterList().size());

		LinkFilter linkFilter = link.getLink().getLinkFilters().getLinkFilterList().get(0);
		assertEquals("VoipLinkFilter", linkFilter.getName());
		assertTrue("[" + formatLabel + "] VoIP packet must match VoipLinkFilter",
				DhcpServerConfiguration.msgMatchesFilter(voipMsg, linkFilter));

		// Verify Policy Precedence: LinkFilter policy overrides Global & Link
		assertEquals("[" + formatLabel + "] VoIP client must get LinkFilter policy (3600)",
				3600, DhcpServerPolicies.effectivePolicyAsInt(voipMsg, link.getLink(), Property.V4_DEFAULT_LEASETIME));
		assertEquals("[" + formatLabel + "] PC client must get Global policy (86400)",
				86400, DhcpServerPolicies.effectivePolicyAsInt(pcMsg, link.getLink(), Property.V4_DEFAULT_LEASETIME));

		// Verify Option Scoping
		Map<Integer, DhcpOption> pcOpts = serverConfig.effectiveV4AddrOptions(pcMsg, link);
		DhcpV4DomainNameOption linkDomainOpt = (DhcpV4DomainNameOption) pcOpts.get((int) DhcpConstants.V4OPTION_DOMAIN_NAME);
		assertNotNull("[" + formatLabel + "] PC client gets link domain name option", linkDomainOpt);
		assertEquals("corp.local", linkDomainOpt.getString());
		assertNull("[" + formatLabel + "] PC client must NOT receive VoIP boot file option",
				pcOpts.get((int) DhcpConstants.V4OPTION_BOOT_FILE_NAME));

		Map<Integer, DhcpOption> voipOpts = serverConfig.effectiveV4AddrOptions(voipMsg, link);
		DhcpV4BootFileNameOption voipBootOpt = (DhcpV4BootFileNameOption) voipOpts.get((int) DhcpConstants.V4OPTION_BOOT_FILE_NAME);
		assertNotNull("[" + formatLabel + "] VoIP client gets scoped boot file name", voipBootOpt);
		assertEquals("voip/link-specific.bin", voipBootOpt.getString());

		// Verify Binding Pool Order & Dynamic Allocation
		V4AddrBindingManagerImpl v4Mgr = new V4AddrBindingManagerImpl();
		try {
			v4Mgr.setIaMgr(mockIaMgr);
			v4Mgr.init();

			List<? extends BindingPool> pools = v4Mgr.getBindingPoolMap().get("10.10.1.0/24");
			assertNotNull("[" + formatLabel + "] Pools must exist for link", pools);
			assertEquals(2, pools.size());

			// Pool 0: Filtered pool (10.10.1.10-50)
			assertEquals("10.10.1.10", pools.get(0).getStartAddress().getHostAddress());
			assertEquals("10.10.1.50", pools.get(0).getEndAddress().getHostAddress());
			assertNotNull(pools.get(0).getLinkFilter());

			// Pool 1: Default pool (10.10.1.100-199)
			assertEquals("10.10.1.100", pools.get(1).getStartAddress().getHostAddress());
			assertEquals("10.10.1.199", pools.get(1).getEndAddress().getHostAddress());
			assertNull(pools.get(1).getLinkFilter());

			// Allocation isolation
			InetAddress voipIp = v4Mgr.getNextFreeAddress(link, voipMsg);
			assertNotNull("[" + formatLabel + "] VoIP client must allocate IP", voipIp);
			DhcpAssert.assertIpInRange(voipIp, "10.10.1.10", "10.10.1.50");

			InetAddress pcIp = v4Mgr.getNextFreeAddress(link, pcMsg);
			assertNotNull("[" + formatLabel + "] PC client must allocate IP", pcIp);
			DhcpAssert.assertIpInRange(pcIp, "10.10.1.100", "10.10.1.199");
		} finally {
			v4Mgr.close();
		}
	}

	/**
	 * Verify human-authored native YAML configuration parses cleanly and
	 * executes with complete equivalence to XML.
	 */
	@Test
	public void testNativeYamlFilterConfiguration() throws Exception {
		String nativeYaml =
			"policies:\n" +
			"  policyList:\n" +
			"    - name: v4.defaultLeasetime\n" +
			"      value: 43200\n" +
			"links:\n" +
			"  linkList:\n" +
			"    - name: NativeYamlLink\n" +
			"      address: 172.16.50.0/24\n" +
			"      v4AddrPools:\n" +
			"        poolList:\n" +
			"          - range: 172.16.50.100-172.16.50.200\n" +
			"      linkFilters:\n" +
			"        linkFilterList:\n" +
			"          - name: PrinterFilter\n" +
			"            filterExpressions:\n" +
			"              filterExpressionList:\n" +
			"                - clientClassExpression:\n" +
			"                    operator: startsWith\n" +
			"                    v4VendorClassOption:\n" +
			"                      opaqueData:\n" +
			"                        asciiValue: HP-LaserJet\n" +
			"            policies:\n" +
			"              policyList:\n" +
			"                - name: v4.defaultLeasetime\n" +
			"                  value: 604800\n" +
			"            v4AddrPools:\n" +
			"              poolList:\n" +
			"                - range: 172.16.50.10-172.16.50.30\n";

		context.initYaml(nativeYaml);
		DhcpServerConfiguration config = DhcpServerConfiguration.getInstance();
		assertNotNull(config);

		DhcpV4Message printerMsg = DhcpV4MessageBuilder.discover()
				.withVendorClass("HP-LaserJet-M404dn")
				.build();

		DhcpV4Message laptopMsg = DhcpV4MessageBuilder.discover()
				.withVendorClass("MSFT 5.0")
				.build();

		DhcpLink link = config.getLinkMap().values().iterator().next();
		LinkFilter filter = link.getLink().getLinkFilters().getLinkFilterList().get(0);

		// Filter match check
		assertTrue("HP LaserJet should match PrinterFilter with startsWith operator in YAML",
				DhcpServerConfiguration.msgMatchesFilter(printerMsg, filter));
		assertFalse("Laptop should not match PrinterFilter",
				DhcpServerConfiguration.msgMatchesFilter(laptopMsg, filter));

		// Policy check
		assertEquals(604800, DhcpServerPolicies.effectivePolicyAsInt(printerMsg, link.getLink(), Property.V4_DEFAULT_LEASETIME));
		assertEquals(43200, DhcpServerPolicies.effectivePolicyAsInt(laptopMsg, link.getLink(), Property.V4_DEFAULT_LEASETIME));

		// Pool allocation check
		V4AddrBindingManagerImpl v4Mgr = new V4AddrBindingManagerImpl();
		try {
			v4Mgr.setIaMgr(mockIaMgr);
			v4Mgr.init();

			InetAddress printerIp = v4Mgr.getNextFreeAddress(link, printerMsg);
			assertNotNull(printerIp);
			DhcpAssert.assertIpInRange(printerIp, "172.16.50.10", "172.16.50.30");

			InetAddress laptopIp = v4Mgr.getNextFreeAddress(link, laptopMsg);
			assertNotNull(laptopIp);
			DhcpAssert.assertIpInRange(laptopIp, "172.16.50.100", "172.16.50.200");
		} finally {
			v4Mgr.close();
		}
	}

	/**
	 * Verify human-authored native JSON configuration parses cleanly and
	 * executes with complete equivalence.
	 */
	@Test
	public void testNativeJsonFilterConfiguration() throws Exception {
		String nativeJson =
			"{\n" +
			"  \"policies\": {\n" +
			"    \"policyList\": [\n" +
			"      { \"name\": \"v4.defaultLeasetime\", \"value\": \"14400\" }\n" +
			"    ]\n" +
			"  },\n" +
			"  \"links\": {\n" +
			"    \"linkList\": [\n" +
			"      {\n" +
			"        \"name\": \"NativeJsonLink\",\n" +
			"        \"address\": \"10.20.30.0/24\",\n" +
			"        \"v4AddrPools\": {\n" +
			"          \"poolList\": [\n" +
			"            { \"range\": \"10.20.30.100-10.20.30.200\" }\n" +
			"          ]\n" +
			"        },\n" +
			"        \"linkFilters\": {\n" +
			"          \"linkFilterList\": [\n" +
			"            {\n" +
			"              \"name\": \"MobileFilter\",\n" +
			"              \"filterExpressions\": {\n" +
			"                \"filterExpressionList\": [\n" +
			"                  {\n" +
			"                    \"clientClassExpression\": {\n" +
			"                      \"operator\": \"contains\",\n" +
			"                      \"v4VendorClassOption\": {\n" +
			"                        \"opaqueData\": { \"asciiValue\": \"Android\" }\n" +
			"                      }\n" +
			"                    }\n" +
			"                  }\n" +
			"                ]\n" +
			"              },\n" +
			"              \"policies\": {\n" +
			"                \"policyList\": [\n" +
			"                  { \"name\": \"v4.defaultLeasetime\", \"value\": \"1800\" }\n" +
			"                ]\n" +
			"              },\n" +
			"              \"v4AddrPools\": {\n" +
			"                \"poolList\": [\n" +
			"                  { \"range\": \"10.20.30.10-10.20.30.50\" }\n" +
			"                ]\n" +
			"              }\n" +
			"            }\n" +
			"          ]\n" +
			"        }\n" +
			"      }\n" +
			"    ]\n" +
			"  }\n" +
			"}";

		context.initJson(nativeJson);
		DhcpServerConfiguration config = DhcpServerConfiguration.getInstance();
		assertNotNull(config);

		DhcpV4Message androidMsg = DhcpV4MessageBuilder.discover()
				.withVendorClass("dhcpcd-6.8.2:Linux-3.10:Android-10")
				.build();

		DhcpV4Message pcMsg = DhcpV4MessageBuilder.discover()
				.withVendorClass("MSFT 5.0")
				.build();

		DhcpLink link = config.getLinkMap().values().iterator().next();
		LinkFilter filter = link.getLink().getLinkFilters().getLinkFilterList().get(0);

		// Filter match check
		assertTrue("Android vendor string should match MobileFilter with contains operator in JSON",
				DhcpServerConfiguration.msgMatchesFilter(androidMsg, filter));
		assertFalse("PC should not match MobileFilter",
				DhcpServerConfiguration.msgMatchesFilter(pcMsg, filter));

		// Policy check
		assertEquals(1800, DhcpServerPolicies.effectivePolicyAsInt(androidMsg, link.getLink(), Property.V4_DEFAULT_LEASETIME));
		assertEquals(14400, DhcpServerPolicies.effectivePolicyAsInt(pcMsg, link.getLink(), Property.V4_DEFAULT_LEASETIME));

		// Pool allocation check
		V4AddrBindingManagerImpl v4Mgr = new V4AddrBindingManagerImpl();
		try {
			v4Mgr.setIaMgr(mockIaMgr);
			v4Mgr.init();

			InetAddress androidIp = v4Mgr.getNextFreeAddress(link, androidMsg);
			assertNotNull(androidIp);
			DhcpAssert.assertIpInRange(androidIp, "10.20.30.10", "10.20.30.50");

			InetAddress pcIp = v4Mgr.getNextFreeAddress(link, pcMsg);
			assertNotNull(pcIp);
			DhcpAssert.assertIpInRange(pcIp, "10.20.30.100", "10.20.30.200");
		} finally {
			v4Mgr.close();
		}
	}

	/**
	 * Verify complex compound filter expressions (AND logic across vendor class + MAC address)
	 * function identically in XML, YAML, and JSON.
	 */
	@Test
	public void testCompoundExpressionsParityAcrossFormats() throws Exception {
		String xml =
			"<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
			"  <filters>\n" +
			"    <filterList>\n" +
			"      <name>SecuredVoipFilter</name>\n" +
			"      <filterExpressions>\n" +
			"        <filterExpressionList>\n" +
			"          <clientClassExpression operator=\"equals\">\n" +
			"            <v4VendorClassOption>\n" +
			"              <opaqueData><asciiValue>PolycomSoundPoint</asciiValue></opaqueData>\n" +
			"            </v4VendorClassOption>\n" +
			"          </clientClassExpression>\n" +
			"        </filterExpressionList>\n" +
			"        <filterExpressionList>\n" +
			"          <optionExpression code=\"12\" v4=\"true\" operator=\"startsWith\">\n" +
			"            <stringOption><string>polycom-phone</string></stringOption>\n" +
			"          </optionExpression>\n" +
			"        </filterExpressionList>\n" +
			"      </filterExpressions>\n" +
			"    </filterList>\n" +
			"  </filters>\n" +
			"</dhc:dhcpServerConfig>";

		DhcpServerConfig xmlCfg = context.initXml(xml);
		assertNotNull(xmlCfg);

		ByteArrayOutputStream yamlBaos = new ByteArrayOutputStream();
		DhcpServerConfiguration.saveYamlConfig(xmlCfg, yamlBaos);
		String yamlString = yamlBaos.toString(StandardCharsets.UTF_8.name());

		ByteArrayOutputStream jsonBaos = new ByteArrayOutputStream();
		DhcpServerConfiguration.saveJsonConfig(xmlCfg, jsonBaos);
		String jsonString = jsonBaos.toString(StandardCharsets.UTF_8.name());

		// Test cases for compound matching
		DhcpV4Message matchBoth = DhcpV4MessageBuilder.discover()
				.withMac("00:04:f2:11:22:33")
				.withVendorClass("PolycomSoundPoint")
				.withHostName("polycom-phone-101")
				.build();

		DhcpV4Message matchVendorOnly = DhcpV4MessageBuilder.discover()
				.withMac("00:11:22:33:44:55")
				.withVendorClass("PolycomSoundPoint")
				.withHostName("other-device")
				.build();

		DhcpV4Message matchHostOnly = DhcpV4MessageBuilder.discover()
				.withMac("00:04:f2:99:88:77")
				.withVendorClass("OtherPhone")
				.withHostName("polycom-phone-102")
				.build();

		// Check XML
		Filter fXml = DhcpServerConfiguration.getInstance().getGlobalFilters().getFilterList().get(0);
		assertTrue("XML: Must match both", DhcpServerConfiguration.msgMatchesFilter(matchBoth, fXml));
		assertFalse("XML: Must reject vendor only", DhcpServerConfiguration.msgMatchesFilter(matchVendorOnly, fXml));
		assertFalse("XML: Must reject hostname only", DhcpServerConfiguration.msgMatchesFilter(matchHostOnly, fXml));

		// Check YAML
		context.initYaml(yamlString);
		Filter fYaml = DhcpServerConfiguration.getInstance().getGlobalFilters().getFilterList().get(0);
		assertTrue("YAML: Must match both", DhcpServerConfiguration.msgMatchesFilter(matchBoth, fYaml));
		assertFalse("YAML: Must reject vendor only", DhcpServerConfiguration.msgMatchesFilter(matchVendorOnly, fYaml));
		assertFalse("YAML: Must reject hostname only", DhcpServerConfiguration.msgMatchesFilter(matchHostOnly, fYaml));

		// Check JSON
		context.initJson(jsonString);
		Filter fJson = DhcpServerConfiguration.getInstance().getGlobalFilters().getFilterList().get(0);
		assertTrue("JSON: Must match both", DhcpServerConfiguration.msgMatchesFilter(matchBoth, fJson));
		assertFalse("JSON: Must reject vendor only", DhcpServerConfiguration.msgMatchesFilter(matchVendorOnly, fJson));
		assertFalse("JSON: Must reject hostname only", DhcpServerConfiguration.msgMatchesFilter(matchHostOnly, fJson));
	}

	/**
	 * Verify IPv6 DUID / hexValue opaque data filter parity across XML, YAML, and JSON.
	 */
	@Test
	public void testHexAndOpaqueDataParityAcrossFormats() throws Exception {
		String xml =
			"<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
			"  <filters>\n" +
			"    <filterList>\n" +
			"      <name>SpecificDuidFilter</name>\n" +
			"      <filterExpressions>\n" +
			"        <filterExpressionList>\n" +
			"          <optionExpression code=\"1\" v4=\"false\" operator=\"equals\">\n" +
			"            <opaqueDataOption>\n" +
			"              <opaqueData><hexValue>00010001aabbccdd001122334455</hexValue></opaqueData>\n" +
			"            </opaqueDataOption>\n" +
			"          </optionExpression>\n" +
			"        </filterExpressionList>\n" +
			"      </filterExpressions>\n" +
			"    </filterList>\n" +
			"  </filters>\n" +
			"</dhc:dhcpServerConfig>";

		DhcpServerConfig xmlCfg = context.initXml(xml);
		assertNotNull(xmlCfg);

		ByteArrayOutputStream yamlBaos = new ByteArrayOutputStream();
		DhcpServerConfiguration.saveYamlConfig(xmlCfg, yamlBaos);
		String yamlString = yamlBaos.toString(StandardCharsets.UTF_8.name());

		ByteArrayOutputStream jsonBaos = new ByteArrayOutputStream();
		DhcpServerConfiguration.saveJsonConfig(xmlCfg, jsonBaos);
		String jsonString = jsonBaos.toString(StandardCharsets.UTF_8.name());

		DhcpV6Message matchingV6 = DhcpV6MessageBuilder.solicit()
				.withDuid("00010001aabbccdd001122334455")
				.build();

		DhcpV6Message nonMatchingV6 = DhcpV6MessageBuilder.solicit()
				.withDuid("00010001aabbccdd998877665544")
				.build();

		// Check XML
		Filter fXml = DhcpServerConfiguration.getInstance().getGlobalFilters().getFilterList().get(0);
		assertTrue("XML: DUID matches", DhcpServerConfiguration.msgMatchesFilter(matchingV6, fXml));
		assertFalse("XML: DUID non-match", DhcpServerConfiguration.msgMatchesFilter(nonMatchingV6, fXml));

		// Check YAML
		context.initYaml(yamlString);
		Filter fYaml = DhcpServerConfiguration.getInstance().getGlobalFilters().getFilterList().get(0);
		assertTrue("YAML: DUID matches", DhcpServerConfiguration.msgMatchesFilter(matchingV6, fYaml));
		assertFalse("YAML: DUID non-match", DhcpServerConfiguration.msgMatchesFilter(nonMatchingV6, fYaml));

		// Check JSON
		context.initJson(jsonString);
		Filter fJson = DhcpServerConfiguration.getInstance().getGlobalFilters().getFilterList().get(0);
		assertTrue("JSON: DUID matches", DhcpServerConfiguration.msgMatchesFilter(matchingV6, fJson));
		assertFalse("JSON: DUID non-match", DhcpServerConfiguration.msgMatchesFilter(nonMatchingV6, fJson));
	}

	/**
	 * Verify IPv6 IA_NA pools and linkFilters defined via native YAML allocate properly.
	 */
	@Test
	public void testNativeYamlV6PoolAllocationParity() throws Exception {
		String nativeYamlV6 =
			"links:\n" +
			"  linkList:\n" +
			"    - name: V6YamlLink\n" +
			"      address: 2001:db8:55::/64\n" +
			"      v6NaAddrPools:\n" +
			"        poolList:\n" +
			"          - range: 2001:db8:55::100-2001:db8:55::199\n" +
			"      linkFilters:\n" +
			"        linkFilterList:\n" +
			"          - name: V6EnterpriseFilter\n" +
			"            filterExpressions:\n" +
			"              filterExpressionList:\n" +
			"                - clientClassExpression:\n" +
			"                    operator: equals\n" +
			"                    v6UserClassOption:\n" +
			"                      opaqueDataList:\n" +
			"                        - asciiValue: Enterprise\n" +
			"            v6NaAddrPools:\n" +
			"              poolList:\n" +
			"                - range: 2001:db8:55::10-2001:db8:55::50\n";

		context.initYaml(nativeYamlV6);
		DhcpServerConfiguration config = DhcpServerConfiguration.getInstance();
		assertNotNull(config);

		DhcpLink link = config.getLinkMap().values().iterator().next();
		assertNotNull(link);

		DhcpV6Message enterpriseMsg = DhcpV6MessageBuilder.solicit()
				.withDuid("00010001aabbccdd001122334455")
				.withUserClass("Enterprise")
				.build();

		DhcpV6Message standardMsg = DhcpV6MessageBuilder.solicit()
				.withDuid("00010001aabbccdd009988776655")
				.build();

		V6NaAddrBindingManagerImpl v6Mgr = new V6NaAddrBindingManagerImpl();
		try {
			v6Mgr.setIaMgr(mockIaMgr);
			v6Mgr.init();

			InetAddress entAddr = v6Mgr.getNextFreeAddress(link, enterpriseMsg);
			assertNotNull("Enterprise client must allocate from filtered pool", entAddr);
			DhcpAssert.assertIpInRange(entAddr, "2001:db8:55::10", "2001:db8:55::50");

			InetAddress stdAddr = v6Mgr.getNextFreeAddress(link, standardMsg);
			assertNotNull("Standard client must allocate from default pool", stdAddr);
			DhcpAssert.assertIpInRange(stdAddr, "2001:db8:55::100", "2001:db8:55::199");
		} finally {
			v6Mgr.close();
		}
	}
}
