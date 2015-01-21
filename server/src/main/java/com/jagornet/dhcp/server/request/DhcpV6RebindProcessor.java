/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6RebindProcessor.java is part of Jagornet DHCP.
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
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.request.binding.Binding;
import com.jagornet.dhcp.server.request.binding.V6NaAddrBindingManager;
import com.jagornet.dhcp.server.request.binding.V6PrefixBindingManager;
import com.jagornet.dhcp.server.request.binding.V6TaAddrBindingManager;
import com.jagornet.dhcp.util.DhcpConstants;

/**
 * Title: DhcpV6RebindProcessor
 * Description: The main class for processing V6 REBIND messages.
 * 
 * @author A. Gregory Rabil
 */

public class DhcpV6RebindProcessor extends BaseDhcpV6Processor
{
	private static Logger log = LoggerFactory.getLogger(DhcpV6RebindProcessor.class);
    
    /**
     * Construct an DhcpRebindProcessor processor.
     * 
     * @param requestMsg the Rebind message
     * @param clientLinkAddress the client link address
     */
    public DhcpV6RebindProcessor(DhcpV6Message requestMsg, InetAddress clientLinkAddress)
    {
        super(requestMsg, clientLinkAddress);
    }

    /*
     * FROM RFC 3315:
     * 
     * 15.7. Rebind Message
     * 
     *    Servers MUST discard any received Rebind messages that do not include
     *    a Client Identifier option or that do include a Server Identifier
     *    option.
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
    		log.warn("Ignoring unicast Rebind Message");
    		return false;
    	}
    	
    	if (requestMsg.getDhcpClientIdOption() == null) {
    		log.warn("Ignoring Rebind message: " +
    				"ClientId option is null");
    		return false;
    	}
    	
    	if (requestMsg.getDhcpServerIdOption() != null) {
    		log.warn("Ignoring Rebind message: " +
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
//		   If the server cannot find a client entry for the IA and the server
//		   determines that the addresses in the IA are not appropriate for the
//		   link to which the client's interface is attached according to the
//		   server's explicit configuration information, the server MAY send a
//		   Reply message to the client containing the client's IA, with the
//		   lifetimes for the addresses in the IA set to zero.  This Reply
//		   constitutes an explicit notification to the client that the addresses
//		   in the IA are no longer valid.  In this situation, if the server does
//		   not send a Reply message it silently discards the Rebind message.
//
//		   If the server finds that any of the addresses are no longer
//		   appropriate for the link to which the client is attached, the server
//		   returns the address to the client with lifetimes of 0.
//
//		   If the server finds the addresses in the IA for the client then the
//		   server SHOULD send back the IA to the client with new lifetimes and
//		   T1/T2 times.

		boolean sendReply = true;
		DhcpV6ClientIdOption clientIdOption = requestMsg.getDhcpClientIdOption();
		
		List<DhcpV6IaNaOption> iaNaOptions = requestMsg.getIaNaOptions();
    	if ((iaNaOptions != null) && !iaNaOptions.isEmpty()) {
    		V6NaAddrBindingManager bindingMgr = dhcpServerConfig.getNaAddrBindingMgr();
    		if (bindingMgr != null) {
	    		for (DhcpV6IaNaOption dhcpIaNaOption : iaNaOptions) {
	    			log.info("Processing IA_NA Rebind: " + dhcpIaNaOption.toString());
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
						if (DhcpServerPolicies.effectivePolicyAsBoolean(requestMsg,
								clientLink.getLink(), Property.VERIFY_UNKNOWN_REBIND)) {
							// zero out the lifetimes of any invalid addresses
							allIaAddrsOnLink(dhcpIaNaOption, clientLink);
							replyMsg.addIaNaOption(dhcpIaNaOption);
						}
					}
				}
    		}
    		else {
    			log.error("Unable to process IA_NA Rebind:" +
    					" No NaAddrBindingManager available");
    		}
    	}
		
		List<DhcpV6IaTaOption> iaTaOptions = requestMsg.getIaTaOptions();
    	if ((iaTaOptions != null) && !iaTaOptions.isEmpty()) {
    		V6TaAddrBindingManager bindingMgr = dhcpServerConfig.getTaAddrBindingMgr();
    		if (bindingMgr != null) {
	    		for (DhcpV6IaTaOption dhcpIaTaOption : iaTaOptions) {
	    			log.info("Processing IA_TA Rebind: " + dhcpIaTaOption.toString());
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
						if (DhcpServerPolicies.effectivePolicyAsBoolean(requestMsg,
								clientLink.getLink(), Property.VERIFY_UNKNOWN_REBIND)) {
							// zero out the lifetimes of any invalid addresses
							allIaAddrsOnLink(dhcpIaTaOption, clientLink);
							replyMsg.addIaTaOption(dhcpIaTaOption);
						}
					}
				}
    		}
    		else {
    			log.error("Unable to process IA_TA Rebind:" +
    					" No TaAddrBindingManager available");
    		}
    	}
		
		List<DhcpV6IaPdOption> iaPdOptions = requestMsg.getIaPdOptions();
    	if ((iaPdOptions != null) && !iaPdOptions.isEmpty()) {
    		V6PrefixBindingManager bindingMgr = dhcpServerConfig.getPrefixBindingMgr();
    		if (bindingMgr != null) {
	    		for (DhcpV6IaPdOption dhcpIaPdOption : iaPdOptions) {
	    			log.info("Processing IA_PD Rebind: " + dhcpIaPdOption.toString());
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
						if (DhcpServerPolicies.effectivePolicyAsBoolean(requestMsg,
								clientLink.getLink(), Property.VERIFY_UNKNOWN_REBIND)) {
							// zero out the lifetimes of any invalid addresses
							allIaPrefixesOnLink(dhcpIaPdOption, clientLink);
							replyMsg.addIaPdOption(dhcpIaPdOption);
						}
					}
				}
    		}
    		else {
    			log.error("Unable to process IA_PD Rebind:" +
    					" No PrefixBindingManager available");
    		}
    	}

	    if (bindings.isEmpty() && 
	    		!DhcpServerPolicies.effectivePolicyAsBoolean(requestMsg,
	    				clientLink.getLink(), Property.VERIFY_UNKNOWN_REBIND)) {
			sendReply = false;
		}

	    if (sendReply) {
            replyMsg.setMessageType(DhcpConstants.V6MESSAGE_TYPE_REPLY);
            if (!bindings.isEmpty()) {
            	populateReplyMsgOptions(clientLink);
    			processDdnsUpdates(true);
           }
	    }
		return sendReply;    	
    }
}
