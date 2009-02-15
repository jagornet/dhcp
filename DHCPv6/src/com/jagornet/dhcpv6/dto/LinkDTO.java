package com.jagornet.dhcpv6.dto;

import java.io.Serializable;
import java.util.List;


public class LinkDTO implements Serializable
{
	protected String name;
    protected String address;
	protected List<PolicyDTO> policies;
    protected ServerIdOptionDTO serverIdOption;
    protected StandardOptionsDTO standardOptions;
//    protected List<OptionDTO> otherOptions;
    protected List<FilterDTO> filters;
//    protected List<PoolDTO> pools;

    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public List<PolicyDTO> getPolicies() {
		return policies;
	}
	public void setPolicies(List<PolicyDTO> policies) {
		this.policies = policies;
	}
	public ServerIdOptionDTO getServerIdOption() {
		return serverIdOption;
	}
	public void setServerIdOption(ServerIdOptionDTO serverIdOption) {
		this.serverIdOption = serverIdOption;
	}
	public StandardOptionsDTO getStandardOptions() {
		return standardOptions;
	}
	public void setStandardOptions(StandardOptionsDTO standardOptions) {
		this.standardOptions = standardOptions;
	}
	public List<FilterDTO> getFilters() {
		return filters;
	}
	public void setFilters(List<FilterDTO> filters) {
		this.filters = filters;
	}
}

