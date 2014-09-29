package com.jagornet.dhcp.server.request.binding;

import java.math.BigInteger;
import java.net.InetAddress;

import com.jagornet.dhcp.server.config.DhcpConfigObject;
import com.jagornet.dhcp.xml.LinkFilter;

/**
 * Interface BindingPool.  
 * Common interface for V6AddressBindingPool, V6PrefixBindingPool and V4AddressBindingPool
 * 
 * @author A. Gregory Rabil
 */
public interface BindingPool extends DhcpConfigObject
{
	public InetAddress getStartAddress();
	public InetAddress getEndAddress();
	public InetAddress getNextAvailableAddress();
	public void setUsed(InetAddress addr);
	public void setFree(InetAddress addr);
	public boolean contains(InetAddress addr);
	public LinkFilter getLinkFilter();
	public BigInteger getSize();
}
