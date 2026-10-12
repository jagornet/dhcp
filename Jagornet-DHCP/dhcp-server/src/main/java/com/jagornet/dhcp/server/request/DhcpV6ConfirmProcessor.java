/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.request;

import java.net.InetAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaNaOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * Title: DhcpV6ConfirmProcessor
 * Description: The main class for processing V6 CONFIRM messages.
 * 
 * @author A. Gregory Rabil
 */

public class DhcpV6ConfirmProcessor extends BaseDhcpV6Processor
{
	private static Logger log = LoggerFactory.getLogger(DhcpV6ConfirmProcessor.class);
    
    /**
     * Construct an DhcpConfirmProcessor processor.
     * 
     * @param requestMsg the Confirm message
     * @param clientLinkAddress the client link address
     */
    public DhcpV6ConfirmProcessor(DhcpV6Message requestMsg, InetAddress clientLinkAddress)
    {
        super(requestMsg, clientLinkAddress);
    }

    /*
     * FROM RFC 3315:
     * 
     * 15.5. Confirm Message
     * 
     *    Servers MUST discard any received Confirm messages that do not
     *    include a Client Identifier option or that do include a Server
     *    Identifier option.
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
    	
    	// this check enforced by TAHI DHCP server tests
    	if (requestMsg.isUnicast()) {
    		log.warn("Ignoring unicast Confirm Message");
    		return false;
    	}
    	
    	if (requestMsg.getDhcpClientIdOption() == null) {
    		log.warn("Ignoring Solicit message: " +
    				"ClientId option is null");
    		return false;
    	}
    	
    	if (requestMsg.getDhcpServerIdOption() != null) {
    		log.warn("Ignoring Solicit message: " +
					 "ServerId option is not null");
    		return false;
    	}
        
    	return true;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.server.request.BaseDhcpProcessor#process()
     */
    @Override    
    public boolean process()
    {
//      When the server receives a Confirm message, the server determines
//      whether the addresses in the Confirm message are appropriate for the
//      link to which the client is attached.  If all of the addresses in the
//      Confirm message pass this test, the server returns a status of
//      Success.  If any of the addresses do not pass this test, the server
//      returns a status of NotOnLink.  If the server is unable to perform
//      this test (for example, the server does not have information about
//      prefixes on the link to which the client is connected), or there were
//      no addresses in any of the IAs sent by the client, the server MUST
//      NOT send a reply to the client.

		boolean sendReply = false;
		boolean allOnLink = true;
		List<DhcpV6IaNaOption> iaNaOptions = requestMsg.getIaNaOptions();
    	if (iaNaOptions != null) {
    		for (DhcpV6IaNaOption dhcpIaNaOption : iaNaOptions) {
    			log.info("Processing IA_NA Confirm: " + dhcpIaNaOption.toString());
    			if ((dhcpIaNaOption.getIaAddrOptions() != null) &&
    					!dhcpIaNaOption.getIaAddrOptions().isEmpty()) {
		    		if (!allIaAddrsOnLink(dhcpIaNaOption, clientLink)) {
						// TAHI tests want the status at the message level, but it
						// should be safe to put it at both message and IA levels
 		    			addIaNaOptionStatusToReply(dhcpIaNaOption,
		    					DhcpConstants.V6STATUS_CODE_NOTONLINK);
 		    			allOnLink = false;
		    			sendReply = true;
		    		}
		    		else {
		    			// TAHI tests want the status at the message level, but it
		    			// should be safe to put it at both message and IA levels
		    			addIaNaOptionStatusToReply(dhcpIaNaOption,
		    					DhcpConstants.V6STATUS_CODE_SUCCESS);
		    			sendReply = true;
		    		}
    			}
			}
    	}
    	
    	if (sendReply) {
			// TAHI tests want the status at the message level
    		if (allOnLink) {
    			setReplyStatus(DhcpConstants.V6STATUS_CODE_SUCCESS);
    		}
    		else {
    			setReplyStatus(DhcpConstants.V6STATUS_CODE_NOTONLINK);
    		}
            replyMsg.setMessageType(DhcpConstants.V6MESSAGE_TYPE_REPLY);
    	}
		return sendReply;    	
    }
}
