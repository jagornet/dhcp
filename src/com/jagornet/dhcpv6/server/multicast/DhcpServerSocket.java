/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpServerSocket.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.server.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.util.DhcpConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class DhcpServerSocket.
 * 
 * @author agrabil
 */
public class DhcpServerSocket implements Runnable
{
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpServerSocket.class);
	
	/** the local network interface for this socket. */
	private final NetworkInterface netIf;

	/** the multicast socket itself */
	private final MulticastSocket msock;
	
	/** the socket address of the local network interface for this socket
	where the IP is the interface address and the port is the server port */
	private final InetSocketAddress localAddress;
	
	/**
	 * Instantiates a new DHCP server socket.
	 * 
	 * @param netIf the multicast interface to create the socket on
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public DhcpServerSocket(NetworkInterface netIf) throws IOException
	{
		this(netIf, DhcpConstants.SERVER_PORT);
	}
	
	/**
	 * Instantiates a new DHCP server socket.
	 * 
	 * @param netIf the multicast interface to create the socket on
	 * @param port the port number for the socket
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public DhcpServerSocket(NetworkInterface netIf, int port) throws IOException
	{
		this.netIf = netIf;
        log.debug("Configuring MulticastSocket on:" + 
        		  " interface=" + netIf.getDisplayName() +
        		  " port=" + port);	
    	SocketAddress saddr = new InetSocketAddress(port);
    	msock = new MulticastSocket(saddr);
    	msock.setNetworkInterface(netIf);
        log.debug("Joining All_DHCP_Relay_Agents_and_Servers multicast group: " + 
      		  DhcpConstants.ALL_DHCP_RELAY_AGENTS_AND_SERVERS);
        msock.joinGroup(DhcpConstants.ALL_DHCP_RELAY_AGENTS_AND_SERVERS);
        log.debug("Joining All_DHCP_Servers multicast group: " + 
        		  DhcpConstants.ALL_DHCP_SERVERS);
        msock.joinGroup(DhcpConstants.ALL_DHCP_SERVERS);
        localAddress = new InetSocketAddress(msock.getInterface(), port);
	}

	/**
	 * Gets the local address for this DHCP server socket.
	 * 
	 * @return the local InetSocketAddress
	 */
	public InetSocketAddress getLocalAddress()
	{
		return localAddress;
	}
	
	/**
	 * Gets the local network interface for this DHCP server socket.
	 * 
	 * @return the local NetworkInterface
	 */
	public NetworkInterface getNetworkInterface()
	{
		return netIf;
	}
    
    /**
     * Loop forever.  Receive a message from the multicast socket,
     * and queue the message to be worked on by the WorkProcessor.
     */
    public void run()
    {
    	while (true) {
    		try {
    			DhcpMessage msg = receiveMessage();
                if (msg != null) {
                    MulticastDhcpServer.queueMessage(msg);
                }
                else {
                    log.error("Null message");
                }
    		}
    		catch (Exception ex) {
    			log.error("Exception caught", ex);
    		}
    	}
    }
	
    /**
     * Receive a DhcpMessage.  Wait for a packet and decode it.
     * 
     * @return a decoded DhcpMessage, or null if an error occurred
     */
    public DhcpMessage receiveMessage()
    {
        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, 1024);

        log.info("Waiting for multicast datagram...");
        try {
	        msock.receive(packet);
	        if (packet != null) {
	            InetSocketAddress remoteAddress =
	                (InetSocketAddress)packet.getSocketAddress();
	            if (remoteAddress != null) {
	                log.info("Received datagram from: " + 
	                		DhcpMessage.socketAddressAsString(remoteAddress));
	                ByteBuffer bb = ByteBuffer.wrap(packet.getData());
	                try {
	                	return DhcpMessage.decode(bb, localAddress, remoteAddress);
	                }
	                catch (IOException ex) {
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
     * 
     * @param outMessage well-formed DhcpMessage to be sent to the host
     * identified therein by the remote address of the message.
     */
    public void sendMessage(DhcpMessage outMessage)
    {
    	try {
			if (outMessage != null) {
			    if (log.isDebugEnabled())
			        log.debug("Sending message: " + outMessage.toStringWithOptions());
			    ByteBuffer buf = outMessage.encode();
			    DatagramPacket dp = new DatagramPacket(buf.array(), buf.limit());
			    InetSocketAddress remoteAddr = outMessage.getRemoteAddress();
			    dp.setSocketAddress(remoteAddr);
			    msock.send(dp);
			    log.info("Sent datagram to: " + DhcpMessage.socketAddressAsString(remoteAddr));
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
