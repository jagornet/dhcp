/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6DomainSearchListOption.java is part of DHCPv6.
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
package com.jagornet.dhcp.option.v6;

import com.jagornet.dhcp.option.base.BaseDomainNameListOption;
import com.jagornet.dhcp.util.DhcpConstants;
import com.jagornet.dhcp.xml.V6DomainSearchListOption;

/**
 * <p>Title: DhcpV6DomainSearchListOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6DomainSearchListOption extends BaseDomainNameListOption
{
	/**
	 * Instantiates a new dhcp domain search list option.
	 */
	public DhcpV6DomainSearchListOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp domain search list option.
	 * 
	 * @param domainSearchListOption the domain search list option
	 */
	public DhcpV6DomainSearchListOption(V6DomainSearchListOption domainSearchListOption)
	{
		super(domainSearchListOption);
		setCode(DhcpConstants.V6OPTION_DOMAIN_SEARCH_LIST);
	}
}
