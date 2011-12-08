/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file Util.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.util;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ReadOnlyBufferException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.TimeZone;

/**
 * Title: Util
 * Description: Utility class for converting byte arrays to/from hex strings.
 * 
 * @author A. Gregory Rabil
 */
public class Util
{
    /**
     * To hex string.
     * 
     * @param binary the byte array to convert
     * 
     * @return the string the converted hex string
     */
    public static String toHexString(byte[] binary)
    {
        if(binary != null) {
            StringBuffer str = new StringBuffer(binary.length * 2);
            for(int i=0; i < binary.length; i++) {
                int v = (binary[i] << 24) >>> 24;
                str.append((v < 0x10 ? "0" : "") + Integer.toHexString(v));
            }
            return str.toString();
        }
        return null;
    }

    /**
     * From hex string.
     * 
     * @param hexString the hex string to convert
     * 
     * @return the byte[] the converted byte array
     */
    public static byte[] fromHexString(String hexString)
    {
        if(hexString != null) {
            int strlen = hexString.length();
            int blen = strlen/2 +
                       strlen%2;
            byte[] barray = new byte[blen];
            int i=0;
            int j=0;
            if(strlen%2 > 0) {
                // if odd number of characters, prepend a zero
                hexString = "0" + hexString;
            }
            while(i < hexString.length()) {
                int hex = Integer.parseInt(hexString.substring(i, i+2), 16);
                barray[j] = (byte)hex;
                i += 2;
                j++;
            }
            return barray;
        }
        return null;
    }
    
    /**
     * Reads one unsigned byte as a short integer.
     * @see org.apache.mina.core.buffer.IoBuffer.getUnsigned
     */
    public static final short getUnsignedByte(ByteBuffer buf) {
        return (short) (buf.get() & 0xff);
    }

    /**
     * Reads two bytes unsigned integer.
     * @see org.apache.mina.core.buffer.IoBuffer.getUnsignedShort
     */
    public static final int getUnsignedShort(ByteBuffer buf) {
        return (buf.getShort() & 0xffff);
    }

    /**
     * Reads four bytes unsigned integer.
     * @see org.apache.mina.core.buffer.IoBuffer.getUnsignedInt
     */
    public static final long getUnsignedInt(ByteBuffer buf) {
        return (buf.getInt() & 0xffffffffL);
    }

    /**
     * Relative <i>get</i> method for reading an unsigned medium int value.
     *
     * <p> Reads the next three bytes at this buffer's current position,
     * composing them into an int value according to the current byte order,
     * and then increments the position by three.</p>
     *
     * @return  The unsigned medium int value at the buffer's current position
     * 
     * @see org.apache.mina.core.buffer.IoBuffer.getUnsignedMediumInt
     */
    public static final int getUnsignedMediumInt(ByteBuffer buf) {
        int b1 = getUnsignedByte(buf);
        int b2 = getUnsignedByte(buf);
        int b3 = getUnsignedByte(buf);
        if (ByteOrder.BIG_ENDIAN.equals(buf.order())) {
            return b1 << 16 | b2 << 8 | b3;
        } else {
            return b3 << 16 | b2 << 8 | b1;
        }
    }
    
    /**
     * Relative <i>put</i> method for writing a medium int
     * value.
     *
     * <p> Writes three bytes containing the given int value, in the
     * current byte order, into this buffer at the current position, and then
     * increments the position by three.</p>
     *
     * @param  value
     *         The medium int value to be written
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     *          If there are fewer than three bytes
     *          remaining in this buffer
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is read-only
     *          
     * @see org.apache.mina.core.buffer.IoBuffer.putMediumInt
     */
    public static final ByteBuffer putMediumInt(ByteBuffer buf, int value) {
        byte b1 = (byte) (value >> 16);
        byte b2 = (byte) (value >> 8);
        byte b3 = (byte) value;

        if (ByteOrder.BIG_ENDIAN.equals(buf.order())) {
            buf.put(b1).put(b2).put(b3);
        } else {
            buf.put(b3).put(b2).put(b1);
        }

        return buf;
    }
    
    /**
     * Compare IP addresses to determine order
     * 
     * @param ip1
     * @param ip2
     * @return -1 if ip1<ip2, 0 if ip1=ip2, 1 if ip1>ip2
     */
    public static int compareInetAddrs(InetAddress ip1, InetAddress ip2)
    {
    	BigInteger bi1 = new BigInteger(ip1.getAddress());
    	BigInteger bi2 = new BigInteger(ip2.getAddress());
    	return bi1.compareTo(bi2);
    }
	
    /**
     * Use this instead of InetSocketAddress.toString() to avoid
     * DNS lookup - i.e. faster.
     * @param saddr
     * @return
     */
	public static String socketAddressAsString(InetSocketAddress saddr) {
		if (saddr == null)
			return null;
		else
			return saddr.getAddress().getHostAddress() +
					":" + saddr.getPort();
	}	
    
	/**
	 * Get the IPv6 link-local address for the given network interface.
	 * 
	 * @param netIf the network interface
	 * @return the first IPv6 link-local address recorded on the interface
	 */
	public static InetAddress netIfIPv6LinkLocalAddress(NetworkInterface netIf)
	{
        InetAddress v6Addr = null;
        Enumeration<InetAddress> ifAddrs = netIf.getInetAddresses();
        while (ifAddrs.hasMoreElements()) {
        	InetAddress addr = ifAddrs.nextElement();
        	if ((addr instanceof Inet6Address) && addr.isLinkLocalAddress()) {
        		// choose the link-local IPv6 address, which should be unique
        		// because it it must be the EUI-64 auto-configured address
        		v6Addr = addr;
        	}
        }
        return v6Addr;
	}
    public static String LINE_SEPARATOR = System.getProperty("line.separator");
    public static TimeZone GMT_TIMEZONE = TimeZone.getTimeZone("GMT");
    public static Calendar GMT_CALENDAR = Calendar.getInstance(GMT_TIMEZONE);
    
}