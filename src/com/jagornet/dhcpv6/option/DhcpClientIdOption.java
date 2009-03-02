/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpClientIdOption.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.xml.ClientIdOption;
import com.jagornet.dhcpv6.xml.OpaqueData;

/**
 * <p>Title: DhcpClientIdOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpClientIdOption extends BaseOpaqueDataOption
{ 
    /** The client id option. */
    private ClientIdOption clientIdOption;
    
    /**
     * Instantiates a new dhcp client id option.
     */
    public DhcpClientIdOption()
    {
    	this(null);
    }
    
    /**
     * Instantiates a new dhcp client id option.
     * 
     * @param clientIdOption the client id option
     */
    public DhcpClientIdOption(ClientIdOption clientIdOption)
    {
        super();
        if (clientIdOption != null)
            this.clientIdOption = clientIdOption;
        else
            this.clientIdOption = ClientIdOption.Factory.newInstance();
    }

    /**
     * Gets the client id option.
     * 
     * @return the client id option
     */
    public ClientIdOption getClientIdOption()
    {
        return clientIdOption;
    }

    /**
     * Sets the client id option.
     * 
     * @param clientIdOption the new client id option
     */
    public void setClientIdOption(ClientIdOption clientIdOption)
    {
        if (clientIdOption != null)
            this.clientIdOption = clientIdOption;
    }
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return clientIdOption.getCode();
    }
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.BaseOpaqueDataOption#getOpaqueData()
     */
    public OpaqueData getOpaqueData()
    {
        return (OpaqueData)this.clientIdOption;
    }
}
