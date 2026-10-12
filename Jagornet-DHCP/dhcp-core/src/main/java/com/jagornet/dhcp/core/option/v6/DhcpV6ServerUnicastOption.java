/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.v6;

import com.jagornet.dhcp.core.option.base.BaseIpAddressOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * <p>Title: DhcpV6ServerUnicastOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6ServerUnicastOption extends BaseIpAddressOption
{
	/**
	 * Instantiates a new dhcp server unicast option.
	 */
	public DhcpV6ServerUnicastOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp server unicast option.
	 * 
	 * @param serverUnicastOption the server unicast option
	 */
	public DhcpV6ServerUnicastOption(String serverUnicast)
	{
		super(serverUnicast);
		setCode(DhcpConstants.V6OPTION_UNICAST);
	}
}
