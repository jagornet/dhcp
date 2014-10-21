/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestLinkMap.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.util;

import java.net.InetAddress;
import java.util.SortedMap;
import java.util.TreeMap;

import junit.framework.TestCase;

import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.util.Subnet;
import com.jagornet.dhcp.xml.Link;

public class TestLinkMap extends TestCase 
{
    private SortedMap<Subnet, DhcpLink> linkMap;
    
    @Override
    protected void setUp() throws Exception
    {
    	super.setUp();
    	linkMap = new TreeMap<Subnet, DhcpLink>();
        String addr = "2001:DB8:1::/64";
        String[] s = addr.split("/");
        if ((s != null) && (s.length == 2)) {
            Subnet subnet = new Subnet(s[0], s[1]);
            linkMap.put(subnet, new DhcpLink(subnet, Link.Factory.newInstance()));
        }
        addr = "2001:DB8:1:1::/64";
        s = addr.split("/");
        if ((s != null) && (s.length == 2)) {
            Subnet subnet = new Subnet(s[0], s[1]);
            linkMap.put(subnet, new DhcpLink(subnet, Link.Factory.newInstance()));
        }
        addr = "2001:DB8:1:2::/64";
        s = addr.split("/");
        if ((s != null) && (s.length == 2)) {
            Subnet subnet = new Subnet(s[0], s[1]);
            linkMap.put(subnet, new DhcpLink(subnet, Link.Factory.newInstance()));
        }
    }
    
    public void testFindSubnetAddress() throws Exception
    {
    	InetAddress addr = InetAddress.getByName("2001:DB8:1:1:0:0:0:0");
    	assertNotNull(findLink(addr));
    }
    
    public void testFindAddress() throws Exception
    {
    	InetAddress addr = InetAddress.getByName("2001:DB8:1:1:0:0:0:1");
    	assertNotNull(findLink(addr));
    }
    
    private DhcpLink findLink(InetAddress addr) throws Exception
    {
        Subnet s = new Subnet(addr, 128);
        // find links less than the given address
        SortedMap<Subnet, DhcpLink> subMap = linkMap.headMap(s);
        if ((subMap != null) && !subMap.isEmpty()) {
            Subnet k = subMap.lastKey();
            if (k.contains(addr)) {
                return subMap.get(k);
            }
        }
        // find links greater or equal to given address
        subMap = linkMap.tailMap(s);
        if ((subMap != null) && !subMap.isEmpty()) {
            Subnet k = subMap.firstKey();
            if (k.contains(addr)) {
                return subMap.get(k);
            }
        }
        return null;
    }
}
