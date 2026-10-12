/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.v4;

import com.jagornet.dhcp.core.option.base.BaseStringOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * <p>Title: DhcpV4DomainNameOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4DomainNameOption extends BaseStringOption
{
	/**
	 * Instantiates a new dhcp v4 domain name option.
	 */
	public DhcpV4DomainNameOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp v4 domain name option.
	 * 
	 * @param v4DomainNameOption the v4 domain name option
	 */
	public DhcpV4DomainNameOption(String v4DomainName)
	{
		super(v4DomainName); 
		setCode(DhcpConstants.V4OPTION_DOMAIN_NAME);
		setV4(true);
	}
}
