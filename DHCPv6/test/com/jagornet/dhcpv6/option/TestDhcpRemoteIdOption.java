/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestDhcpUserClassOption.java is part of DHCPv6.
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

import java.nio.ByteBuffer;

import junit.framework.TestCase;

import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.xml.RemoteIdOption;

// TODO: Auto-generated Javadoc
/**
 * The Class TestDhcpUserClassOption.
 */
public class TestDhcpRemoteIdOption extends TestCase
{
    
    /**
     * Test encode.
     * 
     * @throws Exception the exception
     */
    public void testEncode() throws Exception
    {
        DhcpRemoteIdOption drio = new DhcpRemoteIdOption();
        RemoteIdOption rio = (RemoteIdOption)drio.getOpaqueDataOptionType();
        rio.setEnterpriseNumber(9999);
        rio.getOpaqueData().setAsciiValue("This is a test remote id");	// 28 (enterpriseNum=4 bytes, data=24 bytes) 
        ByteBuffer bb = drio.encode();
        assertNotNull(bb);
        assertEquals(32, bb.capacity());    // +4 (code=2bytes, len=2bytes)
        assertEquals(32, bb.limit());
        assertEquals(0, bb.position());
        assertEquals(DhcpConstants.OPTION_REMOTE_ID, bb.getShort());
        assertEquals((short)28, bb.getShort());   // length
        assertEquals((int)9999, bb.getInt());	  // enterprise num
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
        bb.putInt((int)9999);     	// enterprise number
        bb.put("This is a test remote id".getBytes());
        bb.flip();
        DhcpRemoteIdOption drio = new DhcpRemoteIdOption();
        drio.decode(bb);
        assertNotNull(drio.getOpaqueDataOptionType());
        RemoteIdOption rio = (RemoteIdOption) drio.getOpaqueDataOptionType();
        assertNotNull(rio);
        assertEquals((long)9999, rio.getEnterpriseNumber());
        assertEquals("This is a test remote id", rio.getOpaqueData().getAsciiValue());
    }
    
    /**
     * Test to string.
     */
    public void testToString()
    {
        DhcpRemoteIdOption drio = new DhcpRemoteIdOption();
        RemoteIdOption rio = (RemoteIdOption)drio.getOpaqueDataOptionType();
        rio.setEnterpriseNumber(9999);
        rio.getOpaqueData().setAsciiValue("This is a test remote id");	// 28 (enterpriseNum=4 bytes, data=24 bytes) 
        System.out.println(drio);
    }
}
