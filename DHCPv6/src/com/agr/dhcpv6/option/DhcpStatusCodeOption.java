package com.agr.dhcpv6.option;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.server.config.xml.StatusCodeOption;
import com.agr.dhcpv6.server.config.xml.OpaqueData;
import com.agr.dhcpv6.server.config.xml.OptionExpression;

/**
 * <p>Title: DhcpStatusCodeOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpStatusCodeOption implements DhcpOption, DhcpComparableOption
{
    private static Log log = LogFactory.getLog(DhcpStatusCodeOption.class);
    
    private StatusCodeOption statusCodeOption;
    
    public DhcpStatusCodeOption()
    {
        super();
        statusCodeOption = new StatusCodeOption();
    }
    public DhcpStatusCodeOption(StatusCodeOption statusCodeOption)
    {
        super();
        if (statusCodeOption != null)
            this.statusCodeOption = statusCodeOption;
        else
            this.statusCodeOption = new StatusCodeOption();
    }

    public StatusCodeOption getStatusCodeOption()
    {
        return statusCodeOption;
    }

    public void setStatusCodeOption(StatusCodeOption statusCodeOption)
    {
        if (statusCodeOption != null)
            this.statusCodeOption = statusCodeOption;
    }

    public short getLength()
    {
        String msg = statusCodeOption.getMessage();
        return (short)(2 + (msg!=null ? msg.length() : 0));
    }

    public ByteBuffer encode() throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(2+2+ getLength());
        bb.putShort(this.getCode());
        bb.putShort(this.getLength());
        bb.putShort(statusCodeOption.getStatusCode());
        bb.put(statusCodeOption.getMessage().getBytes());
        return (ByteBuffer)bb.flip();
    }

    public void decode(ByteBuffer bb) throws IOException
    {
        if ((bb != null) && bb.hasRemaining()) {
            // already have the code, so length is next
            short len = bb.getShort();
            if (log.isDebugEnabled())
                log.debug(statusCodeOption.getName() + " reports length=" + len +
                          ":  bytes remaining in buffer=" + bb.remaining());
            short eof = (short)(bb.position() + len);
            if (bb.position() < eof) {
                statusCodeOption.setStatusCode(bb.getShort());
                if (bb.position() < eof) {
                    byte[] data = new byte[len-2];  // minus 2 for the status code
                    bb.get(data);
                    statusCodeOption.setMessage(new String(data));
                }
            }
        }
    }

    /**
     * Matches only the status code, not the message text
     */
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
                    if (statusCodeOption.getStatusCode() == Short.parseShort(ascii)) {
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
                    if (statusCodeOption.getStatusCode() == hexShort) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public short getCode()
    {
        return statusCodeOption.getCode();
    }

    public String getName()
    {
        return statusCodeOption.getName();
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder(statusCodeOption.getName());
        sb.append(": ");
        sb.append("code=");
        sb.append(statusCodeOption.getStatusCode());
        sb.append(" message=");
        sb.append(statusCodeOption.getMessage());
        return sb.toString();
    }
    
}
