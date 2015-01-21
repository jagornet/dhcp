/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestDhcpStatusCodeOption.java is part of Jagornet DHCP.
 *
 *   Jagornet DHCP is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Jagornet DHCP is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Jagornet DHCP.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcp.option;

import java.nio.ByteBuffer;

import junit.framework.TestCase;

import com.jagornet.dhcp.option.v6.DhcpV6StatusCodeOption;
import com.jagornet.dhcp.util.DhcpConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class TestDhcpStatusCodeOption.
 */
public class TestDhcpStatusCodeOption extends TestCase
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
