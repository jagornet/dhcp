/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.request.binding;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.jagornet.dhcp.core.message.DhcpMessage;
import com.jagornet.dhcp.server.config.DhcpConfigObject;
import com.jagornet.dhcp.server.config.xml.FiltersType;

public abstract class StaticBinding implements DhcpConfigObject
{
	public abstract boolean matches(byte[] duid, byte iatype, long iaid,
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
