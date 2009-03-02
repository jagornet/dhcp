/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpServerIdOption.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.ServerIdOption;

/**
 * <p>Title: DhcpServerIdOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpServerIdOption extends BaseOpaqueDataOption
{
    /** The server id option. */
    private ServerIdOption serverIdOption;
    
    /**
     * Instantiates a new dhcp server id option.
     */
    public DhcpServerIdOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new dhcp server id option.
     * 
     * @param serverIdOption the server id option
     */
    public DhcpServerIdOption(ServerIdOption serverIdOption)
    {
        super();
        if (serverIdOption != null)
            this.serverIdOption = serverIdOption;
        else
            this.serverIdOption = ServerIdOption.Factory.newInstance();
    }

    /**
     * Gets the server id option.
     * 
     * @return the server id option
     */
    public ServerIdOption getServerIdOption()
    {
        return serverIdOption;
    }

    /**
     * Sets the server id option.
     * 
     * @param serverIdOption the new server id option
     */
    public void setServerIdOption(ServerIdOption serverIdOption)
    {
        if (serverIdOption != null)
            this.serverIdOption = serverIdOption;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return serverIdOption.getCode();
    }
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.BaseOpaqueDataOption#getOpaqueData()
     */
    public OpaqueData getOpaqueData()
    {
        return (OpaqueData)this.serverIdOption;
    }
}
