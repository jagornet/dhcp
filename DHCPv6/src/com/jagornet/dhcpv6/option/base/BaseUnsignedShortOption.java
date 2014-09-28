/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseUnsignedShortOption.java is part of DHCPv6.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.option.DhcpComparableOption;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcp.xml.OpaqueData;
import com.jagornet.dhcp.xml.OpaqueDataOptionType;
import com.jagornet.dhcp.xml.Operator;
import com.jagornet.dhcp.xml.OptionExpression;
import com.jagornet.dhcp.xml.UnsignedShortOptionType;

/**
 * Title: BaseUnsignedShortOption
 * Description: The abstract base class for unsigned short DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseUnsignedShortOption extends BaseDhcpOption implements DhcpComparableOption
{ 
	private static Logger log = LoggerFactory.getLogger(BaseUnsignedShortOption.class);

    protected int unsignedShort;
    
    /**
     * Instantiates a new unsigned short option.
     */
    public BaseUnsignedShortOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new unsigned short option.
     * 
     * @param uShortOption the elapsed time option
     */
    public BaseUnsignedShortOption(UnsignedShortOptionType uShortOption)
    {
        super();
        if (uShortOption != null) {
            unsignedShort = uShortOption.getUnsignedShort();
        }
    }

    public int getUnsignedShort() {
		return unsignedShort;
	}

	public void setUnsignedShort(int unsignedShort) {
		this.unsignedShort = unsignedShort;
	}

	/* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        return 2;   // always two bytes (short)
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        buf.putShort((short)unsignedShort);
        return (ByteBuffer) buf.flip();
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Decodable#decode(java.nio.ByteBuffer)
     */
    public void decode(ByteBuffer buf) throws IOException
    {
    	int len = super.decodeLength(buf);
    	if ((len > 0) && (len <= buf.remaining())) {
    		unsignedShort = Util.getUnsignedShort(buf);
        }
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
        
        UnsignedShortOptionType exprOption = expression.getUShortOption();
        if (exprOption != null) {
        	int exprUshort = exprOption.getUnsignedShort();
        	Operator.Enum op = expression.getOperator();
        	if (op.equals(Operator.EQUALS)) {
        		return (unsignedShort == exprUshort);
        	}
        	else if (op.equals(Operator.LESS_THAN)) {
        		return (unsignedShort < exprUshort);
        	}
        	else if (op.equals(Operator.LESS_THAN_OR_EQUAL)) {
        		return (unsignedShort <= exprUshort);
        	}
        	else if (op.equals(Operator.GREATER_THAN)) {
        		return (unsignedShort > exprUshort);
        	}
        	else if (op.equals(Operator.GREATER_THAN_OR_EQUAL)) {
        		return (unsignedShort >= exprUshort);
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
	                try {
	                	// need an Integer to handle unsigned short
	                    if (unsignedShort == Integer.parseInt(ascii)) {
	                        return true;
	                    }
	                }
	                catch (NumberFormatException ex) { 
	                	log.error("Invalid unsigned short ASCII value for OpaqueData: " + ascii, ex);
	                }
	            }
	            else {
	                byte[] hex = opaque.getHexValue();
	                if ( (hex != null) && 
	                     (hex.length >= 1) && (hex.length <= 2) ) {
	                	int hexUnsignedShort = Integer.valueOf(Util.toHexString(hex), 16);
	                    if (unsignedShort == hexUnsignedShort) {
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
        sb.append(": unsignedShort=");
        sb.append(unsignedShort);
        return sb.toString();
    }
    
}
