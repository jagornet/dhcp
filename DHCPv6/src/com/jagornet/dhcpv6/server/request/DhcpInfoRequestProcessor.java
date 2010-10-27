/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpInfoRequestProcessor.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.server.request;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpServerIdOption;
import com.jagornet.dhcpv6.util.DhcpConstants;

/**
 * Title: DhcpInfoRequestProcessor
 * Description: The main class for processing INFO_REQUEST messages.
 * 
 * @author A. Gregory Rabil
 */

public class DhcpInfoRequestProcessor extends BaseDhcpProcessor
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpInfoRequestProcessor.class);
    
    /**
     * Construct an DhcpInfoRequest processor.
     * 
     * @param requestMsg the Info-Request message
     * @param clientLinkAddress the client link address
     */
    public DhcpInfoRequestProcessor(DhcpMessage requestMsg, InetAddress clientLinkAddress)
    {
    	super(requestMsg, clientLinkAddress);
    }
    
    /*
     * FROM RFC 3315:
     * 
     * 15.12. Information-request Message
     *
     *  Servers MUST discard any received Information-request message that
     *  meets any of the following conditions:
     *
     *  -  The message includes a Server Identifier option and the DUID in
     *     the option does not match the server's DUID.
     *
     *  -  The message includes an IA option.
     *  
     */
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.server.request.BaseDhcpProcessor#preProcess()
     */
    @Override
    public boolean preProcess()
    {
    	if (!super.preProcess()) {
    		return false;
    	}

    	// if the client provided a ServerID option, then it MUST
        // match our configured ServerID, otherwise ignore the request
    	DhcpServerIdOption requestedServerIdOption = requestMsg.getDhcpServerIdOption();
        if ( (requestedServerIdOption != null) &&
             !dhcpServerIdOption.equals(requestedServerIdOption) ) {
            log.warn("Ignoring Info-Request message: " +
                     "Requested ServerId: " + requestedServerIdOption +
                     " My ServerId: " + dhcpServerIdOption);
            return false;
        }

        // if the client message has an IA option (IA_NA, IA_TA)
        // then the DHCPv6 server must ignore the request
        if ( ((requestMsg.getIaNaOptions() != null) && !requestMsg.getIaNaOptions().isEmpty()) ||
        	 ((requestMsg.getIaTaOptions() != null) && !requestMsg.getIaTaOptions().isEmpty()) ||
        	 ((requestMsg.getIaPdOptions() != null) && !requestMsg.getIaPdOptions().isEmpty()) ) {
            log.warn("Ignoring Info-Request message: " +
                     " client message contains IA option(s).");
            return false;
        }
        
    	return true;
    }

    /**
     * Process the client request.  Find appropriate configuration based on any
     * criteria in the request message that can be matched against the server's
     * configuration, then formulate a response message containing the options
     * to be sent to the client.
     * 
     * @return true if a reply should be sent, false otherwise
     */
    @Override
    public boolean process()
    {
//    	   When the server receives an Information-request message, the client
//    	   is requesting configuration information that does not include the
//    	   assignment of any addresses.  The server determines all configuration
//    	   parameters appropriate to the client, based on the server
//    	   configuration policies known to the server.

    	replyMsg.setMessageType(DhcpConstants.REPLY);
    	populateReplyMsgOptions(clientLink);

    	return true;
    }
}
