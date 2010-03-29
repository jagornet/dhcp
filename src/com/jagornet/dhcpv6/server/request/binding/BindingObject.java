package com.jagornet.dhcpv6.server.request.binding;

import java.net.InetAddress;
import java.util.Date;

/**
 * Interface BindingObject.  Common interface for BindingAddress and BindingPrefix.
 * 
 * @author A. Gregory Rabil
 */
public interface BindingObject
{
	public void setState(byte state);
	public BindingPool getBindingPool();
	public void setStartTime(Date startDate);
	public void setPreferredEndTime(Date preferredDate);
	public void setValidEndTime(Date validDate);
	public InetAddress getIpAddress();
}
