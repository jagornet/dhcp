/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestDhcpDnsServersOption.java is part of DHCPv6.
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
import java.util.List;

import junit.framework.TestCase;

import com.jagornet.dhcpv6.option.v6.DhcpV6DnsServersOption;
import com.jagornet.dhcpv6.util.DhcpConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class TestDhcpDnsServersOption.
 */
public class TestDhcpDnsServersOption extends TestCase
{
	
	/** The dns1. */
	InetAddress dns1 = null;
	
	/** The dns2. */
	InetAddress dns2 = null;
	
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception
	{
		super.setUp();
		dns1 = InetAddress.getByName("2001:db8::1");
		dns2 = InetAddress.getByName("2001:db8::2");
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		dns1 = null;
		dns2 = null;
	}

	/**
	 * Test encode.
	 * 
	 * @throws Exception the exception
	 */
	public void testEncode() throws Exception
    {
        DhcpV6DnsServersOption dso = new DhcpV6DnsServersOption();
        dso.addIpAddress(dns1);    // 16 bytes
        dso.addIpAddress(dns2);    // 16 bytes
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

    /**
     * Test decode.
     * 
     * @throws Exception the exception
     */
    public void testDecode() throws Exception
    {
        // just 34 bytes, because we start decoding
        // _after_ the option code itself
        ByteBuffer bb = ByteBuffer.allocate(34);
        bb.putShort((short)32);     // length of option
        bb.put(dns1.getAddress());
        bb.put(dns2.getAddress());
        bb.flip();
        DhcpV6DnsServersOption dso = new DhcpV6DnsServersOption();
        dso.decode(bb);
        assertEquals(2, dso.getIpAddressList().size());
        List<String> dnsServers = dso.getIpAddressList();
        assertEquals(dns1, 
                     InetAddress.getByName(dnsServers.get(0)));
        assertEquals(dns2, 
                     InetAddress.getByName(dnsServers.get(1)));
    }
    
    /**
     * Test to string.
     */
    public void testToString()
    {
        DhcpV6DnsServersOption dso = new DhcpV6DnsServersOption();
        dso.addIpAddress(dns1);    // 16 bytes
        dso.addIpAddress(dns2);    // 16 bytes
    	System.out.println(dso);
    }
}
