package com.jagornet.dhcp.server.request.binding;

import java.net.InetAddress;

import junit.framework.TestCase;

import com.jagornet.dhcp.server.request.binding.V6PrefixBindingPool;
import com.jagornet.dhcp.xml.V6PrefixPool;

public class TestV6PrefixBindingPool extends TestCase
{
	public void testNext64bitPrefix() throws Exception
	{
		V6PrefixPool pool = V6PrefixPool.Factory.newInstance();
		pool.setRange("2001:DB8:FFFF::/48");
		pool.setPrefixLength(64);
		V6PrefixBindingPool pbp = new V6PrefixBindingPool(pool);
		InetAddress next = pbp.getNextAvailableAddress();
		assertEquals(InetAddress.getByName("2001:DB8:FFFF:0::"), next);
		next = pbp.getNextAvailableAddress();
		assertEquals(InetAddress.getByName("2001:DB8:FFFF:1::"), next);
		next = pbp.getNextAvailableAddress();
		assertEquals(InetAddress.getByName("2001:DB8:FFFF:2::"), next);
	}
}
