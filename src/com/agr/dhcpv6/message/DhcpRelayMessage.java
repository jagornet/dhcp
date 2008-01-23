package com.agr.dhcpv6.message;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.option.DhcpOption;
import com.agr.dhcpv6.option.DhcpRelayOption;
import com.agr.dhcpv6.util.DhcpConstants;

/**
 * <p>Title: DhcpRelayMessage </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpRelayMessage extends DhcpMessage
{
    private static Log log = LogFactory.getLog(DhcpRelayMessage.class);

    // messageType is in DhcpMessage superclass
    protected byte hopCount = 0;
    protected InetAddress linkAddress = null;
    protected InetAddress peerAddress = null;
    
    // MUST have a relay option to be a relay message
    // this object is here as a convenience, because
    // it must ALSO be in the dhcpOptions Map that is
    // in the DhcpMessage superclass
    protected DhcpRelayOption relayOption = null;
    
    public DhcpRelayMessage(InetAddress peerAddress)
    {
        super(peerAddress, DhcpConstants.SERVER_PORT);
    }

    @Override
    public ByteBuffer encode() throws IOException
    {
        if (log.isDebugEnabled())
            log.debug("Encoding DhcpRelayMessage for: " + socketAddress);
        
        long s = System.currentTimeMillis();
        
        ByteBuffer buf = ByteBuffer.allocate(1024);
        buf.put(messageType);
        buf.put(hopCount);
        buf.put(linkAddress.getAddress());
        buf.put(peerAddress.getAddress());
        buf.put(encodeOptions());
        buf.flip();
        
        if (log.isDebugEnabled())
            log.debug("DhcpRelayMessage encoded in " +
                    String.valueOf(System.currentTimeMillis()-s) + " ms.");
        
        return buf;
    }
    
    public static DhcpRelayMessage decode(InetSocketAddress srcInetSocketAddress,
                                          ByteBuffer buf)
            throws IOException
    {
        DhcpRelayMessage relayMessage = 
            new DhcpRelayMessage(srcInetSocketAddress.getAddress());
        relayMessage.decode(buf);
        return relayMessage;
    }

    @Override
    public void decode(ByteBuffer buf) throws IOException
    {
        if (log.isDebugEnabled())
            log.debug("Decoding DhcpRelayMessage from: " + socketAddress);

        long s = System.currentTimeMillis();

        if ((buf != null) && buf.hasRemaining()) {
            decodeMessageType(buf);
            setHopCount(buf.get());
            if (log.isDebugEnabled())
                log.debug("HopCount=" + getHopCount());
            
            if (buf.hasRemaining()) {
                InetAddress linkAddr = decodeInet6Address(buf);
                setLinkAddress(linkAddr);
                if (log.isDebugEnabled())
                    log.debug("LinkAddress: " + linkAddr);
                
                if (buf.hasRemaining()) {
                    InetAddress peerAddr = decodeInet6Address(buf);
                    setPeerAddress(peerAddr);
                    if (log.isDebugEnabled())
                        log.debug("PeerAddress: " + peerAddr);
                    
                    if (buf.hasRemaining()) {
                        Map<Short,DhcpOption> options = decodeOptions(buf);
                        if ( (options != null) &&
                             options.containsKey(DhcpConstants.OPTION_RELAY_MSG)) {
                            setDhcpOptions(options);
                            setRelayOption((DhcpRelayOption)
                                        options.get(DhcpConstants.OPTION_RELAY_MSG));
                        }
                        else {
                            String errmsg = "Failed to decode relay message: no relay option found";
                            log.error(errmsg);
                            throw new IOException(errmsg);
                        }
                    }
                    else {
                        String errmsg = "Failed to decode options: buffer is empty"; 
                        log.error(errmsg);
                        throw new IOException(errmsg);
                    }
                }
                else {
                    String errmsg = "Failed to decode peer address: buffer is empty";
                    log.error(errmsg);
                    throw new IOException(errmsg);
                }
            }
            else {
                String errmsg = "Failed to decode link address: buffer is empty";
                log.error(errmsg);
                throw new IOException(errmsg);
            }
        }
        else {
            String errmsg = "Failed to decode hop count: buffer is empty";
            log.error(errmsg);
            throw new IOException(errmsg);
        }
        
        if (log.isDebugEnabled()) {
            log.debug("DhcpRelayMessage decoded in " +
                    String.valueOf(System.currentTimeMillis()-s) + " ms.");
        }
    }
    
    protected InetAddress decodeInet6Address(ByteBuffer buf) throws IOException
    {
        if (buf.remaining() >= 16) {
            byte[] addr = new byte[16];
            buf.get(addr);
            return Inet6Address.getByAddress(addr);
        }
        else {
            String errmsg = "Failed to decode address: " + buf.remaining() +
                            " bytes remaining in buffer.";
            log.error(errmsg);
            throw new IOException(errmsg);
        }
    }
    
    @Override
    public short getLength()
    {
        short len = 34;     // relay msg type (1) + hop count (1) +
                            // link addr (16) + peer addr (16)
        len += getOptionsLength();
        return len;
    }
    
    public byte getHopCount()
    {
        return hopCount;
    }
    public void setHopCount(byte hopCount)
    {
        this.hopCount = hopCount;
    }
    public InetAddress getLinkAddress()
    {
        return linkAddress;
    }
    public void setLinkAddress(InetAddress linkAddress)
    {
        this.linkAddress = linkAddress;
    }
    public InetAddress getPeerAddress()
    {
        return peerAddress;
    }
    public void setPeerAddress(InetAddress peerAddress)
    {
        this.peerAddress = peerAddress;
    }
    public DhcpRelayOption getRelayOption()
    {
        return relayOption;
    }
    public void setRelayOption(DhcpRelayOption relayOption)
    {
        this.relayOption = relayOption;
    }
    
    @Override
    public InetAddress getHost()
    {
        return peerAddress;
    }
}
