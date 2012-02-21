package com.jagornet.dhcpv6.server.config;

import com.jagornet.dhcpv6.option.DhcpConfigOptions;

public interface DhcpOptionConfigObject extends DhcpConfigObject 
{
	public DhcpConfigOptions getDhcpConfigOptions();
}
