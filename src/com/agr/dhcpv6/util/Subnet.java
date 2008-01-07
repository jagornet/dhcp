package com.agr.dhcpv6.util;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:
 * @author
 * @version 1.0
 */

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Subnet
{
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