/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV4ClientFqdnOption.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.option.v4;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.jagornet.dhcpv6.option.base.BaseDomainNameOption;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.V4ClientFqdnOption;

/**
 * <p>Title: DhcpV4ClientFqdnOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4ClientFqdnOption extends BaseDomainNameOption
{
	/**
	 * From RFC 4702:
	 * 
	 * 2.1.  The Flags Field
	 *
	 *   The format of the 1-octet Flags field is:
	 *
	 *        0 1 2 3 4 5 6 7
	 *       +-+-+-+-+-+-+-+-+
	 *       |  MBZ  |N|E|O|S|
	 *       +-+-+-+-+-+-+-+-+
	 *       
	 * 2.2.  The RCODE Fields
	 * 
	 *    The two 1-octet RCODE1 and RCODE2 fields are deprecated.  A client
	 *    SHOULD set these to 0 when sending the option and SHOULD ignore them
	 *    on receipt.  A server SHOULD set these to 255 when sending the option
	 *    and MUST ignore them on receipt.
	 * 
	 */
	// need short to handle unsigned byte
	private short flags;
	private short rcode1;
	private short rcode2;
	
	/**
	 * Instantiates a new dhcp client fqdn option.
	 */
	public DhcpV4ClientFqdnOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp client fqdn option.
	 * 
	 * @param clientFqdnOption the client fqdn option
	 */
	public DhcpV4ClientFqdnOption(V4ClientFqdnOption clientFqdnOption)
	{
		super(clientFqdnOption);
		setCode(DhcpConstants.V4OPTION_CLIENT_FQDN);
		setV4(true);
	}
	
    public short getFlags() {
		return flags;
	}

	public void setFlags(short flags) {
		this.flags = flags;
	}

	public short getRcode1() {
		return rcode1;
	}

	public void setRcode1(short rcode1) {
		this.rcode1 = rcode1;
	}

	public short getRcode2() {
		return rcode2;
	}

	public void setRcode2(short rcode2) {
		this.rcode2 = rcode2;
	}

	/* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        int len = 3;	// size of flags (byte) + rcode1 (byte) + rcode2 (byte)
        if (getEncodingBit()) {
        	len += super.getLength();
        }
        else if (getDomainName() != null) {
        	// ASCII encoding, just add length of domain name string
        	len += getDomainName().length();
        }
        return len;
    }
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        buf.put((byte)getFlags());
        buf.put((byte)getRcode1());
        buf.put((byte)getRcode2());
        if (domainName != null) {
        	if (getEncodingBit()) {
        		encodeDomainName(buf, domainName);
        	}
        	else {
        		// ASCII encoding, just append the domain name string
        		buf.put(domainName.getBytes());
        	}
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
                setFlags(Util.getUnsignedByte(buf));
                setRcode1(Util.getUnsignedByte(buf));
                setRcode2(Util.getUnsignedByte(buf));
                String domain = null;
                if (getEncodingBit()) {
                	domain = decodeDomainName(buf, eof);
                }
                else {
                	// ASCII encoding (deprecated, but used by Microsoft)
                	byte[] b = new byte[len-3];
                	buf.get(b);
                	domain = new String(b);
                }
                setDomainName(domain);
            }
    	}
    }
    
    /**
     * Get the S bit.
     * 
     * @return the update aaaa bit
     */
    public boolean getUpdateABit()
    {
    	short sbit = (short) (getFlags() & 0x01);
    	return (sbit > 0);
    }
    
    /**
     * Set the S bit.
     * 
     * @param bit the bit
     */
    public void setUpdateABit(boolean bit)
    {
    	if (bit)
    		setFlags((short) (getFlags() | 0x01));	// 0001
    	else
    		setFlags((short) (getFlags() & 0x0e));	// 1110
    }
    
    /**
     * Get the O bit.
     * 
     * @return the override bit
     */
    public boolean getOverrideBit()
    {
    	short obit = (short) (getFlags() & 0x02);
    	return (obit > 0);
    }
    
    /**
     * Set the O bit.
     * 
     * @param bit the bit
     */
    public void setOverrideBit(boolean bit)
    {
    	if (bit)
    		setFlags((short) (getFlags() | 0x02));	// 0010
    	else
    		setFlags((short) (getFlags() & 0x0d));	// 1101
    }
    
    /**
     * Get the E bit.
     * 
     * @return the encoding bit
     */
    public boolean getEncodingBit()
    {
    	short obit = (short) (getFlags() & 0x04);
    	return (obit > 0);
    }
    
    /**
     * Set the E bit.
     * 
     * @param bit the bit
     */
    public void setEncodingBit(boolean bit)
    {
    	if (bit)
    		setFlags((short) (getFlags() | 0x04));	// 0100
    	else
    		setFlags((short) (getFlags() & 0x0b));	// 1011
    }
    
    /**
     * Get the N bit.
     * 
     * @return the no update bit
     */
    public boolean getNoUpdateBit()
    {
    	short nbit = (short) (getFlags() & 0x08);
    	return (nbit == 1);
    }
    
    /**
     * Set the N bit.  If set to true, will also set the S bit to 0.
     * 
     * @param bit the bit
     */
    public void setNoUpdateBit(boolean bit)
    {
    	if (bit) {
    		setFlags((short) (getFlags() | 0x08));	// 1000
    		// If the "N" bit is 1, the "S" bit MUST be 0.
    		setUpdateABit(false);
    	}
    	else {
    		setFlags((short) (getFlags() & 0x07));	// 0111
    	}
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(" flags=");
        sb.append(flags);
        sb.append(" rcode1=");
        sb.append(rcode1);
        sb.append(" rcode2=");
        sb.append(rcode2);
        return sb.toString();
    }
}
