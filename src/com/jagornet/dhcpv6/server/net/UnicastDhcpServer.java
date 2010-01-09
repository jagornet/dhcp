/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file NetDhcpServer.java is part of DHCPv6.
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

import java.net.InetAddress;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies.Property;

/**
 * This is a simple, multi-threaded DHCPv6 server that uses
 * DatagramSockets for receiving unicast packets.
 * 
 * Note: Windows Vista/2008 and Java 7 should fully support IPv6,
 * and then this class can be deprecated in favor of the MINA based
 * multithreaded implementation.
 * 
 * @author A. Gregory Rabil
 */
public class UnicastDhcpServer extends NetDhcpServer
{
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(UnicastDhcpServer.class);

    /**
     * Instantiates a new unicast dhcp server.
     * 
     * @param addrs the list of addresses to listen on
     * @param port the server port to listen on
     * 
     * @throws Exception the exception
     */
    public UnicastDhcpServer(List<InetAddress> addrs, int port) throws Exception
    {
    	int queueSize = DhcpServerPolicies.globalPolicyAsInt(Property.UCAST_SERVER_QUEUE_SIZE);
    	workQueue = new LinkedBlockingQueue<DhcpMessage>(queueSize);
    	try {
    		for (InetAddress addr : addrs) {
				DhcpServerSocket dhcpSocket = new DhcpUnicastSocket(addr, port);
				dhcpSocket.setNetDhcpServer(this);
				dhcpSocketMap.put(dhcpSocket.getLocalAddress(), dhcpSocket);
			}
	        workProcessor = new WorkProcessor(this);
	        processorThread = new Thread(workProcessor, "ucast.work.processor");
	        processorThread.start();
    	}
        catch (Exception ex) {
            log.error("Failed to initialize server: " + ex, ex);
            throw ex;
        }
    }

    /**
     * Start the unicast server.
     */
    public void start()
    {
    	run();
    }

    /**
     * Launch a new thread for each configured DhcpServerSocket
     */
    public void run()
    {
    	if (dhcpSocketMap != null) {
    		Collection<DhcpServerSocket> dhcpSockets = dhcpSocketMap.values();
	    	if (dhcpSockets != null) {
	    		for (DhcpServerSocket dhcpSock : dhcpSockets) {
	    			if (dhcpSock instanceof DhcpUnicastSocket) {
	    				DhcpUnicastSocket ucastSock = (DhcpUnicastSocket) dhcpSock;
	    				Thread sockThread = 
	    					new Thread(ucastSock, "ucast.socket." +
	    							   ucastSock.getLocalAddress());
		    			sockThread.start();
	    			}
				}
	    	}
    	}
    }
}