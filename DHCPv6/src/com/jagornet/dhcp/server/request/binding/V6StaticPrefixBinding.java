package com.jagornet.dhcp.server.request.binding;

import java.util.Arrays;

import com.jagornet.dhcp.db.IdentityAssoc;
import com.jagornet.dhcp.message.DhcpMessage;
import com.jagornet.dhcp.option.v6.DhcpV6ConfigOptions;
import com.jagornet.dhcp.server.config.DhcpV6OptionConfigObject;
import com.jagornet.dhcp.util.Util;
import com.jagornet.dhcp.xml.PoliciesType;
import com.jagornet.dhcp.xml.V6PrefixBinding;

public class V6StaticPrefixBinding extends StaticBinding implements DhcpV6OptionConfigObject
{
	// the XML object wrapped by this class
	protected V6PrefixBinding prefixBinding;
	
	// The configured options for this binding
	protected DhcpV6ConfigOptions dhcpConfigOptions;
	
	public V6StaticPrefixBinding(V6PrefixBinding prefixBinding)
	{
		this.prefixBinding = prefixBinding;
		dhcpConfigOptions = 
			new DhcpV6ConfigOptions(prefixBinding.getPrefixConfigOptions());
	}
	
	@Override
	public boolean matches(byte[] duid, byte iatype, long iaid,
			DhcpMessage requestMsg)
	{
		boolean rc = false;
		if (prefixBinding != null) {
			if (iatype == IdentityAssoc.PD_TYPE) {
				if (Arrays.equals(duid, prefixBinding.getDuid().getHexValue())) {
					if (!prefixBinding.isSetIaid()) {
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
	public DhcpV6ConfigOptions getDhcpConfigOptions() {
		return dhcpConfigOptions;
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
