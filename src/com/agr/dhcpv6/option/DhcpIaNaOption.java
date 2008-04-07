package com.agr.dhcpv6.option;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.IoBuffer;

import com.agr.dhcpv6.util.DhcpConstants;

public class DhcpIaNaOption implements DhcpOption
{
    private static Log log = LogFactory.getLog(DhcpIaNaOption.class);

    public int getCode()
    {
        return DhcpConstants.OPTION_IA_NA;
    }
    
    public String getName()
    {
        return "IA_NA";
    }

    public int getLength()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public IoBuffer encode() throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void decode(IoBuffer iobuf) throws IOException
    {
        if ((iobuf != null) && iobuf.hasRemaining()) {
            // already have the code, so length is next
            int len = iobuf.getUnsignedShort();
            if (log.isDebugEnabled())
                log.debug("IA_NA option reports length=" + len +
                          ":  bytes remaining in buffer=" + iobuf.remaining());
            int eof = iobuf.position() + len;
            if (iobuf.position() < eof) {
                // we don't really decode the option, because
                // this message will be thrown away as per RFC3736
                // but we'll "eat" the option to complete the decode
                iobuf.get(new byte[len]);
            }
        }
    }

}
