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
import java.nio.ByteBuffer;

import com.jagornet.dhcp.core.util.Util;

/**
 * Title: BaseIpAddressOption
 * Description: The abstract base class for ip address DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseIpAddressOption extends BaseDhcpOption
{
	protected String ipAddress;
	
	public BaseIpAddressOption()
	{
		this(null);
	}
	
	public BaseIpAddressOption(String ipAddress) {
		super();
		this.ipAddress = ipAddress;
	}
	
    public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

    @Override
    public int getLength()
    {
    	if (!super.isV4())
    		return 16;		// 128-bit IPv6 address
    	else
    		return 4;		// 32-bit IPv4 address
    }

	@Override
    public ByteBuffer encode() throws IOException
    {
    	ByteBuffer buf = super.encodeCodeAndLength();
        if (ipAddress != null) {
        	InetAddress inetAddr = null;
        	if (!super.isV4()) {
        		inetAddr = Inet6Address.getByName(ipAddress);
        	}
        	else {
        		inetAddr = Inet4Address.getByName(ipAddress);
        	}
            buf.put(inetAddr.getAddress());
        }
        return (ByteBuffer) buf.flip();
    }

    @Override
    public void decode(ByteBuffer buf) throws IOException
    {
    	int len = super.decodeLength(buf);
    	if ((len > 0) && (len <= buf.remaining())) {
    		if (!super.isV4()) {
    			ipAddress = decodeIpAddress(buf);
    		}
    		else {
    			ipAddress = decodeIpV4Address(buf);
    		}
        }
    }
    
    /**
     * Convert an IPv6 address received from the wire to a string.
     * 
     * @param buf the ByteBuffer containing the IPv6 address to be decoded from the wire
     * @return the string representation of the IPv6 address
     * @throws IOException
     */
    public static String decodeIpAddress(ByteBuffer buf) throws IOException
    {
        // it has to be hex from the wire, right?
        byte[] b = new byte[16];
        buf.get(b);
        InetAddress inetAddr = InetAddress.getByAddress(b);
        return inetAddr.getHostAddress();
    }
    
    /**
     * Convert an IPv4 address received from the wire to a string.
     * 
     * @param buf the ByteBuffer containing the IPv4 address to be decoded from the wire
     * @return the string representation of the IPv4 address
     * @throws IOException
     */
    public static String decodeIpV4Address(ByteBuffer buf) throws IOException
    {
        // it has to be hex from the wire, right?
        byte[] b = new byte[4];
        buf.get(b);
        InetAddress inetAddr = InetAddress.getByAddress(b);
        return inetAddr.getHostAddress();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(Util.LINE_SEPARATOR);
        sb.append(super.getName());
        sb.append(": ipAddress=");
        sb.append(ipAddress);
        return sb.toString();
    }

}
