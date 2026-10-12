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
 * <p>Title: DhcpV4RoutersOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4RoutersOption extends BaseIpAddressListOption
{
	/**
	 * Instantiates a new dhcp v4 routers option.
	 */
	public DhcpV4RoutersOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp v4 routers option.
	 * 
	 * @param v4RoutersOption the v4 routers option
	 */
	public DhcpV4RoutersOption(List<String> v4Routers)
	{
		super(v4Routers);
		setCode(DhcpConstants.V4OPTION_ROUTERS);
		setV4(true);
	}
}
