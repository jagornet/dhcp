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
 * Title: BaseUnsignedIntOption
 * Description: The abstract base class for unsigned integer DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseUnsignedIntOption extends BaseDhcpOption
{ 
    protected long unsignedInt;
    
    public BaseUnsignedIntOption() {
		this((long)0);
	}
    
    public BaseUnsignedIntOption(long unsignedInt) {
    	super();
		this.unsignedInt = unsignedInt;
	}

    public long getUnsignedInt() {
		return unsignedInt;
	}

	public void setUnsignedInt(long unsignedInt) {
		this.unsignedInt = unsignedInt;
	}

	@Override
    public int getLength()
    {
        return 4;   // always four bytes (int)
    }

	@Override
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        buf.putInt((int)unsignedInt);
        return (ByteBuffer) buf.flip();
    }

	@Override
    public void decode(ByteBuffer buf) throws IOException
    {
    	int len = super.decodeLength(buf); 
    	if ((len > 0) && (len <= buf.remaining())) {
    		unsignedInt = Util.getUnsignedInt(buf);
        }
    }

	@Override
	public String toString()
    {
        StringBuilder sb = new StringBuilder(Util.LINE_SEPARATOR);
        sb.append(super.getName());
        sb.append(": unsignedInt=");
        sb.append(unsignedInt);
        return sb.toString();
    }
}
