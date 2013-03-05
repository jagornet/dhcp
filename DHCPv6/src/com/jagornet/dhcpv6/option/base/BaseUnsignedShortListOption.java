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
import java.util.ArrayList;
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

    protected List<Integer> unsignedShortList;

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
        if (uShortListOption != null) {
            if (uShortListOption.getUnsignedShortList() != null) {
            	unsignedShortList = uShortListOption.getUnsignedShortList();
            }
        }
    }

    public List<Integer> getUnsignedShortList() {
		return unsignedShortList;
	}

	public void setUnsignedShortList(List<Integer> unsignedShorts) {
		this.unsignedShortList = unsignedShorts;
	}
	
	public void addUnsignedShort(int unsignedShort) {
		if (unsignedShortList == null) {
			unsignedShortList = new ArrayList<Integer>();
		}
		unsignedShortList.add(unsignedShort);
	}

	/* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        if ((unsignedShortList != null) && !unsignedShortList.isEmpty()) {
            for (int ushort : unsignedShortList) {
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
                	addUnsignedShort(Util.getUnsignedShort(buf));
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
        if ((unsignedShortList != null) && !unsignedShortList.isEmpty()) {
            len = unsignedShortList.size() * 2;
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
        if (unsignedShortList == null)
        	return false;
        
        UnsignedShortListOptionType exprOption = expression.getUShortListOption();
        if (exprOption != null) {
        	List<Integer> exprUshorts = exprOption.getUnsignedShortList();
            Operator.Enum op = expression.getOperator();
            if (op.equals(Operator.EQUALS)) {
            	return unsignedShortList.equals(exprUshorts);
            }
            else if (op.equals(Operator.CONTAINS)) {
            	return unsignedShortList.containsAll(exprUshorts);
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
        sb.append(": unsignedShortList=");
        if ((unsignedShortList != null) && !unsignedShortList.isEmpty()) {
        	for (Integer ushort : unsignedShortList) {
				sb.append(ushort);
				sb.append(',');
			}
        	sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }
    
}
