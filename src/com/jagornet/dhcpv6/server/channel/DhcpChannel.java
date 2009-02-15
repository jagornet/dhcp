package com.jagornet.dhcpv6.server.channel;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;

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
	
	/**
	 * RFC 2460 specifies a minimum MTU of 1280 for IPv6, but
	 * it also recommends using 1500 on link where the MTU is
	 * configurable.  Also, RFC 3146 specifies a default MTU
	 * of 1500 for IEEE1394 networks.  So, we'll use 1500.
	 */
    private static int PACKET_SIZE = 1500;

    /**
     * The channel is the Java NIO abstraction for UDP sockets.
     * Preferring composition to inheritance here.
     */
    private DatagramChannel channel = null;
    /**
     * The Java docs state that the DatagramChannel is not a
     * complete abstraction, so we need to manipulate the
     * underlying DatagramSocket as well, at least for now.
     */
    private DatagramSocket socket = null;

    /**
     * Create a DhcpChannel on the localhost by creating
     * a datagram socket on any available local IP address
     * and binding the socket to the given UDP port.
     * 
     * @param port the UDP port number to bind to
     * @throws IOException
     * @throws SocketException
     */
    public DhcpChannel(int port)
            throws IOException, SocketException
    {
    	this(null, port);
    }
    
    /**
     * Create a DhcpChannel on the localhost by creating
     * a datagram socket on the given local IP address
     * and binding the socket to the given UDP port.
     * 
     * @param localAddress localhost IP address, null will
     * 					   select any available local IP
     * @param port the UDP port number to bind to
     * @throws IOException
     * @throws SocketException
     */
    public DhcpChannel(InetAddress localAddress, int port)
    		throws IOException, SocketException
    {
        channel = DatagramChannel.open();
        socket = channel.socket();
        // create a socket on the local IP address, null for "any"
        SocketAddress sa = new InetSocketAddress(localAddress, port);
        // bind to the UDP port on the localhost
        socket.bind(sa);
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

    /**
     * Encode the supplied DhcpMessage to wire format, and
     * send the data via this channel to the target host
     * defined within the supplied message.
     * 
     * @param outMessage DhcpMessage containing the data to be sent
     * 					 and the target host's socket address.
     */
    public void send(DhcpMessage outMessage)
    {
        try {
            channel.send(outMessage.encode().buf(), outMessage.getSocketAddress());
        }
        catch (IOException ex) {
            log.error("Failure sending message: " + ex);
        }
    }
    
    /**
     * Receive and decode a message on this channel.
     * 
     * @return a DhcpMessage containing the decoded data and the
     * 		   source address of the host which sent the message.
     */
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