package com.agr.dhcpv6.server;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

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
 * Description:  The main class for a DHCPv6 server
 * Copyright:    Copyright (c) 2003
 * Company:      AGR Consulting
 * @author A. Gregory Rabil
 * @version 1.0
 */

public class DhcpServer implements Runnable
{
    private static Log log = LogFactory.getLog(DhcpServer.class);

    protected Options options;
    protected CommandLineParser parser;
    protected HelpFormatter formatter;

    protected static String DEFAULT_CONFIG_FILENAME = "dhcpServerConfig.xml";
    protected static int QUEUE_SIZE = 10000;  

    protected String configFilename = DEFAULT_CONFIG_FILENAME;

    protected Selector selector;
    protected DhcpChannel dhcpChannel;
    
    // just a thought... maybe it would be more flexible
    // if there was a queue for each message type, allowing
    // a configurable number of workers for each queue?
    private LinkedBlockingQueue<DhcpMessage> workQueue;
    private WorkProcessor workProcessor;
    private Thread processorThread;

    public DhcpServer(String args[]) throws Exception
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
        
        selector = Selector.open();
//        dhcpChannel = new DhcpChannel(DhcpConstants.SERVER_PORT);
        dhcpChannel = new DhcpChannel(10000 + DhcpConstants.SERVER_PORT);
        workQueue = new LinkedBlockingQueue<DhcpMessage>(QUEUE_SIZE);
        
        workProcessor = new WorkProcessor(dhcpChannel, workQueue);
        processorThread = new Thread(workProcessor, "work.processor");
        processorThread.start();
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
            DatagramChannel channel = dhcpChannel.getChannel();
            // must be sure to make it non-blocking for use with selector
            channel.configureBlocking(false);
            while (true) {
                // register the DHCP server's UDP socket with selector for read ops
                channel.register(selector, SelectionKey.OP_READ);

                log.info("Checking for activity on socket selector...");
                // See if we've had any activity
                int num = selector.select();
                // If we don't have any activity, loop around and wait again
                if (num == 0) {
                    continue;
                }

                // Get the keys corresponding to the activity
                // that has been detected, and process them
                // one by one
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                while (it.hasNext()) {
                    // Get a key representing one of bits of I/O activity
                    SelectionKey key = it.next();

                    if (key.isReadable()) {
                        log.debug("DHCP socket read selected");
                        channel = (DatagramChannel)key.channel();
                        // this MUST be the DhcpChannel's DatagramChannel!!
                        if (channel == dhcpChannel.getChannel()) {
                            DhcpMessage msg = receiveMessage();
                            if (msg != null) {
                                queueMessage(msg);
                            }
                            else {
                                log.error("Null message");
                            }
                        }
                        else {
                            log.error("Not our DhcpChannel");
                        }
                    }
                    // We remove the selected keys, because we've dealt with them
                    keys.clear();
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
        dhcpChannel.send(outMessage);
    }

    /**
     * Receives a datagram packet containing a DHCP Message into
     * a DhcpMessage object.
     * @return inMessage DhcpMessage object representing the received message
     */
    public DhcpMessage receiveMessage()
    {
//        DhcpMessage inMessage = new DhcpMessage();        
//        dhcpChannel.receive(inMessage);
//        return inMessage;
        return dhcpChannel.receive();
    }

    public void queueMessage(DhcpMessage msg)
    {
        if (log.isInfoEnabled()) {
            StringBuffer sb = new StringBuffer("Received DHCP ");
            sb.append(msg.toString());
            log.info(sb.toString());
            debugOptions(msg);
        }
        log.info("Queueing message...");
        if (workQueue.offer(msg)) {
            log.info("Message queued");
        }
        else {
            log.error("Failed to queue message: full queue");
        }
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
/*
    public static void main(String[] args)
    {
        try {
            DhcpServer.loadConfig(args[0]);
            DhcpServer server = new DhcpServer();
            server.run();
        }
        catch (Exception ex) {
            System.err.println(ex);
            ex.printStackTrace();
        }
    }
*/
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
            DhcpServer server = new DhcpServer(args);
            server.run();
        }
        catch (Exception ex) {
            System.err.println("Fatal exception: " + ex);
        }
    }    
}