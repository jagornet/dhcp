package com.jagornet.dhcpv6.message;

import org.apache.mina.core.buffer.IoBuffer;

public class DhcpTransactionId
{
    public static IoBuffer encode(long xId)
    {
        IoBuffer iobuf = IoBuffer.allocate(3);
        //byte i0 = (byte)(xId >>> 24);         // r_shift out the last three bytes
        byte i1 = (byte)((xId << 8) >>> 24);  // l_shift first byte out of int, then unsigned r_shift back to one byte
        byte i2 = (byte)((xId << 16) >>> 24); // l_shift first 2 bytes out of int, then unsigned r_shift back to one byte
        byte i3 = (byte)((xId << 24) >>> 24); // l_shift first 3 bytes out of int, then unsigned r_shift back to one byte
        iobuf.put(i1);
        iobuf.put(i2);
        iobuf.put(i3);
        return iobuf.flip();
    }

    public static long decode(IoBuffer iobuf)
    {
    	return iobuf.getUnsignedMediumInt();
    }
}
