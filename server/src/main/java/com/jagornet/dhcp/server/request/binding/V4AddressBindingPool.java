/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file V4AddressBindingPool.java is part of Jagornet DHCP.
 *
 *   Jagornet DHCP is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Jagornet DHCP is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Jagornet DHCP.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcp.server.request.binding;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.db.IaAddress;
import com.jagornet.dhcp.option.v4.DhcpV4ConfigOptions;
import com.jagornet.dhcp.server.config.DhcpServerConfigException;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpV4OptionConfigObject;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.util.Util;
import com.jagornet.dhcp.xml.FiltersType;
import com.jagornet.dhcp.xml.LinkFilter;
import com.jagornet.dhcp.xml.PoliciesType;
import com.jagornet.dhcp.xml.V4AddressPool;

/**
 * The Class V4AddressBindingPool.  A wrapper class for a configured V4AddressPool
 * for handling runtime activity for that pool.
 * 
 * @author A. Gregory Rabil
 */
public class V4AddressBindingPool implements BindingPool, DhcpV4OptionConfigObject
{
	private static Logger log = LoggerFactory.getLogger(V4AddressBindingPool.class);

	protected Range range;
	protected FreeList freeList;
	protected long leasetime;
	protected V4AddressPool pool;
	protected DhcpV4ConfigOptions v4ConfigOptions;
	protected LinkFilter linkFilter; 
	protected Timer reaper;
	
	/**
	 * Instantiates a new binding pool.
	 * 
	 * @param pool the pool
	 * 
	 * @throws DhcpServerConfigException if the AddressPool definition is invalid
	 */
	public V4AddressBindingPool(V4AddressPool pool) throws DhcpServerConfigException
	{
		this.pool = pool;
		try {
			this.range = new Range(pool.getRange());
		} 
		catch (NumberFormatException ex) {
			log.error("Invalid AddressPool definition", ex);
			throw new DhcpServerConfigException("Invalid AddressPool definition", ex);
		} 
		catch (UnknownHostException ex) {
			log.error("Invalid AddressPool definition", ex);
			throw new DhcpServerConfigException("Invalid AddressPool definition", ex);
		}
		freeList = 
			new FreeList(new BigInteger(range.getStartAddress().getAddress()),
					new BigInteger(range.getEndAddress().getAddress()));
		reaper = new Timer(pool.getRange()+"_Reaper");
		v4ConfigOptions = new DhcpV4ConfigOptions(pool.getConfigOptions());
	}
	
	/**
	 * Gets the range.
	 * 
	 * @return the range
	 */
	public Range getRange()
	{
		return range;
	}
	
	/**
	 * Gets the next available address in this address pool.
	 * 
	 * @return the next available address
	 */
	public InetAddress getNextAvailableAddress()
	{
		if (freeList != null) {
			BigInteger next = freeList.getNextFree();
			if (next != null) {
				try {
					InetAddress ip = InetAddress.getByAddress(next.toByteArray());
					int pingCheckTimeout = 
						DhcpServerPolicies.globalPolicyAsInt(Property.V4_PINGCHECK_TIMEOUT);
					if (pingCheckTimeout > 0) {
						try {
							if (ip.isReachable(pingCheckTimeout)) {
								log.warn("Next free address answered ping check: " + 
										ip.getHostAddress());
								setUsed(ip);
								return getNextAvailableAddress();	// try again
							}
						}
						catch (IOException ex) {
							log.error("Failed to perform v4 ping check: " + ex);
						}
					}
					return ip;
				}
				catch (UnknownHostException ex) {
					log.error("Unable to build IPv4 address from next free: " + ex);
				}
			}
		}		
		return null;
	}
	
	/**
	 * Sets an IP address in this address pool as used.
	 * 
	 * @param addr the address to set used
	 */
	public void setUsed(InetAddress addr)
	{
		if (contains(addr)) {
			freeList.setUsed(new BigInteger(addr.getAddress()));
		}
	}
	
	/**
	 * Sets an IP address in this address pool as free.
	 * 
	 * @param addr the address to set free
	 */
	public void setFree(InetAddress addr)
	{
		if (contains(addr)) {
			freeList.setFree(new BigInteger(addr.getAddress()));
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
	 * Test if the given address is contained within this address pool.
	 * 
	 * @param addr the address to test for containment in this address pool
	 * 
	 * @return true, if successful
	 */
	public boolean contains(InetAddress addr)
	{
		if ((Util.compareInetAddrs(addr, range.getStartAddress()) >= 0) &&
				(Util.compareInetAddrs(addr, range.getEndAddress()) <= 0)) {
			return true;
		}
		return false;
	}

	/**
	 * Gets the start address.
	 * 
	 * @return the start address
	 */
	public InetAddress getStartAddress() {
		return range.getStartAddress();
	}

	/**
	 * Sets the start address.
	 * 
	 * @param startAddress the new start address
	 */
	public void setStartAddress(InetAddress startAddress) {
		range.setEndAddress(startAddress);
	}

	/**
	 * Gets the end address.
	 * 
	 * @return the end address
	 */
	public InetAddress getEndAddress() {
		return range.getEndAddress();
	}

	/**
	 * Sets the end address.
	 * 
	 * @param endAddress the new end address
	 */
	public void setEndAddress(InetAddress endAddress) {
		range.setEndAddress(endAddress);
	}

	/**
	 * Gets the leasetime.
	 * 
	 * @return the leasetime
	 */
	public long getLeasetime() {
		return leasetime;
	}
	
	/**
	 * Gets the leasetime ms.
	 * 
	 * @return the leasetime ms
	 */
	public long getLeasetimeMs() {
		return leasetime*1000;
	}

	/**
	 * Sets the leasetime.
	 * 
	 * @param leasetime the new leasetime
	 */
	public void setLeasetime(long leasetime) {
		this.leasetime = leasetime;
	}
	
	/**
	 * Gets the pool.
	 * 
	 * @return the pool
	 */
	public V4AddressPool getV4AddressPool() {
		return pool;
	}
	
	/**
	 * Sets the pool.
	 * 
	 * @param pool the new pool
	 */
	public void setV4AddressPool(V4AddressPool pool) {
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
	
	public DhcpV4ConfigOptions getV4ConfigOptions() {
		return v4ConfigOptions;
	}

	public void setV4ConfigOptions(DhcpV4ConfigOptions v4ConfigOptions) {
		this.v4ConfigOptions = v4ConfigOptions;
	}

	public String toString()
	{
		return range.getStartAddress().getHostAddress() + "-" + 
				range.getEndAddress().getHostAddress();
	}
	
	public String freeListToString()
	{
		return freeList.toString();
	}

	@Override
	public long getPreferredLifetime() {
		// TODO: check this overloaded use of v6 interface
		return getLeasetime();
	}

	@Override
	public long getValidLifetime() {
		// TODO: check this overloaded use of v6 interface
		return getLeasetime();
	}

	@Override
	public long getPreferredLifetimeMs() {
		// TODO: check this overloaded use of v6 interface
		return getLeasetimeMs();
	}

	@Override
	public long getValidLifetimeMs() {
		// TODO: check this overloaded use of v6 interface
		return getLeasetimeMs();
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
		return range.size();
	}
}
