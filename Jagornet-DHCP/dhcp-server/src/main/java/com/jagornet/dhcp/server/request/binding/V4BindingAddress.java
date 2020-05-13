/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file V4BindingAddress.java is part of Jagornet DHCP.
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

import java.util.Map;

import com.jagornet.dhcp.core.option.base.DhcpOption;
import com.jagornet.dhcp.server.config.DhcpConfigObject;
import com.jagornet.dhcp.server.db.IaAddress;

/**
 * The Class V4BindingAddress.  A wrapper for an IaAddress object with
 * a reference to the configuration object for that address.
 * 
 * @author A. Gregory Rabil
 */
public class V4BindingAddress extends IaAddress implements BindingObject
{
	private DhcpConfigObject configObj;
	// the IaAddress superclass has the collection of the options stored 
	// in the database, but here we have map of configured options which
	// are filtered by the client requested options, if applicable, and
	// this map is used when populating the DHCPv4 reply message
	private Map<Integer, DhcpOption> dhcpOptionMap;
	
	/**
	 * Instantiates a new binding address.
	 * 
	 * @param iaAddr the ia addr
	 * @param configObj the config object
	 */
	public V4BindingAddress(IaAddress iaAddr, DhcpConfigObject configObj)
	{
		// populate *this* IaAddress from the given one
		//BeanUtils.copyProperties(iaAddr, this);
		super.copyFrom(iaAddr);
		this.configObj = configObj;
	}

	public DhcpConfigObject getConfigObj() {
		return configObj;
	}

	public void setConfigObj(DhcpConfigObject configObj) {
		this.configObj = configObj;
	}

	public Map<Integer, DhcpOption> getDhcpOptionMap() {
		return dhcpOptionMap;
	}

	public void setDhcpOptionMap(Map<Integer, DhcpOption> dhcpOptionMap) {
		this.dhcpOptionMap = dhcpOptionMap;
	}
}
