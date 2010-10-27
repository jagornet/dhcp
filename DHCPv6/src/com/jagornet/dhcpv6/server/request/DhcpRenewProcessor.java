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
import com.jagornet.dhcpv6.option.DhcpIaPdOption;
import com.jagornet.dhcpv6.option.DhcpIaTaOption;
import com.jagornet.dhcpv6.option.DhcpServerIdOption;
import com.jagornet.dhcpv6.server.request.binding.Binding;
import com.jagornet.dhcpv6.server.request.binding.NaAddrBindingManager;
import com.jagornet.dhcpv6.server.request.binding.PrefixBindingManager;
import com.jagornet.dhcpv6.server.request.binding.TaAddrBindingManager;
import com.jagornet.dhcpv6.util.DhcpConstants;

/**
 * Title: DhcpRenewProcessor
 * Description: The main class for processing RENEW messages.
 * 
 * @author A. Gregory Rabil
 */

public class DhcpRenewProcessor extends BaseDhcpProcessor
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpRenewProcessor.class);
    
    /**
     * Construct an DhcpRenewProcessor processor.
     * 
     * @param requestMsg the Renew message
     * @param clientLinkAddress the client link address
     */
    public DhcpRenewProcessor(DhcpMessage requestMsg, InetAddress clientLinkAddress)
    {
        super(requestMsg, clientLinkAddress);
    }

    /*
     * FROM RFC 3315:
     * 
     * 15.6. Renew Message
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

    	DhcpServerIdOption requestedServerIdOption = requestMsg.getDhcpServerIdOption();
        if (requestedServerIdOption == null) {
            log.warn("Ignoring Renew message: " +
                    "Requested ServerId option is null");
           return false;
        }
        
        if (!dhcpServerIdOption.equals(requestedServerIdOption)) {
            log.warn("Ignoring Renew message: " +
                     "Requested ServerId: " + requestedServerIdOption +
                     "My ServerId: " + dhcpServerIdOption);
            return false;
        }
    	
    	if (requestMsg.getDhcpClientIdOption() == null) {
    		log.warn("Ignoring Renew message: " +
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
//      When the server receives a Renew message via unicast from a client
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
    	
//		If the server cannot find a client entry for the IA the server
//		returns the IA containing no addresses with a Status Code option set
//		to NoBinding in the Reply message.
//
//		If the server finds that any of the addresses are not appropriate for
//		the link to which the client is attached, the server returns the
//		address to the client with lifetimes of 0.
//
//		If the server finds the addresses in the IA for the client then the
//		server sends back the IA to the client with new lifetimes and T1/T2
//		times.  The server may choose to change the list of addresses and the
//		lifetimes of addresses in IAs that are returned to the client.

		boolean sendReply = true;
		DhcpClientIdOption clientIdOption = requestMsg.getDhcpClientIdOption();
		
		List<DhcpIaNaOption> iaNaOptions = requestMsg.getIaNaOptions();
    	if ((iaNaOptions != null) && !iaNaOptions.isEmpty()) {
    		NaAddrBindingManager bindingMgr = dhcpServerConfig.getNaAddrBindingMgr();
    		if (bindingMgr != null) {
	    		for (DhcpIaNaOption dhcpIaNaOption : iaNaOptions) {
	    			log.info("Processing IA_NA Renew: " + dhcpIaNaOption.toString());
					Binding binding = bindingMgr.findCurrentBinding(clientLink.getLink(), 
							clientIdOption, dhcpIaNaOption, requestMsg);
					if (binding != null) {
						// zero out the lifetimes of any invalid addresses
						if(!allIaAddrsOnLink(dhcpIaNaOption, clientLink)) {
							replyMsg.addIaNaOption(dhcpIaNaOption);
						}
						else {
							binding = bindingMgr.updateBinding(binding, clientLink.getLink(), 
									clientIdOption, dhcpIaNaOption, requestMsg, IdentityAssoc.COMMITTED);
							if (binding != null) {
								addBindingToReply(clientLink, binding);
								bindings.add(binding);
							}
							else {
								addIaNaOptionStatusToReply(dhcpIaNaOption,
			    						DhcpConstants.STATUS_CODE_NOADDRSAVAIL);
							}
						}
					}
					else {
						addIaNaOptionStatusToReply(dhcpIaNaOption,
	    						DhcpConstants.STATUS_CODE_NOBINDING);
					}
				}
    		}
    		else {
    			log.error("Unable to process IA_NA Renew:" +
    					" No NaAddrBindingManager available");
    		}
    	}
    	
		List<DhcpIaTaOption> iaTaOptions = requestMsg.getIaTaOptions();
    	if ((iaTaOptions != null) && !iaTaOptions.isEmpty()) {
    		TaAddrBindingManager bindingMgr = dhcpServerConfig.getTaAddrBindingMgr();
    		if (bindingMgr != null) {
	    		for (DhcpIaTaOption dhcpIaTaOption : iaTaOptions) {
	    			log.info("Processing IA_TA Renew: " + dhcpIaTaOption.toString());
					Binding binding = bindingMgr.findCurrentBinding(clientLink.getLink(), 
							clientIdOption, dhcpIaTaOption, requestMsg);
					if (binding != null) {
						// zero out the lifetimes of any invalid addresses
						if(!allIaAddrsOnLink(dhcpIaTaOption, clientLink)) {
							replyMsg.addIaTaOption(dhcpIaTaOption);
						}
						else {
							binding = bindingMgr.updateBinding(binding, clientLink.getLink(), 
									clientIdOption, dhcpIaTaOption, requestMsg, IdentityAssoc.COMMITTED);
							if (binding != null) {
								addBindingToReply(clientLink, binding);
								bindings.add(binding);
							}
							else {
								addIaTaOptionStatusToReply(dhcpIaTaOption,
			    						DhcpConstants.STATUS_CODE_NOADDRSAVAIL);
							}
						}
					}
					else {
						addIaTaOptionStatusToReply(dhcpIaTaOption,
	    						DhcpConstants.STATUS_CODE_NOBINDING);
					}
				}
    		}
    		else {
    			log.error("Unable to process IA_TA Renew:" +
    					" No TaAddrBindingManager available");
    		}
    	}
    	
		List<DhcpIaPdOption> iaPdOptions = requestMsg.getIaPdOptions();
    	if ((iaPdOptions != null) && !iaPdOptions.isEmpty()) {
    		PrefixBindingManager bindingMgr = dhcpServerConfig.getPrefixBindingMgr();
    		if (bindingMgr != null) {
	    		for (DhcpIaPdOption dhcpIaPdOption : iaPdOptions) {
	    			log.info("Processing IA_TA Renew: " + dhcpIaPdOption.toString());
					Binding binding = bindingMgr.findCurrentBinding(clientLink.getLink(), 
							clientIdOption, dhcpIaPdOption, requestMsg);
					if (binding != null) {
						// zero out the lifetimes of any invalid addresses
						if(!allIaPrefixesOnLink(dhcpIaPdOption, clientLink)) {
							replyMsg.addIaPdOption(dhcpIaPdOption);
						}
						else {
							binding = bindingMgr.updateBinding(binding, clientLink.getLink(), 
									clientIdOption, dhcpIaPdOption, requestMsg, IdentityAssoc.COMMITTED);
							if (binding != null) {
								addBindingToReply(clientLink, binding);
								bindings.add(binding);
							}
							else {
								addIaPdOptionStatusToReply(dhcpIaPdOption,
			    						DhcpConstants.STATUS_CODE_NOADDRSAVAIL);
							}
						}
					}
					else {
						addIaPdOptionStatusToReply(dhcpIaPdOption,
	    						DhcpConstants.STATUS_CODE_NOBINDING);
					}
				}
    		}
    		else {
    			log.error("Unable to process IA_PD Renew:" +
    					" No PrefixBindingManager available");
    		}
    	}
    	
    	if (sendReply) {
            replyMsg.setMessageType(DhcpConstants.REPLY);
            if (!bindings.isEmpty()) {
            	populateReplyMsgOptions(clientLink);
    			processDdnsUpdates();
            }
            else {
            	log.warn("Reply message has no bindings");
            }
    	}
    	return sendReply;    	
    }
}
