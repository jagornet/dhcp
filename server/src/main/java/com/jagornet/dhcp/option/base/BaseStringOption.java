/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseStringOption.java is part of Jagornet DHCP.
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

import com.jagornet.dhcp.option.DhcpComparableOption;
import com.jagornet.dhcp.util.Util;
import com.jagornet.dhcp.xml.OpaqueData;
import com.jagornet.dhcp.xml.OpaqueDataOptionType;
import com.jagornet.dhcp.xml.Operator;
import com.jagornet.dhcp.xml.OptionExpression;
import com.jagornet.dhcp.xml.StringOptionType;

/**
 * Title: BaseStringOption
 * Description: The abstract base class for string DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseStringOption extends BaseDhcpOption implements DhcpComparableOption
{
	private static Logger log = LoggerFactory.getLogger(BaseStringOption.class);

	protected String string;
	
	public BaseStringOption()
	{
		this(null);
	}
	
	public BaseStringOption(StringOptionType stringOption)
	{
		super();
		if (stringOption != null) {
			string = stringOption.getString();
		}
	}
	
    public String getString() {
		return string;
	}

	public void setString(String string)
	{
		this.string = string;
	}

	/* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        if (string != null) {
            buf.put(string.getBytes());
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
            byte[] b = new byte[len];
            buf.get(b);
            string = new String(b);
        }
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        int len = 0;
        if (string != null) {
            len = string.length();
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
        if (string == null)
        	return false;
        
        StringOptionType exprOption = expression.getStringOption();
        if (exprOption != null) {
        	String exprString = exprOption.getString();
            Operator.Enum op = expression.getOperator();
            if (op.equals(Operator.EQUALS)) {
            	return string.equals(exprString);
            }
            else if (op.equals(Operator.STARTS_WITH)) {
            	return string.startsWith(exprString);
            }
            else if (op.equals(Operator.ENDS_WITH)) {
            	return string.endsWith(exprString);
            }
            else if (op.equals(Operator.CONTAINS)) {
            	return string.contains(exprString);
            }
            else if (op.equals(Operator.REG_EXP)) {
            	return string.matches(exprString);
            }
            else {
            	log.warn("Unsupported expression operator: " + op);
            }
        }
        
        // then see if we have an opaque option
        OpaqueDataOptionType opaqueOption = expression.getOpaqueDataOption();
        if (opaqueOption != null) {
	        OpaqueData opaque = opaqueOption.getOpaqueData();
	        if (opaque != null) {
	            String ascii = opaque.getAsciiValue();
	            if (ascii != null) {
	            	if (string.equals(ascii)) {
	            		return true;
	            	}
	            }
	            else {
	                byte[] hex = opaque.getHexValue();
	                if (hex != null) {
	                	String hexString = new String(hex);
	                	if (string.equals(hexString)) {
	                        return true;
	                    }
	                }
	            }
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
        sb.append(": string=");
        sb.append(string);
        return sb.toString();
    }

}
