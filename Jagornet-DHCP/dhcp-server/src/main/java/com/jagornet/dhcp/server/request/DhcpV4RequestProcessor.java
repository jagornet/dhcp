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

import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.option.v4.DhcpV4RequestedIpAddressOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4ServerIdOption;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.db.IdentityAssoc;
import com.jagornet.dhcp.server.request.binding.Binding;
import com.jagornet.dhcp.server.request.binding.V4AddrBindingManager;

/**
 * Title: DhcpV4RequestProcessor
 * Description: The main class for processing V4 REQUEST messages.
 * 
 * @author A. Gregory Rabil
 */

public class DhcpV4RequestProcessor extends BaseDhcpV4Processor
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpV4RequestProcessor.class);
	
	protected DhcpV4RequestedIpAddressOption requestedIpAddrOption;
	protected enum RequestType {
		Request_Selecting, Request_Renewing, Request_Rebinding, Request_InitReboot;
	}
	protected RequestType type;
    
    /**
     * Construct an DhcpV4RequestProcessor processor.
     * 
     * @param requestMsg the Request message
     * @param clientLinkAddress the client link address
     */
    public DhcpV4RequestProcessor(DhcpV4Message requestMsg, InetAddress clientLinkAddress)
    {
        super(requestMsg, clientLinkAddress);
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.server.request.BaseDhcpProcessor#preProcess()
     */
    @Override
    public boolean preProcess()
    {
    	if (!super.preProcess()) {
    		return false;
    	}

    	DhcpV4ServerIdOption requestedServerIdOption = requestMsg.getDhcpV4ServerIdOption();
        requestedIpAddrOption = (DhcpV4RequestedIpAddressOption)
        						requestMsg.getDhcpOption(DhcpConstants.V4OPTION_REQUESTED_IP);

        // first determine what KIND of request we are dealing with
    	if (requestMsg.getCiAddr().equals(DhcpConstants.ZEROADDR_V4)) {
    		// the ciAddr MUST be 0.0.0.0 for Init-Reboot and Selecting
            if (requestedServerIdOption == null) {
            	// init-reboot MUST NOT have server-id option
            	type = RequestType.Request_InitReboot;
            }
            else {
            	// selecting MUST have server-id option
            	type = RequestType.Request_Selecting;                
            }    		
    	}
    	else {
    		// the ciAddr MUST NOT be 0.0.0.0 for Renew and Rebind
    		if (requestMsg.isUnicast()) {
    			// renew is unicast
    			// NOTE: this will not happen if the v4 broadcast interface used at startup,
    			//		 but handling of DHCPv4 renew/rebind is the same anyway
    			type = RequestType.Request_Renewing;
    		}
    		else {
    			// rebind is broadcast
    			type = RequestType.Request_Rebinding;
    		}
    	}
    	
    	if ((type == RequestType.Request_InitReboot) || (type == RequestType.Request_Selecting)) {
        	if (requestedIpAddrOption == null) {
        		log.warn("Ignoring " + type + " message: " +
        				"Requested IP option is null");
        		return false;
        	}
        	if (type == RequestType.Request_Selecting) {
                String requestedServerId = requestedServerIdOption.getIpAddress();
                DhcpV4ServerIdOption myServerIdOpt = getDhcpV4ServerIdOption();
                String myServerId = (myServerIdOpt != null) ? myServerIdOpt.getIpAddress() : null;
                boolean match = (myServerId != null) && myServerId.equals(requestedServerId);
                if (!match) {
                    InetAddress overrideAddr = getServerIdOverride(requestMsg);
                    if ((overrideAddr != null) && overrideAddr.getHostAddress().equals(requestedServerId)) {
                        match = true;
                        log.info("Accepting " + type + " message: requested ServerId matches RFC 5107 override address: " + 
                                requestedServerId);
                    }
                }
                if (!match) {
                    log.warn("Ignoring " + type + " message: " +
                             "Requested ServerId: " + requestedServerIdOption +
                             " My ServerId: " + myServerIdOpt);
                    return false;
                }
        	}
    	}
    	else {	// type == Renewing or Rebinding
    		if (requestedIpAddrOption != null) {
        		log.warn("Ignoring " + type + " message: " +
						"Requested IP option is not null");
				return false;    			
    		}
    	}

    	return true;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.server.request.BaseDhcpProcessor#process()
     */
    @Override
    public boolean process()
    {
		boolean sendReply = true;
		byte[] clientId = requestMsg.getClientId();
		
		V4AddrBindingManager bindingMgr = dhcpServerConfig.getV4AddrBindingMgr();
		if (bindingMgr != null) {
			log.info("Processing " + type +
					 " from clientId=" + Util.toHexString(clientId) +
					 " ciAddr=" + requestMsg.getCiAddr().getHostAddress() +
					 " requestedIpAddrOption=" + requestedIpAddrOption);

			if (!addrOnLink(requestedIpAddrOption, clientLink)) {
    			log.info("Client requested IP is off-link, returning NAK");
    			replyMsg.setMessageType((short)DhcpConstants.V4MESSAGE_TYPE_NAK);
    			return sendReply;
    		}
    		else {
    			Binding binding = bindingMgr.findCurrentBinding(clientLink, 
    					clientId, requestMsg);
				if (binding != null) {
					binding = bindingMgr.updateBinding(binding, clientLink, 
							clientId, requestMsg, IdentityAssoc.LEASED);
					if (binding != null) {
						addBindingToReply(clientLink, binding);
						bindings.add(binding);
					}
					else {
						log.error("Failed to update binding for client: " + 
								Util.toHexString(clientId));
						sendReply = false;
					}
				}
				else {
					log.error("No Binding available for client: " + 
							Util.toHexString(clientId));
					sendReply = false;
				}
			}
		}
		else {
			log.error("Unable to process V4 Request:" +
					" No V4AddrBindingManager available");
		}
    	
    	if (sendReply) {
            replyMsg.setMessageType((short)DhcpConstants.V4MESSAGE_TYPE_ACK);
            if (!bindings.isEmpty()) {
    			processDdnsUpdates(true);
    			processHaBindingUpdates(replyMsg.getDhcpOptionMap());
            }
    	}
		return sendReply;    	
    }
}
