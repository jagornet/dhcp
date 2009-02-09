package com.jagornet.dhcpv6.message;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.mina.core.buffer.IoBuffer;

import com.jagornet.dhcpv6.xml.ServerIdOption;
import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpClientIdOption;
import com.jagornet.dhcpv6.option.DhcpDnsServersOption;
import com.jagornet.dhcpv6.option.DhcpOption;
import com.jagornet.dhcpv6.option.DhcpOptionRequestOption;
import com.jagornet.dhcpv6.option.DhcpServerIdOption;
import com.jagornet.dhcpv6.option.DhcpUserClassOption;
import com.jagornet.dhcpv6.util.DhcpConstants;

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
        ServerIdOption serverId = ServerIdOption.Factory.newInstance();     // 4 bytes (code + len)
        serverId.setAsciiValue("AGR-DHCPv6");               // 10 bytes
        dhcpMessage.setOption(new DhcpServerIdOption(serverId));

        DhcpDnsServersOption dnsServers = new DhcpDnsServersOption();   // 4 bytes
        dnsServers.addServer(DNS1);                 // 16 bytes
        dnsServers.addServer(DNS2);                 // 16 bytes
        dnsServers.addServer(DNS3);                 // 16 bytes
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
        ByteBuffer bb = dhcpMessage.encode().buf();
        assertNotNull(bb);
        assertEquals(70, bb.limit());
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
        assertEquals(CLIENT_ADDR, dhcpMessage.getSocketAddress());
        assertEquals(DhcpConstants.INFO_REQUEST, dhcpMessage.getMessageType());
        assertEquals(Long.parseLong("FFFFFF", 16), dhcpMessage.getTransactionId());
        Map<Integer, DhcpOption> options = dhcpMessage.getDhcpOptions();
        assertNotNull(options);
        DhcpClientIdOption clientId = 
            (DhcpClientIdOption)options.get(DhcpConstants.OPTION_CLIENTID);
        assertNotNull(clientId);
        assertEquals("MyClientId", clientId.getClientIdOption().getAsciiValue());
        DhcpOptionRequestOption oro =
            (DhcpOptionRequestOption)options.get(DhcpConstants.OPTION_ORO);
        assertNotNull(oro);
        assertEquals(DhcpConstants.OPTION_DNS_SERVERS, oro.getOptionRequestOption().getRequestedOptionCodesArray()[0]); 
        DhcpUserClassOption userClass =
            (DhcpUserClassOption)options.get(DhcpConstants.OPTION_USER_CLASS);
        assertNotNull(userClass);
        assertEquals("UserClass 1",
                userClass.getUserClassOption().getUserClassesArray()[0].getAsciiValue());
        assertEquals("UserClass 2",
                userClass.getUserClassOption().getUserClassesArray()[1].getAsciiValue());
        
    }
    
    public void testDecode() throws Exception
    {
        ByteBuffer bb = buildMockClientRequest();
        int len = bb.limit();
        DhcpMessage dhcpMessage = DhcpMessage.decode(CLIENT_ADDR, IoBuffer.wrap(bb));
        assertNotNull(dhcpMessage);
        assertEquals(len, dhcpMessage.getLength());
        checkMockClientRequest(dhcpMessage);
    }
}
