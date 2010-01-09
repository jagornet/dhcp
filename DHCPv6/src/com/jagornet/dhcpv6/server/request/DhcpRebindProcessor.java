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
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcpv6.server.request.binding.Binding;
import com.jagornet.dhcpv6.util.DhcpConstants;

// TODO: Auto-generated Javadoc
/**
 * Title: DhcpRebindProcessor
 * Description: The main class for processing REBIND messages.
 * 
 * @author A. Gregory Rabil
 */

public class DhcpRebindProcessor extends BaseDhcpProcessor
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpRebindProcessor.class);
    
    /**
     * Construct an DhcpRebindProcessor processor.
     * 
     * @param requestMsg the Rebind message
     * @param clientLinkAddress the client link address
     */
    public DhcpRebindProcessor(DhcpMessage requestMsg, InetAddress clientLinkAddress)
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
		boolean haveBinding = false;
		DhcpClientIdOption clientIdOption = requestMsg.getDhcpClientIdOption();
		List<DhcpIaNaOption> iaNaOptions = requestMsg.getIaNaOptions();
    	if (iaNaOptions != null) {
    		for (DhcpIaNaOption dhcpIaNaOption : iaNaOptions) {
    			log.info("Processing IA_NA Rebind: " + dhcpIaNaOption.toString());
				Binding binding = bindingMgr.findCurrentBinding(clientLink.getLink(), 
						clientIdOption, dhcpIaNaOption, requestMsg);
				if (binding != null) {
					haveBinding = true;
					// zero out the lifetimes of any invalid addresses
					if(!allIaAddrsOnLink(dhcpIaNaOption, clientLink)) {
						replyMsg.addIaNaOption(dhcpIaNaOption);
					}
					else {
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
				}
				else {
					if (DhcpServerPolicies.effectivePolicyAsBoolean(clientLink.getLink(), 
							Property.VERIFY_UNKNOWN_REBIND)) {
						// zero out the lifetimes of any invalid addresses
						allIaAddrsOnLink(dhcpIaNaOption, clientLink);
						replyMsg.addIaNaOption(dhcpIaNaOption);
					}
				}
			}
    	}

	    if (!haveBinding && 
	    		!DhcpServerPolicies.effectivePolicyAsBoolean(clientLink.getLink(), 
	    				Property.VERIFY_UNKNOWN_REBIND)) {
			sendReply = false;
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
