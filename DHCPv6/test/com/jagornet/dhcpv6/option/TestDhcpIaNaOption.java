/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestDhcpIaNaOption.java is part of DHCPv6.
 *
 *   DHCPv6 is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   DHCPv6 is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with DHCPv6.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcpv6.option;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.jagornet.dhcpv6.option.base.BaseDomainNameOption;
import com.jagornet.dhcpv6.server.DhcpV6Server;
import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.PoliciesType;
import com.jagornet.dhcpv6.xml.Policy;

// TODO: Auto-generated Javadoc
/**
 * The Class TestDhcpIaNaOption.
 */
public class TestDhcpIaNaOption extends TestCase
{
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	public void setUp() throws Exception
	{
		String configFilename = DhcpV6Server.DEFAULT_CONFIG_FILENAME;
		DhcpServerConfiguration.configFilename = configFilename;
		DhcpServerConfiguration config = DhcpServerConfiguration.getInstance();
		PoliciesType policies = PoliciesType.Factory.newInstance();
		Policy pcy = policies.addNewPolicy();
		pcy.setName("sendRequestedOptionsOnly");
		pcy.setValue("false");
		config.getDhcpV6ServerConfig().setPolicies(policies);
	}
	
	/**
	 * Test encode.
	 * 
	 * @throws Exception the exception
	 */
	public void testEncode() throws Exception
	{
		int len = 4;	// code + len
		DhcpIaNaOption din = new DhcpIaNaOption();
		din.setIaId(0xdebb1e);
		din.setT1(10000);
		din.setT2(20000);
		len += 12;
		
		List<DhcpIaAddrOption> iaAddrOptions = new ArrayList<DhcpIaAddrOption>();

		DhcpIaAddrOption addr1 = new DhcpIaAddrOption();
		addr1.setIpAddress("3ffe::1");
		addr1.setPreferredLifetime(11000);
		addr1.setValidLifetime(12000);
		len += 4 + 24;	// code + len + ipaddr(16) + preferred(4) + valid(4)
		DhcpDnsServersOption dnsServers1 = new DhcpDnsServersOption();
		dnsServers1.addIpAddress(InetAddress.getByName("3ffe::1:1"));
		dnsServers1.addIpAddress(InetAddress.getByName("3ffe::1:2"));
		addr1.putDhcpOption(dnsServers1);
		iaAddrOptions.add(addr1);
		len += 4 + 16 + 16;
		
		DhcpIaAddrOption addr2 = new DhcpIaAddrOption();
		addr2.setIpAddress("3ffe::2");
		addr2.setPreferredLifetime(21000);
		addr2.setValidLifetime(22000);
		len += 4 + 24;	// code + len + ipaddr(16) + preferred(4) + valid(4)
		DhcpDnsServersOption dnsServers2 = new DhcpDnsServersOption();
		dnsServers2.addIpAddress(InetAddress.getByName("3ffe::2:1"));
		dnsServers2.addIpAddress(InetAddress.getByName("3ffe::2:2"));
		addr2.putDhcpOption(dnsServers2);		
		iaAddrOptions.add(addr2);
		len += 4 + 16 + 16;
		
		din.setIaAddrOptions(iaAddrOptions);
		
		DhcpDomainSearchListOption domainSearch = new DhcpDomainSearchListOption();
		domainSearch.addDomainName("foo.com.");
		domainSearch.addDomainName("bar.com.");
		din.putDhcpOption(domainSearch);
		len += 4 + 9 + 9;
		
		ByteBuffer buf = din.encode();
        assertEquals(len, buf.capacity());
        assertEquals(len, buf.limit());
        assertEquals(0, buf.position());

        assertEquals(DhcpConstants.OPTION_IA_NA, Util.getUnsignedShort(buf));
        assertEquals(len-4, Util.getUnsignedShort(buf));   // length
        
        assertEquals((long)0xdebb1e, Util.getUnsignedInt(buf));
        assertEquals((long)10000, Util.getUnsignedInt(buf));
        assertEquals((long)20000, Util.getUnsignedInt(buf));
        
        assertEquals(DhcpConstants.OPTION_IAADDR, Util.getUnsignedShort(buf));
        assertEquals(60, Util.getUnsignedShort(buf));
        
        byte[] ia = new byte[16];
        buf.get(ia);
        assertEquals(InetAddress.getByName("3ffe::1"), InetAddress.getByAddress(ia));
        assertEquals((long)11000, Util.getUnsignedInt(buf));
        assertEquals((long)12000, Util.getUnsignedInt(buf));
        assertEquals(DhcpConstants.OPTION_DNS_SERVERS, Util.getUnsignedShort(buf));
        assertEquals(32, Util.getUnsignedShort(buf));
        buf.get(ia);
        assertEquals(InetAddress.getByName("3ffe::1:1"), InetAddress.getByAddress(ia));
        buf.get(ia);
        assertEquals(InetAddress.getByName("3ffe::1:2"), InetAddress.getByAddress(ia));

        assertEquals(DhcpConstants.OPTION_IAADDR, Util.getUnsignedShort(buf));
        assertEquals(60, Util.getUnsignedShort(buf));
        
        buf.get(ia);
        assertEquals(InetAddress.getByName("3ffe::2"), InetAddress.getByAddress(ia));
        assertEquals((long)21000, Util.getUnsignedInt(buf));
        assertEquals((long)22000, Util.getUnsignedInt(buf));
        assertEquals(DhcpConstants.OPTION_DNS_SERVERS, Util.getUnsignedShort(buf));
        assertEquals(32, Util.getUnsignedShort(buf));
        buf.get(ia);
        assertEquals(InetAddress.getByName("3ffe::2:1"), InetAddress.getByAddress(ia));
        buf.get(ia);
        assertEquals(InetAddress.getByName("3ffe::2:2"), InetAddress.getByAddress(ia));

        assertEquals(DhcpConstants.OPTION_DOMAIN_SEARCH_LIST, Util.getUnsignedShort(buf));
        assertEquals(18, Util.getUnsignedShort(buf));
        assertEquals("foo.com.", BaseDomainNameOption.decodeDomainName(buf, buf.position()+9));
        assertEquals("bar.com.", BaseDomainNameOption.decodeDomainName(buf, buf.position()+9));
	}
	
	/**
	 * Test decode.
	 * 
	 * @throws Exception the exception
	 */
	public void testDecode() throws Exception
	{
        // just 164 bytes, because we start decoding
        // _after_ the option code itself
        ByteBuffer bb = ByteBuffer.allocate(164);

        bb.putShort((short)162);
		
		bb.putInt(0xdebb1e);
		bb.putInt(10000);
		bb.putInt(20000);
		
		bb.putShort((short)DhcpConstants.OPTION_IAADDR);
		bb.putShort((short)60);
		bb.put(InetAddress.getByName("3ffe::1").getAddress());
		bb.putInt(11000);
		bb.putInt(12000);
		bb.putShort((short)DhcpConstants.OPTION_DNS_SERVERS);
		bb.putShort((short)32);
		bb.put(InetAddress.getByName("3ffe::1:1").getAddress());
		bb.put(InetAddress.getByName("3ffe::1:2").getAddress());
		
		bb.putShort((short)DhcpConstants.OPTION_IAADDR);
		bb.putShort((short)60);
		bb.put(InetAddress.getByName("3ffe::2").getAddress());
		bb.putInt(21000);
		bb.putInt(22000);
		bb.putShort((short)DhcpConstants.OPTION_DNS_SERVERS);
		bb.putShort((short)32);
		bb.put(InetAddress.getByName("3ffe::2:1").getAddress());
		bb.put(InetAddress.getByName("3ffe::2:2").getAddress());
		
		bb.putShort((short)DhcpConstants.OPTION_DOMAIN_SEARCH_LIST);
		bb.putShort((short)18);
		BaseDomainNameOption.encodeDomainName(bb, "foo.com.");
		BaseDomainNameOption.encodeDomainName(bb, "bar.com.");
		bb.flip();
		
		DhcpIaNaOption dhcpIaNaOption = new DhcpIaNaOption();
		dhcpIaNaOption.decode(bb);
		assertEquals(162, dhcpIaNaOption.getDecodedLength());
	}
	
	/**
	 * Test to string.
	 */
	public void testToString()
	{
		int len = 4;	// code + len
		DhcpIaNaOption din = new DhcpIaNaOption();
		din.setIaId(0xdebb1e);
		din.setT1(10000);
		din.setT2(20000);
		len += 12;
		
		DhcpIaAddrOption ao1 = new DhcpIaAddrOption();
		ao1.setIpAddress("3ffe::1");
		ao1.setPreferredLifetime(11000);
		ao1.setValidLifetime(12000);
		len += 4 + 24;	// code + len + ipaddr(16) + preferred(4) + valid(4)
		DhcpDnsServersOption ao1dns = new DhcpDnsServersOption();
		ao1dns.addIpAddress("3ffe::1:1");
		ao1dns.addIpAddress("3ffe::1:2");
		ao1.putDhcpOption(ao1dns);
		len += 4 + 16 + 16;
		
		DhcpIaAddrOption ao2 = new DhcpIaAddrOption();
		ao2.setIpAddress("3ffe::2");
		ao2.setPreferredLifetime(21000);
		ao2.setValidLifetime(22000);
		len += 4 + 24;	// code + len + ipaddr(16) + preferred(4) + valid(4)
		DhcpDnsServersOption ao2dns = new DhcpDnsServersOption();
		ao2dns.addIpAddress("3ffe::2:1");
		ao2dns.addIpAddress("3ffe::2:2");
		ao2.putDhcpOption(ao2dns);
		len += 4 + 16 + 16;
		
		DhcpDomainSearchListOption dslo = new DhcpDomainSearchListOption();
		dslo.addDomainName("foo.com.");
		dslo.addDomainName("bar.com.");
		din.putDhcpOption(dslo);
		len += 4 + 9 + 9;
		
		System.out.println(din);
	}
}
