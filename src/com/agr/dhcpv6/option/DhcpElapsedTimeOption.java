package com.agr.dhcpv6.option;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.server.config.xml.ElapsedTimeOption;
import com.agr.dhcpv6.server.config.xml.OpaqueData;
import com.agr.dhcpv6.server.config.xml.OptionExpression;

/**
 * <p>Title: DhcpElapsedTimeOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpElapsedTimeOption implements DhcpOption, DhcpComparableOption
{
    private static Log log = LogFactory.getLog(DhcpElapsedTimeOption.class);
    
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

    public short getCode()
    {
        return elapsedTimeOption.getCode();
    }

    public short getLength()
    {
        return 2;   // always two bytes (short)
    }

    public ByteBuffer encode() throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(2+2+ getLength());
        bb.putShort(this.getCode());
        bb.putShort(this.getLength());
        bb.putShort(elapsedTimeOption.getValue());
        return (ByteBuffer)bb.flip();
    }

    public void decode(ByteBuffer bb) throws IOException
    {
        if ((bb != null) && bb.hasRemaining()) {
            // already have the code, so length is next
            short len = bb.getShort();
            if (log.isDebugEnabled())
                log.debug(elapsedTimeOption.getName() + " reports length=" + len +
                          ":  bytes remaining in buffer=" + bb.remaining());
            short eof = (short)(bb.position() + len);
            if (bb.position() < eof) {
                elapsedTimeOption.setValue(bb.getShort());
            }
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
                    if (elapsedTimeOption.getValue() == Short.parseShort(ascii)) {
                        return true;
                    }
                }
                catch (NumberFormatException ex) { }
            }
            else {
                byte[] hex = opaque.getHexValue();
                if ( (hex != null) && 
                     (hex.length >= 1) && (hex.length <= 2) ) {
                    short hexShort = 0;
                    if (hex.length == 1) {
                        hexShort = (short)hex[0];
                    }
                    else {
                        hexShort = (short)(hex[0]*256);
                        hexShort += (short)hex[1];
                    }
                    if (elapsedTimeOption.getValue() == hexShort) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder(elapsedTimeOption.getName());
        sb.append(": ");
        sb.append(elapsedTimeOption.getValue());
        return sb.toString();
    }
    
}
