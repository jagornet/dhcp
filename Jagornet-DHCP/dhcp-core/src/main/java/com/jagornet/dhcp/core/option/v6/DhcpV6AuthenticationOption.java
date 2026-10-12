/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.v6;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;

import com.jagornet.dhcp.core.option.base.BaseDhcpOption;
import com.jagornet.dhcp.core.option.base.BaseOpaqueData;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.core.util.Util;

/**
 * <p>Title: DhcpV6AuthenticationOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6AuthenticationOption extends BaseDhcpOption
{
	private short protocol;
	private short algorithm;
	private short rdm;
	private BigInteger replayDetection;
	private BaseOpaqueData authInfo;
    
    /**
     * Instantiates a new dhcp authentication option.
     */
    public DhcpV6AuthenticationOption()
    {
        this((short)0, (short)0, (short)0, BigInteger.ZERO, null);
    }
    
    /**
     * Instantiates a new dhcp authentication option.
     * 
     * @param authenticationOption the authentication option
     */
    public DhcpV6AuthenticationOption(short protocol, short algorithm, short rdm,
    								  BigInteger replayDetection, BaseOpaqueData authInfo)
    {
    	super();
    	this.setProtocol(protocol);
    	this.setAlgorithm(algorithm);
    	this.setRdm(rdm);
    	this.setReplayDetection(replayDetection);
    	this.setAuthInfo(authInfo);
    	setCode(DhcpConstants.V6OPTION_AUTH);
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

	@Override
    public int getLength()
    {
    	int len = 3 + 8;	// size of protocol + algorithm + rdm + replayDetection
    	if (authInfo != null) {
    		len += authInfo.getLength();
    	}
    	return len;
    }

	@Override
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

	@Override
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

	@Override
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
