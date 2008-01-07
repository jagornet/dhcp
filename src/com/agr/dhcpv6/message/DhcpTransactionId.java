package com.agr.dhcpv6.message;

import java.nio.ByteBuffer;

public class DhcpTransactionId
{
    public static ByteBuffer encode(long xId)
    {
        ByteBuffer buf = ByteBuffer.allocate(3);
        //byte i0 = (byte)(xId >>> 24);         // r_shift out the last three bytes
        byte i1 = (byte)((xId << 8) >>> 24);  // l_shift first byte out of int, then unsigned r_shift back to one byte
        byte i2 = (byte)((xId << 16) >>> 24); // l_shift first 2 bytes out of int, then unsigned r_shift back to one byte
        byte i3 = (byte)((xId << 24) >>> 24); // l_shift first 3 bytes out of int, then unsigned r_shift back to one byte
        buf.put(i1);
        buf.put(i2);
        buf.put(i3);
        return (ByteBuffer)buf.flip();
    }

    public static long decode(ByteBuffer buf)
    {
        byte[] dst = new byte[3];
        buf.get(dst);
        return ( (((int)dst[0] & 0xFF) * 256 * 256) +
                 (((int)dst[1] & 0xFF) * 256) +
                 ((int)dst[2] & 0xFF) );
    }
}
