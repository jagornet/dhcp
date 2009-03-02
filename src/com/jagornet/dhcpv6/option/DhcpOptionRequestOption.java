/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpOptionRequestOption.java is part of DHCPv6.
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
import java.util.List;

import org.apache.mina.core.buffer.IoBuffer;

import com.jagornet.dhcpv6.xml.OptionRequestOption;

/**
 * <p>Title: DhcpOptionRequestOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpOptionRequestOption extends BaseDhcpOption
{
    /** The option request option. */
    private OptionRequestOption optionRequestOption;

    /**
     * Instantiates a new dhcp option request option.
     */
    public DhcpOptionRequestOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new dhcp option request option.
     * 
     * @param optionRequestOption the option request option
     */
    public DhcpOptionRequestOption(OptionRequestOption optionRequestOption)
    {
        super();
        if (optionRequestOption != null)
            this.optionRequestOption = optionRequestOption;
        else
            this.optionRequestOption = OptionRequestOption.Factory.newInstance();
    }

    /**
     * Gets the option request option.
     * 
     * @return the option request option
     */
    public OptionRequestOption getOptionRequestOption()
    {
        return optionRequestOption;
    }

    /**
     * Sets the option request option.
     * 
     * @param optionRequestOption the new option request option
     */
    public void setOptionRequestOption(OptionRequestOption optionRequestOption)
    {
        if (optionRequestOption != null)
            this.optionRequestOption = optionRequestOption;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        List<Integer> oros = optionRequestOption.getRequestedOptionCodesList();
        if ((oros != null) && !oros.isEmpty()) {
            for (int oro : oros) {
                iobuf.putShort((short)oro);
            }
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
            for (int i=0; i<len/2; i++) {
                if (iobuf.hasRemaining()) {
                    this.addRequestedOptionCode(iobuf.getUnsignedShort());
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
        List<Integer> oros = optionRequestOption.getRequestedOptionCodesList();
        if ((oros != null) && !oros.isEmpty()) {
            len = oros.size() * 2;
        }
        return len;
    }

    /**
     * Adds the requested option code.
     * 
     * @param code the code
     */
    public void addRequestedOptionCode(Integer code)
    {
        if (code != null) {
        	optionRequestOption.addRequestedOptionCodes(code);
        }
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return optionRequestOption.getCode();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.getName());
        sb.append(": ");
        List<Integer> oros = optionRequestOption.getRequestedOptionCodesList();
        if ((oros != null) && !oros.isEmpty()) {
            for (Integer oro : oros) {
                sb.append(oro);
                sb.append(",");
            }
            sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }
    
}
