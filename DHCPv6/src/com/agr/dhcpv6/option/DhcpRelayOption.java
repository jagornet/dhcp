package com.agr.dhcpv6.option;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.message.DhcpMessage;
import com.agr.dhcpv6.message.DhcpMessageFactory;
import com.agr.dhcpv6.util.DhcpConstants;

public class DhcpRelayOption implements DhcpOption
{
    private static Log log = LogFactory.getLog(DhcpRelayOption.class);
    
    protected static String NAME = "Relay Message";
    
    // a verbatim copy of the contained DhcpMessage, which may itself be
    // a DhcpRelayMessage, thus containing yet another DhcpRelayOption
    protected DhcpMessage relayMessage;
    
    protected InetAddress peerAddress;
    
    public DhcpRelayOption(InetAddress peerAddress)
    {
        this.peerAddress = peerAddress;
    }

    public short getCode()
    {
        return DhcpConstants.OPTION_RELAY_MSG;
    }

    public short getLength()
    {
        // the length of this option is the length of the contained message
        return relayMessage.getLength();
    }

    public ByteBuffer encode() throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(2+2+ getLength());
        bb.putShort(this.getCode());
        bb.putShort(this.getLength());
        bb.put(relayMessage.encode());
        return (ByteBuffer)bb.flip();
    }

    public void decode(ByteBuffer bb) throws IOException
    {
        if ((bb != null) && bb.hasRemaining()) {
            // already have the code, so length is next
            short len = bb.getShort();
            if (log.isDebugEnabled())
                log.debug(NAME + " reports length=" + len +
                          ":  bytes remaining in buffer=" + bb.remaining());
            short eof = (short)(bb.position() + len);
            if (bb.position() < eof) {
                int p = bb.position();
                byte msgtype = bb.get();
                DhcpMessage dhcpMessage = 
                    DhcpMessageFactory.getDhcpMessage(msgtype, peerAddress);
                if (dhcpMessage != null) {
                    // reset buffer position so DhcpMessage decoder will 
                    // read it and set the message type - we only read it 
                    // ourselves so that we could use it for the factory.
                    bb.position(p); 
                    dhcpMessage.decode(bb);
                    // now that it is decoded, we can set
                    // the reference to it for this option
                    relayMessage = dhcpMessage;
                }
            }
        }
    }

    public DhcpMessage getRelayMessage()
    {
        return relayMessage;
    }

    public void setRelayMessage(DhcpMessage relayMessage)
    {
        this.relayMessage = relayMessage;
    }

    public InetAddress getPeerAddress()
    {
        return peerAddress;
    }

    public void setPeerAddress(InetAddress peerAddress)
    {
        this.peerAddress = peerAddress;
    }

    public String toString()
    {
        if (relayMessage == null)
            return null;
        
        StringBuilder sb = new StringBuilder(NAME);
        sb.append(": ");
        sb.append(relayMessage.toString());
        return sb.toString();
    }
}
