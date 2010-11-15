/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseOpaqueDataOption.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.option.base;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.jagornet.dhcpv6.option.DhcpComparableOption;
import com.jagornet.dhcpv6.option.OpaqueDataUtil;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.OpaqueDataOptionType;
import com.jagornet.dhcpv6.xml.OptionExpression;

/**
 * Title: BaseOpaqueDataOption
 * Description: The abstract base class for opaque opaqueData DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseOpaqueDataOption extends BaseDhcpOption implements DhcpComparableOption
{
	protected OpaqueDataOptionType opaqueDataOption;

    /**
     * Instantiates a new opaque opaqueData option.
     */
    public BaseOpaqueDataOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new opaque opaqueData option.
     * 
     * @param opaqueDataOption the opaque opaqueData option
     */
    public BaseOpaqueDataOption(OpaqueDataOptionType opaqueDataOption)
    {
        super();
        if (opaqueDataOption != null) {
            this.opaqueDataOption = opaqueDataOption;
        }
        else {
            this.opaqueDataOption = OpaqueDataOptionType.Factory.newInstance();
            // create an OpaqueData element to actually hold the data
            this.opaqueDataOption.addNewOpaqueData();
        }
    }

    /**
     * Gets the opaque opaqueData option.
     * 
     * @return the opaque opaqueData option
     */
    public OpaqueDataOptionType getOpaqueDataOptionType()
    {
        return opaqueDataOption;
    }

    /**
     * Sets the opaque opaqueData option.
     * 
     * @param opaqueDataOption the new opaque opaqueData option
     */
    public void setOpaqueDataOptionType(OpaqueDataOptionType opaqueDataOption)
    {
        if (opaqueDataOption != null)
            this.opaqueDataOption = opaqueDataOption;
    }

	public int getLength()
    {
        return OpaqueDataUtil.getLengthDataOnly(opaqueDataOption.getOpaqueData());
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        OpaqueDataUtil.encodeDataOnly(buf, opaqueDataOption.getOpaqueData());
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
            while (buf.position() < eof) {
//            	OpaqueData opaque = opaqueDataOption.addNewOpaqueData();
//                OpaqueDataUtil.decodeDataOnly(opaque, buf, len);
            	OpaqueDataUtil.decodeDataOnly(opaqueDataOption.getOpaqueData(), buf, len);
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
        if (opaqueDataOption == null)
        	return false;

        return OpaqueDataUtil.matches(expression, opaqueDataOption.getOpaqueData());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
            return OpaqueDataUtil.equals(this.getOpaqueDataOptionType().getOpaqueData(), 
                                         that.getOpaqueDataOptionType().getOpaqueData());
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(Util.LINE_SEPARATOR);
        sb.append(super.getName());
        sb.append(Util.LINE_SEPARATOR);
        // use XmlObject implementation
        sb.append(opaqueDataOption.toString());
        return sb.toString();
    }
}
