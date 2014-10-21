/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestV6PrefixBindingPool.java is part of Jagornet DHCP.
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
