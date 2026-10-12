/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.v4;

import com.jagornet.dhcp.core.option.base.BaseIpAddressOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * <p>Title: DhcpV4ServerIdOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4ServerIdOption extends BaseIpAddressOption
{
	/**
	 * Instantiates a new dhcp server id option.
	 */
	public DhcpV4ServerIdOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp server id option.
	 * 
	 * @param serverIdOption the server id option
	 */
	public DhcpV4ServerIdOption(String serverId)
	{
		super(serverId);
		setCode(DhcpConstants.V4OPTION_SERVERID);
		setV4(true);
	}
}
