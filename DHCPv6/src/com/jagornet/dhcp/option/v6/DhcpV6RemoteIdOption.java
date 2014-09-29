/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6RemoteIdOption.java is part of DHCPv6.
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
package com.jagornet.dhcp.option.v6;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.jagornet.dhcp.option.base.BaseOpaqueDataOption;
import com.jagornet.dhcp.util.DhcpConstants;
import com.jagornet.dhcp.util.Util;
import com.jagornet.dhcp.xml.OptionExpression;
import com.jagornet.dhcp.xml.V6RemoteIdOption;

/**
 * <p>Title: DhcpV6RemoteIdOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6RemoteIdOption extends BaseOpaqueDataOption
{
	private long enterpriseNumber;	// long for unsigned int
	
	/**
	 * Instantiates a new dhcp remote id option.
	 */
	public DhcpV6RemoteIdOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp remote id option.
	 * 
	 * @param remoteIdOption the remote id option
	 */
	public DhcpV6RemoteIdOption(V6RemoteIdOption remoteIdOption)
	{
    	super(remoteIdOption);
    	if (remoteIdOption != null) {
    		enterpriseNumber = remoteIdOption.getEnterpriseNumber();
    	}
    	setCode(DhcpConstants.OPTION_REMOTE_ID);
	}
	
    public long getEnterpriseNumber() {
		return enterpriseNumber;
	}

	public void setEnterpriseNumber(long enterpriseNumber) {
		this.enterpriseNumber = enterpriseNumber;
	}

	/* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        int len = 4 + super.getLength();  // size of enterprise number (int)
        return len;
    }
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        buf.putInt((int)enterpriseNumber);
        opaqueData.encode(buf);
        return (ByteBuffer) buf.flip();
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Decodable#decode(java.nio.ByteBuffer)
     */
	public void decode(ByteBuffer buf) throws IOException
    {
    	int len = super.decodeLength(buf);
    	if ((len > 0) && (len <= buf.remaining())) {
            int eof = buf.position() + len;
            if (buf.position() < eof) {
                enterpriseNumber = Util.getUnsignedInt(buf);
                if (buf.position() < eof) {
                	opaqueData.decode(buf, len-4);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpComparableOption#matches(com.jagornet.dhcp.xml.OptionExpression)
     */
    public boolean matches(OptionExpression expression)
    {
        if (expression == null)
            return false;
        if (expression.getCode() != this.getCode())
            return false;

        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(" enterpriseNumber=");
        sb.append(enterpriseNumber);
        return sb.toString();
    }
}
