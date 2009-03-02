/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpSipServerAddressesOption.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.option;

import java.util.List;

import com.jagornet.dhcpv6.xml.SipServerAddressesOption;

/**
 * <p>Title: DhcpSipServerAddressesOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpSipServerAddressesOption extends BaseServerAddressesOption
{
    /** The sip server addresses option. */
    private SipServerAddressesOption sipServerAddressesOption;

    /**
     * Instantiates a new dhcp sip server addresses option.
     */
    public DhcpSipServerAddressesOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new dhcp sip server addresses option.
     * 
     * @param sipServerAddressesOption the sip server addresses option
     */
    public DhcpSipServerAddressesOption(SipServerAddressesOption sipServerAddressesOption)
    {
        super();
        if (sipServerAddressesOption != null)
            this.sipServerAddressesOption = sipServerAddressesOption;
        else
            this.sipServerAddressesOption = SipServerAddressesOption.Factory.newInstance();
    }

    /**
     * Gets the sip server addresses option.
     * 
     * @return the sip server addresses option
     */
    public SipServerAddressesOption getSipServerAddressesOption()
    {
        return sipServerAddressesOption;
    }

    /**
     * Sets the sip server addresses option.
     * 
     * @param sipServerAddressesOption the new sip server addresses option
     */
    public void setSipServerAddressesOption(SipServerAddressesOption sipServerAddressesOption)
    {
        if (sipServerAddressesOption != null)
            this.sipServerAddressesOption = sipServerAddressesOption;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return sipServerAddressesOption.getCode();
    }
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.BaseServerAddressesOption#getServerIpAddresses()
     */
    @Override
    public List<String> getServerIpAddresses()
    {
        return sipServerAddressesOption.getServerIpAddressesList();
    }
    
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.option.BaseServerAddressesOption#addServerIpAddress(java.lang.String)
	 */
	@Override
	public void addServerIpAddress(String serverIpAddress)
	{
		sipServerAddressesOption.addServerIpAddresses(serverIpAddress);
	}
}
