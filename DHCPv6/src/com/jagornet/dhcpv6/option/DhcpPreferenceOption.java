/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpPreferenceOption.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.xml.PreferenceOption;

/**
 * <p>Title: DhcpElapsedTimeOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpPreferenceOption extends BaseDhcpOption
{
    /** The preference option. */
    private PreferenceOption preferenceOption;

    /**
     * Instantiates a new dhcp preference option.
     */
    public DhcpPreferenceOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new dhcp preference option.
     * 
     * @param preferenceOption the preference option
     */
    public DhcpPreferenceOption(PreferenceOption preferenceOption)
    {
        super();
        if (preferenceOption != null)
            this.preferenceOption = preferenceOption;
        else
            this.preferenceOption = PreferenceOption.Factory.newInstance();
    }

    /**
     * Gets the preference option.
     * 
     * @return the preference option
     */
    public PreferenceOption getPreferenceOption()
    {
        return preferenceOption;
    }

    /**
     * Sets the preference option.
     * 
     * @param preferenceOption the new preference option
     */
    public void setPreferenceOption(PreferenceOption preferenceOption)
    {
        if (preferenceOption != null)
            this.preferenceOption = preferenceOption;
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
        IoBuffer iobuf = super.encodeCodeAndLength();
        iobuf.put((byte)preferenceOption.getShortValue());
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
            preferenceOption.setShortValue(iobuf.getUnsigned());
        }
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return preferenceOption.getCode();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.getName());
        sb.append(": ");
        sb.append(preferenceOption.getShortValue());
        return sb.toString();
    }
    
}
