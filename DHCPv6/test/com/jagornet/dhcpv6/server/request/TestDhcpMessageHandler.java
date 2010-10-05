package com.jagornet.dhcpv6.server.request;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import junit.framework.TestCase;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.message.DhcpRelayMessage;
import com.jagornet.dhcpv6.message.TestDhcpRelayMessage;
import com.jagornet.dhcpv6.option.DhcpClientIdOption;
import com.jagornet.dhcpv6.option.DhcpInterfaceIdOption;
import com.jagornet.dhcpv6.option.DhcpRelayOption;
import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.xml.ClientIdOption;
import com.jagornet.dhcpv6.xml.OpaqueData;

public class TestDhcpMessageHandler extends TestCase
{
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		DhcpServerConfiguration.configFilename = "test/dhcpv6server-sample.xml";
		DhcpServerConfiguration.getInstance();
		
	}
	
	public void testHandleMessage() throws Exception
	{
		DhcpRelayMessage relayMessage = TestDhcpRelayMessage.buildMockDhcpRelayMessage();
		
		// change the mock relay_reply message to a relay_forward
		relayMessage.setMessageType(DhcpConstants.RELAY_FORW);
		
        DhcpMessage dhcpMessage = 
        	new DhcpMessage(new InetSocketAddress("2001:db8:1::1", DhcpConstants.SERVER_PORT),
        			new InetSocketAddress("fe80::1", DhcpConstants.CLIENT_PORT));
        dhcpMessage.setMessageType(DhcpConstants.INFO_REQUEST);    // 1 byte
        dhcpMessage.setTransactionId(90599);                // 3 bytes

        OpaqueData opaque = OpaqueData.Factory.newInstance();
        opaque.setAsciiValue("jagornet-dhcpv6");	// 15 bytes

        ClientIdOption clientId = ClientIdOption.Factory.newInstance();
        clientId.setOpaqueData(opaque);
        dhcpMessage.putDhcpOption(new DhcpClientIdOption(clientId));
        
        // replace the reply message with a client info-request message
        DhcpRelayOption relayOption = relayMessage.getRelayOption();
        relayOption.setDhcpMessage(dhcpMessage);
        relayMessage.setRelayOption(relayOption);
        
		relayMessage.putDhcpOption(TestDhcpRelayMessage.buildMockInterfaceIdOption());
		DhcpMessage replyMessage = 
			DhcpMessageHandler.handleMessage(InetAddress.getByName("3ffe::1"), relayMessage);
		DhcpInterfaceIdOption ifIdOption = 
			(DhcpInterfaceIdOption) replyMessage.getDhcpOption(DhcpConstants.OPTION_INTERFACE_ID);
		assertNotNull(ifIdOption);
	}
}
