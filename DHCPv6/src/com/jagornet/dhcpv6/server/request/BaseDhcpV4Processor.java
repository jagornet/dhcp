/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpInfoRequestProcessor.java is part of DHCPv6.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpV4Message;
import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.option.v4.DhcpV4LeaseTimeOption;
import com.jagornet.dhcpv6.option.v4.DhcpV4ServerIdOption;
import com.jagornet.dhcpv6.server.config.DhcpLink;
import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcpv6.server.request.binding.Binding;
import com.jagornet.dhcpv6.server.request.binding.BindingObject;
import com.jagornet.dhcpv6.server.request.binding.V4AddressBindingPool;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.xml.V4LeaseTimeOption;

/**
 * Title: BaseDhcpRequestProcessor
 * Description: The base class for processing client messages.
 * 
 * @author A. Gregory Rabil
 */

public abstract class BaseDhcpV4Processor implements DhcpV4MessageProcessor
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(BaseDhcpV4Processor.class);

    /** The dhcp server config. */
    protected static DhcpServerConfiguration dhcpServerConfig = 
                                        DhcpServerConfiguration.getInstance();
    
    // wrap the configured V4ServerId option in a DhcpOption for the wire
    /** The dhcp server id option. */
    protected static DhcpV4ServerIdOption dhcpV4ServerIdOption = 
    	new DhcpV4ServerIdOption(dhcpServerConfig.getDhcpV6ServerConfig().getV4ServerIdOption());
    
    /** The request message. */
    protected final DhcpV4Message requestMsg;
    
    /** The reply message. */
    protected DhcpV4Message replyMsg;

    /** The client link address. */
    protected final InetAddress clientLinkAddress;
    
    /** The configuration Link object for the client link. */
    protected DhcpLink clientLink;
    
    /** The list of Bindings for this request. */
    protected List<Binding> bindings = new ArrayList<Binding>();
    
    /** The recent msgs. */
    protected static Set<DhcpV4Message> recentMsgs = 
    	Collections.synchronizedSet(new HashSet<DhcpV4Message>());
    
    /** The recent msg pruner. */
    protected static Timer recentMsgPruner = new Timer("RecentMsgPruner");
    
    /**
     * Construct an BaseDhcpRequest processor.  Since this class is
     * abstract, this constructor is protected for implementing classes.
     * 
     * @param requestMsg the DhcpMessage received from the client
     * @param clientLinkAddress the client link address
     */
    protected BaseDhcpV4Processor(DhcpV4Message requestMsg, InetAddress clientLinkAddress)
    {
        this.requestMsg = requestMsg;
        this.clientLinkAddress = clientLinkAddress;
    }

    protected Map<Integer, DhcpOption> requestedOptions(Map<Integer, DhcpOption> optionMap,
    		DhcpV4Message requestMsg)
	{
    	if ((optionMap != null)  && !optionMap.isEmpty()) {
    		List<Integer> requestedCodes = requestMsg.getRequestedOptionCodes();
    		if ((requestedCodes != null) && !requestedCodes.isEmpty()) {
    			Map<Integer, DhcpOption> _optionMap = new HashMap<Integer, DhcpOption>();
    			for (Map.Entry<Integer, DhcpOption> option : optionMap.entrySet()) {
					if (requestedCodes.contains(option.getKey())) {
						_optionMap.put(option.getKey(), option.getValue());
					}
				}
    			optionMap = _optionMap;
    		}
    	}
    	return optionMap;
	}
	
    /**
     * Populate v4 options.
     * 
     * @param link the link
     * @param pool the pool
     */
    protected void populateV4Options(DhcpLink dhcpLink, V4AddressBindingPool bindingPool)
    {
    	Map<Integer, DhcpOption> optionMap = 
    		dhcpServerConfig.effectiveV4AddrOptions(requestMsg, dhcpLink, bindingPool);
    	if (DhcpServerPolicies.effectivePolicyAsBoolean(bindingPool.getV4AddressPool(),
    			dhcpLink.getLink(), Property.SEND_REQUESTED_OPTIONS_ONLY)) {
    		optionMap = requestedOptions(optionMap, requestMsg);
    	}
    	replyMsg.putAllDhcpOptions(optionMap);
    }    
    
    /**
     * Populate reply msg options.
     */
    protected void populateReplyMsgOptions()
    {
    	Map<Integer, DhcpOption> optionMap = dhcpServerConfig.effectiveV4AddrOptions(requestMsg);
    	if (DhcpServerPolicies.globalPolicyAsBoolean(Property.SEND_REQUESTED_OPTIONS_ONLY)) {
    		optionMap = requestedOptions(optionMap, requestMsg);
    	}
    	replyMsg.putAllDhcpOptions(optionMap);
    }
    
    /**
     * Populate reply msg options.
     * 
     * @param link the link
     */
    protected void populateReplyMsgOptions(DhcpLink dhcpLink)
    {
    	Map<Integer, DhcpOption> optionMap = 
    		dhcpServerConfig.effectiveV4AddrOptions(requestMsg, dhcpLink);
    	if (DhcpServerPolicies.effectivePolicyAsBoolean(dhcpLink.getLink(),
    			Property.SEND_REQUESTED_OPTIONS_ONLY)) {
    		optionMap = requestedOptions(optionMap, requestMsg);
    	}
    	replyMsg.putAllDhcpOptions(optionMap);
    }

    /**
     * Process the client request.  Find appropriate configuration based on any
     * criteria in the request message that can be matched against the server's
     * configuration, then formulate a response message containing the options
     * to be sent to the client.
     * 
     * @return a Reply DhcpMessage
     */
    public DhcpV4Message processMessage()
    {
    	try {
        	if (!preProcess()) {
        		log.warn("Message dropped by preProcess");
        		return null;
        	}
            
    		if (log.isDebugEnabled()) {
    			log.debug("Processing: " + requestMsg.toStringWithOptions());
    		}
    		else if (log.isInfoEnabled()) {
    	        log.info("Processing: " + requestMsg.toString());
    		}
	        
	        // build a reply message using the local and remote sockets from the request
	        replyMsg = new DhcpV4Message(requestMsg.getLocalAddress(), requestMsg.getRemoteAddress());
	        
	        replyMsg.setOp((short)DhcpConstants.OP_REPLY);
	        // copy fields from request to reply
	        replyMsg.setTransactionId(requestMsg.getTransactionId());
	        replyMsg.setFlags(requestMsg.getFlags());
	        replyMsg.setGiAddr(requestMsg.getGiAddr());
	        replyMsg.setChAddr(requestMsg.getChAddr());
	
	        // MUST put Server Identifier in REPLY message
	        replyMsg.putDhcpOption(dhcpV4ServerIdOption);
	
	        if (!process()) {
	        	log.warn("Message dropped by processor");
	        	return null;
	        }
	        
	        if (log.isDebugEnabled()) {
	        	log.debug("Returning: " + replyMsg.toStringWithOptions());
	        }
	        else if (log.isInfoEnabled()) {
	        	log.info("Returning: " + replyMsg.toString());
	        }
	        
    	}
    	finally {
	        if (!postProcess()) {
	    		log.warn("Message dropped by postProcess");
	        	replyMsg = null;
	        }
    	}
        
        return replyMsg;
    }

    /**
     * Pre process.
     * 
     * @return true if processing should continue
     */
    public boolean preProcess()
    {        
        // locate configuration for the client's link
//        log.info("Client link address: " + clientLinkAddress.getHostAddress());
//        clientLink = dhcpServerConfig.findLinkForAddress(clientLinkAddress);
        clientLink = dhcpServerConfig.findDhcpLink(requestMsg.getLocalAddress().getAddress(),
        		requestMsg.getRemoteAddress().getAddress());
        if (clientLink == null) {
//        	log.error("No Link configured for client link address: " + 
//        			clientLinkAddress.getHostAddress());
        	log.error("No Link configured for client request: " +
        			" localAddress=" + requestMsg.getLocalAddress().getAddress(),
        			" remoteAddress=" + requestMsg.getRemoteAddress().getAddress());
        	return false;	// must configure link for server to reply
        }

        synchronized (recentMsgs) {
    		boolean isNew = recentMsgs.add(requestMsg);
    		if (!isNew) {
    			if (log.isDebugEnabled())
    				log.debug("Dropping recent message");
    			return false;	// don't process
    		}
    	}
    	
		if (log.isDebugEnabled())
			log.debug("Processing new message");
		
		long timer = DhcpServerPolicies.effectivePolicyAsLong(clientLink.getLink(),
				Property.DHCP_PROCESSOR_RECENT_MESSAGE_TIMER);
		if (timer > 0) {
			recentMsgPruner.schedule(new RecentMsgTimerTask(requestMsg), timer);
		}
    	return true;	// ok to process
    }
    
    /**
     * Process.
     * 
     * @return true if a reply should be sent
     */
    public abstract boolean process();
    
    /**
     * Post process.
     * 
     * @return true if a reply should be sent
     */
    public boolean postProcess()
    {
		//TODO consider the implications of always removing the
		//     recently processed message b/c we could just keep
		//     getting blasted by an attempted DOS attack?
    	synchronized (recentMsgs) {
    		if (recentMsgs.remove(requestMsg)) {
    			if (log.isDebugEnabled())
    				log.debug("Removed recent message: " + requestMsg.toString());
    		}
    	}
    	return true;
    }

	/**
	 * Adds the v4 binding to reply.
	 * 
	 * @param clientLink the client link
	 * @param binding the binding
	 */
	protected void addBindingToReply(DhcpLink clientLink, Binding binding)
	{
		Collection<BindingObject> bindingObjs = binding.getBindingObjects();
		if ((bindingObjs != null) && !bindingObjs.isEmpty()) {
			if (bindingObjs.size() == 1) {
				BindingObject bindingObj = bindingObjs.iterator().next();
				InetAddress inetAddr = bindingObj.getIpAddress();
				if (inetAddr != null) {
					replyMsg.setYiAddr(inetAddr);
					// must be an V4AddresBindingPool for v4 binding
					V4AddressBindingPool bindingPool = 
						(V4AddressBindingPool) bindingObj.getBindingPool();
					if (bindingPool != null) {
						long preferred = bindingPool.getPreferredLifetime();
						DhcpV4LeaseTimeOption dhcpV4LeaseTimeOption = new DhcpV4LeaseTimeOption();
						V4LeaseTimeOption v4LeaseTimeOption = (V4LeaseTimeOption)
									dhcpV4LeaseTimeOption.getUnsignedIntOption();
						v4LeaseTimeOption.setUnsignedInt(preferred);
						replyMsg.putDhcpOption(dhcpV4LeaseTimeOption);
						populateV4Options(clientLink, bindingPool);
						//TODO when do actually start the timer?  currently, two get
						//     created - one during advertise, one during reply
						//     policy to allow real-time expiration?
	//					bp.startExpireTimerTask(bindingAddr, iaAddrOption.getValidLifetime());
					}
					else {
						log.error("Null binding pool in binding: " + binding.toString());
					}
				}
				else {
					log.error("Null address in binding: " + binding.toString());
				}
			}
			else {
				log.error("Expected only one bindingObject in v4 Binding, but found " +
						bindingObjs.size() + "bindingObjects");
			}
		}
		else {
			log.error("No V4 bindings in binding object!");
		}
	}
	
	protected boolean addrOnLink(InetAddress inetAddr, DhcpLink clientLink)
	{
		boolean onLink = true;	// assume all IPs are on link
		
		return onLink;
	}
	
    /**
     * The Class RecentMsgTimerTask.
     */
    class RecentMsgTimerTask extends TimerTask
    {
    	
	    /** The dhcp msg. */
	    private DhcpV4Message dhcpMsg;
    	
    	/**
	     * Instantiates a new recent msg timer task.
	     * 
	     * @param dhcpMsg the dhcp msg
	     */
	    public RecentMsgTimerTask(DhcpV4Message dhcpMsg)
    	{
    		this.dhcpMsg = dhcpMsg;
    	}
    	
    	/* (non-Javadoc)
	     * @see java.util.TimerTask#run()
	     */
	    @Override
    	public void run() {
    		synchronized (recentMsgs) {
    			if (recentMsgs.remove(dhcpMsg)) {
        			if (log.isDebugEnabled())
        				log.debug("Pruned recent message: " + dhcpMsg.toString());
    			}
    		}
    	}

    } 
}
