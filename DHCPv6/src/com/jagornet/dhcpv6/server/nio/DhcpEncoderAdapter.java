/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpEncoderAdapter.java is part of DHCPv6.
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

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderException;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.util.Util;

/**
 * Title: DhcpEncoderAdapter 
 * Description: The protocol encoder used by the MINA-based unicast DHCP server
 * when sending packets.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpEncoderAdapter extends ProtocolEncoderAdapter
{
    /** The log. */
    private static Logger log = LoggerFactory.getLogger(DhcpEncoderAdapter.class);
    
    /* (non-Javadoc)
     * @see org.apache.mina.filter.codec.ProtocolEncoder#encode(org.apache.mina.core.session.IoSession, java.lang.Object, org.apache.mina.filter.codec.ProtocolEncoderOutput)
     */
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out)
            throws ProtocolEncoderException
    {
    	try {
	        if (message instanceof DhcpMessage) {
	            DhcpMessage dhcpMessage = (DhcpMessage) message;
	            ByteBuffer buf = dhcpMessage.encode();
	            if (log.isDebugEnabled())
	                log.debug("Writing " + buf.limit() + " bytes for: " + 
	                		Util.socketAddressAsString(dhcpMessage.getRemoteAddress()));
	            out.write(IoBuffer.wrap(buf));
	        }
	        else {
	            String errmsg = "Unknown message object class: " + message.getClass();
	            log.error(errmsg);
	            throw new ProtocolEncoderException(errmsg);
	        }
    	}
    	catch (IOException ex) {
    		log.error("Failure encoding message: " + ex);
    		throw new ProtocolEncoderException(ex);
    	}
    }
}
