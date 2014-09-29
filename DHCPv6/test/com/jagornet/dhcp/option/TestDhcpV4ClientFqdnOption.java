/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestDhcpV4ClientFqdnOption.java is part of DHCPv6.
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

import junit.framework.TestCase;

import com.jagornet.dhcp.option.v4.DhcpV4ClientFqdnOption;

// TODO: Auto-generated Javadoc
/**
 * The Class TestDhcpV4ClientFqdnOption.
 */
public class TestDhcpV4ClientFqdnOption extends TestCase
{
		
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

    /**
     * Test decode.
     * 
     * @throws Exception the exception
     */
    public void testDecodeFqdn() throws Exception
    {
        // just 16 bytes, because we start decoding
        // _after_ the option code itself
        ByteBuffer bb = ByteBuffer.allocate(17);
        bb.put((byte)16);     // length of option
        bb.put((byte)4);	  // flags (00000100) E=1 (canonical)
        bb.put((byte)0);	  // rcode1
        bb.put((byte)0);	  // rcode2
        bb.put((byte)3);
        bb.put("foo".getBytes());
        bb.put((byte)3);
        bb.put("bar".getBytes());
        bb.put((byte)3);
        bb.put("com".getBytes());
        bb.put((byte)0);
        bb.flip();
        DhcpV4ClientFqdnOption dcfo = new DhcpV4ClientFqdnOption();
        dcfo.decode(bb);
        assertEquals("foo.bar.com.", dcfo.getDomainName());
    }
    
    public void testNonFqdn() throws Exception
    {
        // just 16 bytes, because we start decoding
        // _after_ the option code itself
        ByteBuffer bb = ByteBuffer.allocate(11);
        bb.put((byte)10);     // length of option
        bb.put((byte)4);	  // flags (00000100) E=1 (canonical)
        bb.put((byte)0);	  // rcode1
        bb.put((byte)0);	  // rcode2
        bb.put((byte)6);	  // len
        bb.put("foobar".getBytes());
        // no terminating zero
        bb.flip();
        DhcpV4ClientFqdnOption dcfo = new DhcpV4ClientFqdnOption();
        dcfo.decode(bb);
        assertEquals("foobar", dcfo.getDomainName());
    }
    
    public void testAsciiFqdn() throws Exception
    {
        // just 16 bytes, because we start decoding
        // _after_ the option code itself
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.put((byte)6);     // length of option
        bb.put((byte)0);	  // flags
        bb.put((byte)0);	  // rcode1
        bb.put((byte)0);	  // rcode2
        bb.put("CLN".getBytes());
        // no terminating zero
        bb.flip();
        DhcpV4ClientFqdnOption dcfo = new DhcpV4ClientFqdnOption();
        dcfo.decode(bb);
        assertEquals("CLN", dcfo.getDomainName());
    }
}
