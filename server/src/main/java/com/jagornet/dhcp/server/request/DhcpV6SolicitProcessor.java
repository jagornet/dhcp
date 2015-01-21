/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6SolicitProcessor.java is part of Jagornet DHCP.
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

import com.jagornet.dhcp.db.IaAddress;
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
import com.jagornet.dhcp.xml.Link;

/**
 * Title: DhcpV6SolicitProcessor
 * Description: The main class for processing V6 SOLICIT messages.
 * 
 * @author A. Gregory Rabil
 */

public class DhcpV6SolicitProcessor extends BaseDhcpV6Processor
{
	private static Logger log = LoggerFactory.getLogger(DhcpV6SolicitProcessor.class);
    
    /**
     * Construct an DhcpSolicitProcessor processor.
     * 
     * @param requestMsg the Solicit message
     * @param clientLinkAddress the client link address
     */
    public DhcpV6SolicitProcessor(DhcpV6Message requestMsg, InetAddress clientLinkAddress)
    {
        super(requestMsg, clientLinkAddress);
    }

    /*
     * FROM RFC 3315:
     * 
     * 15.2. Solicit Message
     * 
     *    Servers MUST discard any Solicit messages that do not include a
     *    Client Identifier option or that do include a Server Identifier
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
    		log.warn("Ignoring unicast Solicit Message");
    		return false;
    	}
    	
    	DhcpV6ClientIdOption dhcpClientId = requestMsg.getDhcpClientIdOption();
    	if ((dhcpClientId == null) || 
    			(dhcpClientId.getOpaqueData() == null) ||
    			((dhcpClientId.getOpaqueData().getAscii() == null) &&
    					(dhcpClientId.getOpaqueData().getHex() == null))) {
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
//		   If the Solicit message from the client included one or more IA
//		   options, the server MUST include IA options in the Advertise message
//		   containing any addresses that would be assigned to IAs contained in
//		   the Solicit message from the client.  If the client has included
//		   addresses in the IAs in the Solicit message, the server uses those
//		   addresses as hints about the addresses the client would like to
//		   receive.
//
//		   If the server will not assign any addresses to any IAs in a
//		   subsequent Request from the client, the server MUST send an Advertise
//		   message to the client that includes only a Status Code option with
//		   code NoAddrsAvail and a status message for the user, a Server
//		   Identifier option with the server's DUID, and a Client Identifier
//		   option with the client's DUID.

		boolean sendReply = true;
		boolean rapidCommit = isRapidCommit(requestMsg, clientLink.getLink());
		byte state = rapidCommit ? IaAddress.COMMITTED : IaAddress.ADVERTISED;
		DhcpV6ClientIdOption clientIdOption = requestMsg.getDhcpClientIdOption();
		
		List<DhcpV6IaNaOption> iaNaOptions = requestMsg.getIaNaOptions();
    	if ((iaNaOptions != null) && !iaNaOptions.isEmpty()) {
    		V6NaAddrBindingManager bindingMgr = dhcpServerConfig.getNaAddrBindingMgr();
    		if (bindingMgr != null) {
	    		for (DhcpV6IaNaOption dhcpIaNaOption : iaNaOptions) {
	    			log.info("Processing IA_NA Solicit: " + dhcpIaNaOption.toString());
					Binding binding = bindingMgr.findCurrentBinding(clientLink, 
							clientIdOption, dhcpIaNaOption, requestMsg);
					if (binding == null) {
						// no current binding for this IA_NA, create a new one
						binding = bindingMgr.createSolicitBinding(clientLink, 
								clientIdOption, dhcpIaNaOption, requestMsg, state);
					}
					else {
						binding = bindingMgr.updateBinding(binding, clientLink, 
								clientIdOption, dhcpIaNaOption, requestMsg, state);
					}
					if (binding != null) {
						// have a good binding, put it in the reply with options
						addBindingToReply(clientLink, binding);
						bindings.add(binding);
					}
					else {
						// something went wrong, report NoAddrsAvail status for IA_NA
// TAHI tests want this status at the message level
//	    				addIaNaOptionStatusToReply(dhcpIaNaOption, 
//	    						DhcpConstants.STATUS_CODE_NOADDRSAVAIL);
		    			setReplyStatus(DhcpConstants.V6STATUS_CODE_NOADDRSAVAIL);
					}
	    		}
    		}
    		else {
    			log.error("Unable to process IA_NA Solicit:" +
    					" No NaAddrBindingManager available");
    		}
    	}
		
		List<DhcpV6IaTaOption> iaTaOptions = requestMsg.getIaTaOptions();
    	if ((iaTaOptions != null) && !iaTaOptions.isEmpty()) {
    		V6TaAddrBindingManager bindingMgr = dhcpServerConfig.getTaAddrBindingMgr();
    		if (bindingMgr != null) {
	    		for (DhcpV6IaTaOption dhcpIaTaOption : iaTaOptions) {
	    			log.info("Processing IA_TA Solicit: " + dhcpIaTaOption.toString());
					Binding binding = bindingMgr.findCurrentBinding(clientLink, 
							clientIdOption, dhcpIaTaOption, requestMsg);
					if (binding == null) {
						// no current binding for this IA_TA, create a new one
						binding = bindingMgr.createSolicitBinding(clientLink, 
								clientIdOption, dhcpIaTaOption, requestMsg, state);
					}
					else {
						binding = bindingMgr.updateBinding(binding, clientLink, 
								clientIdOption, dhcpIaTaOption, requestMsg, state);
					}
					if (binding != null) {
						// have a good binding, put it in the reply with options
						addBindingToReply(clientLink, binding);
						bindings.add(binding);
					}
					else {
						// something went wrong, report NoAddrsAvail status for IA_TA
// TAHI tests want this status at the message level
//	    				addIaTaOptionStatusToReply(dhcpIaTaOption, 
//	    						DhcpConstants.STATUS_CODE_NOADDRSAVAIL);
		    			setReplyStatus(DhcpConstants.V6STATUS_CODE_NOADDRSAVAIL);
					}
	    		}
    		}
    		else {
    			log.error("Unable to process IA_TA Solicit:" +
    					" No TaAddrBindingManager available");
    		}
    	}
		
		List<DhcpV6IaPdOption> iaPdOptions = requestMsg.getIaPdOptions();
    	if ((iaPdOptions != null) && !iaPdOptions.isEmpty()) {
    		V6PrefixBindingManager bindingMgr = dhcpServerConfig.getPrefixBindingMgr();
    		if (bindingMgr != null) {
	    		for (DhcpV6IaPdOption dhcpIaPdOption : iaPdOptions) {
	    			log.info("Processing IA_PD Solicit: " + dhcpIaPdOption.toString());
					Binding binding = bindingMgr.findCurrentBinding(clientLink, 
							clientIdOption, dhcpIaPdOption, requestMsg);
					if (binding == null) {
						// no current binding for this IA_PD, create a new one
						binding = bindingMgr.createSolicitBinding(clientLink, 
								clientIdOption, dhcpIaPdOption, requestMsg, state);
					}
					else {
						binding = bindingMgr.updateBinding(binding, clientLink, 
								clientIdOption, dhcpIaPdOption, requestMsg, state);
					}
					if (binding != null) {
						// have a good binding, put it in the reply with options
						addBindingToReply(clientLink, binding);
						bindings.add(binding);
					}
					else {
						// something went wrong, report NoPrefixAvail status for IA_PD
// TAHI tests want this status at the message level
//	    				addIaPdOptionStatusToReply(dhcpIaPdOption, 
//	    						DhcpConstants.STATUS_CODE_NOPREFIXAVAIL);
		    			setReplyStatus(DhcpConstants.V6STATUS_CODE_NOPREFIXAVAIL);
					}
	    		}
    		}
    		else {
    			log.error("Unable to process IA_PD Solicit:" +
    					" No PrefixBindingManager available");
    		}
    	}
    	
    	if (sendReply) {
    		if (rapidCommit) {
    			replyMsg.setMessageType(DhcpConstants.V6MESSAGE_TYPE_REPLY);
    		}
    		else {
    	        replyMsg.setMessageType(DhcpConstants.V6MESSAGE_TYPE_ADVERTISE);
    		}
    		if (!bindings.isEmpty()) {
    			populateReplyMsgOptions(clientLink);
    			if (rapidCommit) {
    				processDdnsUpdates(true);
    			}
    			else {
    				processDdnsUpdates(false);
    			}
    		}
    	}    	
		return sendReply;
    }

	/**
	 * Checks if is rapid commit.
	 * 
	 * @param requestMsg the request msg
	 * @param clientLink the client link
	 * 
	 * @return true, if is rapid commit
	 */
	private boolean isRapidCommit(DhcpV6Message requestMsg, Link clientLink)
	{
		if (requestMsg.hasOption(DhcpConstants.V6OPTION_RAPID_COMMIT) && 
				DhcpServerPolicies.effectivePolicyAsBoolean(requestMsg, clientLink, 
						Property.SUPPORT_RAPID_COMMIT)) {
			return true;
		}
		return false;
	}
}
