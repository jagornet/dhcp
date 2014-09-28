/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV4DomainServersOption.java is part of DHCPv6.
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
import com.jagornet.dhcp.xml.V4DomainServersOption;

/**
 * <p>Title: DhcpV4DomainServersOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4DomainServersOption extends BaseIpAddressListOption
{
	/**
	 * Instantiates a new dhcp v4 domain servers option.
	 */
	public DhcpV4DomainServersOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp v4 domain servers option.
	 * 
	 * @param v4DomainServersOption the v4 domain servers option
	 */
	public DhcpV4DomainServersOption(V4DomainServersOption v4DomainServersOption)
	{
		super(v4DomainServersOption);
		setCode(DhcpConstants.V4OPTION_DOMAIN_SERVERS);
		setV4(true);
	}
}
