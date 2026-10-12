/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.v6;

import java.util.List;

import com.jagornet.dhcp.core.option.base.BaseIpAddressListOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * <p>Title: DhcpV6NisServersOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6NisServersOption extends BaseIpAddressListOption
{
	/**
	 * Instantiates a new dhcp nis servers option.
	 */
	public DhcpV6NisServersOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp nis servers option.
	 * 
	 * @param nisServersOption the nis servers option
	 */
	public DhcpV6NisServersOption(List<String> nisServers)
	{
		super(nisServers);
		setCode(DhcpConstants.V6OPTION_NIS_SERVERS);
	}
}
