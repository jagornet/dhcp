package com.agr.dhcpv6.server.mina;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.IoBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderException;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.agr.dhcpv6.message.DhcpMessage;

public class DhcpEncoderAdapter extends ProtocolEncoderAdapter
{
    private static Log log = LogFactory.getLog(DhcpEncoderAdapter.class);
    
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out)
            throws Exception
    {
        if (message instanceof DhcpMessage) {
            DhcpMessage dhcpMessage = (DhcpMessage) message;
            IoBuffer buffer = IoBuffer.wrap(dhcpMessage.encode());
            if (log.isDebugEnabled())
                log.debug("Writing encoded message for: " + 
                          dhcpMessage.getSocketAddress());
            out.write(buffer);
        }
        else {
            String errmsg = "Unknown message object class: " + message.getClass();
            log.error(errmsg);
            throw new ProtocolEncoderException(errmsg);
        }
    }
}
