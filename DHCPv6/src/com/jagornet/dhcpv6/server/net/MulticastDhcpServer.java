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

import java.net.NetworkInterface;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;

/**
 * This is a simple, multi-threaded DHCPv6 server that uses
 * MulticastSockets for receiving multicast packets.
 * 
 * Note: Java 7 should support MulticastChannels, and then
 * this class can be deprecated in favor of the MINA based
 * multithreaded implementation.
 * 
 * @author A. Gregory Rabil
 */
public class MulticastDhcpServer extends NetDhcpServer
{
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(MulticastDhcpServer.class);

    /**
     * Instantiates a new multicast dhcp server.
     * 
     * @param netIfs the list of interfaces to listen
     * 				 for DHCP client requests on
     * @param port the server port to listen on
     * 
     * @throws Exception the exception
     */
    public MulticastDhcpServer(List<NetworkInterface> netIfs, int port) throws Exception
    {
    	int queueSize = DhcpServerConfiguration.getIntPolicy("mcast.server.queueSize",
    														  DEFAULT_QUEUE_SIZE);
    	workQueue = new LinkedBlockingQueue<DhcpMessage>(queueSize);
    	try {
    		for (NetworkInterface netIf : netIfs) {
				DhcpServerSocket dhcpSocket = new DhcpMulticastSocket(netIf, port);
				dhcpSocket.setNetDhcpServer(this);
				dhcpSocketMap.put(dhcpSocket.getLocalAddress(), dhcpSocket);
			}
	        workProcessor = new WorkProcessor(this);
	        processorThread = new Thread(workProcessor, "mcast.work.processor");
	        processorThread.start();
    	}
        catch (Exception ex) {
            log.error("Failed to initialize server: " + ex, ex);
            throw ex;
        }
    }

    /**
     * Start the multicast server.
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
	    			if (dhcpSock instanceof DhcpMulticastSocket) {
	    				DhcpMulticastSocket mcastSock = (DhcpMulticastSocket) dhcpSock;
	    				Thread sockThread = 
	    					new Thread(mcastSock, "mcast.socket." +
	    							   mcastSock.getNetworkInterface().getName());
		    			sockThread.start();
	    			}
				}
	    	}
    	}
    }
}