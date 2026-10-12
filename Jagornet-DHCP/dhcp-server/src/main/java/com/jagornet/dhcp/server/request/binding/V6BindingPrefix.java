/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.request.binding;

import java.util.Map;

import org.springframework.beans.BeanUtils;

import com.jagornet.dhcp.core.option.base.DhcpOption;
import com.jagornet.dhcp.server.config.DhcpConfigObject;
import com.jagornet.dhcp.server.config.DhcpV6OptionConfigObject;
import com.jagornet.dhcp.server.db.IaPrefix;

/**
 * The Class V6BindingPrefix.  A wrapper for an IaPrefix object with
 * a reference to the configuration object for that prefix.
 */
public class V6BindingPrefix extends IaPrefix implements BindingObject
{
	private DhcpConfigObject configObj;
	// the IaAddress superclass has the collection of the options stored 
	// in the database, but here we have map of configured options which
	// are filtered by the client requested options, if applicable, and
	// this map is used when populating the DHCPv6 reply message
	private Map<Integer, DhcpOption> dhcpOptionMap;
	private Map<Integer, DhcpOption> iaDhcpOptionMap;
	private Map<Integer, DhcpOption> iaAddrDhcpOptionMap;
	
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

	@Override
	public DhcpConfigObject getConfigObj() {
		return configObj;
	}

	public void setConfigObj(DhcpV6OptionConfigObject configObj) {
		this.configObj = configObj;
	}

	@Override
	public Map<Integer, DhcpOption> getDhcpOptionMap() {
		return dhcpOptionMap;
	}

	@Override
	public void setDhcpOptionMap(Map<Integer, DhcpOption> dhcpOptionMap) {
		this.dhcpOptionMap = dhcpOptionMap;
	}

	public Map<Integer, DhcpOption> getIaDhcpOptionMap() {
		return iaDhcpOptionMap;
	}

	public void setIaDhcpOptionMap(Map<Integer, DhcpOption> iaDhcpOptionMap) {
		this.iaDhcpOptionMap = iaDhcpOptionMap;
	}

	public Map<Integer, DhcpOption> getIaAddrDhcpOptionMap() {
		return iaAddrDhcpOptionMap;
	}

	public void setIaAddrDhcpOptionMap(Map<Integer, DhcpOption> iaAddrDhcpOptionMap) {
		this.iaAddrDhcpOptionMap = iaAddrDhcpOptionMap;
	}
	
}
