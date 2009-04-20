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

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.base.DhcpOption;

/**
 * The Class NetDhcpServer.
 * 
 * @author A. Gregory Rabil
 * 
 */
public abstract class NetDhcpServer implements Runnable
{
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(NetDhcpServer.class);

	/** the map of DhcpServerSockets keyed by InetSocketAddress so
	 *  that it can be located by the DhcpHandlerThread thread */
	protected Map<InetSocketAddress, DhcpServerSocket> dhcpSocketMap =
    	new HashMap<InetSocketAddress, DhcpServerSocket>();

    /** The default queue size. */
	protected static int DEFAULT_QUEUE_SIZE = 10000; 
    
    /** The work queue. The queue is shared by all the DhcpServerSockets 
     *  running in this NetDhcpServer instance. */
	protected BlockingQueue<DhcpMessage> workQueue;
    
    // would more than one queue need more processors?
    /** The work processor. The work processor is shared by all the
     *  DhcpServerSockets running in this NetDhcpServer instance. */
	protected WorkProcessor workProcessor;
    
    /** The processor thread. */
	protected Thread processorThread;

    /**
     * Gets the DhcpServerSocket for the the given socket address.
     * 
     * @param sockAddr the InetSocketAddress key for the map of DhcpServerSockets
     * 
     * @return the DhcpServerSocket or null if not found
     */
    public DhcpServerSocket getDhcpServerSocket(InetSocketAddress sockAddr)
    {
    	if (dhcpSocketMap != null) {
    		return dhcpSocketMap.get(sockAddr);
    	}
    	return null;
    }
    
    /**
     * Gets the work queue.
     * 
     * @return the work queue
     */
    public BlockingQueue<DhcpMessage> getWorkQueue()
    {
    	return workQueue;
    }

    /**
     * Queue a DhcpMessage to be processed by the WorkProcessor.
     * 
     * @param msg the DhcpMessage to put on the work queue.
     */
    public void queueMessage(DhcpMessage msg)
    {
    	if (msg != null) {
	        if (log.isInfoEnabled()) {
	            StringBuffer sb = new StringBuffer("Received DHCP ");
	            sb.append(msg.toString());
	            log.info(sb.toString());
	            debugOptions(msg);
	        }
	        if (workQueue != null) {
		        log.info("Queueing message...");
		        if (workQueue.offer(msg)) {
		            log.info("Message queued");
		        }
		        else {
		            log.error("Failed to queue message: queue capacity reached");
		        }
	        }
	        else {
	        	log.error("Failed to queue message: workQueue is null");
	        }
    	}
    	else {
    		log.error("Failed to queue message: message is null");
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
