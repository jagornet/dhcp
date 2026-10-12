/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.message;

import java.nio.ByteBuffer;

import com.jagornet.dhcp.core.util.Util;

/**
 * Title: DhcpV6TransactionId
 * Description: A class representing the Transaction Id of a DhcpV6Message.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6TransactionId
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
