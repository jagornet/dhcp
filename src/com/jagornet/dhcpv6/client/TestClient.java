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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpClientIdOption;
import com.jagornet.dhcpv6.option.DhcpElapsedTimeOption;
import com.jagornet.dhcpv6.option.DhcpUserClassOption;
import com.jagornet.dhcpv6.server.nio.DhcpDecoderAdapter;
import com.jagornet.dhcpv6.server.nio.DhcpEncoderAdapter;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.xml.ClientIdOption;
import com.jagornet.dhcpv6.xml.ElapsedTimeOption;
import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.UserClassOption;

/**
 * A test client that just sends messages to a DHCPv6 server
 * via either unicast or multicast.
 * 
 * @author A. Gregory Rabil
 */
public class TestClient  extends IoHandlerAdapter
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
			if (mcastNetIf == null) 
				start();
			else
				startMcast();
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
     * Start.
     */
    public void start()
    {
    	// pre-configure the message factory instance
    	
        log.debug("Creating a datagram connector");
        IoConnector connector = new NioDatagramConnector();

        log.debug("Setting the handler");
        connector.setHandler(this);

        DefaultIoFilterChainBuilder chain = connector.getFilterChain();  
        chain.addLast("logger", new LoggingFilter());

        ProtocolEncoder encoder = new DhcpEncoderAdapter();
        ProtocolDecoder decoder = new DhcpDecoderAdapter();
        chain.addLast("codec", new ProtocolCodecFilter(encoder, decoder));
        
        log.debug("About to connect to the server...");
        ConnectFuture connFuture = 
            connector.connect(new InetSocketAddress(serverAddr, serverPort));

        log.debug("About to wait.");
        connFuture.awaitUninterruptibly();

        log.debug("Adding a future listener.");
        connFuture.addListener(new IoFutureListener<ConnectFuture>() {
            public void operationComplete(ConnectFuture future) {
                if (future.isConnected()) {
                    log.debug("...connected");
                    IoSession session = future.getSession();
                    List<DhcpMessage> msgs = buildRequestMessages();
                    for (DhcpMessage msg : msgs) {
                        // write the message, the codec will convert it
                        // to wire format... well, hopefully it will!
                        session.write(msg);							
					}
                    System.exit(0);
                } else {
                    log.error("Not connected...exiting");
                }
            }
        });
    }
    
    /**
     * Start mcast.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws SocketException the socket exception
     */
    public void startMcast() throws IOException, SocketException
    {
    	MulticastSocket msock = 
    		new MulticastSocket(new InetSocketAddress(clientPort));
    	msock.setNetworkInterface(mcastNetIf);
    	
    	// override any serverAddr to be the All_Servers_and_Relays multicast address
		serverAddr = InetAddress.getByName("FF02::1:2");
    	
		List<DhcpMessage> msgs = buildRequestMessages();
		for (DhcpMessage msg : msgs) {
			byte[] buf = msg.encode().array();
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			packet.setAddress(msg.getRemoteAddress().getAddress());
			packet.setPort(msg.getRemoteAddress().getPort());
			msock.send(packet);	
		}
		System.exit(0);
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

    /* (non-Javadoc)
     * @see org.apache.mina.core.service.IoHandlerAdapter#exceptionCaught(org.apache.mina.core.session.IoSession, java.lang.Throwable)
     */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause)
            throws Exception {
        cause.printStackTrace();
    }

    /* (non-Javadoc)
     * @see org.apache.mina.core.service.IoHandlerAdapter#messageReceived(org.apache.mina.core.session.IoSession, java.lang.Object)
     */
    @Override
    public void messageReceived(IoSession session, Object message)
            throws Exception {
        log.debug("Session recv...");
    }

    /* (non-Javadoc)
     * @see org.apache.mina.core.service.IoHandlerAdapter#messageSent(org.apache.mina.core.session.IoSession, java.lang.Object)
     */
    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        log.debug("Message sent...");
    }

    /* (non-Javadoc)
     * @see org.apache.mina.core.service.IoHandlerAdapter#sessionClosed(org.apache.mina.core.session.IoSession)
     */
    @Override
    public void sessionClosed(IoSession session) throws Exception {
        log.debug("Session closed...");
    }

    /* (non-Javadoc)
     * @see org.apache.mina.core.service.IoHandlerAdapter#sessionCreated(org.apache.mina.core.session.IoSession)
     */
    @Override
    public void sessionCreated(IoSession session) throws Exception {
        log.debug("Session created...");
    }

    /* (non-Javadoc)
     * @see org.apache.mina.core.service.IoHandlerAdapter#sessionIdle(org.apache.mina.core.session.IoSession, org.apache.mina.core.session.IdleStatus)
     */
    @Override
    public void sessionIdle(IoSession session, IdleStatus status)
            throws Exception {
        log.debug("Session idle...");
    }

    /* (non-Javadoc)
     * @see org.apache.mina.core.service.IoHandlerAdapter#sessionOpened(org.apache.mina.core.session.IoSession)
     */
    @Override
    public void sessionOpened(IoSession session) throws Exception {
        log.debug("Session opened...");
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
