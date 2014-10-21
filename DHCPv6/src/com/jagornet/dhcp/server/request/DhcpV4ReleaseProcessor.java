/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV4ReleaseProcessor.java is part of Jagornet DHCP.
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
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.message.DhcpV4Message;
import com.jagornet.dhcp.server.request.binding.Binding;
import com.jagornet.dhcp.server.request.binding.BindingObject;
import com.jagornet.dhcp.server.request.binding.V4AddrBindingManager;
import com.jagornet.dhcp.server.request.binding.V4BindingAddress;
import com.jagornet.dhcp.util.DhcpConstants;
import com.jagornet.dhcp.util.Util;

/**
 * Title: DhcpV4ReleaseProcessor
 * Description: The main class for processing V4 RELEASE messages.
 * 
 * @author A. Gregory Rabil
 */

public class DhcpV4ReleaseProcessor extends BaseDhcpV4Processor
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpV4ReleaseProcessor.class);
    
    /**
     * Construct an DhcpV4ReleaseProcessor processor.
     * 
     * @param requestMsg the Release message
     * @param clientLinkAddress the client link address
     */
    public DhcpV4ReleaseProcessor(DhcpV4Message requestMsg, InetAddress clientLinkAddress)
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
    		log.warn("Ignoring Release message: " +
    				"ciAddr is null");
    		return false;
    	}
    	
    	if (requestMsg.getCiAddr().equals(DhcpConstants.ZEROADDR_V4)) {
    		log.warn("Ignoring Release message: " +
					"ciAddr is zero");
    	}
        
    	return true;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.server.request.BaseDhcpProcessor#process()
     */
    @Override
    public boolean process()
    {
		byte chAddr[] = requestMsg.getChAddr();
		
		V4AddrBindingManager bindingMgr = dhcpServerConfig.getV4AddrBindingMgr();
		if (bindingMgr != null) {
			log.info("Processing Release" +
					 " from chAddr=" + Util.toHexString(chAddr) +
					 " ciAddr=" + requestMsg.getCiAddr().getHostAddress());
			Binding binding = bindingMgr.findCurrentBinding(clientLink, 
															chAddr, requestMsg);
			if (binding != null) {
				Collection<BindingObject> bindingObjs = binding.getBindingObjects();
				if ((bindingObjs != null) && !bindingObjs.isEmpty()) {
					V4BindingAddress bindingAddr = (V4BindingAddress) bindingObjs.iterator().next();
					bindingMgr.releaseIaAddress(binding, bindingAddr);
				}
				else {
					log.error("No binding addresses in binding for client: " + 
							Util.toHexString(chAddr));
				}
			}
			else {
				log.error("No Binding available for client: " + 
						Util.toHexString(chAddr));
			}
		}
		else {
			log.error("Unable to process V4 Release:" +
					" No V4AddrBindingManager available");
		}
		
	    return false;	// no reply for v4 release
    }
}
