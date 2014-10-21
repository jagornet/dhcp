/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseDomainNameOption.java is part of Jagornet DHCP.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.util.Util;
import com.jagornet.dhcp.xml.DomainNameOptionType;
import com.jagornet.dhcp.xml.Operator;
import com.jagornet.dhcp.xml.OptionExpression;

/**
 * Title: BaseDomainNameOption
 * Description: The abstract base class for domain name DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseDomainNameOption extends BaseDhcpOption
{
	private static Logger log = LoggerFactory.getLogger(BaseDomainNameOption.class);

	protected String domainName;
	
	public BaseDomainNameOption()
	{
		this(null);
	}
	
	public BaseDomainNameOption(DomainNameOptionType domainNameOption)
	{
		super();
		if (domainNameOption != null) {
			domainName = domainNameOption.getDomainName();
		}
	}
	
    public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	/* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        if (domainName != null) {
            encodeDomainName(buf, domainName);
        }
        return (ByteBuffer) buf.flip();
    }
    
    /**
     * Encode domain name.
     * 
     * @param buf the buf
     * @param domain the domain
     */
    public static void encodeDomainName(ByteBuffer buf, String domain)
    {
    	if (domain != null) {
        	boolean fqdn = domain.endsWith(".");
	        // we split the human-readable string representing the
	        // fully-qualified domain name along the dots, which
	        // gives us the list of labels that make up the FQDN
	        String[] labels = domain.split("\\.");
	        if (labels != null) {
	            for (String label : labels) {
	                // domain names are encoded according to RFC1035 sec 3.1
	                // a 'label' consists of a length byte (i.e. octet) with
	                // the two high order bits set to zero (which means each
	                // label is limited to 63 bytes) followed by length number
	                // of bytes (i.e. octets) which make up the name
	            	buf.put((byte)label.length());
	            	if (label.length() > 0) {
	            		buf.put(label.getBytes());
	            	}
	            }
	            if (fqdn) {
	            	buf.put((byte)0);    // terminate with zero-length "root" label
	            }
	        }
    	}
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Decodable#decode(java.nio.ByteBuffer)
     */
    public void decode(ByteBuffer buf) throws IOException
    {
    	int len = super.decodeLength(buf);
    	if ((len > 0) && (len <= buf.remaining())) {
            int eof = buf.position() + len;
            domainName = decodeDomainName(buf, eof);
        }
    }
    
    /**
     * Decode domain name.
     * 
     * @param buf the buf
     * @param eof the eof
     * 
     * @return the string
     */
    public static String decodeDomainName(ByteBuffer buf, int eof)
    {
        StringBuilder domain = new StringBuilder();
        while (buf.position() < eof) {
            short l = Util.getUnsignedByte(buf);  // length byte as short
            if (l == 0)
                break;      // terminating null "root" label
            byte[] b = new byte[l];
            buf.get(b);      // get next label
            domain.append(new String(b));
            if (buf.position() < eof)
            	domain.append('.');     // build the FQDN by appending labels
        }
        return domain.toString();
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        int len = 0;
        if (domainName != null) {
            len = getDomainNameLength(domainName);
        }
        return len;
    }

    /**
     * Gets the domain name length.
     * 
     * @param domainName the domain name
     * 
     * @return the domain name length
     */
    public static int getDomainNameLength(String domainName)
    {
        int len = 0;
        if (domainName != null) {
        	boolean fqdn = domainName.endsWith(".");
	        String[] labels = domainName.split("\\.");
	        if (labels != null) {
	            for (String label : labels) {
	                // each label consists of a length byte and opaqueData
	                len += 1 + label.length();
	            }
	        }
            if (fqdn) {
            	len += 1;   // one extra byte for the zero length terminator
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
        if (domainName == null)
        	return false;

        DomainNameOptionType exprOption = expression.getDomainNameOption();
        if (exprOption != null) {
        	String exprDomainName = exprOption.getDomainName();
            Operator.Enum op = expression.getOperator();
            if (op.equals(Operator.EQUALS)) {
            	return domainName.equals(exprDomainName);
            }
            else if (op.equals(Operator.STARTS_WITH)) {
            	return domainName.startsWith(exprDomainName);
            }
            else if (op.equals(Operator.ENDS_WITH)) {
            	return domainName.endsWith(exprDomainName);
            }
            else if (op.equals(Operator.CONTAINS)) {
            	return domainName.contains(exprDomainName);
            }
            else if (op.equals(Operator.REG_EXP)) {
            	return domainName.matches(exprDomainName);
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
        sb.append(": domainName=");
        sb.append(domainName);
        return sb.toString();
    }

}
