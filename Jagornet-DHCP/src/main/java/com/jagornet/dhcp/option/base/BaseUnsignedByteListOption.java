/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseUnsignedByteListOption.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.option.base;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.jagornet.dhcp.util.Util;

/**
 * Title: BaseUnsignedByteListOption
 * Description: The abstract base class for unsigned short list DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseUnsignedByteListOption extends BaseDhcpOption
{
    protected List<Short> unsignedByteList;

    public BaseUnsignedByteListOption() {
		this(null);
	}

    public BaseUnsignedByteListOption(List<Short> unsignedBytes) {
    	super();
		this.unsignedByteList = unsignedBytes;
	}
    
    public List<Short> getUnsignedByteList() {
		return unsignedByteList;
	}

	public void setUnsignedByteList(List<Short> unsignedBytes) {
		this.unsignedByteList = unsignedBytes;
	}
	
	public void addUnsignedByte(short ubyte) {
		if (unsignedByteList == null) {
			unsignedByteList = new ArrayList<Short>();
		}
		unsignedByteList.add(ubyte);
	}

	@Override
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        if ((unsignedByteList != null) && !unsignedByteList.isEmpty()) {
            for (short ubyte : unsignedByteList) {
                buf.put((byte)ubyte);
            }
        }
        return (ByteBuffer) buf.flip();
    }
    
	@Override
    public void decode(ByteBuffer buf) throws IOException
    {
    	int len = super.decodeLength(buf);
    	if ((len > 0) && (len <= buf.remaining())) {
            for (int i=0; i<len; i++) {
                if (buf.hasRemaining()) {
                	addUnsignedByte(Util.getUnsignedByte(buf));
                }
            }
        }
    }

	@Override
    public int getLength()
    {
        int len = 0;
        if ((unsignedByteList != null) && !unsignedByteList.isEmpty()) {
            len = unsignedByteList.size();
        }
        return len;
    }

	@Override
	public String toString()
    {
        StringBuilder sb = new StringBuilder(Util.LINE_SEPARATOR);
        sb.append(super.getName());
        sb.append(": unsignedByteList=");
        if ((unsignedByteList != null) && !unsignedByteList.isEmpty()) {
        	for (Short ubyte : unsignedByteList) {
				sb.append(ubyte);
				sb.append(',');
			}
        	sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }
    
}
