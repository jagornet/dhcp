/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseDomainNameListOption.java is part of Jagornet DHCP.
 *
 *   Jagornet DHCP is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Jagornet DHCP is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Jagornet DHCP.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcp.option.base;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.option.DhcpComparableOption;
import com.jagornet.dhcp.util.Util;
import com.jagornet.dhcp.xml.DomainNameListOptionType;
import com.jagornet.dhcp.xml.Operator;
import com.jagornet.dhcp.xml.OptionExpression;

/**
 * Title: BaseDomainNameListOption
 * Description: The abstract base class for domain name list DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseDomainNameListOption extends BaseDhcpOption implements DhcpComparableOption
{
	private static Logger log = LoggerFactory.getLogger(BaseDomainNameListOption.class);

	protected List<String> domainNameList;
	
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
		if (domainNameListOption != null) {
			if (domainNameListOption.getDomainNameList() != null) {
				domainNameList = domainNameListOption.getDomainNameList();
			}
		}
	}
	
	public List<String> getDomainNameList() {
		return domainNameList;
	}

	public void setDomainNameList(List<String> domainNames) {
		this.domainNameList = domainNames;
	}

	public void addDomainName(String domainName) {
		if (domainName != null) {
			if (domainNameList == null) {
				domainNameList = new ArrayList<String>();
			}
			domainNameList.add(domainName);
		}
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.option.base.Encodable#encode()
	 */
	public ByteBuffer encode() throws IOException
    {
    	ByteBuffer buf = super.encodeCodeAndLength();
        if (domainNameList != null) {
            for (String domain : domainNameList) {
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
                addDomainName(domain);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        int len = 0;
        if (domainNameList != null) {
            for (String domain : domainNameList) {
                len += BaseDomainNameOption.getDomainNameLength(domain);
            }
        }
        return len;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpComparableOption#matches(com.jagornet.dhcp.xml.OptionExpression)
     */
    public boolean matches(OptionExpression expression)
    {
        if (expression == null)
            return false;
        if (expression.getCode() != this.getCode())
            return false;
        if (domainNameList == null)
        	return false;
        
        // first see if we have a domain name list option to compare to
        DomainNameListOptionType exprOption = expression.getDomainNameListOption();
        if (exprOption != null) {
        	List<String> exprDomainNames = exprOption.getDomainNameList();
            Operator.Enum op = expression.getOperator();
            if (op.equals(Operator.EQUALS)) {
            	return domainNameList.equals(exprDomainNames);
            }
            else if (op.equals(Operator.CONTAINS)) {
            	return domainNameList.containsAll(exprDomainNames);
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
        sb.append(": domainNameList=");
        if (domainNameList != null) {
        	for (String domain : domainNameList) {
				sb.append(domain);
				sb.append(',');
			}
        	sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }
    
}
