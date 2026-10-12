/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.v6;

import com.jagornet.dhcp.core.option.base.BaseUnsignedIntOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * <p>Title: DhcpV6InfoRefreshTimeOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6InfoRefreshTimeOption extends BaseUnsignedIntOption
{
	/**
	 * Instantiates a new dhcp info refresh time option.
	 */
	public DhcpV6InfoRefreshTimeOption()
	{
		this((long)0);
	}
	
	/**
	 * Instantiates a new dhcp info refresh time option.
	 * 
	 * @param infoRefreshTimeOption the info refresh time option
	 */
	public DhcpV6InfoRefreshTimeOption(long infoRefreshTime)
	{
		super(infoRefreshTime);
		setCode(DhcpConstants.V6OPTION_INFO_REFRESH_TIME);
	}
}
