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
 * <p>Title: BaseEmptyOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseEmptyOption extends BaseDhcpOption
{
    /**
     * Instantiates a new empty option.
     */
    public BaseEmptyOption()
    {
        super();
    }

    @Override
    public int getLength()
    {
        return 0;   // always zero bytes
    }

    @Override
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        return (ByteBuffer) buf.flip();
    }

    @Override
    public void decode(ByteBuffer buf) throws IOException
    {
    	super.decodeLength(buf);		// length is always zero
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(Util.LINE_SEPARATOR);
        sb.append(super.getName());
        return sb.toString();
    }
    
}
