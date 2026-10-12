/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.v4;

import java.util.List;

import com.jagornet.dhcp.core.option.base.BaseIpAddressListOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * <p>Title: DhcpV4TimeServersOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4TimeServersOption extends BaseIpAddressListOption
{
	/**
	 * Instantiates a new dhcp v4 time servers option.
	 */
	public DhcpV4TimeServersOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp v4 time servers option.
	 * 
	 * @param v4TimeServersOption the v4 time servers option
	 */
	public DhcpV4TimeServersOption(List<String> v4TimeServers)
	{
		super(v4TimeServers);
		setCode(DhcpConstants.V4OPTION_TIME_SERVERS);
		setV4(true);
	}
}
