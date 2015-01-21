/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6MessageHandler.java is part of Jagornet DHCP.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.message.DhcpV6Message;
import com.jagornet.dhcp.message.DhcpV6RelayMessage;
import com.jagornet.dhcp.option.v6.DhcpV6RelayOption;
import com.jagornet.dhcp.util.DhcpConstants;

/**
 * Title: DhcpV6MessageHandler
 * Description: The main DHCPv6 message handler class.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6MessageHandler
{
	private static Logger log = LoggerFactory.getLogger(DhcpV6MessageHandler.class);
	
	/**
	 * -  The link to which the client is attached.  The server determines
	 * the link as follows:
	 * If the server receives the message directly from the client and
	 * the source address in the IP datagram in which the message was
	 * received is a link-local address, then the client is on the
	 * same link to which the interface over which the message was
	 * received is attached.
	 * If the server receives the message from a forwarding relay
	 * agent, then the client is on the same link as the one to which
	 * the interface, identified by the link-address field in the
	 * message from the relay agent, is attached.
	 * If the server receives the message directly from the client and
	 * the source address in the IP datagram in which the message was
	 * received is not a link-local address, then the client is on the
	 * link identified by the source address in the IP datagram (note
	 * that this situation can occur only if the server has enabled
	 * the use of unicast message delivery by the client and the
	 * client has sent a message for which unicast delivery is
	 * allowed).
	 * 
	 * @param localAddress the address on the local host which received the message
	 * @param dhcpMessage the Info-Request or Relay-Forward message to be handled
	 * 
	 * @return a Reply or Relay-Reply message
	 */
	//NOTE: this is the magic method where the nio and net implementations come together
    public static DhcpV6Message handleMessage(InetAddress localAddress, DhcpV6Message dhcpMessage)
    {
		DhcpV6Message replyMessage = null;
	    if (dhcpMessage instanceof DhcpV6RelayMessage) {
	    	if (dhcpMessage.getMessageType() == DhcpConstants.V6MESSAGE_TYPE_RELAY_FORW) {
		        DhcpV6RelayMessage relayMessage = (DhcpV6RelayMessage) dhcpMessage;
		        replyMessage = handleRelayForward(relayMessage);
		    }
		    else {
		        log.error("Unsupported message type: " + dhcpMessage.getMessageType());
		    }
		}
		else {
			log.info("Handling client request on local client link address: " +
					localAddress.getHostAddress());
			replyMessage = handleClientRequest(localAddress, dhcpMessage);
		}
		return replyMessage;
	}
    
    /**
     * Handle relay forward message.
     * 
     * @param relayMessage the Relay-Forward message
     * 
     * @return a Relay-Reply message
     */
    private static DhcpV6RelayMessage handleRelayForward(DhcpV6RelayMessage relayMessage)
    {
        InetAddress linkAddr = relayMessage.getLinkAddress();
		log.info("Handling relay forward on link address: " + linkAddr.getHostAddress());
        DhcpV6RelayOption relayOption = relayMessage.getRelayOption();
        if (relayOption != null) {
            DhcpV6Message relayOptionMessage = relayOption.getDhcpMessage();
            while (relayOptionMessage != null) {
                // check what kind of message is in the option
                if (relayOptionMessage instanceof DhcpV6RelayMessage) {
                    // encapsulated message is another relay message
                    DhcpV6RelayMessage anotherRelayMessage = 
                        (DhcpV6RelayMessage)relayOptionMessage;
                    // flip this inner relay_forward into a relay_reply,
                    // because we reuse the relay message "stack" for the reply
                    anotherRelayMessage.setMessageType(DhcpConstants.V6MESSAGE_TYPE_RELAY_REPL);
                    // reset the client link reference
                    linkAddr = anotherRelayMessage.getLinkAddress();
                    // reset the current relay option reference to the
                    // encapsulated relay message's relay option
                    relayOption = anotherRelayMessage.getRelayOption();
                    // reset the relayOptionMessage reference to recurse
                    relayOptionMessage = relayOption.getDhcpMessage();
                }
                else {
                    // we've peeled off all the layers of the relay message(s),
                    // so now go handle the client request
        			log.info("Handling client request on remote client link address: " +
        					linkAddr.getHostAddress());
                	DhcpV6Message replyMessage = handleClientRequest(linkAddr, relayOptionMessage);
                    if (replyMessage != null) {
                        // replace the original client request message inside
                        // the relayed message with the generated Reply message
                        relayOption.setDhcpMessage(replyMessage);
                        // flip the outer-most relay_foward into a relay_reply
                        relayMessage.setMessageType(DhcpConstants.V6MESSAGE_TYPE_RELAY_REPL);
                        // return the relay message we started with, 
                        // with each relay "layer" flipped from a relay_forward
                        // to a relay_reply, and the lowest level relayOption
                        // will contain our Reply for the client request
                        return relayMessage;
                    }
                    relayOptionMessage = null;  // done with relayed messages
                }
            }
        }
        else {
            log.error("Relay message does not contain a relay option");
        }
        // if we get here, no reply was generated
        return null;
    }
    
    /**
     * Handle client request.
     * 
     * @param linkAddress the link address
     * @param dhcpMessage the dhcp message
     * 
     * @return the dhcp message
     */
    public static DhcpV6Message handleClientRequest(InetAddress linkAddress, DhcpV6Message dhcpMessage)
    {
    	DhcpV6MessageProcessor processor = null;
    	switch (dhcpMessage.getMessageType()) {        
	        case DhcpConstants.V6MESSAGE_TYPE_SOLICIT:
	        	processor = new DhcpV6SolicitProcessor(dhcpMessage, linkAddress);
	        	break;
	        case DhcpConstants.V6MESSAGE_TYPE_REQUEST:
	        	processor = new DhcpV6RequestProcessor(dhcpMessage, linkAddress);
	        	break;
	        case DhcpConstants.V6MESSAGE_TYPE_CONFIRM:
	        	processor = new DhcpV6ConfirmProcessor(dhcpMessage, linkAddress);
	        	break;
	        case DhcpConstants.V6MESSAGE_TYPE_RENEW:
	        	processor = new DhcpV6RenewProcessor(dhcpMessage, linkAddress);
	        	break;
	        case DhcpConstants.V6MESSAGE_TYPE_REBIND:
	        	processor = new DhcpV6RebindProcessor(dhcpMessage, linkAddress);
	        	break;
	        case DhcpConstants.V6MESSAGE_TYPE_RELEASE:
	        	processor = new DhcpV6ReleaseProcessor(dhcpMessage, linkAddress);
	        	break;
	        case DhcpConstants.V6MESSAGE_TYPE_DECLINE:
	        	processor = new DhcpV6DeclineProcessor(dhcpMessage, linkAddress);
	        	break;
	        case DhcpConstants.V6MESSAGE_TYPE_INFO_REQUEST:
	        	processor = new DhcpV6InfoRequestProcessor(dhcpMessage, linkAddress);
	        	break;
	        default:
	            log.error("Unknown message type.");
	            break;
    	}
    	if (processor != null) {
    		return processor.processMessage();
    	}
    	else {
    		log.error("No processor found for message type: " + dhcpMessage.getMessageType());
    	}
    	return null;
    }
}
