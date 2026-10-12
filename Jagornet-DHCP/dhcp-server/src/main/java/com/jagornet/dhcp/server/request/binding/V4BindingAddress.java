/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
