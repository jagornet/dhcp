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
 * <p>Title: DhcpV4HostnameOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4HostnameOption extends BaseStringOption
{
	/**
	 * Instantiates a new dhcp v4 hostname option.
	 */
	public DhcpV4HostnameOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp v4 hostname option.
	 * 
	 * @param v4DomainNameOption the v4 hostname option
	 */
	public DhcpV4HostnameOption(String v4Hostname)
	{
		super(v4Hostname);
		setCode(DhcpConstants.V4OPTION_HOSTNAME);
		setV4(true);
	}
}
