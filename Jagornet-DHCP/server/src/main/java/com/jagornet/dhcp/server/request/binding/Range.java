/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file Range.java is part of Jagornet DHCP.
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

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.jagornet.dhcp.server.config.DhcpServerConfigException;
import com.jagornet.dhcp.util.Subnet;
import com.jagornet.dhcp.util.Util;

/**
 * The Class Range.
 * 
 * @author A. Gregory Rabil
 */
public class Range
{
	protected InetAddress startAddress;
	protected InetAddress endAddress;

	/**
	 * Instantiates a new range.
	 * 
	 * @param range the range
	 * @throws UnknownHostException 
	 * @throws NumberFormatException 
	 * 
	 * @throws NumberFormatException, UnknownHostException, DhcpServerConfigException
	 */
	public Range(String range) 
			throws NumberFormatException, UnknownHostException, DhcpServerConfigException
	{
		// assume the range is in preferred CIDR notation
		String[] cidr = range.split("/");
		if ((cidr != null) && (cidr.length == 2)) {
			Subnet subnet = new Subnet(cidr[0], cidr[1]);
			startAddress = subnet.getSubnetAddress();
			endAddress = subnet.getEndAddress();
		}
		else {
			// otherwise assume the range is in start-end format
			cidr = range.split("-");
			if ((cidr != null) && (cidr.length == 2)) {
				startAddress = InetAddress.getByName(cidr[0]);
				endAddress = InetAddress.getByName(cidr[1]);
			}
			else {
				throw new DhcpServerConfigException("Failed to parse range: " + range);
			}
		}
	}
	
	/**
	 * Instantiates a new range.
	 * 
	 * @param startAddress the start address
	 * @param endAddress the end address
	 */
	public Range(InetAddress startAddress, InetAddress endAddress)
	{
		this.startAddress = startAddress;
		this.endAddress = endAddress;
	}

	/**
	 * Gets the start address.
	 * 
	 * @return the start address
	 */
	public InetAddress getStartAddress() {
		return startAddress;
	}

	/**
	 * Sets the start address.
	 * 
	 * @param startAddress the new start address
	 */
	public void setStartAddress(InetAddress startAddress) {
		this.startAddress = startAddress;
	}

	/**
	 * Gets the end address.
	 * 
	 * @return the end address
	 */
	public InetAddress getEndAddress() {
		return endAddress;
	}

	/**
	 * Sets the end address.
	 * 
	 * @param endAddress the new end address
	 */
	public void setEndAddress(InetAddress endAddress) {
		this.endAddress = endAddress;
	}
	
	/**
	 * Contains.
	 * 
	 * @param inetAddr the inet addr
	 * 
	 * @return true, if successful
	 */
	public boolean contains(InetAddress inetAddr)
	{
		if ((Util.compareInetAddrs(startAddress, inetAddr) <= 0) &&
				(Util.compareInetAddrs(endAddress, inetAddr) >= 0)) {
			return true;
		}
		return false;
	}
	
	public BigInteger size() {
		return new BigInteger(endAddress.getAddress()).
				subtract(new BigInteger(startAddress.getAddress())).
				add(BigInteger.ONE);
	}
}
