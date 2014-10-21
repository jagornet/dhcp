/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file OpaqueDataUtil.java is part of Jagornet DHCP.
 *
 *   Jagornet DHCP is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Jagornet DHCP is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Jagornet DHCP.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcp.option;

import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.option.base.BaseOpaqueData;
import com.jagornet.dhcp.util.Util;
import com.jagornet.dhcp.xml.OpaqueData;
import com.jagornet.dhcp.xml.OpaqueDataOptionType;
import com.jagornet.dhcp.xml.Operator;
import com.jagornet.dhcp.xml.OptionExpression;

/**
 * <p>Title: OpaqueDataUtil </p>
 * <p>Description: Utility class for handling OpaqueData objects.</p>
 * 
 * @author A. Gregory Rabil
 */
public class OpaqueDataUtil
{	
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(OpaqueDataUtil.class);

    /**
     * Matches.
     * 
     * @param expression the expression
     * @param myOpaque the my opaque
     * 
     * @return true, if successful
     */
    public static boolean matches(OptionExpression expression, BaseOpaqueData myOpaque)
    {
        if (expression == null)
            return false;
        
        OpaqueDataOptionType opaqueOption = expression.getOpaqueDataOption();
        if (opaqueOption == null)
        	return false;

        Operator.Enum op = expression.getOperator();
        return matches(myOpaque, opaqueOption.getOpaqueData(), op);
    }

    public static boolean matches(BaseOpaqueData myOpaque, OpaqueData that, Operator.Enum op)
    {
        if (that != null) {
            String expAscii = that.getAsciiValue();
            String myAscii = myOpaque.getAscii();
            if ( (expAscii != null) && (myAscii != null) ) {
                if (op.equals(Operator.EQUALS)) {
                    return myAscii.equalsIgnoreCase(expAscii);
                }
                else if (op.equals(Operator.STARTS_WITH)) {
                    return myAscii.startsWith(expAscii);
                }
                else if (op.equals(Operator.CONTAINS)) {
                    return myAscii.contains(expAscii);
                }
                else if (op.equals(Operator.ENDS_WITH)) {
                    return myAscii.endsWith(expAscii);
                }
                else if (op.equals(Operator.REG_EXP)) {
                    return myAscii.matches(expAscii);
                }
                else {
                    log.error("Unsupported expression operator: " + op);
                    return false;
                }
            }
            else if ( (expAscii == null) && (myAscii == null) ) {
                byte[] expHex = that.getHexValue();
                byte[] myHex = myOpaque.getHex();
                if ( (expHex != null) && (myHex != null) ) {
                    if (op.equals(Operator.EQUALS)) {
                    	return Arrays.equals(myHex, expHex);
                    }
                    else if (op.equals(Operator.STARTS_WITH)) {
                        if (myHex.length >= expHex.length) {
                            for (int i=0; i<expHex.length; i++) {
                                if (myHex[i] != expHex[i]) {
                                    return false;
                                }
                            }
                            return true;    // if we get here, it matches
                        }
                        else {
                            return false;   // exp length too long
                        }
                    }
                    else if (op.equals(Operator.CONTAINS)) {
                        if (myHex.length >= expHex.length) {
                            int j=0;
                            for (int i=0; i<myHex.length; i++) {
                                if (myHex[i] == expHex[j]) {
                                    // found a potential match
                                    j++;
                                    boolean matches = true;
                                    for (int ii=i+1; ii<myHex.length; ii++) {
                                        if (myHex[ii] != expHex[j++]) {
                                            matches = false;
                                            break;
                                        }
                                    }
                                    if (matches) {
                                        return true;
                                    }
                                    j=0;    // reset to start of exp
                                }
                            }
                            return false;    // if we get here, it didn't match
                        }
                        else {
                            return false;   // exp length too long
                        }
                    }
                    else if (op.equals(Operator.ENDS_WITH)) {
                        if (myHex.length >= expHex.length) {
                            for (int i=myHex.length-1; 
                                 i>=myHex.length-expHex.length; 
                                 i--) {
                                if (myHex[i] != expHex[i]) {
                                    return false;
                                }
                            }
                            return true;    // if we get here, it matches
                        }
                        else {
                            return false;   // exp length too long
                        }
                    }
                    else if (op.equals(Operator.REG_EXP)) {
                        log.error("Regular expression operator not valid for hex opaque opaqueData");
                        return false;
                    }
                    else {
                        log.error("Unsupported expression operator: " + op);
                        return false;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * To string.
     * 
     * @param opaque the opaque
     * 
     * @return the string
     */
    public static String toString(OpaqueData opaque)
    {
        if (opaque == null)
            return null;
        
        String ascii = opaque.getAsciiValue();
        if (ascii != null)
            return ascii;
        else
            return Util.toHexString(opaque.getHexValue());
    }
    
    /**
     * Equals.
     * 
     * @param opaque1 the opaque1
     * @param opaque2 the opaque2
     * 
     * @return true, if successful
     */    
    public static boolean equals(BaseOpaqueData opaque1, BaseOpaqueData opaque2)
    {
        if ( (opaque1 == null) || (opaque2 == null) )
            return false;
        
        String ascii1 = opaque1.getAscii();
        if (ascii1 != null) {
            String ascii2 = opaque2.getAscii();
            if (ascii1.equalsIgnoreCase(ascii2)) {
                return true;
            }
        }
        else {
        	return Arrays.equals(opaque1.getHex(), opaque2.getHex());
        }
        return false;
    }
    
    /**
     * Generate the DHCPv6 Server's DUID-LLT.  See sections 9 and 22.3 of RFC 3315.
     * 
     * @return the opaque opaqueData
     */
    public static OpaqueData generateDUID_LLT()
    {
    	OpaqueData opaque = null;
    	try {
    		Enumeration<NetworkInterface> intfs = NetworkInterface.getNetworkInterfaces();
    		if (intfs != null) {
    			while (intfs.hasMoreElements()) {
	    			NetworkInterface intf = intfs.nextElement();
	    			if (intf.isUp() && !intf.isLoopback() && !intf.isPointToPoint() && !intf.isVirtual()) {
	    				opaque = OpaqueData.Factory.newInstance();
	    				ByteBuffer bb = ByteBuffer.allocate(intf.getHardwareAddress().length + 8);
	    				bb.putShort((short)1);	// DUID based on LLT
	    				bb.putShort((short)1);	// assume ethernet
	    				bb.putInt((int)(System.currentTimeMillis()/1000));	// seconds since the Epoch
	    				bb.put(intf.getHardwareAddress());
	    				opaque.setHexValue(bb.array());
	    				break;
	    			}
    			}
    		}
    	}
    	catch (Exception ex) {
    		log.error("Failed to generate DUID-LLT: " + ex);
    	}
    	return opaque;
    }
    
    /**
     * The main method.
     * 
     * @param args the arguments
     */
    public static void main(String[] args)
    {
    	/*
        OpaqueData o1 = OpaqueData.Factory.newInstance();
        OpaqueData o2 = OpaqueData.Factory.newInstance();
        o1.setHexValue(new byte[] { (byte)0xde, (byte)0xbb, (byte)0x1e });
        o2.setHexValue(new byte[] { (byte)0xde, (byte)0xbb, (byte)0x1e });
        System.out.println(equals(o1,o2));
        */
    	OpaqueData opaque = generateDUID_LLT();
    	System.out.println(OpaqueDataUtil.toString(opaque));
    }
    
}
