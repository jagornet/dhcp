/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6UserClassOption.java is part of DHCPv6.
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

import com.jagornet.dhcp.option.base.BaseOpaqueDataListOption;
import com.jagornet.dhcp.util.DhcpConstants;
import com.jagornet.dhcp.xml.Operator;
import com.jagornet.dhcp.xml.V6UserClassOption;

/**
 * <p>Title: DhcpV6ClientIdOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6UserClassOption extends BaseOpaqueDataListOption
{
	/**
	 * Instantiates a new dhcp user class option.
	 */
	public DhcpV6UserClassOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp user class option.
	 * 
	 * @param userClassOption the user class option
	 */
	public DhcpV6UserClassOption(V6UserClassOption userClassOption)
	{
		super(userClassOption);
		setCode(DhcpConstants.V6OPTION_USER_CLASS);
	}
    
    public boolean matches(DhcpV6UserClassOption that, Operator.Enum op)
    {
        if (that == null)
            return false;
        if (that.getCode() != this.getCode())
            return false;

        return super.matches(that, op);
    }
}
