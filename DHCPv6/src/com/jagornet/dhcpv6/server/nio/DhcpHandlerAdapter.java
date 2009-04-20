/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpHandlerAdapter.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.server.nio;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.server.request.DhcpMessageHandler;

/**
 * Title: DhcpHandlerAdapter 
 * Description: The handler used by the MINA-based unicast DHCP server
 * for handling DhcpMessages.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpHandlerAdapter extends IoHandlerAdapter
{
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpHandlerAdapter.class);

    /**
     * Instantiates a new dhcp handler adapter.
     */
    public DhcpHandlerAdapter()
    {
    	// empty constructor
    }

    /* (non-Javadoc)
     * @see org.apache.mina.core.service.IoHandlerAdapter#exceptionCaught(org.apache.mina.core.session.IoSession, java.lang.Throwable)
     */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause)
            throws Exception 
    {
        log.error("Session exception caught", cause);
        session.close(true);
    }

    /* (non-Javadoc)
     * @see org.apache.mina.core.service.IoHandlerAdapter#messageReceived(org.apache.mina.core.session.IoSession, java.lang.Object)
     */
    @Override
    public void messageReceived(IoSession session, Object message)
            throws Exception 
    {
        SocketAddress remoteAddress = session.getRemoteAddress();
        if (message instanceof DhcpMessage) {
            
            DhcpMessage dhcpMessage = (DhcpMessage) message;
            log.info("Received: " + dhcpMessage.toStringWithOptions());
            
            InetAddress localAddr = ((InetSocketAddress)session.getLocalAddress()).getAddress(); 
            DhcpMessage replyMessage = 
            	DhcpMessageHandler.handleMessage(localAddr,
            				  dhcpMessage);
            
            if (replyMessage != null) {
                // do we really want to write to the remoteAddress
                // from the session - i.e. is this really just going
                // to send the reply message to the client or relay
                // that sent or forwarded the request?
                session.write(replyMessage, remoteAddress);
            }
            else {
                log.warn("Null DHCP reply message returned from processor");
            }
            
        }
        else {
            // Note: in theory, we can't get here, because the
            // codec would have thrown an exception beforehand
            log.error("Received unknown message object: " + message.getClass());
        }
    }
    
}
