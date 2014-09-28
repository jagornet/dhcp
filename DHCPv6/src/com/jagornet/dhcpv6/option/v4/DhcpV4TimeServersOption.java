/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV4TimeServersOption.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.option.v4;

import com.jagornet.dhcpv6.option.base.BaseIpAddressListOption;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcp.xml.V4TimeServersOption;

/**
 * <p>Title: DhcpV4TimeServersOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4TimeServersOption extends BaseIpAddressListOption
{
	/**
	 * Instantiates a new dhcp v4 time servers option.
	 */
	public DhcpV4TimeServersOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp v4 time servers option.
	 * 
	 * @param v4TimeServersOption the v4 time servers option
	 */
	public DhcpV4TimeServersOption(V4TimeServersOption v4TimeServersOption)
	{
		super(v4TimeServersOption);
		setCode(DhcpConstants.V4OPTION_TIME_SERVERS);
		setV4(true);
	}
}
