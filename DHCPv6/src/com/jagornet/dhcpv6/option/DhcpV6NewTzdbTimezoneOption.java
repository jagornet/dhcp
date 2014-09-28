/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6NewTzdbTimezoneOption.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.option;

import com.jagornet.dhcpv6.option.base.BaseStringOption;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcp.xml.V6NewTzdbTimezoneOption;

/**
 * <p>Title: DhcpV6NewTzdbTimezoneOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6NewTzdbTimezoneOption extends BaseStringOption
{
	/**
	 * Instantiates a new dhcp new tzdb timezone option.
	 */
	public DhcpV6NewTzdbTimezoneOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp new tzdb timezone option.
	 * 
	 * @param newTzdbTimezoneOption the new tzdb timezone option
	 */
	public DhcpV6NewTzdbTimezoneOption(V6NewTzdbTimezoneOption newTzdbTimezoneOption)
	{
		super(newTzdbTimezoneOption);
		setCode(DhcpConstants.OPTION_NEW_TZDB_TIMEZONE);
	}
}
