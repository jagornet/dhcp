/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV4ClientIdOption.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.option.OpaqueDataUtil;
import com.jagornet.dhcpv6.option.base.BaseOpaqueDataOption;
import com.jagornet.dhcpv6.xml.Operator;
import com.jagornet.dhcpv6.xml.V4ClientIdOption;

/**
 * <p>Title: DhcpV4ClientIdOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4ClientIdOption extends BaseOpaqueDataOption
{
	
	/**
	 * Instantiates a new dhcpv4 client id option.
	 */
	public DhcpV4ClientIdOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcpv4 client id option.
	 * 
	 * @param v4ClientIdOption the client id option
	 */
	public DhcpV4ClientIdOption(V4ClientIdOption v4ClientIdOption)
	{
		if (v4ClientIdOption != null) {
			this.opaqueDataOption = v4ClientIdOption;
		}
		else {
			this.opaqueDataOption = V4ClientIdOption.Factory.newInstance();
            // create an OpaqueData element to actually hold the data
            this.opaqueDataOption.addNewOpaqueData();
		}
		super.setV4(true);
	}
	
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return ((V4ClientIdOption)opaqueDataOption).getCode();
    }    

    public boolean matches(V4ClientIdOption that, Operator.Enum op)
    {
        if (that == null)
            return false;
        if (that.getCode() != this.getCode())
            return false;
        if (that.getOpaqueData() == null)
        	return false;

        return OpaqueDataUtil.matches(opaqueDataOption.getOpaqueData(), 
        								that.getOpaqueData(), op);
    }
}
