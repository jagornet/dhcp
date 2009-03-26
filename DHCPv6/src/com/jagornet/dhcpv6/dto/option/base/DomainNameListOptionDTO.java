/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DomainNameListOptionDTO.java is part of DHCPv6.
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
 * The Class DomainNameListOptionDTO.
 * 
 * @author A. Gregory Rabil
 */
public class DomainNameListOptionDTO extends BaseOptionDTO
{
	/**
	 * Default serial version id.
	 */
	private static final long serialVersionUID = 1L;
    
    /** The domain name list. */
    protected List<String> domainNameList;

    /**
     * Gets the domain name list.
     * 
     * @return the domain name list
     */
    public List<String> getDomainNameList()
    {
        return domainNameList;
    }
    
    /**
     * Provided to simplify dozer mapping.
     * @return
     */
    public String[] getDomainNameArray()
    {
    	if (domainNameList != null)
    		return (String[])domainNameList.toArray(new String[0]);
    	
    	return null;
    }

    /**
     * Sets the domain name list.
     * 
     * @param domainNameList the new domain name list
     */
    public void setDomainNameList(List<String> domainNameList)
    {
        this.domainNameList = domainNameList;
    }
}
