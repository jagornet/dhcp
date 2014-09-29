package com.jagornet.dhcp.server.config;

import com.jagornet.dhcp.option.v4.DhcpV4ConfigOptions;

public interface DhcpV4OptionConfigObject extends DhcpConfigObject 
{
	public DhcpV4ConfigOptions getV4ConfigOptions();
}
