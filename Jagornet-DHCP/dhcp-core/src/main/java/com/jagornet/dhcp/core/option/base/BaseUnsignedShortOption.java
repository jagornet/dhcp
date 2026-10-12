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
 * Title: BaseUnsignedShortOption
 * Description: The abstract base class for unsigned short DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseUnsignedShortOption extends BaseDhcpOption
{ 
    protected int unsignedShort;
    
    public BaseUnsignedShortOption() {
		this((int)0);
	}
    
    public BaseUnsignedShortOption(int unsignedShort) {
    	super();
		this.unsignedShort = unsignedShort;
	}

    public int getUnsignedShort() {
		return unsignedShort;
	}

	public void setUnsignedShort(int unsignedShort) {
		this.unsignedShort = unsignedShort;
	}

	@Override
    public int getLength()
    {
        return 2;   // always two bytes (short)
    }

	@Override
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        buf.putShort((short)unsignedShort);
        return (ByteBuffer) buf.flip();
    }

	@Override
    public void decode(ByteBuffer buf) throws IOException
    {
    	int len = super.decodeLength(buf);
    	if ((len > 0) && (len <= buf.remaining())) {
    		unsignedShort = Util.getUnsignedShort(buf);
        }
    }

	@Override
	public String toString()
    {
        StringBuilder sb = new StringBuilder(Util.LINE_SEPARATOR);
        sb.append(super.getName());
        sb.append(": unsignedShort=");
        sb.append(unsignedShort);
        return sb.toString();
    }
    
}
