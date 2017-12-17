/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file NettyDhcpServer.java is part of Jagornet DHCP.
 *
 *   Jagornet DHCP is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Jagornet DHCP is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Jagornet DHCP.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcp.server.netty4;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.config.DhcpServerConfigException;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.oio.OioDatagramChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4JLoggerFactory;

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
	
	/** The V6 unicast socket addresses */
	private List<InetAddress> v6Addrs;
	
	/** The V6 mulitcast network interfaces */
	private List<NetworkInterface> v6NetIfs;
	
	/** The DHCPv6 server port. */
	private int v6Port;
	
	/** The V4 unicast socket addresses */
	private List<InetAddress> v4Addrs;

	/** The V4 broadcast network interface */
	private NetworkInterface v4NetIf;
	
	/** The DHCPv4 server port. */
	private int v4Port;
	
    /** The collection of channels this server listens on. */
    protected Collection<DatagramChannel> channels = new ArrayList<DatagramChannel>();
    
    /** The executor service thread pool for processing requests. */
    //protected ExecutorService executorService = Executors.newCachedThreadPool();
    protected EventExecutorGroup eventExecutorGroup = new DefaultEventExecutorGroup(16);
    
    /**
     * Create a NettyDhcpServer.
     * 
     * @param v6Addrs the addresses to listen on for unicast traffic
     * @param v6NetIfs the network interfaces to listen on for multicast traffic
     * @param v6Port the port to listen on
     * @param v4Addrs the addresses to listen on for unicast traffic
     * @param v4NetIf the network interface to listen on for broadcast traffic
     * @param v4Port the port to listen on
     */
    public NettyDhcpServer(List<InetAddress> v6Addrs, List<NetworkInterface> v6NetIfs, int v6Port,
    						List<InetAddress> v4Addrs, NetworkInterface v4NetIf, int v4Port)
    {
    	this.v6Addrs = v6Addrs;
    	this.v6NetIfs = v6NetIfs;
    	this.v6Port = v6Port;
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
        	
        	final boolean ignoreSelfPackets = 
        			DhcpServerPolicies.globalPolicyAsBoolean(Property.DHCP_IGNORE_SELF_PACKETS);
        	final int corePoolSize = 
        			DhcpServerPolicies.globalPolicyAsInt(Property.CHANNEL_THREADPOOL_SIZE);
        	final int maxChannelMemorySize = 
        			DhcpServerPolicies.globalPolicyAsInt(Property.CHANNEL_MAX_CHANNEL_MEMORY);
        	final int maxTotalMemorySize = 
        			DhcpServerPolicies.globalPolicyAsInt(Property.CHANNEL_MAX_TOTAL_MEMORY);
        	final int receiveBufSize = 
        			DhcpServerPolicies.globalPolicyAsInt(Property.CHANNEL_READ_BUFFER_SIZE);
        	final int sendBufSize = 
        			DhcpServerPolicies.globalPolicyAsInt(Property.CHANNEL_WRITE_BUFFER_SIZE);
        	
        	log.info("Initializing channels:" + 
        			" corePoolSize=" + corePoolSize +
        			" maxChannelMemorySize=" + maxChannelMemorySize +
        			" maxTotalMemorySize=" + maxTotalMemorySize + 
        			" receiveBufferSize=" + receiveBufSize +
        			" sendBufferSize=" + sendBufSize);
        	
        	boolean v6SocketChecked = false;
        	if (v6Addrs != null) {
        		// test if this socket is already in use, which means
        		// there is probably already a DHCPv6 server running
        		checkSocket(v6Port);
        		v6SocketChecked = true;
	        	for (InetAddress addr : v6Addrs) {
	        		// local address for packets received on this channel
		            final InetSocketAddress sockAddr = new InetSocketAddress(addr, v6Port);

	        		Bootstrap bootstrap = new Bootstrap();
		            String io = null;
	            	EventLoopGroup group = null;
	            	if (Util.IS_WINDOWS) {
	            		// Use OioDatagramChannels for IPv6 unicast addresses on Windows
		            	bootstrap.channel(OioDatagramChannel.class);
	            		group = new OioEventLoopGroup();
		            	io = "Old I/O";
	            	}
	            	else {
	            		// Use NioDatagramChannels for IPv6 unicast addresses on real OSes
		            	bootstrap.channel(NioDatagramChannel.class);
	            		group = new NioEventLoopGroup();
		            	io = "New I/O";
	            	}	            	
	            	bootstrap.group(group);
	            	bootstrap.option(ChannelOption.SO_REUSEADDR, true);
	            	bootstrap.option(ChannelOption.SO_RCVBUF, receiveBufSize);
	            	bootstrap.option(ChannelOption.SO_SNDBUF, sendBufSize);
	            	bootstrap.handler(new ChannelInitializer<DatagramChannel>() {
						@Override
						protected void initChannel(DatagramChannel channel) throws Exception {
			        		ChannelPipeline pipeline = channel.pipeline();
				            pipeline.addLast("logger", new LoggingHandler());
				            pipeline.addLast("decoder", new DhcpV6UnicastChannelDecoder(sockAddr, ignoreSelfPackets));
// don't think the encoder is needed now that the handler send the reply directly
//				            pipeline.addLast("encoder", new DhcpV6ChannelEncoder());
// don't need executor because hander has executor group
//				            pipeline.addLast("executor", new ExecutionHandler(
//				            		new OrderedMemoryAwareThreadPoolExecutor(corePoolSize, 
//				            												maxChannelMemorySize,
//				            												maxTotalMemorySize)));
				            pipeline.addLast(eventExecutorGroup, "handler", new DhcpV6ChannelHandler());
						}
	            	});
		            
		            log.info("Binding " + io + " datagram channel on IPv6 socket address: " + sockAddr);
		            ChannelFuture future = bootstrap.bind(sockAddr);
		            future.await();
		            if (!future.isSuccess()) {
		            	log.error("Failed to bind to IPv6 unicast channel: " + future.cause());
		            	throw new IOException(future.cause());
		            }
//TODO		            channels.add(channel);
	        	}
        	}
        	
        	if (v6NetIfs != null) {
        		if (!v6SocketChecked) {
        			// if in-use socket check has not been done yet, then do it
        			checkSocket(v6Port);
        		}
	        	for (NetworkInterface netIf : v6NetIfs) {
	        		// find the link local IPv6 address for this interface
	        		InetAddress addr = Util.netIfIPv6LinkLocalAddress(netIf);
	        		if (addr == null) {
	        			String msg = "No IPv6 link local addresses found on interface: " + netIf;
	        			log.error(msg);
	        			throw new DhcpServerConfigException(msg);	        			
	        		}
	        		// local address for packets received on this channel
		            final InetSocketAddress sockAddr = new InetSocketAddress(addr, v6Port); 

		            Bootstrap bootstrap = new Bootstrap();
		        	// Use OioDatagramChannels for IPv6 multicast interfaces
		            //TODO: why Oio?
	            	bootstrap.channel(OioDatagramChannel.class);
	            	bootstrap.group(new OioEventLoopGroup());
	            	bootstrap.option(ChannelOption.SO_REUSEADDR, true);
	            	bootstrap.option(ChannelOption.SO_RCVBUF, receiveBufSize);
	            	bootstrap.option(ChannelOption.SO_SNDBUF, sendBufSize);
	            	bootstrap.handler(new ChannelInitializer<DatagramChannel>() {
						@Override
						protected void initChannel(DatagramChannel channel) throws Exception {
			        		ChannelPipeline pipeline = channel.pipeline();
				            pipeline.addLast("logger", new LoggingHandler());
				            pipeline.addLast("decoder", new DhcpV6ChannelDecoder(sockAddr, ignoreSelfPackets));
// don't think the encoder is needed now that the handler send the reply directly
//				            pipeline.addLast("encoder", new DhcpV6ChannelEncoder());
// don't need executor because hander has executor group
//				            pipeline.addLast("executor", new ExecutionHandler(
//				            		new OrderedMemoryAwareThreadPoolExecutor(corePoolSize, 
//																			maxChannelMemorySize,
//																			maxTotalMemorySize)));
				            pipeline.addLast(eventExecutorGroup, "handler", new DhcpV6ChannelHandler());
						}
	            	});
	            	
		            // must be bound in order to join multicast group
		            InetSocketAddress wildAddr = new InetSocketAddress(DhcpConstants.ZEROADDR_V6, v6Port);
		            //log.info("Binding New I/O multicast channel on IPv6 wildcard address: " + wildAddr);
		            //TODO: why Oio?
		            log.info("Binding Old I/O multicast channel on IPv6 wildcard address: " + wildAddr);
		            ChannelFuture future = bootstrap.bind(wildAddr);
		            future.await();
		            if (!future.isSuccess()) {
		            	log.error("Failed to bind to IPv6 multicast channel: " + future.cause());
		            	throw new IOException(future.cause());
		            }
		            InetSocketAddress relayGroup = 
		            	new InetSocketAddress(DhcpConstants.ALL_DHCP_RELAY_AGENTS_AND_SERVERS, v6Port);
		            log.info("Joining multicast group: " + relayGroup +
		            		" on interface: " + netIf.getName());
		            //TODO: get channel to join group
//		            bootstrap.joinGroup(relayGroup, netIf);
//		            InetSocketAddress serverGroup = 
//		            	new InetSocketAddress(DhcpConstants.ALL_DHCP_SERVERS, v6Port); 
//		            log.info("Joining multicast group: " + serverGroup +
//		            		" on interface: " + netIf.getName());
//		            bootstrap.joinGroup(serverGroup, netIf);
//		            channels.add(channel);
	        	}
        	}
        	
        	boolean v4SocketChecked = false;
    		Map<InetAddress, Channel> v4UcastChannels = new HashMap<InetAddress, Channel>();
        	if (v4Addrs != null) {
        		checkSocket(v4Port);
        		v4SocketChecked = true;
	        	for (InetAddress addr : v4Addrs) {
	        		// local address for packets received on this channel
		            final InetSocketAddress sockAddr = new InetSocketAddress(addr, v4Port); 
		            
		            Bootstrap bootstrap = new Bootstrap();
		            String io = null;
	            	EventLoopGroup group = null;
	            	if (Util.IS_WINDOWS) {
		            	// Use OioDatagramChannels for IPv4 unicast addresses on Windows
		            	bootstrap.channel(OioDatagramChannel.class);
	            		group = new OioEventLoopGroup();
		            	io = "Old I/O";
	            	}
	            	else {
		            	// Use NioDatagramChannels for IPv4 unicast addresses on real OSes
		            	bootstrap.channel(NioDatagramChannel.class);
	            		group = new NioEventLoopGroup();
		            	io = "New I/O";
	            	}	            	
	            	bootstrap.group(group);
	            	bootstrap.option(ChannelOption.SO_REUSEADDR, true);
	            	bootstrap.option(ChannelOption.SO_BROADCAST, true);
	            	bootstrap.option(ChannelOption.SO_RCVBUF, receiveBufSize);
	            	bootstrap.option(ChannelOption.SO_SNDBUF, sendBufSize);
	            	bootstrap.handler(new ChannelInitializer<DatagramChannel>() {
						@Override
						protected void initChannel(DatagramChannel channel) throws Exception {
			        		ChannelPipeline pipeline = channel.pipeline();
				            pipeline.addLast("logger", new LoggingHandler());
				            pipeline.addLast("decoder", new DhcpV4UnicastChannelDecoder(sockAddr, ignoreSelfPackets));
// don't think the encoder is needed now that the handler send the reply directly
//				            pipeline.addLast("encoder", new DhcpV4ChannelEncoder());
// don't need executor because hander has executor group
//				            pipeline.addLast("executor", new ExecutionHandler(
//				            		new OrderedMemoryAwareThreadPoolExecutor(corePoolSize, 
//																			maxChannelMemorySize,
//																			maxTotalMemorySize)));
				            pipeline.addLast(eventExecutorGroup, "handler", new DhcpV4ChannelHandler(null));
						}	        		
	            	});

//TODO		            v4UcastChannels.put(addr, channel);
		            
		            log.info("Binding " + io + " datagram channel on IPv4 socket address: " + sockAddr);
		            ChannelFuture future = bootstrap.bind(sockAddr);
		            future.await();
		            if (!future.isSuccess()) {
		            	log.error("Failed to bind to IPv4 unicast channel: " + future.cause());
		            	throw new IOException(future.cause());
		            }
//TODO		            channels.add(channel);
	        	}
        	}
        	
        	if (v4NetIf != null) {
        		if (!v4SocketChecked) {
        			// if in-use socket check has not been done yet, then do it
        			checkSocket(v4Port);
        		}
        		boolean foundV4Addr = false;
        		// get the first v4 address on the interface and bind to it
        		Enumeration<InetAddress> addrs = v4NetIf.getInetAddresses();
        		while (addrs.hasMoreElements()) {
        			InetAddress addr = addrs.nextElement();
        			if (addr instanceof Inet4Address) {
        				final Channel bcastChannel = v4UcastChannels.get(addr);
        				if (bcastChannel == null) {
                			String msg = "IPv4 address: " + addr.getHostAddress() + 
                						" for broadcast interface: " + v4NetIf +
                						" must be included in unicast IPv4 address list";
                			log.error(msg);
                			throw new DhcpServerConfigException(msg);        					
        				}
		        		foundV4Addr = true;
			            final InetSocketAddress sockAddr = new InetSocketAddress(addr, v4Port); 
			            Bootstrap bootstrap = new Bootstrap();
		            	bootstrap.channel(NioDatagramChannel.class);
		            	bootstrap.group(new NioEventLoopGroup());
		            	bootstrap.option(ChannelOption.SO_REUSEADDR, true);
		            	bootstrap.option(ChannelOption.SO_BROADCAST, true);
		            	bootstrap.option(ChannelOption.SO_RCVBUF, receiveBufSize);
		            	bootstrap.option(ChannelOption.SO_SNDBUF, sendBufSize);
		            	bootstrap.handler(new ChannelInitializer<DatagramChannel>() {
							@Override
							protected void initChannel(DatagramChannel channel) throws Exception {
				        		ChannelPipeline pipeline = channel.pipeline();
					            pipeline.addLast("logger", new LoggingHandler());
					            pipeline.addLast("decoder", new DhcpV4ChannelDecoder(sockAddr, ignoreSelfPackets));
// don't think the encoder is needed now that the handler send the reply directly
//					            pipeline.addLast("encoder", new DhcpV4ChannelEncoder());
// don't need executor because hander has executor group
//					            pipeline.addLast("executor", new ExecutionHandler(
//					            		new OrderedMemoryAwareThreadPoolExecutor(corePoolSize, 
//																				maxChannelMemorySize,
//																				maxTotalMemorySize)));
					            pipeline.addLast(eventExecutorGroup, "handler", new DhcpV4ChannelHandler(bcastChannel));
							}
		            	});
			            
			            InetSocketAddress wildAddr = new InetSocketAddress(DhcpConstants.ZEROADDR_V4, v4Port);
			            log.info("Binding New I/O datagram channel on IPv4 wildcard address: " + wildAddr);
			            ChannelFuture future = bootstrap.bind(wildAddr);
			            future.await();
			            if (!future.isSuccess()) {
			            	log.error("Failed to bind to IPv4 broadcast channel: " + future.cause());
			            	throw new IOException(future.cause());
			            }
//TODO			            channels.add(channel);
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
    
    private void checkSocket(int port) throws SocketException {
    	DatagramSocket ds = null;
    	try {
    		log.info("Checking for existing socket on port=" + port);
    		ds = new DatagramSocket(port);
    	}
    	finally {
    		if (ds != null) {
    			ds.close();
    		}
    	}
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
//TODO        executorService.shutdown();     
    }
}
