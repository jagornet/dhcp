/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file NettyDhcpServer.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.server.netty;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.DatagramChannel;
import org.jboss.netty.channel.socket.DatagramChannelFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.jboss.netty.channel.socket.oio.OioDatagramChannelFactory;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Log4JLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.util.Util;

// TODO: Auto-generated Javadoc
/**
 * This is a JBoss Netty based DHCPv6 server that uses
 * Java NIO DatagramChannels for receiving unicast messages.
 * 
 * Note: Java 7 should support MulticastChannels, and then
 * this class can be refactored to support both unicast and
 * multicast packets.
 * 
 * @author A. Gregory Rabil
 */
public class NettyDhcpServer
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(NettyDhcpServer.class);
	
	/** The addrs. */
	private List<InetAddress> addrs;		// unicast socket addresses
	
	/** The net ifs. */
	private List<NetworkInterface> netIfs;	// multicast socket interfaces
	
	/** The port. */
	private int port;
	
    /** The bootstrap. */
    protected Collection<DatagramChannel> channels = new ArrayList<DatagramChannel>();
    
    /** The executor service. */
    protected ExecutorService executorService;
    
    /**
     * Instantiates a new unicast dhcp server.
     * 
     * @param port the port
     * @param addrs the addrs
     * @param netIfs the net ifs
     * 
     * @throws Exception the exception
     */
    public NettyDhcpServer(List<InetAddress> addrs, 
    						List<NetworkInterface> netIfs, int port) throws Exception
    {
    	this.addrs = addrs;
    	this.netIfs = netIfs;
    	this.port = port;
    }
    
    /**
     * Start the unicast server.
     * 
     * @throws Exception the exception
     */
    public void start() throws Exception
    {
        try {
        	executorService = Executors.newCachedThreadPool();
        	InternalLoggerFactory.setDefaultFactory(new Log4JLoggerFactory());
        	
        	for (InetAddress addr : addrs) {

        		ChannelPipeline pipeline = Channels.pipeline();
	            pipeline.addLast("logger", new LoggingHandler());
	            pipeline.addLast("decoder", new DhcpChannelDecoder());
	            pipeline.addLast("encoder", new DhcpChannelEncoder());
	            pipeline.addLast("executor", new ExecutionHandler(
	            		new OrderedMemoryAwareThreadPoolExecutor(16, 1048576, 1048576)));
	            pipeline.addLast("handler", new DhcpChannelHandler());
        		
	            DatagramChannelFactory factory =
	                new NioDatagramChannelFactory(executorService);

	            // create an unbound channel
	            DatagramChannel channel = factory.newChannel(pipeline);
	            channel.getConfig().setReuseAddress(true);
	            
	            SocketAddress sockAddr = new InetSocketAddress(addr, port); 
	            log.info("Binding datagram channel on IPv6 socket address: " + sockAddr);
	            channel.bind(sockAddr);
	            channels.add(channel);
        	}
        	
        	for (NetworkInterface netIf : netIfs) {
        		
	            ChannelPipeline pipeline = Channels.pipeline();
	            pipeline.addLast("logger", new LoggingHandler());
	            pipeline.addLast("decoder", new DhcpChannelDecoder());
	            pipeline.addLast("encoder", new DhcpChannelEncoder());
	            pipeline.addLast("executor", new ExecutionHandler(
	            		new OrderedMemoryAwareThreadPoolExecutor(16, 1048576, 1048576)));
	            pipeline.addLast("handler", new DhcpChannelHandler());

	            DatagramChannelFactory factory =
	                new OioDatagramChannelFactory(executorService);

	            // create an unbound channel
	            DatagramChannel channel = factory.newChannel(pipeline);	            

	            InetAddress v6Addr = Util.netIfIPv6LinkLocalAddress(netIf);
	            
	            // must be bound in order to join multicast group
	            SocketAddress sockAddr = new InetSocketAddress(v6Addr, port); 
	            log.info("Binding multicast channel on IPv6 socket address: " + sockAddr);
	            channel.bind(sockAddr);
	            
	            InetSocketAddress relayGroup = 
	            	new InetSocketAddress(DhcpConstants.ALL_DHCP_RELAY_AGENTS_AND_SERVERS, port);
	            log.info("Joining multicast group: " + relayGroup +
	            		" on interface: " + netIf.getName());
	            channel.joinGroup(relayGroup, netIf);
	            InetSocketAddress serverGroup = 
	            	new InetSocketAddress(DhcpConstants.ALL_DHCP_SERVERS, port); 
	            log.info("Joining multicast group: " + serverGroup +
	            		" on interface: " + netIf.getName());
	            channel.joinGroup(serverGroup, netIf);
	            channels.add(channel);
        	}
        }
        catch (Exception ex) {
            log.error("Failed to initialize server: " + ex, ex);
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
		for (DatagramChannel channel : channels) {
			channel.close();
		}
        executorService.shutdown();        
    }
}
