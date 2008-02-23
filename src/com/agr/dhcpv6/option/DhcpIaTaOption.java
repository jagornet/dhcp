package com.agr.dhcpv6.option;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.util.DhcpConstants;

public class DhcpIaTaOption implements DhcpOption
{
    private static Log log = LogFactory.getLog(DhcpIaTaOption.class);

    public short getCode()
    {
        return DhcpConstants.OPTION_IA_TA;
    }
    
    public String getName()
    {
        return "IA_TA";
    }

    public short getLength()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public ByteBuffer encode() throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void decode(ByteBuffer bb) throws IOException
    {
        if ((bb != null) && bb.hasRemaining()) {
            // already have the code, so length is next
            short len = bb.getShort();
            if (log.isDebugEnabled())
                log.debug("IA_TA option reports length=" + len +
                          ":  bytes remaining in buffer=" + bb.remaining());
            short eof = (short)(bb.position() + len);
            if (bb.position() < eof) {
                // we don't really decode the option, because
                // this message will be thrown away as per RFC3736
                // but we'll "eat" the option to complete the decode
                bb.get(new byte[len]);
            }
        }
    }

}
