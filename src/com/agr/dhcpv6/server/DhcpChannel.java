package com.agr.dhcpv6.server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agr.dhcpv6.message.DhcpMessage;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2000</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class DhcpChannel
{
	private static Logger log = LoggerFactory.getLogger(DhcpChannel.class);
    private static int PACKET_SIZE = 1500;   // default max MTU for ethernet

    private DatagramChannel channel = null;
    private DatagramSocket socket = null;

    public DhcpChannel(int port)
            throws IOException, SocketException
    {
        channel = DatagramChannel.open();
        socket = channel.socket();
        // the socket address represents the wildcard address for localhost
        SocketAddress sa = new InetSocketAddress(port);
        socket.bind(sa);    // bind to localhost DHCP port
    }

    public DatagramChannel getChannel()
    {
        return channel;
    }
    public void setChannel(DatagramChannel dc)
    {
        channel = dc;
    }

    public DatagramSocket getSocket()
    {
        return socket;
    }
    public void setSocket(DatagramSocket ds)
    {
        socket = ds;
    }

    public static void setMTU(int mtu)
    {
        PACKET_SIZE = mtu;
    }
    public static int getMTU()
    {
        return PACKET_SIZE;
    }

    public void send(DhcpMessage outMessage)
    {
        try {
            channel.send(outMessage.encode().buf(), outMessage.getSocketAddress());
        }
        catch (IOException ex) {
            log.error("Failure sending message: " + ex);
        }
    }
    
    public void receive(DhcpMessage inMessage)
    {
        IoBuffer iobuf = IoBuffer.allocate(PACKET_SIZE);
        try {
            
            InetSocketAddress srcInetSocketAddress =
                (InetSocketAddress)channel.receive(iobuf.buf());
            // in theory, receive should never return null
            // but better safe than NPE!
            if (srcInetSocketAddress != null) {
                inMessage.setSocketAddress(srcInetSocketAddress);
                log.info("Received datagram from: " + srcInetSocketAddress);
                iobuf.flip();
                inMessage.decode(iobuf);
                if (log.isDebugEnabled())
                    log.debug("Decoded message: " + 
                              inMessage.toStringWithOptions());
            }
            else {
                log.error("Channel received returned null!");
            }
        }
        catch (IOException ex) {
            log.error("Failure receiving message: " + ex);
        }
    }
    
    public DhcpMessage receive()
    {
        IoBuffer iobuf = IoBuffer.allocate(PACKET_SIZE);
        DhcpMessage inMessage = null; 
        try {
            
            InetSocketAddress srcInetSocketAddress =
                (InetSocketAddress)channel.receive(iobuf.buf());
            // in theory, receive should never return null
            // but better safe than NPE!
            if (srcInetSocketAddress != null) {
                log.info("Received datagram from: " + srcInetSocketAddress);
                iobuf.flip();
                inMessage = DhcpMessage.decode(srcInetSocketAddress, iobuf);
                if (log.isDebugEnabled())
                    log.debug("Decoded message: " + 
                              (inMessage != null ? 
                              inMessage.toStringWithOptions() : "null"));
            }
            else {
                log.error("Channel received returned null!");
            }
        }
        catch (IOException ex) {
            log.error("Failure receiving message: " + ex);
        }
        return inMessage;
    }
}