package com.jagornet.dhcpv6.server.request.binding;

import java.net.InetAddress;

import com.jagornet.dhcpv6.xml.PrefixPool;

import junit.framework.TestCase;

public class TestPrefixBindingPool extends TestCase
{
	public void testNext64bitPrefix() throws Exception
	{
		PrefixPool pool = PrefixPool.Factory.newInstance();
		pool.setRange("2001:DB8:FFFF::/48");
		pool.setPrefixLength(64);
		PrefixBindingPool pbp = new PrefixBindingPool(pool);
		InetAddress next = pbp.getNextAvailableAddress();
		assertEquals(InetAddress.getByName("2001:DB8:FFFF:0::"), next);
		next = pbp.getNextAvailableAddress();
		assertEquals(InetAddress.getByName("2001:DB8:FFFF:1::"), next);
		next = pbp.getNextAvailableAddress();
		assertEquals(InetAddress.getByName("2001:DB8:FFFF:2::"), next);
	}
}
