/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseIpAddressOption.java is part of DHCPv6.
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
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.xml.IpAddressOptionType;
import com.jagornet.dhcpv6.xml.Operator;
import com.jagornet.dhcpv6.xml.OptionExpression;

/**
 * Title: BaseIpAddressOption
 * Description: The abstract base class for ip address DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseIpAddressOption extends BaseDhcpOption
{
	private static Logger log = LoggerFactory.getLogger(BaseIpAddressOption.class);

	protected IpAddressOptionType ipAddressOption;
	
	public BaseIpAddressOption()
	{
		this(null);
	}
	
	public BaseIpAddressOption(IpAddressOptionType ipAddressOption)
	{
		super();
		if (ipAddressOption != null)
			this.ipAddressOption = ipAddressOption;
		else
			this.ipAddressOption = IpAddressOptionType.Factory.newInstance();
	}
	
    public IpAddressOptionType getIpAddressOption()
    {
		return ipAddressOption;
	}

	public void setIpAddressOption(IpAddressOptionType ipAddressOption)
	{
		if (ipAddressOption != null)
			this.ipAddressOption = ipAddressOption;
	}

	/* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        String ipAddress = ipAddressOption.getIpAddress();
        if (ipAddress != null) {
            InetAddress inet6Addr = Inet6Address.getByName(ipAddress);
            iobuf.put(inet6Addr.getAddress());
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
            // it has to be hex from the wire, right?
            byte b[] = new byte[16];
            iobuf.get(b);
            InetAddress inetAddr = InetAddress.getByAddress(b);
            ipAddressOption.setIpAddress(inetAddr.getHostAddress());
        }
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        return 16;		// 128-bit IPv6 address
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
        if (ipAddressOption == null)
        	return false;
        
        String myIpAddress = ipAddressOption.getIpAddress();
        if (myIpAddress == null)
        	return false;

        IpAddressOptionType that = expression.getIpAddressOption();
        if (that != null) {
        	String ipAddress = that.getIpAddress();
            Operator.Enum op = expression.getOperator();
            if (op.equals(Operator.EQUALS)) {
            	return myIpAddress.equals(ipAddress);
            }
            else if (op.equals(Operator.STARTS_WITH)) {
            	return myIpAddress.startsWith(ipAddress);
            }
            else if (op.equals(Operator.ENDS_WITH)) {
            	return myIpAddress.endsWith(ipAddress);
            }
            else if (op.equals(Operator.CONTAINS)) {
            	return myIpAddress.contains(ipAddress);
            }
            else if (op.equals(Operator.REG_EXP)) {
            	return myIpAddress.matches(ipAddress);
            }
            else {
            	log.warn("Unsupported expression operator: " + op);
            }
        }
        
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        if (this == null)
            return null;
        
        StringBuilder sb = new StringBuilder(this.getName());
        sb.append(": ");
        String ipAddress = ipAddressOption.getIpAddress();
        if (ipAddress != null) {
            sb.append(ipAddress);
        }            
        return sb.toString();
    }

}
