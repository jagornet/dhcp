/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseStringValueOption.java is part of DHCPv6.
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

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.option.DhcpComparableOption;
import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.OpaqueDataOptionType;
import com.jagornet.dhcpv6.xml.Operator;
import com.jagornet.dhcpv6.xml.OptionExpression;
import com.jagornet.dhcpv6.xml.StringOptionType;

/**
 * Title: BaseStringOption
 * Description: The abstract base class for string DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseStringOption extends BaseDhcpOption implements DhcpComparableOption
{
	private static Logger log = LoggerFactory.getLogger(BaseStringOption.class);

	protected StringOptionType stringOption;
	
	public BaseStringOption()
	{
		this(null);
	}
	
	public BaseStringOption(StringOptionType stringOption)
	{
		super();
		if (stringOption != null)
			this.stringOption = stringOption;
		else
			this.stringOption = StringOptionType.Factory.newInstance();
	}
	
    public StringOptionType getStringOption()
    {
		return stringOption;
	}

	public void setStringOption(StringOptionType stringOption)
	{
		if (stringOption != null)
			this.stringOption = stringOption;
	}

	/* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        String stringValue = stringOption.getString();
        if (stringValue != null) {
            iobuf.put(stringValue.getBytes());
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
            byte b[] = new byte[len];
            iobuf.get(b);
            stringOption.setString(new String(b));
        }
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        int len = 0;
        String stringValue = stringOption.getString();
        if (stringValue != null) {
            len = stringValue.length();
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
        if (stringOption == null)
        	return false;
        
        String myString = stringOption.getString();
        if (myString == null)
        	return false;
        
        StringOptionType that = expression.getStringOption();
        if (that != null) {
        	String string = that.getString();
            Operator.Enum op = expression.getOperator();
            if (op.equals(Operator.EQUALS)) {
            	return myString.equals(string);
            }
            else if (op.equals(Operator.STARTS_WITH)) {
            	return myString.startsWith(string);
            }
            else if (op.equals(Operator.ENDS_WITH)) {
            	return myString.endsWith(string);
            }
            else if (op.equals(Operator.CONTAINS)) {
            	return myString.contains(string);
            }
            else if (op.equals(Operator.REG_EXP)) {
            	return myString.matches(string);
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
	            	if (stringOption.getString().equals(ascii)) {
	            		return true;
	            	}
	            }
	            else {
	                byte[] hex = opaque.getHexValue();
	                if (hex != null) {
	                	String hexString = new String(hex);
	                	if (stringOption.getString().equals(hexString)) {
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
        if (this == null)
            return null;
        
        StringBuilder sb = new StringBuilder(this.getName());
        sb.append(": ");
        String stringValue = stringOption.getString();
        if (stringValue != null) {
            sb.append(stringValue);
        }            
        return sb.toString();
    }

}
