/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpElapsedTimeOption.java is part of DHCPv6.
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
import com.jagornet.dhcpv6.xml.ElapsedTimeOption;
import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.OptionExpression;

/**
 * <p>Title: DhcpElapsedTimeOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpElapsedTimeOption extends BaseDhcpOption implements DhcpComparableOption
{ 
    /** The elapsed time option. */
    private ElapsedTimeOption elapsedTimeOption;
    
    /**
     * Instantiates a new dhcp elapsed time option.
     */
    public DhcpElapsedTimeOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new dhcp elapsed time option.
     * 
     * @param elapsedTimeOption the elapsed time option
     */
    public DhcpElapsedTimeOption(ElapsedTimeOption elapsedTimeOption)
    {
        super();
        if (elapsedTimeOption != null)
            this.elapsedTimeOption = elapsedTimeOption;
        else
            this.elapsedTimeOption = ElapsedTimeOption.Factory.newInstance();
    }

    /**
     * Gets the elapsed time option.
     * 
     * @return the elapsed time option
     */
    public ElapsedTimeOption getElapsedTimeOption()
    {
        return elapsedTimeOption;
    }

    /**
     * Sets the elapsed time option.
     * 
     * @param elapsedTimeOption the new elapsed time option
     */
    public void setElapsedTimeOption(ElapsedTimeOption elapsedTimeOption)
    {
        if (elapsedTimeOption != null)
            this.elapsedTimeOption = elapsedTimeOption;
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
        IoBuffer iobuf = super.encodeCodeAndLength();
        iobuf.putShort((short)elapsedTimeOption.getIntValue());
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
    		elapsedTimeOption.setIntValue(iobuf.getUnsignedShort());
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
                	// need an Integer to handle unsigned short
                    if (elapsedTimeOption.getIntValue() == Integer.parseInt(ascii)) {
                        return true;
                    }
                }
                catch (NumberFormatException ex) { }
            }
            else {
                byte[] hex = opaque.getHexValue();
                if ( (hex != null) && 
                     (hex.length >= 1) && (hex.length <= 2) ) {
                	int hexUnsignedShort = Integer.valueOf(Util.toHexString(hex), 16);
                    if (elapsedTimeOption.getIntValue() == hexUnsignedShort) {
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
        return elapsedTimeOption.getCode();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.getName());
        sb.append(": ");
        sb.append(elapsedTimeOption.getIntValue());
        return sb.toString();
    }
    
}
