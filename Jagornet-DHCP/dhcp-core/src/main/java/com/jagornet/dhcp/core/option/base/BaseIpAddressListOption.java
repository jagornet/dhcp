/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.base;

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

import com.jagornet.dhcp.core.util.Util;

/**
 * Title: BaseIpAddressListOption
 * Description: The abstract base class for server address list DHCP options.
 * 
 * @author A. Gregory Rabil
 */

public abstract class BaseIpAddressListOption extends BaseDhcpOption
{
	private static Logger log = LoggerFactory.getLogger(BaseIpAddressListOption.class);

	protected List<String> ipAddressList;
	
	public BaseIpAddressListOption() {
		this(null);
	}
	
	public BaseIpAddressListOption(List<String> ipAddresses) {
		super();
		this.ipAddressList = ipAddresses;
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
            log.error("Failed to add IpAddress: " + ex);
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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
