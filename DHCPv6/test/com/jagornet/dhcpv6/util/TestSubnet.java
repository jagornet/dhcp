/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestSubnet.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.util;

import java.net.InetAddress;

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
}
