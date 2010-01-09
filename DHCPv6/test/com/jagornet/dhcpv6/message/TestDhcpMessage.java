package com.jagornet.dhcpv6.message;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.jagornet.dhcpv6.option.DhcpClientIdOption;
import com.jagornet.dhcpv6.option.DhcpDnsServersOption;
import com.jagornet.dhcpv6.option.DhcpOptionRequestOption;
import com.jagornet.dhcpv6.option.DhcpServerIdOption;
import com.jagornet.dhcpv6.option.DhcpUserClassOption;
import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.ServerIdOption;

public class TestDhcpMessage extends TestCase
{
    // the address of the client itself
    public static InetSocketAddress CLIENT_ADDR =
        new InetSocketAddress("2001:DB8::A", DhcpConstants.CLIENT_PORT);

    public static String DNS1 = "2001:DB8:1::1";
    public static String DNS2 = "2001:DB8:1::2";
    public static String DNS3 = "2001:DB8:1::3";
    
    public static DhcpMessage buildMockDhcpMessage()
    {
        DhcpMessage dhcpMessage = 
        	new DhcpMessage(new InetSocketAddress(DhcpConstants.SERVER_PORT),
        			new InetSocketAddress(DhcpConstants.CLIENT_PORT));
        dhcpMessage.setMessageType(DhcpConstants.REPLY);    // 1 byte
        dhcpMessage.setTransactionId(90599);                // 3 bytes

        OpaqueData opaque = OpaqueData.Factory.newInstance();
        opaque.setAsciiValue("jagornet-dhcpv6");	// 15 bytes

        // MUST include server id in reply
        ServerIdOption serverId = ServerIdOption.Factory.newInstance();     // 4 bytes (code + len)
        serverId.setOpaqueData(opaque);
        dhcpMessage.putDhcpOption(new DhcpServerIdOption(serverId));

        DhcpDnsServersOption dnsServers = new DhcpDnsServersOption();	// 4 bytes
        dnsServers.getIpAddressListOption().addIpAddress(DNS1);		// 16 bytes
        dnsServers.getIpAddressListOption().addIpAddress(DNS2);		// 16 bytes
        dnsServers.getIpAddressListOption().addIpAddress(DNS3);		// 16 bytes
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
        DhcpMessage dhcpMessage = buildMockDhcpMessage();
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

    public static void checkMockClientRequest(DhcpMessage dhcpMessage)
    {
        assertEquals(CLIENT_ADDR.getAddress(), dhcpMessage.getRemoteAddress().getAddress());
        assertEquals(DhcpConstants.INFO_REQUEST, dhcpMessage.getMessageType());
        assertEquals(Long.parseLong("FFFFFF", 16), dhcpMessage.getTransactionId());
        Map<Integer, DhcpOption> options = dhcpMessage.getDhcpOptionMap();
        assertNotNull(options);
        DhcpClientIdOption clientId = 
            (DhcpClientIdOption)options.get(DhcpConstants.OPTION_CLIENTID);
        assertNotNull(clientId);
        assertEquals("MyClientId", clientId.getOpaqueDataOptionType().getOpaqueData().getAsciiValue());
        DhcpOptionRequestOption oro =
            (DhcpOptionRequestOption)options.get(DhcpConstants.OPTION_ORO);
        assertNotNull(oro);
        assertEquals(Integer.valueOf(DhcpConstants.OPTION_DNS_SERVERS), 
        		oro.getUnsignedShortListOption().getUnsignedShortList().get(0)); 
        DhcpUserClassOption userClass =
            (DhcpUserClassOption)options.get(DhcpConstants.OPTION_USER_CLASS);
        assertNotNull(userClass);
        assertEquals("UserClass 1",
                userClass.getOpaqueDataListOptionType().getOpaqueDataList().get(0).getAsciiValue());
        assertEquals("UserClass 2",
                userClass.getOpaqueDataListOptionType().getOpaqueDataList().get(1).getAsciiValue());
        
    }
    
    public void testDecode() throws Exception
    {
        ByteBuffer bb = buildMockClientRequest();
        int len = bb.limit();
        DhcpMessage dhcpMessage = 
        	new DhcpMessage(new InetSocketAddress(DhcpConstants.SERVER_PORT),
        					CLIENT_ADDR);
        dhcpMessage.decode(bb);
        assertNotNull(dhcpMessage);
        assertEquals(len, dhcpMessage.getLength());
        checkMockClientRequest(dhcpMessage);
    }
    
    public  void testEquals() throws Exception
    {
    	DhcpMessage msg1 = 
    		new DhcpMessage(new InetSocketAddress(DhcpConstants.SERVER_PORT), CLIENT_ADDR);
    	msg1.setTransactionId(12345);
    	msg1.setMessageType((short)1);
    	DhcpClientIdOption c1 = new DhcpClientIdOption();
    	c1.getOpaqueDataOptionType().getOpaqueData().setHexValue(
    			new byte[] { (byte)0xde, (byte)0xbb, (byte)0x1e, (byte)0xde, (byte)0xbb, (byte)0x1e });
    	msg1.putDhcpOption(c1);

    	DhcpMessage msg2 = 
    		new DhcpMessage(new InetSocketAddress(DhcpConstants.SERVER_PORT), CLIENT_ADDR);
    	msg2.setTransactionId(12345);
    	msg2.setMessageType((short)1);
    	DhcpClientIdOption c2 = new DhcpClientIdOption();
    	c2.getOpaqueDataOptionType().getOpaqueData().setHexValue(
    			new byte[] { (byte)0xde, (byte)0xbb, (byte)0x1e, (byte)0xde, (byte)0xbb, (byte)0x1e });
    	msg2.putDhcpOption(c2);
    	
    	assertEquals(msg1, msg2);
    }
    
    public void testSet() throws Exception
    {
    	DhcpMessage msg1 = 
    		new DhcpMessage(new InetSocketAddress(DhcpConstants.SERVER_PORT), CLIENT_ADDR);
    	msg1.setTransactionId(12345);
    	msg1.setMessageType((short)1);
    	DhcpClientIdOption c1 = new DhcpClientIdOption();
    	c1.getOpaqueDataOptionType().getOpaqueData().setHexValue(
    			new byte[] { (byte)0xde, (byte)0xbb, (byte)0x1e, (byte)0xde, (byte)0xbb, (byte)0x1e });
    	msg1.putDhcpOption(c1);
//    	System.out.println("msg1.hash=" + msg1.hashCode());

    	DhcpMessage msg2 = 
    		new DhcpMessage(new InetSocketAddress(DhcpConstants.SERVER_PORT), CLIENT_ADDR);
    	msg2.setTransactionId(12345);
    	msg2.setMessageType((short)1);
    	DhcpClientIdOption c2 = new DhcpClientIdOption();
    	c2.getOpaqueDataOptionType().getOpaqueData().setHexValue(
    			new byte[] { (byte)0xde, (byte)0xbb, (byte)0x1e, (byte)0xde, (byte)0xbb, (byte)0x1e });
    	msg2.putDhcpOption(c2);
//    	System.out.println("msg2.hash=" + msg2.hashCode());
    	
    	Set<DhcpMessage> s = new HashSet<DhcpMessage>();
    	s.add(msg1);
    	
    	assertTrue(s.contains(msg2));
    }
}
