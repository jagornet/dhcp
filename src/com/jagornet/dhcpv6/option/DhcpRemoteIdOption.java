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

import org.apache.mina.core.buffer.IoBuffer;

import com.jagornet.dhcpv6.option.base.BaseOpaqueDataOption;
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
	public DhcpRemoteIdOption()
	{
		this(null);
	}
	
	public DhcpRemoteIdOption(RemoteIdOption remoteIdOption)
	{
		if (remoteIdOption != null)
			this.opaqueDataOption = remoteIdOption;
		else
			this.opaqueDataOption = RemoteIdOption.Factory.newInstance();
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
        IoBuffer iobuf = super.encodeCodeAndLength();
        RemoteIdOption remoteIdOption = (RemoteIdOption)opaqueDataOption;
        iobuf.putInt((int)remoteIdOption.getEnterpriseNumber());
        OpaqueDataUtil.encodeDataOnly(iobuf, remoteIdOption.getOpaqueData());
        return iobuf.flip().buf();
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Decodable#decode(java.nio.ByteBuffer)
     */
	@SuppressWarnings("unused")
    public void decode(ByteBuffer buf) throws IOException
    {
    	IoBuffer iobuf = IoBuffer.wrap(buf);
    	int len = super.decodeLength(iobuf);
    	if ((len > 0) && (len <= iobuf.remaining())) {
            int eof = iobuf.position() + len;
            if (iobuf.position() < eof) {
                RemoteIdOption remoteIdOption = (RemoteIdOption)opaqueDataOption;
                remoteIdOption.setEnterpriseNumber(iobuf.getUnsignedInt());
                while (iobuf.position() < eof) {
                	OpaqueData opaque = remoteIdOption.addNewOpaqueData();
                    OpaqueDataUtil.decodeDataOnly(opaque, iobuf, len);
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
        StringBuilder sb = new StringBuilder(super.getName());
        sb.append(": ");
        sb.append("Enterprise Number=");
        RemoteIdOption remoteIdOption = (RemoteIdOption)opaqueDataOption;
        sb.append(remoteIdOption.getEnterpriseNumber());
        sb.append(" ");
        sb.append(super.toString());
        return sb.toString();
    }
}
