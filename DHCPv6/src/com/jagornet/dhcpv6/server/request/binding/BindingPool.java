/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BindingPool.java is part of DHCPv6.
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

import java.net.InetAddress;
import java.util.Timer;

import com.jagornet.dhcpv6.db.IaAddress;
import com.jagornet.dhcpv6.util.Subnet;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.LinkFilter;
import com.jagornet.dhcpv6.xml.Pool;

// TODO: Auto-generated Javadoc
/**
 * The Class BindingPool.
 */
public class BindingPool
{
	
	/** The range. */
	protected Range range;
	
	/** The free list. */
	protected FreeList freeList;
	
	/** The preferred lifetime. */
	protected long preferredLifetime;
	
	/** The valid lifetime. */
	protected long validLifetime;
	
	/** The pool. */
	protected Pool pool;
	
	/** The link filter. */
	protected LinkFilter linkFilter;	// this LinkFilter containing this pool, if any
	
	/** The reaper. */
	protected Timer reaper;
	
	/**
	 * Instantiates a new binding pool.
	 * 
	 * @param pool the pool
	 * 
	 * @throws Exception the exception
	 */
	public BindingPool(Pool pool) throws Exception
	{
		this.pool = pool;
		this.range = new Range(pool.getRange());
		init();
	}
	
	/**
	 * Instantiates a new binding pool.
	 * 
	 * @param subnet the subnet
	 */
	public BindingPool(Subnet subnet)
	{
		this.range = new Range(subnet.getSubnetAddress(), subnet.getEndAddress());
		init();
	}
	
	/**
	 * Instantiates a new binding pool.
	 * 
	 * @param startAddr the start addr
	 * @param endAddr the end addr
	 */
	public BindingPool(InetAddress startAddr, InetAddress endAddr)
	{
		this.range = new Range(startAddr, startAddr);
		init();
	}
	
	/**
	 * Inits the.
	 */
	private void init()
	{
		if ((range.getStartAddress() != null) && (range.getEndAddress() != null)) {
			freeList = new FreeList(range.getStartAddress(), range.getEndAddress());
//			bindingMap = new ConcurrentHashMap<InetAddress, Binding>();
		}
		else {
			throw new IllegalStateException("Invalid Pool: start or end address is null");
		}
		
		reaper = new Timer(pool.getRange()+"_Reaper");
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
	 * Gets the next available address.
	 * 
	 * @return the next available address
	 */
	public InetAddress getNextAvailableAddress()
	{
		if (freeList != null)
			return freeList.getNextFreeAddress();
		
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
			freeList.setUsed(addr);
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
			freeList.setFree(addr);
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
		reaper.schedule(new ExpireTimerTask(iaAddr), secsUntilExpiration*1000);
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
	public Pool getPool() {
		return pool;
	}
	
	/**
	 * Sets the pool.
	 * 
	 * @param pool the new pool
	 */
	public void setPool(Pool pool) {
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
	
}
