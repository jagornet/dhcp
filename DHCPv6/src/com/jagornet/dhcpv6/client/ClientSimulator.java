/*
 * Copyright 2011 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file ClientSimulator.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.client;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.DatagramChannel;
import org.jboss.netty.channel.socket.DatagramChannelFactory;
import org.jboss.netty.channel.socket.oio.OioDatagramChannelFactory;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpClientIdOption;
import com.jagornet.dhcpv6.option.DhcpElapsedTimeOption;
import com.jagornet.dhcpv6.option.DhcpIaNaOption;
import com.jagornet.dhcpv6.server.netty.DhcpChannelDecoder;
import com.jagornet.dhcpv6.server.netty.DhcpChannelEncoder;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.util.Util;

/**
 * A test client that sends solict/request/release messages 
 * to a DHCPv6 server via multicast.
 * 
 * @author A. Gregory Rabil
 */
@ChannelHandler.Sharable
public class ClientSimulator extends SimpleChannelUpstreamHandler
{
	private static Logger log = LoggerFactory.getLogger(ClientSimulator.class);

	protected Random random = new Random();
    protected Options options = new Options();
    protected CommandLineParser parser = new BasicParser();
    protected HelpFormatter formatter;

    protected NetworkInterface DEFAULT_NETIF = null;
    protected NetworkInterface mcastNetIf = null;
   	protected InetAddress DEFAULT_ADDR;
    protected InetAddress serverAddr;
    protected int serverPort = DhcpConstants.SERVER_PORT;
    protected int clientPort = DhcpConstants.CLIENT_PORT;
    protected boolean rapidCommit = false;
    protected int numRequests = 100;
    protected AtomicInteger solicitsSent = new AtomicInteger();
    protected AtomicInteger advertisementsReceived = new AtomicInteger();
    protected AtomicInteger requestsSent = new AtomicInteger();
    protected AtomicInteger requestRepliesReceived = new AtomicInteger();
    protected AtomicInteger releasesSent = new AtomicInteger();
    protected AtomicInteger releaseRepliesReceived = new AtomicInteger();
    protected int successCnt = 0;
    protected long startTime = 0;    
    protected long endTime = 0;
    protected long timeout = 0;
    protected int poolSize = 0;
    protected Object syncDone = new Object();

    protected InetSocketAddress server = null;
    protected InetSocketAddress client = null;
    
    protected DatagramChannel channel = null;	
	protected ExecutorService executor = Executors.newCachedThreadPool();
	
	protected Map<String, ClientMachine> clientMap =
			Collections.synchronizedMap(new HashMap<String, ClientMachine>());

    /**
     * Instantiates a new test client.
     *
     * @param args the args
     * @throws Exception the exception
     */
    public ClientSimulator(String[] args) throws Exception 
    {
    	DEFAULT_NETIF = NetworkInterface.getNetworkInterfaces().nextElement();
    	DEFAULT_ADDR = DhcpConstants.ALL_DHCP_RELAY_AGENTS_AND_SERVERS;
    	
        setupOptions();

        if(!parseOptions(args)) {
            formatter = new HelpFormatter();
            String cliName = this.getClass().getName();
            formatter.printHelp(cliName, options);
            System.exit(0);
        }
        
        try {
			start();
		} 
        catch (Exception ex) {
			ex.printStackTrace();
		}
    }
    
	/**
	 * Setup options.
	 */
	private void setupOptions()
    {
		Option numOption = new Option("n", "number", true,
										"Number of client requests to send" +
										" [" + numRequests + "]");
		options.addOption(numOption);
		
        Option miOption = new Option("mi", "multicastinterface", true,
        								"Multicast interface of the DHCPv6 Client" +
        								" [" + DEFAULT_NETIF.getName() + "]");
        options.addOption(miOption);
		
        Option saOption = new Option("sa", "serveraddress", true,
        								"Address of DHCPv6 Server" +
        								" [" + DEFAULT_ADDR + "]");		
        options.addOption(saOption);

        Option cpOption = new Option("cp", "clientport", true,
        							  "Client Port Number" +
        							  " [" + clientPort + "]");
        options.addOption(cpOption);

        Option spOption = new Option("sp", "serverport", true,
        							  "Server Port Number" +
        							  " [" + serverPort + "]");
        options.addOption(spOption);
        
        Option rOption = new Option("r", "rapidcommit", false,
        							"Send rapid-commit Solicit requests");
        options.addOption(rOption);
        
        Option toOption = new Option("to", "timeout", true,
        							"Timeout");
        options.addOption(toOption);
        
        Option psOption = new Option("ps", "poolsize", true,
        							"Size of the pool; wait for release after this many requests");
        options.addOption(psOption);
        
        Option helpOption = new Option("?", "help", false, "Show this help page.");
        
        options.addOption(helpOption);
    }

	
	protected int parseIntegerOption(String opt, String str, int defval) {
    	int val = defval;
    	try {
    		val = Integer.parseInt(str);
    	}
    	catch (NumberFormatException ex) {
    		System.err.println("Invalid " + opt + " '" + str +
    							"' using default: " + defval +
    							" Exception=" + ex);
    		val = defval;
    	}
    	return val;
	}
	
	protected InetAddress parseIpAddressOption(String opt, String str, InetAddress defaddr) {
    	InetAddress addr = defaddr;
    	try {
    		addr = InetAddress.getByName(str);
    	}
    	catch (UnknownHostException ex) {
    		System.err.println("Invalid " + opt + " address: '" + str +
    							"' using default: " + defaddr +
    							" Exception=" + ex);
    		addr = defaddr;
    	}
    	return addr;
	}
	
    /**
     * Parses the options.
     * 
     * @param args the args
     * 
     * @return true, if successful
     */
    protected boolean parseOptions(String[] args)
    {
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("?")) {
                return false;
            }
            if (cmd.hasOption("n")) {
            	numRequests = 
            			parseIntegerOption("num requests", cmd.getOptionValue("n"), 100);
            }
            mcastNetIf = DEFAULT_NETIF;
            if (cmd.hasOption("mi")) {
            	try {
					mcastNetIf = NetworkInterface.getByName(cmd.getOptionValue("mi"));
				} 
            	catch (SocketException e) {
					e.printStackTrace();
					return false;
				}
            }
            serverAddr = DEFAULT_ADDR;
            if (cmd.hasOption("sa")) {
            	serverAddr = 
            			parseIpAddressOption("server", cmd.getOptionValue("sa"), DEFAULT_ADDR);
            }
            if (cmd.hasOption("cp")) {
            	clientPort = 
            			parseIntegerOption("client port", cmd.getOptionValue("cp"), 
            								DhcpConstants.CLIENT_PORT);
            }
            if (cmd.hasOption("sp")) {
            	serverPort = 
            			parseIntegerOption("server port", cmd.getOptionValue("sp"), 
            								DhcpConstants.SERVER_PORT);
            }
            if (cmd.hasOption("r")) {
            	rapidCommit = true;
            }
            if (cmd.hasOption("to")) {
            	timeout = 
            			parseIntegerOption("timeout", cmd.getOptionValue("to"), 0);
            }
            if (cmd.hasOption("ps")) {
            	poolSize = 
            			parseIntegerOption("pool size", cmd.getOptionValue("ps"), 0);
            }
        }
        catch (ParseException pe) {
            System.err.println("Command line option parsing failure: " + pe);
            return false;
		}
        return true;
    }
    
    /**
     * Start sending DHCPv6 SOLICITs.
     */
    public void start()
    {
    	DatagramChannelFactory factory = 
    		new OioDatagramChannelFactory(Executors.newCachedThreadPool());
    	
    	server = new InetSocketAddress(serverAddr, serverPort);
    	client = new InetSocketAddress(clientPort);
    	
		ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("logger", new LoggingHandler());
        pipeline.addLast("encoder", new DhcpChannelEncoder());
        pipeline.addLast("decoder", new DhcpChannelDecoder(client, false));
        pipeline.addLast("executor", new ExecutionHandler(
        		new OrderedMemoryAwareThreadPoolExecutor(16, 1048576, 1048576)));
        pipeline.addLast("handler", this);
    	
        channel = factory.newChannel(pipeline);
		channel.getConfig().setNetworkInterface(mcastNetIf);
    	channel.bind(client);

    	for (int i=1; i<=numRequests; i++) {
    		executor.execute(new ClientMachine(i));
    	}

    	synchronized (syncDone) {
    		long ms = timeout * 1000;
        	try {
        		log.info("Waiting total of " + timeout + " milliseconds for completion");
        		syncDone.wait(ms);
        	}
        	catch (InterruptedException ex) {
        		log.error("Interrupted", ex);
        	}
		}

		log.info("Complete: solicitsSent=" + solicitsSent +
				" advertisementsReceived=" + advertisementsReceived +
				" requestsSent=" + requestsSent +
				" requestRepliesReceived=" + requestRepliesReceived +
				" releasesSent=" + releasesSent +
				" releaseRepliesReceived=" + releaseRepliesReceived +
				" elapsedTime=" + (endTime - startTime) + "ms");

    	log.info("Shutting down executor...");
    	executor.shutdownNow();
    	log.info("Closing channel...");
    	channel.close();
    	log.info("Done.");
    	if ((solicitsSent.get() == advertisementsReceived.get()) &&
    			(requestsSent.get() == requestRepliesReceived.get()) &&
    			(releasesSent.get() == releaseRepliesReceived.get())) {
    		
    		System.exit(0);
    	}
    	else {
    		System.exit(1);
    	}
    }

    /**
     * The Class ClientMachine.
     */
    class ClientMachine implements Runnable, ChannelFutureListener
    {
    	DhcpMessage msg;
    	int id;
    	String duid;
    	boolean released;
    	
    	/**
	     * Instantiates a new client machine.
	     *
	     * @param msg the msg
	     * @param server the server
	     */
	    public ClientMachine(int id) {
    		this.id = id;
    		this.duid = buildDuid(id);
    		this.released = false;
    	}
		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			if (poolSize > 0) {
				synchronized (clientMap) {
					if (poolSize <= clientMap.size()) {
						try {
							log.info("Waiting for release...");
							clientMap.wait();
						} 
						catch (InterruptedException ex) {
							log.error("Interrupted", ex);
						}
					}
					clientMap.put(duid, this);
				}
			}
			else {
				clientMap.put(duid, this);
			}
			solicit();
		}
		
		public void solicit() {
			msg = buildSolicitMessage(duid); 
			ChannelFuture future = channel.write(msg, server);
			future.addListener(this);
		}
		
		public void request(DhcpMessage advertiseMsg) {
        	msg = buildRequestMessage(advertiseMsg);
			ChannelFuture future = channel.write(msg, server);
			future.addListener(this);
		}
		
		public void release(DhcpMessage confirmMsg) {
        	msg = buildReleaseMessage(confirmMsg);
			ChannelFuture future = channel.write(msg, server);
			future.addListener(this);
		}
		
		/* (non-Javadoc)
		 * @see org.jboss.netty.channel.ChannelFutureListener#operationComplete(org.jboss.netty.channel.ChannelFuture)
		 */
		@Override
		public void operationComplete(ChannelFuture future) throws Exception
		{
			if (future.isSuccess()) {
				if (startTime == 0) {
					startTime = System.currentTimeMillis();
					log.info("Starting at: " + startTime);
				}
				if (msg.getMessageType() == DhcpConstants.SOLICIT) {
					solicitsSent.getAndIncrement();
					log.info("Succesfully sent solicit message duid=" + duid +
							" cnt=" + solicitsSent);
				}
				else if (msg.getMessageType() == DhcpConstants.REQUEST) {
					requestsSent.getAndIncrement();
					log.info("Succesfully sent request message duid=" + duid +
							" cnt=" + requestsSent);
				}
				else if (msg.getMessageType() == DhcpConstants.RELEASE) {
					released = true;
					releasesSent.getAndIncrement();
					log.info("Succesfully sent release message duid=" + duid +
							" cnt=" + releasesSent);
				}
			}
			else {
				log.warn("Failed to send message id=" + msg.getTransactionId());
			}
		}
    }

    private String buildDuid(long id) {
        byte[] bid = BigInteger.valueOf(id).toByteArray();
        byte[] chAddr = new byte[6];
        chAddr[0] = (byte)0xde;
        chAddr[1] = (byte)0xb1;
        if (bid.length == 4) {
            chAddr[2] = bid[0];
            chAddr[3] = bid[1];
            chAddr[4] = bid[2];
            chAddr[5] = bid[3];
        }
        else if (bid.length == 3) {
	        chAddr[2] = 0;
	        chAddr[3] = bid[0];
	        chAddr[4] = bid[1];
	        chAddr[5] = bid[2];
        }
        else if (bid.length == 2) {
	        chAddr[2] = 0;
	        chAddr[3] = 0;
	        chAddr[4] = bid[0];
	        chAddr[5] = bid[1];
        }
        else if (bid.length == 1) {
	        chAddr[2] = 0;
	        chAddr[3] = 0;
	        chAddr[4] = 0;
	        chAddr[5] = bid[0];
        }
        return "clientid-" + Util.toHexString(chAddr);
    }
    
    /**
     * Builds the solict message.
     * 
     * @return the  dhcp message
     */
    private DhcpMessage buildSolicitMessage(String duid)
    {
        DhcpMessage msg = new DhcpMessage(null, new InetSocketAddress(serverAddr, serverPort));

        msg.setTransactionId(random.nextInt());
        DhcpClientIdOption dhcpClientId = new DhcpClientIdOption();
        dhcpClientId.getOpaqueData().setAscii(duid);
        
        msg.putDhcpOption(dhcpClientId);
        
        DhcpElapsedTimeOption dhcpElapsedTime = new DhcpElapsedTimeOption();
        dhcpElapsedTime.setUnsignedShort(1);
        msg.putDhcpOption(dhcpElapsedTime);
        
    	msg.setMessageType(DhcpConstants.SOLICIT);
        DhcpIaNaOption dhcpIaNa = new DhcpIaNaOption();
        dhcpIaNa.setIaId(1);
        msg.putDhcpOption(dhcpIaNa);

        return msg;
    }
    
    private DhcpMessage buildRequestMessage(DhcpMessage advertisement) {
    	
        DhcpMessage msg = new DhcpMessage(null, new InetSocketAddress(serverAddr, serverPort));

        msg.setTransactionId(advertisement.getTransactionId());
        msg.putDhcpOption(advertisement.getDhcpClientIdOption());
        msg.putDhcpOption(advertisement.getDhcpServerIdOption());
        msg.setMessageType(DhcpConstants.REQUEST);
        msg.putDhcpOption(advertisement.getIaNaOptions().get(0));
        
        return msg;
    }
    
    private DhcpMessage buildReleaseMessage(DhcpMessage reply) {
    	
        DhcpMessage msg = new DhcpMessage(null, new InetSocketAddress(serverAddr, serverPort));

        msg.setTransactionId(reply.getTransactionId());
        msg.putDhcpOption(reply.getDhcpClientIdOption());
        msg.putDhcpOption(reply.getDhcpServerIdOption());
        msg.setMessageType(DhcpConstants.RELEASE);
        msg.putDhcpOption(reply.getIaNaOptions().get(0));
        
        return msg;
    }

	/*
	 * (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
	 */
	@Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
    {
    	Object message = e.getMessage();
        if (message instanceof DhcpMessage) {
            
            DhcpMessage dhcpMessage = (DhcpMessage) message;
            if (log.isDebugEnabled())
            	log.debug("Received: " + dhcpMessage.toStringWithOptions());
            else
            	log.info("Received: " + dhcpMessage.toString());
            
            if (dhcpMessage.getMessageType() == DhcpConstants.ADVERTISE) {
	            ClientMachine client = 
	            		clientMap.get(dhcpMessage.getDhcpClientIdOption().getOpaqueData().getAscii());
	            if (client != null) {
	            	advertisementsReceived.getAndIncrement();
	            	client.request(dhcpMessage);
	            }
	            else {
	            	log.error("Received advertise for client not found in map: duid=" + 
	            			dhcpMessage.getDhcpClientIdOption().getOpaqueData().getAscii());
	            }
            }
            else if (dhcpMessage.getMessageType() == DhcpConstants.REPLY) {
            	String key = dhcpMessage.getDhcpClientIdOption().getOpaqueData().getAscii();
	            ClientMachine client = clientMap.get(key);
	            if (client != null) {
	            	if (!client.released) {
		            	requestRepliesReceived.getAndIncrement();
		            	client.release(dhcpMessage);
	            	}
	            	else {
	            		releaseRepliesReceived.getAndIncrement();
	            		clientMap.remove(key);	            		
						if (releaseRepliesReceived.get() == numRequests) {
							endTime = System.currentTimeMillis();
							log.info("Ending at: " + endTime);
							synchronized (syncDone) {
								syncDone.notifyAll();
							}
						}
						else if (poolSize > 0) {
							synchronized (clientMap) {
								clientMap.notify();
							}
						}
	            	}
	            }
	            else {
	            	log.error("Received reply for client not found in map: duid=" + 
	            			dhcpMessage.getDhcpClientIdOption().getOpaqueData().getAscii());
	            }
            }
            else {
            	log.warn("Received unhandled message type: " + dhcpMessage.getMessageType());
            }
        }
        else {
            // Note: in theory, we can't get here, because the
            // codec would have thrown an exception beforehand
            log.error("Received unknown message object: " + message.getClass());
        }
    }
	 
	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ExceptionEvent)
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
	{
    	log.error("Exception caught: ", e.getCause());
    	e.getChannel().close();
	}
    
    /**
     * The main method.
     * 
     * @param args the arguments
     */
    public static void main(String[] args) {
        try {
			new ClientSimulator(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

}
