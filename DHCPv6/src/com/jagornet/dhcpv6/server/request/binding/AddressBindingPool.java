/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file AddressBindingPool.java is part of DHCPv6.
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
import com.jagornet.dhcpv6.option.DhcpConfigOptions;
import com.jagornet.dhcpv6.server.config.DhcpOptionConfigObject;
import com.jagornet.dhcpv6.server.config.DhcpServerConfigException;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.AddressPool;
import com.jagornet.dhcpv6.xml.FiltersType;
import com.jagornet.dhcpv6.xml.LinkFilter;
import com.jagornet.dhcpv6.xml.PoliciesType;

/**
 * The Class AddressBindingPool.  A wrapper class for a configured AddressPool
 * for handling runtime activity for that pool.
 * 
 * @author A. Gregory Rabil
 */
public class AddressBindingPool implements BindingPool, DhcpOptionConfigObject
{
	private static Logger log = LoggerFactory.getLogger(AddressBindingPool.class);

	/** The range containing the start and end address for this pool */
	protected Range range;
	
	/** The free list. */
	protected FreeList freeList;
	
	/** The configured preferred lifetime for this pool */
	protected long preferredLifetime;
	
	/** The configured valid lifetime for this pool */
	protected long validLifetime;
	
	/** The address pool wrapped by this AddressBindingPool object */
	protected AddressPool pool;
	
	/** The configured options for this pool */
	protected DhcpConfigOptions dhcpConfigOptions;
	
	/** The LinkFilter containing this pool, if any */
	protected LinkFilter linkFilter; 
	
	/** The reaper which finds expired leases */
	protected Timer reaper;
	
	/**
	 * Instantiates a new binding pool.
	 * 
	 * @param pool the pool
	 * 
	 * @throws DhcpServerConfigException if the AddressPool definition is invalid
	 */
	public AddressBindingPool(AddressPool pool) throws DhcpServerConfigException
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
		dhcpConfigOptions = new DhcpConfigOptions(pool.getAddrConfigOptions());
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
					return InetAddress.getByAddress(next.toByteArray());
				}
				catch (UnknownHostException ex) {
					log.error("Unable to build IPv6 address from next free: " + ex);
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
	public AddressPool getAddressPool() {
		return pool;
	}
	
	/**
	 * Sets the pool.
	 * 
	 * @param pool the new pool
	 */
	public void setAddressPool(AddressPool pool) {
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
	
	public DhcpConfigOptions getDhcpConfigOptions() {
		return dhcpConfigOptions;
	}

	public void setDhcpConfigOptions(DhcpConfigOptions dhcpConfigOptions) {
		this.dhcpConfigOptions = dhcpConfigOptions;
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
}
