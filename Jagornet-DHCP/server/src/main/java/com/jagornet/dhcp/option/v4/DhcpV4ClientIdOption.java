/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV4ClientIdOption.java is part of Jagornet DHCP.
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

import com.jagornet.dhcp.option.OpaqueDataUtil;
import com.jagornet.dhcp.option.base.BaseOpaqueDataOption;
import com.jagornet.dhcp.util.DhcpConstants;
import com.jagornet.dhcp.xml.Operator;
import com.jagornet.dhcp.xml.V4ClientIdOption;

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
		super(v4ClientIdOption);
		setCode(DhcpConstants.V4OPTION_CLIENT_ID);
		setV4(true);
	}

    public boolean matches(V4ClientIdOption that, Operator.Enum op)
    {
        if (that == null)
            return false;
        if (that.getCode() != this.getCode())
            return false;
        if (that.getOpaqueData() == null)
        	return false;

        return OpaqueDataUtil.matches(opaqueData, that.getOpaqueData(), op);
    }
}
