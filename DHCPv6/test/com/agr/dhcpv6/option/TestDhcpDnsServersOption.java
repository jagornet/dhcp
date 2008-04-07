package com.agr.dhcpv6.option;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.mina.common.IoBuffer;

import junit.framework.TestCase;
import net.sf.dozer.util.mapping.DozerBeanMapper;
import net.sf.dozer.util.mapping.MapperIF;

import com.agr.dhcpv6.dto.DnsServersOptionDTO;
import com.agr.dhcpv6.server.config.xml.DnsServersOption;
import com.agr.dhcpv6.util.DhcpConstants;

public class TestDhcpDnsServersOption extends TestCase
{
    public void testEncode() throws Exception
    {
        DhcpDnsServersOption dso = new DhcpDnsServersOption();
        dso.addServer("2001:db8::1");    // 16 bytes
        dso.addServer("2001:db8::2");    // 16 bytes
        ByteBuffer bb = dso.encode().buf();
        assertNotNull(bb);
        assertEquals(36, bb.capacity());    // +4 (code=2bytes, len=2bytes)
        assertEquals(36, bb.limit());
        assertEquals(0, bb.position());
        assertEquals(DhcpConstants.OPTION_DNS_SERVERS, bb.getShort());
        assertEquals((short)32, bb.getShort());   // length
        byte[] buf = new byte[16];
        bb.get(buf);
        assertEquals(InetAddress.getByName("2001:db8::1"), 
                     InetAddress.getByAddress(buf));
        bb.get(buf);
        assertEquals(InetAddress.getByName("2001:db8::2"), 
                     InetAddress.getByAddress(buf));
    }

    public void testDecode() throws Exception
    {
        // just 34 bytes, because we start decoding
        // _after_ the option code itself
        ByteBuffer bb = ByteBuffer.allocate(34);
        bb.putShort((short)32);     // length of option
        bb.put(InetAddress.getByName("2001:db8::1").getAddress());
        bb.put(InetAddress.getByName("2001:db8::2").getAddress());
        bb.flip();
        DhcpDnsServersOption dso = new DhcpDnsServersOption();
        dso.decode(IoBuffer.wrap(bb));
        assertNotNull(dso.getDnsServersOption());
        assertEquals(2, dso.getDnsServersOption().getServerIpAddresses().size());
        List dnsServers = dso.getDnsServersOption().getServerIpAddresses();
        assertEquals(InetAddress.getByName("2001:db8::1"), 
                     InetAddress.getByName((String)dnsServers.get(0)));
        assertEquals(InetAddress.getByName("2001:db8::2"), 
                     InetAddress.getByName((String)dnsServers.get(1)));
    }
    
    public void testToDTO() throws Exception
    {
        List mappingFiles = new ArrayList();
        mappingFiles.add("com/agr/dhcpv6/dto/dozermap.xml");
        
        DnsServersOption dso = new DnsServersOption();
        dso.getServerIpAddresses().add("2001:db8::1");
        dso.getServerIpAddresses().add("2001:db8::2");
        MapperIF mapper = new DozerBeanMapper(mappingFiles);
        DnsServersOptionDTO dto = (DnsServersOptionDTO)
                                    mapper.map(dso, DnsServersOptionDTO.class);
        assertNotNull(dto);
        assertNotNull(dto.getServerIpAddresses());
        assertEquals(2, dto.getServerIpAddresses().size());
        assertEquals("2001:db8::1", dto.getServerIpAddresses().get(0));
        assertEquals("2001:db8::2", dto.getServerIpAddresses().get(1));
    }
    
    public void testFromDTO() throws Exception
    {
        List mappingFiles = new ArrayList();
        mappingFiles.add("com/agr/dhcpv6/dto/dozermap.xml");
        
        DnsServersOptionDTO dto = new DnsServersOptionDTO();
        List serverIps = new ArrayList();
        serverIps.add("2001:db8::1");
        serverIps.add("2001:db8::2");
        dto.setServerIpAddresses(serverIps);
        MapperIF mapper = new DozerBeanMapper(mappingFiles);
        DnsServersOption dso = (DnsServersOption)
                                mapper.map(dto, DnsServersOption.class);
        assertNotNull(dso);
        assertNotNull(dso.getServerIpAddresses());
        assertEquals(2, dso.getServerIpAddresses().size());
        assertEquals("2001:db8::1", dso.getServerIpAddresses().get(0));
        assertEquals("2001:db8::2", dso.getServerIpAddresses().get(1));
    }
}
