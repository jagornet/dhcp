package com.agr.dhcpv6.server.config;

import java.net.InetAddress;
import java.util.SortedMap;
import java.util.TreeMap;

import junit.framework.TestCase;

import com.agr.dhcpv6.server.config.xml.DhcpV6ServerConfig.Links;
import com.agr.dhcpv6.util.Subnet;

public class TestDhcpServerConfiguration extends TestCase
{
    public void testLinkMap() throws Exception
    {
        DhcpServerConfiguration.
        init("/svn/dhcpv6/trunk/DHCPv6/src/com/agr/dhcpv6/server/dhcpServerConfigLinkTest1.xml");
        
        TreeMap<Subnet, Links> linkMap = DhcpServerConfiguration.getLinkMap();
        assertNotNull(linkMap);
        assertEquals(5, linkMap.size());
        Subnet searchAddr = new Subnet("2001:DB8:3:1:DEB:DEB:DEB:1", 128);
        // there are two subnets greater than our search address
        SortedMap<Subnet, Links> subMap = linkMap.tailMap(searchAddr);
        assertNotNull(subMap);
        assertEquals(2, subMap.size());

        // there are two subnets greater than our search address
        subMap = linkMap.headMap(searchAddr);
        assertNotNull(subMap);
        assertEquals(3, subMap.size());
        // the last subnet from the head list is the one we want
        assertEquals(InetAddress.getByName("2001:DB8:3::"), 
                     subMap.lastKey().getSubnetAddress());
    }
}
