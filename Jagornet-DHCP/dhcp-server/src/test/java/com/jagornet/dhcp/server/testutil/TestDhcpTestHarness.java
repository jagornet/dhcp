/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.testutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaAddrOption;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.xml.DhcpServerConfig;

/**
 * Validates the core test harness components:
 * {@link DhcpV4MessageBuilder}, {@link DhcpV6MessageBuilder},
 * {@link DhcpConfigTestContext}, and {@link DhcpAssert}.
 * 
 * @author A. Gregory Rabil
 */
public class TestDhcpTestHarness {

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
	public void testV4MessageBuilder() {
		DhcpV4Message msg = DhcpV4MessageBuilder.discover()
				.withMac("00:11:22:33:44:55")
				.withVendorClass("Cisco-Phone")
				.withHostName("my-host")
				.withRequestedIp("192.168.1.150")
				.withRequestedOptions(1, 3, 6, 15)
				.build();

		DhcpAssert.assertThat(msg)
				.isNotNull()
				.hasMessageType((short) DhcpConstants.V4MESSAGE_TYPE_DISCOVER)
				.hasChAddr("001122334455")
				.hasOption(DhcpConstants.V4OPTION_VENDOR_CLASS)
				.hasOption(DhcpConstants.V4OPTION_HOSTNAME)
				.hasOption(DhcpConstants.V4OPTION_REQUESTED_IP)
				.hasOption(DhcpConstants.V4OPTION_PARAM_REQUEST_LIST)
				.doesNotHaveOption(DhcpConstants.V4OPTION_TFTP_SERVER_NAME);
	}

	@Test
	public void testV6MessageBuilder() {
		DhcpV6Message msg = DhcpV6MessageBuilder.solicit()
				.withDuid("0001000128ABCDEF001122334455")
				.withUserClass("Engineering")
				.withVendorClass(9, "SwitchDevice")
				.withIaNa(1001)
				.withRequestedOptions(23, 24)
				.build();

		DhcpAssert.assertThat(msg)
				.isNotNull()
				.hasMessageType((short) DhcpConstants.V6MESSAGE_TYPE_SOLICIT)
				.hasOption(DhcpConstants.V6OPTION_CLIENTID)
				.hasOption(DhcpConstants.V6OPTION_USER_CLASS)
				.hasOption(DhcpConstants.V6OPTION_VENDOR_CLASS)
				.hasOption(DhcpConstants.V6OPTION_ORO)
				.hasIaNa()
				.doesNotHaveOption(DhcpConstants.V6OPTION_SERVERID);
	}

	@Test
	public void testV4Assertions_OfferAndAck() {
		DhcpV4Message offer = DhcpV4MessageBuilder.ofType((short) DhcpConstants.V4MESSAGE_TYPE_OFFER)
				.withYiAddr("192.168.1.125")
				.withMac("00-AA-BB-CC-DD-EE")
				.build();

		DhcpAssert.assertThat(offer)
				.isOffer()
				.hasYiAddr("192.168.1.125")
				.hasYiAddrInRange("192.168.1.100", "192.168.1.200")
				.hasChAddr("00AABBCCDDEE");

		DhcpV4Message ack = DhcpV4MessageBuilder.ofType((short) DhcpConstants.V4MESSAGE_TYPE_ACK)
				.withYiAddr("10.0.0.5")
				.build();

		DhcpAssert.assertThat(ack)
				.isAck()
				.hasYiAddr("10.0.0.5");
	}

	@Test
	public void testV6Assertions_AdvertiseWithIaNaAddress() throws Exception {
		DhcpV6Message advertise = DhcpV6MessageBuilder.ofType((short) DhcpConstants.V6MESSAGE_TYPE_ADVERTISE)
				.withIaNa(1)
				.build();

		// Add an IA_ADDR to the IA_NA
		DhcpV6IaAddrOption addrOpt = new DhcpV6IaAddrOption();
		addrOpt.setIpAddress("2001:db8:1::150");
		advertise.getIaNaOptions().get(0).getIaAddrOptions().add(addrOpt);

		DhcpAssert.assertThat(advertise)
				.isAdvertise()
				.hasIaNa()
				.hasIaNaAddressInRange("2001:db8:1::100", "2001:db8:1::200");
	}

	@Test
	public void testContextXmlSnippetInitAndReset() throws Exception {
		String xml = "<dhc:dhcpServerConfig xmlns:dhc=\"http://jagornet.com/dhcp/xml\">\n" +
				"  <policies>\n" +
				"    <policyList>\n" +
				"      <name>dhcp.sendRequestedOptionsOnly</name>\n" +
				"      <value>true</value>\n" +
				"    </policyList>\n" +
				"  </policies>\n" +
				"</dhc:dhcpServerConfig>";

		DhcpServerConfig cfg = context.initXml(xml);
		assertNotNull(cfg);
		assertEquals("true",
				DhcpServerPolicies.globalPolicy(DhcpServerPolicies.Property.DHCP_SEND_REQUESTED_OPTIONS_ONLY));

		// Reset context
		DhcpConfigTestContext.reset();
		assertEquals("false",
				DhcpServerPolicies.globalPolicy(DhcpServerPolicies.Property.DHCP_SEND_REQUESTED_OPTIONS_ONLY));
	}
}
