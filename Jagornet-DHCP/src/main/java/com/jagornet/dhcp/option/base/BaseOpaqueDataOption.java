/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseOpaqueDataOption.java is part of Jagornet DHCP.
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
import java.util.Arrays;

import com.jagornet.dhcp.util.Util;

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
		this.opaqueData = opaqueData;
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
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
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
