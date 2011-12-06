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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.option.DhcpOptionRequestOption;
import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.option.v4.DhcpV4MsgTypeOption;
import com.jagornet.dhcpv6.option.v4.DhcpV4OptionFactory;
import com.jagornet.dhcpv6.option.v4.DhcpV4ServerIdOption;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.UnsignedShortListOptionType;

/**
 * Title:        DhcpV4Message
 * Description:  Object that represents a DHCPv4 message as defined in
 *               RFC 2131.
 *               
 * The following diagram illustrates the format of DHCP messages sent
 * between clients and servers:
 *
 * <pre>
 *   0                   1                   2                   3
 *   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *   |     op (1)    |   htype (1)   |   hlen (1)    |   hops (1)    |
 *   +---------------+---------------+---------------+---------------+
 *   |                            xid (4)                            |
 *   +-------------------------------+-------------------------------+
 *   |           secs (2)            |           flags (2)           |
 *   +-------------------------------+-------------------------------+
 *   |                          ciaddr  (4)                          |
 *   +---------------------------------------------------------------+
 *   |                          yiaddr  (4)                          |
 *   +---------------------------------------------------------------+
 *   |                          siaddr  (4)                          |
 *   +---------------------------------------------------------------+
 *   |                          giaddr  (4)                          |
 *   +---------------------------------------------------------------+
 *   |                                                               |
 *   |                          chaddr  (16)                         |
 *   |                                                               |
 *   |                                                               |
 *   +---------------------------------------------------------------+
 *   |                                                               |
 *   |                          sname   (64)                         |
 *   +---------------------------------------------------------------+
 *   |                                                               |
 *   |                          file    (128)                        |
 *   +---------------------------------------------------------------+
 *   |                                                               |
 *   |                          options (variable)                   |
 *   +---------------------------------------------------------------+
 * </pre>
 * 
 * @author A. Gregory Rabil
 */

public class DhcpV4Message implements DhcpMessageInterface
{
    private static Logger log = LoggerFactory.getLogger(DhcpV4Message.class);
    
    // true if the message was received on a unicast socket, false otherwise
    protected boolean unicast;
    
    // the IP and port on the local host on 
    // which the message is sent or received
    protected InetSocketAddress localAddress;
    
    // the IP and port on the remote host from
    // which the message is received or sent
    protected InetSocketAddress remoteAddress;
    
    protected short op = 0;	// need a short to hold unsigned byte
    protected short htype = 0;
    protected short hlen = 0;
    protected short hops = 0;
    protected long transactionId = 0;   	// need a long to hold unsigned int
    protected int secs = 0;
    protected int flags = 0;
    protected InetAddress ciAddr;
    protected InetAddress yiAddr;
    protected InetAddress siAddr;
    protected InetAddress giAddr;
    protected byte[] chAddr;
    protected String sName;
    protected String file;
    protected static byte[] magicCookie = new byte[] { (byte)99, (byte)130, (byte)83, (byte)99 };
    protected Map<Integer, DhcpOption> dhcpOptions = new HashMap<Integer, DhcpOption>();

    /**
     * Construct a DhcpMessage.
     * 
     * @param localAddress	InetSocketAddress on the local host on which
     * 						this message is received or sent
     * @param remoteAddress InetSocketAddress on the remote host on which
     * 						this message is sent or received
     */
    public DhcpV4Message(InetSocketAddress localAddress, InetSocketAddress remoteAddress)
    {
    	this.localAddress = localAddress;
    	this.remoteAddress = remoteAddress;
    }
    
    public void setUnicast(boolean unicast)
    {
    	this.unicast = unicast;
    }
    
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
        buf.put((byte)op);
        buf.put((byte)htype);
        buf.put((byte)hlen);
        buf.put((byte)hops);
        buf.putInt((int)transactionId);
        buf.putShort((short)secs);
        buf.putShort((short)flags);
        if (ciAddr != null) {
        	buf.put(ciAddr.getAddress());
        }
        else {
        	buf.put(new byte[4]);
        }
        if (yiAddr != null) {
        	buf.put(yiAddr.getAddress());
        }
        else {
        	buf.put(new byte[4]);
        }
        if (siAddr != null) {
        	buf.put(siAddr.getAddress());
        }
        else {
        	buf.put(new byte[4]);
        }
        if (giAddr != null) {
        	buf.put(giAddr.getAddress());
        }
        else {
        	buf.put(new byte[4]);
        }
        buf.put(Arrays.copyOf(chAddr, 16));		// pad to 16 bytes for encoded packet
        
        StringBuffer sNameBuf = new StringBuffer();
        if (sName != null) {
        	sNameBuf.append(sName);
        }
        sNameBuf.setLength(64-sNameBuf.length());
        buf.put(sNameBuf.toString().getBytes());
        
        StringBuffer fileBuf = new StringBuffer();
        if (file != null) {
        	fileBuf.append(file);
        }
        fileBuf.setLength(128-fileBuf.length());
        buf.put(fileBuf.toString().getBytes());

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
    	ByteBuffer buf = ByteBuffer.allocate(788); // 788 - 236 = 1020 (options)
        if (dhcpOptions != null) {
        	// magic cookie as per rfc1497
        	buf.put(magicCookie);
        	for (DhcpOption option : dhcpOptions.values()) {
                 buf.put(option.encode());
            }
        	buf.put((byte)DhcpConstants.V4OPTION_EOF);	// end option
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
	public static DhcpV4Message decode(ByteBuffer buf, InetSocketAddress localAddr, InetSocketAddress remoteAddr)
			throws IOException
	{
		DhcpV4Message dhcpMessage = null;		
		if ((buf != null) && buf.hasRemaining()) {

			if (log.isDebugEnabled()) {
	            log.debug("Decoding packet:" + 
	            		" size=" + buf.limit() +
	            		" localAddr=" + Util.socketAddressAsString(localAddr) +
	            		" remoteAddr=" + Util.socketAddressAsString(remoteAddr));
			}

            // we'll "peek" at the message type to use for this mini-factory
			buf.mark();
            byte _op = buf.get();
            if (log.isDebugEnabled())
                log.debug("op byte=" + _op);
            
            // allow for reply(op=2) messages for use by client
            // TODO: see if this is a problem for the server
            if ((_op == 1) || (_op == 2)) {
            	dhcpMessage = new DhcpV4Message(localAddr, remoteAddr);   	
            }
            else {
            	log.error("Unsupported op code: " + _op);
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
            log.debug("Decoding DhcpV4Message from: " + 
            		Util.socketAddressAsString(remoteAddress));
        
        if ((buf != null) && buf.hasRemaining()) {
        	// buffer must be at least the size of the fixed
        	// portion of the DHCPv4 header plus the required
        	// "magic cookie", and at least the message type option
        	// and the end option: 244 = 236 + 4 + 3 + 1
        	if (buf.limit() >= 244) {
	        	op = buf.get();
        		log.debug("op=" + op);
	        	if (op != 1) {
	        		// TODO
	        	}
        		htype = buf.get();
        		log.debug("htype=" + htype);
      			hlen = buf.get();
      			log.debug("hlen=" + hlen);
      			hops = buf.get();
      			log.debug("hops=" + hops);
      			transactionId = buf.getInt();
      			log.debug("xid=" + transactionId);
      			secs = buf.getShort();
      			log.debug("secs=" + secs);
      			flags = buf.getShort();
      			log.debug("flags=" + flags);
      			byte[] ipbuf = new byte[4];
      			buf.get(ipbuf);
      			ciAddr = InetAddress.getByAddress(ipbuf);
      			log.debug("ciaddr=" + ciAddr.getHostAddress());
      			buf.get(ipbuf);
      			siAddr = InetAddress.getByAddress(ipbuf);
      			log.debug("siaddr=" + siAddr.getHostAddress());
      			buf.get(ipbuf);
      			yiAddr = InetAddress.getByAddress(ipbuf);
      			log.debug("yiaddr=" + yiAddr.getHostAddress());
      			buf.get(ipbuf);
      			giAddr = InetAddress.getByAddress(ipbuf);
      			log.debug("giaddr=" + giAddr.getHostAddress());
      			byte[] chbuf = new byte[16];
      			buf.get(chbuf);
      			chAddr = Arrays.copyOf(chbuf, hlen);	// hlen defines len of chAddr
      			log.debug("chaddr=" + Util.toHexString(chAddr));
      			byte[] sbuf = new byte[64];
      			buf.get(sbuf);
      			sName = new String(sbuf);
      			log.debug("sname=" + sName);
      			byte[] fbuf = new byte[128];
      			buf.get(fbuf);
      			file = new String(fbuf);
      			log.debug("file=" + file);
      			byte[] cookieBuf = new byte[4];
      			buf.get(cookieBuf);
      			if (!Arrays.equals(cookieBuf, magicCookie)) {
                    String errmsg = "Failed to decode DHCPv4 message: invalid magic cookie";
                    log.error(errmsg);
                    throw new IOException(errmsg);
      			}
      			decodeOptions(buf);
            }
            else {
                String errmsg = "Failed to decode DHCPv4 message: packet too short";
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
     * Decode the options.
     * @param buf	ByteBuffer positioned at the start of the options in the packet
     * @return	a Map of DhcpOptions keyed by the option code
     * @throws IOException
     */
    protected Map<Integer, DhcpOption> decodeOptions(ByteBuffer buf) 
            throws IOException
    {
        while (buf.hasRemaining()) {
            short code = Util.getUnsignedByte(buf);
            log.debug("Option code=" + code);
            DhcpOption option = DhcpV4OptionFactory.getDhcpOption(code);
            if (option != null) {
                option.decode(buf);
                dhcpOptions.put(option.getCode(), option);
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
    	// portion of the DHCPv4 header plus the required
    	// "magic cookie", and at least the message type option
    	// and the end option: 244 = 236 + 4 + 3 + 1
    	int len = 244;
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
                len += 2;   // option code (1 byte) + length (1 byte) 
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
	
	private DhcpV4ServerIdOption dhcpServerIdOption;
	/**
	 * Convenience method to get ServerID option.
	 * @return
	 */
	public DhcpV4ServerIdOption getDhcpV4ServerIdOption() {
		if (dhcpServerIdOption == null) {
			if (dhcpOptions != null) {
				dhcpServerIdOption = 
					(DhcpV4ServerIdOption) dhcpOptions.get(DhcpConstants.V4OPTION_SERVERID);
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
	        	DhcpOptionRequestOption oro = 
	        		(DhcpOptionRequestOption) dhcpOptions.get(DhcpConstants.V4OPTION_PARAM_REQUEST_LIST);
	        	if (oro != null) {
	        		UnsignedShortListOptionType ushortListOption = oro.getUnsignedShortListOption();
	        		if (ushortListOption != null) {
	        			requestedOptionCodes = ushortListOption.getUnsignedShortList();
	        		}
	        	}
			}
		}
		return requestedOptionCodes;
	}
	
	public String toString()
    {
        StringBuffer sb = new StringBuffer();
        //TODO
        sb.append(Util.socketAddressAsString(remoteAddress));
        return sb.toString();
    }
    
    public String toStringWithOptions()
    {
        StringBuffer sb = new StringBuffer(this.toString());
        if ((dhcpOptions != null) && !dhcpOptions.isEmpty()) {
            sb.append(Util.LINE_SEPARATOR);
        	sb.append("dhcpOptions");
        	for (DhcpOption dhcpOption : dhcpOptions.values()) {
				sb.append(dhcpOption.toString());
			}
        }
        return sb.toString();
    }

	public short getOp() {
		return op;
	}

	public void setOp(short op) {
		this.op = op;
	}

	public short getHtype() {
		return htype;
	}

	public void setHtype(short htype) {
		this.htype = htype;
	}

	public short getHlen() {
		return hlen;
	}

	public void setHlen(short hlen) {
		this.hlen = hlen;
	}

	public short getHops() {
		return hops;
	}

	public void setHops(short hops) {
		this.hops = hops;
	}

	public long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(long transactionId) {
		this.transactionId = transactionId;
	}

	public int getSecs() {
		return secs;
	}

	public void setSecs(int secs) {
		this.secs = secs;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public InetAddress getCiAddr() {
		return ciAddr;
	}

	public void setCiAddr(InetAddress ciAddr) {
		this.ciAddr = ciAddr;
	}

	public InetAddress getYiAddr() {
		return yiAddr;
	}

	public void setYiAddr(InetAddress yiAddr) {
		this.yiAddr = yiAddr;
	}

	public InetAddress getSiAddr() {
		return siAddr;
	}

	public void setSiAddr(InetAddress siAddr) {
		this.siAddr = siAddr;
	}

	public InetAddress getGiAddr() {
		return giAddr;
	}

	public void setGiAddr(InetAddress giAddr) {
		this.giAddr = giAddr;
	}

	public byte[] getChAddr() {
		return chAddr;
	}

	public void setChAddr(byte[] chAddr) {
		this.chAddr = chAddr;
	}

	public String getsName() {
		return sName;
	}

	public void setsName(String sName) {
		this.sName = sName;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}
	
	public void setMessageType(short msgType) {
		DhcpV4MsgTypeOption msgTypeOption = new DhcpV4MsgTypeOption();
		msgTypeOption.getUnsignedByteOption().setUnsignedByte(msgType);
		putDhcpOption(msgTypeOption);
	}
	
	public short getMessageType() {
		DhcpV4MsgTypeOption msgType = (DhcpV4MsgTypeOption)
					dhcpOptions.get(DhcpConstants.V4OPTION_MESSAGE_TYPE);
		if (msgType != null) {
			return msgType.getUnsignedByteOption().getUnsignedByte();
		}
		return 0;
	}
    
	public boolean isBroadcastFlagSet() {
		return (this.flags & 0x80) > 0;
	}
}
