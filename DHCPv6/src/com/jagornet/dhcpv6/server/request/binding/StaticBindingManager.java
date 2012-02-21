package com.jagornet.dhcpv6.server.request.binding;

import com.jagornet.dhcpv6.message.DhcpMessageInterface;
import com.jagornet.dhcpv6.xml.Link;

public interface StaticBindingManager 
{
	public StaticBinding findStaticBinding(Link clientLink, byte[] duid, byte iatype, long iaid,
			DhcpMessageInterface requestMsg);
}
