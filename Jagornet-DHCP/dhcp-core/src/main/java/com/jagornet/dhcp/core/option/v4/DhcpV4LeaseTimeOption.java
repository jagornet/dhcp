/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.v4;

import com.jagornet.dhcp.core.option.base.BaseUnsignedIntOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * <p>Title: DhcpV4LeaseTimeOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4LeaseTimeOption extends BaseUnsignedIntOption
{
	/**
	 * Instantiates a new dhcpv4 lease time option.
	 */
	public DhcpV4LeaseTimeOption()
	{
		this((long)0);
	}
	
	/**
	 * Instantiates a new dhcpv4 lease time option.
	 * 
	 * @param v4LeaseTimeOption the v4 lease time option
	 */
	public DhcpV4LeaseTimeOption(long v4LeaseTime)
	{
		super(v4LeaseTime);
		setCode(DhcpConstants.V4OPTION_LEASE_TIME);
		setV4(true);
	}
}
