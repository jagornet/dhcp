/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file GeoconfCivicOptionDTO.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.dto.option;

import java.util.ArrayList;
import java.util.List;

import com.jagornet.dhcpv6.dto.option.base.BaseOptionDTO;

/**
 * The Class GeoconfCivicOptionDTO.
 * 
 * @author A. Gregory Rabil
 */
public class GeoconfCivicOptionDTO extends BaseOptionDTO
{
	
	/** Default serial version id. */
	private static final long serialVersionUID = 1L;

	/** The what. */
	protected Short what;
	
	/** The country code. */
	protected String countryCode;
	
	/** The civic address elements. */
    protected List<CivicAddress> civicAddresses;

    /**
     * Gets the civic addresses.
     * 
     * @return the civic addresses
     */
    public List<CivicAddress> getCivicAddresses()
    {
        return civicAddresses;
    }

    /**
     * Sets the civic addresses.
     * 
     * @param civicAddresses the new civic addresses
     */
    public void setCivicAddresses(List<CivicAddress> civicAddresses)
    {
        this.civicAddresses = civicAddresses;
    }

    /**
     * Adds the civic addresses.
     * 
     * @param civicAddress the new civic address
     */
    public void addCivicAddress(CivicAddress civicAddress)
    {
        if (this.civicAddresses == null)
            this.civicAddresses = new ArrayList<CivicAddress>();
        civicAddresses.add(civicAddress);
    }
    
    /**
     * The Class CivicAddress.
     */
    public class CivicAddress
    {
	    /** The ca type. */
	    private Short caType;
    	
	    /** The ca value. */
	    private String caValue;
		
		/**
		 * Gets the ca type.
		 * 
		 * @return the ca type
		 */
		public Short getCaType() {
			return caType;
		}
		
		/**
		 * Sets the ca type.
		 * 
		 * @param caType the new ca type
		 */
		public void setCaType(Short caType) {
			this.caType = caType;
		}
		
		/**
		 * Gets the ca value.
		 * 
		 * @return the ca value
		 */
		public String getCaValue() {
			return caValue;
		}
		
		/**
		 * Sets the ca value.
		 * 
		 * @param caValue the new ca value
		 */
		public void setCaValue(String caValue) {
			this.caValue = caValue;
		}
    }
}
