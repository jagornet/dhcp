package com.jagornet.dhcpv6.util;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:
 * @author
 * @version 1.0
 */

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Subnet
{
	private static Logger log = LoggerFactory.getLogger(Subnet.class);

    private InetAddress subnetAddress;
    private int prefixLength;

    public Subnet(String subnetAddress, String prefixLength)
		throws UnknownHostException, NumberFormatException
    {
		this(InetAddress.getByName(subnetAddress), Integer.parseInt(prefixLength));
    }

    public Subnet(String subnetAddress, int prefixLength)
        throws UnknownHostException, NumberFormatException
    {
        this(InetAddress.getByName(subnetAddress), prefixLength);
    }
    
    public Subnet(InetAddress subnetAddress, int prefixLength)
    {
		this.subnetAddress = subnetAddress;
		this.prefixLength = prefixLength;
    }

    public InetAddress getSubnetAddress()
    {
        return subnetAddress;
    }

    public void setSubnetAddress(InetAddress subnetAddress)
    {
        this.subnetAddress = subnetAddress;
    }

    public int getPrefixLength()
    {
        return prefixLength;
    }

    public void setPrefixLength(int prefixLength)
    {
        this.prefixLength = prefixLength;
    }
    
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