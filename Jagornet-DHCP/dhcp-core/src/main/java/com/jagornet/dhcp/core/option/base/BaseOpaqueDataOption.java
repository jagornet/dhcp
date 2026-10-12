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
import java.util.Arrays;

import com.jagornet.dhcp.core.util.Util;

/**
 * Title: BaseOpaqueDataOption
 * Description: The abstract base class for opaque opaqueData DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseOpaqueDataOption extends BaseDhcpOption
{
	protected BaseOpaqueData opaqueData;
	
	public BaseOpaqueDataOption() {
		this(null);
	}

	public BaseOpaqueDataOption(BaseOpaqueData opaqueData) {
		super();
		if (opaqueData != null) {
			this.opaqueData = opaqueData;
		}
		else {
			this.opaqueData = new BaseOpaqueData();
		}
	}
	
	public BaseOpaqueData getOpaqueData() {
		return opaqueData;
	}

	public void setOpaqueData(BaseOpaqueData opaqueData) {
		this.opaqueData = opaqueData;
	}

	@Override
	public int getLength()
    {
        return opaqueData.getLength();
    }

	@Override
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        opaqueData.encode(buf);        
        return (ByteBuffer) buf.flip();
    }

	@Override
    public void decode(ByteBuffer buf) throws IOException
    {
    	int len = super.decodeLength(buf); 
    	if ((len > 0) && (len <= buf.remaining())) {
            int eof = buf.position() + len;
            if (buf.position() < eof) {
            	opaqueData.decode(buf, len);
            }
        }
    }

	@Override
    public boolean equals(Object obj)
    {
		/* 
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		// if (getClass() != obj.getClass())
		// 	return false;
        if (obj instanceof BaseOpaqueDataOption) {
        	BaseOpaqueDataOption that = (BaseOpaqueDataOption) obj;
        	if (that.opaqueData != null) {
	            if (opaqueData.getAscii() != null) {
	            	
	                return opaqueData.getAscii().equalsIgnoreCase(that.opaqueData.getAscii());
	            }
	            else {
	            	return Arrays.equals(opaqueData.getHex(), that.opaqueData.getHex());
	            }
        	}
        }
        return false;
		*/
		return super.equals(obj);
    }

	@Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(Util.LINE_SEPARATOR);
        sb.append(super.getName());
        sb.append(": data=");
        if(opaqueData != null)
        	sb.append(opaqueData.toString());
        return sb.toString();
    }
}
