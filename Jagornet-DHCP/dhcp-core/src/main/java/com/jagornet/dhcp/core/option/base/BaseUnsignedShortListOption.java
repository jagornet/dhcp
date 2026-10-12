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
