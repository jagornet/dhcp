package com.jagornet.dhcpv6.server.request.binding;

import java.net.InetAddress;

import com.jagornet.dhcpv6.xml.LinkFilter;

/**
 * Interface BindingPool.  Common interface for Address and Prefix BindingPools.
 * 
 * @author A. Gregory Rabil
 */
public interface BindingPool
{
	public InetAddress getStartAddress();
	public InetAddress getEndAddress();
	public InetAddress getNextAvailableAddress();
	public void setUsed(InetAddress addr);
	public void setFree(InetAddress addr);
	public boolean contains(InetAddress addr);
	public LinkFilter getLinkFilter();
	public long getPreferredLifetime();
	public long getValidLifetime();
	public long getPreferredLifetimeMs();
	public long getValidLifetimeMs();
}
