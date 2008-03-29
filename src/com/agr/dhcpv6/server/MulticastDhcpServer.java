package com.agr.dhcpv6.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.filter.codec.ProtocolDecoderException;

import com.agr.dhcpv6.message.DhcpMessage;
import com.agr.dhcpv6.server.config.DhcpServerConfiguration;
import com.agr.dhcpv6.server.mina.DhcpDecoderAdapter;
import com.agr.dhcpv6.server.mina.MinaDhcpHandler;
import com.agr.dhcpv6.util.DhcpConstants;

/**
 * Title:        DHCPServer
 * Copyright:    Copyright (c) 2003
 * Company:      AGR Consulting
 * @author A. Gregory Rabil
 * @version 1.0
 */

/**
 * This is a simple, single-threaded DHCPv6 server that uses
 * MulticastSocket because MulticastChannels are not yet
 * available in Java :-(
 */
public class MulticastDhcpServer implements Runnable
{
    private static Log log = LogFactory.getLog(MulticastDhcpServer.class);

    protected MulticastSocket dhcpSocket;

    public MulticastDhcpServer(String configFilename, int port) throws Exception
    {
        DhcpServerConfiguration.init(configFilename);

        dhcpSocket = new MulticastSocket(port);
        dhcpSocket.joinGroup(DhcpConstants.ALL_DHCP_RELAY_AGENTS_AND_SERVERS);
        dhcpSocket.joinGroup(DhcpConstants.ALL_DHCP_SERVERS);
        
        run();
    }

    /**
     * Loop forever, processing incoming packets from the DHCPv6 server UDP
     * socket.
     */
    public void run()
    {
        try {
            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, 1024);

            while (true) {
                log.info("Waiting for datagram packet...");
                dhcpSocket.receive(packet);
                
                if (packet != null) {
                    InetSocketAddress srcInetSocketAddress =
                        (InetSocketAddress)packet.getSocketAddress();
                    // in theory, receive should never return null
                    // but better safe than NPE!
                    if (srcInetSocketAddress != null) {
                        log.info("Received datagram from: " + srcInetSocketAddress);
                        ByteBuffer bbuf = ByteBuffer.wrap(buf);
                        
                        DhcpDecoderAdapter decoder = new DhcpDecoderAdapter();
                        DhcpMessage inMessage = null;
                        try {
                        	inMessage = decoder.decode(bbuf, srcInetSocketAddress);
                        }
                        catch (ProtocolDecoderException ex) {
                        	log.error("Failed to decode message: " + ex);
                        	continue;
                        }
                        if (inMessage != null) {
                            if (log.isDebugEnabled())
                                log.debug("Decoded message: " + 
                                          (inMessage != null ? 
                                          inMessage.toStringWithOptions() : "null"));
                        	MinaDhcpHandler handler = new MinaDhcpHandler();
                        	DhcpMessage outMessage = 
                        		handler.handleMessage(getLocalAddress(), inMessage);
                            if (outMessage != null) {
                                sendMessage(outMessage);
                            }
                            else {
                                log.warn("Handler returned null reply message");
                            }
                        }
                    }
                    else {
                        log.error("Null source address for packet");
                    }
                }
                else {
                    log.error("Null packet received");
                }
            }
        }
        catch (IOException ex) {
            log.fatal("DHCPv6 Server Abort", ex);
        }
    }

	InetAddress localAddr = null;
    private InetAddress getLocalAddress()
    {
    	if (localAddr == null) {
	    	try {
	    		localAddr = Inet6Address.getLocalHost();
	    	}
	    	catch (UnknownHostException ex) {
	    		log.error("Failed to get local IPv6 address: " + ex);
	    	}
    	}
    	return localAddr;
    }
    
    /**
     * Sends a DhcpMessage object to a predefined host.
     * @param outMessage well-formed DhcpMessage to be sent to a host
     */
    public void sendMessage(DhcpMessage outMessage)
            throws IOException
    {
        if (log.isDebugEnabled())
            log.debug("Sending message: " + 
                      (outMessage != null ? 
                       outMessage.toStringWithOptions() : "null"));
        ByteBuffer bbuf = outMessage.encode();
        DatagramPacket dp = new DatagramPacket(bbuf.array(), bbuf.limit());
        dp.setSocketAddress(outMessage.getSocketAddress());
        dhcpSocket.send(dp);
        log.info("Sent datagram to: " + dp.getSocketAddress());
    }
}