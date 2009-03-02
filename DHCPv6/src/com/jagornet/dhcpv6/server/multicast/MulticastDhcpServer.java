/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file MulticastDhcpServer.java is part of DHCPv6.
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

import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpOption;

/**
 * This is a simple, multi-threaded DHCPv6 server that uses
 * MulticastSockets for receiving packets.
 * 
 * Note: Java 7 should support MulticastChannels, and then
 * this class can be deprecated in favor of the MINA based
 * multithreaded implementation.
 * 
 * @author A. Gregory Rabil
 */
public class MulticastDhcpServer implements Runnable
{
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(MulticastDhcpServer.class);

	/** the map of DhcpServerSockets keyed by InetSocketAddress so
	 that it can be located by the DhcpHandlerThread thread */
	private static Map<InetSocketAddress, DhcpServerSocket> dhcpSocketMap =
    	new HashMap<InetSocketAddress, DhcpServerSocket>();

    // TODO: a configurable queue size is needed at least,
    //		 and may want more than one queue?
    /** The queue size. */
    private static int QUEUE_SIZE = 10000; 
    
    /** The work queue. */
    private static BlockingQueue<DhcpMessage> workQueue =
    	new LinkedBlockingQueue<DhcpMessage>(QUEUE_SIZE);
    
    // would more than one queue need more processors?
    /** The work processor. */
    private WorkProcessor workProcessor;
    
    /** The processor thread. */
    private Thread processorThread;

    /**
     * Instantiates a new multicast dhcp server.
     * 
     * @param netIfs the list of multicast interfaces to listen
     * for DHCP client requests on
     * @param port the server port to listen on
     * 
     * @throws Exception the exception
     */
    public MulticastDhcpServer(List<NetworkInterface> netIfs, int port) throws Exception
    {
    	try {
    		for (NetworkInterface netIf : netIfs) {
				DhcpServerSocket dhcpSocket = new DhcpServerSocket(netIf, port);
				dhcpSocketMap.put(dhcpSocket.getLocalAddress(), dhcpSocket);
			}
	        workProcessor = new WorkProcessor(workQueue);
	        processorThread = new Thread(workProcessor, "work.processor");
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
	    			Thread mcastThread = 
	    				new Thread(dhcpSock, "mcast.socket." + 
	    						   dhcpSock.getNetworkInterface().getDisplayName());
	    			mcastThread.start();
				}
	    	}
    	}
    }

    /**
     * Gets the DhcpServerSocket for the the given socket address.
     * 
     * @param sockAddr the InetSocketAddress key for the map of DhcpServerSockets
     * 
     * @return the DhcpServerSocket or null if not found
     */
    public static DhcpServerSocket getDhcpServerSocket(InetSocketAddress sockAddr)
    {
    	return dhcpSocketMap.get(sockAddr);
    }
    
    /**
     * Queue a DhcpMessage to be processed by the WorkProcessor.
     * 
     * @param msg the DhcpMessage to put on the work queue.
     */
    public static void queueMessage(DhcpMessage msg)
    {
        if (log.isInfoEnabled()) {
            StringBuffer sb = new StringBuffer("Received DHCP ");
            sb.append(msg.toString());
            log.info(sb.toString());
            debugOptions(msg);
        }
        log.info("Queueing message...");
        if (workQueue.offer(msg)) {
            log.info("Message queued");
        }
        else {
            log.error("Failed to queue message: full queue");
        }
    }

    /**
     * Print out the options contained in a DHCPMessage to the
     * logger.
     * 
     * @param msg the DhcpMessage to print to the log
     */
    public static void debugOptions(DhcpMessage msg)
    {
        if (log.isDebugEnabled()) {
            Collection<DhcpOption> options = msg.getOptions();
            if (options != null) {
                for (DhcpOption option : options) {
                    log.debug(option.toString());
                }
            }
        }
    }
}