/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.config;

import java.net.InetAddress;
import java.util.SortedMap;
import java.util.TreeMap;

import com.jagornet.dhcp.core.util.Subnet;
import com.jagornet.dhcp.server.config.xml.Link;

import junit.framework.TestCase;

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
            linkMap.put(subnet, new DhcpLink(subnet, new Link()));
        }
        addr = "2001:DB8:1:1::/64";
        s = addr.split("/");
        if ((s != null) && (s.length == 2)) {
            Subnet subnet = new Subnet(s[0], s[1]);
            linkMap.put(subnet, new DhcpLink(subnet, new Link()));
        }
        addr = "2001:DB8:1:2::/64";
        s = addr.split("/");
        if ((s != null) && (s.length == 2)) {
            Subnet subnet = new Subnet(s[0], s[1]);
            linkMap.put(subnet, new DhcpLink(subnet, new Link()));
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
