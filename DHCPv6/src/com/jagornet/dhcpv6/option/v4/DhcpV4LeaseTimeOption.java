/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpElapsedTimeOption.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.option.base.BaseUnsignedIntOption;
import com.jagornet.dhcpv6.xml.V4LeaseTimeOption;

/**
 * <p>Title: DhcpV4LeaseTimeOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4LeaseTimeOption extends BaseUnsignedIntOption
{
	
	/**
	 * Instantiates a new dhcpv4 lease time option.
	 */
	public DhcpV4LeaseTimeOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcpv4 lease time option.
	 * 
	 * @param v4LeaseTimeOption the v4 lease time option
	 */
	public DhcpV4LeaseTimeOption(V4LeaseTimeOption v4LeaseTimeOption)
	{
		if (v4LeaseTimeOption != null)
			this.uIntOption = v4LeaseTimeOption;
		else
			this.uIntOption = V4LeaseTimeOption.Factory.newInstance();
		
		super.setV4(true);
	}
	
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return ((V4LeaseTimeOption)uIntOption).getCode();
    }
}
