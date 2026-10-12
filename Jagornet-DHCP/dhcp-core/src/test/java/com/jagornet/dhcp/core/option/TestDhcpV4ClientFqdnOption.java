/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option;

import java.nio.ByteBuffer;

import com.jagornet.dhcp.core.option.v4.DhcpV4ClientFqdnOption;

import junit.framework.TestCase;

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
