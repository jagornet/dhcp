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

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
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

import com.jagornet.dhcpv6.server.config.DhcpServerConfigException;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.util.Util;

/**
 * This is a JBoss Netty based DHCPv6 server that uses
 * Java NIO DatagramChannels for receiving unicast messages.
 * It uses OIO (java.net) DatagramSockets for receiving
 * multicast messages.  Java 7 is required to support
 * MulticastChannels.
 * 
 * @author A. Gregory Rabil
 */
public class NettyDhcpServer
{
	private static Logger log = LoggerFactory.getLogger(NettyDhcpServer.class);
	
	/** The unicast socket addresses */
	private List<InetAddress> addrs;
	
	/** The mulitcast network interfaces */
	private List<NetworkInterface> netIfs;
	
	/** The DHCPv6 server port. */
	private int port;
	
	private List<InetAddress> v4Addrs;
	private NetworkInterface v4NetIf;
	
	private int v4Port;
	
    /** The collection of channels this server listens on. */
    protected Collection<DatagramChannel> channels = new ArrayList<DatagramChannel>();
    
    /** The executor service thread pool for processing requests. */
    protected ExecutorService executorService = Executors.newCachedThreadPool();
    
    /**
     * Create a NettyDhcpServer.
     * 
     * @param port the port to listen on
     * @param addrs the addresses to listen on for unicast traffic
     * @param netIfs the network interfaces to listen on for multicast traffic
     */
    public NettyDhcpServer(List<InetAddress> addrs, List<NetworkInterface> netIfs, int port,
    						List<InetAddress> v4Addrs, NetworkInterface v4NetIf, int v4Port)
    {
    	this.addrs = addrs;
    	this.netIfs = netIfs;
    	this.port = port;
    	this.v4Addrs = v4Addrs;
    	this.v4NetIf = v4NetIf;
    	this.v4Port = v4Port;
    }
    
    /**
     * Start the server.
     * 
     * @throws Exception the exception
     */
    public void start() throws Exception
    {
        try {
        	InternalLoggerFactory.setDefaultFactory(new Log4JLoggerFactory());
        	
        	boolean ignoreSelfPackets = 
        			DhcpServerPolicies.globalPolicyAsBoolean(Property.DHCP_IGNORE_SELF_PACKETS);
        	int corePoolSize = 
        			DhcpServerPolicies.globalPolicyAsInt(Property.CHANNEL_THREADPOOL_SIZE);
        	int maxChannelMemorySize = 
        			DhcpServerPolicies.globalPolicyAsInt(Property.CHANNEL_MAX_CHANNEL_MEMORY);
        	int maxTotalMemorySize = 
        			DhcpServerPolicies.globalPolicyAsInt(Property.CHANNEL_MAX_TOTAL_MEMORY);
        	int receiveBufSize = 
        			DhcpServerPolicies.globalPolicyAsInt(Property.CHANNEL_READ_BUFFER_SIZE);
        	int sendBufSize = 
        			DhcpServerPolicies.globalPolicyAsInt(Property.CHANNEL_WRITE_BUFFER_SIZE);
        	
        	log.info("Initializing channels:" + 
        			" corePoolSize=" + corePoolSize +
        			" maxChannelMemorySize=" + maxChannelMemorySize +
        			" maxTotalMemorySize=" + maxTotalMemorySize + 
        			" receiveBufferSize=" + receiveBufSize +
        			" sendBufferSize=" + sendBufSize);
        	
        	if (addrs != null) {
	        	for (InetAddress addr : addrs) {
	        		// local address for packets received on this channel
		            InetSocketAddress sockAddr = new InetSocketAddress(addr, port); 
	        		ChannelPipeline pipeline = Channels.pipeline();
		            pipeline.addLast("logger", new LoggingHandler());
		            pipeline.addLast("decoder", new DhcpUnicastChannelDecoder(sockAddr, ignoreSelfPackets));
		            pipeline.addLast("encoder", new DhcpChannelEncoder());
		            pipeline.addLast("executor", new ExecutionHandler(
		            		new OrderedMemoryAwareThreadPoolExecutor(corePoolSize, 
		            												maxChannelMemorySize,
		            												maxTotalMemorySize)));
		            pipeline.addLast("handler", new DhcpChannelHandler());
	        		
		            String io = null;
		            DatagramChannelFactory factory = null;
		            if (Util.IS_WINDOWS) {
		            	// Use OioDatagramChannels for IPv6 unicast addresses on Windows
		            	factory = new OioDatagramChannelFactory(executorService);
		            	io = "Old I/O";
		            }
		            else {
		            	// Use NioDatagramChannels for IPv6 unicast addresses on real OSes
		                factory = new NioDatagramChannelFactory(executorService);
		                io = "New I/O";
		            }
	
		            // create an unbound channel
		            DatagramChannel channel = factory.newChannel(pipeline);
		            //channel.getConfig().setReuseAddress(true);
		            channel.getConfig().setReceiveBufferSize(receiveBufSize);
		            channel.getConfig().setSendBufferSize(sendBufSize);
		            
		            log.info("Binding " + io + " datagram channel on IPv6 socket address: " + sockAddr);
		            ChannelFuture future = channel.bind(sockAddr);
		            future.await();
		            if (!future.isSuccess()) {
		            	log.error("Failed to bind unicast channel: " + future.getCause());
		            	throw new IOException(future.getCause());
		            }
		            channels.add(channel);
	        	}
        	}
        	
        	if (netIfs != null) {
	        	for (NetworkInterface netIf : netIfs) {
	        		// find the link local IPv6 address for this interface
	        		InetAddress addr = Util.netIfIPv6LinkLocalAddress(netIf);
	        		if (addr == null) {
	        			String msg = "No IPv6 link local addresses found on interface: " + netIf;
	        			log.error(msg);
	        			throw new DhcpServerConfigException(msg);	        			
	        		}
	        		// local address for packets received on this channel
		            InetSocketAddress sockAddr = new InetSocketAddress(addr, port); 
		            ChannelPipeline pipeline = Channels.pipeline();
		            pipeline.addLast("logger", new LoggingHandler());
		            pipeline.addLast("decoder", new DhcpChannelDecoder(sockAddr, ignoreSelfPackets));
		            pipeline.addLast("encoder", new DhcpChannelEncoder());
		            pipeline.addLast("executor", new ExecutionHandler(
		            		new OrderedMemoryAwareThreadPoolExecutor(corePoolSize, 
																	maxChannelMemorySize,
																	maxTotalMemorySize)));
		            pipeline.addLast("handler", new DhcpChannelHandler());
	
		        	// Use OioDatagramChannels for IPv6 multicast interfaces
		            DatagramChannelFactory factory =
		                new OioDatagramChannelFactory(executorService);
	
		            // create an unbound channel
		            DatagramChannel channel = factory.newChannel(pipeline);
		            channel.getConfig().setReceiveBufferSize(receiveBufSize);
		            channel.getConfig().setSendBufferSize(sendBufSize);
		            
		            // must be bound in order to join multicast group
		            SocketAddress wildAddr = new InetSocketAddress(port);
		            log.info("Binding New I/O multicast channel on IPv6 wildcard address: " + wildAddr);
		            ChannelFuture future = channel.bind(wildAddr);
		            future.await();
		            if (!future.isSuccess()) {
		            	log.error("Failed to bind multicast channel: " + future.getCause());
		            	throw new IOException(future.getCause());
		            }
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
        	
    		Map<InetAddress, Channel> v4UcastChannels = new HashMap<InetAddress, Channel>();
        	if (v4Addrs != null) {
	        	for (InetAddress addr : v4Addrs) {
	        		// local address for packets received on this channel
		            InetSocketAddress sockAddr = new InetSocketAddress(addr, v4Port); 
	        		ChannelPipeline pipeline = Channels.pipeline();
		            pipeline.addLast("logger", new LoggingHandler());
		            pipeline.addLast("decoder", new DhcpV4UnicastChannelDecoder(sockAddr, ignoreSelfPackets));
		            pipeline.addLast("encoder", new DhcpV4ChannelEncoder());
		            pipeline.addLast("executor", new ExecutionHandler(
		            		new OrderedMemoryAwareThreadPoolExecutor(corePoolSize, 
																	maxChannelMemorySize,
																	maxTotalMemorySize)));
		            pipeline.addLast("handler", new DhcpV4ChannelHandler(null));
	        		
		            String io = null;
		            DatagramChannelFactory factory = null;
		            if (Util.IS_WINDOWS) {
		            	// Use OioDatagramChannels for IPv4 unicast addresses on Windows
		            	factory = new OioDatagramChannelFactory(executorService);
		            	io = "Old I/O";
		            }
		            else {
		            	// Use NioDatagramChannels for IPv4 unicast addresses on real OSes
		                factory = new NioDatagramChannelFactory(executorService);
		                io = "New I/O";
		            }
	
		            // create an unbound channel
		            DatagramChannel channel = factory.newChannel(pipeline);
		            //channel.getConfig().setReuseAddress(true);
		            channel.getConfig().setBroadcast(true);
		            channel.getConfig().setReceiveBufferSize(receiveBufSize);
		            channel.getConfig().setSendBufferSize(sendBufSize);

		            v4UcastChannels.put(addr, channel);
		            
		            log.info("Binding " + io + " datagram channel on IPv4 socket address: " + sockAddr);
		            ChannelFuture future = channel.bind(sockAddr);
		            future.await();
		            if (!future.isSuccess()) {
		            	log.error("Failed to bind unicast channel: " + future.getCause());
		            	throw new IOException(future.getCause());
		            }
		            channels.add(channel);
	        	}
        	}
        	
        	if (v4NetIf != null) {
        		boolean foundV4Addr = false;
        		// get the first v4 address on the interface and bind to it
        		Enumeration<InetAddress> addrs = v4NetIf.getInetAddresses();
        		while (addrs.hasMoreElements()) {
        			InetAddress addr = addrs.nextElement();
        			if (addr instanceof Inet4Address) {
        				Channel bcastChannel = v4UcastChannels.get(addr);
        				if (bcastChannel == null) {
                			String msg = "IPv4 address: " + addr.getHostAddress() + 
                						" for broadcast interface: " + v4NetIf +
                						" must be included in unicast IPv4 address list";
                			log.error(msg);
                			throw new DhcpServerConfigException(msg);        					
        				}
		        		foundV4Addr = true;
			            InetSocketAddress sockAddr = new InetSocketAddress(addr, v4Port); 
        				ChannelPipeline pipeline = Channels.pipeline();
			            pipeline.addLast("logger", new LoggingHandler());
			            pipeline.addLast("decoder", new DhcpV4ChannelDecoder(sockAddr, ignoreSelfPackets));
			            pipeline.addLast("encoder", new DhcpV4ChannelEncoder());
			            pipeline.addLast("executor", new ExecutionHandler(
			            		new OrderedMemoryAwareThreadPoolExecutor(corePoolSize, 
																		maxChannelMemorySize,
																		maxTotalMemorySize)));
			            pipeline.addLast("handler", new DhcpV4ChannelHandler(bcastChannel));
		        		
			            DatagramChannelFactory factory = new NioDatagramChannelFactory(executorService);
		
			            // create an unbound channel
			            DatagramChannel channel = factory.newChannel(pipeline);
			            //channel.getConfig().setReuseAddress(true);
			            channel.getConfig().setReceiveBufferSize(receiveBufSize);
			            channel.getConfig().setSendBufferSize(sendBufSize);
			            channel.getConfig().setBroadcast(true);
			            
			            InetSocketAddress wildAddr = new InetSocketAddress(v4Port);
			            log.info("Binding New I/O datagram channel on IPv4 wildcard address: " + wildAddr);
			            ChannelFuture future = channel.bind(wildAddr);
			            future.await();
			            if (!future.isSuccess()) {
			            	log.error("Failed to bind IPv4 channel: " + future.getCause());
			            	throw new IOException(future.getCause());
			            }
			            channels.add(channel);
			            break; 	// no need to continue looking at IPs on the interface
        			}
        		}
        		if (!foundV4Addr) {
        			String msg = "No IPv4 addresses found on interface: " + v4NetIf;
        			log.error(msg);
        			throw new DhcpServerConfigException(msg);
        		}
        	}
        }
        catch (Exception ex) {
            log.error("Failed to initialize server: " + ex, ex);
            throw ex;
        }

    	Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
            	  shutdown();
                }
            });
    }
    
    /**
     * Shutdown.
     */
    public void shutdown()
    {
    	log.info("Closing channels");
		for (DatagramChannel channel : channels) {
			channel.close();
		}
		log.info("Executor shutdown");
        executorService.shutdown();     
    }
}
