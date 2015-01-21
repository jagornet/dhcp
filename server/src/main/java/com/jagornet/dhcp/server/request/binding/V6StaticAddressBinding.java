/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file V6StaticAddressBinding.java is part of Jagornet DHCP.
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

import com.jagornet.dhcp.message.DhcpMessage;
import com.jagornet.dhcp.option.v6.DhcpV6ConfigOptions;
import com.jagornet.dhcp.server.config.DhcpV6OptionConfigObject;
import com.jagornet.dhcp.util.Util;
import com.jagornet.dhcp.xml.V6AddressBinding;
import com.jagornet.dhcp.xml.PoliciesType;

public class V6StaticAddressBinding extends StaticBinding implements DhcpV6OptionConfigObject
{
	// the XML object wrapped by this class
	protected V6AddressBinding addressBinding;
	protected byte iaType;
	
	// The configured options for this binding
	protected DhcpV6ConfigOptions dhcpConfigOptions;
	
	public V6StaticAddressBinding(V6AddressBinding addressBinding, byte iaType)
	{
		this.addressBinding = addressBinding;
		this.iaType = iaType;
		dhcpConfigOptions = 
			new DhcpV6ConfigOptions(addressBinding.getAddrConfigOptions());
	}
	
	@Override
	public boolean matches(byte[] duid, byte iatype, long iaid,
			DhcpMessage requestMsg)
	{
		boolean rc = false;
		if (addressBinding != null) {
			if (iatype == this.iaType) {
				if (Arrays.equals(duid, addressBinding.getDuid().getHexValue())) {
					if (!addressBinding.isSetIaid()) {
						return true;
					}
					else {
						if (iaid == addressBinding.getIaid()) {
							return true;
						}
					}
				}
			}
		}
		return rc;
	}
	
	@Override
	public String getIpAddress() {
		return addressBinding.getIpAddress();
	}

	public V6AddressBinding getV6AddressBinding() {
		return addressBinding;
	}

	public void setV6AddressBinding(V6AddressBinding addressBinding) {
		this.addressBinding = addressBinding;
	}

	@Override
	public DhcpV6ConfigOptions getDhcpConfigOptions() {
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
		sb.append(this.getClass().getSimpleName() + ": iatype=");
		sb.append(iaType);
		sb.append(" ip=");
		sb.append(addressBinding.getIpAddress());
		sb.append(" duid=");
		sb.append(Util.toHexString(addressBinding.getDuid().getHexValue()));
		sb.append(" iaid=");
		sb.append(addressBinding.getIaid());
		return sb.toString();
	}
}
