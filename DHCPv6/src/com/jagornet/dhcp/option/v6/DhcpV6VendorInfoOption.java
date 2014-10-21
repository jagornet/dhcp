/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6VendorInfoOption.java is part of DHCPv6.
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
package com.jagornet.dhcp.option.v6;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.jagornet.dhcp.option.base.BaseDhcpOption;
import com.jagornet.dhcp.option.base.DhcpOption;
import com.jagornet.dhcp.option.generic.GenericOpaqueDataOption;
import com.jagornet.dhcp.option.generic.GenericOptionFactory;
import com.jagornet.dhcp.util.DhcpConstants;
import com.jagornet.dhcp.util.Util;
import com.jagornet.dhcp.xml.GenericOptionsType;
import com.jagornet.dhcp.xml.OptionDefType;
import com.jagornet.dhcp.xml.V6VendorInfoOption;

/**
 * <p>Title: DhcpV6VendorInfoOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6VendorInfoOption extends BaseDhcpOption
{
	private long enterpriseNumber;	// long for unsigned int
	private List<DhcpOption> suboptionList;

    /**
     * Instantiates a new dhcp vendor info option.
     */
    public DhcpV6VendorInfoOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new dhcp vendor info option.
     * 
     * @param vendorInfoOption the vendor info option
     */
    public DhcpV6VendorInfoOption(V6VendorInfoOption vendorInfoOption)
    {
    	super();
    	if (vendorInfoOption != null) {
    		enterpriseNumber = vendorInfoOption.getEnterpriseNumber();
    		GenericOptionsType genericOptions = vendorInfoOption.getSuboptionList();
    		if (genericOptions != null) {
    			List<OptionDefType> optdefs = genericOptions.getOptionDefList();
    			if (optdefs != null) {
    				for (OptionDefType optdef : optdefs) {
						DhcpOption subopt = GenericOptionFactory.getDhcpOption(optdef);
						addSuboption(subopt);
					}
    			}
    		}
    	}
    	setCode(DhcpConstants.V6OPTION_VENDOR_OPTS);
    }
    
    public long getEnterpriseNumber() {
		return enterpriseNumber;
	}

	public void setEnterpriseNumber(long enterpriseNumber) {
		this.enterpriseNumber = enterpriseNumber;
	}

	public List<DhcpOption> getSuboptionList() {
		return suboptionList;
	}

	public void setSuboptionList(List<DhcpOption> suboptionList) {
		this.suboptionList = suboptionList;
	}
	
	public void addSuboption(DhcpOption suboption) {
		if (suboptionList == null) {
			suboptionList = new ArrayList<DhcpOption>();
		}
		suboptionList.add(suboption);
	}

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        int len = 4;  // size of enterprise number (int)
        if ((suboptionList != null) && !suboptionList.isEmpty()) {
            for (DhcpOption subopt : suboptionList) {
            	if (subopt != null) {
            		// code + len of suboption + suboption itself
            		len += 2 + 2 + subopt.getLength();	// patch from Audrey Zhdanov 9/22/11
            	}
            }
        }
        return len;
    }

	/* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        buf.putInt((int)enterpriseNumber);
        if ((suboptionList != null) && !suboptionList.isEmpty()) {
            for (DhcpOption subopt : suboptionList) {
        		buf.put(subopt.encode());
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
                enterpriseNumber = Util.getUnsignedInt(buf);
                while (buf.position() < eof) {
                	int code = Util.getUnsignedShort(buf);
                	GenericOpaqueDataOption subopt = new GenericOpaqueDataOption(code, null);
                	subopt.decode(buf);
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
        if ((suboptionList != null) && !suboptionList.isEmpty()) {
        	sb.append(" suboptions=");
            for (DhcpOption subopt : suboptionList) {
        		sb.append(subopt.toString());
        		sb.append(',');
            }
            sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }
}
