/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV4HostnameOption.java is part of Jagornet DHCP.
 *
 *   Jagornet DHCP is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Jagornet DHCP is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Jagornet DHCP.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcp.option.v4;

import com.jagornet.dhcp.option.base.BaseStringOption;
import com.jagornet.dhcp.util.DhcpConstants;
import com.jagornet.dhcp.xml.V4HostnameOption;

/**
 * <p>Title: DhcpV4HostnameOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4HostnameOption extends BaseStringOption
{
	/**
	 * Instantiates a new dhcp v4 hostname option.
	 */
	public DhcpV4HostnameOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp v4 hostname option.
	 * 
	 * @param v4DomainNameOption the v4 hostname option
	 */
	public DhcpV4HostnameOption(V4HostnameOption v4HostnameOption)
	{
		super(v4HostnameOption);
		setCode(DhcpConstants.V4OPTION_HOSTNAME);
		setV4(true);
	}
}
