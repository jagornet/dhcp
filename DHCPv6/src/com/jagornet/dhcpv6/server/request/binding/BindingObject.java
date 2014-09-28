package com.jagornet.dhcpv6.server.request.binding;

import java.net.InetAddress;
import java.util.Date;

import com.jagornet.dhcpv6.server.config.DhcpConfigObject;

/**
 * Interface BindingObject.  
 * Common interface for V6BindingAddress, V6BindingPrefix and V4BindingAddress.
 * 
 * @author A. Gregory Rabil
 */
public interface BindingObject
{
	public void setState(byte state);
	public DhcpConfigObject getConfigObj();
	public void setStartTime(Date startDate);
	public void setPreferredEndTime(Date preferredDate);
	public void setValidEndTime(Date validDate);
	public InetAddress getIpAddress();
}
