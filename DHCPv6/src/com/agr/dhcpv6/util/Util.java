package com.agr.dhcpv6.util;


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
}