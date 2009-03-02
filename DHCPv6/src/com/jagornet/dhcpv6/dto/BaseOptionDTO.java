/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseOptionDTO.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.dto;

import java.io.Serializable;

/**
 * Title: BaseOptionDTO 
 * Description: Abstract base class for Data Transfer Objects (DTO)
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseOptionDTO implements Serializable
{ 
    /** The DHCP option code. */
    protected Integer code;
    
    /** The name of the DHCP option. */
    protected String name;
    
    /**
     * Gets the DHCP option code.
     * 
     * @return the code
     */
    public Integer getCode()
    {
        return code;
    }
    
    /**
     * Sets the DHCP option code.
     * 
     * @param code the new code
     */
    public void setCode(Integer code)
    {
        this.code = code;
    }
    
    /**
     * Gets the name of the DHCP option.
     * 
     * @return the name
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Sets the name of the DHCP option.
     * 
     * @param name the new name
     */
    public void setName(String name)
    {
        this.name = name;
    }
}
