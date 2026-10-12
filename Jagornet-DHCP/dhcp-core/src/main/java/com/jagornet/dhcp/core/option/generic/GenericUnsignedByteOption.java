/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.generic;

import com.jagornet.dhcp.core.option.base.BaseUnsignedByteOption;

/**
 * <p>Title: GenericUnsignedByteOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class GenericUnsignedByteOption extends BaseUnsignedByteOption
{	
	/**
	 * Instantiates a new generic unsigned byte option.
	 * 
	 * @param code the option code
	 * @param name the option name
	 */
	public GenericUnsignedByteOption(int code, String name)
	{
		this(code, name, (short)0);
	}
	
	/**
	 * Instantiates a new generic unsigned byte option.
	 * 
	 * @param code the option code
	 * @param name the option name
	 * @param unsignedByteOption the unsigned byte option
	 */
	public GenericUnsignedByteOption(int code, String name, short unsignedByte)
	{
		super(unsignedByte);
		setCode(code);
		setName(name);
	}
}
