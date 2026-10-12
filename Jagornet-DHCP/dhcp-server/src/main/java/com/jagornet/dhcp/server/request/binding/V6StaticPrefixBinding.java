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
import com.jagornet.dhcp.server.config.xml.V6PrefixBinding;
import com.jagornet.dhcp.server.db.IdentityAssoc;

public class V6StaticPrefixBinding extends StaticBinding implements DhcpV6OptionConfigObject
{
	// the XML object wrapped by this class
	protected V6PrefixBinding prefixBinding;
	
	
	protected DhcpV6ConfigOptions msgConfigOptions;
	protected DhcpV6ConfigOptions iaConfigOptions;
	protected DhcpV6ConfigOptions prefixConfigOptions;
	
	public V6StaticPrefixBinding(V6PrefixBinding prefixBinding)
	{
		this.prefixBinding = prefixBinding;
		msgConfigOptions = new DhcpV6ConfigOptions(prefixBinding.getMsgConfigOptions());
		iaConfigOptions = new DhcpV6ConfigOptions(prefixBinding.getIaConfigOptions());
		prefixConfigOptions = new DhcpV6ConfigOptions(prefixBinding.getPrefixConfigOptions());
	}
	
	@Override
	public boolean matches(byte[] duid, byte iatype, long iaid,
			DhcpMessage requestMsg)
	{
		boolean rc = false;
		if (prefixBinding != null) {
			if (iatype == IdentityAssoc.PD_TYPE) {
				if (Arrays.equals(duid, prefixBinding.getDuid().getHexValue())) {
					if (prefixBinding.getIaid() == null) {
						return true;
					}
					else {
						if (iaid == prefixBinding.getIaid()) {
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
		return prefixBinding.getPrefix();
	}

	public V6PrefixBinding getV6PrefixBinding() {
		return prefixBinding;
	}

	public void setV6PrefixBinding(V6PrefixBinding prefixBinding) {
		this.prefixBinding = prefixBinding;
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
		return getPrefixConfigOptions();
	}

	public DhcpV6ConfigOptions getPrefixConfigOptions() {
		return prefixConfigOptions;
	}

	public void setPrefixConfigOptions(DhcpV6ConfigOptions prefixConfigOptions) {
		this.prefixConfigOptions = prefixConfigOptions;
	}

	@Override
	public PoliciesType getPolicies() {
		if (prefixBinding != null)
			return prefixBinding.getPolicies();
		return null;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName() + ": Prefix=");
		sb.append(prefixBinding.getPrefix());
		sb.append('/');
		sb.append(prefixBinding.getPrefixLength());
		sb.append(" duid=");
		sb.append(Util.toHexString(prefixBinding.getDuid().getHexValue()));
		sb.append(" iaid=");
		sb.append(prefixBinding.getIaid());
		return sb.toString();
	}
}
