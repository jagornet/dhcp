/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.request;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.option.v6.DhcpV6ServerIdOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * Title: DhcpV6InfoRequestProcessor
 * Description: The main class for processing V6 INFO_REQUEST messages.
 * 
 * @author A. Gregory Rabil
 */

public class DhcpV6InfoRequestProcessor extends BaseDhcpV6Processor {
    private static Logger log = LoggerFactory.getLogger(DhcpV6InfoRequestProcessor.class);

    /**
     * Construct an DhcpInfoRequest processor.
     * 
     * @param requestMsg        the Info-Request message
     * @param clientLinkAddress the client link address
     */
    public DhcpV6InfoRequestProcessor(DhcpV6Message requestMsg, InetAddress clientLinkAddress) {
        super(requestMsg, clientLinkAddress);
    }

    /*
     * FROM RFC 3315:
     * 
     * 15.12. Information-request Message
     *
     * Servers MUST discard any received Information-request message that
     * meets any of the following conditions:
     *
     * - The message includes a Server Identifier option and the DUID in
     * the option does not match the server's DUID.
     *
     * - The message includes an IA option.
     * 
     */
    /*
     * (non-Javadoc)
     * 
     * @see com.jagornet.dhcpv6.server.request.BaseDhcpProcessor#preProcess()
     */
    @Override
    public boolean preProcess() {
        if (!super.preProcess()) {
            return false;
        }

        // this check enforced by TAHI DHCP server tests
        if (requestMsg.isUnicast()) {
            log.warn("Ignoring unicast InfoRequest Message");
            return false;
        }

        // if the client provided a ServerID option, then it MUST
        // match our configured ServerID, otherwise ignore the request
        DhcpV6ServerIdOption requestedServerIdOption = requestMsg.getDhcpServerIdOption();
        DhcpV6ServerIdOption myServerId = getDhcpV6ServerIdOption();
        if ((requestedServerIdOption != null) &&
                ((myServerId == null) || !myServerId.equals(requestedServerIdOption))) {
            log.warn("Ignoring Info-Request message: " +
                    "Requested ServerId: " + requestedServerIdOption +
                    " My ServerId: " + myServerId);
            return false;
        }

        // if the client message has an IA option (IA_NA, IA_TA)
        // then the DHCPv6 server must ignore the request
        if (((requestMsg.getIaNaOptions() != null) && !requestMsg.getIaNaOptions().isEmpty()) ||
                ((requestMsg.getIaTaOptions() != null) && !requestMsg.getIaTaOptions().isEmpty()) ||
                ((requestMsg.getIaPdOptions() != null) && !requestMsg.getIaPdOptions().isEmpty())) {
            log.warn("Ignoring Info-Request message: " +
                    " client message contains IA option(s).");
            return false;
        }

        return true;
    }

    /**
     * Process the client request. Find appropriate configuration based on any
     * criteria in the request message that can be matched against the server's
     * configuration, then formulate a response message containing the options
     * to be sent to the client.
     * 
     * @return true if a reply should be sent, false otherwise
     */
    @Override
    public boolean process() {
        // When the server receives an Information-request message, the client
        // is requesting configuration information that does not include the
        // assignment of any addresses. The server determines all configuration
        // parameters appropriate to the client, based on the server
        // configuration policies known to the server.

        replyMsg.setMessageType(DhcpConstants.V6MESSAGE_TYPE_REPLY);
        populateReplyMsgOptions(clientLink);

        return true;
    }
}
