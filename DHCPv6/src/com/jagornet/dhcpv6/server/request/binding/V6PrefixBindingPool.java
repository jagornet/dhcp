/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file V6PrefixBindingPool.java is part of DHCPv6.
 *
 *   DHCPv6 is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   DHCPv6 is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with DHCPv6.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcpv6.server.request.binding;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.db.IaAddress;
import com.jagornet.dhcpv6.option.DhcpV6ConfigOptions;
import com.jagornet.dhcpv6.server.config.DhcpV6OptionConfigObject;
import com.jagornet.dhcpv6.server.config.DhcpServerConfigException;
import com.jagornet.dhcpv6.util.Subnet;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcp.xml.FiltersType;
import com.jagornet.dhcp.xml.LinkFilter;
import com.jagornet.dhcp.xml.PoliciesType;
import com.jagornet.dhcp.xml.V6PrefixPool;

/**
 * The Class V6PrefixBindingPool.
 */
public class V6PrefixBindingPool implements BindingPool, DhcpV6OptionConfigObject
{
	private static Logger log = LoggerFactory.getLogger(V6PrefixBindingPool.class);
	
	protected Subnet subnet;
	protected int allocPrefixLen;
	protected FreeList freeList;
	protected long preferredLifetime;
	protected long validLifetime;
	protected V6PrefixPool pool;
	protected DhcpV6ConfigOptions dhcpConfigOptions;
	protected LinkFilter linkFilter; 
	protected Timer reaper;
	
	/**
	 * Instantiates a new binding pool.
	 * 
	 * @param pool the pool
	 * 
	 * @throws DhcpServerConfigException if the PrefixPool definition is invalid
	 */
	public V6PrefixBindingPool(V6PrefixPool pool) throws DhcpServerConfigException
	{
		try {
			this.pool = pool;
			allocPrefixLen = pool.getPrefixLength();
			String[] cidr = pool.getRange().split("/");
			if ((cidr == null) || (cidr.length != 2)) {
				throw new DhcpServerConfigException(
						"Prefix pool must be specified in prefix/len notation");
			}
			subnet = new Subnet(cidr[0], cidr[1]);
			if (allocPrefixLen < subnet.getPrefixLength()) {
				throw new DhcpServerConfigException(
						"Allocation prefix length must be greater or equal to pool prefix length");			
			}
			int numPrefixes = (int) Math.pow(2,(allocPrefixLen - subnet.getPrefixLength()));
			freeList = new FreeList(BigInteger.ZERO, 
					BigInteger.valueOf(numPrefixes).subtract(BigInteger.ONE));
			reaper = new Timer(pool.getRange()+"_Reaper");
			dhcpConfigOptions = new DhcpV6ConfigOptions(pool.getPrefixConfigOptions());
		} 
		catch (NumberFormatException ex) {
			log.error("Invalid PrefixPool definition", ex);
			throw new DhcpServerConfigException("Invalid PrefixPool definition", ex);
		} 
		catch (UnknownHostException ex) {
			log.error("Invalid PrefixPool definition", ex);
			throw new DhcpServerConfigException("Invalid PrefixPool definition", ex);
		}		
	}
	
	public int getAllocPrefixLen()
	{
		return allocPrefixLen;
	}
	
	private BigInteger calculatePrefix()
	{
		return BigInteger.valueOf(2).pow(128-allocPrefixLen);
	}
	
	/**
	 * Gets the next available prefix.
	 * 
	 * @return the next available prefix
	 */
	public InetAddress getNextAvailableAddress()
	{
		if (freeList != null) {
			BigInteger start = new BigInteger(subnet.getSubnetAddress().getAddress());
			BigInteger next = freeList.getNextFree();
			if (next != null) {
				//TODO: if there are no more free addresses, then find one
				//		that has been released or has been expired
				try {
					BigInteger prefix = next.multiply(calculatePrefix());
					return InetAddress.getByAddress(start.add(prefix).toByteArray());
				}
				catch (Exception ex) {
					log.error("Unable to build IPv6 prefix from next free: " + ex);
				}
			}
		}
		return null;
	}
	
	/**
	 * Sets the used.
	 * 
	 * @param addr the new used
	 */
	public void setUsed(InetAddress addr)
	{
		if (contains(addr)) {
			BigInteger prefix = new BigInteger(addr.getAddress());
			BigInteger start = new BigInteger(subnet.getSubnetAddress().getAddress());
			freeList.setUsed(prefix.subtract(start).divide(calculatePrefix()));
		}
	}
	
	/**
	 * Sets the free.
	 * 
	 * @param addr the new free
	 */
	public void setFree(InetAddress addr)
	{
		if (contains(addr)) {
			BigInteger prefix = new BigInteger(addr.getAddress());
			BigInteger start = new BigInteger(subnet.getSubnetAddress().getAddress());
			freeList.setFree(prefix.subtract(start).divide(calculatePrefix()));
		}
	}
	
	/**
	 * Start expire timer task.
	 * 
	 * @param iaAddr the ia addr
	 * @param secsUntilExpiration the secs until expiration
	 */
	public void startExpireTimerTask(IaAddress iaAddr, long secsUntilExpiration)
	{
		// convert delay from seconds (lifetime) --> milliseconds (delay)
//		reaper.schedule(new ExpireTimerTask(iaAddr), secsUntilExpiration*1000);
	}
	
	/**
	 * Contains.
	 * 
	 * @param addr the addr
	 * 
	 * @return true, if successful
	 */
	public boolean contains(InetAddress addr)
	{
		if ((Util.compareInetAddrs(addr, subnet.getSubnetAddress()) >= 0) &&
				(Util.compareInetAddrs(addr, subnet.getEndAddress()) <= 0)) {
			return true;
		}
		return false;
	}
	
	public InetAddress getStartAddress() {
		return subnet.getSubnetAddress();
	}
	
	public InetAddress getEndAddress() {
		return subnet.getEndAddress();
	}

	/**
	 * Gets the preferred lifetime.
	 * 
	 * @return the preferred lifetime
	 */
	public long getPreferredLifetime() {
		return preferredLifetime;
	}
	
	/**
	 * Gets the preferred lifetime ms.
	 * 
	 * @return the preferred lifetime ms
	 */
	public long getPreferredLifetimeMs() {
		return preferredLifetime*1000;
	}

	/**
	 * Sets the preferred lifetime.
	 * 
	 * @param preferredLifetime the new preferred lifetime
	 */
	public void setPreferredLifetime(long preferredLifetime) {
		this.preferredLifetime = preferredLifetime;
	}

	/**
	 * Gets the valid lifetime.
	 * 
	 * @return the valid lifetime
	 */
	public long getValidLifetime() {
		return validLifetime;
	}
	
	/**
	 * Gets the valid lifetime ms.
	 * 
	 * @return the valid lifetime ms
	 */
	public long getValidLifetimeMs() {
		return validLifetime*1000;
	}

	/**
	 * Sets the valid lifetime.
	 * 
	 * @param validLifetime the new valid lifetime
	 */
	public void setValidLifetime(long validLifetime) {
		this.validLifetime = validLifetime;
	}
	
	/**
	 * Gets the pool.
	 * 
	 * @return the pool
	 */
	public V6PrefixPool getV6PrefixPool() {
		return pool;
	}
	
	/**
	 * Sets the pool.
	 * 
	 * @param pool the new pool
	 */
	public void setV6PrefixPool(V6PrefixPool pool) {
		this.pool = pool;
	}

	/**
	 * Gets the link filter.
	 * 
	 * @return the link filter
	 */
	public LinkFilter getLinkFilter() {
		return linkFilter;
	}

	/**
	 * Sets the link filter.
	 * 
	 * @param linkFilter the new link filter
	 */
	public void setLinkFilter(LinkFilter linkFilter) {
		this.linkFilter = linkFilter;
	}
	
	public DhcpV6ConfigOptions getDhcpConfigOptions() {
		return dhcpConfigOptions;
	}

	public void setDhcpConfigOptions(DhcpV6ConfigOptions dhcpConfigOptions) {
		this.dhcpConfigOptions = dhcpConfigOptions;
	}

	public String toString()
	{
		return subnet.getSubnetAddress().getHostAddress() + "/" + subnet.getPrefixLength();
	}

	@Override
	public FiltersType getFilters() {
		if (pool != null)
			return pool.getFilters();
		return null;
	}

	@Override
	public PoliciesType getPolicies() {
		if (pool != null)
			return pool.getPolicies();
		return null;
	}
	
	public BigInteger getSize() {
		// TODO: account for delegated prefix size
		return new BigInteger(subnet.getEndAddress().getAddress()).
				subtract(new BigInteger(subnet.getSubnetAddress().getAddress()));
	}
}
