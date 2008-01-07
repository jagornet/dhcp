package com.agr.dhcpv6.message;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;

import junit.framework.TestCase;

import com.agr.dhcpv6.option.DhcpClientIdOption;
import com.agr.dhcpv6.option.DhcpDnsServersOption;
import com.agr.dhcpv6.option.DhcpOption;
import com.agr.dhcpv6.option.DhcpOptionRequestOption;
import com.agr.dhcpv6.option.DhcpServerIdOption;
import com.agr.dhcpv6.option.DhcpUserClassOption;
import com.agr.dhcpv6.server.config.xml.ServerIdOption;
import com.agr.dhcpv6.util.DhcpConstants;

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
        DhcpMessage dhcpMessage = new DhcpMessage();
        dhcpMessage.setMessageType(DhcpConstants.REPLY);    // 1 byte
        dhcpMessage.setTransactionId(90599);                // 3 bytes

        // MUST include server id in reply
        ServerIdOption serverId = new ServerIdOption();     // 4 bytes (code + len)
        serverId.setAsciiValue("AGR-DHCPv6");               // 10 bytes
        dhcpMessage.setOption(new DhcpServerIdOption(serverId));

        DhcpDnsServersOption dnsServers = new DhcpDnsServersOption();   // 4 bytes
        dnsServers.addDnsServer(DNS1);                 // 16 bytes
        dnsServers.addDnsServer(DNS2);                 // 16 bytes
        dnsServers.addDnsServer(DNS3);                 // 16 bytes
        dhcpMessage.setOption(dnsServers);
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
        assertEquals(10, bb.getShort());
        byte[] b = new byte[10];
        bb.get(b);
        assertEquals("AGR-DHCPv6", new String(b));
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
        assertEquals(70, bb.limit());
        checkEncodedMockDhcpMessage(bb);
        assertEquals(false, bb.hasRemaining());
    }

    public static ByteBuffer buildMockClientRequest()
    {
        ByteBuffer bb = ByteBuffer.allocate(1024);
        bb.put(DhcpConstants.INFO_REQUEST);
        bb.put(DhcpTransactionId.encode(90599));
        // client id option
        bb.putShort(DhcpConstants.OPTION_CLIENTID);
        bb.putShort((short)10);
        bb.put("MyClientId".getBytes());
        // option request option
        bb.putShort(DhcpConstants.OPTION_ORO);
        bb.putShort((short)2);
        bb.putShort(DhcpConstants.OPTION_DNS_SERVERS);
        // user class option
        bb.putShort(DhcpConstants.OPTION_USER_CLASS);
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
        assertEquals(CLIENT_ADDR, dhcpMessage.getSocketAddress());
        assertEquals(DhcpConstants.INFO_REQUEST, dhcpMessage.getMessageType());
        assertEquals(90599, dhcpMessage.getTransactionId());
        Map<Short, DhcpOption> options = dhcpMessage.getDhcpOptions();
        assertNotNull(options);
        DhcpClientIdOption clientId = 
            (DhcpClientIdOption)options.get(DhcpConstants.OPTION_CLIENTID);
        assertNotNull(clientId);
        assertEquals("MyClientId", clientId.getClientIdOption().getAsciiValue());
        DhcpOptionRequestOption oro =
            (DhcpOptionRequestOption)options.get(DhcpConstants.OPTION_ORO);
        assertNotNull(oro);
        assertEquals(DhcpConstants.OPTION_DNS_SERVERS, 
                (short)oro.getOptionRequestOption().getRequestedOptionCodes().get(0));
        DhcpUserClassOption userClass =
            (DhcpUserClassOption)options.get(DhcpConstants.OPTION_USER_CLASS);
        assertNotNull(userClass);
        assertEquals("UserClass 1",
                userClass.getUserClassOption().getUserClasses().get(0).getAsciiValue());
        assertEquals("UserClass 2",
                userClass.getUserClassOption().getUserClasses().get(1).getAsciiValue());
        
    }
    
    public void testDecode() throws Exception
    {
        ByteBuffer bb = buildMockClientRequest();
        int len = bb.limit();
        DhcpMessage dhcpMessage = DhcpMessage.decode(CLIENT_ADDR, bb);
        assertNotNull(dhcpMessage);
        assertEquals(len, dhcpMessage.getLength());
        checkMockClientRequest(dhcpMessage);
    }
}
