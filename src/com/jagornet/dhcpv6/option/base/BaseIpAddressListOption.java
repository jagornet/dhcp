/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseIpAddressListOption.java is part of DHCPv6.
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
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.option.DhcpComparableOption;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.IpAddressListOptionType;
import com.jagornet.dhcpv6.xml.Operator;
import com.jagornet.dhcpv6.xml.OptionExpression;

/**
 * Title: BaseIpAddressListOption
 * Description: The abstract base class for server address list DHCP options.
 * 
 * @author A. Gregory Rabil
 */

public abstract class BaseIpAddressListOption extends BaseDhcpOption implements DhcpComparableOption
{
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(BaseIpAddressListOption.class);
	
	protected IpAddressListOptionType ipAddressListOption;
	
	public BaseIpAddressListOption()
	{
		this(null);
	}
	
	public BaseIpAddressListOption(IpAddressListOptionType ipAddressListOption)
	{
		super();
		if (ipAddressListOption != null)
			this.ipAddressListOption = ipAddressListOption;
		else
			this.ipAddressListOption = IpAddressListOptionType.Factory.newInstance();
	}

    public IpAddressListOptionType getIpAddressListOption()
    {
		return ipAddressListOption;
	}

	public void setIpAddressListOption(IpAddressListOptionType ipAddressListOption)
	{
		if (ipAddressListOption != null)
			this.ipAddressListOption = ipAddressListOption;
	}

	/* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
    	ByteBuffer buf = super.encodeCodeAndLength();
        List<String> serverIps = ipAddressListOption.getIpAddressList();
        if (serverIps != null) {
            for (String ip : serverIps) {
                InetAddress inet6Addr = Inet6Address.getByName(ip);
                buf.put(inet6Addr.getAddress());
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
            while (buf.position() < eof) {
                // it has to be hex from the wire, right?
                byte[] b = new byte[16];
                buf.get(b);
                this.addServer(b);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        int len = 0;
        List<String> serverIps = ipAddressListOption.getIpAddressList();
        if (serverIps != null) {
            len += serverIps.size() * 16;   // each IPv6 address is 16 bytes
        }
        return len;
    }

    /**
     * Adds the server.
     * 
     * @param addr the addr
     */
    public void addServer(byte[] addr)
    {
        try {
            if (addr != null) {
                InetAddress inetAddr = InetAddress.getByAddress(addr);
                this.addServer(inetAddr);
            }
        }
        catch (UnknownHostException ex) {
            log.error("Failed to add DnsServer: " + ex);
        }
    }

    /**
     * Adds the server.
     * 
     * @param inetAddr the inet addr
     */
    public void addServer(InetAddress inetAddr)
    {
        if (inetAddr != null) {
        	ipAddressListOption.addIpAddress(inetAddr.getHostAddress());
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
        if (ipAddressListOption == null)
        	return false;

        List<String> myServerIps = ipAddressListOption.getIpAddressList();
        if (myServerIps == null)
        	return false;
        
        // first see if we have a ip address list option to compare to
        IpAddressListOptionType that = expression.getIpAddressListOption();
        if (that != null) {
        	List<String> serverIps = that.getIpAddressList();
            Operator.Enum op = expression.getOperator();
            if (op.equals(Operator.EQUALS)) {
            	return myServerIps.equals(serverIps);
            }
            else if (op.equals(Operator.CONTAINS)) {
            	return myServerIps.containsAll(serverIps);
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
        StringBuilder sb = new StringBuilder(Util.LINE_SEPARATOR);
        sb.append(super.getName());
        sb.append(Util.LINE_SEPARATOR);
        // use XmlObject implementation
        sb.append(ipAddressListOption.toString());
        return sb.toString();
    }
}
