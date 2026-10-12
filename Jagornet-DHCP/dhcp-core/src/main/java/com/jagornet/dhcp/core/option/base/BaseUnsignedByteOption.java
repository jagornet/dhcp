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

import com.jagornet.dhcp.core.util.Util;

/**
 * Title: BaseUnsignedByteOption
 * Description: The abstract base class for unsigned byte DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseUnsignedByteOption extends BaseDhcpOption
{
    protected short unsignedByte;
    
    public BaseUnsignedByteOption() {
		this((short)0);
	}
    
    public BaseUnsignedByteOption(short unsignedByte) {
    	super();
		this.unsignedByte = unsignedByte;
	}

    public short getUnsignedByte() {
		return unsignedByte;
	}

	public void setUnsignedByte(short unsignedByte) {
		this.unsignedByte = unsignedByte;
	}

	@Override
    public int getLength()
    {
        return 1;   // always one byte
    }

	@Override
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        buf.put((byte)unsignedByte);
        return (ByteBuffer) buf.flip();
    }

	@Override
    public void decode(ByteBuffer buf) throws IOException
    {
    	int len = super.decodeLength(buf);
    	if ((len > 0) && (len <= buf.remaining())) {
    		unsignedByte = Util.getUnsignedByte(buf);
        }
    }

	@Override
	public String toString()
    {
        StringBuilder sb = new StringBuilder(Util.LINE_SEPARATOR);
        sb.append(super.getName());
        sb.append(": unsignedByte=");
        sb.append(unsignedByte);
        return sb.toString();
    }
    
}
