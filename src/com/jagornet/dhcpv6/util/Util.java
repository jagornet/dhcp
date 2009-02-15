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

/**
 * The class Util.
 * Utility class for converting byte arrays to/from hex strings.
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
                str.append((v < 0x10 ? "0" : "") + Integer.toHexString((int) v));
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
}