/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpVendorClassOption.java is part of DHCPv6.
 *
 *   DHCPv6 is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   DHCPv6 is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with DHCPv6.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcpv6.option;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.mina.core.buffer.IoBuffer;

import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.OptionExpression;
import com.jagornet.dhcpv6.xml.VendorClassOption;

/**
 * <p>Title: DhcpVendorClassOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpVendorClassOption extends BaseDhcpOption implements DhcpComparableOption
{
    /** The vendor class option. */
    private VendorClassOption vendorClassOption;

    /**
     * Instantiates a new dhcp vendor class option.
     */
    public DhcpVendorClassOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new dhcp vendor class option.
     * 
     * @param vendorClassOption the vendor class option
     */
    public DhcpVendorClassOption(VendorClassOption vendorClassOption)
    {
        super();
        if (vendorClassOption != null)
            this.vendorClassOption = vendorClassOption;
        else
            this.vendorClassOption = VendorClassOption.Factory.newInstance();
    }

    /**
     * Gets the vendor class option.
     * 
     * @return the vendor class option
     */
    public VendorClassOption getVendorClassOption()
    {
        return vendorClassOption;
    }

    /**
     * Sets the vendor class option.
     * 
     * @param vendorClassOption the new vendor class option
     */
    public void setVendorClassOption(VendorClassOption vendorClassOption)
    {
        if (vendorClassOption != null)
            this.vendorClassOption = vendorClassOption;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        int len = 4;  // size of enterprise number (int)
        List<OpaqueData> vendorClasses = vendorClassOption.getVendorClassesList();
        if ((vendorClasses != null) && !vendorClasses.isEmpty()) {
            for (OpaqueData opaque : vendorClasses) {
                len += 2;   // opaque data length
                len += OpaqueDataUtil.getLength(opaque);
            }
        }
        return len;
    }
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        iobuf.putInt((int)vendorClassOption.getEnterpriseNumber());
        List<OpaqueData> vendorClasses = vendorClassOption.getVendorClassesList();
        if ((vendorClasses != null) && !vendorClasses.isEmpty()) {
            for (OpaqueData opaque : vendorClasses) {
                OpaqueDataUtil.encode(iobuf, opaque);
            }
        }
        return iobuf.flip().buf();        
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Decodable#decode(java.nio.ByteBuffer)
     */
    public void decode(ByteBuffer buf) throws IOException
    {
    	IoBuffer iobuf = IoBuffer.wrap(buf);
    	int len = super.decodeLength(iobuf);
    	if ((len > 0) && (len <= iobuf.remaining())) {
            int eof = iobuf.position() + len;
            if (iobuf.position() < eof) {
                vendorClassOption.setEnterpriseNumber(iobuf.getUnsignedInt());
                while (iobuf.position() < eof) {
                    OpaqueData opaque = OpaqueDataUtil.decode(iobuf);
                    this.addVendorClass(opaque);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpComparableOption#matches(com.jagornet.dhcpv6.xml.OptionExpression)
     */
    public boolean matches(OptionExpression expression)
    {
        if (expression == null)
            return false;
        if (expression.getCode() != this.getCode())
            return false;

        List<OpaqueData> vendorClasses = vendorClassOption.getVendorClassesList();
        if ((vendorClasses != null) && !vendorClasses.isEmpty()) {
            for (OpaqueData opaque : vendorClasses) {
                if (OpaqueDataUtil.matches(expression, opaque)) {
                    // if one of the vendor classes matches the
                    // expression, then consider it a match
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Adds the vendor class.
     * 
     * @param vendorclass the vendorclass
     */
    public void addVendorClass(String vendorclass)
    {
        if (vendorclass != null) {
            OpaqueData opaque = OpaqueData.Factory.newInstance();
            opaque.setAsciiValue(vendorclass);
            this.addVendorClass(opaque);
        }
    }

    /**
     * Adds the vendor class.
     * 
     * @param vendorclass the vendorclass
     */
    public void addVendorClass(byte[] vendorclass)
    {
        if (vendorclass != null) {
            OpaqueData opaque = OpaqueData.Factory.newInstance();
            opaque.setHexValue(vendorclass);
            this.addVendorClass(opaque);
        }
    }

    /**
     * Adds the vendor class.
     * 
     * @param opaque the opaque
     */
    public void addVendorClass(OpaqueData opaque)
    {
        vendorClassOption.addNewVendorClasses().set(opaque);        
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return vendorClassOption.getCode();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.getName());
        sb.append(": ");
        sb.append("Enterprise Number=");
        sb.append(vendorClassOption.getEnterpriseNumber());
        sb.append(" ");
        List<OpaqueData> vendorClasses = vendorClassOption.getVendorClassesList();
        if ((vendorClasses != null) && !vendorClasses.isEmpty()) {
            for (OpaqueData opaque : vendorClasses) {
                sb.append(OpaqueDataUtil.toString(opaque));
                sb.append(",");
            }
            sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }
}
