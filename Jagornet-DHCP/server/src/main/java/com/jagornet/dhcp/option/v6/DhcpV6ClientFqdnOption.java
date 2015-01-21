/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6ClientFqdnOption.java is part of Jagornet DHCP.
 *
 *   Jagornet DHCP is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Jagornet DHCP is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Jagornet DHCP.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcp.option.v6;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.jagornet.dhcp.option.base.BaseDomainNameOption;
import com.jagornet.dhcp.util.DhcpConstants;
import com.jagornet.dhcp.util.Util;
import com.jagornet.dhcp.xml.V6ClientFqdnOption;

/**
 * <p>Title: DhcpV6ClientFqdnOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6ClientFqdnOption extends BaseDomainNameOption
{
	/**
	 * From RFC 4704:
	 * 
	 * 4.1.  The Flags Field
	 * 
	 *   The format of the Flags field is:
	 *        0 1 2 3 4 5 6 7
	 *       +-+-+-+-+-+-+-+-+
	 *       |  MBZ    |N|O|S|
	 *       +-+-+-+-+-+-+-+-+
	 * 
	 */
	// need short to handle unsigned byte
	private short flags;
	
	/**
	 * Instantiates a new dhcp client fqdn option.
	 */
	public DhcpV6ClientFqdnOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp client fqdn option.
	 * 
	 * @param clientFqdnOption the client fqdn option
	 */
	public DhcpV6ClientFqdnOption(V6ClientFqdnOption clientFqdnOption)
	{
		super(clientFqdnOption);
		setCode(DhcpConstants.V6OPTION_CLIENT_FQDN);
	}
	
    public short getFlags() {
		return flags;
	}

	public void setFlags(short flags) {
		this.flags = flags;
	}

	/* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        int len = 1 + super.getLength();  // size of flags (byte)
        return len;
    }
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        buf.put((byte)getFlags());
        if (getDomainName() != null) {
            encodeDomainName(buf, getDomainName());
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
                String domain = decodeDomainName(buf, eof);
                setDomainName(domain);
            }
    	}
    }
    
    /**
     * Get the S bit.
     * 
     * @return the update aaaa bit
     */
    public boolean getUpdateAaaaBit()
    {
    	short sbit = (short) (getFlags() & 0x01);
    	return (sbit > 0);
    }
    
    /**
     * Set the S bit.
     * 
     * @param bit the bit
     */
    public void setUpdateAaaaBit(boolean bit)
    {
    	if (bit)
    		setFlags((short) (getFlags() | 0x01));	// 0001
    	else
    		setFlags((short) (getFlags() & 0x06));	// 0110
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
    		setFlags((short) (getFlags() & 0x05));	// 0101
    }
    
    /**
     * Get the N bit.
     * 
     * @return the no update bit
     */
    public boolean getNoUpdateBit()
    {
    	short nbit = (short) (getFlags() & 0x04);
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
    		setFlags((short) (getFlags() | 0x04));	// 0100
    		// If the "N" bit is 1, the "S" bit MUST be 0.
    		setUpdateAaaaBit(false);
    	}
    	else {
    		setFlags((short) (getFlags() & 0x03));	// 0011
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
        return sb.toString();
    }
}
