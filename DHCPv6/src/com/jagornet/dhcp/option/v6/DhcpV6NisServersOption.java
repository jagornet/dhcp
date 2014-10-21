/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6NisServersOption.java is part of DHCPv6.
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
package com.jagornet.dhcp.option.v6;

import com.jagornet.dhcp.option.base.BaseIpAddressListOption;
import com.jagornet.dhcp.util.DhcpConstants;
import com.jagornet.dhcp.xml.V6NisServersOption;

/**
 * <p>Title: DhcpV6NisServersOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6NisServersOption extends BaseIpAddressListOption
{
	/**
	 * Instantiates a new dhcp nis servers option.
	 */
	public DhcpV6NisServersOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp nis servers option.
	 * 
	 * @param nisServersOption the nis servers option
	 */
	public DhcpV6NisServersOption(V6NisServersOption nisServersOption)
	{
		super(nisServersOption);
		setCode(DhcpConstants.V6OPTION_NIS_SERVERS);
	}
}
