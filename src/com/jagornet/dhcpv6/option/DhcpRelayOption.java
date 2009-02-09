package com.jagornet.dhcpv6.option;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.mina.core.buffer.IoBuffer;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.message.DhcpMessageFactory;
import com.jagornet.dhcpv6.util.DhcpConstants;

public class DhcpRelayOption extends BaseDhcpOption
{
    protected static String NAME = "Relay Message";
    
    // a verbatim copy of the contained DhcpMessage, which may itself be
    // a DhcpRelayMessage, thus containing yet another DhcpRelayOption
    protected DhcpMessage relayMessage;
    
    protected InetAddress peerAddress;
    
    public DhcpRelayOption(InetAddress peerAddress)
    {
        this.peerAddress = peerAddress;
    }

    public int getLength()
    {
        // the length of this option is the length of the contained message
        return relayMessage.getLength();
    }

    public IoBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        iobuf.put(relayMessage.encode());
        return iobuf.flip();
    }

    public void decode(IoBuffer iobuf) throws IOException
    {
    	int len = super.decodeLength(iobuf);
    	if ((len > 0) && (len <= iobuf.remaining())) {
            int p = iobuf.position();
            short msgtype = iobuf.getUnsigned();
            DhcpMessage dhcpMessage = 
                DhcpMessageFactory.getDhcpMessage(msgtype, peerAddress);
            if (dhcpMessage != null) {
                // reset buffer position so DhcpMessage decoder will 
                // read it and set the message type - we only read it 
                // ourselves so that we could use it for the factory.
                iobuf.position(p); 
                dhcpMessage.decode(iobuf);
                // now that it is decoded, we can set
                // the reference to it for this option
                relayMessage = dhcpMessage;
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

    public int getCode()
    {
        return DhcpConstants.OPTION_RELAY_MSG;
    }

    public String getName()
    {
        return NAME;
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
