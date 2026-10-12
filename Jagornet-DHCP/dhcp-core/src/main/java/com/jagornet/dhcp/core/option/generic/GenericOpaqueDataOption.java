/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.generic;

import com.jagornet.dhcp.core.option.base.BaseOpaqueData;
import com.jagornet.dhcp.core.option.base.BaseOpaqueDataOption;

/**
 * <p>Title: GenericOpaqueDataOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class GenericOpaqueDataOption extends BaseOpaqueDataOption
{	
	/**
	 * Instantiates a new generic opaque opaqueData option.
	 * 
	 * @param code the option code
	 * @param name the option name
	 */
	public GenericOpaqueDataOption(int code, String name)
	{
		this(code, name, null);
	}
	
	/**
	 * Instantiates a new generic opaque opaqueData option.
	 * 
	 * @param code the option code
	 * @param name the option name
	 * @param opaqueDataOption the opaque opaqueData option
	 */
	public GenericOpaqueDataOption(int code, String name, BaseOpaqueData opaqueData)
	{
		super(opaqueData);
		setCode(code);
		setName(name);
	}
}
