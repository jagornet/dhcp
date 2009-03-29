package com.jagornet.dhcpv6.option;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import net.sf.dozer.util.mapping.DozerBeanMapper;
import net.sf.dozer.util.mapping.MapperIF;

import com.jagornet.dhcpv6.dto.option.DnsServersOptionDTO;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.xml.DnsServersOption;

public class TestDhcpDnsServersOption extends TestCase
{
	InetAddress dns1 = null;
	InetAddress dns2 = null;
	
    @Override
	protected void setUp() throws Exception
	{
		super.setUp();
		dns1 = InetAddress.getByName("2001:db8::1");;
		dns2 = InetAddress.getByName("2001:db8::2");
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testEncode() throws Exception
    {
        DhcpDnsServersOption dso = new DhcpDnsServersOption();
        dso.addServer(dns1);    // 16 bytes
        dso.addServer(dns2);    // 16 bytes
        ByteBuffer bb = dso.encode();
        assertNotNull(bb);
        assertEquals(36, bb.capacity());    // +4 (code=2bytes, len=2bytes)
        assertEquals(36, bb.limit());
        assertEquals(0, bb.position());
        assertEquals(DhcpConstants.OPTION_DNS_SERVERS, bb.getShort());
        assertEquals((short)32, bb.getShort());   // length
        byte[] buf = new byte[16];
        bb.get(buf);
        assertEquals(dns1, InetAddress.getByAddress(buf));
        bb.get(buf);
        assertEquals(dns2, InetAddress.getByAddress(buf));
    }

    public void testDecode() throws Exception
    {
        // just 34 bytes, because we start decoding
        // _after_ the option code itself
        ByteBuffer bb = ByteBuffer.allocate(34);
        bb.putShort((short)32);     // length of option
        bb.put(dns1.getAddress());
        bb.put(dns2.getAddress());
        bb.flip();
        DhcpDnsServersOption dso = new DhcpDnsServersOption();
        dso.decode(bb);
        assertNotNull(dso.getIpAddressListOption());
        assertEquals(2, dso.getIpAddressListOption().getIpAddressList().size());
        List<String> dnsServers = dso.getIpAddressListOption().getIpAddressList();
        assertEquals(dns1, 
                     InetAddress.getByName((String)dnsServers.get(0)));
        assertEquals(dns2, 
                     InetAddress.getByName((String)dnsServers.get(1)));
    }
    
    public void testToDTO() throws Exception
    {
        List<String> mappingFiles = new ArrayList<String>();
        mappingFiles.add("com/jagornet/dhcpv6/dto/dozermap.xml");
        
        DnsServersOption dso = DnsServersOption.Factory.newInstance();
        dso.addIpAddress(dns1.getHostAddress());
        dso.addIpAddress(dns2.getHostAddress());
        // hack to get/set the "fixed" fields so that the 'isSet' flag
        // for each field is turned ON (true) to allow Dozer to map them
//        dso.setCode(dso.getCode());
//        dso.setName(dso.getName());
        MapperIF mapper = new DozerBeanMapper(mappingFiles);
        DnsServersOptionDTO dto = (DnsServersOptionDTO)
                                    mapper.map(dso, DnsServersOptionDTO.class);
        assertNotNull(dto);
        assertEquals(DhcpConstants.OPTION_DNS_SERVERS, dto.getCode().intValue());
        assertNotNull(dto.getIpAddressList());
        assertEquals(2, dto.getIpAddressList().size());
        assertEquals(dns1.getHostAddress(), dto.getIpAddressList().get(0));
        assertEquals(dns2.getHostAddress(), dto.getIpAddressList().get(1));
    }
    
    public void testFromDTO() throws Exception
    {
        List<String> mappingFiles = new ArrayList<String>();
        mappingFiles.add("com/jagornet/dhcpv6/dto/dozermap.xml");
        
        DnsServersOptionDTO dto = new DnsServersOptionDTO();
        List<String> serverIps = new ArrayList<String>();
        serverIps.add(dns1.getHostAddress());
        serverIps.add(dns2.getHostAddress());
        dto.setIpAddressList(serverIps);
        MapperIF mapper = new DozerBeanMapper(mappingFiles);
        DnsServersOption dso = (DnsServersOption)
                                mapper.map(dto, DnsServersOption.class);
        assertNotNull(dso);
        assertEquals(DhcpConstants.OPTION_DNS_SERVERS, dso.getCode());
        assertNotNull(dso.getIpAddressList());
        assertEquals(2, dso.getIpAddressList().size());
        assertEquals(dns1.getHostAddress(), dso.getIpAddressList().get(0));
        assertEquals(dns2.getHostAddress(), dso.getIpAddressList().get(1));
    }
}
