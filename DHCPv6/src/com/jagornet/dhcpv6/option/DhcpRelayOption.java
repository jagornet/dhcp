/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpRelayOption.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.option;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.message.DhcpRelayMessage;
import com.jagornet.dhcpv6.option.base.BaseDhcpOption;
import com.jagornet.dhcpv6.util.DhcpConstants;

/**
 * <p>Title: DhcpRelayOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpRelayOption extends BaseDhcpOption
{    
    
    /** The NAME. */
    protected static String NAME = "Relay Message";
    
    // the DhcpRelayMessage which contains
    // this DhcpRelayOption
    /** The relay message. */
    protected DhcpRelayMessage relayMessage;
    
    // the DhcpMessage contained in this relay option, 
    // which may itself be a DhcpRelayMessage, 
    // thus containing yet another DhcpRelayOption
    /** The dhcp message being relayed. */
    protected DhcpMessage dhcpMessage;
    
    /**
     * Instantiates a new dhcp relay option.
     */
    public DhcpRelayOption()
    {
    }
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getLength()
     */
    public int getLength()
    {
        // the length of this option is the length of the contained message
        return dhcpMessage.getLength();
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Encodable#encode()
     */
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = super.encodeCodeAndLength();
        buf.put(dhcpMessage.encode());
        return (ByteBuffer) buf.flip();
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.Decodable#decode(java.nio.ByteBuffer)
     */
    public void decode(ByteBuffer buf) throws IOException
    {
    	super.decodeLength(buf);
        // since the linkAddr of the relay message is the interface
        // on which the relay itself received the message to be forwarded
        // we can assume that address is logically a server port
        InetSocketAddress relayMsgLocalAddr = 
        	new InetSocketAddress(relayMessage.getLinkAddress(),
        			DhcpConstants.SERVER_PORT);
        // the peerAddr of the relay message is the source address of
        // the message which it received and is forwarding, thus the
        // peerAddress is either another relay or the client itself,
        // so we don't really know what port to logically set for the
        // inner message's remote port
        InetSocketAddress relayMsgRemoteAddr =
        	new InetSocketAddress(relayMessage.getPeerAddress(), 0);
        dhcpMessage = DhcpMessage.decode(buf, relayMsgLocalAddr, relayMsgRemoteAddr);
    }

    /**
     * Gets the relay message.
     * 
     * @return the relay message
     */
    public DhcpRelayMessage getRelayMessage() {
		return relayMessage;
	}

	/**
	 * Sets the relay message.
	 * 
	 * @param relayMessage the new relay message
	 */
	public void setRelayMessage(DhcpRelayMessage relayMessage) {
		this.relayMessage = relayMessage;
	}

	/**
	 * Gets the dhcp message.
	 * 
	 * @return the dhcp message
	 */
    public DhcpMessage getDhcpMessage()
    {
        return dhcpMessage;
    }

    /**
     * Sets the dhcp message.
     * 
     * @param relayMessage the relay message
     */
    public void setDhcpMessage(DhcpMessage relayMessage)
    {
        this.dhcpMessage = relayMessage;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return DhcpConstants.OPTION_RELAY_MSG;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.BaseDhcpOption#getName()
     */
    public String getName()
    {
        return NAME;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        if (dhcpMessage == null)
            return null;
        
        StringBuilder sb = new StringBuilder(NAME);
        sb.append(": ");
        sb.append(dhcpMessage.toString());
        return sb.toString();
    }
}
