/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpUnknownOption.java is part of DHCPv6.
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
package com.jagornet.dhcp.option;

import com.jagornet.dhcp.option.base.BaseOpaqueDataOption;
import com.jagornet.dhcp.xml.OpaqueDataOptionType;

/**
 * <p>Title: DhcpUnknownOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpUnknownOption extends BaseOpaqueDataOption
{
	
	/** The option code for this unknown option. */
	protected int code;
	
	/**
	 * Instantiates a new dhcp unknown option.
	 */
	public DhcpUnknownOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp unknown option.
	 * 
	 * @param unknownOption the unknown option
	 */
	public DhcpUnknownOption(OpaqueDataOptionType unknownOption)
	{
		super(unknownOption);	
	}
}
