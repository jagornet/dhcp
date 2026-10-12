/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.request.binding;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.jagornet.dhcp.core.util.Subnet;
import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.config.DhcpServerConfigException;

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
		return (Util.compareInetAddrs(startAddress, inetAddr) <= 0) &&
				(Util.compareInetAddrs(endAddress, inetAddr) >= 0);
	}
	
	public BigInteger size() {
		return new BigInteger(endAddress.getAddress()).
				subtract(new BigInteger(startAddress.getAddress())).
				add(BigInteger.ONE);
	}
	
	public String toString() {
		return startAddress.getHostAddress() + "-" + endAddress.getHostAddress();
	}
}
