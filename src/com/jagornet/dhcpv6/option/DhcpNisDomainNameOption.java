/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpNisDomainNameOption.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.xml.NisDomainNameOption;

/**
 * <p>Title: DhcpNisDomainNameOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpNisDomainNameOption extends BaseDomainNameOption
{ 
    /** The nis domain name option. */
    private NisDomainNameOption nisDomainNameOption;

    /**
     * Instantiates a new dhcp nis domain name option.
     */
    public DhcpNisDomainNameOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new dhcp nis domain name option.
     * 
     * @param nisDomainNameOption the nis domain name option
     */
    public DhcpNisDomainNameOption(NisDomainNameOption nisDomainNameOption)
    {
        super();
        if (nisDomainNameOption != null)
            this.nisDomainNameOption = nisDomainNameOption;
        else
            this.nisDomainNameOption = NisDomainNameOption.Factory.newInstance();
    }

    /**
     * Gets the nis domain name option.
     * 
     * @return the nis domain name option
     */
    public NisDomainNameOption getNisDomainNameOption()
    {
        return nisDomainNameOption;
    }

    /**
     * Sets the nis domain name option.
     * 
     * @param nisDomainNameOption the new nis domain name option
     */
    public void setNisDomainNameOption(NisDomainNameOption nisDomainNameOption)
    {
        if (nisDomainNameOption != null)
            this.nisDomainNameOption = nisDomainNameOption;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return nisDomainNameOption.getCode();
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.BaseDomainNameOption#getDomainName()
     */
    public String getDomainName()
    {
        return nisDomainNameOption.getDomainName();
    }
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.BaseDomainNameOption#setDomainName(java.lang.String)
     */
    public void setDomainName(String domainName)
    {
        nisDomainNameOption.setDomainName(domainName);
    }
}
