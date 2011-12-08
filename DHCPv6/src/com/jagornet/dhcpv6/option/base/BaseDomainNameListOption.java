/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseDomainNameListOption.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.option.base;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.option.DhcpComparableOption;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.DomainNameListOptionType;
import com.jagornet.dhcpv6.xml.Operator;
import com.jagornet.dhcpv6.xml.OptionExpression;

/**
 * Title: BaseDomainNameListOption
 * Description: The abstract base class for domain name list DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseDomainNameListOption extends BaseDhcpOption implements DhcpComparableOption
{
	private static Logger log = LoggerFactory.getLogger(BaseDomainNameListOption.class);

	protected DomainNameListOptionType domainNameListOption;
	
	/**
	 * Instantiates a new base domain name list option.
	 */
	public BaseDomainNameListOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new base domain name list option.
	 *
	 * @param domainNameListOption the domain name list option
	 */
	public BaseDomainNameListOption(DomainNameListOptionType domainNameListOption)
	{
		super();
		if (domainNameListOption != null)
			this.domainNameListOption = domainNameListOption;
		else
			this.domainNameListOption = DomainNameListOptionType.Factory.newInstance();
	}
	
    /**
     * Gets the domain name list option.
     *
     * @return the domain name list option
     */
    public DomainNameListOptionType getDomainNameListOption()
    {
		return domainNameListOption;
	}

	/**
	 * Sets the domain name list option.
	 *
	 * @param domainNameListOption the new domain name list option
	 */
	public void setDomainNameListOption(DomainNameListOptionType domainNameListOption)
	{
		if (domainNameListOption != null)
			this.domainNameListOption = domainNameListOption;
	}
	
	/**
	 * Adds the domain name.
	 *
	 * @param domainName the domain name
	 */
	public void addDomainName(String domainName)
	{
		if (domainName != null) {
			domainNameListOption.addDomainName(domainName);
		}
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.option.base.Encodable#encode()
	 */
	public ByteBuffer encode() throws IOException
    {
    	ByteBuffer buf = super.encodeCodeAndLength();
        List<String> domainNames = domainNameListOption.getDomainNameList();
        if (domainNames != null) {
            for (String domain : domainNames) {
                BaseDomainNameOption.encodeDomainName(buf, domain);
            }
        }
        return (ByteBuffer) buf.flip();
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Decodable#decode(java.nio.ByteBuffer)
     */
    public void decode(ByteBuffer buf) throws IOException
    {
    	int len = super.decodeLength(buf); 
    	if ((len > 0) && (len <= buf.remaining())) {
            int eof = buf.position() + len;
            while (buf.position() < eof) {
                String domain = BaseDomainNameOption.decodeDomainName(buf, eof);
                domainNameListOption.addDomainName(domain);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        int len = 0;
        List<String> domainNames = domainNameListOption.getDomainNameList();
        if (domainNames != null) {
            for (String domain : domainNames) {
                len += BaseDomainNameOption.getDomainNameLength(domain);
            }
        }
        return len;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpComparableOption#matches(com.jagornet.dhcpv6.xml.OptionExpression)
     */
    public boolean matches(OptionExpression expression)
    {
        if (expression == null)
            return false;
        if (expression.getCode() != this.getCode())
            return false;
        if (domainNameListOption == null)
        	return false;

        List<String> myDomainNames = domainNameListOption.getDomainNameList();
        if (myDomainNames == null)
        	return false;
        
        // first see if we have a domain name list option to compare to
        DomainNameListOptionType that = expression.getDomainNameListOption();
        if (that != null) {
        	List<String> domainNames = that.getDomainNameList();
            Operator.Enum op = expression.getOperator();
            if (op.equals(Operator.EQUALS)) {
            	return myDomainNames.equals(domainNames);
            }
            else if (op.equals(Operator.CONTAINS)) {
            	return myDomainNames.containsAll(domainNames);
            }
            else {
            	log.warn("Unsupported expression operator: " + op);
            }
        }
        
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(Util.LINE_SEPARATOR);
        sb.append(super.getName());
        sb.append(Util.LINE_SEPARATOR);
        // use XmlObject implementation
        sb.append(domainNameListOption.toString());
        return sb.toString();
    }
    
}
