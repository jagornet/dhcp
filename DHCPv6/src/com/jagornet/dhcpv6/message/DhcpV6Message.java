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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6ClientIdOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6IaNaOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6IaPdOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6IaTaOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6OptionFactory;
import com.jagornet.dhcpv6.option.v6.DhcpV6OptionRequestOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6RelayOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6ServerIdOption;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.util.Util;

/**
 * Title:        DhcpV6Message
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

public class DhcpV6Message implements DhcpMessage
{
    private static Logger log = LoggerFactory.getLogger(DhcpV6Message.class);
    
    // true if the message was received on a unicast socket, false otherwise
    // note that a DhcpRelayMessage (subclass) may be unicast, but the "inner"
    // DhcpMessage will _not_ be unicast, which is the desired behavior
    protected boolean unicast;
    
    // the IP and port on the local host on 
    // which the message is sent or received
    protected InetSocketAddress localAddress;
    
    // the IP and port on the remote host from
    // which the message is received or sent
    protected InetSocketAddress remoteAddress;
    
    protected short messageType = 0;	// need a short to hold unsigned byte
    protected int transactionId = 0;   	// we only use low order three bytes
    protected Map<Integer, DhcpOption> dhcpOptions = new HashMap<Integer, DhcpOption>();
    protected List<DhcpV6IaNaOption> iaNaOptions = new ArrayList<DhcpV6IaNaOption>();
    protected List<DhcpV6IaTaOption> iaTaOptions = new ArrayList<DhcpV6IaTaOption>();
    protected List<DhcpV6IaPdOption> iaPdOptions = new ArrayList<DhcpV6IaPdOption>();

    /**
     * Construct a DhcpMessage.
     * 
     * @param localAddress	InetSocketAddress on the local host on which
     * 						this message is received or sent
     * @param remoteAddress InetSocketAddress on the remote host on which
     * 						this message is sent or received
     */
    public DhcpV6Message(InetSocketAddress localAddress, InetSocketAddress remoteAddress)
    {
    	this.localAddress = localAddress;
    	this.remoteAddress = remoteAddress;
    }
    
    /**
     * Set the unicast flag for this message.
     * 
     * @param unicast
     */
    public void setUnicast(boolean unicast)
    {
    	this.unicast = unicast;
    }
    
    /**
     * Check if this message was received via unicast.
     * 
     * @return true if unicast message, false otherwise
     */
    public boolean isUnicast()
    {
    	return unicast;
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
            		Util.socketAddressAsString(remoteAddress));
        
        ByteBuffer buf = ByteBuffer.allocate(1024);
        buf.put((byte)messageType);
        buf.put(DhcpTransactionId.encode(transactionId));
        buf.put(encodeOptions());
        buf.flip();
        
        if (log.isDebugEnabled())
            log.debug("DhcpMessage encoded.");
        
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
        if (iaNaOptions != null) {
        	for (DhcpV6IaNaOption iaNaOption : iaNaOptions) {
				buf.put(iaNaOption.encode());
			}
        }
        if (iaTaOptions != null) {
        	for (DhcpV6IaTaOption iaTaOption : iaTaOptions) {
				buf.put(iaTaOption.encode());
			}
        }
        if (iaPdOptions != null) {
        	for (DhcpV6IaPdOption iaPdOption : iaPdOptions) {
				buf.put(iaPdOption.encode());
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
	public static DhcpV6Message decode(ByteBuffer buf, InetSocketAddress localAddr, InetSocketAddress remoteAddr)
			throws IOException
	{
		DhcpV6Message dhcpMessage = null;		
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

            if ( (msgtype >= DhcpConstants.SOLICIT) &&
            		(msgtype <= DhcpConstants.INFO_REQUEST) ) {
            	dhcpMessage = new DhcpV6Message(localAddr, remoteAddr);
            }
            else if ( (msgtype >= DhcpConstants.RELAY_FORW) &&
            		(msgtype <= DhcpConstants.RELAY_REPL) ) {
            	// note that it doesn't make much sense to be decoding
            	// a relay-reply message unless we implement a relay
            	dhcpMessage = new DhcpV6RelayMessage(localAddr, remoteAddr);
            }
            else {
            	log.error("Unknown message type: " + msgtype);
            }
            
            if (dhcpMessage != null) {
            	// reset the buffer to point at the message type byte
            	// because the message decoder will expect it
            	buf.reset();
                dhcpMessage.decode(buf);
            }
        }
        else {
            String errmsg = "Buffer is null or empty";
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
            		Util.socketAddressAsString(remoteAddress));
        
        if ((buf != null) && buf.hasRemaining()) {
            decodeMessageType(buf);
            if (buf.hasRemaining()) {
                setTransactionId(DhcpTransactionId.decode(buf));
                log.debug("TransactionId=" + transactionId);
                if (buf.hasRemaining()) {
                    decodeOptions(buf);
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
        while (buf.hasRemaining()) {
            int code = Util.getUnsignedShort(buf);
            log.debug("Option code=" + code);
            DhcpOption option = DhcpV6OptionFactory.getDhcpOption(code);
            if (option != null) {
            	if ((option instanceof DhcpV6RelayOption) &&
            			(this instanceof DhcpV6RelayMessage)) {
            		DhcpV6RelayOption relayOption = (DhcpV6RelayOption) option;
            		relayOption.setRelayMessage((DhcpV6RelayMessage)this);
            	}
                option.decode(buf);
                if (option instanceof DhcpV6IaNaOption) {
                	iaNaOptions.add((DhcpV6IaNaOption)option);
                }
                else if (option instanceof DhcpV6IaTaOption) {
                	iaTaOptions.add((DhcpV6IaTaOption)option);
                }
                else if (option instanceof DhcpV6IaPdOption) {
                	iaPdOptions.add((DhcpV6IaPdOption)option);
                } 
                else {
                	dhcpOptions.put(option.getCode(), option);
                }
            }
            else {
                break;  // no more options, or one is malformed, so we're done
            }
        }
        return dhcpOptions;
    }

    /**
     * Return the length of this DhcpMessage in bytes.
     * @return	an int containing a length of a least four(4)
     */
    public int getLength()
    {
        int len = 4;    // msg type (1) + transaction id (3)
        len += getOptionsLength();
        len += getIaNaOptionsLength();
        len += getIaTaOptionsLength();
        len += getIaPdOptionsLength();
        return len;
    }
    
    /**
     * Get the length of the options in this DhcpMessage in bytes.
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
    
    /**
     * Get the length of the IA_NA options in this DhcpMessage in bytes.
     * @return an int containing the total length of all IA_NA options
     */
    protected int getIaNaOptionsLength()
    {
        int len = 0;
        if (iaNaOptions != null) {
            for (DhcpV6IaNaOption option : iaNaOptions) {
                len += 4;   // option code (2 bytes) + length (2 bytes) 
                len += option.getLength();
            }
        }
        return len;
    }
    
    /**
     * Get the length of the IA_TA options in this DhcpMessage in bytes.
     * @return an int containing the total length of all IA_TA options
     */
    protected int getIaTaOptionsLength()
    {
        int len = 0;
        if (iaTaOptions != null) {
            for (DhcpV6IaTaOption option : iaTaOptions) {
                len += 4;   // option code (2 bytes) + length (2 bytes) 
                len += option.getLength();
            }
        }
        return len;
    }
    
    /**
     * Return the length of the IA_PD options in this DhcpMessage in bytes.
     * @return an int containing the total length of all IA_PD options
     */
    protected int getIaPdOptionsLength()
    {
        int len = 0;
        if (iaPdOptions != null) {
            for (DhcpV6IaPdOption option : iaPdOptions) {
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

    public DhcpOption getDhcpOption(int optionCode)
    {
        return dhcpOptions.get(optionCode);
    }
    public void putDhcpOption(DhcpOption dhcpOption)
    {
        if(dhcpOption != null) {
            dhcpOptions.put(dhcpOption.getCode(), dhcpOption);
        }
    }
    public void putAllDhcpOptions(Map<Integer, DhcpOption> dhcpOptions)
    {
    	this.dhcpOptions.putAll(dhcpOptions);
    }
    
    public Map<Integer, DhcpOption> getDhcpOptionMap()
    {
        return dhcpOptions;
    }
    public void setDhcpOptionMap(Map<Integer, DhcpOption> dhcpOptions)
    {
        this.dhcpOptions = dhcpOptions;
    }

    public Collection<DhcpOption> getDhcpOptions()
    {
        return dhcpOptions.values();
    }
        
    public List<DhcpV6IaNaOption> getIaNaOptions() {
		return iaNaOptions;
	}
    
    public void setIaNaOptions(List<DhcpV6IaNaOption> iaNaOptions) {
    	this.iaNaOptions = iaNaOptions;
    }
    
    public void addIaNaOption(DhcpV6IaNaOption iaNaOption) {
    	if (iaNaOptions == null) {
    		iaNaOptions = new ArrayList<DhcpV6IaNaOption>();
    	}
    	iaNaOptions.add(iaNaOption);
    }

	public List<DhcpV6IaTaOption> getIaTaOptions() {
		return iaTaOptions;
	}
    
    public void setIaTaOptions(List<DhcpV6IaTaOption> iaTaOptions) {
    	this.iaTaOptions = iaTaOptions;
    }
    
    public void addIaTaOption(DhcpV6IaTaOption iaTaOption) {
    	if (iaTaOptions == null) {
    		iaTaOptions = new ArrayList<DhcpV6IaTaOption>();
    	}
    	iaTaOptions.add(iaTaOption);
    }

	public List<DhcpV6IaPdOption> getIaPdOptions() {
		return iaPdOptions;
	}
    
    public void setIaPdOptions(List<DhcpV6IaPdOption> iaPdOptions) {
    	this.iaPdOptions = iaPdOptions;
    }
    
    public void addIaPdOption(DhcpV6IaPdOption iaPdOption) {
    	if (iaPdOptions == null) {
    		iaPdOptions = new ArrayList<DhcpV6IaPdOption>();
    	}
    	iaPdOptions.add(iaPdOption);
    }
	

	private DhcpV6ClientIdOption dhcpClientIdOption;
	/**
	 * Convenience method to get ClientID option.
	 * @return
	 */
	public DhcpV6ClientIdOption getDhcpClientIdOption() {
		if (dhcpClientIdOption == null) {
			if (dhcpOptions != null) {
				dhcpClientIdOption = 
					(DhcpV6ClientIdOption) dhcpOptions.get(DhcpConstants.OPTION_CLIENTID);
			}
		}
		return dhcpClientIdOption;
	}
	
	private DhcpV6ServerIdOption dhcpServerIdOption;
	/**
	 * Convenience method to get ServerID option.
	 * @return
	 */
	public DhcpV6ServerIdOption getDhcpServerIdOption() {
		if (dhcpServerIdOption == null) {
			if (dhcpOptions != null) {
				dhcpServerIdOption = 
					(DhcpV6ServerIdOption) dhcpOptions.get(DhcpConstants.OPTION_SERVERID);
			}
		}
		return dhcpServerIdOption;
	}

	private List<Integer> requestedOptionCodes;
	/**
	 * Convenience method to get the requested option codes.
	 * @return
	 */
	public List<Integer> getRequestedOptionCodes() {
		if (requestedOptionCodes == null) {
			if (dhcpOptions != null) {
	        	DhcpV6OptionRequestOption oro = 
	        		(DhcpV6OptionRequestOption) dhcpOptions.get(DhcpConstants.OPTION_ORO);
	        	if (oro != null) {
        			requestedOptionCodes = oro.getUnsignedShortList();
	        	}
			}
		}
		return requestedOptionCodes;
	}
	
	public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(DhcpConstants.getMessageString(getMessageType()));
        sb.append(" (xactId=");
        sb.append(getTransactionId());
        sb.append(')');
        if ((this.messageType == DhcpConstants.ADVERTISE) ||
        		(this.messageType == DhcpConstants.REPLY) ||
        		(this.messageType == DhcpConstants.RECONFIGURE))
        	sb.append(" to ");
        else
        	sb.append(" from ");
        sb.append(Util.socketAddressAsString(remoteAddress));
        return sb.toString();
    }
    
    public String toStringWithOptions()
    {
        StringBuffer sb = new StringBuffer(this.toString());
        if ((dhcpOptions != null) && !dhcpOptions.isEmpty()) {
            sb.append(Util.LINE_SEPARATOR);
        	sb.append("MSG_DHCPOPTIONS");
        	for (DhcpOption dhcpOption : dhcpOptions.values()) {
				sb.append(dhcpOption.toString());
			}
        }
        if ((iaNaOptions != null) && !iaNaOptions.isEmpty()) {
        	sb.append(Util.LINE_SEPARATOR);
        	sb.append("IA_NA_OPTIONS");
        	for (DhcpV6IaNaOption iaNaOption : iaNaOptions) {
        		sb.append(iaNaOption.toString());
        	}
        }
        if ((iaTaOptions != null) && !iaTaOptions.isEmpty()) {
        	sb.append(Util.LINE_SEPARATOR);
        	sb.append("IA_TA_OPTIONS");
        	for (DhcpV6IaTaOption iaTaOption : iaTaOptions) {
        		sb.append(iaTaOption.toString());
        	}
        }
        if ((iaPdOptions != null) && !iaPdOptions.isEmpty()) {
        	sb.append(Util.LINE_SEPARATOR);
        	sb.append("IA_PD_OPTIONS");
        	for (DhcpV6IaPdOption iaPdOption : iaPdOptions) {
        		sb.append(iaPdOption.toString());
        	}
        }
        return sb.toString();
    }
}
