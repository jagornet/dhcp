/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.v6;

import com.jagornet.dhcp.core.option.base.BaseDomainNameOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * <p>Title: DhcpV6LostServerDomainNameOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6LostServerDomainNameOption extends BaseDomainNameOption
{
	/**
	 * Instantiates a new dhcp lost server domain name option.
	 */
	public DhcpV6LostServerDomainNameOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp lost server domain name option.
	 * 
	 * @param lostServerDomainNameOption the lost server domain name option
	 */
	public DhcpV6LostServerDomainNameOption(String lostServerDomainName)
	{
		super(lostServerDomainName); 
		setCode(DhcpConstants.V6OPTION_LOST_SERVER_DOMAIN_NAME);
	}
}
