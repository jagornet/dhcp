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
	 * @return a Replay or Relay-Reply message
	 */
    public static DhcpMessage handleMessage(InetAddress localAddress,
    								 		DhcpMessage dhcpMessage)
    {
		DhcpMessage replyMessage = null;
		if (dhcpMessage.getMessageType() == DhcpConstants.INFO_REQUEST) {
		    replyMessage = handleInfoRequest(localAddress, dhcpMessage);
		}
		else if (dhcpMessage.getMessageType() == DhcpConstants.RELAY_FORW) {
		    if (dhcpMessage instanceof DhcpRelayMessage) {
		        DhcpRelayMessage relayMessage = (DhcpRelayMessage) dhcpMessage;
		        replyMessage = handleRelayForward(relayMessage);
		    }
		    else {
		        // Note: in theory, we can't get here, because the
		        // codec would have thrown an exception beforehand
		        log.error("Received unknown relay message object: " + 
		                  dhcpMessage.getClass());
		    }
		}
		else {
		    log.warn("Ignoring unsupported message type: " + 
		             DhcpConstants.getMessageString(dhcpMessage.getMessageType()));
		}
		return replyMessage;
	}
    
    /**
     * Handle client Info-Request message.
     * 
     * @param linkAddr an address on the client's link
     * @param dhcpMessage the Info-Request message
     * 
     * @return a Reply message
     */
    private static DhcpMessage handleInfoRequest(InetAddress linkAddr, 
                                          		 DhcpMessage dhcpMessage)
    {
        DhcpInfoRequestProcessor processor = 
            new DhcpInfoRequestProcessor(linkAddr, dhcpMessage);
        
        DhcpMessage reply = processor.process();
        return reply;
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
        DhcpRelayOption relayOption = relayMessage.getRelayOption();
        if (relayOption != null) {
            DhcpMessage relayOptionMessage = relayOption.getRelayMessage();
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
                    relayOptionMessage = relayOption.getRelayMessage();
                }
                else {
                    // we've peeled off all the layers of the relay message(s),
                    // so now go handle the Info-Request, assuming it is one
                    if (relayOptionMessage.getMessageType() == DhcpConstants.INFO_REQUEST) {
                        DhcpMessage replyMessage = 
                            handleInfoRequest(linkAddr, relayOptionMessage);
                        if (replyMessage != null) {
                            // replace the original Info-Request message inside
                            // the relayed message with the generated Reply message
                            relayOption.setRelayMessage(replyMessage);
                            // flip the outer-most relay_foward into a relay_reply
                            relayMessage.setMessageType(DhcpConstants.RELAY_REPL);
                            // return the relay message we started with, 
                            // with each relay "layer" flipped from a relay_forward
                            // to a relay_reply, and the lowest level relayOption
                            // will contain our Reply for the Info-Request
                            return relayMessage;
                        }
                    }
                    else {
                        log.error("Lowest level message in relay message is not an Info-Request");
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
}
