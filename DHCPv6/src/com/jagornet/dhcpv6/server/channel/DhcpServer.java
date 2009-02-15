package com.jagornet.dhcpv6.server.channel;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpOption;
import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;

/**
 * Title:        DHCPServer
 * Description:  The main class for a DHCPv6 server.  This is my original, hand-rolled, multi-threaded
 * 				 implementation using NIO Channels and Java 5 Concurrency package.  This class is now
 * 				 deprecated in favor of the MINA-based DHCP server, but it is kept here for posterity.
 * Copyright:    Copyright (c) 2003
 * Company:      AGR Consulting
 * @author A. Gregory Rabil
 * @version 1.0
 * @deprecated	 See description above.
 */

public class DhcpServer implements Runnable
{
	private static Logger log = LoggerFactory.getLogger(DhcpServer.class);

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

    public DhcpServer(String configFilename, int port) throws Exception
    {
        DhcpServerConfiguration.init(configFilename);
        
        selector = Selector.open();
        dhcpChannel = new DhcpChannel(port);
        workQueue = new LinkedBlockingQueue<DhcpMessage>(QUEUE_SIZE);
        
        workProcessor = new WorkProcessor(dhcpChannel, workQueue);
        processorThread = new Thread(workProcessor, "work.processor");
        processorThread.start();
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
            log.error("DHCPv6 Server Abort", ex);
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
}