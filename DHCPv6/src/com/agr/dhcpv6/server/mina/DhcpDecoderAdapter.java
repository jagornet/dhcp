package com.agr.dhcpv6.server.mina;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.IoBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.agr.dhcpv6.message.DhcpMessage;
import com.agr.dhcpv6.message.DhcpMessageFactory;
import com.agr.dhcpv6.util.DhcpConstants;

public class DhcpDecoderAdapter extends ProtocolDecoderAdapter
{
    private static Log log = LogFactory.getLog(DhcpDecoderAdapter.class);

    public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out)
            throws Exception
    {
        InetSocketAddress from = (InetSocketAddress)session.getRemoteAddress();
        if (log.isDebugEnabled())
            log.debug("Decoding DhcpMessage from: " + from);
        
        DhcpMessage dhcpMessage = decode(in, from);
        
        if (dhcpMessage != null) {
            if (log.isDebugEnabled())
                log.debug("Writing decoded message from: " + from);
            out.write(dhcpMessage);
        }
        else {
            log.error("No message decoded.");
        }
    }

	public DhcpMessage decode(IoBuffer in, InetSocketAddress from)
			throws IOException,	ProtocolDecoderException
	{
		DhcpMessage dhcpMessage = null;		
		if ((in != null) && in.hasRemaining()) {

            // get the message type byte to see if we can handle it
            byte msgtype = in.get();
            if (log.isDebugEnabled())
                log.debug("MessageType=" + DhcpConstants.getMessageString(msgtype));

            // get either a DhcpMessage or a DhcpRelayMessage object
            dhcpMessage = DhcpMessageFactory.getDhcpMessage(msgtype, from.getAddress());
            if (dhcpMessage != null) {
                // rewind buffer so DhcpMessage decoder will read it and 
                // set the message type - we only read it ourselves so 
                // that we could use it for the factory.
                in.rewind();
                dhcpMessage.decode(in);
            }
        }
        else {
            String errmsg = "Failed to decode message: buffer is empty";
            log.error(errmsg);
            throw new ProtocolDecoderException(errmsg);
        }
		return dhcpMessage;
	}
}
