/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseUnsignedByteListOption.java is part of DHCPv6.
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
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.option.DhcpComparableOption;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.Operator;
import com.jagornet.dhcpv6.xml.OptionExpression;
import com.jagornet.dhcpv6.xml.UnsignedByteListOptionType;

/**
 * Title: BaseUnsignedByteListOption
 * Description: The abstract base class for unsigned short list DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseUnsignedByteListOption extends BaseDhcpOption implements DhcpComparableOption
{
	private static Logger log = LoggerFactory.getLogger(BaseUnsignedByteListOption.class);

    protected List<Short> unsignedByteList;

    /**
     * Instantiates a new unsigned short list option.
     */
    public BaseUnsignedByteListOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new unsigned short list option.
     * 
     * @param uByteListOption the option request option
     */
    public BaseUnsignedByteListOption(UnsignedByteListOptionType uByteListOption)
    {
        super();
        if (uByteListOption != null) {
            if (uByteListOption.getUnsignedByteList() != null) {
            	unsignedByteList = uByteListOption.getUnsignedByteList();
            }
        }
    }

    public List<Short> getUnsignedByteList() {
		return unsignedByteList;
	}

	public void setUnsignedByteList(List<Short> unsignedBytes) {
		this.unsignedByteList = unsignedBytes;
	}
	
	public void addUnsignedByte(short ubyte) {
		if (unsignedByteList == null) {
			unsignedByteList = new ArrayList<Short>();
		}
		unsignedByteList.add(ubyte);
	}

	/* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        if ((unsignedByteList != null) && !unsignedByteList.isEmpty()) {
            for (short ubyte : unsignedByteList) {
                buf.put((byte)ubyte);
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
            for (int i=0; i<len; i++) {
                if (buf.hasRemaining()) {
                	addUnsignedByte(Util.getUnsignedByte(buf));
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
        if ((unsignedByteList != null) && !unsignedByteList.isEmpty()) {
            len = unsignedByteList.size();
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
        if (unsignedByteList == null)
        	return false;
        
        UnsignedByteListOptionType exprOption = expression.getUByteListOption();
        if (exprOption != null) {
        	List<Short> exprUbytes = exprOption.getUnsignedByteList();
            Operator.Enum op = expression.getOperator();
            if (op.equals(Operator.EQUALS)) {
            	return unsignedByteList.equals(exprUbytes);
            }
            else if (op.equals(Operator.CONTAINS)) {
            	return unsignedByteList.containsAll(exprUbytes);
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
        sb.append(": unsignedByteList=");
        if ((unsignedByteList != null) && !unsignedByteList.isEmpty()) {
        	for (Short ubyte : unsignedByteList) {
				sb.append(ubyte);
				sb.append(',');
			}
        	sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }
    
}
