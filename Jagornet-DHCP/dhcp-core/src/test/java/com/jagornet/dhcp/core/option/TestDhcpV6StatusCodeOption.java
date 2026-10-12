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

import com.jagornet.dhcp.core.option.v6.DhcpV6StatusCodeOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class TestDhcpStatusCodeOption.
 */
public class TestDhcpV6StatusCodeOption extends TestCase
{
    
    /**
     * Test encode.
     * 
     * @throws Exception the exception
     */
    public void testEncode() throws Exception
    {
        DhcpV6StatusCodeOption dsco = new DhcpV6StatusCodeOption();
        dsco.setStatusCode(DhcpConstants.V6STATUS_CODE_SUCCESS);
        dsco.setMessage("All is well");
        ByteBuffer bb = dsco.encode();
        assertNotNull(bb);
        assertEquals(17, bb.capacity());
        assertEquals(17, bb.limit());
        assertEquals(0, bb.position());
        assertEquals(DhcpConstants.V6OPTION_STATUS_CODE, bb.getShort());
        assertEquals((short)13, bb.getShort());   // length
        assertEquals(DhcpConstants.V6STATUS_CODE_SUCCESS, bb.getShort());
        byte[] buf = new byte[11];
        bb.get(buf);
        assertEquals("All is well", new String(buf));
    }

    /**
     * Test decode.
     * 
     * @throws Exception the exception
     */
    public void testDecode() throws Exception
    {
        // just 15 bytes, because we start decoding
        // _after_ the option code itself
        ByteBuffer bb = ByteBuffer.allocate(15);
        bb.putShort((short)13);  // length
        bb.putShort((short)DhcpConstants.V6STATUS_CODE_SUCCESS);
        bb.put("All is well".getBytes());
        bb.flip();
        DhcpV6StatusCodeOption dsco = new DhcpV6StatusCodeOption();
        dsco.decode(bb);
        assertEquals(13, dsco.getLength());
        assertEquals(DhcpConstants.V6STATUS_CODE_SUCCESS, 
                     dsco.getStatusCode());
        assertEquals("All is well", dsco.getMessage());
    }
    
    /**
     * Test to string.
     * 
     * @throws Exception the exception
     */
    public void testToString() throws Exception
    {
        DhcpV6StatusCodeOption dsco = new DhcpV6StatusCodeOption();
        dsco.setStatusCode(DhcpConstants.V6STATUS_CODE_SUCCESS);
        dsco.setMessage("All is well");
    	System.out.println(dsco);
    }
}
