/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpSipServerDomainNamesOption.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.xml.SipServerDomainNamesOption;

/**
 * <p>Title: DhcpSipServerDomainNamesOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpSipServerDomainNamesOption extends BaseDomainNameListOption
{
    /** The sip server domain names option. */
    private SipServerDomainNamesOption sipServerDomainNamesOption;

    /**
     * Instantiates a new dhcp sip server domain names option.
     */
    public DhcpSipServerDomainNamesOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new dhcp sip server domain names option.
     * 
     * @param sipServerDomainNamesOption the sip server domain names option
     */
    public DhcpSipServerDomainNamesOption(SipServerDomainNamesOption sipServerDomainNamesOption)
    {
        super();
        if (sipServerDomainNamesOption != null)
            this.sipServerDomainNamesOption = sipServerDomainNamesOption;
        else
            this.sipServerDomainNamesOption = SipServerDomainNamesOption.Factory.newInstance();
    }

    /**
     * Gets the sip server domain names option.
     * 
     * @return the sip server domain names option
     */
    public SipServerDomainNamesOption getSipServerDomainNamesOption()
    {
        return sipServerDomainNamesOption;
    }

    /**
     * Sets the sip server domain names option.
     * 
     * @param sipServerDomainNamesOption the new sip server domain names option
     */
    public void setSipServerDomainNamesOption(SipServerDomainNamesOption sipServerDomainNamesOption)
    {
        if (sipServerDomainNamesOption != null)
            this.sipServerDomainNamesOption = sipServerDomainNamesOption;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return sipServerDomainNamesOption.getCode();
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.BaseDomainNameListOption#getDomainNames()
     */
    @Override
    public List<String> getDomainNames()
    {
    	return sipServerDomainNamesOption.getDomainNamesList();
    }
    
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.option.BaseDomainNameListOption#addDomainName(java.lang.String)
	 */
	@Override
	public void addDomainName(String domainName)
	{
		sipServerDomainNamesOption.addDomainNames(domainName);
	}
}
