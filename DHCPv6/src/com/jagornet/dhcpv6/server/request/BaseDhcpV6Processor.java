/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseDhcpProcessor.java is part of DHCPv6.
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

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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

import com.jagornet.dhcp.xml.V6AddressPool;
import com.jagornet.dhcp.xml.V6PrefixPool;
import com.jagornet.dhcpv6.message.DhcpV6Message;
import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6ClientFqdnOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6IaAddrOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6IaNaOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6IaPdOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6IaPrefixOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6IaTaOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6ServerIdOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6StatusCodeOption;
import com.jagornet.dhcpv6.server.config.DhcpConfigObject;
import com.jagornet.dhcpv6.server.config.DhcpLink;
import com.jagornet.dhcpv6.server.config.DhcpV6OptionConfigObject;
import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcpv6.server.request.binding.Binding;
import com.jagornet.dhcpv6.server.request.binding.V6BindingAddress;
import com.jagornet.dhcpv6.server.request.binding.BindingObject;
import com.jagornet.dhcpv6.server.request.binding.V6BindingPrefix;
import com.jagornet.dhcpv6.server.request.ddns.DdnsCallback;
import com.jagornet.dhcpv6.server.request.ddns.DdnsUpdater;
import com.jagornet.dhcpv6.server.request.ddns.DhcpV6DdnsComplete;
import com.jagornet.dhcpv6.util.DhcpConstants;

/**
 * Title: BaseDhcpV6Processor
 * Description: The base class for processing DHCPv6 client messages.
 * 
 * @author A. Gregory Rabil
 */

public abstract class BaseDhcpV6Processor implements DhcpV6MessageProcessor
{
	private static Logger log = LoggerFactory.getLogger(BaseDhcpV6Processor.class);

    protected static DhcpServerConfiguration dhcpServerConfig = 
                                        DhcpServerConfiguration.getInstance();
    
    // wrap the configured ServerId option in a DhcpOption for the wire
    protected static DhcpV6ServerIdOption dhcpServerIdOption = 
    	new DhcpV6ServerIdOption(dhcpServerConfig.getDhcpServerConfig().getV6ServerIdOption());
    
    protected final DhcpV6Message requestMsg;
    protected DhcpV6Message replyMsg;
    protected final InetAddress clientLinkAddress;
    protected DhcpLink clientLink;
    protected List<Binding> bindings = new ArrayList<Binding>();
    protected static Set<DhcpV6Message> recentMsgs = 
    	Collections.synchronizedSet(new HashSet<DhcpV6Message>());
    protected static Timer recentMsgPruner = new Timer("RecentMsgPruner");
    
    /**
     * Construct an BaseDhcpRequest processor.  Since this class is
     * abstract, this constructor is protected for implementing classes.
     * 
     * @param requestMsg the DhcpMessage received from the client
     * @param clientLinkAddress the client link address
     */
    protected BaseDhcpV6Processor(DhcpV6Message requestMsg, InetAddress clientLinkAddress)
    {
        this.requestMsg = requestMsg;
        this.clientLinkAddress = clientLinkAddress;
    }

    protected Map<Integer, DhcpOption> requestedOptions(Map<Integer, DhcpOption> optionMap,
    		DhcpV6Message requestMsg)
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
     * Populate reply msg options.
     */
    protected void populateReplyMsgOptions()
    {
    	Map<Integer, DhcpOption> optionMap = dhcpServerConfig.effectiveMsgOptions(requestMsg);
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
    		dhcpServerConfig.effectiveMsgOptions(requestMsg, dhcpLink);
    	if (DhcpServerPolicies.effectivePolicyAsBoolean(dhcpLink.getLink(),
    			Property.SEND_REQUESTED_OPTIONS_ONLY)) {
    		optionMap = requestedOptions(optionMap, requestMsg);
    	}
    	replyMsg.putAllDhcpOptions(optionMap);
    }
    
    /**
     * Populate ia na options.
     * 
     * @param iaNaOption the ia na option
     */
    protected void populateIaNaOptions(DhcpV6IaNaOption iaNaOption)
    {
    	Map<Integer, DhcpOption> optionMap = dhcpServerConfig.effectiveIaNaOptions(requestMsg);
    	if (DhcpServerPolicies.globalPolicyAsBoolean(Property.SEND_REQUESTED_OPTIONS_ONLY)) {
    		optionMap = requestedOptions(optionMap, requestMsg);
    	}
    	iaNaOption.putAllDhcpOptions(optionMap);
    }
    
    /**
     * Populate ia ta options.
     * 
     * @param iaTaOption the ia ta option
     */
    protected void populateIaTaOptions(DhcpV6IaTaOption iaTaOption)
    {
    	Map<Integer, DhcpOption> optionMap = dhcpServerConfig.effectiveIaTaOptions(requestMsg);
    	if (DhcpServerPolicies.globalPolicyAsBoolean(Property.SEND_REQUESTED_OPTIONS_ONLY)) {
    		optionMap = requestedOptions(optionMap, requestMsg);
    	}
    	iaTaOption.putAllDhcpOptions(optionMap);
    }
    
    /**
     * Populate ia pd options.
     * 
     * @param iaPdOption the ia pd option
     */
    protected void populateIaPdOptions(DhcpV6IaPdOption iaPdOption)
    {
    	Map<Integer, DhcpOption> optionMap = dhcpServerConfig.effectiveIaPdOptions(requestMsg);
    	if (DhcpServerPolicies.globalPolicyAsBoolean(Property.SEND_REQUESTED_OPTIONS_ONLY)) {
    		optionMap = requestedOptions(optionMap, requestMsg);
    	}
    	iaPdOption.putAllDhcpOptions(optionMap);
    }

    /**
     * Populate ia na options.
     * 
     * @param iaNaOption the ia na option
     * @param link the link
     */
    protected void populateIaNaOptions(DhcpV6IaNaOption iaNaOption, DhcpLink dhcpLink)
    {
    	Map<Integer, DhcpOption> optionMap = 
    		dhcpServerConfig.effectiveIaNaOptions(requestMsg, dhcpLink);
    	if (DhcpServerPolicies.effectivePolicyAsBoolean(dhcpLink.getLink(),
    			Property.SEND_REQUESTED_OPTIONS_ONLY)) {
    		optionMap = requestedOptions(optionMap, requestMsg);
    	}
    	iaNaOption.putAllDhcpOptions(optionMap);
    }
    
    /**
     * Populate ia ta options.
     * 
     * @param iaTaOption the ia ta option
     * @param link the link
     */
    protected void populateIaTaOptions(DhcpV6IaTaOption iaTaOption, DhcpLink dhcpLink)
    {
    	Map<Integer, DhcpOption> optionMap = 
    		dhcpServerConfig.effectiveIaTaOptions(requestMsg, dhcpLink);
    	if (DhcpServerPolicies.effectivePolicyAsBoolean(dhcpLink.getLink(),
    			Property.SEND_REQUESTED_OPTIONS_ONLY)) {
    		optionMap = requestedOptions(optionMap, requestMsg);
    	}
    	iaTaOption.putAllDhcpOptions(optionMap);
    }
    
    /**
     * Populate ia pd options.
     * 
     * @param iaPdOption the ia pd option
     * @param link the link
     */
    protected void populateIaPdOptions(DhcpV6IaPdOption iaPdOption, DhcpLink dhcpLink)
    {
    	Map<Integer, DhcpOption> optionMap = 
    		dhcpServerConfig.effectiveIaPdOptions(requestMsg, dhcpLink);
    	if (DhcpServerPolicies.effectivePolicyAsBoolean(dhcpLink.getLink(),
    			Property.SEND_REQUESTED_OPTIONS_ONLY)) {
    		optionMap = requestedOptions(optionMap, requestMsg);
    	}
    	iaPdOption.putAllDhcpOptions(optionMap);
    }
    
    /**
     * Populate na addr options.
     * 
     * @param iaAddrOption the ia addr option
     */
    protected void populateNaAddrOptions(DhcpV6IaAddrOption iaAddrOption)
    {
    	Map<Integer, DhcpOption> optionMap = dhcpServerConfig.effectiveNaAddrOptions(requestMsg);
    	if (DhcpServerPolicies.globalPolicyAsBoolean(Property.SEND_REQUESTED_OPTIONS_ONLY)) {
    		optionMap = requestedOptions(optionMap, requestMsg);
    	}
    	iaAddrOption.putAllDhcpOptions(optionMap);
    }
    
    /**
     * Populate ta addr options.
     * 
     * @param iaAddrOption the ia addr option
     */
    protected void populateTaAddrOptions(DhcpV6IaAddrOption iaAddrOption)
    {
    	Map<Integer, DhcpOption> optionMap = dhcpServerConfig.effectiveTaAddrOptions(requestMsg);
    	if (DhcpServerPolicies.globalPolicyAsBoolean(Property.SEND_REQUESTED_OPTIONS_ONLY)) {
    		optionMap = requestedOptions(optionMap, requestMsg);
    	}
    	iaAddrOption.putAllDhcpOptions(optionMap);
    }
    
    /**
     * Populate prefix options.
     * 
     * @param iaPrefixOption the ia prefix option
     */
    protected void populatePrefixOptions(DhcpV6IaPrefixOption iaPrefixOption)
    {
    	Map<Integer, DhcpOption> optionMap = dhcpServerConfig.effectivePrefixOptions(requestMsg);
    	if (DhcpServerPolicies.globalPolicyAsBoolean(Property.SEND_REQUESTED_OPTIONS_ONLY)) {
    		optionMap = requestedOptions(optionMap, requestMsg);
    	}
    	iaPrefixOption.putAllDhcpOptions(optionMap);
    }
    
    /**
     * Populate na addr options.
     * 
     * @param iaAddrOption the ia addr option
     * @param link the link
     */
    protected void populateNaAddrOptions(DhcpV6IaAddrOption iaAddrOption, DhcpLink dhcpLink)
    {
    	Map<Integer, DhcpOption> optionMap = 
    		dhcpServerConfig.effectiveNaAddrOptions(requestMsg, dhcpLink);
    	if (DhcpServerPolicies.effectivePolicyAsBoolean(dhcpLink.getLink(),
    			Property.SEND_REQUESTED_OPTIONS_ONLY)) {
    		optionMap = requestedOptions(optionMap, requestMsg);
    	}
    	iaAddrOption.putAllDhcpOptions(optionMap);
    }
    
    /**
     * Populate ta addr options.
     * 
     * @param iaAddrOption the ia addr option
     * @param link the link
     */
    protected void populateTaAddrOptions(DhcpV6IaAddrOption iaAddrOption, DhcpLink dhcpLink)
    {
    	Map<Integer, DhcpOption> optionMap = 
    		dhcpServerConfig.effectiveTaAddrOptions(requestMsg, dhcpLink);
    	if (DhcpServerPolicies.effectivePolicyAsBoolean(dhcpLink.getLink(),
    			Property.SEND_REQUESTED_OPTIONS_ONLY)) {
    		optionMap = requestedOptions(optionMap, requestMsg);
    	}
    	iaAddrOption.putAllDhcpOptions(optionMap);
    }
    
    /**
     * Populate prefix options.
     * 
     * @param iaPrefixOption the ia prefix option
     * @param link the link
     */
    protected void populatePrefixOptions(DhcpV6IaPrefixOption iaPrefixOption, DhcpLink dhcpLink)
    {
    	Map<Integer, DhcpOption> optionMap = 
    		dhcpServerConfig.effectivePrefixOptions(requestMsg, dhcpLink);
    	if (DhcpServerPolicies.effectivePolicyAsBoolean(dhcpLink.getLink(),
    			Property.SEND_REQUESTED_OPTIONS_ONLY)) {
    		optionMap = requestedOptions(optionMap, requestMsg);
    	}
    	iaPrefixOption.putAllDhcpOptions(optionMap);
    }
	
    /**
     * Populate na addr options.
     * 
     * @param iaAddrOption the ia addr option
     * @param link the link
     * @param pool the pool
     */
    protected void populateNaAddrOptions(DhcpV6IaAddrOption iaAddrOption, DhcpLink dhcpLink, 
    		DhcpV6OptionConfigObject configObj)
    {
    	Map<Integer, DhcpOption> optionMap = 
    		dhcpServerConfig.effectiveNaAddrOptions(requestMsg, dhcpLink, configObj);
    	if (DhcpServerPolicies.effectivePolicyAsBoolean(configObj,
    			dhcpLink.getLink(), Property.SEND_REQUESTED_OPTIONS_ONLY)) {
    		optionMap = requestedOptions(optionMap, requestMsg);
    	}
    	iaAddrOption.putAllDhcpOptions(optionMap);
    }    
    
    /**
     * Populate ta addr options.
     * 
     * @param iaAddrOption the ia addr option
     * @param link the link
     * @param pool the pool
     */
    protected void populateTaAddrOptions(DhcpV6IaAddrOption iaAddrOption, DhcpLink dhcpLink, 
    		DhcpV6OptionConfigObject configObj)
    {
    	Map<Integer, DhcpOption> optionMap = 
    		dhcpServerConfig.effectiveTaAddrOptions(requestMsg, dhcpLink, configObj);
    	if (DhcpServerPolicies.effectivePolicyAsBoolean(configObj,
    			dhcpLink.getLink(), Property.SEND_REQUESTED_OPTIONS_ONLY)) {
    		optionMap = requestedOptions(optionMap, requestMsg);
    	}
    	iaAddrOption.putAllDhcpOptions(optionMap);
    }    
    
    /**
     * Populate prefix options.
     * 
     * @param iaPrefixOption the ia prefix option
     * @param link the link
     * @param pool the pool
     */
    protected void populatePrefixOptions(DhcpV6IaPrefixOption iaPrefixOption, DhcpLink dhcpLink, 
    		DhcpV6OptionConfigObject configObj)
    {
    	Map<Integer, DhcpOption> optionMap = 
    		dhcpServerConfig.effectivePrefixOptions(requestMsg, dhcpLink, configObj);
    	if (DhcpServerPolicies.effectivePolicyAsBoolean(configObj,
    			dhcpLink.getLink(), Property.SEND_REQUESTED_OPTIONS_ONLY)) {
    		optionMap = requestedOptions(optionMap, requestMsg);
    	}
	    iaPrefixOption.putAllDhcpOptions(optionMap);
    }    

    /**
     * Process the client request.  Find appropriate configuration based on any
     * criteria in the request message that can be matched against the server's
     * configuration, then formulate a response message containing the options
     * to be sent to the client.
     * 
     * @return a Reply DhcpMessage
     */
    public DhcpV6Message processMessage()
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
	        replyMsg = new DhcpV6Message(requestMsg.getLocalAddress(), requestMsg.getRemoteAddress());
	        // copy the transaction ID into the reply
	        replyMsg.setTransactionId(requestMsg.getTransactionId());
	
	        // MUST put Server Identifier DUID in ADVERTISE or REPLY message
	        replyMsg.putDhcpOption(dhcpServerIdOption);
	        
	        // copy Client Identifier DUID if given in client request message
	        replyMsg.putDhcpOption(requestMsg.getDhcpClientIdOption());
	
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
    	InetSocketAddress localSocketAddr = requestMsg.getLocalAddress();
    	InetSocketAddress remoteSocketAddr = requestMsg.getRemoteAddress();
    	
        clientLink = dhcpServerConfig.findDhcpLink(
        		(Inet6Address)localSocketAddr.getAddress(),
        		(Inet6Address)remoteSocketAddr.getAddress());
        if (clientLink == null) {
        	log.error("No Link configured for DHCPv6 client request: " +
        			" localAddress=" + localSocketAddr.getAddress().getHostAddress() +
        			" remoteAddress=" + remoteSocketAddr.getAddress().getHostAddress());
        	return false;	// must configure link for server to reply
        }

/* TODO: check if this DOS mitigation is useful
 * 
		boolean isNew = recentMsgs.add(requestMsg);
		if (!isNew) {
			if (log.isDebugEnabled())
				log.debug("Dropping recent message");
			return false;	// don't process
		}

		if (log.isDebugEnabled())
			log.debug("Processing new message");
		
		long timer = DhcpServerPolicies.effectivePolicyAsLong(clientLink.getLink(),
				Property.DHCP_PROCESSOR_RECENT_MESSAGE_TIMER);
		if (timer > 0) {
			recentMsgPruner.schedule(new RecentMsgTimerTask(requestMsg), timer);
		}
*/    	
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
// Exactly!?... the comment above says it all
//    		if (recentMsgs.remove(requestMsg)) {
//    			if (log.isDebugEnabled())
//    				log.debug("Removed recent message: " + requestMsg.toString());
//    		}
    	return true;
    }
	
    /**
     * Should multicast.
     * 
     * @return true, if successful
     */
    protected boolean shouldMulticast()
    {
    	if (requestMsg.isUnicast()) {
        	Map<Integer, DhcpOption> effectiveMsgOptions = 
    	      	dhcpServerConfig.effectiveMsgOptions(requestMsg, clientLink);
        	if ((effectiveMsgOptions == null) ||
        			!effectiveMsgOptions.containsKey(DhcpConstants.OPTION_UNICAST)) {
            	// if the server has not explicitly told the client to unicast,
        		// then tell the client that it should send multicast packets
    			return true;
    		}
    	}
    	return false;
    }
    
	/**
	 * Sets the reply status.
	 * 
	 * @param statusCode the new reply status
	 */
	protected void setReplyStatus(int statusCode)
	{
		DhcpV6StatusCodeOption statusOption = new DhcpV6StatusCodeOption();
		statusOption.setStatusCode(statusCode);
		replyMsg.putDhcpOption(statusOption);
	}
	
	/**
	 * Adds the ia na option status to reply.
	 * 
	 * @param iaNaOption the ia na option
	 * @param statusCode the status code
	 */
	protected void addIaNaOptionStatusToReply(DhcpV6IaNaOption iaNaOption, int statusCode)
	{
		DhcpV6IaNaOption replyIaNaOption = new DhcpV6IaNaOption();
		replyIaNaOption.setIaId(iaNaOption.getIaId());
		DhcpV6StatusCodeOption status = new DhcpV6StatusCodeOption();
		status.setStatusCode(statusCode);
		replyIaNaOption.putDhcpOption(status);
		replyMsg.addIaNaOption(replyIaNaOption);
	}
	
	/**
	 * Adds the ia ta option status to reply.
	 * 
	 * @param iaTaOption the ia ta option
	 * @param statusCode the status code
	 */
	protected void addIaTaOptionStatusToReply(DhcpV6IaTaOption iaTaOption, int statusCode)
	{
		DhcpV6IaTaOption replyIaTaOption = new DhcpV6IaTaOption();
		replyIaTaOption.setIaId(iaTaOption.getIaId());
		DhcpV6StatusCodeOption status = new DhcpV6StatusCodeOption();
		status.setStatusCode(statusCode);
		replyIaTaOption.putDhcpOption(status);
		replyMsg.addIaTaOption(replyIaTaOption);
	}
	
	/**
	 * Adds the ia pd option status to reply.
	 * 
	 * @param iaPdOption the ia pd option
	 * @param statusCode the status code
	 */
	protected void addIaPdOptionStatusToReply(DhcpV6IaPdOption iaPdOption, int statusCode)
	{
		DhcpV6IaPdOption replyIaPdOption = new DhcpV6IaPdOption();
		replyIaPdOption.setIaId(iaPdOption.getIaId());
		DhcpV6StatusCodeOption status = new DhcpV6StatusCodeOption();
		status.setStatusCode(statusCode);
		replyIaPdOption.putDhcpOption(status);
		replyMsg.addIaPdOption(replyIaPdOption);
	}
	
	/**
	 * Adds the binding to reply.
	 * 
	 * @param clientLink the client link
	 * @param binding the binding
	 */
	protected void addBindingToReply(DhcpLink clientLink, Binding binding)
	{
		if (binding.getIatype() == Binding.NA_TYPE) {
			addNaBindingToReply(clientLink, binding);
		}
		else if (binding.getIatype() == Binding.TA_TYPE) {
			addTaBindingToReply(clientLink, binding);
		}
		else if (binding.getIatype() == Binding.PD_TYPE) {
			addPdBindingToReply(clientLink, binding);
		}
	}
	
	/**
	 * Adds the na binding to reply.
	 * 
	 * @param clientLink the client link
	 * @param binding the binding
	 */
	protected void addNaBindingToReply(DhcpLink clientLink, Binding binding)
	{
		DhcpV6IaNaOption dhcpIaNaOption = new DhcpV6IaNaOption();
		dhcpIaNaOption.setIaId(binding.getIaid());

		long minPreferredLifetime = 0;
		Collection<BindingObject> bindingObjs = binding.getBindingObjects();
		if ((bindingObjs != null) && !bindingObjs.isEmpty()) {
			minPreferredLifetime = 0xffffffff;
			List<DhcpV6IaAddrOption> dhcpIaAddrOptions = new ArrayList<DhcpV6IaAddrOption>(); 
			for (BindingObject bindingObj : bindingObjs) {
				DhcpV6IaAddrOption dhcpIaAddrOption = new DhcpV6IaAddrOption();
				InetAddress inetAddr = bindingObj.getIpAddress();
				if (inetAddr != null) {
					dhcpIaAddrOption.setIpAddress(inetAddr.getHostAddress());
					// must be an DhcpOptionConfigObject for IA_NA binding
					DhcpV6OptionConfigObject configObj = 
						(DhcpV6OptionConfigObject) bindingObj.getConfigObj();
					if (configObj != null) {
						long preferred = configObj.getPreferredLifetime();
						if ((minPreferredLifetime == 0xffffffff) ||
								(preferred < minPreferredLifetime))  {
							minPreferredLifetime = preferred; 
						}
						dhcpIaAddrOption.setPreferredLifetime(preferred);
						dhcpIaAddrOption.setValidLifetime(configObj.getValidLifetime());
						populateNaAddrOptions(dhcpIaAddrOption, clientLink, configObj);
						dhcpIaAddrOptions.add(dhcpIaAddrOption);
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
			dhcpIaNaOption.setIaAddrOptions(dhcpIaAddrOptions);
		}
		else {
			log.error("No IA_NA bindings in binding object!");
		}
		
		setIaNaT1(clientLink, dhcpIaNaOption, minPreferredLifetime);
		setIaNaT2(clientLink, dhcpIaNaOption, minPreferredLifetime);
		
		populateIaNaOptions(dhcpIaNaOption, clientLink);
		replyMsg.addIaNaOption(dhcpIaNaOption);
	}
	
	/**
	 * Adds the ta binding to reply.
	 * 
	 * @param clientLink the client link
	 * @param binding the binding
	 */
	protected void addTaBindingToReply(DhcpLink clientLink, Binding binding)
	{
		DhcpV6IaTaOption dhcpIaTaOption = new DhcpV6IaTaOption();
		dhcpIaTaOption.setIaId(binding.getIaid());
		
		Collection<BindingObject> bindingObjs = binding.getBindingObjects();
		if ((bindingObjs != null) && !bindingObjs.isEmpty()) {
			List<DhcpV6IaAddrOption> dhcpIaAddrOptions = new ArrayList<DhcpV6IaAddrOption>(); 
			for (BindingObject bindingObj : bindingObjs) {
				DhcpV6IaAddrOption dhcpIaAddrOption = new DhcpV6IaAddrOption();
				InetAddress inetAddr = bindingObj.getIpAddress();
				if (inetAddr != null) {
					dhcpIaAddrOption.setIpAddress(inetAddr.getHostAddress());
					// must be an DhcpOptionConfigObject for IA_TA binding
					DhcpV6OptionConfigObject configObj = 
						(DhcpV6OptionConfigObject) bindingObj.getConfigObj();
					if (configObj != null) {
						dhcpIaAddrOption.setPreferredLifetime(configObj.getPreferredLifetime());
						dhcpIaAddrOption.setValidLifetime(configObj.getValidLifetime());
						populateTaAddrOptions(dhcpIaAddrOption, clientLink, configObj);
						dhcpIaAddrOptions.add(dhcpIaAddrOption);
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
			dhcpIaTaOption.setIaAddrOptions(dhcpIaAddrOptions);
		}
		else {
			log.error("No IA_TA bindings in binding object!");
		}
		
		populateIaTaOptions(dhcpIaTaOption, clientLink);
		replyMsg.addIaTaOption(dhcpIaTaOption);
	}
	
	/**
	 * Adds the pd binding to reply.
	 * 
	 * @param clientLink the client link
	 * @param binding the binding
	 */
	protected void addPdBindingToReply(DhcpLink clientLink, Binding binding)
	{
		DhcpV6IaPdOption dhcpIaPdOption = new DhcpV6IaPdOption();
		dhcpIaPdOption.setIaId(binding.getIaid());
		
		long minPreferredLifetime = 0;
		Collection<BindingObject> bindingObjs = binding.getBindingObjects();
		if ((bindingObjs != null) && !bindingObjs.isEmpty()) {
			minPreferredLifetime = 0xffffffff;
			List<DhcpV6IaPrefixOption> dhcpIaPrefixOptions = new ArrayList<DhcpV6IaPrefixOption>(); 
			for (BindingObject bindingObj : bindingObjs) {
				// must be a Binding Prefix for IaPd binding
				V6BindingPrefix bindingPrefix = (V6BindingPrefix) bindingObj;
				DhcpV6IaPrefixOption dhcpIaPrefixOption = new DhcpV6IaPrefixOption();
				InetAddress inetAddr = bindingObj.getIpAddress();
				if (inetAddr != null) {
					dhcpIaPrefixOption.setIpAddress(inetAddr.getHostAddress());
					dhcpIaPrefixOption.setPrefixLength(bindingPrefix.getPrefixLength());
					// must be an DhcpOptionConfigObject for IA_PD binding
					DhcpV6OptionConfigObject configObj = 
						(DhcpV6OptionConfigObject) bindingObj.getConfigObj();
					if (configObj != null) {
						long preferred = configObj.getPreferredLifetime();
						if ((minPreferredLifetime == 0xffffffff) ||
								(preferred < minPreferredLifetime))  {
							minPreferredLifetime = preferred; 
						}
						dhcpIaPrefixOption.setPreferredLifetime(preferred);
						dhcpIaPrefixOption.setValidLifetime(configObj.getValidLifetime());
						populatePrefixOptions(dhcpIaPrefixOption, clientLink, configObj);
						dhcpIaPrefixOptions.add(dhcpIaPrefixOption);
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
			dhcpIaPdOption.setIaPrefixOptions(dhcpIaPrefixOptions);
		}
		else {
			log.error("No IA_PD bindings in binding object!");
		}
		
		setIaPdT1(clientLink, dhcpIaPdOption, minPreferredLifetime);
		setIaPdT2(clientLink, dhcpIaPdOption, minPreferredLifetime);
		
		populateIaPdOptions(dhcpIaPdOption, clientLink);
		replyMsg.addIaPdOption(dhcpIaPdOption);
	}
	
/*
	   The server selects the T1 and T2 times to allow the client to extend
	   the lifetimes of any addresses in the IA_NA before the lifetimes
	   expire, even if the server is unavailable for some short period of
	   time.  Recommended values for T1 and T2 are .5 and .8 times the
	   shortest preferred lifetime of the addresses in the IA that the
	   server is willing to extend, respectively.  If the "shortest"
	   preferred lifetime is 0xffffffff ("infinity"), the recommended T1 and
	   T2 values are also 0xffffffff.  If the time at which the addresses in
	   an IA_NA are to be renewed is to be left to the discretion of the
	   client, the server sets T1 and T2 to 0.
*/
	/**
	 * Sets the ia na t1.
	 * 
	 * @param clientLink the client link
	 * @param iaNaOption the ia na option
	 * @param minPreferredLifetime the min preferred lifetime
	 */
	private void setIaNaT1(DhcpLink clientLink, DhcpV6IaNaOption iaNaOption,
			long minPreferredLifetime)
	{
		float t1 = DhcpServerPolicies.effectivePolicyAsFloat(clientLink.getLink(), Property.IA_NA_T1);
		if (t1 > 1) {
			log.debug("Setting IA_NA T1 to configured number of seconds: " + t1);
			// if T1 is greater than one, then treat it as an
			// absolute value which specifies the number of seconds
			iaNaOption.setT1((long)t1);
		}
		else {
			// if T1 is less than one and greater than or equal to zero,
			// then treat is as a percentage of the minimum preferred lifetime
			// unless the minimum preferred lifetime is infinity (0xffffffff)
			if (minPreferredLifetime == 0xffffffff) {
				log.debug("Setting IA_NA T1 to minPreferredLifetime of infinity: " +
						minPreferredLifetime);
				iaNaOption.setT1(minPreferredLifetime);
			} 
			else {
				 if (t1 >= 0) {	// zero means let the client decide
					 log.debug("Setting IA_NA T1 to configured ratio=" + t1 +
							 " of minPreferredLifetime=" + minPreferredLifetime);
					 iaNaOption.setT1((long)(t1 * minPreferredLifetime));
				 }
				 else {
					 log.debug("Setting IA_NA T1 to standard ratio=0.5" + 
							 " of minPreferredLifetime=" + minPreferredLifetime);
					 iaNaOption.setT1((long)(0.5f * minPreferredLifetime));
				 }
			}
		}
	}

	/**
	 * Sets the ia na t2.
	 * 
	 * @param clientLink the client link
	 * @param iaNaOption the ia na option
	 * @param minPreferredLifetime the min preferred lifetime
	 */
	private void setIaNaT2(DhcpLink clientLink, DhcpV6IaNaOption iaNaOption,
			long minPreferredLifetime)
	{
		float t2 = DhcpServerPolicies.effectivePolicyAsFloat(clientLink.getLink(), Property.IA_NA_T2);
		if (t2 > 1) {
			log.debug("Setting IA_NA T2 to configured number of seconds: " + t2);
			iaNaOption.setT2((long)t2);
		}
		else {
			// if T2 is less than one and greater than or equal to zero,
			// then treat is as a percentage of the minimum preferred lifetime
			// unless the minimum preferred lifetime is infinity (0xffffffff)
			if (minPreferredLifetime == 0xffffffff) {
				log.debug("Setting IA_NA T2 to minPreferredLifetime of infinity: " +
						minPreferredLifetime);
				iaNaOption.setT2(minPreferredLifetime);
			} 
			else {
				 if (t2 >= 0) {	// zero means let the client decide
					 log.debug("Setting IA_NA T2 to configured ratio=" + t2 +
							 " of minPreferredLifetime=" + minPreferredLifetime);
					 iaNaOption.setT2((long)(t2 * minPreferredLifetime));
				 }
				 else {
					 log.debug("Setting IA_NA T2 to standard ratio=0.8" + 
							 " of minPreferredLifetime=" + minPreferredLifetime);
					 iaNaOption.setT2((long)(0.8f * minPreferredLifetime));
				 }
			}
		}
		// ensure that T2 >= T1
		if (iaNaOption.getT2() < iaNaOption.getT1()) {
			log.warn("IA_NA T2(" + iaNaOption.getT2() + ")" +
					" < IA_NA T1(" + iaNaOption.getT1() + "): setting T2=T1");
			iaNaOption.setT2(iaNaOption.getT1());
		}
	}

	/**
	 * Sets the ia pd t1.
	 * 
	 * @param clientLink the client link
	 * @param iaPdOption the ia na option
	 * @param minPreferredLifetime the min preferred lifetime
	 */
	private void setIaPdT1(DhcpLink clientLink, DhcpV6IaPdOption iaPdOption,
			long minPreferredLifetime)
	{
		float t1 = DhcpServerPolicies.effectivePolicyAsFloat(clientLink.getLink(), Property.IA_PD_T1);
		if (t1 > 1) {
			log.debug("Setting IA_PD T1 to configured number of seconds: " + t1);
			// if T1 is greater than one, then treat it as an
			// absolute value which specifies the number of seconds
			iaPdOption.setT1((long)t1);
		}
		else {
			// if T1 is less than one and greater than or equal to zero,
			// then treat is as a percentage of the minimum preferred lifetime
			// unless the minimum preferred lifetime is infinity (0xffffffff)
			if (minPreferredLifetime == 0xffffffff) {
				log.debug("Setting IA_PD T1 to minPreferredLifetime of infinity: " +
						minPreferredLifetime);
				iaPdOption.setT1(minPreferredLifetime);
			} 
			else {
				 if (t1 >= 0) {	// zero means let the client decide
					 log.debug("Setting IA_PD T1 to configured ratio=" + t1 +
							 " of minPreferredLifetime=" + minPreferredLifetime);
					 iaPdOption.setT1((long)(t1 * minPreferredLifetime));
				 }
				 else {
					 log.debug("Setting IA_PD T1 to standard ratio=0.5" + 
							 " of minPreferredLifetime=" + minPreferredLifetime);
					 iaPdOption.setT1((long)(0.5f * minPreferredLifetime));
				 }
			}
		}
	}

	/**
	 * Sets the ia pd t2.
	 * 
	 * @param clientLink the client link
	 * @param iaPdOption the ia pd option
	 * @param minPreferredLifetime the min preferred lifetime
	 */
	private void setIaPdT2(DhcpLink clientLink, DhcpV6IaPdOption iaPdOption,
			long minPreferredLifetime)
	{
		float t2 = DhcpServerPolicies.effectivePolicyAsFloat(clientLink.getLink(), Property.IA_PD_T2);
		if (t2 > 1) {
			log.debug("Setting IA_PD T2 to configured number of seconds: " + t2);
			iaPdOption.setT2((long)t2);
		}
		else {
			// if T2 is less than one and greater than or equal to zero,
			// then treat is as a percentage of the minimum preferred lifetime
			// unless the minimum preferred lifetime is infinity (0xffffffff)
			if (minPreferredLifetime == 0xffffffff) {
				log.debug("Setting IA_PD T2 to minPreferredLifetime of infinity: " +
						minPreferredLifetime);
				iaPdOption.setT2(minPreferredLifetime);
			} 
			else {
				 if (t2 >= 0) {	// zero means let the client decide
					 log.debug("Setting IA_PD T2 to configured ratio=" + t2 +
							 " of minPreferredLifetime=" + minPreferredLifetime);
					 iaPdOption.setT2((long)(t2 * minPreferredLifetime));
				 }
				 else {
					 log.debug("Setting IA_PD T2 to standard ratio=0.8" + 
							 " of minPreferredLifetime=" + minPreferredLifetime);
					 iaPdOption.setT2((long)(0.8f * minPreferredLifetime));
				 }
			}
		}
		// ensure that T2 >= T1
		if (iaPdOption.getT2() < iaPdOption.getT1()) {
			log.warn(" IA_PD T2(" + iaPdOption.getT2() + ")" +
					" <  IA_PD T1(" + iaPdOption.getT1() + "): setting T2=T1");
			iaPdOption.setT2(iaPdOption.getT1());
		}
	}
	
	/**
	 * Process ddns updates.
	 */
	protected void processDdnsUpdates(boolean sendUpdates)
	{
		DhcpV6ClientFqdnOption clientFqdnOption = 
			(DhcpV6ClientFqdnOption) requestMsg.getDhcpOption(DhcpConstants.OPTION_CLIENT_FQDN);
		if (clientFqdnOption == null) {
			//TODO allow name generation?
			log.debug("No Client FQDN option in request.  Skipping DDNS update processing.");
			return;
		}
		
		boolean includeFqdnOptionInReply = false;
		if ((requestMsg.getRequestedOptionCodes() != null) &&
				requestMsg.getRequestedOptionCodes().contains(DhcpConstants.OPTION_CLIENT_FQDN)) {
			// RFC 4704 section 6 says:
			//   Servers MUST only include a Client FQDN option in ADVERTISE and REPLY
			//   messages if the client included a Client FQDN option and the Client
			//   FQDN option is requested by the Option Request option in the client's
			//   message to which the server is responding.
			includeFqdnOptionInReply = true;
		}

		DhcpV6ClientFqdnOption replyFqdnOption = new DhcpV6ClientFqdnOption();
		replyFqdnOption.setDomainName(clientFqdnOption.getDomainName());
		replyFqdnOption.setUpdateAaaaBit(false);
		replyFqdnOption.setOverrideBit(false);
		replyFqdnOption.setNoUpdateBit(false);
		
		String fqdn = clientFqdnOption.getDomainName();
		if ((fqdn == null) || (fqdn.length() <= 0)) {
			log.error("Client FQDN option domain name is null/empty.  No DDNS udpates performed.");
			if (includeFqdnOptionInReply) {
				replyFqdnOption.setNoUpdateBit(true);	// tell client that server did no updates
				replyMsg.putDhcpOption(replyFqdnOption);
			}
			return;
		}
		
		String policy = DhcpServerPolicies.effectivePolicy(requestMsg,
				clientLink.getLink(), Property.DDNS_UPDATE);
		log.info("Server configuration for ddns.update policy: " + policy);
		if ((policy == null) || policy.equalsIgnoreCase("none")) {
			log.info("Server configuration for ddns.update policy is null or 'none'." +
					"  No DDNS updates performed.");
			if (includeFqdnOptionInReply) {
				replyFqdnOption.setNoUpdateBit(true);	// tell client that server did no updates
				replyMsg.putDhcpOption(replyFqdnOption);
			}
			return;
		}
				
		if (clientFqdnOption.getNoUpdateBit() && policy.equalsIgnoreCase("honorNoUpdate")) {
			log.info("Client FQDN NoUpdate flag set.  Server configured to honor request." +
					"  No DDNS updates performed.");
			if (includeFqdnOptionInReply) {
				replyFqdnOption.setNoUpdateBit(true);	// tell client that server did no updates
				replyMsg.putDhcpOption(replyFqdnOption);
			}
			//TODO: RFC 4704 Section 6.1
			//		...the server SHOULD delete any RRs that it previously added 
			//		via DNS updates for the client.
			return;
		}

		boolean doForwardUpdate = true;
		if (!clientFqdnOption.getUpdateAaaaBit() && policy.equalsIgnoreCase("honorNoAAAA")) {
			log.info("Client FQDN NoAAAA flag set.  Server configured to honor request." +
					"  No FORWARD DDNS updates performed.");
			doForwardUpdate = false;
		}
		else {
			replyFqdnOption.setUpdateAaaaBit(true);	// server will do update
			if (!clientFqdnOption.getUpdateAaaaBit())
				replyFqdnOption.setOverrideBit(true);	// tell client that we overrode request flag
		}
		
		String domain = DhcpServerPolicies.effectivePolicy(clientLink.getLink(), Property.DDNS_DOMAIN); 
		if ((domain != null) && !domain.isEmpty()) {
			log.info("Server configuration for domain policy: " + domain);
			// if there is a configured domain, then replace the domain provide by the client
			int dot = fqdn.indexOf('.');
			if (dot > 0) {
				fqdn = fqdn.substring(0, dot+1) + domain;
			}
			else {
				fqdn = fqdn + "." + domain;
			}
			replyFqdnOption.setDomainName(fqdn);
		}
		
		if (includeFqdnOptionInReply) {
			replyMsg.putDhcpOption(replyFqdnOption);
		}
		
		if (sendUpdates) {
			for (Binding binding : bindings) {
				if (binding.getState() == Binding.COMMITTED) {
					Collection<BindingObject> bindingObjs = binding.getBindingObjects();
					if (bindingObjs != null) {
						for (BindingObject bindingObj : bindingObjs) {
							
							V6BindingAddress bindingAddr = (V6BindingAddress) bindingObj;
							
		        			DhcpConfigObject configObj = bindingAddr.getConfigObj();
		        			
		        			DdnsCallback ddnsComplete = 
		        				new DhcpV6DdnsComplete(bindingAddr, replyFqdnOption);
		        			
							DdnsUpdater ddns =
								new DdnsUpdater(requestMsg, clientLink.getLink(), configObj,
										bindingAddr.getIpAddress(), fqdn, 
										requestMsg.getDhcpClientIdOption().getDuid(),
										configObj.getValidLifetime(), doForwardUpdate, false,
										ddnsComplete);
							
							ddns.processUpdates();
						}
					}
				}
			}
		}
	}

	/**
	 * Check if all the IPs in the IAADDR options of the given IA_NA option
	 * are on the client's link based on the configured prefix/prefixlen (subnet).
	 * NOTE:  This method has a **SIDE EFFECT** of setting the preferred and valid
	 * lifetimes of any off-link IAADDRs to zero(0).
	 * 
	 * @param dhcpIaNaOption the dhcp ia na option
	 * @param clientLink the client link
	 * 
	 * @return true if all the IADDRs are on-link, false otherwise
	 */
	protected boolean allIaAddrsOnLink(DhcpV6IaNaOption dhcpIaNaOption, DhcpLink clientLink)
	{
		boolean onLink = true;	// assume all IPs are on link
		if (dhcpIaNaOption != null) {
			List<DhcpV6IaAddrOption> iaAddrOpts = dhcpIaNaOption.getIaAddrOptions();
			if (iaAddrOpts != null) {
				for (DhcpV6IaAddrOption iaAddrOpt : iaAddrOpts) {
					if (clientLink.getSubnet().getSubnetAddress().isLinkLocalAddress()) {
						// if the Link address is link-local, then check if the
						// address is within one of the pools configured for this
						// local Link, which automatically makes this server
						// "authoritative" (in ISC parlance) for this local net
						V6AddressPool p = DhcpServerConfiguration.findNaAddrPool(clientLink.getLink(),
									iaAddrOpt.getInetAddress());
						if (p == null) {
							log.info("No local address pool found for requested IA_NA: " + 
									iaAddrOpt.getInetAddress().getHostAddress() +
									" - considered to be off link");
							iaAddrOpt.setPreferredLifetime(0);
							iaAddrOpt.setValidLifetime(0);
							onLink = false;
						}
					}
					else {
						// it the Link address is remote, then check 
						// if the address is valid for that link
						if (!clientLink.getSubnet().contains(iaAddrOpt.getInetAddress())) {
							log.info("Setting zero(0) lifetimes for off link address: " +
									iaAddrOpt.getInetAddress().getHostAddress());
							iaAddrOpt.setPreferredLifetime(0);
							iaAddrOpt.setValidLifetime(0);
							onLink = false;
						}
					}
				}
			}
		}
		return onLink;
	}
	
	/**
	 * All ia addrs on link.
	 * 
	 * @param dhcpIaTaOption the dhcp ia ta option
	 * @param clientLink the client link
	 * 
	 * @return true, if successful
	 */
	protected boolean allIaAddrsOnLink(DhcpV6IaTaOption dhcpIaTaOption, DhcpLink clientLink)
	{
		boolean onLink = true;	// assume all IPs are on link
		if (dhcpIaTaOption != null) {
			List<DhcpV6IaAddrOption> iaAddrOpts = dhcpIaTaOption.getIaAddrOptions();
			if (iaAddrOpts != null) {
				for (DhcpV6IaAddrOption iaAddrOpt : iaAddrOpts) {
					if (clientLink.getSubnet().getSubnetAddress().isLinkLocalAddress()) {
						// if the Link address is link-local, then check if the
						// address is within one of the pools configured for this
						// local Link, which automatically makes this server
						// "authoritative" (in ISC parlance) for this local net
						V6AddressPool p = DhcpServerConfiguration.findTaAddrPool(clientLink.getLink(),
									iaAddrOpt.getInetAddress());
						if (p == null) {
							log.info("No local address pool found for requested IA_TA: " + 
									iaAddrOpt.getInetAddress().getHostAddress() +
									" - considered to be off link");
							iaAddrOpt.setPreferredLifetime(0);
							iaAddrOpt.setValidLifetime(0);
							onLink = false;
						}
					}
					else {
						if (!clientLink.getSubnet().contains(iaAddrOpt.getInetAddress())) {
							log.info("Setting zero(0) lifetimes for off link address: " +
									iaAddrOpt.getInetAddress().getHostAddress());
							iaAddrOpt.setPreferredLifetime(0);
							iaAddrOpt.setValidLifetime(0);
							onLink = false;
						}
					}
				}
			}
		}
		return onLink;
	}
	
	/**
	 * All ia prefixes on link.
	 * 
	 * @param dhcpIaPdOption the dhcp ia pd option
	 * @param clientLink the client link
	 * 
	 * @return true, if successful
	 */
	protected boolean allIaPrefixesOnLink(DhcpV6IaPdOption dhcpIaPdOption, DhcpLink clientLink)
	{
		boolean onLink = true;	// assume all IPs are on link
		if (dhcpIaPdOption != null) {
			List<DhcpV6IaPrefixOption> iaPrefixOpts = dhcpIaPdOption.getIaPrefixOptions();
			if (iaPrefixOpts != null) {
				for (DhcpV6IaPrefixOption iaPrefixOpt : iaPrefixOpts) {
					if (clientLink.getSubnet().getSubnetAddress().isLinkLocalAddress()) {
						// if the Link address is link-local, then check if the
						// address is within one of the pools configured for this
						// local Link, which automatically makes this server
						// "authoritative" (in ISC parlance) for this local net
						V6PrefixPool p = DhcpServerConfiguration.findPrefixPool(clientLink.getLink(),
									iaPrefixOpt.getInetAddress());
						if (p == null) {
							log.info("No local prefix pool found for requested IA_PD: " + 
									iaPrefixOpt.getInetAddress().getHostAddress() +
									" - considered to be off link");
							iaPrefixOpt.setPreferredLifetime(0);
							iaPrefixOpt.setValidLifetime(0);
							onLink = false;
						}
					}
					else {
						if (!clientLink.getSubnet().contains(iaPrefixOpt.getInetAddress())) {
							log.info("Setting zero(0) lifetimes for off link prefix: " +
									iaPrefixOpt.getInetAddress().getHostAddress());
							iaPrefixOpt.setPreferredLifetime(0);
							iaPrefixOpt.setValidLifetime(0);
							onLink = false;
						}
					}
				}
			}
		}
		return onLink;
	}
	
    /**
     * The Class RecentMsgTimerTask.
     */
    class RecentMsgTimerTask extends TimerTask
    {
    	
	    /** The dhcp msg. */
	    private DhcpV6Message dhcpMsg;
    	
    	/**
	     * Instantiates a new recent msg timer task.
	     * 
	     * @param dhcpMsg the dhcp msg
	     */
	    public RecentMsgTimerTask(DhcpV6Message dhcpMsg)
    	{
    		this.dhcpMsg = dhcpMsg;
    	}
    	
    	/* (non-Javadoc)
	     * @see java.util.TimerTask#run()
	     */
	    @Override
    	public void run() {
			if (recentMsgs.remove(dhcpMsg)) {
    			if (log.isDebugEnabled())
    				log.debug("Pruned recent message: " + dhcpMsg.toString());
			}
    	}

    } 
}
