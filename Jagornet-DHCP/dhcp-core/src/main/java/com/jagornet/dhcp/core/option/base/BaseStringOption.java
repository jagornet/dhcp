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
 * Title: BaseStringOption
 * Description: The abstract base class for string DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseStringOption extends BaseDhcpOption
{
	protected String string;
	
	public BaseStringOption() {
		this(null);
	}
	
	public BaseStringOption(String string) {
		this.string = string;
	}
		
    public String getString() {
		return string;
	}

	public void setString(String string)
	{
		this.string = string;
	}

	@Override
    public int getLength()
    {
        int len = 0;
        if (string != null) {
            len = string.length();
        }
        return len;
    }

	@Override
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        if (string != null) {
            buf.put(string.getBytes());
        }
        return (ByteBuffer) buf.flip();
    }

	@Override
    public void decode(ByteBuffer buf) throws IOException
    {
    	int len = super.decodeLength(buf);
    	if ((len > 0) && (len <= buf.remaining())) {
            byte[] b = new byte[len];
            buf.get(b);
            string = new String(b);
        }
    }

	@Override
	public String toString()
    {
        StringBuilder sb = new StringBuilder(Util.LINE_SEPARATOR);
        sb.append(super.getName());
        sb.append(": string=");
        sb.append(string);
        return sb.toString();
    }

}
