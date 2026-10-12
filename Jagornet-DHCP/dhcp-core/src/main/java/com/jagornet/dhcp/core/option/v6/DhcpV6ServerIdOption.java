/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.v6;

import com.jagornet.dhcp.core.option.base.BaseOpaqueData;
import com.jagornet.dhcp.core.option.base.BaseOpaqueDataOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * <p>Title: DhcpV6ServerIdOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6ServerIdOption extends BaseOpaqueDataOption
{
	/**
	 * Instantiates a new dhcp server id option.
	 */
	public DhcpV6ServerIdOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp server id option.
	 * 
	 * @param serverIdOption the server id option
	 */
	public DhcpV6ServerIdOption(BaseOpaqueData baseOpaqueData)
	{
		super(baseOpaqueData);
		setCode(DhcpConstants.V6OPTION_SERVERID);
	}
}
