package com.jagornet.dhcpv6.server.mina;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderException;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;

public class DhcpEncoderAdapter extends ProtocolEncoderAdapter
{
    private static Logger log = LoggerFactory.getLogger(DhcpEncoderAdapter.class);
    
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out)
            throws Exception
    {
        if (message instanceof DhcpMessage) {
            DhcpMessage dhcpMessage = (DhcpMessage) message;
            IoBuffer buffer = dhcpMessage.encode();
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
