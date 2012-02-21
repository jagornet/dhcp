package com.jagornet.dhcpv6.server.request.binding;

import java.util.Arrays;

import com.jagornet.dhcpv6.message.DhcpMessageInterface;
import com.jagornet.dhcpv6.option.DhcpConfigOptions;
import com.jagornet.dhcpv6.server.config.DhcpOptionConfigObject;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.AddressBinding;
import com.jagornet.dhcpv6.xml.PoliciesType;

public class StaticAddressBinding extends StaticBinding implements DhcpOptionConfigObject
{
	// the XML object wrapped by this class
	protected AddressBinding addressBinding;
	protected byte iaType;
	
	/** The configured options for this binding */
	protected DhcpConfigOptions dhcpConfigOptions;
	
	public StaticAddressBinding(AddressBinding addressBinding, byte iaType)
	{
		this.addressBinding = addressBinding;
		this.iaType = iaType;
		dhcpConfigOptions = 
			new DhcpConfigOptions(addressBinding.getAddrConfigOptions());
	}
	
	@Override
	public boolean matches(byte[] duid, byte iatype, long iaid,
			DhcpMessageInterface requestMsg)
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

	public AddressBinding getAddressBinding() {
		return addressBinding;
	}

	public void setAddressBinding(AddressBinding addressBinding) {
		this.addressBinding = addressBinding;
	}

	@Override
	public DhcpConfigOptions getDhcpConfigOptions() {
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
		sb.append("StaticAddressBinding: iatype=");
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
