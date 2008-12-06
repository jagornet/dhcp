package com.agr.dhcpv6.option;

import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agr.dhcpv6.server.config.xml.OpaqueData;
import com.agr.dhcpv6.server.config.xml.OptionExpression;
import com.agr.dhcpv6.server.config.xml.StatusCodeOption;
import com.agr.dhcpv6.util.Util;

/**
 * <p>Title: DhcpStatusCodeOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpStatusCodeOption extends BaseDhcpOption implements DhcpComparableOption
{
	private static Logger log = LoggerFactory.getLogger(DhcpStatusCodeOption.class);
    
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

    public int getLength()
    {
        String msg = statusCodeOption.getMessage();
        return (2 + (msg!=null ? msg.length() : 0));
    }

    public IoBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        iobuf.putShort((short)statusCodeOption.getStatusCode());
        String msg = statusCodeOption.getMessage();
        if (msg != null) {
        	iobuf.put(msg.getBytes());
        }
        return iobuf.flip();
    }

    public void decode(IoBuffer iobuf) throws IOException
    {
    	int len = super.decodeLength(iobuf);
    	if ((len > 0) && (len <= iobuf.remaining())) {
            statusCodeOption.setStatusCode(iobuf.getUnsignedShort());
            int eof = iobuf.position() + len;
            if (iobuf.position() < eof) {
                byte[] data = new byte[len-2];  // minus 2 for the status code
                iobuf.get(data);
                statusCodeOption.setMessage(new String(data));
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
                	// need an int to handle unsigned short
                    if (statusCodeOption.getStatusCode() == Integer.parseInt(ascii)) {
                        return true;
                    }
                }
                catch (NumberFormatException ex) { }
            }
            else {
                byte[] hex = opaque.getHexValue();
                if ( (hex != null) && 
                     (hex.length >= 1) && (hex.length <= 2) ) {
                	int hexInt = Integer.parseInt(Util.toHexString(hex));
                    if (statusCodeOption.getStatusCode() == hexInt) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int getCode()
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
