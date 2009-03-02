/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpDomainSearchListOption.java is part of DHCPv6.
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

import java.util.List;

import com.jagornet.dhcpv6.xml.DomainSearchListOption;

/**
 * <p>Title: DhcpDomainSearchListOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpDomainSearchListOption extends BaseDomainNameListOption
{ 
    /** The domain search list option. */
    private DomainSearchListOption domainSearchListOption;

    /**
     * Instantiates a new dhcp domain search list option.
     */
    public DhcpDomainSearchListOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new dhcp domain search list option.
     * 
     * @param domainSearchListOption the domain search list option
     */
    public DhcpDomainSearchListOption(DomainSearchListOption domainSearchListOption)
    {
        super();
        if (domainSearchListOption != null)
            this.domainSearchListOption = domainSearchListOption;
        else
            this.domainSearchListOption = DomainSearchListOption.Factory.newInstance();
    }

    /**
     * Gets the domain search list option.
     * 
     * @return the domain search list option
     */
    public DomainSearchListOption getDomainSearchListOption()
    {
        return domainSearchListOption;
    }

    /**
     * Sets the domain search list option.
     * 
     * @param domainSearchListOption the new domain search list option
     */
    public void setDomainSearchListOption(DomainSearchListOption domainSearchListOption)
    {
        if (domainSearchListOption != null)
            this.domainSearchListOption = domainSearchListOption;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return domainSearchListOption.getCode();
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.BaseDomainNameListOption#getDomainNames()
     */
    @Override
    public List<String> getDomainNames()
    {
    	return domainSearchListOption.getDomainNamesList();
    }
    
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.option.BaseDomainNameListOption#addDomainName(java.lang.String)
	 */
	@Override
	public void addDomainName(String domainName)
	{
		domainSearchListOption.addDomainNames(domainName);
	}
}
