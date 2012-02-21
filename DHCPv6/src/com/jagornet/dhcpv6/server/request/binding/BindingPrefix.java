/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BindingPrefix.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.server.request.binding;

import org.springframework.beans.BeanUtils;

import com.jagornet.dhcpv6.db.IaPrefix;
import com.jagornet.dhcpv6.server.config.DhcpConfigObject;
import com.jagornet.dhcpv6.server.config.DhcpOptionConfigObject;

/**
 * The Class BindingPrefix.  A wrapper for an IaPrefix object with
 * a reference to the configuration object for that prefix.
 */
public class BindingPrefix extends IaPrefix implements BindingObject
{
	/** The config obj. */
	private DhcpConfigObject configObj;
	
	/**
	 * Instantiates a new binding address.
	 * 
	 * @param iaPrefix the ia addr
	 * @param bindingPool the binding pool
	 */
	public BindingPrefix(IaPrefix iaPrefix, DhcpConfigObject configObj)
	{
		// populate *this* IaPrefix from the given one
		BeanUtils.copyProperties(iaPrefix, this);
		this.configObj = configObj;
	}

	public DhcpConfigObject getConfigObj() {
		return configObj;
	}

	public void setConfigObj(DhcpOptionConfigObject configObj) {
		this.configObj = configObj;
	}
}
