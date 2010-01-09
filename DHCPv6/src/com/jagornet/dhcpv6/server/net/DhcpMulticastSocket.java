/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpMulticastSocket.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.server.net;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.util.Util;

/**
 * The Class DhcpMulticastSocket.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpMulticastSocket extends DhcpServerSocket
{
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpMulticastSocket.class);
	
	/** the local network interface for this socket. */
	private final NetworkInterface netIf;
	
	/**
	 * Instantiates a new DHCP multicast server socket on the default port.
	 * 
	 * @param netIf the interface to create the socket on
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public DhcpMulticastSocket(NetworkInterface netIf) throws IOException
	{
		this(netIf, DhcpConstants.SERVER_PORT);
	}
	
	/**
	 * Instantiates a new DHCP multicast server socket.
	 * 
	 * @param netIf the multicast interface to create the socket on
	 * @param port the port number for the socket
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public DhcpMulticastSocket(NetworkInterface netIf, int port) throws IOException
	{
		this.netIf = netIf;
        log.info("Configuring DhcpMulticastSocket on:" + 
        		  " interface=" + netIf.getDisplayName() +
        		  " port=" + port);	
    	SocketAddress saddr = new InetSocketAddress(port);
    	MulticastSocket msock = new MulticastSocket(saddr);
    	msock.setNetworkInterface(netIf);
        log.debug("Joining All_DHCP_Relay_Agents_and_Servers multicast group: " +
        		DhcpConstants.ALL_DHCP_RELAY_AGENTS_AND_SERVERS);
        msock.joinGroup(DhcpConstants.ALL_DHCP_RELAY_AGENTS_AND_SERVERS);
        log.debug("Joining All_DHCP_Servers multicast group: " +
        		DhcpConstants.ALL_DHCP_SERVERS);
        msock.joinGroup(DhcpConstants.ALL_DHCP_SERVERS);
        Enumeration<InetAddress> netIfAddrs = netIf.getInetAddresses();
        while (netIfAddrs.hasMoreElements()) {
        	InetAddress netIfAddr = netIfAddrs.nextElement();
        	if (netIfAddr instanceof Inet6Address) {
                localAddress = new InetSocketAddress(netIfAddr, port);
        		break;
        	}
        }
        if (localAddress != null) {
        	log.info("Local IPv6 address=" + Util.socketAddressAsString(localAddress) + 
        			" for interface: " + netIf.getDisplayName());
        }
        else {
        	throw new IllegalStateException(
        			"Failed to determine local IPv6 address for interface: " + netIf);
        }
        sock = msock;
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
}
