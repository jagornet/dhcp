/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseUnsignedShortListOption.java is part of DHCPv6.
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
import com.jagornet.dhcpv6.xml.Operator;
import com.jagornet.dhcpv6.xml.OptionExpression;
import com.jagornet.dhcpv6.xml.UnsignedShortListOptionType;

/**
 * Title: BaseUnsignedShortListOption
 * Description: The abstract base class for unsigned short list DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseUnsignedShortListOption extends BaseDhcpOption implements DhcpComparableOption
{
	private static Logger log = LoggerFactory.getLogger(BaseUnsignedShortListOption.class);

    /** The unsigned short list option. */
    protected UnsignedShortListOptionType uShortListOption;

    /**
     * Instantiates a new unsigned short list option.
     */
    public BaseUnsignedShortListOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new unsigned short list option.
     * 
     * @param uShortListOption the option request option
     */
    public BaseUnsignedShortListOption(UnsignedShortListOptionType uShortListOption)
    {
        super();
        if (uShortListOption != null)
            this.uShortListOption = uShortListOption;
        else
            this.uShortListOption = UnsignedShortListOptionType.Factory.newInstance();
    }

    /**
     * Gets the unsigned short list option.
     * 
     * @return the unsigned short list option
     */
    public UnsignedShortListOptionType getUnsignedShortListOption()
    {
        return uShortListOption;
    }

    /**
     * Sets the unsigned short list option.
     * 
     * @param uShortListOption the new unsigned short list option
     */
    public void setUnsignedShortListOption(UnsignedShortListOptionType uShortListOption)
    {
        if (uShortListOption != null)
            this.uShortListOption = uShortListOption;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        List<Integer> ushorts = uShortListOption.getUnsignedShortList();
        if ((ushorts != null) && !ushorts.isEmpty()) {
            for (int ushort : ushorts) {
                buf.putShort((short)ushort);
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
            for (int i=0; i<len/2; i++) {
                if (buf.hasRemaining()) {
                	uShortListOption.addUnsignedShort(Util.getUnsignedShort(buf));
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        int len = 0;
        List<Integer> ushorts = uShortListOption.getUnsignedShortList();
        if ((ushorts != null) && !ushorts.isEmpty()) {
            len = ushorts.size() * 2;
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
        if (uShortListOption == null)
        	return false;

        List<Integer> myUshorts = uShortListOption.getUnsignedShortList();
        if (myUshorts == null)
        	return false;
        
        UnsignedShortListOptionType that = expression.getUShortListOption();
        if (that != null) {
        	List<Integer> ushorts = that.getUnsignedShortList();
            Operator.Enum op = expression.getOperator();
            if (op.equals(Operator.EQUALS)) {
            	return myUshorts.equals(ushorts);
            }
            else if (op.equals(Operator.CONTAINS)) {
            	return myUshorts.containsAll(ushorts);
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
        sb.append(uShortListOption.toString());
        return sb.toString();
    }
    
}
