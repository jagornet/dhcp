/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV4InformProcessor.java is part of Jagornet DHCP.
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

import com.jagornet.dhcp.message.DhcpV4Message;
import com.jagornet.dhcp.util.DhcpConstants;

/**
 * Title: DhcpV4InformRequestProcessor
 * Description: The main class for processing V4 INFORM messages.
 * 
 * @author A. Gregory Rabil
 */

public class DhcpV4InformProcessor extends BaseDhcpV4Processor
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpV4InformProcessor.class);
    
    /**
     * Construct an DhcpV4InformRequest processor.
     * 
     * @param requestMsg the Inform message
     * @param clientLinkAddress the client link address
     */
    public DhcpV4InformProcessor(DhcpV4Message requestMsg, InetAddress clientLinkAddress)
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
    	
    	if (requestMsg.getCiAddr() == null) {
    		log.warn("Ignoring Inform message: " +
    				"ciAddr is null");
    		return false;
    	}
    	
    	if (requestMsg.getCiAddr().equals(DhcpConstants.ZEROADDR_V4)) {
    		log.warn("Ignoring Inform message: " +
					"ciAddr is zero");
    	}
        
    	return true;
    }

    /**
     * Process the client request.  Find appropriate configuration based on any
     * criteria in the request message that can be matched against the server's
     * configuration, then formulate a response message containing the options
     * to be sent to the client.
     * 
     * @return true if a reply should be sent, false otherwise
     */
    @Override
    public boolean process()
    {
//    	   When the server receives an Information-request message, the client
//    	   is requesting configuration information that does not include the
//    	   assignment of any addresses.  The server determines all configuration
//    	   parameters appropriate to the client, based on the server
//    	   configuration policies known to the server.

    	replyMsg.setCiAddr(requestMsg.getCiAddr());		// copy the ciAddr from client
        replyMsg.setMessageType((short)DhcpConstants.V4MESSAGE_TYPE_ACK);
    	populateV4Reply(clientLink, null);

    	return true;
    }
}
