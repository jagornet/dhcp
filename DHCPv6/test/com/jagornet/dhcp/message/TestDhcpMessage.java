package com.jagornet.dhcp.message;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.jagornet.dhcp.message.DhcpV6Message;
import com.jagornet.dhcp.option.base.DhcpOption;
import com.jagornet.dhcp.option.v6.DhcpV6ClientIdOption;
import com.jagornet.dhcp.option.v6.DhcpV6DnsServersOption;
import com.jagornet.dhcp.option.v6.DhcpV6OptionRequestOption;
import com.jagornet.dhcp.option.v6.DhcpV6ServerIdOption;
import com.jagornet.dhcp.option.v6.DhcpV6UserClassOption;
import com.jagornet.dhcp.util.DhcpConstants;
import com.jagornet.dhcp.xml.OpaqueData;
import com.jagornet.dhcp.xml.V6ServerIdOption;

public class TestDhcpMessage extends TestCase
{
    // the address of the client itself
    public static InetSocketAddress CLIENT_ADDR =
        new InetSocketAddress("2001:DB8::A", DhcpConstants.CLIENT_PORT);

    public static String DNS1 = "2001:DB8:1::1";
    public static String DNS2 = "2001:DB8:1::2";
    public static String DNS3 = "2001:DB8:1::3";
    
    public static DhcpV6Message buildMockDhcpMessage()
    {
        DhcpV6Message dhcpMessage = 
        	new DhcpV6Message(new InetSocketAddress(DhcpConstants.SERVER_PORT),
        			new InetSocketAddress(DhcpConstants.CLIENT_PORT));
        dhcpMessage.setMessageType(DhcpConstants.REPLY);    // 1 byte
        dhcpMessage.setTransactionId(90599);                // 3 bytes

        OpaqueData opaque = OpaqueData.Factory.newInstance();
        opaque.setAsciiValue("jagornet-dhcpv6");	// 15 bytes

        // MUST include server id in reply
        V6ServerIdOption serverId = V6ServerIdOption.Factory.newInstance();     // 4 bytes (code + len)
        serverId.setOpaqueData(opaque);
        dhcpMessage.putDhcpOption(new DhcpV6ServerIdOption(serverId));

        DhcpV6DnsServersOption dnsServers = new DhcpV6DnsServersOption();	// 4 bytes
        dnsServers.addIpAddress(DNS1);		// 16 bytes
        dnsServers.addIpAddress(DNS2);		// 16 bytes
        dnsServers.addIpAddress(DNS3);		// 16 bytes
        dhcpMessage.putDhcpOption(dnsServers);
        return dhcpMessage;
    }
    
    public static void checkEncodedMockDhcpMessage(ByteBuffer bb) throws Exception
    {
        // message type
        assertEquals(DhcpConstants.REPLY, bb.get());
        // transaction id
        assertEquals((byte)0x01, bb.get());
        assertEquals((byte)0x61, bb.get());
        assertEquals((byte)0xe7, bb.get());
        // server id option
        assertEquals(DhcpConstants.OPTION_SERVERID, bb.getShort());
        assertEquals(15, bb.getShort());
        byte[] b = new byte[15];
        bb.get(b);
        assertEquals("jagornet-dhcpv6", new String(b));
        // dns servers option
        assertEquals(DhcpConstants.OPTION_DNS_SERVERS, bb.getShort());
        assertEquals(48, bb.getShort());
        b = new byte[16];
        bb.get(b);
        assertEquals(InetAddress.getByName(DNS1), InetAddress.getByAddress(b));
        bb.get(b);
        assertEquals(InetAddress.getByName(DNS2), InetAddress.getByAddress(b));
        bb.get(b);
        assertEquals(InetAddress.getByName(DNS3), InetAddress.getByAddress(b));
    }
    
    public void testEncode() throws Exception
    {
        DhcpV6Message dhcpMessage = buildMockDhcpMessage();
        ByteBuffer bb = dhcpMessage.encode();
        assertNotNull(bb);
        assertEquals(75, bb.limit());
        checkEncodedMockDhcpMessage(bb);
        assertEquals(false, bb.hasRemaining());
    }

    public static ByteBuffer buildMockClientRequest()
    {
        ByteBuffer bb = ByteBuffer.allocate(1024);
        bb.put((byte)DhcpConstants.INFO_REQUEST);
        bb.put(new byte[] { (byte)0xff, (byte)0xff, (byte)0xff });
        // client id option
        bb.putShort((short)DhcpConstants.OPTION_CLIENTID);
        bb.putShort((short)10);
        bb.put("MyClientId".getBytes());
        // option request option
        bb.putShort((short)DhcpConstants.OPTION_ORO);
        bb.putShort((short)2);
        bb.putShort((short)DhcpConstants.OPTION_DNS_SERVERS);
        // user class option
        bb.putShort((short)DhcpConstants.OPTION_USER_CLASS);
        bb.putShort((short)26);
        bb.putShort((short)11);
        bb.put("UserClass 1".getBytes());
        bb.putShort((short)11);
        bb.put("UserClass 2".getBytes());
        bb.flip();
        return bb;
    }

    public static void checkMockClientRequest(DhcpV6Message dhcpMessage)
    {
        assertEquals(CLIENT_ADDR.getAddress(), dhcpMessage.getRemoteAddress().getAddress());
        assertEquals(DhcpConstants.INFO_REQUEST, dhcpMessage.getMessageType());
        assertEquals(Long.parseLong("FFFFFF", 16), dhcpMessage.getTransactionId());
        Map<Integer, DhcpOption> options = dhcpMessage.getDhcpOptionMap();
        assertNotNull(options);
        DhcpV6ClientIdOption clientId = 
            (DhcpV6ClientIdOption)options.get(DhcpConstants.OPTION_CLIENTID);
        assertNotNull(clientId);
        assertEquals("MyClientId", clientId.getOpaqueData().getAscii());
        DhcpV6OptionRequestOption oro =
            (DhcpV6OptionRequestOption)options.get(DhcpConstants.OPTION_ORO);
        assertNotNull(oro);
        assertEquals(Integer.valueOf(DhcpConstants.OPTION_DNS_SERVERS), 
        		oro.getUnsignedShortList().get(0)); 
        DhcpV6UserClassOption userClass =
            (DhcpV6UserClassOption)options.get(DhcpConstants.OPTION_USER_CLASS);
        assertNotNull(userClass);
        assertEquals("UserClass 1",
                userClass.getOpaqueDataList().get(0).getAscii());
        assertEquals("UserClass 2",
                userClass.getOpaqueDataList().get(1).getAscii());
        
    }
    
    public void testDecode() throws Exception
    {
        ByteBuffer bb = buildMockClientRequest();
        int len = bb.limit();
        DhcpV6Message dhcpMessage = 
        	new DhcpV6Message(new InetSocketAddress(DhcpConstants.SERVER_PORT),
        					CLIENT_ADDR);
        dhcpMessage.decode(bb);
        assertNotNull(dhcpMessage);
        assertEquals(len, dhcpMessage.getLength());
        checkMockClientRequest(dhcpMessage);
    }
    
    public  void testEquals() throws Exception
    {
    	DhcpV6Message msg1 = 
    		new DhcpV6Message(new InetSocketAddress(DhcpConstants.SERVER_PORT), CLIENT_ADDR);
    	msg1.setTransactionId(12345);
    	msg1.setMessageType((short)1);
    	DhcpV6ClientIdOption c1 = new DhcpV6ClientIdOption();
    	c1.getOpaqueData().setHex(
    			new byte[] { (byte)0xde, (byte)0xbb, (byte)0x1e, (byte)0xde, (byte)0xbb, (byte)0x1e });
    	msg1.putDhcpOption(c1);

    	DhcpV6Message msg2 = 
    		new DhcpV6Message(new InetSocketAddress(DhcpConstants.SERVER_PORT), CLIENT_ADDR);
    	msg2.setTransactionId(12345);
    	msg2.setMessageType((short)1);
    	DhcpV6ClientIdOption c2 = new DhcpV6ClientIdOption();
    	c2.getOpaqueData().setHex(
    			new byte[] { (byte)0xde, (byte)0xbb, (byte)0x1e, (byte)0xde, (byte)0xbb, (byte)0x1e });
    	msg2.putDhcpOption(c2);
    
// This was testing equality of message for cheap DOS detection, but we got rid of that
//    	assertEquals(msg1, msg2);
    }
    
    public void testSet() throws Exception
    {
    	DhcpV6Message msg1 = 
    		new DhcpV6Message(new InetSocketAddress(DhcpConstants.SERVER_PORT), CLIENT_ADDR);
    	msg1.setTransactionId(12345);
    	msg1.setMessageType((short)1);
    	DhcpV6ClientIdOption c1 = new DhcpV6ClientIdOption();
    	c1.getOpaqueData().setHex(
    			new byte[] { (byte)0xde, (byte)0xbb, (byte)0x1e, (byte)0xde, (byte)0xbb, (byte)0x1e });
    	msg1.putDhcpOption(c1);
//    	System.out.println("msg1.hash=" + msg1.hashCode());

    	DhcpV6Message msg2 = 
    		new DhcpV6Message(new InetSocketAddress(DhcpConstants.SERVER_PORT), CLIENT_ADDR);
    	msg2.setTransactionId(12345);
    	msg2.setMessageType((short)1);
    	DhcpV6ClientIdOption c2 = new DhcpV6ClientIdOption();
    	c2.getOpaqueData().setHex(
    			new byte[] { (byte)0xde, (byte)0xbb, (byte)0x1e, (byte)0xde, (byte)0xbb, (byte)0x1e });
    	msg2.putDhcpOption(c2);
//    	System.out.println("msg2.hash=" + msg2.hashCode());
    	
    	Set<DhcpV6Message> s = new HashSet<DhcpV6Message>();
    	s.add(msg1);
    	
// This was testing equality of message for cheap DOS detection, but we got rid of that
//    	assertTrue(s.contains(msg2));
    }
}
