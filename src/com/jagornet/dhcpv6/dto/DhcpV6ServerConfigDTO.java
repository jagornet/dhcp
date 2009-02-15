package com.jagornet.dhcpv6.dto;

import java.io.Serializable;
import java.util.List;

/**
 * <p>Title: DhcpV6ServerConfigDTO </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpV6ServerConfigDTO implements Serializable
{
	protected List<PolicyDTO> policies;
    protected ServerIdOptionDTO serverIdOption;
    protected StandardOptionsDTO standardOptions;
//    protected List<OptionDTO> otherOptions;
    protected List<FilterDTO> filters;
	protected List<LinkDTO> links;

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

	public List<LinkDTO> getLinks() {
		return links;
	}

	public void setLinks(List<LinkDTO> links) {
		this.links = links;
	}
	    
}
