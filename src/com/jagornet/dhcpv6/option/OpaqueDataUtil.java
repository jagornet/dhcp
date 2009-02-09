package com.jagornet.dhcpv6.option;

import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Enumeration;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.Operator;
import com.jagornet.dhcpv6.xml.OptionExpression;
import com.jagornet.dhcpv6.util.Util;

/**
 * <p>Title: OpaqueDataUtil </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class OpaqueDataUtil
{
	private static Logger log = LoggerFactory.getLogger(OpaqueDataUtil.class);
    
    public static int getLength(OpaqueData opaque)
    {
        if (opaque == null)
            return 0;
        
        String ascii = opaque.getAsciiValue();
        if (ascii != null) {
            // two bytes for the len + len of string
            return 2 + ascii.length();
        }
        else {
            // two bytes for the len + len of hex data
            return 2 + opaque.getHexValue().length;
        }
    }

    public static int getLengthDataOnly(OpaqueData opaque)
    {
        if (opaque == null)
            return 0;
        
        String ascii = opaque.getAsciiValue();
        if (ascii != null) {
            return ascii.length();
        }
        else {
        	byte[] hex = opaque.getHexValue();
        	if (hex == null)
        		return 0;
            return hex.length;
        }
    }
    
    /**
     * @param iobuf
     * @param opaque
     */
    public static void encode(IoBuffer iobuf, OpaqueData opaque)
    {
        if ( (iobuf == null) || (opaque == null) )
            return;
        
        String ascii = opaque.getAsciiValue();
        if (ascii != null) {
            iobuf.putShort((short)ascii.length());
            iobuf.put(ascii.getBytes());
        }
        else {
            byte[] hexval = opaque.getHexValue();
            if (hexval != null) {
                iobuf.putShort((short)hexval.length);
                iobuf.put(hexval);
            }
        }        
    }
    
    /**
     * @param iobuf
     * @param opaque
     */
    public static void encodeDataOnly(IoBuffer iobuf, OpaqueData opaque)
    {
        if ( (iobuf == null) || (opaque == null) )
            return;

        String ascii = opaque.getAsciiValue();
        if (ascii != null) {
            iobuf.put(ascii.getBytes());
        }
        else {
            byte[] hexval = opaque.getHexValue();
            if (hexval != null) {
                iobuf.put(hexval);
            }
        }        
    }
    
    public static OpaqueData decode(IoBuffer iobuf)
    {
        if ((iobuf == null) || !iobuf.hasRemaining())
            return null;
        
        OpaqueData opaque = OpaqueData.Factory.newInstance();
        int len = iobuf.getUnsignedShort();
        if (len > 0) {
            byte[] data = new byte[len];
            iobuf.get(data);
            String str = new String(data);
            if (str.matches("\\p{Print}+")) {
                opaque.setAsciiValue(str);
            }
            else {
                opaque.setHexValue(data);
            }
        }
        return opaque;
    }
    
    public static OpaqueData decodeDataOnly(IoBuffer iobuf, int len)
    {
        if (iobuf == null)
            return null;
        
        OpaqueData opaque = OpaqueData.Factory.newInstance();
        if (len > 0) {
            byte[] data = new byte[len];
            iobuf.get(data);
            String str = new String(data);
            if (str.matches("\\p{Print}+")) {
                opaque.setAsciiValue(str);
            }
            else {
                opaque.setHexValue(data);
            }
        }
        return opaque;
    }

    public static boolean matches(OptionExpression expression, OpaqueData myOpaque)
    {
        if (expression == null)
            return false;
        
        Operator.Enum op = expression.getOperator();
        OpaqueData expOpaque = expression.getData();
        if (expOpaque != null) {
            String expAscii = expOpaque.getAsciiValue();
            String myAscii = myOpaque.getAsciiValue();
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
                    log.error("Unknown expression operator: " + op);
                    return false;
                }
            }
            else if ( (expAscii == null) && (myAscii == null) ) {
                byte[] expHex = expOpaque.getHexValue();
                byte[] myHex = myOpaque.getHexValue();
                if ( (expHex != null) && (myHex != null) ) {
                    if (op.equals(Operator.EQUALS)) {
                        if (myHex.length == expHex.length) {
                            for (int i=0; i<myHex.length; i++) {
                                if (myHex[i] != expHex[i]) {
                                    return false;
                                }
                            }
                            return true;    // if we get here, it matches
                        }
                        else {
                            return false;   // not same length
                        }
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
                        log.error("Regular expression operator not valid for hex opaque data");
                        return false;
                    }
                    else {
                        log.error("Unknown expression operator: " + op);
                        return false;
                    }
                }
            }
        }
        return false;
    }
    
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
    
    public static boolean equals(OpaqueData opaque1, OpaqueData opaque2)
    {
        if ( (opaque1 == null) || (opaque2 == null) )
            return false;
        
        String ascii1 = opaque1.getAsciiValue();
        if (ascii1 != null) {
            String ascii2 = opaque2.getAsciiValue();
            if (ascii1.equals(ascii2)) {
                return true;
            }
        }
        else {
            byte[] hex1 = opaque1.getHexValue();
            if (hex1 != null) {
                byte[] hex2 = opaque2.getHexValue();
                if (hex2 != null) {
                    if (hex1.length == hex2.length) {
                        for (int i = 0; i < hex1.length; i++) {
                            if (hex1[i] != hex2[i]) {
                                return false;
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Generate the DHCPv6 Server's DUID-LLT.  See sections 9 and 22.3 of RFC 3315.
     * @return
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
