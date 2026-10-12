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
 * <p>Title: DhcpV6BcmcsDomainNamesOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6BcmcsDomainNamesOption extends BaseDomainNameListOption
{
	/**
	 * Instantiates a new dhcp bcmcs domain names option.
	 */
	public DhcpV6BcmcsDomainNamesOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp bcmcs domain names option.
	 * 
	 * @param bcmcsDomainNamesOption the bcmcs domain names option
	 */
	public DhcpV6BcmcsDomainNamesOption(List<String> bcmcsDomainNames)
	{
		super(bcmcsDomainNames);
		setCode(DhcpConstants.V6OPTION_BCMCS_DOMAIN_NAMES);
	}
}
