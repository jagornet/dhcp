/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file GenericUnsignedShortListOption.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.option.generic;

import com.jagornet.dhcpv6.option.base.BaseUnsignedShortListOption;
import com.jagornet.dhcp.xml.UnsignedShortListOptionType;

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
	public GenericUnsignedShortListOption(int code, String name,
										  UnsignedShortListOptionType unsignedShortListOption)
	{
		super(unsignedShortListOption);
		setCode(code);
		setName(name);
	}
}
