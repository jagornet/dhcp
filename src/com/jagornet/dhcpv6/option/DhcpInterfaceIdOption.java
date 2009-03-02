/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpInterfaceIdOption.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.xml.InterfaceIdOption;
import com.jagornet.dhcpv6.xml.OpaqueData;

/**
 * <p>Title: DhcpInterfaceIdOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpInterfaceIdOption extends BaseOpaqueDataOption
{ 
    /** The interface id option. */
    private InterfaceIdOption interfaceIdOption;
    
    /**
     * Instantiates a new dhcp interface id option.
     */
    public DhcpInterfaceIdOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new dhcp interface id option.
     * 
     * @param interfaceIdOption the interface id option
     */
    public DhcpInterfaceIdOption(InterfaceIdOption interfaceIdOption)
    {
        super();
        if (interfaceIdOption != null)
            this.interfaceIdOption = interfaceIdOption;
        else
            this.interfaceIdOption = InterfaceIdOption.Factory.newInstance();
    }

    /**
     * Gets the interface id option.
     * 
     * @return the interface id option
     */
    public InterfaceIdOption getInterfaceIdOption()
    {
        return interfaceIdOption;
    }

    /**
     * Sets the interface id option.
     * 
     * @param interfaceIdOption the new interface id option
     */
    public void setInterfaceIdOption(InterfaceIdOption interfaceIdOption)
    {
        if (interfaceIdOption != null)
            this.interfaceIdOption = interfaceIdOption;
    }
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return interfaceIdOption.getCode();
    }
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.BaseOpaqueDataOption#getOpaqueData()
     */
    public OpaqueData getOpaqueData()
    {
        return (OpaqueData)this.interfaceIdOption;
    }
}
