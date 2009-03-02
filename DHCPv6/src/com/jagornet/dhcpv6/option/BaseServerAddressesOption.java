/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseServerAddressesOption.java is part of DHCPv6.
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
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Title: BaseServerAddressesOption
 * Description: The abstract base class for server address list DHCP options.
 * 
 * @author A. Gregory Rabil
 */

public abstract class BaseServerAddressesOption extends BaseDhcpOption
{
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(BaseServerAddressesOption.class);

    /**
     * Gets the server ip addresses.
     * 
     * @return the server ip addresses
     */
    public abstract List<String> getServerIpAddresses();
    
    /**
     * Adds the server ip address.
     * 
     * @param serverIpAddress the server ip address
     */
    public abstract void addServerIpAddress(String serverIpAddress);

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        List<String> serverIps = this.getServerIpAddresses();
        if (serverIps != null) {
            for (String ip : serverIps) {
                InetAddress inet6Addr = Inet6Address.getByName(ip);
                iobuf.put(inet6Addr.getAddress());
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
                // it has to be hex from the wire, right?
                byte b[] = new byte[16];
                iobuf.get(b);
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
        List<String> serverIps = this.getServerIpAddresses();
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
            this.addServer(inetAddr.getHostAddress());
        }
    }
    
    /**
     * Adds the server.
     * 
     * @param ip the ip
     */
    public void addServer(String ip)
    {
        if (ip != null) {
            this.addServerIpAddress(ip);
        }
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
        List<String> serverIps = this.getServerIpAddresses();
        if (serverIps != null) {
            for (String ip : serverIps) {
                sb.append(ip);
                sb.append(",");
            }
            sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }
}
