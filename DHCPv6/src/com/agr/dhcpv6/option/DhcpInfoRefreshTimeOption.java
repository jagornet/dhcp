package com.agr.dhcpv6.option;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.server.config.xml.InfoRefreshTimeOption;
import com.agr.dhcpv6.server.config.xml.OpaqueData;
import com.agr.dhcpv6.server.config.xml.OptionExpression;

/**
 * <p>Title: DhcpInfoRefreshTimeOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpInfoRefreshTimeOption implements DhcpOption, DhcpComparableOption
{
    private static Log log = LogFactory.getLog(DhcpInfoRefreshTimeOption.class);
    
    private InfoRefreshTimeOption infoRefreshTimeOption;
    
    public DhcpInfoRefreshTimeOption()
    {
        super();
        infoRefreshTimeOption = new InfoRefreshTimeOption();
    }
    public DhcpInfoRefreshTimeOption(InfoRefreshTimeOption infoRefreshTimeOption)
    {
        super();
        if (infoRefreshTimeOption != null)
            this.infoRefreshTimeOption = infoRefreshTimeOption;
        else
            this.infoRefreshTimeOption = new InfoRefreshTimeOption();
    }

    public InfoRefreshTimeOption getInfoRefreshTimeOption()
    {
        return infoRefreshTimeOption;
    }

    public void setInfoRefreshTimeOption(InfoRefreshTimeOption infoRefreshTimeOption)
    {
        if (infoRefreshTimeOption != null)
            this.infoRefreshTimeOption = infoRefreshTimeOption;
    }

    public short getLength()
    {
        return 4;   // always four bytes (int)
    }

    public ByteBuffer encode() throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(2+2+ getLength());
        bb.putShort(this.getCode());
        bb.putShort(this.getLength());
        bb.putInt(infoRefreshTimeOption.getValue());
        return (ByteBuffer)bb.flip();
    }

    public void decode(ByteBuffer bb) throws IOException
    {
        if ((bb != null) && bb.hasRemaining()) {
            // already have the code, so length is next
            short len = bb.getShort();
            if (log.isDebugEnabled())
                log.debug(infoRefreshTimeOption.getName() + " reports length=" + len +
                          ":  bytes remaining in buffer=" + bb.remaining());
            short eof = (short)(bb.position() + len);
            if (bb.position() < eof) {
                infoRefreshTimeOption.setValue(bb.getInt());
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
                    if (infoRefreshTimeOption.getValue() == Integer.parseInt(ascii)) {
                        return true;
                    }
                }
                catch (NumberFormatException ex) { }
            }
            else {
                byte[] hex = opaque.getHexValue();
                if ( (hex != null) && 
                     (hex.length >= 1) && (hex.length <= 4) ) {
                    int hexInt = 0;
                    if (hex.length == 1) {
                        hexInt = (int)hex[0];
                    }
                    else if (hex.length == 2) {
                        hexInt = (int)(hex[0]*256);
                        hexInt += (int)hex[1];
                    }
                    else if (hex.length == 3) {
                        hexInt = (int)(hex[0]*256*256);
                        hexInt += (int)(hex[1]*256);
                        hexInt += (int)hex[2];
                    }
                    else if (hex.length == 4) {
                        hexInt = (int)(hex[0]*256*256*256);
                        hexInt += (int)(hex[1]*256*256);
                        hexInt += (int)(hex[2]*256);
                        hexInt += (int)hex[3];
                    }
                    if (infoRefreshTimeOption.getValue() == hexInt) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public short getCode()
    {
        return infoRefreshTimeOption.getCode();
    }

    public String getName()
    {
        return infoRefreshTimeOption.getName();
    }
   
    public String toString()
    {
        StringBuilder sb = new StringBuilder(infoRefreshTimeOption.getName());
        sb.append(": ");
        sb.append(infoRefreshTimeOption.getValue());
        return sb.toString();
    }    
}
