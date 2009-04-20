/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file WorkProcessor.java is part of DHCPv6.
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

import java.lang.management.ManagementFactory;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;

/**
 * Title: WorkProcessor
 * Description: The main thread which pulls DhcpMessages off of a queue
 * and processes the message in a thread pool.
 * 
 * @author A. Gregory Rabil
 */
public class WorkProcessor implements Runnable
{
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(WorkProcessor.class);

    /** The default thread pool size. */
    private static int DEFAULT_POOL_SIZE = 100;  
    
    /** The thread pool. */
    private TrackingThreadPool threadPool;
    
    /** The NetDhcpServer which holds this WorkProcessor. */
    private NetDhcpServer netDhcpServer;

    /**
     * Instantiates a new work processor.
     * 
     * @param netDhcpServer the NetDhcpServer which holds this WorkProcessor
     */
    public WorkProcessor(NetDhcpServer netDhcpServer)
    {
        this.netDhcpServer = netDhcpServer;
        int maxPoolSize = DhcpServerConfiguration.getIntPolicy("mcast.server.poolSize", 
        										 				DEFAULT_POOL_SIZE);
        threadPool = new TrackingThreadPool(DEFAULT_POOL_SIZE, 
                                            maxPoolSize,
                                            0,
                                            TimeUnit.SECONDS,
                                            new LinkedBlockingQueue<Runnable>());
        ThreadPoolStatus threadPoolStatus = new ThreadPoolStatus(threadPool);
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            log.debug("Registering ThreadPoolStatus MBean...");
            server.registerMBean(threadPoolStatus, 
                                 new ObjectName("dhcpv6:type=WorkProcessor,name=ThreadPool"));
            log.debug("ThreadPoolStatus MBean registered successfully.");
        }
        catch (Exception ex) {
            log.error("Failed to register ThreadPoolStatus MBean: " + ex);
        }        
    }
    
    /**
     * Loop forever.  Create a new DhcpHandlerThread whenever a new DhcpMessage
     * appears on the work queue.  Start the handler via the thread pool.
     */
    public void run()
    {
        while (true) {
            try {
                log.info("Waiting for work...");
                DhcpMessage message = netDhcpServer.getWorkQueue().take();
                if (message != null) {
                    log.info("Have work message, creating handler...");
                    Runnable handler = new DhcpHandlerThread(message, netDhcpServer);
                    if (handler != null) {
                        log.info("Message handler: " + handler.getClass());
	                    if (log.isDebugEnabled())
	                        log.debug("Sending to pool: " + message.toStringWithOptions());
	                    else if (log.isInfoEnabled())
	                        log.info("Sending to pool: " + message.toString());
	                    threadPool.execute(handler);
                    }
                    else {
                    	log.warn("Message dropped: " + message.toString());
                    }
                }
                else {
                    log.error("Work message is null");
                }
            }
            catch (InterruptedException ex) {
                log.info("Interrupted while waiting");
            }
        }
    }
}
