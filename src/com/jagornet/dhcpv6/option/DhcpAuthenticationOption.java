/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpAuthenticationOption.java is part of DHCPv6.
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
import java.math.BigInteger;
import java.nio.ByteBuffer;

import com.jagornet.dhcpv6.option.base.BaseDhcpOption;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.AuthenticationOption;
import com.jagornet.dhcpv6.xml.OpaqueData;

/**
 * <p>Title: DhcpAuthenticationOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpAuthenticationOption extends BaseDhcpOption
{
    
    /** The authentication option. */
    private AuthenticationOption authenticationOption;
    
    /**
     * Instantiates a new dhcp authentication option.
     */
    public DhcpAuthenticationOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new dhcp authentication option.
     * 
     * @param authenticationOption the authentication option
     */
    public DhcpAuthenticationOption(AuthenticationOption authenticationOption)
    {
        super();
        if (authenticationOption != null)
            this.authenticationOption = authenticationOption;
        else
            this.authenticationOption = AuthenticationOption.Factory.newInstance();
    }

    /**
     * Gets the authentication option.
     * 
     * @return the authentication option
     */
    public AuthenticationOption getAuthenticationOption()
    {
        return authenticationOption;
    }

    /**
     * Sets the authentication option.
     * 
     * @param authenticationOption the new authentication option
     */
    public void setAuthenticationOption(AuthenticationOption authenticationOption)
    {
        if (authenticationOption != null)
            this.authenticationOption = authenticationOption;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
    	int len = 3 + 8;	// size of protocol + algorithm + rdm + replayDetection
    	OpaqueData authInfo = authenticationOption.getAuthInfo();
    	if (authInfo != null) {
    		len += OpaqueDataUtil.getLength(authInfo);
    	}
    	return len;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        buf.put((byte)authenticationOption.getAlgorithm());
        buf.put((byte)authenticationOption.getProtocol());
        buf.put((byte)authenticationOption.getRdm());
        OpaqueData authInfo = authenticationOption.getAuthInfo();
        if (authInfo != null) {
        	OpaqueDataUtil.encodeDataOnly(buf, authInfo);
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
            int eof = buf.position() + len;
            if (buf.position() < eof) {
            	authenticationOption.setAlgorithm(Util.getUnsignedByte(buf));
                if (buf.position() < eof) {
                	authenticationOption.setProtocol(Util.getUnsignedByte(buf));
                    if (buf.position() < eof) {
                    	authenticationOption.setRdm(Util.getUnsignedByte(buf));
                    	if (buf.position() < eof) {
                    		byte[] replayDetection = new byte[8];
                    		buf.get(replayDetection);
                    		authenticationOption.setReplayDetection(new BigInteger(replayDetection));
                    		if (buf.position() < eof) {
	                    		OpaqueData authInfo = OpaqueData.Factory.newInstance();
	                    		OpaqueDataUtil.decodeDataOnly(authInfo, buf, len-8-3);
	                    		authenticationOption.setAuthInfo(authInfo);
                    		}
                    	}
                	}
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return authenticationOption.getCode();
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
        sb.append(authenticationOption.toString());
        return sb.toString();
    }
    
}
