/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpNisPlusServersOption.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.xml.NisPlusServersOption;

/**
 * <p>Title: DhcpNisPlusServersOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpNisPlusServersOption extends BaseServerAddressesOption
{
    /** The nis plus servers option. */
    private NisPlusServersOption nisPlusServersOption;

    /**
     * Instantiates a new dhcp nis plus servers option.
     */
    public DhcpNisPlusServersOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new dhcp nis plus servers option.
     * 
     * @param nisPlusServersOption the nis plus servers option
     */
    public DhcpNisPlusServersOption(NisPlusServersOption nisPlusServersOption)
    {
        super();
        if (nisPlusServersOption != null)
            this.nisPlusServersOption = nisPlusServersOption;
        else
            this.nisPlusServersOption = NisPlusServersOption.Factory.newInstance();
    }

    /**
     * Gets the nis plus servers option.
     * 
     * @return the nis plus servers option
     */
    public NisPlusServersOption getNisPlusServersOption()
    {
        return nisPlusServersOption;
    }

    /**
     * Sets the nis plus servers option.
     * 
     * @param nisPlusServersOption the new nis plus servers option
     */
    public void setNisPlusServersOption(NisPlusServersOption nisPlusServersOption)
    {
        if (nisPlusServersOption != null)
            this.nisPlusServersOption = nisPlusServersOption;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return nisPlusServersOption.getCode();
    }
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.BaseServerAddressesOption#getServerIpAddresses()
     */
    @Override
    public List<String> getServerIpAddresses()
    {
        return nisPlusServersOption.getServerIpAddressesList();
    }
    
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.option.BaseServerAddressesOption#addServerIpAddress(java.lang.String)
	 */
	@Override
	public void addServerIpAddress(String serverIpAddress)
	{
		nisPlusServersOption.addServerIpAddresses(serverIpAddress);
	}
}
