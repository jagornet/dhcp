/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseOpaqueDataOption.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.OptionExpression;

/**
 * Title: BaseOpaqueDataOption
 * Description: The abstract base class for opaque data DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseOpaqueDataOption extends BaseDhcpOption implements DhcpComparableOption
{
    /**
     * Gets the opaque data.
     * 
     * @return the opaque data
     */
    public abstract OpaqueData getOpaqueData();
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        return OpaqueDataUtil.getLengthDataOnly(this.getOpaqueData());
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        OpaqueDataUtil.encodeDataOnly(iobuf, this.getOpaqueData());
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
            int eof = iobuf.position() + len;
            while (iobuf.position() < eof) {
                OpaqueData opaque = OpaqueDataUtil.decodeDataOnly(iobuf, len);
                if (opaque.getAsciiValue() != null) {
                    this.getOpaqueData().setAsciiValue(opaque.getAsciiValue());
                }
                else {
                    this.getOpaqueData().setHexValue(opaque.getHexValue());
                }
            }
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

        return OpaqueDataUtil.matches(expression, this.getOpaqueData());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof BaseOpaqueDataOption) {
            BaseOpaqueDataOption that = (BaseOpaqueDataOption) obj;
            return OpaqueDataUtil.equals(this.getOpaqueData(), 
                                         that.getOpaqueData());
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(this.getName());
        sb.append(": ");
        sb.append(OpaqueDataUtil.toString(this.getOpaqueData()));
        return sb.toString();
    }
}
