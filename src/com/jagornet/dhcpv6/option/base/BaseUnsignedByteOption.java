/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseUnsignedByteOption.java is part of DHCPv6.
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
import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.OpaqueDataOptionType;
import com.jagornet.dhcpv6.xml.Operator;
import com.jagornet.dhcpv6.xml.OptionExpression;
import com.jagornet.dhcpv6.xml.UnsignedByteOptionType;

/**
 * Title: BaseUnsignedByteOption
 * Description: The abstract base class for unsigned byte DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseUnsignedByteOption extends BaseDhcpOption implements DhcpComparableOption
{
	private static Logger log = LoggerFactory.getLogger(BaseUnsignedByteOption.class);

	/** The unsigned byte option. */
    protected UnsignedByteOptionType uByteOption;

    /**
     * Instantiates a new unsigned byte option.
     */
    public BaseUnsignedByteOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new unsigned byte option.
     * 
     * @param uByteOption the unsigned byte option
     */
    public BaseUnsignedByteOption(UnsignedByteOptionType uByteOption)
    {
        super();
        if (uByteOption != null)
            this.uByteOption = uByteOption;
        else
            this.uByteOption = UnsignedByteOptionType.Factory.newInstance();
    }

    /**
     * Gets the unsigned byte option.
     * 
     * @return the unsigned byte option
     */
    public UnsignedByteOptionType getUnsignedByteOption()
    {
        return uByteOption;
    }

    /**
     * Sets the unsigned byte option.
     * 
     * @param uByteOption the new unsigned byte option
     */
    public void setUnsignedByteOption(UnsignedByteOptionType uByteOption)
    {
        if (uByteOption != null)
            this.uByteOption = uByteOption;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        return 1;   // always one bytes
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        buf.put((byte)uByteOption.getUnsignedByte());
        return (ByteBuffer) buf.flip();
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Decodable#decode(java.nio.ByteBuffer)
     */
    public void decode(ByteBuffer buf) throws IOException
    {
    	int len = super.decodeLength(buf);
    	if ((len > 0) && (len <= buf.remaining())) {
    		uByteOption.setUnsignedByte(Util.getUnsignedByte(buf));
        }
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
        if (uByteOption == null)
        	return false;

        short myUbyte = uByteOption.getUnsignedByte();
        
        UnsignedByteOptionType that = expression.getUByteOption();
        if (that != null) {
        	short ubyte = that.getUnsignedByte();
        	Operator.Enum op = expression.getOperator();
        	if (op.equals(Operator.EQUALS)) {
        		return (myUbyte == ubyte);
        	}
        	else if (op.equals(Operator.LESS_THAN)) {
        		return (myUbyte < ubyte);
        	}
        	else if (op.equals(Operator.LESS_THAN_OR_EQUAL)) {
        		return (myUbyte <= ubyte);
        	}
        	else if (op.equals(Operator.GREATER_THAN)) {
        		return (myUbyte > ubyte);
        	}
        	else if (op.equals(Operator.GREATER_THAN_OR_EQUAL)) {
        		return (myUbyte >= ubyte);
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
	                	// need an Short to handle unsigned byte
	                    if (uByteOption.getUnsignedByte() == Short.parseShort(ascii)) {
	                        return true;
	                    }
	                }
	                catch (NumberFormatException ex) { 
	                	log.error("Invalid unsigned byte ASCII value for OpaqueData: " + ascii, ex);
	                }
	            }
	            else {
	                byte[] hex = opaque.getHexValue();
	                if ((hex != null) && (hex.length == 1)) {
	                	int hexUnsignedByte = Short.valueOf(Util.toHexString(hex), 16);
	                    if (uByteOption.getUnsignedByte() == hexUnsignedByte) {
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
        sb.append(Util.LINE_SEPARATOR);
        // use XmlObject implementation
        sb.append(uByteOption.toString());
        return sb.toString();
    }
    
}
