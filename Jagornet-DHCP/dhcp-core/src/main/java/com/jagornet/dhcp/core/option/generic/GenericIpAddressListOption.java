/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.generic;

import java.util.List;

import com.jagornet.dhcp.core.option.base.BaseIpAddressListOption;

/**
 * <p>Title: GenericIpAddressListOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class GenericIpAddressListOption extends BaseIpAddressListOption
{
	/**
	 * Instantiates a new generic ip address list option.
	 * 
	 * @param code the option code
	 * @param name the option name
	 */
	public GenericIpAddressListOption(int code, String name)
	{
		this(code, name, null);
	}
	
	/**
	 * Instantiates a new generic ip address list option.
	 * 
	 * @param code the option code
	 * @param name the option name
	 * @param ipAddressListOption the ip address list option
	 */
	public GenericIpAddressListOption(int code, String name, List<String> ipAddresses)
	{
		super(ipAddresses);
		setCode(code);
		setName(name);
	}
}
