/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.generic;

import java.util.List;

import com.jagornet.dhcp.core.option.base.BaseOpaqueData;
import com.jagornet.dhcp.core.option.base.BaseOpaqueDataListOption;

/**
 * <p>Title: GenericOpaqueDataListOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class GenericOpaqueDataListOption extends BaseOpaqueDataListOption
{	
	/**
	 * Instantiates a new generic opaque opaqueData list option.
	 * 
	 * @param code the option code
	 * @param name the option name
	 */
	public GenericOpaqueDataListOption(int code, String name)
	{
		this(code, name, null);
	}
	
	/**
	 * Instantiates a new generic opaque opaqueData list option.
	 * 
	 * @param code the option code
	 * @param name the option name
	 * @param opaqueDataListOption the opaque opaqueData list option
	 */
	public GenericOpaqueDataListOption(int code, String name, List<BaseOpaqueData> opaqueDataList)
	{
		super(opaqueDataList);
		setCode(code);
		setName(name);
	}
}
