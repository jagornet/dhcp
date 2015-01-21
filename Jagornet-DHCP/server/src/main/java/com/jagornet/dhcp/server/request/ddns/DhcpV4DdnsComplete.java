/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV4DdnsComplete.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.server.request.ddns;

import com.jagornet.dhcp.option.v4.DhcpV4ClientFqdnOption;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.request.binding.V4BindingAddress;

public class DhcpV4DdnsComplete implements DdnsCallback
{
    /** The dhcp server config. */
    protected static DhcpServerConfiguration dhcpServerConfig = 
                                        DhcpServerConfiguration.getInstance();
	private V4BindingAddress bindingAddr;
	private DhcpV4ClientFqdnOption fqdnOption;
	
	public DhcpV4DdnsComplete(V4BindingAddress bindingAddr, 
			DhcpV4ClientFqdnOption replyFqdnOption) 
	{
		this.bindingAddr = bindingAddr;
		this.fqdnOption = replyFqdnOption;
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
