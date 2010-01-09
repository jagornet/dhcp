/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpTransactionId.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.message;

import java.nio.ByteBuffer;

import com.jagornet.dhcpv6.util.Util;

/**
 * Title: DhcpTransactionId
 * Description: A class representing the Transaction Id of a DhcpMessage.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpTransactionId
{
    
    /**
     * Encode.
     * 
     * @param xId the transaction id to be encoded
     * 
     * @return the byte buffer
     */
    public static ByteBuffer encode(int xId)
    {
    	ByteBuffer buf = ByteBuffer.allocate(3);
/*
        //byte i0 = (byte)(xId >>> 24);         // r_shift out the last three bytes
        byte i1 = (byte)((xId << 8) >>> 24);  // l_shift first byte out of int, then unsigned r_shift back to one byte
        byte i2 = (byte)((xId << 16) >>> 24); // l_shift first 2 bytes out of int, then unsigned r_shift back to one byte
        byte i3 = (byte)((xId << 24) >>> 24); // l_shift first 3 bytes out of int, then unsigned r_shift back to one byte
        buf.put(i1);
        buf.put(i2);
        buf.put(i3);
*/
        Util.putMediumInt(buf, xId);
        return (ByteBuffer) buf.flip();
    }

    /**
     * Decode.
     * 
     * @param buf the byte buffer
     * 
     * @return the int as the decoded transaction id
     */
    public static int decode(ByteBuffer buf)
    {
    	return Util.getUnsignedMediumInt(buf);
    }
}
