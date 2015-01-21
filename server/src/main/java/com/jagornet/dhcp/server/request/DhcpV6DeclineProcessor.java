/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6DeclineProcessor.java is part of Jagornet DHCP.
 *
 *   Jagornet DHCP is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Jagornet DHCP is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Jagornet DHCP.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcp.server.request;

import java.net.InetAddress;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.message.DhcpV6Message;
import com.jagornet.dhcp.option.v6.DhcpV6ClientIdOption;
import com.jagornet.dhcp.option.v6.DhcpV6IaNaOption;
import com.jagornet.dhcp.option.v6.DhcpV6IaPdOption;
import com.jagornet.dhcp.option.v6.DhcpV6IaTaOption;
import com.jagornet.dhcp.option.v6.DhcpV6ServerIdOption;
import com.jagornet.dhcp.server.request.binding.Binding;
import com.jagornet.dhcp.server.request.binding.BindingObject;
import com.jagornet.dhcp.server.request.binding.V6BindingAddress;
import com.jagornet.dhcp.server.request.binding.V6BindingPrefix;
import com.jagornet.dhcp.server.request.binding.V6NaAddrBindingManager;
import com.jagornet.dhcp.server.request.binding.V6PrefixBindingManager;
import com.jagornet.dhcp.server.request.binding.V6TaAddrBindingManager;
import com.jagornet.dhcp.util.DhcpConstants;

/**
 * Title: DhcpV6DeclineProcessor
 * Description: The main class for processing V6 DECLINE messages.
 * 
 * @author A. Gregory Rabil
 */

public class DhcpV6DeclineProcessor extends BaseDhcpV6Processor
{
	private static Logger log = LoggerFactory.getLogger(DhcpV6DeclineProcessor.class);
    
    /**
     * Construct an DhcpDeclientProcessor processor.
     * 
     * @param requestMsg the Decline message
     * @param clientLinkAddress the client link address
     */
    public DhcpV6DeclineProcessor(DhcpV6Message requestMsg, InetAddress clientLinkAddress)
    {
        super(requestMsg, clientLinkAddress);
    }

    /*
     * FROM RFC 3315:
     * 
     * 15.8. Decline Messages
     * 
     * Servers MUST discard any received Renew message that meets any of the
     * following conditions:
     * 
     * -  the message does not include a Server Identifier option.
     * 
     * -  the contents of the Server Identifier option does not match the
     *    server's identifier.
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

    	DhcpV6ServerIdOption requestedServerIdOption = requestMsg.getDhcpServerIdOption();
        if (requestedServerIdOption == null) {
            log.warn("Ignoring Decline message: " +
                    "Requested ServerId option is null");
           return false;
        }
        
        if (!dhcpServerIdOption.equals(requestedServerIdOption)) {
            log.warn("Ignoring Decline message: " +
                     "Requested ServerId: " + requestedServerIdOption +
                     "My ServerId: " + dhcpServerIdOption);
            return false;
        }
    	
    	if (requestMsg.getDhcpClientIdOption() == null) {
    		log.warn("Ignoring Decline message: " +
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
//    	   When the server receives a Decline message via unicast from a client
//    	   to which the server has not sent a unicast option, the server
//    	   discards the Decline message and responds with a Reply message
//    	   containing a Status Code option with the value UseMulticast, a Server
//    	   Identifier option containing the server's DUID, the Client Identifier
//    	   option from the client message, and no other options.
    	
    	if (shouldMulticast()) {
    		replyMsg.setMessageType(DhcpConstants.V6MESSAGE_TYPE_REPLY);
    		setReplyStatus(DhcpConstants.V6STATUS_CODE_USEMULTICAST);
    		return true;
    	}
    	
//    	   Upon the receipt of a valid Decline message, the server examines the
//    	   IAs and the addresses in the IAs for validity.  If the IAs in the
//    	   message are in a binding for the client, and the addresses in the IAs
//    	   have been assigned by the server to those IAs, the server deletes the
//    	   addresses from the IAs.  The server ignores addresses not assigned to
//    	   the IA (though it may choose to log an error if it finds such an
//    	   address).
//
//    	   The client has found any addresses in the Decline messages to be
//    	   already in use on its link.  Therefore, the server SHOULD mark the
//    	   addresses declined by the client so that those addresses are not
//    	   assigned to other clients, and MAY choose to make a notification that
//    	   addresses were declined.  Local policy on the server determines when
//    	   the addresses identified in a Decline message may be made available
//    	   for assignment.
//
//    	   After all the addresses have been processed, the server generates a
//    	   Reply message and includes a Status Code option with the value
//    	   Success, a Server Identifier option with the server's DUID, and a
//    	   Client Identifier option with the client's DUID.  For each IA in the
//    	   Decline message for which the server has no binding information, the
//    	   server adds an IA option using the IAID from the Release message and
//    	   includes a Status Code option with the value NoBinding in the IA
//    	   option.  No other options are included in the IA option.

    	boolean sendReply = true;
		DhcpV6ClientIdOption clientIdOption = requestMsg.getDhcpClientIdOption();
		
		List<DhcpV6IaNaOption> iaNaOptions = requestMsg.getIaNaOptions();
    	if ((iaNaOptions != null) && !iaNaOptions.isEmpty()) {
    		V6NaAddrBindingManager bindingMgr = dhcpServerConfig.getNaAddrBindingMgr();
    		if (bindingMgr != null) {
	    		for (DhcpV6IaNaOption dhcpIaNaOption : iaNaOptions) {
	    			log.info("Processing IA_NA Decline: " + dhcpIaNaOption.toString());
					Binding binding = bindingMgr.findCurrentBinding(clientLink, 
							clientIdOption, dhcpIaNaOption, requestMsg);
					if (binding != null) {
						Collection<BindingObject> bindingObjs = binding.getBindingObjects();
						if ((bindingObjs != null) && !bindingObjs.isEmpty()) {
							for (BindingObject bindingObj : bindingObjs) {
								bindingMgr.declineIaAddress(binding, (V6BindingAddress)bindingObj);
							}
						}
					}
					else {
						addIaNaOptionStatusToReply(dhcpIaNaOption,
								DhcpConstants.V6STATUS_CODE_NOBINDING);
					}
				}
    		}
    		else {
    			log.error("Unable to process IA_NA Decline:" +
    					" No NaAddrBindingManager available");
    		}
    	}
		
		List<DhcpV6IaTaOption> iaTaOptions = requestMsg.getIaTaOptions();
    	if ((iaTaOptions != null) && !iaTaOptions.isEmpty()) {
    		V6TaAddrBindingManager bindingMgr = dhcpServerConfig.getTaAddrBindingMgr();
    		if (bindingMgr != null) {
	    		for (DhcpV6IaTaOption dhcpIaTaOption : iaTaOptions) {
	    			log.info("Processing IA_TA Decline: " + dhcpIaTaOption.toString());
					Binding binding = bindingMgr.findCurrentBinding(clientLink, 
							clientIdOption, dhcpIaTaOption, requestMsg);
					if (binding != null) {
						Collection<BindingObject> bindingObjs = binding.getBindingObjects();
						if ((bindingObjs != null) && !bindingObjs.isEmpty()) {
							for (BindingObject bindingObj : bindingObjs) {
								bindingMgr.declineIaAddress(binding, (V6BindingAddress)bindingObj);
							}
						}
					}
					else {
						addIaTaOptionStatusToReply(dhcpIaTaOption,
								DhcpConstants.V6STATUS_CODE_NOBINDING);
					}
				}
    		}
    		else {
    			log.error("Unable to process IA_TA Decline:" +
    					" No TaAddrBindingManager available");
    		}
    	}
		
		List<DhcpV6IaPdOption> iaPdOptions = requestMsg.getIaPdOptions();
    	if ((iaPdOptions != null) && !iaPdOptions.isEmpty()) {
    		V6PrefixBindingManager bindingMgr = dhcpServerConfig.getPrefixBindingMgr();
    		if (bindingMgr != null) {
	    		for (DhcpV6IaPdOption dhcpIaPdOption : iaPdOptions) {
	    			log.info("Processing IA_PD Decline: " + dhcpIaPdOption.toString());
					Binding binding = bindingMgr.findCurrentBinding(clientLink, 
							clientIdOption, dhcpIaPdOption, requestMsg);
					if (binding != null) {
						Collection<BindingObject> bindingObjs = binding.getBindingObjects();
						if ((bindingObjs != null) && !bindingObjs.isEmpty()) {
							for (BindingObject bindingObj : bindingObjs) {
								bindingMgr.declineIaPrefix((V6BindingPrefix)bindingObj);
							}
						}
					}
					else {
						addIaPdOptionStatusToReply(dhcpIaPdOption,
								DhcpConstants.V6STATUS_CODE_NOBINDING);
					}
				}
    		}
    		else {
    			log.error("Unable to process IA_PD Decline:" +
    					" No PrefixBindingManager available");
    		}
    	}
    	
    	if (sendReply) {
            replyMsg.setMessageType(DhcpConstants.V6MESSAGE_TYPE_REPLY);
    		setReplyStatus(DhcpConstants.V6STATUS_CODE_SUCCESS);
    	}
		return sendReply;    	
    }
}
