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

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.option.DhcpComparableOption;
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
	
	public BaseDomainNameListOption()
	{
		this(null);
	}
	
	public BaseDomainNameListOption(DomainNameListOptionType domainNameListOption)
	{
		super();
		if (domainNameListOption != null)
			this.domainNameListOption = domainNameListOption;
		else
			this.domainNameListOption = DomainNameListOptionType.Factory.newInstance();
	}
	
    public DomainNameListOptionType getDomainNameListOption()
    {
		return domainNameListOption;
	}

	public void setDomainNameListOption(DomainNameListOptionType domainNameListOption)
	{
		if (domainNameListOption != null)
			this.domainNameListOption = domainNameListOption;
	}
	
	public void addDomainName(String domainName)
	{
		if (domainName != null) {
			domainNameListOption.addDomainName(domainName);
		}
	}

	public ByteBuffer encode() throws IOException
    {
    	IoBuffer iobuf = super.encodeCodeAndLength();
        List<String> domainNames = domainNameListOption.getDomainNameList();
        if (domainNames != null) {
            for (String domain : domainNames) {
                BaseDomainNameOption.encodeDomainName(iobuf, domain);
            }
        }
        return iobuf.flip().buf();
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Decodable#decode(java.nio.ByteBuffer)
     */
    public void decode(ByteBuffer buf) throws IOException
    {
    	IoBuffer iobuf = IoBuffer.wrap(buf);
    	int len = super.decodeLength(iobuf); 
    	if ((len > 0) && (len <= iobuf.remaining())) {
            int eof = iobuf.position() + len;
            while (iobuf.position() < eof) {
                String domain = BaseDomainNameOption.decodeDomainName(iobuf, eof);
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
        if (this == null)
            return null;
        
        StringBuilder sb = new StringBuilder(this.getName());
        sb.append(": ");
        List<String> domainNames = domainNameListOption.getDomainNameList();
        if (domainNames != null) {
            for (String domain : domainNames) {
                // there is trailing null label domain as per RFC 1035 sec 3.1
                if (domain.length() > 0) {
                    sb.append(domain);
                    sb.append(",");
                }
            }
            sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }
    
}
