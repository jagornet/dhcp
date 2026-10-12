/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.v6;

import com.jagornet.dhcp.core.option.base.BaseUnsignedByteOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * <p>Title: DhcpV6ElapsedTimeOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6PreferenceOption extends BaseUnsignedByteOption
{
	/**
	 * Instantiates a new dhcp preference option.
	 */
	public DhcpV6PreferenceOption()
	{
		this((short)0);
	}
	
	/**
	 * Instantiates a new dhcp preference option.
	 * 
	 * @param preferenceOption the preference option
	 */
	public DhcpV6PreferenceOption(short preference)
	{
		super(preference);
		setCode(DhcpConstants.V6OPTION_PREFERENCE);
	}
}
