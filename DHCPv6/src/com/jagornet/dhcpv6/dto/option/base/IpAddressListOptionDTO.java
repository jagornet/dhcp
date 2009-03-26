/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file IpAddressListOptionDTO.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.dto.option.base;

import java.util.List;

/**
 * The abstract Class IpAddressListOptionDTO.
 * 
 * @author A. Gregory Rabil
 */
public abstract class IpAddressListOptionDTO extends BaseOptionDTO
{   
    /** The ip address list. */
    protected List<String> ipAddressList;

    /**
     * Gets the ip address list.
     * 
     * @return the ip address list
     */
    public List<String> getIpAddressList()
    {
        return ipAddressList;
    }
    
    /**
     * Provided to simplify dozer mapping.
     * @return
     */
    public String[] getIpAddressArray()
    {
    	if (ipAddressList != null)
    		return ipAddressList.toArray(new String[0]);
    	
    	return null;
    }

    /**
     * Sets the ip address list.
     * 
     * @param ipAddressList the new ip address list
     */
    public void setIpAddressList(List<String> ipAddressList)
    {
        this.ipAddressList = ipAddressList;
    }
}
