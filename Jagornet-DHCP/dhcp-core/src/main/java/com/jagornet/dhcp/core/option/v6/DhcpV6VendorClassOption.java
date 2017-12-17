/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6VendorClassOption.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.core.option.v6;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import com.jagornet.dhcp.core.option.base.BaseOpaqueData;
import com.jagornet.dhcp.core.option.base.BaseOpaqueDataListOption;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.core.util.Util;

/**
 * <p>Title: DhcpV6VendorClassOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6VendorClassOption extends BaseOpaqueDataListOption
{
	private long enterpriseNumber;	// long for unsigned int
	
	/**
	 * Instantiates a new dhcp vendor class option.
	 */
	public DhcpV6VendorClassOption()
	{
		this((long)0, null);
	}
	
	/**
	 * Instantiates a new dhcp vendor class option.
	 * 
	 * @param vendorClassOption the vendor class option
	 */
	public DhcpV6VendorClassOption(long enterpriseNumber, List<BaseOpaqueData> baseOpaqueDataList)
	{
    	super(baseOpaqueDataList);
    	setEnterpriseNumber(enterpriseNumber);
    	setCode(DhcpConstants.V6OPTION_VENDOR_CLASS);
	}
    
    public long getEnterpriseNumber() {
    	return enterpriseNumber;
    }
    
    public void setEnterpriseNumber(long enterpriseNumber) {
    	this.enterpriseNumber = enterpriseNumber;
    }
	
    @Override
    public int getLength()
    {
        int len = 4 + super.getLength();  // size of enterprise number (int)
        return len;
    }
    
    @Override
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        buf.putInt((int)getEnterpriseNumber());
        List<BaseOpaqueData> vendorClasses = getOpaqueDataList();
        if ((vendorClasses != null) && !vendorClasses.isEmpty()) {
            for (BaseOpaqueData opaque : vendorClasses) {
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
            if (buf.position() < eof) {
                setEnterpriseNumber(Util.getUnsignedInt(buf));
                while (buf.position() < eof) {
    				BaseOpaqueData opaque = new BaseOpaqueData();
                    opaque.decodeLengthAndData(buf);
                    addOpaqueData(opaque);
                }
            }
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(" enterpriseNumber=");
        sb.append(enterpriseNumber);
        return sb.toString();
    }

}
