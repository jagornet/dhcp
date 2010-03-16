/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpMessageHandler.java is part of DHCPv6.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.message.DhcpRelayMessage;
import com.jagornet.dhcpv6.option.DhcpRelayOption;
import com.jagornet.dhcpv6.util.DhcpConstants;

/**
 * Title: DhcpMessageHandler
 * Description: The main DHCP message handler class.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpMessageHandler
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpMessageHandler.class);
	
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
    public static DhcpMessage handleMessage(InetAddress localAddress, DhcpMessage dhcpMessage)
    {
		DhcpMessage replyMessage = null;
	    if (dhcpMessage instanceof DhcpRelayMessage) {
	    	if (dhcpMessage.getMessageType() == DhcpConstants.RELAY_FORW) {
		        DhcpRelayMessage relayMessage = (DhcpRelayMessage) dhcpMessage;
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
    private static DhcpRelayMessage handleRelayForward(DhcpRelayMessage relayMessage)
    {
    	/**
    	 * TODO: Verify that because we re-use the relay_forward message
    	 * 		 for our relay_reply, then we will end up including any
    	 * 		 Interface-ID option that was contained therein, as
    	 * 		 required by RFC 3315 Section 22.18.
    	 */
        InetAddress linkAddr = relayMessage.getLinkAddress();
		log.info("Handling relay forward on link address: " + linkAddr.getHostAddress());
        DhcpRelayOption relayOption = relayMessage.getRelayOption();
        if (relayOption != null) {
            DhcpMessage relayOptionMessage = relayOption.getDhcpMessage();
            while (relayOptionMessage != null) {
                // check what kind of message is in the option
                if (relayOptionMessage instanceof DhcpRelayMessage) {
                    // encapsulated message is another relay message
                    DhcpRelayMessage anotherRelayMessage = 
                        (DhcpRelayMessage)relayOptionMessage;
                    // flip this inner relay_forward into a relay_reply,
                    // because we reuse the relay message "stack" for the reply
                    anotherRelayMessage.setMessageType(DhcpConstants.RELAY_REPL);
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
                	DhcpMessage replyMessage = handleClientRequest(linkAddr, relayOptionMessage);
                    if (replyMessage != null) {
                        // replace the original client request message inside
                        // the relayed message with the generated Reply message
                        relayOption.setDhcpMessage(replyMessage);
                        // flip the outer-most relay_foward into a relay_reply
                        relayMessage.setMessageType(DhcpConstants.RELAY_REPL);
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
    public static DhcpMessage handleClientRequest(InetAddress linkAddress, DhcpMessage dhcpMessage)
    {
    	DhcpMessageProcessor processor = null;
    	switch (dhcpMessage.getMessageType()) {        
	        case DhcpConstants.SOLICIT:
	        	processor = new DhcpSolicitProcessor(dhcpMessage, linkAddress);
	        	break;
	        case DhcpConstants.REQUEST:
	        	processor = new DhcpRequestProcessor(dhcpMessage, linkAddress);
	        	break;
	        case DhcpConstants.CONFIRM:
	        	processor = new DhcpConfirmProcessor(dhcpMessage, linkAddress);
	        	break;
	        case DhcpConstants.RENEW:
	        	processor = new DhcpRenewProcessor(dhcpMessage, linkAddress);
	        	break;
	        case DhcpConstants.REBIND:
	        	processor = new DhcpRebindProcessor(dhcpMessage, linkAddress);
	        	break;
	        case DhcpConstants.RELEASE:
	        	processor = new DhcpReleaseProcessor(dhcpMessage, linkAddress);
	        	break;
	        case DhcpConstants.DECLINE:
	        	processor = new DhcpDeclineProcessor(dhcpMessage, linkAddress);
	        	break;
	        case DhcpConstants.INFO_REQUEST:
	        	processor = new DhcpInfoRequestProcessor(dhcpMessage, linkAddress);
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

    
    
//	private static String INFO_REQUEST_PROCESSOR_IMPL_DEFAULT =
//		"com.jagornet.dhcpv6.server.request.DhcpInfoRequestProcessor";
//	private static String SOLICIT_PROCESSOR_IMPL_DEFAULT =
//		"com.jagornet.dhcpv6.server.request.DhcpSolicitProcessor";
//	private static String REQUEST_PROCESSOR_IMPL_DEFAULT =
//		"com.jagornet.dhcpv6.server.request.DhcpRequestProcessor";
//	protected static String infoRequestProcessorImplClass = 
//		INFO_REQUEST_PROCESSOR_IMPL_DEFAULT;
//	protected static String solicitProcessorImplClass = 
//		SOLICIT_PROCESSOR_IMPL_DEFAULT;
//	protected static String requestProcessorImplClass = 
//		REQUEST_PROCESSOR_IMPL_DEFAULT;
//	    
//    
//    private static DhcpMessage handleInfoRequest(InetAddress linkAddress, DhcpMessage dhcpMessage)
//    {
//    	DhcpMessageProcessor messageProcessor = null;
//		try {
//			Class<?> c = Class.forName(infoRequestProcessorImplClass);
//			messageProcessor = 
//				(DhcpMessageProcessor) c
//				.getConstructor(DhcpMessage.class, InetAddress.class)
//				.newInstance(dhcpMessage, linkAddress);
//		}
//		catch (Exception e) {
//			log.error("Failed to create Info-Request Processor: " + e);
//		}
//		
//		if (messageProcessor != null) {
//			return messageProcessor.process();
//		}
//		
//		return null;
//    }
//    
//    private static DhcpMessage handleSolicit(InetAddress linkAddress, DhcpMessage dhcpMessage)
//    {
//    	DhcpMessageProcessor messageProcessor = null;
//		try {
//			Class<?> c = Class.forName(solicitProcessorImplClass);
//			messageProcessor = 
//				(DhcpMessageProcessor) c
//				.getConstructor(DhcpMessage.class, InetAddress.class)
//				.newInstance(dhcpMessage, linkAddress);
//		}
//		catch (Exception e) {
//			log.error("Failed to create Solicit Processor: " + e);
//		}
//		
//		if (messageProcessor != null) {
//			return messageProcessor.process();
//		}
//		
//		return null;
//    }
//    
//    private static DhcpMessage handleRequest(InetAddress linkAddress, DhcpMessage dhcpMessage)
//    {
//    	DhcpMessageProcessor messageProcessor = null;
//		try {
//			Class<?> c = Class.forName(requestProcessorImplClass);
//			messageProcessor = 
//				(DhcpMessageProcessor) c
//				.getConstructor(DhcpMessage.class, InetAddress.class)
//				.newInstance(dhcpMessage, linkAddress);
//		}
//		catch (Exception e) {
//			log.error("Failed to create Request Processor: " + e);
//		}
//		
//		if (messageProcessor != null) {
//			return messageProcessor.process();
//		}
//		
//		return null;
//    }
}
