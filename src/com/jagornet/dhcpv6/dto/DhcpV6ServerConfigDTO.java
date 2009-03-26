/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6ServerConfigDTO.java is part of DHCPv6.
 *
 *   DHCPv6 is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   DHCPv6 is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with DHCPv6.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/*
 *   This file DhcpV6ServerConfigDTO.java is part of DHCPv6.
 *
 *   DHCPv6 is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   DHCPv6 is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with DHCPv6.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcpv6.dto;

import java.io.Serializable;

import com.jagornet.dhcpv6.dto.option.ServerIdOptionDTO;

/**
 * Title: DhcpV6ServerConfigDTO
 * Description: The main Data Transfer Object (DTO) representing
 * the DhcpV6ServerConfiguration. 
 * 
 * @author A. Gregory Rabil
 */

public class DhcpV6ServerConfigDTO implements Serializable
{
	/**
	 * Default serial version id.
	 */
	private static final long serialVersionUID = 1L;
	
	/** The policies. */
	protected PoliciesDTO policies;
    
    /** The server id option. */
    protected ServerIdOptionDTO serverIdOption;
    
    /** The configured options. */
    protected OptionsDTO options;
    
    /** The filters. */
    protected FiltersDTO filters;
	
	/** The links. */
    protected LinksDTO links;

    /**
	 * Gets the server id option.
	 * 
	 * @return the server id option
	 */
	public ServerIdOptionDTO getServerIdOption() {
		return serverIdOption;
	}

	/**
	 * Sets the server id option.
	 * 
	 * @param serverIdOption the new server id option
	 */
	public void setServerIdOption(ServerIdOptionDTO serverIdOption) {
		this.serverIdOption = serverIdOption;
	}

	/**
	 * Gets the configured options.
	 * 
	 * @return the configured options
	 */
	public OptionsDTO getOptions() {
		return options;
	}

	/**
	 * Sets the configured options.
	 * 
	 * @param options the new configured options
	 */
	public void setOptions(OptionsDTO options) {
		this.options = options;
	}

	public PoliciesDTO getPolicies() {
		return policies;
	}

	public void setPolicies(PoliciesDTO policies) {
		this.policies = policies;
	}

	public FiltersDTO getFilters() {
		return filters;
	}

	public void setFilters(FiltersDTO filters) {
		this.filters = filters;
	}

	public LinksDTO getLinks() {
		return links;
	}

	public void setLinks(LinksDTO links) {
		this.links = links;
	}
}
