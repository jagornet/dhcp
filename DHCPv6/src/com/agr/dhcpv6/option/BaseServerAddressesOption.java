package com.agr.dhcpv6.option;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.mina.common.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Title: BaseServerAddressesOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public abstract class BaseServerAddressesOption extends BaseDhcpOption
{
	private static Logger log = LoggerFactory.getLogger(BaseServerAddressesOption.class);

    public abstract List<String> getServerIpAddresses();

    public IoBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        List<String> serverIps = this.getServerIpAddresses();
        if (serverIps != null) {
            for (String ip : serverIps) {
                InetAddress inet6Addr = Inet6Address.getByName(ip);
                iobuf.put(inet6Addr.getAddress());
            }
        }
        return (IoBuffer)iobuf.flip();
    }

    public void decode(IoBuffer iobuf) throws IOException
    {
    	int len = super.decodeLength(iobuf); 
    	if ((len > 0) && (len <= iobuf.remaining())) {
            int eof = iobuf.position() + len;
            while (iobuf.position() < eof) {
                // it has to be hex from the wire, right?
                byte b[] = new byte[16];
                iobuf.get(b);
                this.addServer(b);
            }
        }
    }

    public int getLength()
    {
        int len = 0;
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
