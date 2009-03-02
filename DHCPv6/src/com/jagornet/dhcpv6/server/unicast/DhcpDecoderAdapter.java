/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpDecoderAdapter.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.server.unicast;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;

/**
 * Title: DhcpDecoderAdapter 
 * Description: The protocol decoder used by the MINA-based unicast DHCP server
 * when receiving packets.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpDecoderAdapter extends ProtocolDecoderAdapter
{
    /** The log. */
    private static Logger log = LoggerFactory.getLogger(DhcpDecoderAdapter.class);

    /* (non-Javadoc)
     * @see org.apache.mina.filter.codec.ProtocolDecoder#decode(org.apache.mina.core.session.IoSession, org.apache.mina.core.buffer.IoBuffer, org.apache.mina.filter.codec.ProtocolDecoderOutput)
     */
    public void decode(IoSession session, IoBuffer iobuf, ProtocolDecoderOutput out)
            throws ProtocolDecoderException
    {
    	try {
    		InetSocketAddress local = (InetSocketAddress)session.getLocalAddress();
    		InetSocketAddress remote = (InetSocketAddress)session.getRemoteAddress(); 
    			
	        DhcpMessage dhcpMessage = DhcpMessage.decode(iobuf.buf(), local, remote);
	        
	        if (dhcpMessage != null) {
	            if (log.isDebugEnabled())
	                log.debug("Writing decoded message from: " +
	                		DhcpMessage.socketAddressAsString(remote));
	            out.write(dhcpMessage);
	        }
	        else {
	            String errmsg = "Null message returned from decoder";
	            log.error(errmsg);
	            throw new ProtocolDecoderException(errmsg);
	        }
    	}
    	catch (IOException ex) {
    		log.error("Failure decoding message: " + ex);
    		throw new ProtocolDecoderException(ex);
    	}
    }
}
