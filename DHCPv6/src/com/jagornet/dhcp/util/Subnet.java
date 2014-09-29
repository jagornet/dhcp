/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file Subnet.java is part of DHCPv6.
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
package com.jagornet.dhcp.util;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Title: Subnet
 * Description: A utility class for managing IPv6 subnets.
 * 
 * TODO: Refactor to use java.net.InterfaceAddress?
 * 
 * @author A. Gregory Rabil
 */
public class Subnet implements Comparable<Subnet>
{
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(Subnet.class);

    /** The subnet address. */
    private InetAddress subnetAddress;
    
    /** The prefix length. */
    private int prefixLength;

    /**
     * Instantiates a new subnet.
     * 
     * @param subnetAddress the subnet address
     * @param prefixLength the prefix length
     * 
     * @throws UnknownHostException the unknown host exception
     * @throws NumberFormatException the number format exception
     */
    public Subnet(String subnetAddress, String prefixLength)
		throws UnknownHostException, NumberFormatException
    {
		this(InetAddress.getByName(subnetAddress), Integer.parseInt(prefixLength));
    }

    /**
     * Instantiates a new subnet.
     * 
     * @param subnetAddress the subnet address
     * @param prefixLength the prefix length
     * 
     * @throws UnknownHostException the unknown host exception
     * @throws NumberFormatException the number format exception
     */
    public Subnet(String subnetAddress, int prefixLength)
        throws UnknownHostException, NumberFormatException
    {
        this(InetAddress.getByName(subnetAddress), prefixLength);
    }
    
    /**
     * Instantiates a new subnet.
     * 
     * @param subnetAddress the subnet address
     * @param prefixLength the prefix length
     */
    public Subnet(InetAddress subnetAddress, int prefixLength)
    {
		this.subnetAddress = subnetAddress;
		this.prefixLength = prefixLength;
    }

    /**
     * Gets the subnet address.
     * 
     * @return the subnet address
     */
    public InetAddress getSubnetAddress()
    {
        return subnetAddress;
    }

    /**
     * Sets the subnet address.
     * 
     * @param subnetAddress the new subnet address
     */
    public void setSubnetAddress(InetAddress subnetAddress)
    {
        this.subnetAddress = subnetAddress;
    }

    /**
     * Gets the prefix length.
     * 
     * @return the prefix length
     */
    public int getPrefixLength()
    {
        return prefixLength;
    }

    /**
     * Sets the prefix length.
     * 
     * @param prefixLength the new prefix length
     */
    public void setPrefixLength(int prefixLength)
    {
        this.prefixLength = prefixLength;
    }
    
    /**
     * Gets the end address.
     * 
     * @return the end address
     */
    public InetAddress getEndAddress()
    {
        InetAddress endAddr = null;
        int maxPrefix = 0;
        if (subnetAddress instanceof Inet4Address) {
        	maxPrefix = 32;
        }
        else {
        	maxPrefix = 128;
        }
        BigInteger start = new BigInteger(subnetAddress.getAddress());
        // turn on each bit that isn't masked by the prefix
        // note that bit zero(0) is the lowest order bit, so
        // this loop logically moves from right to left
        for (int i=0; i<(maxPrefix-prefixLength); i++) {
            start = start.setBit(i);
        }
        try {
            endAddr = InetAddress.getByAddress(start.toByteArray());
        }
        catch (UnknownHostException ex) {
            log.error("Failed to calculate subnet end address: " + ex);
        }
        return endAddr;
    }
    
    /**
     * Contains.  Test if an IP address falls within a subnet.
     * 
     * @param inetAddr the IP address to check
     * 
     * @return true, if subnet contains the IP address
     */
    public boolean contains(InetAddress inetAddr)
    {
        boolean rc = false;
        BigInteger start = new BigInteger(getSubnetAddress().getAddress());
        BigInteger end = new BigInteger(getEndAddress().getAddress());
        BigInteger addr = new BigInteger(inetAddr.getAddress());
        if ((addr.compareTo(start) >= 0) && (addr.compareTo(end) <= 0)) {
            rc = true;
        }
        return rc;
    }

	public int compareTo(Subnet that)
	{
        BigInteger thisAddr = new BigInteger(this.getSubnetAddress().getAddress());
        BigInteger thatAddr = new BigInteger(that.getSubnetAddress().getAddress());
        if (thisAddr.equals(thatAddr)) {
        	Integer thisPrefix = this.getPrefixLength();
        	Integer thatPrefix = that.getPrefixLength();
            // if we have two subnets with the same starting address
            // then the _smaller_ subnet is the one with the _larger_
            // prefix length, which logically places the more specific
            // subnet _before_ the less specific subnet in the map
            // this allows us to work from "inside-out"?
        	// must negate the comparison to make bigger smaller
        	return -1 * thisPrefix.compareTo(thatPrefix);
        }
        else {
            // subnet addresses are different, so return
            // the standard compare for the address
            return thisAddr.compareTo(thatAddr);
        }
	}    
    
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (subnetAddress != null) {
			sb.append(subnetAddress.getHostAddress());
			sb.append('/');
			sb.append(prefixLength);
			sb.append(" (");
			sb.append(subnetAddress.getHostAddress());
			sb.append('-');
			sb.append(getEndAddress().getHostAddress());
			sb.append(')');
		}
		return sb.toString();
	}
}