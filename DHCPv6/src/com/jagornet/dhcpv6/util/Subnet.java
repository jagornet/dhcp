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
package com.jagornet.dhcpv6.util;

import java.math.BigInteger;
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
public class Subnet
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
        BigInteger start = new BigInteger(subnetAddress.getAddress());
        // turn on each bit that isn't masked by the prefix
        // note that bit zero(0) is the lowest order bit, so
        // this loop logically moves from right to left
        for (int i=0; i<(128-prefixLength); i++) {
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
    
    /**
     * Override the standard Object.equals() method to
     * satisfy the comparator used when loading the configuration
     * 
     * @param that the that
     * 
     * @return true, if equals
     */
    @Override
    public boolean equals(Object that)
    {
        if (that != null) {
            if ( this.subnetAddress.equals(((Subnet)that).subnetAddress) &&
                 (this.prefixLength == ((Subnet)that).prefixLength) )
                return true;
        }
        return false;
    }
}