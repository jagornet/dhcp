/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file V4StaticAddressBinding.java is part of Jagornet DHCP.
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

import java.util.Arrays;

import com.jagornet.dhcp.db.IdentityAssoc;
import com.jagornet.dhcp.message.DhcpMessage;
import com.jagornet.dhcp.option.v4.DhcpV4ConfigOptions;
import com.jagornet.dhcp.server.config.DhcpV4OptionConfigObject;
import com.jagornet.dhcp.util.Util;
import com.jagornet.dhcp.xml.PoliciesType;
import com.jagornet.dhcp.xml.V4AddressBinding;

public class V4StaticAddressBinding extends StaticBinding implements DhcpV4OptionConfigObject
{
	// the XML object wrapped by this class
	protected V4AddressBinding addressBinding;
	
	// The configured options for this binding
	protected DhcpV4ConfigOptions dhcpConfigOptions;
	
	public V4StaticAddressBinding(V4AddressBinding addressBinding)
	{
		this.addressBinding = addressBinding;
		dhcpConfigOptions = 
			new DhcpV4ConfigOptions(addressBinding.getConfigOptions());
	}
	
	@Override
	public boolean matches(byte[] duid, byte iatype, long iaid,
			DhcpMessage requestMsg)
	{
		boolean rc = false;
		if (addressBinding != null) {
			if (iatype == IdentityAssoc.V4_TYPE) {
				if (Arrays.equals(duid, addressBinding.getChaddr())) {
					return true;
				}
			}
		}
		return rc;
	}

	@Override
	public String getIpAddress() {
		return addressBinding.getIpAddress();
	}

	public V4AddressBinding getV4AddressBinding() {
		return addressBinding;
	}

	public void setV6AddressBinding(V4AddressBinding addressBinding) {
		this.addressBinding = addressBinding;
	}

	@Override
	public DhcpV4ConfigOptions getV4ConfigOptions() {
		return dhcpConfigOptions;
	}

	@Override
	public PoliciesType getPolicies() {
		if (addressBinding != null)
			return addressBinding.getPolicies();
		return null;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName() + ": ip=");
		sb.append(addressBinding.getIpAddress());
		sb.append(" chaddr=");
		sb.append(Util.toHexString(addressBinding.getChaddr()));
		return sb.toString();
	}
}
