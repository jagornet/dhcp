/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpUserClassOption.java is part of DHCPv6.
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
import com.jagornet.dhcpv6.xml.UserClassOption;

/**
 * <p>Title: DhcpClientIdOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpUserClassOption extends BaseDhcpOption implements DhcpComparableOption
{
    /** The user class option. */
    private UserClassOption userClassOption;

    /**
     * Instantiates a new dhcp user class option.
     */
    public DhcpUserClassOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new dhcp user class option.
     * 
     * @param userClassOption the user class option
     */
    public DhcpUserClassOption(UserClassOption userClassOption)
    {
        super();
        if (userClassOption != null)
            this.userClassOption = userClassOption;
        else
            this.userClassOption = UserClassOption.Factory.newInstance();
    }

    /**
     * Gets the user class option.
     * 
     * @return the user class option
     */
    public UserClassOption getUserClassOption()
    {
        return userClassOption;
    }

    /**
     * Sets the user class option.
     * 
     * @param userClassOption the new user class option
     */
    public void setUserClassOption(UserClassOption userClassOption)
    {
        if (userClassOption != null)
            this.userClassOption = userClassOption;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        int len = 0;
        List<OpaqueData> userClasses = userClassOption.getUserClassesList();
        if ((userClasses != null) && !userClasses.isEmpty()) {
            for (OpaqueData opaque : userClasses) {
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
        List<OpaqueData> userClasses = userClassOption.getUserClassesList();
        if ((userClasses != null) && !userClasses.isEmpty()) {
            for (OpaqueData opaque : userClasses) {
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
            while (iobuf.position() < eof) {
                OpaqueData opaque = OpaqueDataUtil.decode(iobuf);
                this.addUserClass(opaque);
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

        List<OpaqueData> userClasses = userClassOption.getUserClassesList();
        if ((userClasses != null) && !userClasses.isEmpty()) {
            for (OpaqueData opaque : userClasses) {
                if (OpaqueDataUtil.matches(expression, opaque)) {
                    // if one of the user classes matches the
                    // expression, then consider it a match
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Adds the user class.
     * 
     * @param userclass the userclass
     */
    public void addUserClass(String userclass)
    {
        if (userclass != null) {
            OpaqueData opaque = OpaqueData.Factory.newInstance();
            opaque.setAsciiValue(userclass);
            this.addUserClass(opaque);
        }
    }

    /**
     * Adds the user class.
     * 
     * @param userclass the userclass
     */
    public void addUserClass(byte[] userclass)
    {
        if (userclass != null) {
            OpaqueData opaque = OpaqueData.Factory.newInstance();
            opaque.setHexValue(userclass);
            this.addUserClass(opaque);
        }
    }
    
    /**
     * Adds the user class.
     * 
     * @param opaque the opaque
     */
    public void addUserClass(OpaqueData opaque)
    {
        userClassOption.addNewUserClasses().set(opaque);
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return userClassOption.getCode();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        if (userClassOption == null)
            return null;
        
        StringBuilder sb = new StringBuilder(super.getName());
        sb.append(": ");
        List<OpaqueData> userClasses = userClassOption.getUserClassesList();
        if ((userClasses != null) && !userClasses.isEmpty()) {
            for (OpaqueData opaque : userClasses) {
                sb.append(OpaqueDataUtil.toString(opaque));
                sb.append(",");
            }
            sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }

}
