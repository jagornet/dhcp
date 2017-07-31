/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file V6BindingPrefix.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.server.request.binding;

import org.springframework.beans.BeanUtils;

import com.jagornet.dhcp.db.IaPrefix;
import com.jagornet.dhcp.server.config.DhcpConfigObject;
import com.jagornet.dhcp.server.config.DhcpV6OptionConfigObject;

/**
 * The Class V6BindingPrefix.  A wrapper for an IaPrefix object with
 * a reference to the configuration object for that prefix.
 */
public class V6BindingPrefix extends IaPrefix implements BindingObject
{
	private DhcpConfigObject configObj;
	
	/**
	 * Instantiates a new binding address.
	 * 
	 * @param iaPrefix the ia addr
	 * @param configObj the config object
	 */
	public V6BindingPrefix(IaPrefix iaPrefix, DhcpConfigObject configObj)
	{
		// populate *this* IaPrefix from the given one
		BeanUtils.copyProperties(iaPrefix, this);
		this.configObj = configObj;
	}

	public DhcpConfigObject getConfigObj() {
		return configObj;
	}

	public void setConfigObj(DhcpV6OptionConfigObject configObj) {
		this.configObj = configObj;
	}
}
