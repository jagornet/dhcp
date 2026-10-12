/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.generic;

import com.jagornet.dhcp.core.option.base.BaseUnsignedShortOption;

/**
 * <p>Title: GenericUnsignedShortOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class GenericUnsignedShortOption extends BaseUnsignedShortOption
{	
	/**
	 * Instantiates a new generic unsigned short option.
	 * 
	 * @param code the option code
	 * @param name the option name
	 */
	public GenericUnsignedShortOption(int code, String name)
	{
		this(code, name, (int)0);
	}
	
	/**
	 * Instantiates a new generic unsigned short option.
	 * 
	 * @param code the option code
	 * @param name the option name
	 * @param unsignedShortOption the unsigned short option
	 */
	public GenericUnsignedShortOption(int code, String name, int unsignedShort)
	{
		super(unsignedShort);
		setCode(code);
		setName(name);
	}
}
