/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file NioDhcpServer.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.server.nio;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.integration.jmx.IoServiceMBean;
import org.apache.mina.integration.jmx.IoSessionMBean;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a Apache MINA based DHCPv6 server that uses
 * Java NIO DatagramChannels for receiving unicast messages.
 * 
 * Note: Java 7 should support MulticastChannels, and then
 * this class can be refactored to support both unicast and
 * multicast packets.
 * 
 * @author A. Gregory Rabil
 */
public class NioDhcpServer
{
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(NioDhcpServer.class);
    
    /** The acceptor. */
    protected NioDatagramAcceptor acceptor;
    
    /** The executor service. */
    protected ExecutorService executorService;
    
    /**
     * Instantiates a new unicast dhcp server.
     * 
     * @param port the port
     * 
     * @throws Exception the exception
     */
    public NioDhcpServer(List<InetAddress> addrs, int port) throws Exception
    {
        try {
            acceptor = new NioDatagramAcceptor();
            if ((addrs != null) && !addrs.isEmpty()) {
                List<SocketAddress> socketAddrs = new ArrayList<SocketAddress>();
            	for (InetAddress addr : addrs) {
					SocketAddress sockAddr = new InetSocketAddress(addr, port);
					socketAddrs.add(sockAddr);
				}
                acceptor.setDefaultLocalAddresses(socketAddrs);
            }
                        
            acceptor.setHandler(new DhcpHandlerAdapter());
    
            registerJmx(acceptor);
            
            DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();  
            chain.addLast("logger", new LoggingFilter());
            
            ProtocolEncoder encoder = new DhcpEncoderAdapter();
            ProtocolDecoder decoder = new DhcpDecoderAdapter();
/*
 * From the MINA Wiki documentation:
 * 
 *      Where should I put an ExecutorFilter in an IoFilterChain?
 * 
 *          It depends on the characteristics of your application. 
 *          For an application with a ProtocolCodecFilter implementation and 
 *          a usual IoHandler implementation with database operations, 
 *          I'd suggest you to add the ExecutorFilter after the 
 *          ProtocolCodecFilter implementation. It is because the 
 *          performance characteristic of most protocol codec implementations 
 *          is CPU-bound, which is the same with I/O processor threads.      
 */        
            // Add CPU-bound job first,
            chain.addLast("codec", new ProtocolCodecFilter(encoder, decoder));
    
            // and then a thread pool.
            executorService = Executors.newCachedThreadPool();
            chain.addLast("threadPool", new ExecutorFilter(executorService));    
            
            DatagramSessionConfig dcfg = acceptor.getSessionConfig();
            dcfg.setReuseAddress(true);
        }
        catch (Exception ex) {
            log.error("Failed to initialize server: " + ex, ex);
            throw ex;
        }
    }
    
    /**
     * Start the unicast server.
     * 
     * @throws Exception the exception
     */
    public void start() throws Exception
    {
    	try {
	    	List<SocketAddress> defaultAddrs = acceptor.getDefaultLocalAddresses();
	        for (SocketAddress socketAddress : defaultAddrs) {
	            log.info("Binding to local address: " + socketAddress);
	        }
	        acceptor.bind();
    	}
    	catch (Exception ex) {
    		log.error("Failed to start Unicast server thread", ex);
    		throw ex;
    	}
    }
    
    // TODO: Support calling this shutdown method somehow
    //       see - http://java.sun.com/j2se/1.4.2/docs/guide/lang/hook-design.html
    /**
     * Shutdown.
     */
    protected void shutdown()
    {
        acceptor.unbind();
        executorService.shutdown();        
    }
    
    /**
     * Register jmx.
     * 
     * @param service the service
     */
    protected void registerJmx(IoService service)
    {
        try {
            IoServiceMBean serviceMBean = new IoServiceMBean(service);
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();  
            ObjectName name = new ObjectName( "com.jagornet.dhcpv6:type=IOServiceMBean,name=NioDhcpServer" );
            mbs.registerMBean(serviceMBean, name);
            service.addListener( new IoServiceListener()
            {
                public void serviceActivated(IoService service) {
                }

                public void serviceDeactivated(IoService service) {
                }

                public void serviceIdle(IoService service, IdleStatus idleStatus) {
                }

                public void sessionCreated(IoSession session)
                {
                    try {
                        IoSessionMBean sessionMBean = new IoSessionMBean(session);
                        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();  
                        ObjectName name = 
                            new ObjectName("com.jagornet.dhcpv6:type=IoSessionMBean,name=" + 
                                           session.getRemoteAddress().toString().replace( ':', '/' ) );
                        mbs.registerMBean(sessionMBean, name);
                    }
                    catch(JMException ex) {
                        log.error("JMX Exception in sessionCreated: ", ex);
                    }      
                }

                public void sessionDestroyed(IoSession session)
                {
                    try {
                        ObjectName name = 
                            new ObjectName("com.jagornet.dhcpv6:type=IoSessionMBean,name=" + 
                                           session.getRemoteAddress().toString().replace( ':', '/' ) );
                        ManagementFactory.getPlatformMBeanServer().unregisterMBean(name);
                    }
                    catch(JMException ex) {
                        log.error("JMX Exception in sessionDestroyed: ", ex);
                    }      
                }
            });

        }
        catch (Exception ex) {
            log.error("Failure registering server in JMX: " + ex);
        }
    }
}
