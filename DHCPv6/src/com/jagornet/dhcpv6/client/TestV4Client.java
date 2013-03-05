/*
 * Copyright 2011 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestV4Client.java is part of DHCPv6.
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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import org.jboss.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpV4Message;
import com.jagornet.dhcpv6.option.v4.DhcpV4MsgTypeOption;
import com.jagornet.dhcpv6.server.netty.DhcpV4ChannelDecoder;
import com.jagornet.dhcpv6.server.netty.DhcpV4ChannelEncoder;
import com.jagornet.dhcpv6.util.DhcpConstants;

/**
 * A test client that sends discover messages to a DHCPv4 server
 * via unicast.
 * 
 * @author A. Gregory Rabil
 */
@ChannelHandler.Sharable
public class TestV4Client extends SimpleChannelUpstreamHandler
{
	private static Logger log = LoggerFactory.getLogger(TestV4Client.class);

    protected Options options = new Options();
    protected CommandLineParser parser = new BasicParser();
    protected HelpFormatter formatter;
    
	protected InetAddress DEFAULT_ADDR;
    protected InetAddress serverAddr;
    protected int serverPort = DhcpConstants.V4_SERVER_PORT;
    protected InetAddress clientAddr;
    protected int clientPort = DhcpConstants.V4_CLIENT_PORT;
    protected boolean rapidCommit = false;
    protected int numRequests = 100;
    protected int requestsSent = 0;
    protected int successCnt = 0;
    protected long startTime = 0;    
    protected long endTime = 0;
    
    protected DatagramChannel channel = null;	
	protected ExecutorService executor = Executors.newCachedThreadPool();
    
    protected Map<Long, DhcpV4Message> requestMap =
    	Collections.synchronizedMap(new HashMap<Long, DhcpV4Message>());

    /**
     * Instantiates a new test client.
     *
     * @param args the args
     * @throws Exception the exception
     */
    public TestV4Client(String[] args) throws Exception 
    {
    	DEFAULT_ADDR = InetAddress.getLocalHost();
    	
        setupOptions();

        if(!parseOptions(args)) {
            formatter = new HelpFormatter();
            String cliName = this.getClass().getName();
            formatter.printHelp(cliName, options);
//            PrintWriter stderr = new PrintWriter(System.err, true);	// auto-flush=true
//            formatter.printHelp(stderr, 80, cliName, null, options, 2, 2, null);
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

        Option helpOption = new Option("?", "help", false, "Show this help page.");
        
        options.addOption(helpOption);
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
            	String n = cmd.getOptionValue("n");
            	try {
            		numRequests = Integer.parseInt(n);
            	}
            	catch (NumberFormatException ex) {
            		numRequests = 100;
            		System.err.println("Invalid number of requests: '" + n +
            							"' using default: " + numRequests +
            							" Exception=" + ex);
            	}
            }
            clientAddr = DEFAULT_ADDR;
            if (cmd.hasOption("ca")) {
            	String a = cmd.getOptionValue("ca");
            	try {
            		clientAddr = InetAddress.getByName(a);
            	}
            	catch (UnknownHostException ex) {
            		clientAddr = DEFAULT_ADDR;
            		System.err.println("Invalid address: '" + a +
            							"' using default: " + serverAddr +
            							" Exception=" + ex);
            	}
            }
            serverAddr = DEFAULT_ADDR;
            if (cmd.hasOption("sa")) {
            	String a = cmd.getOptionValue("sa");
            	try {
            		serverAddr = InetAddress.getByName(a);
            	}
            	catch (UnknownHostException ex) {
            		serverAddr = DEFAULT_ADDR;
            		System.err.println("Invalid address: '" + a +
            							"' using default: " + serverAddr +
            							" Exception=" + ex);
            	}
            }
            if (cmd.hasOption("cp")) {
            	String p = cmd.getOptionValue("cp");
            	try {
            		clientPort = Integer.parseInt(p);
            	}
            	catch (NumberFormatException ex) {
            		clientPort = DhcpConstants.CLIENT_PORT;
            		System.err.println("Invalid client port number: '" + p +
            							"' using default: " + clientPort +
            							" Exception=" + ex);
            	}
            }
            if (cmd.hasOption("sp")) {
            	String p = cmd.getOptionValue("sp");
            	try {
            		serverPort = Integer.parseInt(p);
            	}
            	catch (NumberFormatException ex) {
            		serverPort = DhcpConstants.SERVER_PORT;
            		System.err.println("Invalid server port number: '" + p +
            							"' using default: " + serverPort +
            							" Exception=" + ex);
            	}
            }
            if (cmd.hasOption("r")) {
            	rapidCommit = true;
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
    	
    	InetSocketAddress server = new InetSocketAddress(serverAddr, serverPort);
    	InetSocketAddress client = new InetSocketAddress(clientPort);
    	
		ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("logger", new LoggingHandler());
        pipeline.addLast("encoder", new DhcpV4ChannelEncoder());
        pipeline.addLast("decoder", new DhcpV4ChannelDecoder(client, false));
        pipeline.addLast("handler", this);
    	
        channel = factory.newChannel(pipeline);
    	channel.bind(client);

    	List<DhcpV4Message> requestMsgs = buildRequestMessages();
    	
    	for (DhcpV4Message msg : requestMsgs) {
    		executor.execute(new RequestSender(msg, server));
    	}

    	long ms = 0;
		if (rapidCommit)
			ms = requestMsgs.size()*200;
		else
			ms = requestMsgs.size()*20;

    	synchronized (requestMap) {
        	try {
        		log.info("Waiting total of " + ms + " milliseconds for completion");
//        		requestMap.wait(ms);
        		requestMap.wait();
        	}
        	catch (InterruptedException ex) {
        		log.error("Interrupted", ex);
        	}
		}

		log.info("Successfully processed " + successCnt +
				" of " + requestsSent + " messages in " +
				(endTime - startTime) + " milliseconds");

    	log.info("Shutting down executor...");
    	executor.shutdownNow();
    	log.info("Closing channel...");
    	channel.close();
    	log.info("Done.");
    	System.exit(0);
    }

    /**
     * The Class RequestSender.
     */
    class RequestSender implements Runnable, ChannelFutureListener
    {
    	
	    /** The msg. */
	    DhcpV4Message msg;
    	
	    /** The server. */
	    InetSocketAddress server;
    	
    	/**
	     * Instantiates a new request sender.
	     *
	     * @param msg the msg
	     * @param server the server
	     */
	    public RequestSender(DhcpV4Message msg, InetSocketAddress server)
    	{
    		this.msg = msg;
    		this.server = server;
    	}
		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			ChannelFuture future = channel.write(msg, server);
			future.addListener(this);
		}
		
		/* (non-Javadoc)
		 * @see org.jboss.netty.channel.ChannelFutureListener#operationComplete(org.jboss.netty.channel.ChannelFuture)
		 */
		@Override
		public void operationComplete(ChannelFuture future) throws Exception
		{
			long id = msg.getTransactionId();
			if (future.isSuccess()) {
				if (startTime == 0) {
					startTime = System.currentTimeMillis();
				}
				requestsSent++;
				log.info("Succesfully sent message id=" + id);
				requestMap.put(id, msg);
			}
			else {
				log.warn("Failed to send message id=" + msg.getTransactionId());
			}
		}
    }
    
    /**
     * Builds the request messages.
     * 
     * @return the list< dhcp message>
     */
    private List<DhcpV4Message> buildRequestMessages()
    {
    	List<DhcpV4Message> requests = new ArrayList<DhcpV4Message>();   	
        for (int id=0; id<numRequests; id++) {
            DhcpV4Message msg = 
            	new DhcpV4Message(null, new InetSocketAddress(serverAddr, serverPort));

            msg.setOp((short)DhcpConstants.OP_REQUEST);
            msg.setTransactionId(id);
            byte[] chAddr = new byte[] { (byte)0xde, (byte) 0xbb, (byte)0x1e, 
            								(byte)0x00, (byte)0x00, (byte)id };
            msg.setChAddr(chAddr);
            
            DhcpV4MsgTypeOption msgTypeOption = new DhcpV4MsgTypeOption();
            msgTypeOption.setUnsignedByte(
            		(short)DhcpConstants.V4MESSAGE_TYPE_DISCOVER);
            
            msg.putDhcpOption(msgTypeOption);
            requests.add(msg);
        }
        return requests;
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
            
            DhcpV4Message requestMsg = requestMap.remove(dhcpMessage.getTransactionId());
            if (requestMsg != null) {
            	successCnt++;
            	endTime = System.currentTimeMillis();
            	synchronized (requestMap) {
            		if (requestMap.isEmpty()) {
            			requestMap.notify();
            		}
            	}
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
			new TestV4Client(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

}
