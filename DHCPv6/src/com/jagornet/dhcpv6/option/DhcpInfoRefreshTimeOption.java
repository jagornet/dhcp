/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpInfoRefreshTimeOption.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.option;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.mina.core.buffer.IoBuffer;

import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.InfoRefreshTimeOption;
import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.OptionExpression;

/**
 * <p>Title: DhcpInfoRefreshTimeOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpInfoRefreshTimeOption extends BaseDhcpOption implements DhcpComparableOption
{ 
    /** The info refresh time option. */
    private InfoRefreshTimeOption infoRefreshTimeOption;
    
    /**
     * Instantiates a new dhcp info refresh time option.
     */
    public DhcpInfoRefreshTimeOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new dhcp info refresh time option.
     * 
     * @param infoRefreshTimeOption the info refresh time option
     */
    public DhcpInfoRefreshTimeOption(InfoRefreshTimeOption infoRefreshTimeOption)
    {
        super();
        if (infoRefreshTimeOption != null)
            this.infoRefreshTimeOption = infoRefreshTimeOption;
        else
            this.infoRefreshTimeOption = InfoRefreshTimeOption.Factory.newInstance();
    }

    /**
     * Gets the info refresh time option.
     * 
     * @return the info refresh time option
     */
    public InfoRefreshTimeOption getInfoRefreshTimeOption()
    {
        return infoRefreshTimeOption;
    }

    /**
     * Sets the info refresh time option.
     * 
     * @param infoRefreshTimeOption the new info refresh time option
     */
    public void setInfoRefreshTimeOption(InfoRefreshTimeOption infoRefreshTimeOption)
    {
        if (infoRefreshTimeOption != null)
            this.infoRefreshTimeOption = infoRefreshTimeOption;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        return 4;   // always four bytes (int)
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        iobuf.putInt((int)infoRefreshTimeOption.getLongValue());
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
    		infoRefreshTimeOption.setLongValue(iobuf.getUnsignedInt());
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

        OpaqueData opaque = expression.getData();
        if (opaque != null) {
            String ascii = opaque.getAsciiValue();
            if (ascii != null) {
                try {
                	// need a long to handle unsigned int
                    if (infoRefreshTimeOption.getLongValue() == Long.parseLong(ascii)) {
                        return true;
                    }
                }
                catch (NumberFormatException ex) { }
            }
            else {
                byte[] hex = opaque.getHexValue();
                if ( (hex != null) && 
                     (hex.length >= 1) && (hex.length <= 4) ) {
                	long hexLong = Long.valueOf(Util.toHexString(hex), 16);
                    if (infoRefreshTimeOption.getLongValue() == hexLong) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return infoRefreshTimeOption.getCode();
    }
   
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.getName());
        sb.append(": ");
        sb.append(infoRefreshTimeOption.getLongValue());
        return sb.toString();
    }    
}
