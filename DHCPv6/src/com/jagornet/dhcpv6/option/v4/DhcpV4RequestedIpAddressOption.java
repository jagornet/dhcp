/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV4SubnetMaskOption.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.option.base.BaseIpAddressOption;
import com.jagornet.dhcpv6.xml.V4RequestedIpAddressOption;

/**
 * <p>Title: DhcpV4RequestedIpAddressOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4RequestedIpAddressOption extends BaseIpAddressOption
{
	
	/**
	 * Instantiates a new dhcp v4 requested ip address option.
	 */
	public DhcpV4RequestedIpAddressOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp v4 requested ip address option.
	 * 
	 * @param v4RequestedIpAddressOption the v4 requested ip address option
	 */
	public DhcpV4RequestedIpAddressOption(V4RequestedIpAddressOption v4RequestedIpAddressOption)
	{
		if (v4RequestedIpAddressOption != null)
			this.ipAddressOption = v4RequestedIpAddressOption;
		else
			this.ipAddressOption = V4RequestedIpAddressOption.Factory.newInstance();
		
		super.setV4(true);
	}
	
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return ((V4RequestedIpAddressOption)ipAddressOption).getCode();
    }
}
