/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.v6;

import com.jagornet.dhcp.core.option.base.BaseUnsignedShortOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * <p>Title: DhcpV6ElapsedTimeOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6ElapsedTimeOption extends BaseUnsignedShortOption
{
	/**
	 * Instantiates a new dhcp elapsed time option.
	 */
	public DhcpV6ElapsedTimeOption()
	{
		this((int)0);
	}
	
	/**
	 * Instantiates a new dhcp elapsed time option.
	 * 
	 * @param elapsedTimeOption the elapsed time option
	 */
	public DhcpV6ElapsedTimeOption(int elapsedTime)
	{
		super(elapsedTime);
		setCode(DhcpConstants.V6OPTION_ELAPSED_TIME);
	}
}
