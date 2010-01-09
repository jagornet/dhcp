/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpSolicitProcessor.java is part of DHCPv6.
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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.db.IdentityAssoc;
import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpClientIdOption;
import com.jagornet.dhcpv6.option.DhcpIaNaOption;
import com.jagornet.dhcpv6.option.DhcpServerIdOption;
import com.jagornet.dhcpv6.server.request.binding.Binding;
import com.jagornet.dhcpv6.util.DhcpConstants;

// TODO: Auto-generated Javadoc
/**
 * Title: DhcpRequestProcessor
 * Description: The main class for processing REQUEST messages.
 * 
 * @author A. Gregory Rabil
 */

public class DhcpRequestProcessor extends BaseDhcpProcessor
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpRequestProcessor.class);
    
    /**
     * Construct an DhcpRequestProcessor processor.
     * 
     * @param requestMsg the Request message
     * @param clientLinkAddress the client link address
     */
    public DhcpRequestProcessor(DhcpMessage requestMsg, InetAddress clientLinkAddress)
    {
        super(requestMsg, clientLinkAddress);
    }

    /*
     * FROM RFC 3315:
     * 
     * 15.4. Request Message
     * 
     * Servers MUST discard any received Request message that meet any of
     * the following conditions:
     * 
     * -  the message does not include a Server Identifier option.
     * 
     * -  the contents of the Server Identifier option do not match the
     *    server's DUID.
     * 
     * -  the message does not include a Client Identifier option.
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

    	DhcpServerIdOption requestedServerIdOption = requestMsg.getDhcpServerIdOption();
        if (requestedServerIdOption == null) {
            log.warn("Ignoring Request message: " +
                    "Requested ServerId option is null");
           return false;
        }
        
        if (!dhcpServerIdOption.equals(requestedServerIdOption)) {
            log.warn("Ignoring Request message: " +
                     "Requested ServerId: " + requestedServerIdOption +
                     "My ServerId: " + dhcpServerIdOption);
            return false;
        }
    	
    	if (requestMsg.getDhcpClientIdOption() == null) {
    		log.warn("Ignoring Request message: " +
    				"ClientId option is null");
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
//      When the server receives a Request message via unicast from a client
//      to which the server has not sent a unicast option, the server
//      discards the Request message and responds with a Reply message
//      containing a Status Code option with the value UseMulticast, a Server
//      Identifier option containing the server's DUID, the Client Identifier
//      option from the client message, and no other options.

    	if (shouldMulticast()) {
    		replyMsg.setMessageType(DhcpConstants.REPLY);
    		setReplyStatus(DhcpConstants.STATUS_CODE_USEMULTICAST);
    		return true;
    	}

//		   If the server finds that the prefix on one or more IP addresses in
//		   any IA in the message from the client is not appropriate for the link
//		   to which the client is connected, the server MUST return the IA to
//		   the client with a Status Code option with the value NotOnLink.
//
//		   If the server cannot assign any addresses to an IA in the message
//		   from the client, the server MUST include the IA in the Reply message
//		   with no addresses in the IA and a Status Code option in the IA
//		   containing status code NoAddrsAvail.

		boolean sendReply = true;
		boolean haveBinding = false;
		DhcpClientIdOption clientIdOption = requestMsg.getDhcpClientIdOption();
		List<DhcpIaNaOption> iaNaOptions = requestMsg.getIaNaOptions();
    	if (iaNaOptions != null) {
    		for (DhcpIaNaOption dhcpIaNaOption : iaNaOptions) {
    			log.info("Processing IA_NA Request: " + dhcpIaNaOption.toString());
	    		if (!allIaAddrsOnLink(dhcpIaNaOption, clientLink)) {
	    			addIaNaOptionStatusToReply(dhcpIaNaOption,
	    					DhcpConstants.STATUS_CODE_NOTONLINK);
	    		}
	    		else {
					Binding binding = bindingMgr.findCurrentBinding(clientLink.getLink(), 
							clientIdOption, dhcpIaNaOption, requestMsg);
					if (binding != null) {
						haveBinding = true;
						binding = bindingMgr.updateBinding(binding, clientLink.getLink(), 
								clientIdOption, dhcpIaNaOption, requestMsg, IdentityAssoc.COMMITTED);
						if (binding != null) {
							addBindingToReply(clientLink.getLink(), binding);
						}
						else {
							haveBinding = false;
							addIaNaOptionStatusToReply(dhcpIaNaOption,
		    						DhcpConstants.STATUS_CODE_NOADDRSAVAIL);
						}
					}
					else {
						//TODO: what is the right thing to do here - we have
						//		a request, but the solicit failed somehow?
						addIaNaOptionStatusToReply(dhcpIaNaOption,
	    						DhcpConstants.STATUS_CODE_NOBINDING);
					}
	    		}
			}
    	}
    	
    	if (sendReply) {
            replyMsg.setMessageType(DhcpConstants.REPLY);
            if (haveBinding) {
            	populateReplyMsgOptions(clientLink.getLink());
            }
    	}
		return sendReply;    	
    }
}
