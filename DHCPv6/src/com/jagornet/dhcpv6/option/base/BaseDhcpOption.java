/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseDhcpOption.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.option.base;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.util.Util;

/**
 * Title: BaseDhcpOption
 * Description: The abstract base class for DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseDhcpOption implements DhcpOption
{	
	private static Logger log = LoggerFactory.getLogger(BaseDhcpOption.class);
	
	protected String name;
	protected boolean v4;	// true only if DHCPv4 option

	/**
     * Encode the DHCP option code and length fields of any DHCP option.
     * 
     * @return the ByteBuffer containing the encoded code and length fields
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected ByteBuffer encodeCodeAndLength() throws IOException
	{
    	ByteBuffer buf = null;
    	if (!v4) {
			buf = ByteBuffer.allocate(2 + 2 + getLength());
			buf.putShort((short)getCode());
			buf.putShort((short)getLength());
    	}
    	else {
    		buf = ByteBuffer.allocate(1 + 1 + getLength());
			buf.put((byte)getCode());
			buf.put((byte)getLength());
    	}
		return buf;
	}

    /**
     * Decode the DHCP option length field of any DHCP option.  Because we have a
     * DhcpOptionFactory to build the option based on the code, then the code is already
     * decoded, so this method is invoked by the concrete class to decode the length.
     * 
     * @param buf the ByteBuffer containing the opaqueData to be decoded
     * 
     * @return the length of the option, or zero if there is no opaqueData for the option
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
	protected int decodeLength(ByteBuffer buf) throws IOException
	{
        if ((buf != null) && buf.hasRemaining()) {
            // already have the code, so length is next
            int len = 0;
            if (!v4) {
            	len = Util.getUnsignedShort(buf);
            }
            else {
            	len = Util.getUnsignedByte(buf);
            }
            if (log.isDebugEnabled())
                log.debug(getName() + " reports length=" + len +
                          ":  bytes remaining in buffer=" + buf.remaining());
            return len;
        }
        return 0;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.option.DhcpOption#getName()
	 */
	public String getName()
	{
		if (name != null)
			return name;
		
		String className = this.getClass().getSimpleName();
		if (className.startsWith("Dhcp"))
			return className;
		
		return "Option-" + this.getCode(); 
	}

	public boolean isV4() {
		return v4;
	}

	public void setV4(boolean v4) {
		this.v4 = v4;
	}
	
}
