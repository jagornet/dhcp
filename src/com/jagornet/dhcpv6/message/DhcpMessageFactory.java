/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpMessageFactory.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.message;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.util.DhcpConstants;

/**
 * Title: DhcpMessageFactory
 * Description: A factory for creating DhcpMessage objects.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpMessageFactory
{	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpMessageFactory.class);
	
	/**
	 * The type of DhcpMessageFactory. 
	 */
	public enum Type { STATELESS_SERVER,
					   STATEFUL_SERVER,
					   STATELESS_CLIENT };
	
	/** The INSTANCE. */
	private static DhcpMessageFactory INSTANCE = null;
	
	/** The type. */
	private static Type type = Type.STATELESS_SERVER;	// default
	
	/**
	 * Instantiates a new dhcp message factory.
	 */
	private DhcpMessageFactory() { }
	
	/**
	 * Sets the type.
	 * 
	 * @param type the new type
	 */
	public static void setType(Type type)
	{
		DhcpMessageFactory.type = type;
	}

	/**
	 * Gets the single instance of DhcpMessageFactory.
	 * 
	 * @return single instance of DhcpMessageFactory
	 */
	public static DhcpMessageFactory getInstance()
	{
		if (INSTANCE == null) {
			INSTANCE = new DhcpMessageFactory();
		}
		return INSTANCE;
	}
	
    /**
     * Create a new DhcpMessage.
     * 
     * @param msgtype the msgtype
     * @param localAddr the local addr
     * @param remoteAddr the remote addr
     * 
     * @return the dhcp message
     */
    public DhcpMessage getDhcpMessage(short msgtype, 
    								 InetSocketAddress localAddr, 
									 InetSocketAddress remoteAddr) 
    {
    	DhcpMessage msg = null;
    	if (type.equals(Type.STATELESS_SERVER)) {
    		msg = getStatelessServerMessage(msgtype, localAddr, remoteAddr);
    	}
    	else if (type.equals(Type.STATEFUL_SERVER)) {
    		msg = getStatefulServerMessage(msgtype, localAddr, remoteAddr);
    	}
    	else if (type.equals(Type.STATELESS_CLIENT)) {
    		msg = getStatelessClientMessage(msgtype, localAddr, remoteAddr);
    	}
    	else {
    		log.error("Unknown DhcpMessageFactoryType: " + type);
    	}
    	return msg;
    }
    
    /**
     * Create a stateless server message.
     * 
     * @param msgtype the msgtype
     * @param localAddr the local addr
     * @param remoteAddr the remote addr
     * 
     * @return the stateless server message
     */
    private DhcpMessage getStatelessServerMessage(short msgtype,
    											InetSocketAddress localAddr,
    											InetSocketAddress remoteAddr)
    {
        DhcpMessage dhcpMessage = null;
        switch (msgtype) {

        	// this is the only message we can receive as a stateless server
            case DhcpConstants.INFO_REQUEST:
                dhcpMessage = new DhcpMessage(localAddr, remoteAddr);
                break;
            
            // a stateless server can also receive relay-forward messages
            // which themselves contain info-request messages
            case DhcpConstants.RELAY_FORW:
                dhcpMessage = new DhcpRelayMessage(localAddr, remoteAddr);
                break;

            // no support for stateful client messages
            case DhcpConstants.CONFIRM:
            case DhcpConstants.DECLINE:
            case DhcpConstants.REBIND:
            case DhcpConstants.RELEASE:
            case DhcpConstants.RENEW:
            case DhcpConstants.REQUEST:
            case DhcpConstants.SOLICIT:
                log.warn("Unsupported message type.");
                break;
                
            case DhcpConstants.ADVERTISE:
            case DhcpConstants.RECONFIGURE:
            case DhcpConstants.REPLY:
                log.warn("Server message received.");
                break;

            case DhcpConstants.RELAY_REPL:
                log.warn("Relay-reply message received.");
                break;
                
            default:
                log.error("Unknown message type.");
                break;
        }
        return dhcpMessage;
    }

    
    /**
     * Create a stateful server message.
     * 
     * @param msgtype the msgtype
     * @param localAddr the local addr
     * @param remoteAddr the remote addr
     * 
     * @return the stateful server message
     */
    private DhcpMessage getStatefulServerMessage(short msgtype,
    											InetSocketAddress localAddr,
    											InetSocketAddress remoteAddr)
    {
        DhcpMessage dhcpMessage = null;
        switch (msgtype) {
            
        	// these are the messages that we can receive as a stateful server
	        case DhcpConstants.CONFIRM:
	        case DhcpConstants.DECLINE:
	        case DhcpConstants.REBIND:
	        case DhcpConstants.RELEASE:
	        case DhcpConstants.RENEW:
	        case DhcpConstants.REQUEST:
	        case DhcpConstants.SOLICIT:
            case DhcpConstants.INFO_REQUEST:
                dhcpMessage = new DhcpMessage(localAddr, remoteAddr);
                break;
                
            // a stateful server can also receive relay-forward messages
            case DhcpConstants.RELAY_FORW:
                dhcpMessage = new DhcpRelayMessage(localAddr, remoteAddr);
                break;

            case DhcpConstants.ADVERTISE:
            case DhcpConstants.RECONFIGURE:
            case DhcpConstants.REPLY:
                log.warn("Server message received.");
                break;

            case DhcpConstants.RELAY_REPL:
                log.warn("Relay-reply message received.");
                break;

            default:
                log.error("Unknown message type.");
                break;
        }
        return dhcpMessage;
    }
    
    /**
     * Create a stateless client message.
     * 
     * @param msgtype the msgtype
     * @param localAddr the local addr
     * @param remoteAddr the remote addr
     * 
     * @return the stateless client message
     */
    private DhcpMessage getStatelessClientMessage(short msgtype,
    											InetSocketAddress localAddr,
    											InetSocketAddress remoteAddr)
    {
        DhcpMessage dhcpMessage = null;
        switch (msgtype) {

        	// these are the only messages we can receive as a client
	        case DhcpConstants.ADVERTISE:
	        case DhcpConstants.RECONFIGURE:
	        case DhcpConstants.REPLY:
	            dhcpMessage = new DhcpMessage(localAddr, remoteAddr);
	            break;
            
	        case DhcpConstants.CONFIRM:
	        case DhcpConstants.DECLINE:
	        case DhcpConstants.REBIND:
	        case DhcpConstants.RELEASE:
	        case DhcpConstants.RENEW:
	        case DhcpConstants.REQUEST:
	        case DhcpConstants.SOLICIT:
            case DhcpConstants.INFO_REQUEST:
                log.warn("Client message received.");
                break;

            case DhcpConstants.RELAY_FORW:
            case DhcpConstants.RELAY_REPL:
                log.warn("Relay message received.");
                break;
                
            default:
                log.error("Unknown message type.");
                break;
        }
        return dhcpMessage;
    }
}
