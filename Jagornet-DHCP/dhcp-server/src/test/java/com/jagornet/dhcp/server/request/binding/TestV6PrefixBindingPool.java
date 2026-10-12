/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.request.binding;

import java.net.InetAddress;

import com.jagornet.dhcp.server.config.xml.V6PrefixPool;

import junit.framework.TestCase;

public class TestV6PrefixBindingPool extends TestCase
{
	public void testNext64bitPrefix() throws Exception
	{
		V6PrefixPool pool = new V6PrefixPool();
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
