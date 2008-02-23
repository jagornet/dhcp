package com.agr.dhcpv6.option;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Title: BaseServerAddressesOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public abstract class BaseServerAddressesOption implements DhcpOption
{
    private static Log log = LogFactory.getLog(BaseServerAddressesOption.class);

    public abstract List<String> getServerIpAddresses();

    public ByteBuffer encode() throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(2+2+ getLength());
        bb.putShort(this.getCode());
        bb.putShort(this.getLength());
        List<String> serverIps = this.getServerIpAddresses();
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
        if ((bb != null) && bb.hasRemaining()) {
            // already have the code, so length is next
            short len = bb.getShort();
            if (log.isDebugEnabled())
                log.debug(this.getName() + " reports length=" + len +
                          ":  bytes remaining in buffer=" + bb.remaining());
            short eof = (short)(bb.position() + len);
            while (bb.position() < eof) {
                // it has to be hex from the wire, right?
                byte b[] = new byte[16];
                bb.get(b);
                this.addServer(b);
            }
        }
    }

    public short getLength()
    {
        short len = 0;
        List<String> serverIps = this.getServerIpAddresses();
        if (serverIps != null) {
            len += serverIps.size() * 16;   // each IPv6 address is 16 bytes
        }
        return len;
    }

    public void addServer(byte[] addr)
    {
        try {
            if (addr != null) {
                InetAddress inetAddr = InetAddress.getByAddress(addr);
                this.addServer(inetAddr);
            }
        }
        catch (UnknownHostException ex) {
            log.error("Failed to add DnsServer: " + ex);
        }
    }
    public void addServer(String ip)
    {
        if (ip != null) {
            this.getServerIpAddresses().add(ip);
        }
    }

    public void addServer(InetAddress inetAddr)
    {
        if (inetAddr != null) {
            this.getServerIpAddresses().add(inetAddr.getHostAddress());
        }
    }
    
    public String toString()
    {
        if (this == null)
            return null;
        
        StringBuilder sb = new StringBuilder(this.getName());
        sb.append(": ");
        List<String> serverIps = this.getServerIpAddresses();
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
