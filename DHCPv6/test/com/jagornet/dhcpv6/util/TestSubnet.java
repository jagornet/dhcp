package com.jagornet.dhcpv6.util;

import java.net.InetAddress;

import com.jagornet.dhcpv6.util.Subnet;

import junit.framework.TestCase;

public class TestSubnet extends TestCase
{
    public void testEndAddress() throws Exception
    {
        Subnet subnet = new Subnet("2001:DB8::", 48);
        assertNotNull(subnet);
        assertEquals(InetAddress.getByName("2001:DB8:0:FFFF:FFFF:FFFF:FFFF:FFFF"),
                     subnet.getEndAddress());
    }
    
    public void testContains() throws Exception
    {
        Subnet subnet = new Subnet("2001:DB8::", 48);
        assertNotNull(subnet);
        assertTrue(subnet.contains(InetAddress.getByName("2001:DB8:0:1:1:1:1:1")));
        assertFalse(subnet.contains(InetAddress.getByName("2001:DB8:1:1:1:1:1:1")));
    }
}
