/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.generic;

import com.jagornet.dhcp.core.option.base.BaseUnsignedIntOption;

/**
 * <p>Title: GenericUnsignedIntOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class GenericUnsignedIntOption extends BaseUnsignedIntOption
{	
	/**
	 * Instantiates a new generic unsigned int option.
	 * 
	 * @param code the option code
	 * @param name the option name
	 */
	public GenericUnsignedIntOption(int code, String name)
	{
		this(code, name, (long)0);
	}
	
	/**
	 * Instantiates a new generic unsigned int option.
	 * 
	 * @param code the option code
	 * @param name the option name
	 * @param unsignedIntOption the unsigned int option
	 */
	public GenericUnsignedIntOption(int code, String name, long unsignedInt)
	{
		super(unsignedInt);
		setCode(code);
		setName(name);
	}
}
