/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpSntpServersOption.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.xml.SntpServersOption;

/**
 * <p>Title: DhcpSntpServersOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpSntpServersOption extends BaseServerAddressesOption
{
    /** The sntp servers option. */
    private SntpServersOption sntpServersOption;

    /**
     * Instantiates a new dhcp sntp servers option.
     */
    public DhcpSntpServersOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new dhcp sntp servers option.
     * 
     * @param sntpServersOption the sntp servers option
     */
    public DhcpSntpServersOption(SntpServersOption sntpServersOption)
    {
        super();
        if (sntpServersOption != null)
            this.sntpServersOption = sntpServersOption;
        else
            this.sntpServersOption = SntpServersOption.Factory.newInstance();
    }

    /**
     * Gets the sntp servers option.
     * 
     * @return the sntp servers option
     */
    public SntpServersOption getSntpServersOption()
    {
        return sntpServersOption;
    }

    /**
     * Sets the sntp servers option.
     * 
     * @param sntpServersOption the new sntp servers option
     */
    public void setSntpServersOption(SntpServersOption sntpServersOption)
    {
        if (sntpServersOption != null)
            this.sntpServersOption = sntpServersOption;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return sntpServersOption.getCode();
    }
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.BaseServerAddressesOption#getServerIpAddresses()
     */
    @Override
    public List<String> getServerIpAddresses()
    {
        return sntpServersOption.getServerIpAddressesList();
    }
    
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.option.BaseServerAddressesOption#addServerIpAddress(java.lang.String)
	 */
	@Override
	public void addServerIpAddress(String serverIpAddress)
	{
		sntpServersOption.addServerIpAddresses(serverIpAddress);
	}
}
