/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseIpAddressListOption.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.option.base;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.option.DhcpComparableOption;
import com.jagornet.dhcp.util.Util;
import com.jagornet.dhcp.xml.IpAddressListOptionType;
import com.jagornet.dhcp.xml.Operator;
import com.jagornet.dhcp.xml.OptionExpression;

/**
 * Title: BaseIpAddressListOption
 * Description: The abstract base class for server address list DHCP options.
 * 
 * @author A. Gregory Rabil
 */

public abstract class BaseIpAddressListOption extends BaseDhcpOption implements DhcpComparableOption
{
	private static Logger log = LoggerFactory.getLogger(BaseIpAddressListOption.class);
	
	protected List<String> ipAddressList;
	
	public BaseIpAddressListOption()
	{
		this(null);
	}
	
	public BaseIpAddressListOption(IpAddressListOptionType ipAddressListOption)
	{
		super();
		if (ipAddressListOption != null) {
			if (ipAddressListOption.getIpAddressList() != null) {
				ipAddressList = ipAddressListOption.getIpAddressList();
			}
		}
	}

	public List<String> getIpAddressList() {
		return ipAddressList;
	}

	public void setIpAddressList(List<String> ipAddresses) {
		this.ipAddressList = ipAddresses;
	}
	
	public void addIpAddress(String ipAddress) {
		if (ipAddress != null) {
			if (ipAddressList == null) {
				ipAddressList = new ArrayList<String>();
			}
			ipAddressList.add(ipAddress);
		}
	}

    /**
     * Adds a server IP address from a byte array
     * 
     * @param addr the IP address to add as a byte array
     */
    public void addIpAddress(byte[] addr)
    {
        try {
            if (addr != null) {
                InetAddress inetAddr = InetAddress.getByAddress(addr);
                this.addIpAddress(inetAddr);
            }
        }
        catch (UnknownHostException ex) {
            log.error("Failed to add DnsServer: " + ex);
        }
    }

    /**
     * Adds a server IP address from an InetAddress
     * 
     * @param inetAddr the IP address to add as an InetAddr
     */
    public void addIpAddress(InetAddress inetAddr)
    {
        if (inetAddr != null) {
        	this.addIpAddress(inetAddr.getHostAddress());
        }
    }

	/* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
    	ByteBuffer buf = super.encodeCodeAndLength();
        if (ipAddressList != null) {
            for (String ip : ipAddressList) {
            	InetAddress inetAddr = null;
            	if (!super.isV4()) {
            		inetAddr = Inet6Address.getByName(ip);
            	}
            	else {
            		inetAddr = Inet4Address.getByName(ip);
            	}
                buf.put(inetAddr.getAddress());
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
            	byte[] b;
            	if (!super.isV4()) {
            		b = new byte[16];
            	}
            	else {
            		b = new byte[4];
            	}
                buf.get(b);
                this.addIpAddress(b);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        int len = 0;
        if (ipAddressList != null) {
        	if (!super.isV4()) {
        		len += ipAddressList.size() * 16;   // each IPv6 address is 16 bytes
        	}
        	else {
        		len += ipAddressList.size() * 4;	// each IPv4 address is 4 bytes
        	}
        }
        return len;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpComparableOption#matches(com.jagornet.dhcp.xml.OptionExpression)
     */
    public boolean matches(OptionExpression expression)
    {
        if (expression == null)
            return false;
        if (expression.getCode() != this.getCode())
            return false;
        if (ipAddressList == null)
        	return false;

        // first see if we have a ip address list option to compare to
        IpAddressListOptionType exprOption = expression.getIpAddressListOption();
        if (exprOption != null) {
        	List<String> exprIpAddresses = exprOption.getIpAddressList();
            Operator.Enum op = expression.getOperator();
            if (op.equals(Operator.EQUALS)) {
            	return ipAddressList.equals(exprIpAddresses);
            }
            else if (op.equals(Operator.CONTAINS)) {
            	return ipAddressList.containsAll(exprIpAddresses);
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
        sb.append(": ipAddressList=");
        if (ipAddressList != null) {
        	for (String ipAddress : ipAddressList) {
				sb.append(ipAddress);
				sb.append(',');
			}
        	sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }
}
