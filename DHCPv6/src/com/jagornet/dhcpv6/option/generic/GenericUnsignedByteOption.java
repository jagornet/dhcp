/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file GenericUnsignedByteOption.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.option.base.BaseUnsignedByteOption;
import com.jagornet.dhcpv6.xml.UnsignedByteOptionType;

/**
 * <p>Title: GenericUnsignedByteOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class GenericUnsignedByteOption extends BaseUnsignedByteOption
{	
	/** The option code. */
	private int code;
	
	/**
	 * Instantiates a new generic unsigned byte option.
	 * 
	 * @param code the option code
	 * @param name the option name
	 */
	public GenericUnsignedByteOption(int code, String name)
	{
		this(code, name, null);
	}
	
	/**
	 * Instantiates a new generic unsigned byte option.
	 * 
	 * @param code the option code
	 * @param name the option name
	 * @param unsignedByteOption the unsigned byte option
	 */
	public GenericUnsignedByteOption(int code, String name,
									 UnsignedByteOptionType unsignedByteOption)
	{
		super(unsignedByteOption);
		this.code = code;
		super.setName(name);
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
	 */
	public int getCode()
	{
		return code;
	}
}
