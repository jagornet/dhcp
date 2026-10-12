/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.generic;

import java.util.List;

import com.jagornet.dhcp.core.option.base.BaseUnsignedShortListOption;

/**
 * <p>Title: GenericUnsignedShortListOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class GenericUnsignedShortListOption extends BaseUnsignedShortListOption
{	
	/**
	 * Instantiates a new generic unsigned short list option.
	 * 
	 * @param code the option code
	 * @param name the option name
	 */
	public GenericUnsignedShortListOption(int code, String name)
	{
		this(code, name, null);
	}
	
	/**
	 * Instantiates a new generic unsigned short list option.
	 * 
	 * @param code the option code
	 * @param name the option name
	 * @param unsignedShortListOption the unsigned short list option
	 */
	public GenericUnsignedShortListOption(int code, String name, List<Integer> unsignedShorts)
	{
		super(unsignedShorts);
		setCode(code);
		setName(name);
	}
}
