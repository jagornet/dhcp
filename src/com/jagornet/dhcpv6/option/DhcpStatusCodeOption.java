/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpStatusCodeOption.java is part of DHCPv6.
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
import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.OptionExpression;
import com.jagornet.dhcpv6.xml.StatusCodeOption;

/**
 * <p>Title: DhcpStatusCodeOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpStatusCodeOption extends BaseDhcpOption implements DhcpComparableOption
{
    /** The status code option. */
    private StatusCodeOption statusCodeOption;
    
    /**
     * Instantiates a new dhcp status code option.
     */
    public DhcpStatusCodeOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new dhcp status code option.
     * 
     * @param statusCodeOption the status code option
     */
    public DhcpStatusCodeOption(StatusCodeOption statusCodeOption)
    {
        super();
        if (statusCodeOption != null)
            this.statusCodeOption = statusCodeOption;
        else
            this.statusCodeOption = StatusCodeOption.Factory.newInstance();
    }

    /**
     * Gets the status code option.
     * 
     * @return the status code option
     */
    public StatusCodeOption getStatusCodeOption()
    {
        return statusCodeOption;
    }

    /**
     * Sets the status code option.
     * 
     * @param statusCodeOption the new status code option
     */
    public void setStatusCodeOption(StatusCodeOption statusCodeOption)
    {
        if (statusCodeOption != null)
            this.statusCodeOption = statusCodeOption;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        String msg = statusCodeOption.getMessage();
        return (2 + (msg!=null ? msg.length() : 0));
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        iobuf.putShort((short)statusCodeOption.getStatusCode());
        String msg = statusCodeOption.getMessage();
        if (msg != null) {
        	iobuf.put(msg.getBytes());
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
            statusCodeOption.setStatusCode(iobuf.getUnsignedShort());
            int eof = iobuf.position() + len;
            if (iobuf.position() < eof) {
                byte[] data = new byte[len-2];  // minus 2 for the status code
                iobuf.get(data);
                statusCodeOption.setMessage(new String(data));
            }
        }
    }

    /**
     * Matches only the status code, not the message text.
     * 
     * @param expression the expression
     * 
     * @return true, if matches
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
                	// need an int to handle unsigned short
                    if (statusCodeOption.getStatusCode() == Integer.parseInt(ascii)) {
                        return true;
                    }
                }
                catch (NumberFormatException ex) { }
            }
            else {
                byte[] hex = opaque.getHexValue();
                if ( (hex != null) && 
                     (hex.length >= 1) && (hex.length <= 2) ) {
                	int hexInt = Integer.parseInt(Util.toHexString(hex));
                    if (statusCodeOption.getStatusCode() == hexInt) {
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
        return statusCodeOption.getCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.getName());
        sb.append(": ");
        sb.append("code=");
        sb.append(statusCodeOption.getStatusCode());
        sb.append(" message=");
        sb.append(statusCodeOption.getMessage());
        return sb.toString();
    }
    
}
