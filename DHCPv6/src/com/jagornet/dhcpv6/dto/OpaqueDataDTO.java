/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file OpaqueDataDTO.java is part of DHCPv6.
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

/*
 *   This file OpaqueDataDTO.java is part of DHCPv6.
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
 * The Class OpaqueDataDTO.
 * 
 * @author A. Gregory Rabil
 */
public class OpaqueDataDTO implements Serializable
{    
	/**
	 * Default serial version id.
	 */
	private static final long serialVersionUID = 1L;

	/** The ascii value. */
    protected String asciiValue;
    
    /** The hex value. */
    protected byte[] hexValue;

    /**
     * Gets the ascii value.
     * 
     * @return the ascii value
     */
    public String getAsciiValue()
    {
        return asciiValue;
    }
    
    /**
     * Sets the ascii value.
     * 
     * @param asciiValue the new ascii value
     */
    public void setAsciiValue(String asciiValue)
    {
        this.asciiValue = asciiValue;
    }
    
    /**
     * Gets the hex value.
     * 
     * @return the hex value
     */
    public byte[] getHexValue()
    {
        return hexValue;
    }
    
    /**
     * Sets the hex value.
     * 
     * @param hexValue the new hex value
     */
    public void setHexValue(byte[] hexValue)
    {
        this.hexValue = hexValue;
    }
}
