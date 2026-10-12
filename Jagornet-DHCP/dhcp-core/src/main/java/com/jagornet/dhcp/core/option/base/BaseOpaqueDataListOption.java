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
