package com.jagornet.dhcpv6.server.request.binding;

import java.util.Arrays;

import com.jagornet.dhcpv6.db.IdentityAssoc;
import com.jagornet.dhcpv6.message.DhcpMessageInterface;
import com.jagornet.dhcpv6.option.DhcpConfigOptions;
import com.jagornet.dhcpv6.server.config.DhcpOptionConfigObject;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.PoliciesType;
import com.jagornet.dhcpv6.xml.PrefixBinding;

public class StaticPrefixBinding extends StaticBinding implements DhcpOptionConfigObject
{
	// the XML object wrapped by this class
	protected PrefixBinding prefixBinding;
	
	/** The configured options for this binding */
	protected DhcpConfigOptions dhcpConfigOptions;
	
	public StaticPrefixBinding(PrefixBinding prefixBinding)
	{
		this.prefixBinding = prefixBinding;
		dhcpConfigOptions = 
			new DhcpConfigOptions(prefixBinding.getPrefixConfigOptions());
	}
	
	@Override
	public boolean matches(byte[] duid, byte iatype, long iaid,
			DhcpMessageInterface requestMsg)
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

	public PrefixBinding getPrefixBinding() {
		return prefixBinding;
	}

	public void setPrefixBinding(PrefixBinding prefixBinding) {
		this.prefixBinding = prefixBinding;
	}

	@Override
	public DhcpConfigOptions getDhcpConfigOptions() {
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
		sb.append("StaticPrefixBinding: Prefix=");
		sb.append(prefixBinding.getPrefix());
		sb.append("/");
		sb.append(prefixBinding.getPrefixLength());
		sb.append(" duid=");
		sb.append(Util.toHexString(prefixBinding.getDuid().getHexValue()));
		sb.append(" iaid=");
		sb.append(prefixBinding.getIaid());
		return sb.toString();
	}
}
