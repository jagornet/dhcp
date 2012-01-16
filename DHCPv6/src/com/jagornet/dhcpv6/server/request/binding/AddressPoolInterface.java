package com.jagornet.dhcpv6.server.request.binding;

import com.jagornet.dhcpv6.xml.FiltersType;
import com.jagornet.dhcpv6.xml.PoliciesType;

public interface AddressPoolInterface {
	
	public FiltersType getFilters();
	public PoliciesType getPolicies();

}
