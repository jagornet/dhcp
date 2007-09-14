package com.agr.dhcpv6.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.agr.dhcpv6.option.DhcpClientIdOption;
import com.agr.dhcpv6.option.DhcpDnsServersOption;
import com.agr.dhcpv6.option.DhcpDomainListOption;
import com.agr.dhcpv6.option.DhcpElapsedTimeOption;
import com.agr.dhcpv6.option.DhcpOption;
import com.agr.dhcpv6.option.DhcpOptionRequestOption;
import com.agr.dhcpv6.option.DhcpPreferenceOption;
import com.agr.dhcpv6.option.DhcpServerIdOption;
import com.agr.dhcpv6.option.DhcpUserClassOption;
import com.agr.dhcpv6.option.DhcpVendorClassOption;
import com.agr.dhcpv6.option.DhcpVendorInfoOption;
import com.agr.dhcpv6.util.DhcpConstants;

/**
 * Title:        DHCPMessage
 * Description:  Object that represents a DHCPv6 message as defined in
 *               RFC xxxx.
 * Copyright:    Copyright (c) 2003
 * Company:      AGR Consulting
 * @author A. Gregory Rabil
 * @version 1.0
 */

public class DhcpMessage
{
    protected InetSocketAddress socketAddress;

    private byte messageType = 0;
    private long transactionId = 0;   // we only use low order three bytes
    private Map<Short, DhcpOption> dhcpOptions = null;

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
    
    public synchronized ByteBuffer encode() throws IOException
    {
        ByteBuffer buf = ByteBuffer.allocate(1024);
        buf.put(messageType);
        buf.put(encodeTransactionId());
        buf.put(encodeOptions());
        return (ByteBuffer)buf.flip();
    }
    
    
    public synchronized void decode(ByteBuffer buf) throws IOException
    {
        if ((buf != null) && buf.hasRemaining()) {
            setMessageType(buf.get());
            decodeTransactionId(buf);
            if (buf.hasRemaining()) {
                decodeOptions(buf);
            }
        }
    }
    
    public static DhcpMessage decode(InetSocketAddress srcInetSocketAddress,
                                     ByteBuffer buf)
    {
        if ((buf != null) && buf.hasRemaining()) {
            byte msgType = buf.get();
            
        }
        DhcpMessage dhcpMessage = new DhcpMessage(srcInetSocketAddress);
        
        return dhcpMessage;
    }
    
    public ByteBuffer encodeTransactionId()
    {
        ByteBuffer buf = ByteBuffer.allocate(3);
        //byte i0 = (byte)(transactionId >>> 24);         // r_shift out the last three bytes
        byte i1 = (byte)((transactionId << 8) >>> 24);  // l_shift first byte out of int, then unsigned r_shift back to one byte
        byte i2 = (byte)((transactionId << 16) >>> 24); // l_shift first 2 bytes out of int, then unsigned r_shift back to one byte
        byte i3 = (byte)((transactionId << 24) >>> 24); // l_shift first 3 bytes out of int, then unsigned r_shift back to one byte
        buf.put(i1);
        buf.put(i2);
        buf.put(i3);
        return (ByteBuffer)buf.flip();
    }

    private void decodeTransactionId(ByteBuffer buf)
    {
        byte[] dst = new byte[3];
        buf.get(dst);
        setTransactionId( (((int)dst[0] & 0xFF) * 256 * 256) +
                          (((int)dst[1] & 0xFF) * 256) +
                          ((int)dst[2] & 0xFF) );
        
    }

    private ByteBuffer encodeOptions() throws IOException
    {
        ByteBuffer buf = ByteBuffer.allocate(1020); // 1024 - 1(msgType) - 3(transId) = 1020 (options)
        if (dhcpOptions != null) {
            for (DhcpOption option : dhcpOptions.values()) {
                 buf.put(option.encode());
            }
        }
        return (ByteBuffer)buf.flip();
    }
    
    private void decodeOptions(ByteBuffer buf) throws IOException
    {
        while (buf.hasRemaining()) {
            short code = buf.getShort();
            DhcpOption option = getDhcpOption(code);
            if (option != null) {
                option.decode(buf);
                dhcpOptions.put(option.getCode(), option);
            }
            else {
                break;  // no more options, or one is malformed, so we're done
            }
        }
    }

/** @todo use a properties file to map the implementation classes to options */
    public static DhcpOption getDhcpOption(short code)
    {
        DhcpOption option = null;
        switch(code) {
            case DhcpConstants.OPTION_CLIENTID:
                option = new DhcpClientIdOption();
                break;
            case DhcpConstants.OPTION_SERVERID:
                option = new DhcpServerIdOption();
                break;
            case DhcpConstants.OPTION_ORO:
                option = new DhcpOptionRequestOption();
                break;
            case DhcpConstants.OPTION_PREFERENCE:
                option = new DhcpPreferenceOption();
                break;
            case DhcpConstants.OPTION_ELAPSED_TIME:
                option = new DhcpElapsedTimeOption();
                break;
            case DhcpConstants.OPTION_STATUS_CODE:
                //option = new DhcpStatusCodeOption();
                break;
            case DhcpConstants.OPTION_USER_CLASS:
                option = new DhcpUserClassOption();
                break;
            case DhcpConstants.OPTION_VENDOR_CLASS:
                option = new DhcpVendorClassOption();
                break;
            case DhcpConstants.OPTION_VENDOR_OPTS:
                option = new DhcpVendorInfoOption();
                break;
            case DhcpConstants.OPTION_INTERFACE_ID:
                //option = new DhcpInterfaceIdOption();
                break;
            case DhcpConstants.OPTION_DNS_SERVERS:
                option = new DhcpDnsServersOption();
                break;
            case DhcpConstants.OPTION_DOMAIN_LIST:
                option = new DhcpDomainListOption();
                break;
            default:
//                option = new DhcpOption();
//                option.setCode(code);
                break;
        }
        return option;
    }

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
        sb.append(" from ");
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
