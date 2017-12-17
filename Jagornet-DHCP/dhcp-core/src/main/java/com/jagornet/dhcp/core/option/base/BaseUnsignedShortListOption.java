/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseUnsignedShortListOption.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.core.option.base;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.jagornet.dhcp.core.util.Util;

/**
 * Title: BaseUnsignedShortListOption
 * Description: The abstract base class for unsigned short list DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseUnsignedShortListOption extends BaseDhcpOption
{
    protected List<Integer> unsignedShortList;
    
    public BaseUnsignedShortListOption() {
		this(null);
	}
    
    public BaseUnsignedShortListOption(List<Integer> unsignedShorts) {
    	super();
		this.unsignedShortList = unsignedShorts;
	}

    public List<Integer> getUnsignedShortList() {
		return unsignedShortList;
	}

	public void setUnsignedShortList(List<Integer> unsignedShorts) {
		this.unsignedShortList = unsignedShorts;
	}
	
	public void addUnsignedShort(int unsignedShort) {
		if (unsignedShortList == null) {
			unsignedShortList = new ArrayList<Integer>();
		}
		unsignedShortList.add(unsignedShort);
	}

	@Override
    public int getLength()
    {
        int len = 0;
        if ((unsignedShortList != null) && !unsignedShortList.isEmpty()) {
            len = unsignedShortList.size() * 2;
        }
        return len;
    }

	@Override
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        if ((unsignedShortList != null) && !unsignedShortList.isEmpty()) {
            for (int ushort : unsignedShortList) {
                buf.putShort((short)ushort);
            }
        }
        return (ByteBuffer) buf.flip();
    }
    
	@Override
    public void decode(ByteBuffer buf) throws IOException
    {
    	int len = super.decodeLength(buf);
    	if ((len > 0) && (len <= buf.remaining())) {
            for (int i=0; i<len/2; i++) {
                if (buf.hasRemaining()) {
                	addUnsignedShort(Util.getUnsignedShort(buf));
                }
            }
        }
    }

	@Override
	public String toString()
    {
        StringBuilder sb = new StringBuilder(Util.LINE_SEPARATOR);
        sb.append(super.getName());
        sb.append(": unsignedShortList=");
        if ((unsignedShortList != null) && !unsignedShortList.isEmpty()) {
        	for (Integer ushort : unsignedShortList) {
				sb.append(ushort);
				sb.append(',');
			}
        	sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }
    
}
