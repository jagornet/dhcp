package com.jagornet.dhcpv6.server.config;

import com.jagornet.dhcpv6.option.DhcpV6ConfigOptions;

public interface DhcpV6OptionConfigObject extends DhcpConfigObject 
{
	public DhcpV6ConfigOptions getDhcpConfigOptions();
}
