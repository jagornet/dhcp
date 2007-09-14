package com.agr.dhcpv6.option;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.server.config.xml.SipServersAddressListOption;

/**
 * <p>Title: DhcpSipServersAddressListOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpSipServersAddressListOption implements DhcpOption
{
    private static Log log = LogFactory.getLog(DhcpSipServersAddressListOption.class);

    private SipServersAddressListOption sipServersAddressListOption;

    public DhcpSipServersAddressListOption()
    {
        super();
        sipServersAddressListOption = new SipServersAddressListOption();
    }
    public DhcpSipServersAddressListOption(SipServersAddressListOption sipServersAddressListOption)
    {
        super();
        if (sipServersAddressListOption != null)
            this.sipServersAddressListOption = sipServersAddressListOption;
        else
            this.sipServersAddressListOption = new SipServersAddressListOption();
    }

    public SipServersAddressListOption getSipServersAddressListOption()
    {
        return sipServersAddressListOption;
    }

    public void setSipServersAddressListOption(SipServersAddressListOption sipServersAddressListOption)
    {
        if (sipServersAddressListOption != null)
            this.sipServersAddressListOption = sipServersAddressListOption;
    }
    
    public ByteBuffer encode() throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(2+2+ getLength());
        bb.putShort(this.getCode());
        bb.putShort(this.getLength());
        List<String> serverIps = sipServersAddressListOption.getServerIpAddresses();
        if (serverIps != null) {
            for (String ip : serverIps) {
                InetAddress inet6Addr = Inet6Address.getByName(ip);
                bb.put(inet6Addr.getAddress());
            }
        }
        return (ByteBuffer)bb.flip();
    }

    public void decode(ByteBuffer bb) throws IOException
    {
        // already have the code, so length is next
        short len = bb.getShort();
        if (log.isDebugEnabled())
            log.debug(sipServersAddressListOption.getName() + " reports length=" + len +
                      ":  bytes remaining in buffer=" + bb.remaining());
        short eof = (short)(bb.position() + len);
        while (bb.position() < eof) {
            // it has to be hex from the wire, right?
            byte b[] = new byte[16];
            bb.get(b);
            this.addDnsServer(b);
        }
    }

    public short getCode()
    {
        return sipServersAddressListOption.getCode();
    }

    public short getLength()
    {
        short len = 0;
        List<String> serverIps = sipServersAddressListOption.getServerIpAddresses();
        if (serverIps != null) {
            len += serverIps.size() * 16;   // each IPv6 address is 16 bytes
        }
        return len;
    }

    public void addDnsServer(byte[] addr)
    {
        try {
            if (addr != null) {
                InetAddress inetAddr = InetAddress.getByAddress(addr);
                this.addDnsServer(inetAddr);
            }
        }
        catch (UnknownHostException ex) {
            log.error("Failed to add DnsServer: " + ex);
        }
    }
    public void addDnsServer(String ip)
    {
        if (ip != null) {
            sipServersAddressListOption.getServerIpAddresses().add(ip);
        }
    }

    public void addDnsServer(InetAddress inetAddr)
    {
        if (inetAddr != null) {
            sipServersAddressListOption.getServerIpAddresses().add(inetAddr.getHostAddress());
        }
    }

    public String toString()
    {
        if (sipServersAddressListOption == null)
            return null;
        
        StringBuilder sb = new StringBuilder(sipServersAddressListOption.getName());
        sb.append(": ");
        List<String> serverIps = sipServersAddressListOption.getServerIpAddresses();
        if (serverIps != null) {
            for (String ip : serverIps) {
                sb.append(ip);
                sb.append(",");
            }
            sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }

}
