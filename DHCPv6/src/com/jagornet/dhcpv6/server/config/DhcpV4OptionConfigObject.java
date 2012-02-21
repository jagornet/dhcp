package com.jagornet.dhcpv6.server.config;

import com.jagornet.dhcpv6.option.v4.DhcpV4ConfigOptions;

public interface DhcpV4OptionConfigObject extends DhcpConfigObject 
{
	public DhcpV4ConfigOptions getV4ConfigOptions();
}
