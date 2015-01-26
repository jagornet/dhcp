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
package com.jagornet.dhcp.option.v6;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import com.jagornet.dhcp.option.base.BaseOpaqueData;
import com.jagornet.dhcp.option.base.BaseOpaqueDataListOption;
import com.jagornet.dhcp.util.DhcpConstants;
import com.jagornet.dhcp.util.Util;
import com.jagornet.dhcp.xml.Operator;
import com.jagornet.dhcp.xml.V6VendorClassOption;

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
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp vendor class option.
	 * 
	 * @param vendorClassOption the vendor class option
	 */
	public DhcpV6VendorClassOption(V6VendorClassOption vendorClassOption)
	{
    	super(vendorClassOption);
    	if (vendorClassOption != null) {
    		enterpriseNumber = vendorClassOption.getEnterpriseNumber();
    	}
    	setCode(DhcpConstants.V6OPTION_VENDOR_CLASS);
	}
    
    public long getEnterpriseNumber() {
    	return enterpriseNumber;
    }
    
    public void setEnterpriseNumber(long enterpriseNumber) {
    	this.enterpriseNumber = enterpriseNumber;
    }
	
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        int len = 4 + super.getLength();  // size of enterprise number (int)
        return len;
    }
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
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

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Decodable#decode(java.nio.ByteBuffer)
     */
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(" enterpriseNumber=");
        sb.append(enterpriseNumber);
        return sb.toString();
    }

    public boolean matches(DhcpV6VendorClassOption that, Operator.Enum op)
    {
        if (that == null)
            return false;
        if (that.getCode() != this.getCode())
            return false;
        if (that.getEnterpriseNumber() != this.getEnterpriseNumber())
        	return false;

        return super.matches(that, op);
    }    
}
