/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option;

import java.nio.ByteBuffer;

import junit.framework.TestCase;

import com.jagornet.dhcp.core.option.v6.DhcpV6PreferenceOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class TestDhcpPreferenceOption.
 */
public class TestDhcpV6PreferenceOption extends TestCase
{
    
    /**
     * Test encode.
     * 
     * @throws Exception the exception
     */
    public void testEncode() throws Exception
    {
        DhcpV6PreferenceOption dpo = new DhcpV6PreferenceOption();
        dpo.setUnsignedByte((byte)2);
        ByteBuffer bb = dpo.encode();
        assertNotNull(bb);
        assertEquals(5, bb.capacity());
        assertEquals(5, bb.limit());
        assertEquals(0, bb.position());
        assertEquals(DhcpConstants.V6OPTION_PREFERENCE, bb.getShort());
        assertEquals((short)1, bb.getShort());   // length
        assertEquals((byte)2, bb.get());
    }

    /**
     * Test decode.
     * 
     * @throws Exception the exception
     */
    public void testDecode() throws Exception
    {
        // just three bytes, because we start decoding
        // _after_ the option code itself
        ByteBuffer bb = ByteBuffer.allocate(3);
        bb.putShort((short)1);  // length
        bb.put((byte)2);
        bb.flip();
        DhcpV6PreferenceOption dpo = new DhcpV6PreferenceOption();
        dpo.decode(bb);
        assertEquals(1, dpo.getLength());
        assertEquals(2, dpo.getUnsignedByte());
    }
    
    /**
     * Test to string.
     */
    public void testToString()
    {
        DhcpV6PreferenceOption dpo = new DhcpV6PreferenceOption();
        dpo.setUnsignedByte((byte)2);
        System.out.println(dpo);
    }
}
