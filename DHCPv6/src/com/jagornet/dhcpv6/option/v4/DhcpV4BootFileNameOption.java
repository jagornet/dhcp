/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV4BootFileNameOption.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.option.base.BaseStringOption;
import com.jagornet.dhcpv6.xml.V4BootFileNameOption;

/**
 * <p>Title: DhcpV4BootFileNameOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4BootFileNameOption extends BaseStringOption
{
	
	/**
	 * Instantiates a new dhcp v4 boot file name option.
	 */
	public DhcpV4BootFileNameOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp v4 boot file name option.
	 * 
	 * @param v4DomainNameOption the v4 boot file name option
	 */
	public DhcpV4BootFileNameOption(V4BootFileNameOption v4BootFileNameOption)
	{
		if (v4BootFileNameOption != null)
			this.stringOption = v4BootFileNameOption;
		else
			this.stringOption = V4BootFileNameOption.Factory.newInstance();
		
		super.setV4(true);
	}
	
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return ((V4BootFileNameOption)stringOption).getCode();
    }
}
