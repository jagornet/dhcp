/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.generic;

import com.jagornet.dhcp.core.option.base.BaseIpAddressOption;

/**
 * <p>Title: GenericIpAddressOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class GenericIpAddressOption extends BaseIpAddressOption
{
	/**
	 * Instantiates a new generic ip address option.
	 * 
	 * @param code the option code
	 * @param name the option name
	 */
	public GenericIpAddressOption(int code, String name)
	{
		this(code, name, null);
	}
	
	/**
	 * Instantiates a new generic ip address option.
	 * 
	 * @param code the option code
	 * @param name the option name
	 * @param ipAddressOption the ip address option
	 */
	public GenericIpAddressOption(int code, String name, String ipAddress)
	{
		super(ipAddress);
		setCode(code);
		setName(name);
	}

}
