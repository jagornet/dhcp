package com.jagornet.dhcp.message;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import junit.framework.TestCase;

import com.jagornet.dhcp.message.DhcpV6Message;
import com.jagornet.dhcp.message.DhcpV6RelayMessage;
import com.jagornet.dhcp.option.v6.DhcpV6InterfaceIdOption;
import com.jagornet.dhcp.option.v6.DhcpV6RelayOption;
import com.jagornet.dhcp.util.DhcpConstants;
import com.jagornet.dhcp.xml.OpaqueData;
import com.jagornet.dhcp.xml.V6InterfaceIdOption;

public class TestDhcpRelayMessage extends TestCase
{
	public static InetSocketAddress LOCALHOST_ADDR =
		new InetSocketAddress("::1", DhcpConstants.SERVER_PORT);
    // the relay closest to the client
    public static InetSocketAddress RELAY1_CLIENTSIDE_ADDR =
        new InetSocketAddress("2001:DB8::1", DhcpConstants.SERVER_PORT);
    public static InetSocketAddress RELAY1_ADDR =
        new InetSocketAddress("2001:DB8:1::1", DhcpConstants.SERVER_PORT);
    // when used, the relay closest to the server
    public static InetSocketAddress RELAY2_RELAY1SIDE_ADDR =
        new InetSocketAddress("2001:DB8:1::2", DhcpConstants.SERVER_PORT);
    public static InetSocketAddress RELAY2_ADDR =
        new InetSocketAddress("2001:DB8:2::1", DhcpConstants.SERVER_PORT);
    
    public static DhcpV6RelayMessage buildMockDhcpRelayMessage() throws Exception
    {
        DhcpV6RelayMessage relayMessage = new DhcpV6RelayMessage(LOCALHOST_ADDR, RELAY1_ADDR);
        relayMessage.setMessageType(DhcpConstants.RELAY_REPL);
        relayMessage.setHopCount((byte)0);
        relayMessage.setLinkAddress(RELAY1_CLIENTSIDE_ADDR.getAddress());
        relayMessage.setPeerAddress(TestDhcpMessage.CLIENT_ADDR.getAddress());
        
        // build a client info-request message
        DhcpV6Message dhcpMessage = TestDhcpMessage.buildMockDhcpMessage();
        // create relay message option - logically the encapsulated message
        // comes from the client, i.e. peer address, so use that as the source
        DhcpV6RelayOption relayOption = new DhcpV6RelayOption();
        // the client's request is contained within the relay option
        relayOption.setDhcpMessage(dhcpMessage);
        // add the relay message option to the relay message
        relayMessage.putDhcpOption(relayOption);
        relayMessage.setRelayOption(relayOption);
        
        return relayMessage;
    }
    
    public static DhcpV6InterfaceIdOption buildMockInterfaceIdOption()
    {
        V6InterfaceIdOption ifIdOption = V6InterfaceIdOption.Factory.newInstance();
        OpaqueData opaque = OpaqueData.Factory.newInstance();
        opaque.setHexValue(new byte[] { 0x01, 0x02, 0x03, 0x04 } );
    	ifIdOption.setOpaqueData(opaque);
    	return new DhcpV6InterfaceIdOption(ifIdOption);
    }

    public static void checkEncodedMockDhcpRelayMessage(ByteBuffer bb) throws Exception
    {
        // message type
        assertEquals(DhcpConstants.RELAY_REPL, bb.get());
        // hop count
        assertEquals(0, bb.get());
        byte[] b = new byte[16];
        bb.get(b);
        // link addr
        assertEquals(RELAY1_CLIENTSIDE_ADDR.getAddress(), 
                     InetAddress.getByAddress(b));
        bb.get(b);
        // peer addr
        assertEquals(TestDhcpMessage.CLIENT_ADDR.getAddress(), 
                     InetAddress.getByAddress(b));
        // relayed message
        assertEquals(DhcpConstants.OPTION_RELAY_MSG, bb.getShort());
        assertEquals(75, bb.getShort());
        TestDhcpMessage.checkEncodedMockDhcpMessage(bb);        
    }
    
    public void testEncode() throws Exception
    {
        DhcpV6RelayMessage relayMessage = buildMockDhcpRelayMessage();

        ByteBuffer bb  = relayMessage.encode();
        assertNotNull(bb);
        // length = 34(relay message header) + 2 (relay option code)
        //          2 (relay option length) + 75 (relay message option)
        assertEquals(34+4+75, bb.limit());
        
        checkEncodedMockDhcpRelayMessage(bb);

        assertEquals(false, bb.hasRemaining());
    }
    
    public void testEncode2() throws Exception
    {
        DhcpV6RelayMessage relayMessage = buildMockDhcpRelayMessage();        
        // simulate another relay - logically, we receive it from the RELAY2_ADDR
        DhcpV6RelayMessage relayMessage2 = new DhcpV6RelayMessage(LOCALHOST_ADDR, RELAY2_ADDR);
        relayMessage2.setMessageType(DhcpConstants.RELAY_REPL);
        relayMessage2.setHopCount((byte)1);
        // the link address could be zero(0), but logically, it is the address
        // of the interface connected to the link where relay 1 is located
        relayMessage2.setLinkAddress(RELAY2_RELAY1SIDE_ADDR.getAddress());
        // this relay agent's peer is relay 1
        relayMessage2.setPeerAddress(RELAY1_ADDR.getAddress());

        // put the first relay message in this relay message's relay message option
        // logically, the encapsulated relay message comes from relay 1, so
        // use that as the source address of the message in the relay option
        DhcpV6RelayOption relayOption2 = new DhcpV6RelayOption();
        relayOption2.setDhcpMessage(relayMessage);
        
        relayMessage2.putDhcpOption(relayOption2);
        
        ByteBuffer bb = relayMessage2.encode();
        assertNotNull(bb);
        // length = 2 x (34(relay message header) + 2 (relay option code)
        //          2 (relay option length)) + 75 (relay message option)
        assertEquals((2*(34+4))+75, bb.limit());
        
        // message type
        assertEquals(DhcpConstants.RELAY_REPL, bb.get());
        // hop count
        assertEquals(1, bb.get());
        byte[] b = new byte[16];
        bb.get(b);
        // link addr
        assertEquals(RELAY2_RELAY1SIDE_ADDR.getAddress(), 
                     InetAddress.getByAddress(b));
        bb.get(b);
        // peer addr
        assertEquals(RELAY1_ADDR.getAddress(), 
                     InetAddress.getByAddress(b));
        // relayed message
        assertEquals(DhcpConstants.OPTION_RELAY_MSG, bb.getShort());
        assertEquals(34+4+75, bb.getShort());
        checkEncodedMockDhcpRelayMessage(bb);        
    }
    
    public static ByteBuffer buildMockRelayFoward() throws Exception
    {
        ByteBuffer bb = ByteBuffer.allocate(1024);
        bb.put((byte)DhcpConstants.RELAY_FORW);
        bb.put((byte)0);    // hop count
        bb.put(RELAY1_CLIENTSIDE_ADDR.getAddress().getAddress());   // link addr
        bb.put(TestDhcpMessage.CLIENT_ADDR.getAddress().getAddress());  // peer addr
        bb.putShort((short)DhcpConstants.OPTION_RELAY_MSG);
        ByteBuffer bb2 = TestDhcpMessage.buildMockClientRequest();
        bb.putShort((short)bb2.limit());
        bb.put(bb2);
        bb.flip();
        return bb;
    }
    
    public static void checkMockRelayFoward(DhcpV6RelayMessage relayMessage) throws Exception
    {
        assertEquals(RELAY1_ADDR.getAddress(), relayMessage.remoteAddress.getAddress());
        assertEquals(0, relayMessage.getHopCount());
        assertEquals(RELAY1_CLIENTSIDE_ADDR.getAddress(), 
                     relayMessage.getLinkAddress());
        assertEquals(TestDhcpMessage.CLIENT_ADDR.getAddress(), 
                     relayMessage.getPeerAddress());
        assertNotNull(relayMessage.getDhcpOptionMap());
        assertNotNull(relayMessage.getRelayOption());
        TestDhcpMessage.checkMockClientRequest(relayMessage.getRelayOption().getDhcpMessage());        
    }
    
    public void testDecode() throws Exception
    {
        ByteBuffer bb = buildMockRelayFoward();
        int len = bb.limit();
        DhcpV6RelayMessage relayMessage = (DhcpV6RelayMessage)
        	DhcpV6RelayMessage.decode(bb, LOCALHOST_ADDR, RELAY1_ADDR);
        assertNotNull(relayMessage);
        assertEquals(len, relayMessage.getLength());
        checkMockRelayFoward(relayMessage);
    }
    
    public void testDecode2() throws Exception
    {
        ByteBuffer bb = ByteBuffer.allocate(1024);
        bb.put((byte)DhcpConstants.RELAY_FORW);
        bb.put((byte)1);    // hop count
        bb.put(RELAY2_RELAY1SIDE_ADDR.getAddress().getAddress());   // link addr
        bb.put(RELAY1_ADDR.getAddress().getAddress());              // peer addr
        bb.putShort((short)DhcpConstants.OPTION_RELAY_MSG);
        ByteBuffer bb2 = buildMockRelayFoward();
        bb.putShort((short)bb2.limit());
        bb.put(bb2);
        bb.flip();

        int len = bb.limit();
        DhcpV6RelayMessage relayMessage = (DhcpV6RelayMessage)
        	DhcpV6RelayMessage.decode(bb, LOCALHOST_ADDR, RELAY2_ADDR);
        assertNotNull(relayMessage);
        assertEquals(len, relayMessage.getLength());
        assertEquals(1, relayMessage.getHopCount());
        assertEquals(RELAY2_RELAY1SIDE_ADDR.getAddress(), 
                     relayMessage.getLinkAddress());
        assertEquals(RELAY1_ADDR.getAddress(), 
                     relayMessage.getPeerAddress());
        assertNotNull(relayMessage.getDhcpOptionMap());
        assertNotNull(relayMessage.getRelayOption());

        checkMockRelayFoward((DhcpV6RelayMessage)
                             relayMessage.getRelayOption().getDhcpMessage());
    }
}
