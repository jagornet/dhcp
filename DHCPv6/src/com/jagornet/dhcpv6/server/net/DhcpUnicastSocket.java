/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpUnicastSocket.java is part of DHCPv6.
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
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.util.DhcpConstants;

/**
 * The Class DhcpUnicastSocket.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpUnicastSocket extends DhcpServerSocket
{
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpUnicastSocket.class);
	
	/**
	 * Instantiates a new DHCP unicast server socket on the default port.
	 * 
	 * @param addr the address to create the socket on
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public DhcpUnicastSocket(InetAddress addr) throws IOException
	{
		this(addr, DhcpConstants.SERVER_PORT);
	}
	
	/**
	 * Instantiates a new DHCP unicast server socket.
	 * 
	 * @param addr the unicast address to create the socket on
	 * @param port the port number for the socket
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public DhcpUnicastSocket(InetAddress addr, int port) throws IOException
	{
        log.info("Configuring DhcpUnicastSocket on:" + 
        		  " address=" + addr.getHostAddress() +
        		  " port=" + port);	
    	SocketAddress saddr = new InetSocketAddress(addr, port);
    	log.debug("Binding to local datagram socket: " + saddr);
		sock = new DatagramSocket(saddr);
		localAddress = new InetSocketAddress(sock.getInetAddress(), port);
	}
	
	@Override
	public DhcpMessage receiveMessage()
	{
		DhcpMessage msg = super.receiveMessage();
		if (msg != null) {
			msg.setUnicast(true);
		}
		return msg;
	}
}
