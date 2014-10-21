/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV4MessageHandler.java is part of Jagornet DHCP.
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
import com.jagornet.dhcp.option.v4.DhcpV4MsgTypeOption;
import com.jagornet.dhcp.util.DhcpConstants;

/**
 * Title: DhcpV4MessageHandler
 * Description: The main DHCPv4 message handler class.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4MessageHandler
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpV4MessageHandler.class);
	
    public static DhcpV4Message handleMessage(InetAddress localAddress, DhcpV4Message dhcpMessage)
    {
		DhcpV4Message replyMessage = null;
    	if (dhcpMessage.getOp() == DhcpConstants.V4_OP_REQUEST) {
    		InetAddress linkAddress = null;
    		if (dhcpMessage.getGiAddr().equals(DhcpConstants.ZEROADDR_V4)) {
    			linkAddress = localAddress;
				log.info("Handling client request on local client link address: " +
						linkAddress.getHostAddress());
    		}
    		else {
    			linkAddress = dhcpMessage.getGiAddr();
				log.info("Handling client request on remote client link address: " +
						linkAddress.getHostAddress());
    		}
    		DhcpV4MsgTypeOption msgTypeOption = (DhcpV4MsgTypeOption) 
    				dhcpMessage.getDhcpOption(DhcpConstants.V4OPTION_MESSAGE_TYPE);
    		if (msgTypeOption != null) {
    			short msgType = msgTypeOption.getUnsignedByte();
    	    	DhcpV4MessageProcessor processor = null;
	    		switch (msgType) {
	    			case DhcpConstants.V4MESSAGE_TYPE_DISCOVER:
	    				processor = new DhcpV4DiscoverProcessor(dhcpMessage, linkAddress);
	    				break;
	    			case DhcpConstants.V4MESSAGE_TYPE_REQUEST:
	    				processor = new DhcpV4RequestProcessor(dhcpMessage, linkAddress);
	    				break;
	    			case DhcpConstants.V4MESSAGE_TYPE_DECLINE:
	    				processor = new DhcpV4DeclineProcessor(dhcpMessage, linkAddress);
	    				break;
	    			case DhcpConstants.V4MESSAGE_TYPE_RELEASE:
	    				processor = new DhcpV4ReleaseProcessor(dhcpMessage, linkAddress);
	    				break;
	    			case DhcpConstants.V4MESSAGE_TYPE_INFORM:
	    				processor = new DhcpV4InformProcessor(dhcpMessage, linkAddress);
	    				break;
	    	        default:
	    	            log.error("Unknown message type.");
	    	            break;
	        	}
	        	if (processor != null) {
	        		return processor.processMessage();
	        	}
	        	else {
	        		log.error("No processor found for message type: " + msgType);
	        	}
    		}
    		else {
    			log.error("No message type option found in request.");
    		}
        	return null;
	    }
	    else {
	        log.error("Unsupported op code: " + dhcpMessage.getOp());
	    }
		return replyMessage;
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
