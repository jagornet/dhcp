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
 * <p>Title: DhcpV4SubnetMaskOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4SubnetMaskOption extends BaseIpAddressOption
{
	/**
	 * Instantiates a new dhcp v4 subnet mask option.
	 */
	public DhcpV4SubnetMaskOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp v4 subnet mask option.
	 * 
	 * @param v4SubnetMaskOption the v4 subnet mask option
	 */
	public DhcpV4SubnetMaskOption(String v4SubnetMask)
	{
		super(v4SubnetMask);
		setCode(DhcpConstants.V4OPTION_SUBNET_MASK);
		setV4(true);
	}
}
