package com.jagornet.dhcpv6.option;

import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;

import com.jagornet.dhcpv6.xml.ElapsedTimeOption;
import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.OptionExpression;
import com.jagornet.dhcpv6.util.Util;

/**
 * <p>Title: DhcpElapsedTimeOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpElapsedTimeOption extends BaseDhcpOption implements DhcpComparableOption
{
    private ElapsedTimeOption elapsedTimeOption;
    
    public DhcpElapsedTimeOption()
    {
        super();
        elapsedTimeOption = ElapsedTimeOption.Factory.newInstance();
    }
    public DhcpElapsedTimeOption(ElapsedTimeOption elapsedTimeOption)
    {
        super();
        if (elapsedTimeOption != null)
            this.elapsedTimeOption = elapsedTimeOption;
        else
            this.elapsedTimeOption = ElapsedTimeOption.Factory.newInstance();
    }

    public ElapsedTimeOption getElapsedTimeOption()
    {
        return elapsedTimeOption;
    }

    public void setElapsedTimeOption(ElapsedTimeOption elapsedTimeOption)
    {
        if (elapsedTimeOption != null)
            this.elapsedTimeOption = elapsedTimeOption;
    }

    public int getLength()
    {
        return 2;   // always two bytes (short)
    }

    public IoBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        iobuf.putShort((short)elapsedTimeOption.getIntValue());
        return (IoBuffer)iobuf.flip();
    }

    public void decode(IoBuffer iobuf) throws IOException
    {
    	int len = super.decodeLength(iobuf);
    	if ((len > 0) && (len <= iobuf.remaining())) {
    		elapsedTimeOption.setIntValue(iobuf.getUnsignedShort());
        }
    }

    public boolean matches(OptionExpression expression)
    {
        if (expression == null)
            return false;
        if (expression.getCode() != this.getCode())
            return false;

        OpaqueData opaque = expression.getData();
        if (opaque != null) {
            String ascii = opaque.getAsciiValue();
            if (ascii != null) {
                try {
                	// need an Integer to handle unsigned short
                    if (elapsedTimeOption.getIntValue() == Integer.parseInt(ascii)) {
                        return true;
                    }
                }
                catch (NumberFormatException ex) { }
            }
            else {
                byte[] hex = opaque.getHexValue();
                if ( (hex != null) && 
                     (hex.length >= 1) && (hex.length <= 2) ) {
                	int hexUnsignedShort = Integer.valueOf(Util.toHexString(hex), 16);
                    if (elapsedTimeOption.getIntValue() == hexUnsignedShort) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int getCode()
    {
        return elapsedTimeOption.getCode();
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.getName());
        sb.append(": ");
        sb.append(elapsedTimeOption.getIntValue());
        return sb.toString();
    }
    
}
