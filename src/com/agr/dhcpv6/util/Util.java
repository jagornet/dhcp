package com.agr.dhcpv6.util;

import java.net.*;
import java.nio.*;  // jdk 1.4

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2000</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class Util
{
    public static String toHexString(byte[] binary)
    {
        if(binary != null) {
            StringBuffer str = new StringBuffer(binary.length * 2);
            for(int i=0; i < binary.length; i++) {
                int v = (binary[i] << 24) >>> 24;
                str.append((v < 0x10 ? "0" : "") + Integer.toHexString((int) v));
            }
            return str.toString();
        }
        return null;
    }

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

/*
    public static byte[] intToByteArray(int aInt)
    {
		byte[] b = new byte[4];
        b[0] = (byte)(aInt >> 24);
        b[1] = (byte)((aInt >> 16) & 0x000000ff);
        b[2] = (byte)((aInt >> 8) & 0x000000ff);
		b[3] = (byte)(aInt & 0x000000ff);
        return b;
    }
*/

    public static byte[] shortToByteArray(short aShort)
    {
        ByteBuffer bb = ByteBuffer.allocate(2);
        ShortBuffer sb = bb.asShortBuffer();
        sb.put(aShort);
        return bb.array();
    }

    public static byte[] intToByteArray(int aInt)
    {
        ByteBuffer bb = ByteBuffer.allocate(4);
        IntBuffer ib = bb.asIntBuffer();
        ib.put(aInt);
        return bb.array();
    }

    public static byte[] longToByteArray(long aLong)
    {
        ByteBuffer bb = ByteBuffer.allocate(8);
        LongBuffer lb = bb.asLongBuffer();
        lb.put(aLong);
        return bb.array();
    }

	public static InetAddress byteArrayToInetAddress(byte[] aByteArray)
	{
		if(aByteArray.length != 16) {
			throw new IllegalArgumentException("InetAddress byte array must be 16 bytes");
		}
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<16; i++) {
			if( (i > 0) && ((i % 2) == 0) ) {
				sb.append(':');
			}
			sb.append(toHexString(new byte[] {aByteArray[i]}));
		}
		//System.out.println("sb=" + sb.toString());
		InetAddress inetAddr = null;
		try {
			inetAddr = InetAddress.getByName(sb.toString());
		}
		catch(UnknownHostException uhe) {
			System.err.println(uhe);
		}
		return inetAddr;
	}


    /**
     * @param bb
     * @param len
     * @return
     */
    public static String byteBufferGetString(ByteBuffer bb, short len)
    {
        byte[] val = new byte[len];
        bb.get(val);
        String valstring = new String(val);
        return valstring;
    }

	static public void main(String[] args)
	{
		byte[] bArray = new byte[] { Byte.parseByte("3f", 16),
								     Byte.parseByte("2e", 16),
									 Byte.parseByte("29", 16),
									 Byte.parseByte("00", 16),
									 Byte.parseByte("7c", 16),
									 Byte.parseByte("40", 16),
									 Byte.parseByte("0a", 16),
									 Byte.parseByte("1e", 16),
									 Byte.parseByte("00", 16),
									 Byte.parseByte("00", 16),
									 Byte.parseByte("00", 16),
									 Byte.parseByte("00", 16),
									 Byte.parseByte("00", 16),
									 Byte.parseByte("00", 16),
									 Byte.parseByte("12", 16),
									 Byte.parseByte("34", 16) };
		InetAddress inetAddr = byteArrayToInetAddress(bArray);
		System.out.println(inetAddr.getHostAddress());
	}
}