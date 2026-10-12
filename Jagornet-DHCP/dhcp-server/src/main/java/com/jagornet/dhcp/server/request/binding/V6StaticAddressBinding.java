/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.request.binding;

import java.util.Arrays;

import com.jagornet.dhcp.core.message.DhcpMessage;
import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.config.DhcpV6ConfigOptions;
import com.jagornet.dhcp.server.config.DhcpV6OptionConfigObject;
import com.jagornet.dhcp.server.config.xml.PoliciesType;
import com.jagornet.dhcp.server.config.xml.V6AddressBinding;

public class V6StaticAddressBinding extends StaticBinding implements DhcpV6OptionConfigObject
{
	// the XML object wrapped by this class
	protected V6AddressBinding addressBinding;
	protected byte iaType;
	
	protected DhcpV6ConfigOptions msgConfigOptions;
	protected DhcpV6ConfigOptions iaConfigOptions;
	protected DhcpV6ConfigOptions addrConfigOptions;
	
	public V6StaticAddressBinding(V6AddressBinding addressBinding, byte iaType)
	{
		this.addressBinding = addressBinding;
		this.iaType = iaType;
		msgConfigOptions = new DhcpV6ConfigOptions(addressBinding.getMsgConfigOptions());
		iaConfigOptions = new DhcpV6ConfigOptions(addressBinding.getIaConfigOptions());
		addrConfigOptions = new DhcpV6ConfigOptions(addressBinding.getAddrConfigOptions());
	}
	
	@Override
	public boolean matches(byte[] duid, byte iatype, long iaid,
			DhcpMessage requestMsg)
	{
		boolean rc = false;
		if (addressBinding != null) {
			if (iatype == this.iaType) {
				if (Arrays.equals(duid, addressBinding.getDuid().getHexValue())) {
					if (addressBinding.getIaid() == null) {
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
	public DhcpV6ConfigOptions getMsgConfigOptions() {
		return msgConfigOptions;
	}

	public void setMsgConfigOptions(DhcpV6ConfigOptions msgConfigOptions) {
		this.msgConfigOptions = msgConfigOptions;
	}

	@Override
	public DhcpV6ConfigOptions getIaConfigOptions() {
		return iaConfigOptions;
	}

	public void setIaConfigOptions(DhcpV6ConfigOptions iaConfigOptions) {
		this.iaConfigOptions = iaConfigOptions;
	}

	@Override
	public DhcpV6ConfigOptions getAddrConfigOptions() {
		return addrConfigOptions;
	}

	public void setAddrConfigOptions(DhcpV6ConfigOptions addrConfigOptions) {
		this.addrConfigOptions = addrConfigOptions;
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
