package com.agr.dhcpv6.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.Collection;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.message.DhcpMessage;
import com.agr.dhcpv6.option.DhcpOption;
import com.agr.dhcpv6.server.config.DhcpServerConfiguration;
import com.agr.dhcpv6.server.config.xml.DhcpV6ServerConfig;
import com.agr.dhcpv6.util.DhcpConstants;

/**
 * Title:        DHCPServer
 * Copyright:    Copyright (c) 2003
 * Company:      AGR Consulting
 * @author A. Gregory Rabil
 * @version 1.0
 */

/**
 * This is a simple, single-threaded DHCPv6 server that uses
 * MulticastSocket because MulticastChannels are not yet
 * available in Java :-(
 */
public class DhcpSocketServer implements Runnable
{
    private static Log log = LogFactory.getLog(DhcpSocketServer.class);

    protected Options options;
    protected CommandLineParser parser;
    protected HelpFormatter formatter;

    protected static String DEFAULT_CONFIG_FILENAME = "dhcpServerConfig.xml";

    protected String configFilename = DEFAULT_CONFIG_FILENAME;

    protected MulticastSocket dhcpSocket;

    public DhcpSocketServer(String args[]) throws Exception
    {
        options = new Options();
        parser = new BasicParser();
        setupOptions();

        if(!parseOptions(args)) {
            formatter = new HelpFormatter();
            String cliName = this.getClass().getName();
            formatter.printHelp(cliName, options);
            System.exit(0);
        }

        DhcpServerConfiguration.init(configFilename);

        dhcpSocket = new MulticastSocket(DhcpConstants.SERVER_PORT);
        dhcpSocket.joinGroup(DhcpConstants.ALL_DHCP_RELAY_AGENTS_AND_SERVERS);
        dhcpSocket.joinGroup(DhcpConstants.ALL_DHCP_SERVERS);
    }
    
    public static DhcpV6ServerConfig loadConfig(String filename)
    // TODO: change to throws IOException, JAXBException...
    // right now, just throw Exception so as to allow GUI to
    // use this method without requiring JAXB libs
            throws Exception
    {
        return DhcpServerConfiguration.loadConfig(filename);
    }
    
    public static void saveConfig(DhcpV6ServerConfig config, String filename)
        throws Exception    // see comment for loadConfig
    {
        DhcpServerConfiguration.saveConfig(config, filename);
    }

    public static DhcpV6ServerConfig getDhcpServerConfig()
    {
        return DhcpServerConfiguration.getConfig();
    }

    /**
     * Loop forever, processing incoming packets from the DHCPv6 server UDP
     * socket.
     */
    public void run()
    {
        try {
            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, 1024);

            while (true) {
                log.info("Waiting for datagram packet...");
                dhcpSocket.receive(packet);
                
                if (packet != null) {
                    InetSocketAddress srcInetSocketAddress =
                        (InetSocketAddress)packet.getSocketAddress();
                    // in theory, receive should never return null
                    // but better safe than NPE!
                    if (srcInetSocketAddress != null) {
                        log.info("Received datagram from: " + srcInetSocketAddress);
                        ByteBuffer bbuf = ByteBuffer.wrap(buf);
                        DhcpMessage inMessage = 
                            DhcpMessage.decode(srcInetSocketAddress, bbuf);
                        if (log.isDebugEnabled())
                            log.debug("Decoded message: " + 
                                      (inMessage != null ? 
                                      inMessage.toStringWithOptions() : "null"));
                        DhcpInfoRequestProcessor processor =
                            new DhcpInfoRequestProcessor(srcInetSocketAddress.getAddress(),
                                                         inMessage);
                        DhcpMessage outMessage = processor.process();
                        if (outMessage != null) {
                            sendMessage(outMessage);
                        }
                        else {
                            log.warn("Processor returned null reply message");
                        }
                    }
                    else {
                        log.error("Null source address for packet");
                    }
                }
                else {
                    log.error("Null packet received");
                }
            }
        }
        catch (IOException ex) {
            log.fatal("DHCPv6 Server Abort", ex);
        }
    }

    /**
     * Sends a DhcpMessage object to a predifined host.
     * @param outMessage well-formed DhcpMessage to be sent to a host
     */
    public void sendMessage(DhcpMessage outMessage)
            throws IOException
    {
        ByteBuffer bbuf = outMessage.encode();
        DatagramPacket dp = new DatagramPacket(bbuf.array(), bbuf.limit());
        dp.setSocketAddress(outMessage.getSocketAddress());
        dhcpSocket.send(dp);
    }

    /**
     * Print out the options contained in a DHCPMessage to the
     * logger.
     */
    private void debugOptions(DhcpMessage msg)
    {
        if (log.isDebugEnabled()) {
            Collection<DhcpOption> options = msg.getOptions();
            if (options != null) {
                for (DhcpOption option : options) {
                    log.debug(option.toString());
                }
            }
        }
    }

    private void setupOptions()
    {
        Option configFileOption = new Option("c", "configfile", true,
                                             "Configuration File");
        options.addOption(configFileOption);
    }

    protected boolean parseOptions(String[] args)
    {
        try {
            CommandLine cmd = parser.parse(options, args);
            if(cmd.hasOption("c")) {
                configFilename = cmd.getOptionValue("c");
            }
            if(cmd.hasOption("?")) {
                return false;
            }
        }
        catch(ParseException pe) {
            log.warn("Parsing exception: " + pe);
            return false;
        }
        return true;
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        try {
            log.info("Starting DhcpSocketServer");
            DhcpSocketServer server = new DhcpSocketServer(args);
            server.run();
        }
        catch (Exception ex) {
            log.fatal("DhcpSocketServer ABORT!", ex);
        }
    }    
}