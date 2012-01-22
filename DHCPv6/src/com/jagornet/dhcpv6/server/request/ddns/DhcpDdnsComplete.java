package com.jagornet.dhcpv6.server.request.ddns;

import com.jagornet.dhcpv6.option.DhcpClientFqdnOption;
import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;
import com.jagornet.dhcpv6.server.request.binding.BindingAddress;


public class DhcpDdnsComplete implements DdnsCallback
{
    /** The dhcp server config. */
    protected static DhcpServerConfiguration dhcpServerConfig = 
                                        DhcpServerConfiguration.getInstance();
	private BindingAddress bindingAddr;
	private DhcpClientFqdnOption fqdnOption;
	
	public DhcpDdnsComplete(BindingAddress bindingAddr, 
			DhcpClientFqdnOption fqdnOption) 
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
