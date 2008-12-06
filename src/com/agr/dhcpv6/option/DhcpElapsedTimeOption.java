package com.agr.dhcpv6.option;

import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agr.dhcpv6.server.config.xml.ElapsedTimeOption;
import com.agr.dhcpv6.server.config.xml.OpaqueData;
import com.agr.dhcpv6.server.config.xml.OptionExpression;
import com.agr.dhcpv6.util.Util;

/**
 * <p>Title: DhcpElapsedTimeOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpElapsedTimeOption extends BaseDhcpOption implements DhcpComparableOption
{
	private static Logger log = LoggerFactory.getLogger(DhcpElapsedTimeOption.class);
    
    private ElapsedTimeOption elapsedTimeOption;
    
    public DhcpElapsedTimeOption()
    {
        super();
        elapsedTimeOption = new ElapsedTimeOption();
    }
    public DhcpElapsedTimeOption(ElapsedTimeOption elapsedTimeOption)
    {
        super();
        if (elapsedTimeOption != null)
            this.elapsedTimeOption = elapsedTimeOption;
        else
            this.elapsedTimeOption = new ElapsedTimeOption();
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
        iobuf.putShort((short)elapsedTimeOption.getValue());
        return (IoBuffer)iobuf.flip();
    }

    public void decode(IoBuffer iobuf) throws IOException
    {
    	int len = super.decodeLength(iobuf);
    	if ((len > 0) && (len <= iobuf.remaining())) {
    		elapsedTimeOption.setValue(iobuf.getUnsignedShort());
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
                    if (elapsedTimeOption.getValue() == Integer.parseInt(ascii)) {
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
                    if (elapsedTimeOption.getValue() == hexUnsignedShort) {
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

    public String getName()
    {
        return elapsedTimeOption.getName();
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder(elapsedTimeOption.getName());
        sb.append(": ");
        sb.append(elapsedTimeOption.getValue());
        return sb.toString();
    }
    
}
