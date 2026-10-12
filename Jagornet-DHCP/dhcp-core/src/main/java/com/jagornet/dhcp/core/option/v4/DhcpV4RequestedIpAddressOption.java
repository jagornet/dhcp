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
 * <p>Title: DhcpV4RequestedIpAddressOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4RequestedIpAddressOption extends BaseIpAddressOption
{
	/**
	 * Instantiates a new dhcp v4 requested ip address option.
	 */
	public DhcpV4RequestedIpAddressOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp v4 requested ip address option.
	 * 
	 * @param v4RequestedIpAddressOption the v4 requested ip address option
	 */
	public DhcpV4RequestedIpAddressOption(String v4RequestedIpAddress)
	{
		super(v4RequestedIpAddress);
		setCode(DhcpConstants.V4OPTION_REQUESTED_IP);
		super.setV4(true);
	}
}
