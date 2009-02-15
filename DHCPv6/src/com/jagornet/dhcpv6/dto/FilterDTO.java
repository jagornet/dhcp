package com.jagornet.dhcpv6.dto;

import java.io.Serializable;
import java.util.List;

public class FilterDTO implements Serializable
{
	protected String name;
	protected List<FilterExpressionDTO> filterExpressions;
	protected List<PolicyDTO> policies;
    protected ServerIdOptionDTO serverIdOption;
    protected StandardOptionsDTO standardOptions;
//    protected List<OptionDTO> otherOptions;

    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<FilterExpressionDTO> getFilterExpressions() {
		return filterExpressions;
	}
	public void setFilterExpressions(List<FilterExpressionDTO> filterExpressions) {
		this.filterExpressions = filterExpressions;
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
}
