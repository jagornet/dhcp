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
 * <p>Title: DhcpV6ReconfigureMessageOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6ReconfigureMessageOption extends BaseUnsignedByteOption
{
	/**
	 * Instantiates a new dhcp reconfigure message option.
	 */
	public DhcpV6ReconfigureMessageOption()
	{
		this((short)0);
	}
	
	/**
	 * Instantiates a new dhcp reconfigure message option.
	 * 
	 * @param reconfigureMessageOption the reconfigure message option
	 */
	public DhcpV6ReconfigureMessageOption(short reconfigureMessage)
	{
		super(reconfigureMessage);
		setCode(DhcpConstants.V6OPTION_RECONF_MSG);
	}
}
