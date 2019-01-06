/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6Message.java is part of Jagornet DHCP.
 *
 *   Jagornet DHCP is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Jagornet DHCP is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Jagornet DHCP.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcp.server.failover;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jagornet.dhcp.core.message.DhcpMessage;
import com.jagornet.dhcp.core.message.DhcpV6TransactionId;
import com.jagornet.dhcp.core.option.base.DhcpOption;
import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.db.IdentityAssoc;
import com.jagornet.dhcp.server.request.binding.Binding;

/**
 * Title:        DhcpFailoverMessage
 * Description:  Object that represents a proprietary Jagornet DHCP
 * 				 failover message.
 *               
 * The following diagram illustrates the format of Jagornet DHCP
 * failover messages.
 *
 * <pre>
 *     0                   1                   2                   3
 *     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |    msg-type   |               transaction-id                  |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                                                               |
 *    .                              data                             .
 *    .                           (variable)                          .
 *    |                                                               |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *
 *    msg-type             Identifies the DHCP message type; the
 *                         available message types are listed in
 *                         section 5.3.
 *
 *    transaction-id       The transaction ID for this message exchange.
 *
 *    options              Data dependent on the msg-type
 *
 * </pre>
 * 
 * @author A. Gregory Rabil
 */

public class FailoverMessage implements DhcpMessage
{
    private static Logger log = LoggerFactory.getLogger(FailoverMessage.class);
    
    protected static DhcpServerConfiguration serverConfig = DhcpServerConfiguration.getInstance();
    
	protected Gson gson = new GsonBuilder().setPrettyPrinting().create();

	// the IP and port on the local host on 
    // which the message is sent or received
    protected InetSocketAddress localAddress;
    
    // the IP and port on the remote host from
    // which the message is received or sent
    protected InetSocketAddress remoteAddress;
    
    protected short messageType = 0;	// need a short to hold unsigned byte
    protected int transactionId = 0;   	// we only use low order three bytes
    
    protected ByteBuffer failoverData;
    protected Binding binding;

    /**
     * Construct a FailoverMessage.
     * 
     * @param localAddress	InetSocketAddress on the local host on which
     * 						this message is received or sent
     * @param remoteAddress InetSocketAddress on the remote host on which
     * 						this message is sent or received
     */
    public FailoverMessage(InetSocketAddress localAddress, InetSocketAddress remoteAddress)
    {
    	this.localAddress = localAddress;
    	this.remoteAddress = remoteAddress;
    }
    
    /**
     * Encode this FailoverMessage to wire format for sending.
     * 
     * @return	a ByteBuffer containing the encoded FailoverMessage
     * @throws IOException
     */
    public ByteBuffer encode() throws IOException
    {
        if (log.isDebugEnabled())
            log.debug("Encoding FailoverMessage for: " + 
            		Util.socketAddressAsString(remoteAddress));
        
        ByteBuffer buf = ByteBuffer.allocate(1024);
        buf.put((byte)messageType);
        buf.put(DhcpV6TransactionId.encode(transactionId));
        buf.put(encodeData());
        buf.flip();
        
        if (log.isDebugEnabled())
            log.debug("FailoverMessage encoded.");
        
        return buf;
    }

    /**
     * Encode the options of this FailoverMessage to wire format for sending.
     * 
     * @return	a ByteBuffer containing the encoded options
     * @throws IOException
     */
    protected ByteBuffer encodeData() throws IOException
    {
    	ByteBuffer buf = ByteBuffer.allocate(1020); // 1024 - 1(msgType) - 3(transId) = 1020 (options)
        if (failoverData != null) {
        	buf.put(failoverData);
        }
        return (ByteBuffer)buf.flip();
    }

    /**
     * Decode a packet received on the wire into a DhcpMessage object.
     * 
     * @param buf			ByteBuffer containing the packet to be decoded
     * @param localAddr		InetSocketAddress on the local host on which
     * 						packet was received
     * @param remoteAddr	InetSocketAddress on the remote host from which
     * 						the packet was received
     * @return	a decoded DhcpMessage object, or null if the packet could not be decoded
     * @throws IOException
     */
	public static FailoverMessage decode(ByteBuffer buf, InetSocketAddress localAddr, InetSocketAddress remoteAddr)
			throws IOException
	{
		FailoverMessage failoverMessage = null;		
		if ((buf != null) && buf.hasRemaining()) {

			if (log.isDebugEnabled()) {
	            log.debug("Decoding packet:" + 
	            		" size=" + buf.limit() +
	            		" localAddr=" + Util.socketAddressAsString(localAddr) +
	            		" remoteAddr=" + Util.socketAddressAsString(remoteAddr));
			}

            // we'll "peek" at the message type to use for this mini-factory
			buf.mark();
            byte msgtype = buf.get();
            if (log.isDebugEnabled())
                log.debug("Message type byte=" + msgtype);
            
            failoverMessage = new FailoverMessage(localAddr, remoteAddr);
            
            if (failoverMessage != null) {
            	// reset the buffer to point at the message type byte
            	// because the message decoder will expect it
            	buf.reset();
                failoverMessage.decode(buf);
            }
        }
        else {
            String errmsg = "Buffer is null or empty";
            log.error(errmsg);
            throw new IOException(errmsg);
        }
		return failoverMessage;
	}

	/**
	 * Decode a datagram packet into this DhcpMessage object.
	 *  
	 * @param buf	ByteBuffer containing the packet to be decoded
	 * @throws IOException
	 */
    public void decode(ByteBuffer buf) throws IOException
    {
        if (log.isDebugEnabled())
            log.debug("Decoding FailoverMessage from: " + 
            		Util.socketAddressAsString(remoteAddress));
        
        if ((buf != null) && buf.hasRemaining()) {
            decodeMessageType(buf);
            if (buf.hasRemaining()) {
                setTransactionId(DhcpV6TransactionId.decode(buf));
                if (log.isDebugEnabled())
        			log.debug("TransactionId=" + transactionId);
                if (buf.hasRemaining()) {
                	switch (getMessageType()) {
                		case FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPBNDADD:
                		case FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPBNDUPD:
                		case FailoverConstants.FAILOVER_MESSAGE_TYPE_DHCPBNDDEL:
                			binding = decodeBinding(buf);
                		default:
                			failoverData = ByteBuffer.allocate(buf.limit());
                			failoverData.put(buf);                	
                	}
                }
                else {
                    String errmsg = "Failed to decode options: buffer is empty";
                    log.error(errmsg);
                    throw new IOException(errmsg);
                }
            }
            else {
                String errmsg = "Failed to decode transaction id: buffer is empty";
                log.error(errmsg);
                throw new IOException(errmsg);
            }
        }
        else {
            String errmsg = "Failed to decode message: buffer is empty";
            log.error(errmsg);
            throw new IOException(errmsg);
        }
        
        if (log.isDebugEnabled()) {
            log.debug("DhcpMessage decoded.");
        }
    }

    /**
     * Decode the message type.
     * 
     * @param buf	ByteBuffer positioned at the message type in the packet
     * @throws IOException
     */
    protected void decodeMessageType(ByteBuffer buf) throws IOException
    {
        if ((buf != null) && buf.hasRemaining()) {
            setMessageType(Util.getUnsignedByte(buf));
            if (log.isDebugEnabled())
                log.debug("MessageType=" + FailoverConstants.getFailoverMessageString(messageType));
        }
        else {
            String errmsg = "Failed to decode message type: buffer is empty";
            log.error(errmsg);
            throw new IOException(errmsg);
        }
    }
    
    /**
     * Decode the binding
     * @param buf	ByteBuffer positioned at the start of the binding in the packet
     * @return	a Map of DhcpOptions keyed by the option code
     * @throws IOException
     */
    protected Binding decodeBinding(ByteBuffer buf) throws IOException
    {
    	Binding binding = null;
        try {
    		IdentityAssoc ia = gson.fromJson(buf.asCharBuffer().toString(), IdentityAssoc.class);
    		//TODO: null checks for ia.getIaAddresses().iterator...
    		DhcpLink dhcpLink = serverConfig.findLinkForAddress(ia.getIaAddresses().iterator().next().getIpAddress());
    		binding = new Binding(ia, dhcpLink);
        }
        catch (Exception ex) {
        	String errmsg = "Exception decoding binding: " + ex.getMessage();
        	log.error(errmsg);
        	throw new IOException(errmsg);
        }
        return binding;
    }

    /**
     * Return the length of this FailoverMessage in bytes.
     * @return	an int containing a length of a least four(4)
     */
    public int getLength()
    {
        int len = 4;    // msg type (1) + transaction id (3)
        len += getBindingLength();
        return len;
    }
    
    /**
     * Get the length of the options in this FailoverMessage in bytes.
     * @return	an int containing the total length of all options
     */
    protected int getBindingLength()
    {
        int len = 0;
//        if (dhcpOptions != null) {
//            for (DhcpOption option : dhcpOptions.values()) {
//                len += 4;   // option code (2 bytes) + length (2 bytes) 
//                len += option.getLength();
//            }
//        }
        return len;
    }
        
	public InetSocketAddress getLocalAddress() {
		return localAddress;
	}

	public void setLocalAddress(InetSocketAddress localAddress) {
		this.localAddress = localAddress;
	}

	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(InetSocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public short getMessageType()
    {
        return messageType;
    }
    public void setMessageType(short messageType)
    {
        this.messageType = messageType;
    }
    public int getTransactionId()
    {
        return transactionId;
    }
    public void setTransactionId(int transactionId)
    {
        this.transactionId = transactionId;
    }
	
	public ByteBuffer getFailoverData() {
		return failoverData;
	}

	public void setFailoverData(ByteBuffer failoverData) {
		this.failoverData = failoverData;
	}

	public Binding getBinding() {
		return binding;
	}

	public void setBinding(Binding binding) {
		this.binding = binding;
	}

	public String toString()
    {
        StringBuilder sb = new StringBuilder(Util.LINE_SEPARATOR);
        sb.append(FailoverConstants.getFailoverMessageString(getMessageType()));
        sb.append(" (xactId=");
        sb.append(getTransactionId());
        sb.append(')');
        sb.append(" to/from ");
        sb.append(Util.socketAddressAsString(remoteAddress));
        return sb.toString();
    }
    
    public String toStringWithOptions()
    {
        StringBuilder sb = new StringBuilder(this.toString());
//        if ((dhcpOptions != null) && !dhcpOptions.isEmpty()) {
//            sb.append(Util.LINE_SEPARATOR);
//        	sb.append("MSG_DHCPOPTIONS");
//        	for (DhcpOption dhcpOption : dhcpOptions.values()) {
//				sb.append(dhcpOption.toString());
//			}
//        }
        return sb.toString();
    }

	@Override
	public DhcpOption getDhcpOption(int optionCode) {
		// TODO Auto-generated method stub
		return null;
	}
}
