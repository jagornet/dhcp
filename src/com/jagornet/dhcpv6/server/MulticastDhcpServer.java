package com.jagornet.dhcpv6.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;
import com.jagornet.dhcpv6.server.mina.DhcpDecoderAdapter;
import com.jagornet.dhcpv6.server.mina.MinaDhcpHandler;
import com.jagornet.dhcpv6.util.DhcpConstants;

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
public class MulticastDhcpServer
{
	private static Logger log = LoggerFactory.getLogger(MulticastDhcpServer.class);

    protected MulticastSocket dhcpSocket;

    public MulticastDhcpServer(String configFilename, int port) throws Exception
    {
    	try {
	        DhcpServerConfiguration.init(configFilename);
	
	        log.debug("Configuring java.net.MulticastSocket on port: " + port);
	        dhcpSocket = new MulticastSocket(port);
	        log.debug("Joining All_DHCP_Relay_Agents_and_Servers multicast group: " + 
	        		  DhcpConstants.ALL_DHCP_RELAY_AGENTS_AND_SERVERS);
	        dhcpSocket.joinGroup(DhcpConstants.ALL_DHCP_RELAY_AGENTS_AND_SERVERS);
	        log.debug("Joining All_DHCP_Servers multicast group: " + 
	        		  DhcpConstants.ALL_DHCP_SERVERS);
	        dhcpSocket.joinGroup(DhcpConstants.ALL_DHCP_SERVERS);
        }
        catch (Exception ex) {
            log.error("Failed to initialize server: " + ex, ex);
            throw ex;
        }
    }

    public void start()
    {
    	run();
    }
    
    /**
     * Loop forever, processing incoming packets from the DHCPv6 server UDP
     * socket.
     */
    public void run()
    {
        while (true) {
        	DhcpMessage inMessage = receiveMessage();
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
            else {
            	log.warn("No message received");
            }
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
     * Receive a DhcpMessage.
     * Wait for a packet and then try to decode it.
     * 
     * @return a decoded DhcpMessage, or null if an error occurred
     */
    public DhcpMessage receiveMessage()
    {
        DhcpDecoderAdapter decoder = new DhcpDecoderAdapter();

        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, 1024);

        log.info("Waiting for datagram packet...");
        try {
	        dhcpSocket.receive(packet);
	        if (packet != null) {
	            InetSocketAddress srcInetSocketAddress =
	                (InetSocketAddress)packet.getSocketAddress();
	            // in theory, receive should never return null
	            // but better safe than NPE!
	            if (srcInetSocketAddress != null) {
	                log.info("Received datagram from: " + srcInetSocketAddress);
	                IoBuffer iobuf = IoBuffer.wrap(packet.getData());
	                try {
	                	return decoder.decode(iobuf, srcInetSocketAddress);
	                }
	                catch (ProtocolDecoderException ex) {
	                	log.error("Failed to decode message: " + ex);
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
        catch (IOException ex) {
        	log.error("Failure receiving message: " + ex);
        }
        return null;
    }
    
    /**
     * Sends a DhcpMessage object to a predefined host.
     * @param outMessage well-formed DhcpMessage to be sent to a host
     */
    public void sendMessage(DhcpMessage outMessage)
    {
    	try {
			if (outMessage != null) {
			    if (log.isDebugEnabled())
			        log.debug("Sending message: " + outMessage.toStringWithOptions());
			    IoBuffer iobuf = outMessage.encode();
			    DatagramPacket dp = new DatagramPacket(iobuf.buf().array(), iobuf.buf().limit());
			    dp.setSocketAddress(outMessage.getSocketAddress());
			    dhcpSocket.send(dp);
			    log.info("Sent datagram to: " + dp.getSocketAddress());
			}
			else {
				log.error("Attempted to send null message");
			}
		}
    	catch (IOException ex) {
			log.error("Failure sending message: " + ex);
		}
    }
}