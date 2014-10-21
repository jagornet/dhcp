/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6RenewProcessor.java is part of Jagornet DHCP.
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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.db.IdentityAssoc;
import com.jagornet.dhcp.message.DhcpV6Message;
import com.jagornet.dhcp.option.v6.DhcpV6ClientIdOption;
import com.jagornet.dhcp.option.v6.DhcpV6IaNaOption;
import com.jagornet.dhcp.option.v6.DhcpV6IaPdOption;
import com.jagornet.dhcp.option.v6.DhcpV6IaTaOption;
import com.jagornet.dhcp.option.v6.DhcpV6ServerIdOption;
import com.jagornet.dhcp.server.request.binding.Binding;
import com.jagornet.dhcp.server.request.binding.V6NaAddrBindingManager;
import com.jagornet.dhcp.server.request.binding.V6PrefixBindingManager;
import com.jagornet.dhcp.server.request.binding.V6TaAddrBindingManager;
import com.jagornet.dhcp.util.DhcpConstants;

/**
 * Title: DhcpV6RenewProcessor
 * Description: The main class for processing V6 RENEW messages.
 * 
 * @author A. Gregory Rabil
 */

public class DhcpV6RenewProcessor extends BaseDhcpV6Processor
{
	private static Logger log = LoggerFactory.getLogger(DhcpV6RenewProcessor.class);
    
    /**
     * Construct an DhcpRenewProcessor processor.
     * 
     * @param requestMsg the Renew message
     * @param clientLinkAddress the client link address
     */
    public DhcpV6RenewProcessor(DhcpV6Message requestMsg, InetAddress clientLinkAddress)
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

    	DhcpV6ServerIdOption requestedServerIdOption = requestMsg.getDhcpServerIdOption();
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
    		replyMsg.setMessageType(DhcpConstants.V6MESSAGE_TYPE_REPLY);
    		setReplyStatus(DhcpConstants.V6STATUS_CODE_USEMULTICAST);
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
		DhcpV6ClientIdOption clientIdOption = requestMsg.getDhcpClientIdOption();
		
		List<DhcpV6IaNaOption> iaNaOptions = requestMsg.getIaNaOptions();
    	if ((iaNaOptions != null) && !iaNaOptions.isEmpty()) {
    		V6NaAddrBindingManager bindingMgr = dhcpServerConfig.getNaAddrBindingMgr();
    		if (bindingMgr != null) {
	    		for (DhcpV6IaNaOption dhcpIaNaOption : iaNaOptions) {
	    			log.info("Processing IA_NA Renew: " + dhcpIaNaOption.toString());
					Binding binding = bindingMgr.findCurrentBinding(clientLink, 
							clientIdOption, dhcpIaNaOption, requestMsg);
					if (binding != null) {
						// zero out the lifetimes of any invalid addresses
						if(!allIaAddrsOnLink(dhcpIaNaOption, clientLink)) {
							replyMsg.addIaNaOption(dhcpIaNaOption);
						}
						else {
							binding = bindingMgr.updateBinding(binding, clientLink, 
									clientIdOption, dhcpIaNaOption, requestMsg, 
									IdentityAssoc.COMMITTED);
							if (binding != null) {
								addBindingToReply(clientLink, binding);
								bindings.add(binding);
							}
							else {
								addIaNaOptionStatusToReply(dhcpIaNaOption,
			    						DhcpConstants.V6STATUS_CODE_NOADDRSAVAIL);
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
    			log.error("Unable to process IA_NA Renew:" +
    					" No NaAddrBindingManager available");
    		}
    	}
    	
		List<DhcpV6IaTaOption> iaTaOptions = requestMsg.getIaTaOptions();
    	if ((iaTaOptions != null) && !iaTaOptions.isEmpty()) {
    		V6TaAddrBindingManager bindingMgr = dhcpServerConfig.getTaAddrBindingMgr();
    		if (bindingMgr != null) {
	    		for (DhcpV6IaTaOption dhcpIaTaOption : iaTaOptions) {
	    			log.info("Processing IA_TA Renew: " + dhcpIaTaOption.toString());
					Binding binding = bindingMgr.findCurrentBinding(clientLink, 
							clientIdOption, dhcpIaTaOption, requestMsg);
					if (binding != null) {
						// zero out the lifetimes of any invalid addresses
						if(!allIaAddrsOnLink(dhcpIaTaOption, clientLink)) {
							replyMsg.addIaTaOption(dhcpIaTaOption);
						}
						else {
							binding = bindingMgr.updateBinding(binding, clientLink, 
									clientIdOption, dhcpIaTaOption, requestMsg, 
									IdentityAssoc.COMMITTED);
							if (binding != null) {
								addBindingToReply(clientLink, binding);
								bindings.add(binding);
							}
							else {
								addIaTaOptionStatusToReply(dhcpIaTaOption,
			    						DhcpConstants.V6STATUS_CODE_NOADDRSAVAIL);
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
    			log.error("Unable to process IA_TA Renew:" +
    					" No TaAddrBindingManager available");
    		}
    	}
    	
		List<DhcpV6IaPdOption> iaPdOptions = requestMsg.getIaPdOptions();
    	if ((iaPdOptions != null) && !iaPdOptions.isEmpty()) {
    		V6PrefixBindingManager bindingMgr = dhcpServerConfig.getPrefixBindingMgr();
    		if (bindingMgr != null) {
	    		for (DhcpV6IaPdOption dhcpIaPdOption : iaPdOptions) {
	    			log.info("Processing IA_PD Renew: " + dhcpIaPdOption.toString());
					Binding binding = bindingMgr.findCurrentBinding(clientLink, 
							clientIdOption, dhcpIaPdOption, requestMsg);
					if (binding != null) {
						// zero out the lifetimes of any invalid addresses
						if(!allIaPrefixesOnLink(dhcpIaPdOption, clientLink)) {
							replyMsg.addIaPdOption(dhcpIaPdOption);
						}
						else {
							binding = bindingMgr.updateBinding(binding, clientLink, 
									clientIdOption, dhcpIaPdOption, requestMsg, 
									IdentityAssoc.COMMITTED);
							if (binding != null) {
								addBindingToReply(clientLink, binding);
								bindings.add(binding);
							}
							else {
								addIaPdOptionStatusToReply(dhcpIaPdOption,
			    						DhcpConstants.V6STATUS_CODE_NOPREFIXAVAIL);
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
    			log.error("Unable to process IA_PD Renew:" +
    					" No PrefixBindingManager available");
    		}
    	}
    	
    	if (sendReply) {
            replyMsg.setMessageType(DhcpConstants.V6MESSAGE_TYPE_REPLY);
            if (!bindings.isEmpty()) {
            	populateReplyMsgOptions(clientLink);
    			processDdnsUpdates(true);
            }
            else {
            	log.warn("Reply message has no bindings");
            }
    	}
    	return sendReply;    	
    }
}
