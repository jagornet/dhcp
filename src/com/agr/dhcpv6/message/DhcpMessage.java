package com.agr.dhcpv6.message;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.option.DhcpOption;
import com.agr.dhcpv6.option.DhcpOptionFactory;
import com.agr.dhcpv6.util.DhcpConstants;

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
 *    |                          option-data                          |
 *    |                      (option-len octets)                      |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * 
 *    option-code   An unsigned integer identifying the specific option
 *                  type carried in this option.
 * 
 *    option-len    An unsigned integer giving the length of the
 *                  option-data field in this option in octets.
 * 
 *    option-data   The data for the option; the format of this data
 *                  depends on the definition of the option.
 * </pre>
 * 
 * Copyright:    Copyright (c) 2003
 * Company:      AGR Consulting
 * @author A. Gregory Rabil
 * @version 1.0
 */

public class DhcpMessage
{
    private static Log log = LogFactory.getLog(DhcpMessage.class);
    
    protected InetSocketAddress socketAddress;

    protected byte messageType = 0;
    protected long transactionId = 0;   // we only use low order three bytes
    protected Map<Short, DhcpOption> dhcpOptions = null;

    public DhcpMessage()
    {
        this(DhcpConstants.ALL_DHCP_RELAY_AGENTS_AND_SERVERS,
             DhcpConstants.CLIENT_PORT);
    }
    public DhcpMessage(InetAddress host)
    {
        this(host,
             DhcpConstants.CLIENT_PORT);
    }
    public DhcpMessage(int port)
    {
        this(DhcpConstants.ALL_DHCP_RELAY_AGENTS_AND_SERVERS,
             port);
    }
    public DhcpMessage(InetAddress host, int port)
    {
        this(new InetSocketAddress(host, port));
    }
    public DhcpMessage(InetSocketAddress socketAddress)
    {
        this.socketAddress = socketAddress;
        dhcpOptions = new HashMap<Short, DhcpOption>();
    }
    
    public ByteBuffer encode() throws IOException
    {
        if (log.isDebugEnabled())
            log.debug("Encoding DhcpMessage for: " + socketAddress);
        
        long s = System.currentTimeMillis();
        
        ByteBuffer buf = ByteBuffer.allocate(1024);
        buf.put(messageType);
        buf.put(DhcpTransactionId.encode(transactionId));
        buf.put(encodeOptions());
        buf.flip();
        
        if (log.isDebugEnabled())
            log.debug("DhcpMessage encoded in " +
                    String.valueOf(System.currentTimeMillis()-s) + " ms.");
        
        return buf;
    }

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
    
    public static DhcpMessage decode(InetSocketAddress srcInetSocketAddress,
                                     ByteBuffer buf)
            throws IOException
    {
        DhcpMessage dhcpMessage = new DhcpMessage(srcInetSocketAddress);
        dhcpMessage.decode(buf);
        return dhcpMessage;
    }

    public void decode(ByteBuffer buf) throws IOException
    {
        if (log.isDebugEnabled())
            log.debug("Decoding DhcpMessage from: " + socketAddress);
        
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

    public void decodeMessageType(ByteBuffer buf) throws IOException
    {
        if ((buf != null) && buf.hasRemaining()) {
            // TODO check that it is a valid message type, even though
            //      this should have already been done
            setMessageType(buf.get());
            if (log.isDebugEnabled())
                log.debug("MessageType=" + DhcpConstants.getMessageString(messageType));
        }
        else {
            String errmsg = "Failed to decode message type: buffer is empty";
            log.error(errmsg);
            throw new IOException(errmsg);
        }
    }
    
    public Map<Short, DhcpOption> decodeOptions(ByteBuffer buf) 
            throws IOException
    {
        Map<Short, DhcpOption> _dhcpOptions = new HashMap<Short, DhcpOption>();
        while (buf.hasRemaining()) {
            short code = buf.getShort();
            log.debug("Option code=" + code);
            DhcpOption option = DhcpOptionFactory.getDhcpOption(code, getHost());
            if (option != null) {
                option.decode(buf);
                _dhcpOptions.put(option.getCode(), option);
            }
            else {
                break;  // no more options, or one is malformed, so we're done
            }
        }
        return _dhcpOptions;
    }

    public short getLength()
    {
        short len = 4;    // msg type (1) + transaction id (3)
        len += getOptionsLength();
        return len;
    }
    
    public short getOptionsLength()
    {
        short len = 0;
        if (dhcpOptions != null) {
            for (DhcpOption option : dhcpOptions.values()) {
                len += 4;   // option code (2 bytes) + length (2 bytes) 
                len += option.getLength();
            }
        }
        return len;
    }

    /**
     * getHost() is overridden in DhcpRelayMessage to
     * return the peerAddress
     * @return
     */
    public InetAddress getHost()
    {
        return socketAddress.getAddress();
    }
    public int getPort()
    {
        return socketAddress.getPort();
    }
    public InetSocketAddress getSocketAddress()
    {
        return socketAddress;
    }
    public void setSocketAddress(InetSocketAddress socketAddress)
    {
        this.socketAddress = socketAddress;
    }

    public byte getMessageType()
    {
        return messageType;
    }
    public void setMessageType(byte messageType)
    {
        this.messageType = messageType;
    }
    public long getTransactionId()
    {
        return transactionId;
    }
    public void setTransactionId(long transactionId)
    {
        this.transactionId = transactionId;
    }

    public boolean hasOption(short optionCode)
    {
        if(dhcpOptions.containsKey(optionCode)) {
            return true;
        }
        return false;
    }

    public DhcpOption getOption(short optionCode)
    {
        return (DhcpOption)dhcpOptions.get(optionCode);
    }
    public void setOption(DhcpOption dhcpOption)
    {
        if(dhcpOption != null) {
            dhcpOptions.put(dhcpOption.getCode(), dhcpOption);
        }
    }
    
    public Map<Short, DhcpOption> getDhcpOptions()
    {
        return dhcpOptions;
    }
    public void setDhcpOptions(Map<Short, DhcpOption> dhcpOptions)
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
        sb.append(" to/from ");
        sb.append(getHost().getHostAddress());
        sb.append(":");
        sb.append(getPort());
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
}
