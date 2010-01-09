/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file FreeList.java is part of DHCPv6.
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
import java.util.BitSet;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class FreeList.
 */
public class FreeList
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(FreeList.class);
	
	/** The start. */
	protected BigInteger start;
	
	/** The end. */
	protected BigInteger end;

	/** The map of ranges, which are keyed by an index into a logical list of ranges, each offset by the maximum size of an integer. For example: key=0, value=BitSet for range of addresses from start to start+2147483647 key=1, value=BitSet for range of addresses from start+2147483648 to 4294967295 key=2, value=BitSet for range of addresses from start+4294967296 to 6442450943 ... */
	protected SortedMap<BigInteger, BitSet> bitsetRanges;
	
	/**
	 * Instantiates a new free list.
	 * 
	 * @param rangeStart the range start
	 * @param rangeEnd the range end
	 */
	public FreeList(InetAddress rangeStart, InetAddress rangeEnd)
	{
		start = new BigInteger(rangeStart.getAddress());
		end = new BigInteger(rangeEnd.getAddress());
		if (end.compareTo(start) > 0) {
			bitsetRanges = new TreeMap<BigInteger, BitSet>();
			bitsetRanges.put(BigInteger.ZERO, new BitSet());	// create one to start
		}
		else {
			throw new IllegalStateException("Invalid Pool: end address <= start address");
		}
	}

	/**
	 * Gets the bit set.
	 * 
	 * @param bi the bi
	 * 
	 * @return the bit set
	 */
	private BitSet getBitSet(BigInteger bi)
	{
		BitSet bitset = null;
		if ((bi.compareTo(start) >= 0) && (bi.compareTo(end) <= 0)) {
			BigInteger biIndex = bi.subtract(start).divide(BigInteger.valueOf(Integer.MAX_VALUE));
			bitset = bitsetRanges.get(biIndex);
			if (bitset == null) {
				bitset = new BitSet();
				bitsetRanges.put(biIndex, bitset);
			}
		}
		return bitset;
	}
	
	/**
	 * Gets the index.
	 * 
	 * @param bi the bi
	 * 
	 * @return the index
	 */
	private int getIndex(BigInteger bi)
	{
		return bi.subtract(start).mod(BigInteger.valueOf(Integer.MAX_VALUE)).intValue();		
	}
	
	/**
	 * Sets the.
	 * 
	 * @param ip the ip
	 * @param used the used
	 */
	private synchronized void set(InetAddress ip, boolean used)
	{
		BigInteger bi = new BigInteger(ip.getAddress());
		BitSet bitset = getBitSet(bi);
		if (bitset != null) {
			int ndx = getIndex(bi);
			if (used)
				bitset.set(ndx);
			else
				bitset.clear(ndx);
		}
	}

	/**
	 * Sets the used.
	 * 
	 * @param used the new used
	 */
	public void setUsed(InetAddress used)
	{
		this.set(used, true);
	}
	
	/**
	 * Sets the free.
	 * 
	 * @param free the new free
	 */
	public void setFree(InetAddress free)
	{
		this.set(free, false);
	}
	
	/**
	 * Checks if is used.
	 * 
	 * @param used the used
	 * 
	 * @return true, if is used
	 */
	public boolean isUsed(InetAddress used)
	{
		BigInteger bi = new BigInteger(used.getAddress());
		BitSet bitset = getBitSet(bi);
		if (bitset != null) {
			return bitset.get(getIndex(bi));
		}
		return false;
	}
	
	/**
	 * Checks if is free.
	 * 
	 * @param free the free
	 * 
	 * @return true, if is free
	 */
	public boolean isFree(InetAddress free)
	{
		return !this.isUsed(free);
	}
	
	/**
	 * Gets the next free address.
	 * 
	 * @return the next free address
	 */
	public synchronized InetAddress getNextFreeAddress()
	{
		BitSet bitset = null;
		int clearBit = -1;
		BigInteger mapIndex = BigInteger.ZERO;
		for (BitSet bsRange : bitsetRanges.values()) {
			clearBit = bsRange.nextClearBit(0);
			if (clearBit >= 0) {
				bsRange.set(clearBit);	// set it used
				bitset = bsRange;
				break;
			}
			// increment our index into the sorted map
			mapIndex = mapIndex.add(BigInteger.ONE);
		}
		if (clearBit < 0) {
			// assume nothing was free in the existing ranges, and allocate a new one
			clearBit = 0;
			bitset = new BitSet();
			bitset.set(clearBit);
			bitsetRanges.put(mapIndex, new BitSet());
		}
		BigInteger nextAddr = 	
			start.add(
					mapIndex.multiply(
							BigInteger.valueOf(Integer.MAX_VALUE)).add(
									BigInteger.valueOf(clearBit)));
		try {
			return InetAddress.getByAddress(nextAddr.toByteArray());
		}
		catch (UnknownHostException ex) {
			// we sure don't expect this to ever happen, but
			// if so, it seems we should clear the bit
			bitset.clear(clearBit);
			log.error("Failed to get next free address: " + ex);
		}
		return null;
	}
}
