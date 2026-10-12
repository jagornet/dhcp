/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.util;

import java.net.InetAddress;

import com.jagornet.dhcp.core.util.Subnet;

import junit.framework.TestCase;

// TODO: Auto-generated Javadoc
/**
 * The Class TestSubnet.
 */
public class TestSubnet extends TestCase
{
    
    /**
     * Test end address.
     * 
     * @throws Exception the exception
     */
    public void testEndAddress() throws Exception
    {
        Subnet subnet = new Subnet("2001:DB8::", 48);
        assertNotNull(subnet);
        assertEquals(InetAddress.getByName("2001:DB8:0:FFFF:FFFF:FFFF:FFFF:FFFF"),
                     subnet.getEndAddress());
    }
    
    /**
     * Test contains.
     * 
     * @throws Exception the exception
     */
    public void testContains() throws Exception
    {
        Subnet subnet = new Subnet("2001:DB8::", 48);
        assertNotNull(subnet);
        assertTrue(subnet.contains(InetAddress.getByName("2001:DB8:0:1:1:1:1:1")));
        assertFalse(subnet.contains(InetAddress.getByName("2001:DB8:1:1:1:1:1:1")));
    }

    /**
     * Test contains with null address returns false (null-safety).
     */
    public void testContainsNullAddress() throws Exception {
        Subnet subnet = new Subnet("2001:DB8::", 48);
        assertFalse(subnet.contains(null));
    }
}
