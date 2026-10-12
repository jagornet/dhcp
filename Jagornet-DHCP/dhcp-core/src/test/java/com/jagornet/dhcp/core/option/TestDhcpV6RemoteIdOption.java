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

import com.jagornet.dhcp.core.option.v6.DhcpV6RemoteIdOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class TestDhcpUserClassOption.
 */
public class TestDhcpV6RemoteIdOption extends TestCase
{
    
    /**
     * Test encode.
     * 
     * @throws Exception the exception
     */
    public void testEncode() throws Exception
    {
        DhcpV6RemoteIdOption drio = new DhcpV6RemoteIdOption();
        drio.setEnterpriseNumber(9999);
        drio.getOpaqueData().setAscii("This is a test remote id");	// 28 (enterpriseNum=4 bytes, data=24 bytes) 
        ByteBuffer bb = drio.encode();
        assertNotNull(bb);
        assertEquals(32, bb.capacity());    // +4 (code=2bytes, len=2bytes)
        assertEquals(32, bb.limit());
        assertEquals(0, bb.position());
        assertEquals(DhcpConstants.V6OPTION_REMOTE_ID, bb.getShort());
        assertEquals((short)28, bb.getShort());   // length
        assertEquals(9999, bb.getInt());	  // enterprise num
        byte[] b = new byte[24];
        bb.get(b);
        assertEquals("This is a test remote id", new String(b));
    }

    /**
     * Test decode.
     * 
     * @throws Exception the exception
     */
    public void testDecode() throws Exception
    {
        // just 30 bytes, because we start decoding
        // _after_ the option code itself
        ByteBuffer bb = ByteBuffer.allocate(30);
        bb.putShort((short)28);     // length of option
        bb.putInt(9999);     	// enterprise number
        bb.put("This is a test remote id".getBytes());
        bb.flip();
        DhcpV6RemoteIdOption drio = new DhcpV6RemoteIdOption();
        drio.decode(bb);
        assertEquals((long)9999, drio.getEnterpriseNumber());
        assertEquals("This is a test remote id", drio.getOpaqueData().getAscii());
    }
    
    /**
     * Test to string.
     */
    public void testToString()
    {
        DhcpV6RemoteIdOption drio = new DhcpV6RemoteIdOption();
        drio.setEnterpriseNumber(9999);
        drio.getOpaqueData().setAscii("This is a test remote id");	// 28 (enterpriseNum=4 bytes, data=24 bytes) 
        System.out.println(drio);
    }
}
