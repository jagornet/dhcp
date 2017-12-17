/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseOpaqueDataListOption.java is part of Jagornet DHCP.
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
 * Title: BaseOpaqueDataListOption
 * Description: The abstract base class for opaque opaqueDataList DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseOpaqueDataListOption extends BaseDhcpOption
{
    private List<BaseOpaqueData> opaqueDataList;

    public BaseOpaqueDataListOption() {
        this(null);
    }
    
    public BaseOpaqueDataListOption(List<BaseOpaqueData> opaqueDataList) {
    	super();
		this.opaqueDataList = opaqueDataList;
	}
    
    public List<BaseOpaqueData> getOpaqueDataList() {
		return opaqueDataList;
	}

	public void setOpaqueDataList(List<BaseOpaqueData> opaqueDataList) {
		this.opaqueDataList = opaqueDataList;
	}
	
	public void addOpaqueData(BaseOpaqueData baseOpaque) {
		if (baseOpaque != null) {
			if (opaqueDataList == null) {
				opaqueDataList = new ArrayList<BaseOpaqueData>();
			}
			opaqueDataList.add(baseOpaque);
		}
	}

    public void addOpaqueData(String opaqueString) {
        if (opaqueString != null) {
        	BaseOpaqueData opaque = new BaseOpaqueData(opaqueString);
        	addOpaqueData(opaque);
        }
    }

    public void addOpaqueData(byte[] opaqueData)
    {
        if (opaqueData != null) {
        	BaseOpaqueData opaque = new BaseOpaqueData(opaqueData);
        	addOpaqueData(opaque);
        }
    }

	@Override
    public int getLength()
    {
        int len = 0;
        if ((opaqueDataList != null) && !opaqueDataList.isEmpty()) {
            for (BaseOpaqueData opaque : opaqueDataList) {
                len += 2 + opaque.getLength();
            }
        }
        return len;
    }
    
    @Override
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        if ((opaqueDataList != null) && !opaqueDataList.isEmpty()) {
            for (BaseOpaqueData opaque : opaqueDataList) {
            	opaque.encodeLengthAndData(buf);
            }
        }
        return (ByteBuffer) buf.flip();
    }

    @Override
	public void decode(ByteBuffer buf) throws IOException
    {
    	int len = super.decodeLength(buf);
    	if ((len > 0) && (len <= buf.remaining())) {
    		int eof = buf.position() + len;
            while (buf.position() < eof) {
            	BaseOpaqueData opaque = new BaseOpaqueData();
            	opaque.decodeLengthAndData(buf);
                addOpaqueData(opaque);
            }
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(Util.LINE_SEPARATOR);
        sb.append(super.getName());
        sb.append(": dataList=");
        if ((opaqueDataList != null) && !opaqueDataList.isEmpty()) {
        	for (BaseOpaqueData opaque : opaqueDataList) {
				sb.append(opaque.toString());
				sb.append(',');
			}
        	sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }

}
