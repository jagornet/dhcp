/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file IntOptionDTO.java is part of DHCPv6.
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

/**
 * The abstract Class IntOptionDTO.
 * 
 * @author A. Gregory Rabil
 */
public abstract class IntOptionDTO extends BaseOptionDTO
{
    /** The value. */
    protected Integer value;

    /**
     * Gets the value.
     * 
     * @return the value
     */
    public Integer getValue()
    {
        return getIntValue();
    }
    // getIntValue for mapping via Dozer
    /**
     * Gets the int value.
     * 
     * @return the int value
     */
    public Integer getIntValue()
    {
    	return value;
    }
    
    /**
     * Sets the value.
     * 
     * @param value the new value
     */
    public void setValue(Integer value)
    {
        setIntValue(value);
    }
    // setIntValue for mapping via Dozer
    /**
     * Sets the int value.
     * 
     * @param value the new int value
     */
    public void setIntValue(Integer value)
    {
    	this.value = value;
    }
}
