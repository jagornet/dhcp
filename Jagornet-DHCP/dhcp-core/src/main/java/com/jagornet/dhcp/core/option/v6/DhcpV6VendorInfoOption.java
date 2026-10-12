/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.v6;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.jagornet.dhcp.core.option.base.BaseDhcpOption;
import com.jagornet.dhcp.core.option.base.DhcpOption;
import com.jagornet.dhcp.core.option.generic.GenericOpaqueDataOption;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.core.util.Util;

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

    public DhcpV6VendorInfoOption()
    {
        this((long)0, null);
    }
    
    public DhcpV6VendorInfoOption(long enterpriseNumber, List<DhcpOption> suboptionList)
    {
    	super();
    	this.enterpriseNumber = enterpriseNumber;
    	this.suboptionList = suboptionList;
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
