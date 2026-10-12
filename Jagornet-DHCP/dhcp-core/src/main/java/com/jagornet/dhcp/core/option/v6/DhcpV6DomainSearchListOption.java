/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.v6;

import java.util.List;

import com.jagornet.dhcp.core.option.base.BaseDomainNameListOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

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
	public DhcpV6DomainSearchListOption(List<String> domainSearchList)
	{
		super(domainSearchList);
		setCode(DhcpConstants.V6OPTION_DOMAIN_SEARCH_LIST);
	}
}
