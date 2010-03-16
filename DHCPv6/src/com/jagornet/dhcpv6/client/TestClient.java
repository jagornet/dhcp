/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestClient.java is part of DHCPv6.
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
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.DatagramChannel;
import org.jboss.netty.channel.socket.DatagramChannelFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.jboss.netty.channel.socket.oio.OioDatagramChannelFactory;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpClientIdOption;
import com.jagornet.dhcpv6.option.DhcpElapsedTimeOption;
import com.jagornet.dhcpv6.option.DhcpUserClassOption;
import com.jagornet.dhcpv6.server.netty.DhcpChannelDecoder;
import com.jagornet.dhcpv6.server.netty.DhcpChannelEncoder;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.xml.ClientIdOption;
import com.jagornet.dhcpv6.xml.ElapsedTimeOption;
import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.UserClassOption;

/**
 * A test client that sends INFO_REQUEST messages to a DHCPv6 server
 * via either unicast or multicast.
 * 
 * @author A. Gregory Rabil
 */
@ChannelPipelineCoverage("all")
public class TestClient extends SimpleChannelUpstreamHandler
{
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(TestClient.class);

    /** The options. */
    protected Options options = new Options();
    
    /** The parser. */
    protected CommandLineParser parser = new BasicParser();
    
    /** The formatter. */
    protected HelpFormatter formatter;
    
    /** The mcast net if. */
    protected NetworkInterface mcastNetIf = null;
    
    /** The server addr. */
    protected InetAddress serverAddr = DhcpConstants.LOCALHOST_V6;
    
    /** The server port. */
    protected int serverPort = DhcpConstants.SERVER_PORT;
    
    /** The client port. */
    protected int clientPort = DhcpConstants.CLIENT_PORT;
    
    protected int numRequests = 100;

    /**
     * Instantiates a new test client.
     * 
     * @param args the args
     */
    public TestClient(String[] args) 
    {
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
		
        Option addrOption = new Option("a", "address", true,
        								"Address of DHCPv6 Server" +
        								" [" + serverAddr.getHostAddress() + "]");		
        options.addOption(addrOption);

        Option mcastOption = new Option("m", "multicast", true,
									   "Multicast interface [none]");
        options.addOption(mcastOption);

        Option cpOption = new Option("cp", "clientport", true,
        							  "Client Port Number" +
        							  " [" + clientPort + "]");
        options.addOption(cpOption);

        Option spOption = new Option("sp", "serverport", true,
        							  "Server Port Number" +
        							  " [" + serverPort + "]");
        options.addOption(spOption);

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
            							"' using default: " + numRequests);
            	}
            }
            if (cmd.hasOption("a")) {
            	String a = cmd.getOptionValue("a");
            	try {
            		serverAddr = InetAddress.getByName(a);
            	}
            	catch (UnknownHostException ex) {
            		serverAddr = DhcpConstants.LOCALHOST_V6;
            		System.err.println("Invalid address: '" + a +
            							"' using default: " + serverAddr);
            	}
            }
            if (cmd.hasOption("m")) {
            	String m = cmd.getOptionValue("m");
            	try {
            		mcastNetIf = NetworkInterface.getByName(m);
            	}
            	catch (SocketException ex) {
            		System.err.println("Invalid interface: " + m +
            							" - " + ex);
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
            							"' using default: " + clientPort);
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
            							"' using default: " + serverPort);
            	}
            }
        }
        catch (ParseException pe) {
            System.err.println("Command line option parsing failure: " + pe);
            return false;
		}
        return true;
    }
    
    /**
     * Start sending DHCPv6 INFORM_REQUESTs.
     */
    public void start()
    {
    	DatagramChannelFactory factory = null;
    	if (mcastNetIf != null) {
    		factory = new OioDatagramChannelFactory(Executors.newCachedThreadPool());
    		serverAddr = DhcpConstants.ALL_DHCP_RELAY_AGENTS_AND_SERVERS;
    	}
    	else {
    		factory = new NioDatagramChannelFactory(Executors.newCachedThreadPool());
    	}
    	
    	InetSocketAddress server = new InetSocketAddress(serverAddr, serverPort);
    	InetSocketAddress client = new InetSocketAddress(clientPort);
    	
        List<DhcpMessage> msgs = buildRequestMessages();
        for (DhcpMessage msg : msgs) {
        	
    		ChannelPipeline pipeline = Channels.pipeline();
            pipeline.addLast("logger", new LoggingHandler());
            pipeline.addLast("encoder", new DhcpChannelEncoder());
            pipeline.addLast("decoder", new DhcpChannelDecoder(client));
            pipeline.addLast("handler", this);
        	
            DatagramChannel channel = null;
        	if (mcastNetIf != null) {
                channel = factory.newChannel(pipeline);
        		channel.getConfig().setNetworkInterface(mcastNetIf);
        	}
        	else {
                channel = factory.newChannel(pipeline);
        	}
        	channel.bind(client);
            // write the message, the codec will convert it
            // to wire format... well, hopefully it will!
            channel.write(msg, server);
        	if (!channel.getCloseFuture().awaitUninterruptibly(2000)) {
        		log.warn("DHCPv6 info-request timed out.");
        		channel.close().awaitUninterruptibly();
        	}
		}
    	factory.releaseExternalResources();
    }

    /**
     * Builds the request messages.
     * 
     * @return the list< dhcp message>
     */
    private List<DhcpMessage> buildRequestMessages()
    {
    	List<DhcpMessage> msgs = new ArrayList<DhcpMessage>();   	
        for (int id=0; id<numRequests; id++) {
            DhcpMessage msg = 
            	new DhcpMessage(null, new InetSocketAddress(serverAddr, serverPort));
            	
            msg.setMessageType(DhcpConstants.INFO_REQUEST);
            msg.setTransactionId(id);
            byte[] clientIdBytes = { (byte)0xde,
                                     (byte)0xbd,
                                     (byte)0xeb,
                                     (byte)0xde,
                                     (byte)0xb0,
                                     (byte)id };
            OpaqueData opaque = OpaqueData.Factory.newInstance();
            opaque.setHexValue(clientIdBytes);

            ClientIdOption clientIdOption = ClientIdOption.Factory.newInstance();
            clientIdOption.setOpaqueData(opaque);
            
            msg.putDhcpOption(new DhcpClientIdOption(clientIdOption));
            
            ElapsedTimeOption elapsedTimeOption = ElapsedTimeOption.Factory.newInstance();
            elapsedTimeOption.setUnsignedShort(id+1000);
            msg.putDhcpOption(new DhcpElapsedTimeOption(elapsedTimeOption));

            UserClassOption userClassOption = UserClassOption.Factory.newInstance();
            // wrap it with the DhcpOption subclass to get at
            // utility method for adding userclass string
            DhcpUserClassOption dhcpUserClassOption = 
                new DhcpUserClassOption(userClassOption);
            dhcpUserClassOption.addOpaqueData("FilterUserClass");
            msg.putDhcpOption(dhcpUserClassOption);

            msgs.add(msg);
        }
        return msgs;
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
            
//            SocketAddress remoteAddress = e.getRemoteAddress();
//            InetAddress localAddr = ((InetSocketAddress)e.getChannel().getLocalAddress()).getAddress();
            e.getChannel().close();
        }
        else {
            // Note: in theory, we can't get here, because the
            // codec would have thrown an exception beforehand
            log.error("Received unknown message object: " + message.getClass());
        }
    }
	 
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
        new TestClient(args);
    }

}
