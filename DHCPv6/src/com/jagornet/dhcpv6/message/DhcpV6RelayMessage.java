/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpRelayMessage.java is part of DHCPv6.
 *
 *   DHCPv6 is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   DHCPv6 is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with DHCPv6.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcpv6.message;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.option.DhcpV6RelayOption;
import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.util.Util;

/**
 * Title: DhcpV6RelayMessage
 * Description:Object that represents a DHCPv6 relay message as defined in
 * RFC 3315.
 * 
 * There are two relay agent messages, which share the following format:
 * 
 * <pre>
 * 0                   1                   2                   3
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |    msg-type   |   hop-count   |                               |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+                               |
 * |                                                               |
 * |                         link-address                          |
 * |                                                               |
 * |                               +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-|
 * |                               |                               |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+                               |
 * |                                                               |
 * |                         peer-address                          |
 * |                                                               |
 * |                               +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-|
 * |                               |                               |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+                               |
 * .                                                               .
 * .            options (variable number and length)   ....        .
 * |                                                               |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * 
 * The following sections describe the use of the Relay Agent message
 * header.
 * 
 * 7.1. Relay-forward Message
 * 
 * The following table defines the use of message fields in a Relay-
 * forward message.
 * 
 * msg-type       RELAY-FORW
 * 
 * hop-count      Number of relay agents that have relayed this
 * message.
 * 
 * link-address   A global or site-local address that will be used by
 * the server to identify the link on which the client
 * is located.
 * 
 * peer-address   The address of the client or relay agent from which
 * the message to be relayed was received.
 * 
 * options        MUST include a "Relay Message option" (see
 * section 22.10); MAY include other options added by
 * the relay agent.
 * 
 * 7.2. Relay-reply Message
 * 
 * The following table defines the use of message fields in a
 * Relay-reply message.
 * 
 * msg-type       RELAY-REPL
 * 
 * hop-count      Copied from the Relay-forward message
 * 
 * link-address   Copied from the Relay-forward message
 * 
 * peer-address   Copied from the Relay-forward message
 * 
 * options        MUST include a "Relay Message option"; see
 * section 22.10; MAY include other options
 * </pre>
 * 
 * @author A. Gregory Rabil
 */

public class DhcpV6RelayMessage extends DhcpV6Message
{
	private static Logger log = LoggerFactory.getLogger(DhcpV6RelayMessage.class);

    // messageType is in DhcpMessage superclass
	
	/** The hop count.  Need a short to hold unsigned byte. */
    protected short hopCount = 0;
    
    /** The link address.  A global or site-local address that will be used by
     *  the server to identify the link on which the client is located. */
    protected InetAddress linkAddress = null;
    
    /** The peer address.  The address of the client or relay agent from which
     *  the message to be relayed was received. */
	protected InetAddress peerAddress = null;
    
    /** The relay option.  MUST have a relay option to be a relay message.
     * this object is here as a convenience, because
     * it must ALSO be in the dhcpOptions Map that is
     * in the DhcpMessage superclass */
    protected DhcpV6RelayOption relayOption = null;
    
    /**
     * Construct a DhcpRelayMessage.
     * 
     * @param localAddress InetSocketAddress on the local host on which
     * this message is received or sent
     * @param remoteAddress InetSocketAddress on the remote host on which
     * this message is sent or received
     */
    public DhcpV6RelayMessage(InetSocketAddress localAddress, InetSocketAddress remoteAddress)
    {
        super(localAddress, remoteAddress);
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.message.DhcpMessage#encode()
     */
    @Override
    public ByteBuffer encode() throws IOException
    {
        if (log.isDebugEnabled())
            log.debug("Encoding DhcpRelayMessage for: " + remoteAddress);
        
        ByteBuffer buf = ByteBuffer.allocate(1024);
        buf.put((byte)messageType);
        buf.put((byte)hopCount);
        buf.put(linkAddress.getAddress());
        buf.put(peerAddress.getAddress());
        buf.put(encodeOptions());
        buf.flip();
        
        if (log.isDebugEnabled())
            log.debug("DhcpRelayMessage encoded.");
        
        return buf;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.message.DhcpMessage#decode(java.nio.ByteBuffer)
     */
    @Override
    public void decode(ByteBuffer buf) throws IOException
    {
        if (log.isDebugEnabled())
            log.debug("Decoding DhcpRelayMessage from: " + remoteAddress);

        if ((buf != null) && buf.hasRemaining()) {
            decodeMessageType(buf);
            setHopCount(Util.getUnsignedByte(buf));
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
                        Map<Integer,DhcpOption> options = decodeOptions(buf);
                    	DhcpOption dhcpOption = options.get(DhcpConstants.OPTION_RELAY_MSG);
                    	if ((dhcpOption != null) && (dhcpOption instanceof DhcpV6RelayOption)) {
                    		relayOption = (DhcpV6RelayOption) dhcpOption;
                            setDhcpOptionMap(options);                            
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
            log.debug("DhcpRelayMessage decoded.");
        }
    }
    
    /**
     * Decode inet6 address.
     * 
     * @param iobuf the iobuf
     * 
     * @return the inet address
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
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
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.message.DhcpMessage#getLength()
     */
    @Override
    public int getLength()
    {
        int len = 34;     // relay msg type (1) + hop count (1) +
                          // link addr (16) + peer addr (16)
        len += getOptionsLength();
        return len;
    }
    
    /**
     * Gets the hop count.
     * 
     * @return the hop count
     */
    public short getHopCount()
    {
        return hopCount;
    }
    
    /**
     * Sets the hop count.
     * 
     * @param hopCount the new hop count
     */
    public void setHopCount(short hopCount)
    {
        this.hopCount = hopCount;
    }
    
    /**
     * Gets the link address.
     * 
     * @return the link address
     */
    public InetAddress getLinkAddress()
    {
        return linkAddress;
    }
    
    /**
     * Sets the link address.
     * 
     * @param linkAddress the new link address
     */
    public void setLinkAddress(InetAddress linkAddress)
    {
        this.linkAddress = linkAddress;
    }
    
    /**
     * Gets the peer address.
     * 
     * @return the peer address
     */
    public InetAddress getPeerAddress()
    {
		return peerAddress;
	}
	
	/**
	 * Sets the peer address.
	 * 
	 * @param peerAddress the new peer address
	 */
	public void setPeerAddress(InetAddress peerAddress)
	{
		this.peerAddress = peerAddress;
	}
	
	/**
	 * Gets the relay option.
	 * 
	 * @return the relay option
	 */
	public DhcpV6RelayOption getRelayOption()
    {
        return relayOption;
    }
    
    /**
     * Sets the relay option.
     * 
     * @param relayOption the new relay option
     */
    public void setRelayOption(DhcpV6RelayOption relayOption)
    {
        this.relayOption = relayOption;
    }
}
