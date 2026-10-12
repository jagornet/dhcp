/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.v4;

import com.jagornet.dhcp.core.option.base.BaseOpaqueData;
import com.jagornet.dhcp.core.option.base.BaseOpaqueDataOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * <p>Title: DhcpV4ClientIdOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4ClientIdOption extends BaseOpaqueDataOption
{
	/**
	 * Instantiates a new dhcpv4 client id option.
	 */
	public DhcpV4ClientIdOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcpv4 client id option.
	 * 
	 * @param v4ClientIdOption the client id option
	 */
	public DhcpV4ClientIdOption(BaseOpaqueData baseOpaqueData)
	{
		super(baseOpaqueData);
		setCode(DhcpConstants.V4OPTION_CLIENT_ID);
		setV4(true);
	}
	
}
