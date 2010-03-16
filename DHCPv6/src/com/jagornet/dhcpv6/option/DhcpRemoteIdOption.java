/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpRemoteIdOption.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.option.base.BaseOpaqueDataOption;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.OptionExpression;
import com.jagornet.dhcpv6.xml.RemoteIdOption;

/**
 * <p>Title: DhcpRemoteIdOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpRemoteIdOption extends BaseOpaqueDataOption
{
	
	/**
	 * Instantiates a new dhcp remote id option.
	 */
	public DhcpRemoteIdOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp remote id option.
	 * 
	 * @param remoteIdOption the remote id option
	 */
	public DhcpRemoteIdOption(RemoteIdOption remoteIdOption)
	{
		if (remoteIdOption != null) {
			this.opaqueDataOption = remoteIdOption;
		}
		else {
			this.opaqueDataOption = RemoteIdOption.Factory.newInstance();
            // create an OpaqueData element to actually hold the data
            this.opaqueDataOption.addNewOpaqueData();
		}
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
        RemoteIdOption remoteIdOption = (RemoteIdOption)opaqueDataOption;
        buf.putInt((int)remoteIdOption.getEnterpriseNumber());
        OpaqueDataUtil.encodeDataOnly(buf, remoteIdOption.getOpaqueData());
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
                RemoteIdOption remoteIdOption = (RemoteIdOption)opaqueDataOption;
                remoteIdOption.setEnterpriseNumber(Util.getUnsignedInt(buf));
                while (buf.position() < eof) {
                	OpaqueData opaque = remoteIdOption.addNewOpaqueData();
                    OpaqueDataUtil.decodeDataOnly(opaque, buf, len);
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

        return false;
    }
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return ((RemoteIdOption)opaqueDataOption).getCode();
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
        sb.append(((RemoteIdOption)opaqueDataOption).toString());
        return sb.toString();
    }
}
