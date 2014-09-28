package com.jagornet.dhcpv6.server.request.binding;

import java.util.Arrays;

import com.jagornet.dhcpv6.db.IdentityAssoc;
import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.v4.DhcpV4ConfigOptions;
import com.jagornet.dhcpv6.server.config.DhcpV4OptionConfigObject;
import com.jagornet.dhcpv6.util.Util;
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
