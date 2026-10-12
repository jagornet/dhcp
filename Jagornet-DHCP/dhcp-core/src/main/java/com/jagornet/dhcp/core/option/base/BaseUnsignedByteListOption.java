/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.base;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.jagornet.dhcp.core.util.Util;

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
