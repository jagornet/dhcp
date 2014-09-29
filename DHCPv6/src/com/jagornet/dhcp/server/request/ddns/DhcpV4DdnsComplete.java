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
