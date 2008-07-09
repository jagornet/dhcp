package com.agr.dhcpv6.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agr.dhcpv6.message.DhcpMessage;
import com.agr.dhcpv6.message.DhcpMessageHandler;

/**
 * Title:        DhcpInfoRequest
 * Description:  A DhcpMessageHandler for INFO_REQUEST messages.  Basically,
 *               this class implements the processing of a stateless DHCPv6
 *               server.
 * Copyright:    Copyright (c) 2003
 * Company:      AGR Consulting
 * @author A. Gregory Rabil
 * @version 1.0
 */

public class DhcpInfoRequestHandler implements DhcpMessageHandler
{
	private static Logger log = LoggerFactory.getLogger(DhcpInfoRequestHandler.class);

    private DhcpChannel dhcpChannel;
    private DhcpInfoRequestProcessor dhcpProcessor;

    /**
     * Construct an DhcpInfoRequest handler
     *
     * @param   reqMsg  must be an INFO_REQUEST type DhcpMessage
     */
    public DhcpInfoRequestHandler(DhcpChannel channel, DhcpMessage reqMsg)
    {
        dhcpChannel = channel;
        dhcpProcessor = 
            new DhcpInfoRequestProcessor(channel.getSocket().getInetAddress(), 
                                         reqMsg);
    }

    /**
     * Handle the client request.  Find appropriate configuration based on any
     * criteria in the request message that can be matched against the server's
     * configuration, then formulate a response message containing the options
     * to be sent to the client.
     */
    public void run()
    {
        DhcpMessage replyMsg = dhcpProcessor.process();
        if (replyMsg != null) {
            log.info("Sending DHCP reply message");
            dhcpChannel.send(replyMsg);
        }
        else {
            log.warn("Null DHCP reply message returned from processor");
        }
    }
}
