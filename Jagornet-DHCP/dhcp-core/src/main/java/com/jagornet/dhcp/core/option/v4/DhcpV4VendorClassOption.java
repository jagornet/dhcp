/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV4VendorClassOption.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.core.option.v4;

import com.jagornet.dhcp.core.option.base.BaseOpaqueData;
import com.jagornet.dhcp.core.option.base.BaseOpaqueDataOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * <p>Title: DhcpV4VendorClassOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4VendorClassOption extends BaseOpaqueDataOption
{
	/**
	 * Instantiates a new dhcp vendor class option.
	 */
	public DhcpV4VendorClassOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp vendor class option.
	 * 
	 * @param v4VendorClassOption the vendor class option
	 */
	public DhcpV4VendorClassOption(BaseOpaqueData baseOpaqueData)
	{
		super(baseOpaqueData);
		setCode(DhcpConstants.V4OPTION_VENDOR_CLASS);
		setV4(true);
	}

}
