/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.request.ddns;

import com.jagornet.dhcp.core.option.v6.DhcpV6ClientFqdnOption;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.request.binding.V6BindingAddress;


public class DhcpV6DdnsComplete implements DdnsCallback
{
    /** The dhcp server config. */
    protected static DhcpServerConfiguration dhcpServerConfig = 
                                        DhcpServerConfiguration.getInstance();
	private V6BindingAddress bindingAddr;
	private DhcpV6ClientFqdnOption fqdnOption;
	
	public DhcpV6DdnsComplete(V6BindingAddress bindingAddr, 
			DhcpV6ClientFqdnOption fqdnOption) 
	{
		this.bindingAddr = bindingAddr;
		this.fqdnOption = fqdnOption;
	}
	
	public void fwdAddComplete(boolean success) {
		if (success) {
			dhcpServerConfig.getIaMgr().saveDhcpOption(bindingAddr, fqdnOption);
		}			
	}
	
	public void fwdDeleteComplete(boolean success) {
		if (success) {
			dhcpServerConfig.getIaMgr().deleteDhcpOption(bindingAddr, fqdnOption);
		}			
	}
	
	public void revAddComplete(boolean success) {
		if (success) {
			dhcpServerConfig.getIaMgr().saveDhcpOption(bindingAddr, fqdnOption);
		}
	}
	
	public void revDeleteComplete(boolean success) {
		if (success) {
			dhcpServerConfig.getIaMgr().deleteDhcpOption(bindingAddr, fqdnOption);
		}
	}
}
