package com.jagornet.dhcpv6.server.request.binding;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.server.config.DhcpConfigObject;
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
