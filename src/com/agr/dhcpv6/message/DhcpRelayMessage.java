package com.agr.dhcpv6.message;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;

import org.apache.mina.common.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static Logger log = LoggerFactory.getLogger(DhcpRelayMessage.class);

    // messageType is in DhcpMessage superclass
    protected short hopCount = 0;	// need a short to hold unsigned byte
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
    public IoBuffer encode() throws IOException
    {
        if (log.isDebugEnabled())
            log.debug("Encoding DhcpRelayMessage for: " + socketAddress);
        
        long s = System.currentTimeMillis();
        
        IoBuffer iobuf = IoBuffer.allocate(1024);
        iobuf.put((byte)messageType);
        iobuf.put((byte)hopCount);
        iobuf.put(linkAddress.getAddress());
        iobuf.put(peerAddress.getAddress());
        iobuf.put(encodeOptions());
        iobuf.flip();
        
        if (log.isDebugEnabled())
            log.debug("DhcpRelayMessage encoded in " +
                    String.valueOf(System.currentTimeMillis()-s) + " ms.");
        
        return iobuf;
    }
    
    public static DhcpRelayMessage decode(InetSocketAddress srcInetSocketAddress,
                                          IoBuffer iobuf)
            throws IOException
    {
        DhcpRelayMessage relayMessage = 
            new DhcpRelayMessage(srcInetSocketAddress.getAddress());
        relayMessage.decode(iobuf);
        return relayMessage;
    }

    @Override
    public void decode(IoBuffer iobuf) throws IOException
    {
        if (log.isDebugEnabled())
            log.debug("Decoding DhcpRelayMessage from: " + socketAddress);

        long s = System.currentTimeMillis();

        if ((iobuf != null) && iobuf.hasRemaining()) {
            decodeMessageType(iobuf);
            setHopCount(iobuf.getUnsigned());
            if (log.isDebugEnabled())
                log.debug("HopCount=" + getHopCount());
            
            if (iobuf.hasRemaining()) {
                InetAddress linkAddr = decodeInet6Address(iobuf);
                setLinkAddress(linkAddr);
                if (log.isDebugEnabled())
                    log.debug("LinkAddress: " + linkAddr);
                
                if (iobuf.hasRemaining()) {
                    InetAddress peerAddr = decodeInet6Address(iobuf);
                    setPeerAddress(peerAddr);
                    if (log.isDebugEnabled())
                        log.debug("PeerAddress: " + peerAddr);
                    
                    if (iobuf.hasRemaining()) {
                        Map<Integer,DhcpOption> options = decodeOptions(iobuf);
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
    
    protected InetAddress decodeInet6Address(IoBuffer iobuf) throws IOException
    {
        if (iobuf.remaining() >= 16) {
            byte[] addr = new byte[16];
            iobuf.get(addr);
            return Inet6Address.getByAddress(addr);
        }
        else {
            String errmsg = "Failed to decode address: " + iobuf.remaining() +
                            " bytes remaining in buffer.";
            log.error(errmsg);
            throw new IOException(errmsg);
        }
    }
    
    @Override
    public int getLength()
    {
        int len = 34;     // relay msg type (1) + hop count (1) +
                          // link addr (16) + peer addr (16)
        len += getOptionsLength();
        return len;
    }
    
    public short getHopCount()
    {
        return hopCount;
    }
    public void setHopCount(short hopCount)
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
