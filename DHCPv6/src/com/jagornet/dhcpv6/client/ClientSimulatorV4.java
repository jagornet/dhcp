/*
 * Copyright 2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file ClientSimulatorV4.java is part of Jagornet DHCP.
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
package com.jagornet.dhcpv6.client;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpV4Message;
import com.jagornet.dhcpv6.option.v4.DhcpV4MsgTypeOption;
import com.jagornet.dhcpv6.option.v4.DhcpV4RequestedIpAddressOption;
import com.jagornet.dhcpv6.server.netty.DhcpV4ChannelDecoder;
import com.jagornet.dhcpv6.server.netty.DhcpV4ChannelEncoder;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.util.Util;

/**
 * A test client that sends discover/request/release messages 
 * to a DHCPv4 server via unicast, as if sent via a relay.
 * 
 * @author A. Gregory Rabil
 */
@ChannelHandler.Sharable
public class ClientSimulatorV4 extends SimpleChannelUpstreamHandler
{
	private static Logger log = LoggerFactory.getLogger(ClientSimulatorV4.class);

	protected Random random = new Random();
    protected Options options = new Options();
    protected CommandLineParser parser = new BasicParser();
    protected HelpFormatter formatter;
    
	protected InetAddress DEFAULT_ADDR;
    protected InetAddress serverAddr;
    protected int serverPort = DhcpConstants.V4_SERVER_PORT;
    protected InetAddress clientAddr;
    protected int clientPort = DhcpConstants.V4_SERVER_PORT;	// the test client acts as a relay
    protected boolean rapidCommit = false;
    protected int numRequests = 100;
    protected AtomicInteger discoversSent = new AtomicInteger();
    protected AtomicInteger offersReceived = new AtomicInteger();
    protected AtomicInteger requestsSent = new AtomicInteger();
    protected AtomicInteger acksReceived = new AtomicInteger();
    protected AtomicInteger releasesSent = new AtomicInteger();
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
	
	protected Map<BigInteger, ClientMachine> clientMap =
			Collections.synchronizedMap(new HashMap<BigInteger, ClientMachine>());

    /**
     * Instantiates a new test client.
     *
     * @param args the args
     * @throws Exception the exception
     */
    public ClientSimulatorV4(String[] args) throws Exception 
    {
    	DEFAULT_ADDR = InetAddress.getLocalHost();
    	
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
		
        Option caOption = new Option("ca", "clientaddress", true,
        								"Address of DHCPv4 Client (relay)" +
        								" [" + DEFAULT_ADDR + "]");		
        options.addOption(caOption);
		
        Option saOption = new Option("sa", "serveraddress", true,
        								"Address of DHCPv4 Server" +
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
            clientAddr = DEFAULT_ADDR;
            if (cmd.hasOption("ca")) {
            	clientAddr = 
            			parseIpAddressOption("client", cmd.getOptionValue("ca"), DEFAULT_ADDR);
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
     * Start sending DHCPv4 DISCOVERs.
     */
    public void start()
    {
    	DatagramChannelFactory factory = 
    		new NioDatagramChannelFactory(Executors.newCachedThreadPool());
    	
    	server = new InetSocketAddress(serverAddr, serverPort);
    	client = new InetSocketAddress(clientPort);
    	
		ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("logger", new LoggingHandler());
        pipeline.addLast("encoder", new DhcpV4ChannelEncoder());
        pipeline.addLast("decoder", new DhcpV4ChannelDecoder(client, false));
        pipeline.addLast("executor", new ExecutionHandler(
        		new OrderedMemoryAwareThreadPoolExecutor(16, 1048576, 1048576)));
        pipeline.addLast("handler", this);
    	
        channel = factory.newChannel(pipeline);
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

		log.info("Complete: discoversSent=" + discoversSent +
				" offersReceived=" + offersReceived +
				" requestsSent=" + requestsSent +
				" acksReceived=" + acksReceived +
				" releasesSent=" + releasesSent +
				" elapsedTime=" + (endTime - startTime) + "ms");

    	log.info("Shutting down executor...");
    	executor.shutdownNow();
    	log.info("Closing channel...");
    	channel.close();
    	log.info("Done.");
    	if ((discoversSent.get() == offersReceived.get()) &&
    			(requestsSent.get() == acksReceived.get()) &&
    			(releasesSent.get() == numRequests)) {
    		
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
    	DhcpV4Message msg;
    	int id;
    	byte[] mac;
    	BigInteger key;
    	
    	/**
	     * Instantiates a new client machine.
	     *
	     * @param msg the msg
	     * @param server the server
	     */
	    public ClientMachine(int id) {
    		this.id = id;
    		this.mac = buildChAddr(id);
    		this.key = new BigInteger(mac);
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
					clientMap.put(key, this);
				}
			}
			else {
				clientMap.put(key, this);
			}
			discover();
		}
		
		public void discover() {
			msg = buildDiscoverMessage(mac); 
			ChannelFuture future = channel.write(msg, server);
			future.addListener(this);
		}
		
		public void request(DhcpV4Message offerMsg) {
        	msg = buildRequestMessage(offerMsg);
			ChannelFuture future = channel.write(msg, server);
			future.addListener(this);
		}
		
		public void release(DhcpV4Message ackMsg) {
        	msg = buildReleaseMessage(ackMsg);
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
				if (msg.getMessageType() == DhcpConstants.V4MESSAGE_TYPE_DISCOVER) {
					discoversSent.getAndIncrement();
					log.info("Succesfully sent discover message mac=" + Util.toHexString(mac) +
							" cnt=" + discoversSent);
				}
				else if (msg.getMessageType() == DhcpConstants.V4MESSAGE_TYPE_REQUEST) {
					requestsSent.getAndIncrement();
					log.info("Succesfully sent request message mac=" + Util.toHexString(mac) +
							" cnt=" + requestsSent);
				}
				else if (msg.getMessageType() == DhcpConstants.V4MESSAGE_TYPE_RELEASE) {
					clientMap.remove(key);
					releasesSent.getAndIncrement();
					log.info("Succesfully sent release message mac=" + Util.toHexString(mac) +
							" cnt=" + releasesSent);
					if (releasesSent.get() == numRequests) {
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
				log.warn("Failed to send message id=" + msg.getTransactionId());
			}
		}
    }

    private byte[] buildChAddr(long id) {
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
        return chAddr;
    }
    
    /**
     * Builds the discover message.
     * 
     * @return the  dhcp message
     */
    private DhcpV4Message buildDiscoverMessage(byte[] chAddr)
    {
        DhcpV4Message msg = new DhcpV4Message(null, new InetSocketAddress(serverAddr, serverPort));

        msg.setOp((short)DhcpConstants.OP_REQUEST);
        msg.setTransactionId(random.nextLong());
        msg.setHtype((short)1);	// ethernet
        msg.setHlen((byte)6);
        msg.setChAddr(chAddr);
        msg.setGiAddr(clientAddr);	// look like a relay to the DHCP server
        
        DhcpV4MsgTypeOption msgTypeOption = new DhcpV4MsgTypeOption();
        msgTypeOption.setUnsignedByte((short)DhcpConstants.V4MESSAGE_TYPE_DISCOVER);
        
        msg.putDhcpOption(msgTypeOption);
        
        return msg;
    }
    
    private DhcpV4Message buildRequestMessage(DhcpV4Message offer) {
    	
        DhcpV4Message msg = new DhcpV4Message(null, new InetSocketAddress(serverAddr, serverPort));

        msg.setOp((short)DhcpConstants.OP_REQUEST);
        msg.setTransactionId(offer.getTransactionId());
        msg.setHtype((short)1);	// ethernet
        msg.setHlen((byte)6);
        msg.setChAddr(offer.getChAddr());
        msg.setGiAddr(clientAddr);	// look like a relay to the DHCP server
        
        DhcpV4MsgTypeOption msgTypeOption = new DhcpV4MsgTypeOption();
        msgTypeOption.setUnsignedByte((short)DhcpConstants.V4MESSAGE_TYPE_REQUEST);
        
        msg.putDhcpOption(msgTypeOption);
        
        DhcpV4RequestedIpAddressOption reqIpOption = new DhcpV4RequestedIpAddressOption();
        reqIpOption.setIpAddress(offer.getYiAddr().getHostAddress());
        msg.putDhcpOption(reqIpOption);
        
        return msg;
    }
    
    private DhcpV4Message buildReleaseMessage(DhcpV4Message ack) {
    	
        DhcpV4Message msg = new DhcpV4Message(null, new InetSocketAddress(serverAddr, serverPort));

        msg.setOp((short)DhcpConstants.OP_REQUEST);
        msg.setTransactionId(ack.getTransactionId());
        msg.setHtype((short)1);	// ethernet
        msg.setHlen((byte)6);
        msg.setChAddr(ack.getChAddr());
        msg.setGiAddr(clientAddr);	// look like a relay to the DHCP server
        msg.setCiAddr(ack.getYiAddr());
        
        DhcpV4MsgTypeOption msgTypeOption = new DhcpV4MsgTypeOption();
        msgTypeOption.setUnsignedByte((short)DhcpConstants.V4MESSAGE_TYPE_RELEASE);
        
        msg.putDhcpOption(msgTypeOption);
        
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
        if (message instanceof DhcpV4Message) {
            
            DhcpV4Message dhcpMessage = (DhcpV4Message) message;
            if (log.isDebugEnabled())
            	log.debug("Received: " + dhcpMessage.toStringWithOptions());
            else
            	log.info("Received: " + dhcpMessage.toString());
            
            if (dhcpMessage.getMessageType() == DhcpConstants.V4MESSAGE_TYPE_OFFER) {
	            ClientMachine client = clientMap.get(new BigInteger(dhcpMessage.getChAddr()));
	            if (client != null) {
	            	offersReceived.getAndIncrement();
	            	client.request(dhcpMessage);
	            }
	            else {
	            	log.error("Received offer for client not found in map: mac=" + 
	            			Util.toHexString(dhcpMessage.getChAddr()));
	            }
            }
            else if (dhcpMessage.getMessageType() == DhcpConstants.V4MESSAGE_TYPE_ACK) {
	            ClientMachine client = clientMap.get(new BigInteger(dhcpMessage.getChAddr()));
	            if (client != null) {
	            	acksReceived.getAndIncrement();
	            	client.release(dhcpMessage);
	            }
	            else {
	            	log.error("Received ack for client not found in map: mac=" + 
	            			Util.toHexString(dhcpMessage.getChAddr()));
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
			new ClientSimulatorV4(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

}
