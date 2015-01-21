/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file StaticBinding.java is part of Jagornet DHCP.
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
import java.net.UnknownHostException;

import com.jagornet.dhcp.message.DhcpMessage;
import com.jagornet.dhcp.server.config.DhcpConfigObject;
import com.jagornet.dhcp.xml.FiltersType;

public abstract class StaticBinding implements DhcpConfigObject
{
	public abstract boolean matches(byte duid[], byte iatype, long iaid,
			DhcpMessage requestMsg);
	
	public abstract String getIpAddress();

	public InetAddress getInetAddress() {
		String ip = getIpAddress();
		if (ip != null) {
			try {
				return InetAddress.getByName(ip);
			}
			catch (UnknownHostException ex) {
				//TODO
			}
		}
		return null;
	}

	@Override
	public FiltersType getFilters() {
		// no filters for static binding
		return null;
	}

	// all times are infinite for static bindings
	
	@Override
	public long getPreferredLifetime() {
		return 0xffffffff;
	}

	@Override
	public long getValidLifetime() {
		return 0xffffffff;
	}

	@Override
	public long getPreferredLifetimeMs() {
		return 0xffffffff;
	}

	@Override
	public long getValidLifetimeMs() {
		return 0xffffffff;
	}
}
