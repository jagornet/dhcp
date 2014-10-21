/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestDhcpDomainSearchListOption.java is part of DHCPv6.
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
package com.jagornet.dhcp.option;

import java.nio.ByteBuffer;
import java.util.List;

import junit.framework.TestCase;

import com.jagornet.dhcp.option.v6.DhcpV6DomainSearchListOption;
import com.jagornet.dhcp.util.DhcpConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class TestDhcpDomainSearchListOption.
 */
public class TestDhcpDomainSearchListOption extends TestCase
{
	
	/** The domain1. */
	String domain1;
	
	/** The domain2. */
	String domain2;
	
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception
	{
		super.setUp();
		domain1 = "jagornet.com.";		// 08jagornet03com00 8+3+3 =  14
		domain2 = "ipv6universe.com.";	// 0Cipv6universe03com00 12+3+3 = 18
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		domain1 = null;
		domain2 = null;
	}

	/**
	 * Test encode.
	 * 
	 * @throws Exception the exception
	 */
	public void testEncode() throws Exception
    {
        DhcpV6DomainSearchListOption dslo = new DhcpV6DomainSearchListOption();
        dslo.addDomainName(domain1);
        dslo.addDomainName(domain2);
        ByteBuffer bb = dslo.encode();
        assertNotNull(bb);
        assertEquals(36, bb.capacity());    // +4 (code=2bytes, len=2bytes)
        assertEquals(36, bb.limit());
        assertEquals(0, bb.position());
        assertEquals(DhcpConstants.V6OPTION_DOMAIN_SEARCH_LIST, bb.getShort());
        assertEquals((short)32, bb.getShort());   // length
        assertEquals((byte)8, bb.get());
        byte[] buf = new byte[8];
        bb.get(buf);
        assertEquals("jagornet", new String(buf));
        assertEquals((byte)3, bb.get());
        buf = new byte[3];
        bb.get(buf);
        assertEquals("com", new String(buf));
        assertEquals((byte)0, bb.get());
        
        assertEquals((byte)12, bb.get());
        buf = new byte[12];
        bb.get(buf);
        assertEquals("ipv6universe", new String(buf));
        assertEquals((byte)3, bb.get());
        buf = new byte[3];
        bb.get(buf);
        assertEquals("com", new String(buf));
        assertEquals((byte)0, bb.get());
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
        bb.put((byte)8);
        bb.put("jagornet".getBytes());
        bb.put((byte)3);
        bb.put("com".getBytes());
        bb.put((byte)0);
        bb.put((byte)12);
        bb.put("ipv6universe".getBytes());
        bb.put((byte)3);
        bb.put("com".getBytes());
        bb.put((byte)0);
        bb.flip();
        DhcpV6DomainSearchListOption dslo = new DhcpV6DomainSearchListOption();
        dslo.decode(bb);
        assertEquals(2, dslo.getDomainNameList().size());
        List<String> domainNames = dslo.getDomainNameList();
        assertEquals(domain1, domainNames.get(0)); 
        assertEquals(domain2, domainNames.get(1)); 
    }
    
    /**
     * Test to string.
     */
    public void testToString()
    {
        DhcpV6DomainSearchListOption dslo = new DhcpV6DomainSearchListOption();
        dslo.addDomainName(domain1);
        dslo.addDomainName(domain2);
        System.out.println(dslo);
    }
}
