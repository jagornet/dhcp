/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DomainNameOptionDTO.java is part of DHCPv6.
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

/**
 * The absract Class DomainNameOptionDTO
 * 
 * @author A. Gregory Rabil
 */

public abstract class DomainNameOptionDTO extends BaseOptionDTO
{
	/** The domain name. */
    protected String domainName;

    /**
     * Gets the domain name.
     * 
     * @return the domain name
     */
    public String getDomainName()
    {
        return domainName;
    }
    
    /**
     * Sets the domain name.
     * 
     * @param domainName the new domain name
     */
    public void setDomainName(String domainName)
    {
        this.domainName = domainName;
    }
}
