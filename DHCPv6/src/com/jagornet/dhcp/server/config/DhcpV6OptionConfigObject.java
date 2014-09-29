package com.jagornet.dhcp.server.config;

import com.jagornet.dhcp.option.v6.DhcpV6ConfigOptions;

public interface DhcpV6OptionConfigObject extends DhcpConfigObject 
{
	public DhcpV6ConfigOptions getDhcpConfigOptions();
}
