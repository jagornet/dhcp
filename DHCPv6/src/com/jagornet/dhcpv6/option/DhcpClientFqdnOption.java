/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpClientFqdnOption.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.option.base.BaseDomainNameOption;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.ClientFqdnOption;

/**
 * <p>Title: DhcpClientFqdnOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpClientFqdnOption extends BaseDomainNameOption
{
	
	/**
	 * Instantiates a new dhcp client fqdn option.
	 */
	public DhcpClientFqdnOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp client fqdn option.
	 * 
	 * @param clientFqdnOption the client fqdn option
	 */
	public DhcpClientFqdnOption(ClientFqdnOption clientFqdnOption)
	{
		if (clientFqdnOption != null)
			this.domainNameOption = clientFqdnOption;
		else
			this.domainNameOption = ClientFqdnOption.Factory.newInstance();
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
        ClientFqdnOption clientFqdnOption = (ClientFqdnOption)domainNameOption;
        buf.put((byte)clientFqdnOption.getFlags());
        String domainName = clientFqdnOption.getDomainName();
        if (domainName != null) {
            encodeDomainName(buf, domainName);
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
                ClientFqdnOption clientFqdnOption = (ClientFqdnOption)domainNameOption;
                clientFqdnOption.setFlags(Util.getUnsignedByte(buf));
                String domain = decodeDomainName(buf, eof);
                clientFqdnOption.setDomainName(domain);
            }
    	}
    }
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return ((ClientFqdnOption)domainNameOption).getCode();
    }
    
    /**
     * Get the S bit.
     * 
     * @return the update aaaa bit
     */
    public boolean getUpdateAaaaBit()
    {
    	short sbit = (short) (((ClientFqdnOption)domainNameOption).getFlags() & 0x01);
    	return (sbit > 0);
    }
    
    /**
     * Set the S bit.
     * 
     * @param bit the bit
     */
    public void setUpdateAaaaBit(boolean bit)
    {
    	ClientFqdnOption me = (ClientFqdnOption)domainNameOption;
    	if (bit)
    		me.setFlags((short) (me.getFlags() | 0x01));
    	else
    		me.setFlags((short) (me.getFlags() & 0x06));
    }
    
    /**
     * Get the O bit.
     * 
     * @return the override bit
     */
    public boolean getOverrideBit()
    {
    	short obit = (short) (((ClientFqdnOption)domainNameOption).getFlags() & 0x02);
    	return (obit > 0);
    }
    
    /**
     * Set the O bit.
     * 
     * @param bit the bit
     */
    public void setOverrideBit(boolean bit)
    {
    	ClientFqdnOption me = (ClientFqdnOption)domainNameOption;
    	if (bit)
    		me.setFlags((short) (me.getFlags() | 0x02));
    	else
    		me.setFlags((short) (me.getFlags() & 0x05));
    }
    
    /**
     * Get the N bit.
     * 
     * @return the no update bit
     */
    public boolean getNoUpdateBit()
    {
    	short nbit = (short) (((ClientFqdnOption)domainNameOption).getFlags() & 0x04);
    	return (nbit == 1);
    }
    
    /**
     * Set the N bit.  If set to true, will also set the S bit to 0.
     * 
     * @param bit the bit
     */
    public void setNoUpdateBit(boolean bit)
    {
    	ClientFqdnOption me = (ClientFqdnOption)domainNameOption;
    	if (bit) {
    		me.setFlags((short) (me.getFlags() | 0x04));
    		// If the "N" bit is 1, the "S" bit MUST be 0.
    		setUpdateAaaaBit(false);
    	}
    	else {
    		me.setFlags((short) (me.getFlags() & 0x03));
    	}
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
        sb.append(((ClientFqdnOption)domainNameOption).toString());
        return sb.toString();
    }
}
