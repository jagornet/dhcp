/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.util;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Title: Subnet
 * Description: A utility class for managing subnets.
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
    private InetAddress endAddress;
    
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
        if (endAddress == null) {
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
	            endAddress = InetAddress.getByAddress(start.toByteArray());
	        }
	        catch (UnknownHostException ex) {
	            log.error("Failed to calculate subnet end address: " + ex);
	        }
        }
        return endAddress;
    }
    
    public boolean isV6() {
    	if (subnetAddress instanceof Inet6Address) {
    		return true;
    	}
    	return false;
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
        if (inetAddr == null) {
            return false;
        }
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