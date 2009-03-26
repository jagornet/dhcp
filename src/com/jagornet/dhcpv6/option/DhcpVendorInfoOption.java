/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpVendorInfoOption.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.option.base.BaseDhcpOption;
import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.option.generic.GenericOpaqueDataOption;
import com.jagornet.dhcpv6.option.generic.GenericOptionFactory;
import com.jagornet.dhcpv6.xml.GenericOptionsType;
import com.jagornet.dhcpv6.xml.OptionDefType;
import com.jagornet.dhcpv6.xml.VendorInfoOption;

/**
 * <p>Title: DhcpVendorClassOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpVendorInfoOption extends BaseDhcpOption
{
    /** The vendor info option. */
    private VendorInfoOption vendorInfoOption;

    /**
     * Instantiates a new dhcp vendor info option.
     */
    public DhcpVendorInfoOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new dhcp vendor info option.
     * 
     * @param vendorInfoOption the vendor info option
     */
    public DhcpVendorInfoOption(VendorInfoOption vendorInfoOption)
    {
        super();
        if (vendorInfoOption != null)
            this.vendorInfoOption = vendorInfoOption;
        else
            this.vendorInfoOption = VendorInfoOption.Factory.newInstance();
    }

    /**
     * Gets the vendor info option.
     * 
     * @return the vendor info option
     */
    public VendorInfoOption getVendorInfoOption()
    {
        return vendorInfoOption;
    }

    /**
     * Sets the vendor info option.
     * 
     * @param vendorInfoOption the new vendor info option
     */
    public void setVendorInfoOption(VendorInfoOption vendorInfoOption)
    {
        if (vendorInfoOption != null)
            this.vendorInfoOption = vendorInfoOption;
    }
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        iobuf.putInt((int)vendorInfoOption.getEnterpriseNumber());
        GenericOptionsType suboptList = vendorInfoOption.getSuboptionList();
        if (suboptList != null) {
        	List<OptionDefType> subopts = suboptList.getOptionDefList();
	        if ((subopts != null) && !subopts.isEmpty()) {
	            for (OptionDefType subopt : subopts) {
	            	DhcpOption genericOption = GenericOptionFactory.getDhcpOption(subopt);
	            	if (genericOption != null) {
	            		iobuf.put(genericOption.encode());
	            	}
	            }
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
                vendorInfoOption.setEnterpriseNumber(iobuf.getUnsignedInt());
            	GenericOptionsType suboptList = vendorInfoOption.addNewSuboptionList();
                while (iobuf.position() < eof) {
                	int code = iobuf.getUnsignedShort();
                	GenericOpaqueDataOption subopt = new GenericOpaqueDataOption(code, null);
                	subopt.decode(iobuf.buf());
                	OptionDefType optionDef = suboptList.addNewOptionDef();
                	optionDef.setOpaqueDataOption(subopt.getOpaqueDataOptionType());
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        int len = 4;  // size of enterprise number (int)
        GenericOptionsType suboptList = vendorInfoOption.getSuboptionList();
        if (suboptList != null) {
        	List<OptionDefType> subopts = suboptList.getOptionDefList();
	        if ((subopts != null) && !subopts.isEmpty()) {
	            for (OptionDefType subopt : subopts) {
	            	DhcpOption genericOption = GenericOptionFactory.getDhcpOption(subopt);
	            	if (genericOption != null) {
	            		len += genericOption.getLength();
	            	}
	            }
	        }
        }
        return len;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return vendorInfoOption.getCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.getName());
        sb.append(": ");
        sb.append("Enterprise Number=");
        sb.append(vendorInfoOption.getEnterpriseNumber());
        sb.append(" ");
        GenericOptionsType suboptList = vendorInfoOption.getSuboptionList();
        if (suboptList != null) {
        	List<OptionDefType> subopts = suboptList.getOptionDefList();
	        if ((subopts != null) && !subopts.isEmpty()) {
	            sb.append("Suboptions: ");
	            for (OptionDefType subopt : subopts) {
	            	DhcpOption genericOption = GenericOptionFactory.getDhcpOption(subopt);
	            	if (genericOption != null) {
		            	sb.append(genericOption.toString());
	            	}
	            	sb.append(",");
	            }
	            sb.setLength(sb.length()-1);
	        }
        }
        return sb.toString();
    }
}
