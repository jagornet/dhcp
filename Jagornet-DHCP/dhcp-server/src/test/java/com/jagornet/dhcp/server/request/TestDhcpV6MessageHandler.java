/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.request;

import static org.junit.Assert.assertNotNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.message.DhcpV6RelayMessage;
import com.jagornet.dhcp.core.message.TestDhcpRelayMessage;
import com.jagornet.dhcp.core.option.v6.DhcpV6ClientIdOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6InterfaceIdOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6RelayOption;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.server.config.OpaqueDataUtil;
import com.jagornet.dhcp.server.config.xml.OpaqueData;
import com.jagornet.dhcp.server.db.BaseTestCase;

public class TestDhcpV6MessageHandler extends BaseTestCase
{
	@BeforeClass
	public static void oneTimeSetUp() throws Exception
	{
		initializeContext();
	}

	@AfterClass
	public static void oneTimeTearDown() throws Exception
	{
		closeContext();
	}

	@Test
	public void testHandleMessage() throws Exception
	{
		DhcpV6RelayMessage relayMessage = TestDhcpRelayMessage.buildMockDhcpRelayMessage();
		
		// change the mock relay_reply message to a relay_forward
		relayMessage.setMessageType(DhcpConstants.V6MESSAGE_TYPE_RELAY_FORW);
		
        DhcpV6Message dhcpMessage = 
        	new DhcpV6Message(new InetSocketAddress("2001:db8:1::1", DhcpConstants.V6_SERVER_PORT),
        			new InetSocketAddress("fe80::1", DhcpConstants.V6_CLIENT_PORT));
        dhcpMessage.setMessageType(DhcpConstants.V6MESSAGE_TYPE_INFO_REQUEST);    // 1 byte
        dhcpMessage.setTransactionId(90599);                // 3 bytes

        OpaqueData opaque = new OpaqueData();
        opaque.setAsciiValue("jagornet-dhcp-server");	// 20 bytes

        dhcpMessage.putDhcpOption(new DhcpV6ClientIdOption(OpaqueDataUtil.toBaseOpaqueData(opaque)));
        
        // replace the reply message with a client info-request message
        DhcpV6RelayOption relayOption = relayMessage.getRelayOption();
        relayOption.setDhcpMessage(dhcpMessage);
        relayMessage.setRelayOption(relayOption);
        
		relayMessage.putDhcpOption(TestDhcpRelayMessage.buildMockInterfaceIdOption());
		DhcpV6Message replyMessage = 
			DhcpV6MessageHandler.handleMessage(InetAddress.getByName("3ffe::1"), relayMessage);
		DhcpV6InterfaceIdOption ifIdOption = 
			(DhcpV6InterfaceIdOption) replyMessage.getDhcpOption(DhcpConstants.V6OPTION_INTERFACE_ID);
		assertNotNull(ifIdOption);
	}
}
