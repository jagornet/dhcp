/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file GenericOpaqueDataOption.java is part of DHCPv6.
 *
 *   DHCPv6 is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   DHCPv6 is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with DHCPv6.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcp.option.generic;

import com.jagornet.dhcp.option.base.BaseOpaqueDataOption;
import com.jagornet.dhcp.xml.OpaqueDataOptionType;

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
	public GenericOpaqueDataOption(int code, String name,
								   OpaqueDataOptionType opaqueDataOption)
	{
		super(opaqueDataOption);
		setCode(code);
		setName(name);
	}
}
