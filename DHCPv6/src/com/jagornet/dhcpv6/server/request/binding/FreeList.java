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
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * The Class FreeList.
 * 
 * @author A. Gregory Rabil
 */
public class FreeList
{	
	/** The start. */
	protected BigInteger start;
	
	/** The end. */
	protected BigInteger end;

	/** 
	 * The map of ranges, which are keyed by an index into a list of ranges, 
	 * each offset by the maximum size of an integer. For example: 
	 * index=0, for range of values from start to start+2147483647 
	 * index=1, for range of values from start+2147483648 to 4294967295 
	 * index=2, for range of values from start+4294967296 to 6442450943 
	 * ... 
	 */
	protected List<BitSet> bitsetRanges;
	
	protected int nextFreeIndex;
	
	/**
	 * Instantiates a new free list.
	 * 
	 * @param start the range start
	 * @param end the range end
	 */
	public FreeList(BigInteger start, BigInteger end)
	{
		this.start = start;
		this.end = end;
		if (end.compareTo(start) >= 0) {
			bitsetRanges = new ArrayList<BitSet>();
			bitsetRanges.add(new BitSet());	// create one to start
		}
		else {
			throw new IllegalStateException("Failed to create FreeList: end < start");
		}
	}
	
	public boolean isInList(BigInteger bi)
	{
		if ((bi.compareTo(start) >= 0) && (bi.compareTo(end) <= 0)) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * Gets the index into the list for the given BigInteger
	 * 
	 * @param bi the bi
	 * 
	 * @return the index position in the list
	 */
	protected int getIndex(BigInteger bi)
	{
		return bi.subtract(start).divide(BigInteger.valueOf(Integer.MAX_VALUE)).intValue();
	}
	
	/**
	 * Gets the offset into the BitSet for the given BigInteger
	 * 
	 * @param bi the bi
	 * 
	 * @return the offset
	 */
	protected int getOffset(BigInteger bi)
	{
		return bi.subtract(start).mod(BigInteger.valueOf(Integer.MAX_VALUE)).intValue();		
	}
	
	/**
	 * Sets the.
	 * 
	 * @param bi the bi
	 * @param used the used
	 */
	protected synchronized void set(BigInteger bi, boolean used)
	{
		if (isInList(bi)) {
			int offset = getOffset(bi);
			BitSet bitset = null;
			int ndx = getIndex(bi);
			if (ndx < bitsetRanges.size()) {
				bitset = bitsetRanges.get(ndx);
			}
			else {
				while (ndx >= bitsetRanges.size()) {
					bitset = new BitSet();
					bitsetRanges.add(bitset);
				}
			}
			if (used) {
				bitset.set(offset);
			}
			else {
				bitset.clear(offset);
				if (ndx < nextFreeIndex) {
					nextFreeIndex = ndx;	// reset next free search index
				}
			}
		}
	}
	
	/**
	 * Sets the used.
	 * 
	 * @param used the new used
	 */
	public void setUsed(BigInteger used)
	{
		this.set(used, true);
	}
	
	/**
	 * Sets the free.
	 * 
	 * @param free the new free
	 */
	public void setFree(BigInteger free)
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
	public boolean isUsed(BigInteger used)
	{
		if (isInList(used)) {
			int ndx = getIndex(used);
			if (ndx < bitsetRanges.size()) {
				BitSet bitset = bitsetRanges.get(ndx);
				if (bitset != null) {
					return bitset.get(getOffset(used));
				}
			}
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
	public boolean isFree(BigInteger free)
	{
		return !this.isUsed(free);
	}
	
	/**
	 * Gets the next free.
	 * 
	 * @return the next free
	 */
	public synchronized BigInteger getNextFree()
	{
		BigInteger next = start.add(BigInteger.valueOf(nextFreeIndex).
										multiply(BigInteger.valueOf(Integer.MAX_VALUE)));
		int clearBit = -1;
		BitSet bitset = bitsetRanges.get(nextFreeIndex);
		clearBit = bitset.nextClearBit(0);
		if (clearBit >= 0) {
			next = next.add(BigInteger.valueOf(clearBit));
			if (isInList(next)) {
				bitset.set(clearBit);
				return next;
			}
		}
		else {
			// no more available in the last BitSet, so the next available
			// would be the first in the next BitSet, so add max offset
			next = next.add(BigInteger.valueOf(Integer.MAX_VALUE));
			if (isInList(next)) {
				nextFreeIndex++;
				bitset = new BitSet();
				bitset.set(0);
				bitsetRanges.add(nextFreeIndex, bitset);
			}
		}
		return null;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("FreeList:");
		sb.append(" start=" + start);
		sb.append(" end=" + end);
		if ((bitsetRanges != null) && !bitsetRanges.isEmpty()) {
			sb.append(" ranges:\n");
			int i = 0;
			for (BitSet bs : bitsetRanges) {
				sb.append(" bitset[" + i + "].cardinality=" + bs.cardinality());
				sb.append('\n');
				i++;
			}
		}
		return sb.toString();
	}
}
