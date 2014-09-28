package com.jagornet.dhcpv6.server.request.binding;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcp.xml.Link;

public interface StaticBindingManager 
{
	public StaticBinding findStaticBinding(Link clientLink, byte[] duid, byte iatype, long iaid,
			DhcpMessage requestMsg);
}
