package com.jagornet.dhcpv6.server.config;

import com.jagornet.dhcpv6.xml.FiltersType;
import com.jagornet.dhcpv6.xml.PoliciesType;

public interface DhcpConfigObject 
{	
	public FiltersType getFilters();
	public PoliciesType getPolicies();
	public long getPreferredLifetime();
	public long getValidLifetime();
	public long getPreferredLifetimeMs();
	public long getValidLifetimeMs();
}
