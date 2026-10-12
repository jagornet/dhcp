/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.generic;

import com.jagornet.dhcp.core.option.base.BaseStringOption;

/**
 * <p>Title: GenericStringOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class GenericStringOption extends BaseStringOption
{	
	/**
	 * Instantiates a new generic string option.
	 * 
	 * @param code the option code
	 * @param name the option name
	 */
	public GenericStringOption(int code, String name)
	{
		this(code, name, null);
	}
	
	/**
	 * Instantiates a new generic string option.
	 * 
	 * @param code the option code
	 * @param name the option name
	 * @param stringOption the string option
	 */
	public GenericStringOption(int code, String name, String string)
	{
		super(string);
		setCode(code);
		setName(name);
	}
}
