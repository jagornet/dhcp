/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file OpaqueDataOptionDTO.java is part of DHCPv6.
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
 *   This file OpaqueDataOptionDTO.java is part of DHCPv6.
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
 * The Class OpaqueDataOptionDTO.
 * 
 * @author A. Gregory Rabil
 */
public class OpaqueDataOptionDTO extends BaseOptionDTO
{
	/**
	 * Default serial version id.
	 */
	private static final long serialVersionUID = 1L;
	
	/** The data. */
	protected OpaqueDataDTO data;

	/**
	 * Gets the data.
	 * 
	 * @return the data
	 */
	public OpaqueDataDTO getData() {
		return data;
	}

	/**
	 * Sets the data.
	 * 
	 * @param opaqueData the new data
	 */
	public void setData(OpaqueDataDTO opaqueData) {
		this.data = opaqueData;
	}
	
	// provide the necessary getter/setter
	// accessor methods to support DTO mapping
    /**
	 * Gets the ascii value.
	 * 
	 * @return the ascii value
	 */
	public String getAsciiValue()
    {
    	if (data != null)
    		return data.getAsciiValue();
    	
    	return null;
    }
    
    /**
     * Sets the ascii value.
     * 
     * @param asciiValue the new ascii value
     */
    public void setAsciiValue(String asciiValue)
    {
    	if (data == null)
    		data = new OpaqueDataDTO();
    	
    	data.setAsciiValue(asciiValue);
    	data.setHexValue(null);	// it can't be both
    }
    
    /**
     * Gets the hex value.
     * 
     * @return the hex value
     */
    public byte[] getHexValue()
    {
    	if (data != null)
    		return data.getHexValue();
    	
        return null;
    }
    
    /**
     * Sets the hex value.
     * 
     * @param hexValue the new hex value
     */
    public void setHexValue(byte[] hexValue)
    {
    	if (data == null)
    		data = new OpaqueDataDTO();
    	
        data.setHexValue(hexValue);
        data.setAsciiValue(null);	// it can't be both
    }
}
