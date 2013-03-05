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
import com.jagornet.dhcpv6.option.base.BaseOpaqueData;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.AuthenticationOption;

/**
 * <p>Title: DhcpAuthenticationOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpAuthenticationOption extends BaseDhcpOption
{
	private short protocol;
	private short algorithm;
	private short rdm;
	private BigInteger replayDetection;
	private BaseOpaqueData authInfo;
    
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
        if (authenticationOption != null) {
        	protocol = authenticationOption.getProtocol();
        	algorithm = authenticationOption.getAlgorithm();
        	rdm = authenticationOption.getRdm();
        	replayDetection = authenticationOption.getReplayDetection();
        	authInfo = new BaseOpaqueData(authenticationOption.getAuthInfo());
        }
        setCode(DhcpConstants.OPTION_AUTH);
    }

    public short getProtocol() {
		return protocol;
	}

	public void setProtocol(short protocol) {
		this.protocol = protocol;
	}

	public short getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(short algorithm) {
		this.algorithm = algorithm;
	}

	public short getRdm() {
		return rdm;
	}

	public void setRdm(short rdm) {
		this.rdm = rdm;
	}

	public BigInteger getReplayDetection() {
		return replayDetection;
	}

	public void setReplayDetection(BigInteger replayDetection) {
		this.replayDetection = replayDetection;
	}

	public BaseOpaqueData getAuthInfo() {
		return authInfo;
	}

	public void setAuthInfo(BaseOpaqueData authInfo) {
		this.authInfo = authInfo;
	}

	/* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
    	int len = 3 + 8;	// size of protocol + algorithm + rdm + replayDetection
    	if (authInfo != null) {
    		len += authInfo.getLength();
    	}
    	return len;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        buf.put((byte)algorithm);
        buf.put((byte)protocol);
        buf.put((byte)rdm);
        buf.put(replayDetection.toByteArray());
        if (authInfo != null) {
        	authInfo.encode(buf);
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
            	algorithm = Util.getUnsignedByte(buf);
                if (buf.position() < eof) {
                	protocol = Util.getUnsignedByte(buf);
                    if (buf.position() < eof) {
                    	rdm = Util.getUnsignedByte(buf);
                    	if (buf.position() < eof) {
                    		replayDetection = Util.getUnsignedLong(buf);
                    		if (buf.position() < eof) {
                    			authInfo.decode(buf, len-8-3);
                    		}
                    	}
                	}
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(Util.LINE_SEPARATOR);
        sb.append(super.getName());
        sb.append(": protocol=");
        sb.append(protocol);
        sb.append(" algorithm=");
        sb.append(algorithm);
        sb.append(" rdm=");
        sb.append(rdm);
        sb.append(" replayDetection=");
        sb.append(replayDetection);
        sb.append(" authInfo=");
        sb.append(authInfo);
        return sb.toString();
    }
    
}
