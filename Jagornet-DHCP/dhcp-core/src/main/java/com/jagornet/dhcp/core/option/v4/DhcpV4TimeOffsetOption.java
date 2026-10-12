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
 * <p>Title: DhcpV4TimeOffsetOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4TimeOffsetOption extends BaseUnsignedIntOption
{
	/**
	 * Instantiates a new dhcpv4 time offset option.
	 */
	public DhcpV4TimeOffsetOption()
	{
		this((long)0);
	}
	
	/**
	 * Instantiates a new dhcpv4 time offset option.
	 * 
	 * @param v4TimeOffsetOption the v4 time offset option
	 */
	public DhcpV4TimeOffsetOption(long v4TimeOffset)
	{
		super(v4TimeOffset);
		setCode(DhcpConstants.V4OPTION_TIME_OFFSET);
		setV4(true);
	}
}
