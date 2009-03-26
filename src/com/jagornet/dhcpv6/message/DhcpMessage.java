/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpMessage.java is part of DHCPv6.
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
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.option.DhcpOptionFactory;
import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.util.DhcpConstants;

/**
 * Title:        DHCPMessage
 * Description:  Object that represents a DHCPv6 message as defined in
 *               RFC 3315.
 *               
 * The following diagram illustrates the format of DHCP messages sent
 * between clients and servers:
 *
 * <pre>
 *     0                   1                   2                   3
 *     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |    msg-type   |               transaction-id                  |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                                                               |
 *    .                            options                            .
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
 *    options              Options carried in this message; options are
 *                         described in section 22.
 *
 *   The format of DHCP options is:

 *     0                   1                   2                   3
 *     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |          option-code          |           option-len          |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                          option-opaqueData                          |
 *    |                      (option-len octets)                      |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * 
 *    option-code   An unsigned integer identifying the specific option
 *                  type carried in this option.
 * 
 *    option-len    An unsigned integer giving the length of the
 *                  option-opaqueData field in this option in octets.
 * 
 *    option-opaqueData   The opaqueData for the option; the format of this opaqueData
 *                  depends on the definition of the option.
 * </pre>
 * 
 * @author A. Gregory Rabil
 */

public class DhcpMessage
{
    private static Logger log = LoggerFactory.getLogger(DhcpMessage.class);
    
    protected static DhcpMessageFactory msgFactory = DhcpMessageFactory.getInstance();
    
    // the IP and port on the local host on 
    // which the message is sent or received
    protected InetSocketAddress localAddress;
    
    // the IP and port on the remote host from
    // which the message is received or sent
    protected InetSocketAddress remoteAddress;
    
    protected short messageType = 0;	// need a short to hold unsigned byte
    protected int transactionId = 0;   	// we only use low order three bytes
    protected Map<Integer, DhcpOption> dhcpOptions = null;

    /**
     * Construct a DhcpMessage.
     * 
     * @param localAddress	InetSocketAddress on the local host on which
     * 						this message is received or sent
     * @param remoteAddress InetSocketAddress on the remote host on which
     * 						this message is sent or received
     */
    public DhcpMessage(InetSocketAddress localAddress, InetSocketAddress remoteAddress)
    {
    	this.localAddress = localAddress;
    	this.remoteAddress = remoteAddress;
        dhcpOptions = new HashMap<Integer, DhcpOption>();
    }
    
    /**
     * Encode this DhcpMessage to wire format for sending.
     * 
     * @return	a ByteBuffer containing the encoded DhcpMessage
     * @throws IOException
     */
    public ByteBuffer encode() throws IOException
    {
        if (log.isDebugEnabled())
            log.debug("Encoding DhcpMessage for: " + 
            		socketAddressAsString(remoteAddress));
        
        long s = System.currentTimeMillis();
        
        ByteBuffer buf = ByteBuffer.allocate(1024);
        buf.put((byte)messageType);
        buf.put(DhcpTransactionId.encode(transactionId));
        buf.put(encodeOptions());
        buf.flip();
        
        if (log.isDebugEnabled())
            log.debug("DhcpMessage encoded in " +
                    String.valueOf(System.currentTimeMillis()-s) + " ms.");
        
        return buf;
    }

    /**
     * Encode the options of this DhcpMessage to wire format for sending.
     * 
     * @return	a ByteBuffer containing the encoded options
     * @throws IOException
     */
    protected ByteBuffer encodeOptions() throws IOException
    {
    	ByteBuffer buf = ByteBuffer.allocate(1020); // 1024 - 1(msgType) - 3(transId) = 1020 (options)
        if (dhcpOptions != null) {
            for (DhcpOption option : dhcpOptions.values()) {
                 buf.put(option.encode());
            }
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
	public static DhcpMessage decode(ByteBuffer buf, InetSocketAddress localAddr, InetSocketAddress remoteAddr)
			throws IOException
	{
		DhcpMessage dhcpMessage = null;		
		if ((buf != null) && buf.hasRemaining()) {

			if (log.isDebugEnabled()) {
	            log.debug("Decoding DhcpMessage:" + 
	            		" localAddr=" + socketAddressAsString(localAddr) +
	            		" remoteAddr=" + socketAddressAsString(remoteAddr));
			}

            // get the message type byte to see if we can handle it
            byte msgtype = buf.get();
            if (log.isDebugEnabled())
                log.debug("MessageType=" + DhcpConstants.getMessageString(msgtype));

            // get either a DhcpMessage or a DhcpRelayMessage object
            dhcpMessage = msgFactory.getDhcpMessage(msgtype, localAddr, remoteAddr);
            if (dhcpMessage != null) {
                // rewind buffer so DhcpMessage decoder will read it and 
                // set the message type - we only read it ourselves so 
                // that we could use it for the factory.
                buf.rewind();
                dhcpMessage.decode(buf);
            }
        }
        else {
            String errmsg = "IoBuffer is null or empty";
            log.error(errmsg);
            throw new IOException(errmsg);
        }
		return dhcpMessage;
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
            log.debug("Decoding DhcpMessage from: " + 
            		socketAddressAsString(remoteAddress));
        
        long s = System.currentTimeMillis();
        
        if ((buf != null) && buf.hasRemaining()) {
            decodeMessageType(buf);
            if (buf.hasRemaining()) {
                setTransactionId(DhcpTransactionId.decode(buf));
                log.debug("TransactionId=" + transactionId);
                if (buf.hasRemaining()) {
                    setDhcpOptions(decodeOptions(buf));
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
            log.debug("DhcpMessage decoded in " +
                      String.valueOf(System.currentTimeMillis()-s) + " ms.");
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
        	IoBuffer iobuf = IoBuffer.wrap(buf);
            // TODO check that it is a valid message type, even though
            //      this should have already been done
            setMessageType(iobuf.getUnsigned());
            if (log.isDebugEnabled())
                log.debug("MessageType=" + DhcpConstants.getMessageString(messageType));
        }
        else {
            String errmsg = "Failed to decode message type: buffer is empty";
            log.error(errmsg);
            throw new IOException(errmsg);
        }
    }
    
    /**
     * Decode the options.
     * @param buf	ByteBuffer positioned at the start of the options in the packet
     * @return	a Map of DhcpOptions keyed by the option code
     * @throws IOException
     */
    protected Map<Integer, DhcpOption> decodeOptions(ByteBuffer buf) 
            throws IOException
    {
    	IoBuffer iobuf = IoBuffer.wrap(buf);
        Map<Integer, DhcpOption> _dhcpOptions = new HashMap<Integer, DhcpOption>();
        while (iobuf.hasRemaining()) {
            int code = iobuf.getUnsignedShort();
            log.debug("Option code=" + code);
            DhcpOption option = DhcpOptionFactory.getDhcpOption(code);
            if (option != null) {
                option.setDhcpMessage(this);
                option.decode(iobuf.buf());
                _dhcpOptions.put(option.getCode(), option);
            }
            else {
                break;  // no more options, or one is malformed, so we're done
            }
        }
        return _dhcpOptions;
    }

    /**
     * Return the length of this DhcpMessage in bytes.
     * @return	an int containing a length of a least four(4)
     */
    public int getLength()
    {
        int len = 4;    // msg type (1) + transaction id (3)
        len += getOptionsLength();
        return len;
    }
    
    /**
     * Return the length of the options in this DhcpMessage in bytes.
     * @return	an int containing the total length of all options
     */
    protected int getOptionsLength()
    {
        int len = 0;
        if (dhcpOptions != null) {
            for (DhcpOption option : dhcpOptions.values()) {
                len += 4;   // option code (2 bytes) + length (2 bytes) 
                len += option.getLength();
            }
        }
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

    public boolean hasOption(int optionCode)
    {
        if(dhcpOptions.containsKey(optionCode)) {
            return true;
        }
        return false;
    }

    public DhcpOption getOption(int optionCode)
    {
        return (DhcpOption)dhcpOptions.get(optionCode);
    }
    public void setOption(DhcpOption dhcpOption)
    {
        if(dhcpOption != null) {
        	dhcpOption.setDhcpMessage(this);
            dhcpOptions.put(dhcpOption.getCode(), dhcpOption);
        }
    }
    
    public Map<Integer, DhcpOption> getDhcpOptions()
    {
        return dhcpOptions;
    }
    public void setDhcpOptions(Map<Integer, DhcpOption> dhcpOptions)
    {
        this.dhcpOptions = dhcpOptions;
    }

    public Collection<DhcpOption> getOptions()
    {
        return dhcpOptions.values();
    }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(DhcpConstants.getMessageString(getMessageType()));
        sb.append(" (xactId=");
        sb.append(getTransactionId());
        sb.append(")");
        if (this.messageType == DhcpConstants.REPLY)
        	sb.append(" to ");
        else
        	sb.append(" from ");
        sb.append(socketAddressAsString(remoteAddress));
        return sb.toString();
    }
    
    public String toStringWithOptions()
    {
        StringBuffer sb = new StringBuffer(this.toString());
        Collection<DhcpOption> options = getOptions();
        if (options != null) {
            sb.append("\nOptions:");
            for (DhcpOption option : options) {
                sb.append("\n");
                sb.append(option.toString());
            }
        }
        return sb.toString();
    }
	
	public static String socketAddressAsString(InetSocketAddress saddr) {
		if (saddr == null)
			return null;
		else
			return saddr.getAddress().getHostAddress() +
					":" + saddr.getPort();
	}
	
	public static DhcpMessageFactory getDhcpMessageFactory() {
		return msgFactory;
	}
}
