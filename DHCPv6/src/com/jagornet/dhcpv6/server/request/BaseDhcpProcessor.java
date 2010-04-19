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

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpClientFqdnOption;
import com.jagornet.dhcpv6.option.DhcpIaAddrOption;
import com.jagornet.dhcpv6.option.DhcpIaNaOption;
import com.jagornet.dhcpv6.option.DhcpIaPdOption;
import com.jagornet.dhcpv6.option.DhcpIaPrefixOption;
import com.jagornet.dhcpv6.option.DhcpIaTaOption;
import com.jagornet.dhcpv6.option.DhcpServerIdOption;
import com.jagornet.dhcpv6.option.DhcpStatusCodeOption;
import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.server.config.DhcpLink;
import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcpv6.server.request.binding.AddressBindingPool;
import com.jagornet.dhcpv6.server.request.binding.Binding;
import com.jagornet.dhcpv6.server.request.binding.BindingAddress;
import com.jagornet.dhcpv6.server.request.binding.BindingObject;
import com.jagornet.dhcpv6.server.request.binding.BindingPrefix;
import com.jagornet.dhcpv6.server.request.binding.PrefixBindingPool;
import com.jagornet.dhcpv6.server.request.ddns.DdnsUpdater;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.xml.AddressPool;
import com.jagornet.dhcpv6.xml.ClientFqdnOption;
import com.jagornet.dhcpv6.xml.DomainNameOptionType;
import com.jagornet.dhcpv6.xml.IaAddrOption;
import com.jagornet.dhcpv6.xml.IaNaOption;
import com.jagornet.dhcpv6.xml.IaPdOption;
import com.jagornet.dhcpv6.xml.IaPrefixOption;
import com.jagornet.dhcpv6.xml.IaTaOption;
import com.jagornet.dhcpv6.xml.Link;
import com.jagornet.dhcpv6.xml.PrefixPool;

/**
 * Title: BaseDhcpRequestProcessor
 * Description: The base class for processing client messages.
 * 
 * @author A. Gregory Rabil
 */

public abstract class BaseDhcpProcessor implements DhcpMessageProcessor
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(BaseDhcpProcessor.class);

    /** The dhcp server config. */
    protected static DhcpServerConfiguration dhcpServerConfig = 
                                        DhcpServerConfiguration.getInstance();
    
    // wrap the configured ServerId option in a DhcpOption for the wire
    /** The dhcp server id option. */
    protected static DhcpServerIdOption dhcpServerIdOption = 
    	new DhcpServerIdOption(dhcpServerConfig.getDhcpV6ServerConfig().getServerIdOption());
    
    /** The request message. */
    protected final DhcpMessage requestMsg;
    
    /** The reply message. */
    protected DhcpMessage replyMsg;

    /** The client link address. */
    protected final InetAddress clientLinkAddress;
    
    /** The configuration Link object for the client link. */
    protected DhcpLink clientLink;
    
    /** The list of Bindings for this request. */
    protected List<Binding> bindings = new ArrayList<Binding>();
    
    /** The recent msgs. */
    protected static Set<DhcpMessage> recentMsgs = 
    	Collections.synchronizedSet(new HashSet<DhcpMessage>());
    
    /** The recent msg pruner. */
    protected static Timer recentMsgPruner = new Timer("RecentMsgPruner");
    
    /**
     * Construct an BaseDhcpRequest processor.  Since this class is
     * abstract, this constructor is protected for implementing classes.
     * 
     * @param requestMsg the DhcpMessage received from the client
     * @param clientLinkAddress the client link address
     */
    protected BaseDhcpProcessor(DhcpMessage requestMsg, InetAddress clientLinkAddress)
    {
        this.requestMsg = requestMsg;
        this.clientLinkAddress = clientLinkAddress;
    }
    
    /**
     * Populate reply msg options.
     */
    protected void populateReplyMsgOptions()
    {
    	replyMsg.putAllDhcpOptions(dhcpServerConfig.effectiveMsgOptions(requestMsg));
    }
    
    /**
     * Populate reply msg options.
     * 
     * @param link the link
     */
    protected void populateReplyMsgOptions(Link link)
    {
    	replyMsg.putAllDhcpOptions(dhcpServerConfig.effectiveMsgOptions(requestMsg, link));
    }
    
    /**
     * Populate ia na options.
     * 
     * @param iaNaOption the ia na option
     */
    protected void populateIaNaOptions(DhcpIaNaOption iaNaOption)
    {
    	iaNaOption.putAllDhcpOptions(dhcpServerConfig.effectiveIaNaOptions(requestMsg));
    }
    
    /**
     * Populate ia ta options.
     * 
     * @param iaTaOption the ia ta option
     */
    protected void populateIaTaOptions(DhcpIaTaOption iaTaOption)
    {
    	iaTaOption.putAllDhcpOptions(dhcpServerConfig.effectiveIaTaOptions(requestMsg));
    }
    
    /**
     * Populate ia pd options.
     * 
     * @param iaPdOption the ia pd option
     */
    protected void populateIaPdOptions(DhcpIaPdOption iaPdOption)
    {
    	iaPdOption.putAllDhcpOptions(dhcpServerConfig.effectiveIaPdOptions(requestMsg));
    }

    /**
     * Populate ia na options.
     * 
     * @param iaNaOption the ia na option
     * @param link the link
     */
    protected void populateIaNaOptions(DhcpIaNaOption iaNaOption, Link link)
    {
    	iaNaOption.putAllDhcpOptions(dhcpServerConfig.effectiveIaNaOptions(requestMsg, link));
    }
    
    /**
     * Populate ia ta options.
     * 
     * @param iaTaOption the ia ta option
     * @param link the link
     */
    protected void populateIaTaOptions(DhcpIaTaOption iaTaOption, Link link)
    {
    	iaTaOption.putAllDhcpOptions(dhcpServerConfig.effectiveIaTaOptions(requestMsg, link));
    }
    
    /**
     * Populate ia pd options.
     * 
     * @param iaPdOption the ia pd option
     * @param link the link
     */
    protected void populateIaPdOptions(DhcpIaPdOption iaPdOption, Link link)
    {
    	iaPdOption.putAllDhcpOptions(dhcpServerConfig.effectiveIaPdOptions(requestMsg, link));
    }
    
    /**
     * Populate na addr options.
     * 
     * @param iaAddrOption the ia addr option
     */
    protected void populateNaAddrOptions(DhcpIaAddrOption iaAddrOption)
    {
    	iaAddrOption.putAllDhcpOptions(dhcpServerConfig.effectiveNaAddrOptions(requestMsg));
    }
    
    /**
     * Populate ta addr options.
     * 
     * @param iaAddrOption the ia addr option
     */
    protected void populateTaAddrOptions(DhcpIaAddrOption iaAddrOption)
    {
    	iaAddrOption.putAllDhcpOptions(dhcpServerConfig.effectiveTaAddrOptions(requestMsg));
    }
    
    /**
     * Populate prefix options.
     * 
     * @param iaPrefixOption the ia prefix option
     */
    protected void populatePrefixOptions(DhcpIaPrefixOption iaPrefixOption)
    {
    	iaPrefixOption.putAllDhcpOptions(dhcpServerConfig.effectivePrefixOptions(requestMsg));
    }
    
    /**
     * Populate na addr options.
     * 
     * @param iaAddrOption the ia addr option
     * @param link the link
     */
    protected void populateNaAddrOptions(DhcpIaAddrOption iaAddrOption, Link link)
    {
    	iaAddrOption.putAllDhcpOptions(dhcpServerConfig.effectiveNaAddrOptions(requestMsg, link));
    }
    
    /**
     * Populate ta addr options.
     * 
     * @param iaAddrOption the ia addr option
     * @param link the link
     */
    protected void populateTaAddrOptions(DhcpIaAddrOption iaAddrOption, Link link)
    {
    	iaAddrOption.putAllDhcpOptions(dhcpServerConfig.effectiveTaAddrOptions(requestMsg, link));
    }
    
    /**
     * Populate prefix options.
     * 
     * @param iaPrefixOption the ia prefix option
     * @param link the link
     */
    protected void populatePrefixOptions(DhcpIaPrefixOption iaPrefixOption, Link link)
    {
    	iaPrefixOption.putAllDhcpOptions(dhcpServerConfig.effectivePrefixOptions(requestMsg, link));
    }
	
    /**
     * Populate na addr options.
     * 
     * @param iaAddrOption the ia addr option
     * @param link the link
     * @param pool the pool
     */
    protected void populateNaAddrOptions(DhcpIaAddrOption iaAddrOption, Link link, AddressPool pool)
    {
	    iaAddrOption.putAllDhcpOptions(dhcpServerConfig.effectiveNaAddrOptions(requestMsg, link, pool));
    }    
    
    /**
     * Populate ta addr options.
     * 
     * @param iaAddrOption the ia addr option
     * @param link the link
     * @param pool the pool
     */
    protected void populateTaAddrOptions(DhcpIaAddrOption iaAddrOption, Link link, AddressPool pool)
    {
	    iaAddrOption.putAllDhcpOptions(dhcpServerConfig.effectiveTaAddrOptions(requestMsg, link, pool));
    }    
    
    /**
     * Populate prefix options.
     * 
     * @param iaPrefixOption the ia prefix option
     * @param link the link
     * @param pool the pool
     */
    protected void populatePrefixOptions(DhcpIaPrefixOption iaPrefixOption, Link link, PrefixPool pool)
    {
	    iaPrefixOption.putAllDhcpOptions(dhcpServerConfig.effectivePrefixOptions(requestMsg, link, pool));
    }    

    /**
     * Process the client request.  Find appropriate configuration based on any
     * criteria in the request message that can be matched against the server's
     * configuration, then formulate a response message containing the options
     * to be sent to the client.
     * 
     * @return a Reply DhcpMessage
     */
    public DhcpMessage processMessage()
    {
    	if (!preProcess()) {
    		log.warn("Message dropped by preProcess");
    		return null;
    	}
        
    	try {
    		if (log.isDebugEnabled()) {
    			log.debug("Processing: " + requestMsg.toStringWithOptions());
    		}
    		else if (log.isInfoEnabled()) {
    	        log.info("Processing: " + requestMsg.toString());
    		}
	        
	        // build a reply message using the local and remote sockets from the request
	        replyMsg = new DhcpMessage(requestMsg.getLocalAddress(), requestMsg.getRemoteAddress());
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
	        	return null;
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
     * Should multicast.
     * 
     * @return true, if successful
     */
    protected boolean shouldMulticast()
    {
    	if (requestMsg.isUnicast()) {
        	Map<Integer, DhcpOption> effectiveMsgOptions = 
    	      	dhcpServerConfig.effectiveMsgOptions(requestMsg, clientLink.getLink());
        	if ((effectiveMsgOptions != null) &&
        			!effectiveMsgOptions.containsKey(DhcpConstants.OPTION_UNICAST)) {
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
		DhcpStatusCodeOption statusOption = new DhcpStatusCodeOption();
		statusOption.getStatusCodeOption().setStatusCode(statusCode);
		replyMsg.putDhcpOption(statusOption);
	}
	
	/**
	 * Adds the ia na option status to reply.
	 * 
	 * @param iaNaOption the ia na option
	 * @param statusCode the status code
	 */
	protected void addIaNaOptionStatusToReply(DhcpIaNaOption iaNaOption, int statusCode)
	{
		DhcpIaNaOption replyIaNaOption = new DhcpIaNaOption(iaNaOption.getIaNaOption());
		DhcpStatusCodeOption status = new DhcpStatusCodeOption();
		status.getStatusCodeOption().setStatusCode(statusCode);
		replyIaNaOption.putDhcpOption(status);
		replyMsg.addIaNaOption(replyIaNaOption);
	}
	
	/**
	 * Adds the ia ta option status to reply.
	 * 
	 * @param iaTaOption the ia ta option
	 * @param statusCode the status code
	 */
	protected void addIaTaOptionStatusToReply(DhcpIaTaOption iaTaOption, int statusCode)
	{
		DhcpIaTaOption replyIaTaOption = new DhcpIaTaOption(iaTaOption.getIaTaOption());
		DhcpStatusCodeOption status = new DhcpStatusCodeOption();
		status.getStatusCodeOption().setStatusCode(statusCode);
		replyIaTaOption.putDhcpOption(status);
		replyMsg.addIaTaOption(replyIaTaOption);
	}
	
	/**
	 * Adds the ia pd option status to reply.
	 * 
	 * @param iaPdOption the ia pd option
	 * @param statusCode the status code
	 */
	protected void addIaPdOptionStatusToReply(DhcpIaPdOption iaPdOption, int statusCode)
	{
		DhcpIaPdOption replyIaPdOption = new DhcpIaPdOption(iaPdOption.getIaPdOption());
		DhcpStatusCodeOption status = new DhcpStatusCodeOption();
		status.getStatusCodeOption().setStatusCode(statusCode);
		replyIaPdOption.putDhcpOption(status);
		replyMsg.addIaPdOption(replyIaPdOption);
	}
	
	/**
	 * Adds the binding to reply.
	 * 
	 * @param clientLink the client link
	 * @param binding the binding
	 */
	protected void addBindingToReply(Link clientLink, Binding binding)
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
	protected void addNaBindingToReply(Link clientLink, Binding binding)
	{
		DhcpIaNaOption dhcpIaNaOption = new DhcpIaNaOption();
		IaNaOption iaNaOption = dhcpIaNaOption.getIaNaOption(); 
		iaNaOption.setIaId(binding.getIaid());

		long minPreferredLifetime = 0;
		Collection<BindingObject> bindingObjs = binding.getBindingObjects();
		if ((bindingObjs != null) && !bindingObjs.isEmpty()) {
			minPreferredLifetime = 0xffffffff;
			List<DhcpIaAddrOption> dhcpIaAddrOptions = new ArrayList<DhcpIaAddrOption>(); 
			for (BindingObject bindingObj : bindingObjs) {
				DhcpIaAddrOption dhcpIaAddrOption = new DhcpIaAddrOption();
				IaAddrOption iaAddrOption = dhcpIaAddrOption.getIaAddrOption();
				InetAddress inetAddr = bindingObj.getIpAddress();
				if (inetAddr != null) {
					iaAddrOption.setIpv6Address(inetAddr.getHostAddress());
					// must be an AddresBindingPool for IaNa binding
					AddressBindingPool bp = 
						(AddressBindingPool) bindingObj.getBindingPool();
					if (bp != null) {
						long preferred = bp.getPreferredLifetime();
						if ((minPreferredLifetime == 0xffffffff) ||
								(preferred < minPreferredLifetime))  {
							minPreferredLifetime = preferred; 
						}
						iaAddrOption.setPreferredLifetime(preferred);
						iaAddrOption.setValidLifetime(bp.getValidLifetime());
						populateNaAddrOptions(dhcpIaAddrOption, clientLink, bp.getAddressPool());
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
		
		setIaNaT1(clientLink, iaNaOption, minPreferredLifetime);
		setIaNaT2(clientLink, iaNaOption, minPreferredLifetime);
		
		populateIaNaOptions(dhcpIaNaOption, clientLink);
		replyMsg.addIaNaOption(dhcpIaNaOption);
	}
	
	/**
	 * Adds the ta binding to reply.
	 * 
	 * @param clientLink the client link
	 * @param binding the binding
	 */
	protected void addTaBindingToReply(Link clientLink, Binding binding)
	{
		DhcpIaTaOption dhcpIaTaOption = new DhcpIaTaOption();
		IaTaOption iaTaOption = dhcpIaTaOption.getIaTaOption(); 
		iaTaOption.setIaId(binding.getIaid());
		
		Collection<BindingObject> bindingObjs = binding.getBindingObjects();
		if ((bindingObjs != null) && !bindingObjs.isEmpty()) {
			List<DhcpIaAddrOption> dhcpIaAddrOptions = new ArrayList<DhcpIaAddrOption>(); 
			for (BindingObject bindingObj : bindingObjs) {
				DhcpIaAddrOption dhcpIaAddrOption = new DhcpIaAddrOption();
				IaAddrOption iaAddrOption = dhcpIaAddrOption.getIaAddrOption();
				InetAddress inetAddr = bindingObj.getIpAddress();
				if (inetAddr != null) {
					iaAddrOption.setIpv6Address(inetAddr.getHostAddress());
					// must be an AddresBindingPool for IaNa binding
					AddressBindingPool bp = 
						(AddressBindingPool) bindingObj.getBindingPool();
					if (bp != null) {
						iaAddrOption.setPreferredLifetime(bp.getPreferredLifetime());
						iaAddrOption.setValidLifetime(bp.getValidLifetime());
						populateTaAddrOptions(dhcpIaAddrOption, clientLink, bp.getAddressPool());
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
		
		populateIaTaOptions(dhcpIaTaOption, clientLink);
		replyMsg.addIaTaOption(dhcpIaTaOption);
	}
	
	/**
	 * Adds the pd binding to reply.
	 * 
	 * @param clientLink the client link
	 * @param binding the binding
	 */
	protected void addPdBindingToReply(Link clientLink, Binding binding)
	{
		DhcpIaPdOption dhcpIaPdOption = new DhcpIaPdOption();
		IaPdOption iaPdOption = dhcpIaPdOption.getIaPdOption(); 
		iaPdOption.setIaId(binding.getIaid());
		
		long minPreferredLifetime = 0;
		Collection<BindingObject> bindingObjs = binding.getBindingObjects();
		if ((bindingObjs != null) && !bindingObjs.isEmpty()) {
			minPreferredLifetime = 0xffffffff;
			List<DhcpIaPrefixOption> dhcpIaPrefixOptions = new ArrayList<DhcpIaPrefixOption>(); 
			for (BindingObject bindingObj : bindingObjs) {
				// must be a Binding Prefix for IaPd binding
				BindingPrefix bindingPrefix = (BindingPrefix) bindingObj;
				DhcpIaPrefixOption dhcpIaPrefixOption = new DhcpIaPrefixOption();
				IaPrefixOption iaPrefixOption = dhcpIaPrefixOption.getIaPrefixOption();
				InetAddress inetAddr = bindingObj.getIpAddress();
				if (inetAddr != null) {
					iaPrefixOption.setIpv6Prefix(inetAddr.getHostAddress());
					iaPrefixOption.setPrefixLength(bindingPrefix.getPrefixLength());
					// must be an PrefixBindingPool for IaPd binding
					PrefixBindingPool bp = 
						(PrefixBindingPool) bindingPrefix.getBindingPool();
					if (bp != null) {
						long preferred = bp.getPreferredLifetime();
						if ((minPreferredLifetime == 0xffffffff) ||
								(preferred < minPreferredLifetime))  {
							minPreferredLifetime = preferred; 
						}
						iaPrefixOption.setPreferredLifetime(preferred);
						iaPrefixOption.setValidLifetime(bp.getValidLifetime());
						populatePrefixOptions(dhcpIaPrefixOption, clientLink, bp.getPrefixPool());
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
		}
		
		setIaPdT1(clientLink, iaPdOption, minPreferredLifetime);
		setIaPdT2(clientLink, iaPdOption, minPreferredLifetime);
		
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
	private void setIaNaT1(Link clientLink, IaNaOption iaNaOption,
			long minPreferredLifetime)
	{
		float t1 = DhcpServerPolicies.effectivePolicyAsFloat(clientLink, Property.IA_NA_T1);
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
	private void setIaNaT2(Link clientLink, IaNaOption iaNaOption,
			long minPreferredLifetime)
	{
		float t2 = DhcpServerPolicies.effectivePolicyAsFloat(clientLink, Property.IA_NA_T2);
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
	private void setIaPdT1(Link clientLink, IaPdOption iaPdOption,
			long minPreferredLifetime)
	{
		float t1 = DhcpServerPolicies.effectivePolicyAsFloat(clientLink, Property.IA_PD_T1);
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
	private void setIaPdT2(Link clientLink, IaPdOption iaPdOption,
			long minPreferredLifetime)
	{
		float t2 = DhcpServerPolicies.effectivePolicyAsFloat(clientLink, Property.IA_PD_T2);
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
	protected void processDdnsUpdates()
	{
		DhcpClientFqdnOption clientFqdnOption = 
			(DhcpClientFqdnOption) requestMsg.getDhcpOption(DhcpConstants.OPTION_CLIENT_FQDN);
		if (clientFqdnOption == null) {
			//TODO allow name generation?
			log.debug("No Client FQDN option in request.  Skipping DDNS update processing.");
			return;
		}

		DhcpClientFqdnOption replyFqdnOption = 
			new DhcpClientFqdnOption((ClientFqdnOption) clientFqdnOption.getDomainNameOption());
		replyFqdnOption.setUpdateAaaaBit(false);
		replyFqdnOption.setOverrideBit(false);
		replyFqdnOption.setNoUpdateBit(false);
		
		DomainNameOptionType domainNameOption = clientFqdnOption.getDomainNameOption();
		String fqdn = domainNameOption.getDomainName();
		if ((fqdn == null) || (fqdn.length() <= 0)) {
			log.error("Client FQDN option domain name is null/empty.  No DDNS udpates performed.");
			replyFqdnOption.setNoUpdateBit(true);	// tell client that server did no updates
			replyMsg.putDhcpOption(replyFqdnOption);
			return;
		}
		
		String policy = DhcpServerPolicies.effectivePolicy(requestMsg,
				clientLink.getLink(), Property.DDNS_UPDATE);
		log.info("Server configuration for ddns.update policy: " + policy);
		if ((policy == null) || policy.equalsIgnoreCase("none")) {
			log.info("Server configuration for ddns.update policy is null or 'none'." +
					"  No DDNS updates performed.");
			replyFqdnOption.setNoUpdateBit(true);	// tell client that server did no updates
			replyMsg.putDhcpOption(replyFqdnOption);
			return;
		}
				
		if (clientFqdnOption.getNoUpdateBit() && policy.equalsIgnoreCase("honorNoUpdate")) {
			log.info("Client FQDN NoUpdate flag set.  Server configured to honor request." +
					"  No DDNS updates performed.");
			replyFqdnOption.setNoUpdateBit(true);	// tell client that server did no updates
			replyMsg.putDhcpOption(replyFqdnOption);
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
			domainNameOption.setDomainName(fqdn);
			replyFqdnOption.setDomainNameOption(domainNameOption);
		}
		
		replyMsg.putDhcpOption(replyFqdnOption);

		for (Binding binding : bindings) {
			if (binding.getState() == Binding.COMMITTED) {
				Collection<BindingObject> bindingObjs = binding.getBindingObjects();
				if (bindingObjs != null) {
					for (BindingObject bindingObj : bindingObjs) {
						DdnsUpdater ddns = 
							new DdnsUpdater(requestMsg, clientLink.getLink(), 
									(BindingAddress) bindingObj, fqdn, doForwardUpdate, false);
						ddns.processUpdates();
					}
				}
				try {
					byte[] newVal = replyFqdnOption.encode().array();
					// don't store the option code, start with length to
					// simplify decoding when retrieving from database
					newVal = Arrays.copyOfRange(newVal, 2, newVal.length);
					com.jagornet.dhcpv6.db.DhcpOption dbOption = 
						binding.getDhcpOption(DhcpConstants.OPTION_CLIENT_FQDN);
					if (dbOption == null) {
						dbOption = new com.jagornet.dhcpv6.db.DhcpOption();
						dbOption.setCode(replyFqdnOption.getCode());
						dbOption.setValue(newVal);
						dhcpServerConfig.getIaMgr().addDhcpOption(binding, dbOption);
					}
					else {
						if(!Arrays.equals(dbOption.getValue(), newVal)) {
							dbOption.setValue(newVal);
							dhcpServerConfig.getIaMgr().updateDhcpOption(dbOption);
						}
					}
				} 
				catch (IOException ex) {
					log.error("Failed to update binding with Client FQDN Option", ex);
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
	protected boolean allIaAddrsOnLink(DhcpIaNaOption dhcpIaNaOption, DhcpLink clientLink)
	{
		boolean onLink = true;	// assume all IPs are on link
		if (dhcpIaNaOption != null) {
			List<DhcpIaAddrOption> iaAddrOpts = dhcpIaNaOption.getIaAddrOptions();
			if (iaAddrOpts != null) {
				for (DhcpIaAddrOption iaAddrOpt : iaAddrOpts) {
					if (clientLink.getSubnet().getSubnetAddress().isLinkLocalAddress()) {
						// if the Link address is link-local, then check if the
						// address is within one of the pools configured for this
						// local Link, which automatically makes this server
						// "authoritative" (in ISC parlance) for this local net
						AddressPool p = DhcpServerConfiguration.findNaAddrPool(clientLink.getLink(),
									iaAddrOpt.getInetAddress());
						if (p == null) {
							onLink = false;
						}
					}
					else {
						if (!clientLink.getSubnet().contains(iaAddrOpt.getInetAddress())) {
							log.info("Setting zero(0) lifetimes for off link address: " +
									iaAddrOpt.getInetAddress().getHostAddress());
							iaAddrOpt.getIaAddrOption().setPreferredLifetime(0);
							iaAddrOpt.getIaAddrOption().setValidLifetime(0);
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
	protected boolean allIaAddrsOnLink(DhcpIaTaOption dhcpIaTaOption, DhcpLink clientLink)
	{
		boolean onLink = true;	// assume all IPs are on link
		if (dhcpIaTaOption != null) {
			List<DhcpIaAddrOption> iaAddrOpts = dhcpIaTaOption.getIaAddrOptions();
			if (iaAddrOpts != null) {
				for (DhcpIaAddrOption iaAddrOpt : iaAddrOpts) {
					if (clientLink.getSubnet().getSubnetAddress().isLinkLocalAddress()) {
						// if the Link address is link-local, then check if the
						// address is within one of the pools configured for this
						// local Link, which automatically makes this server
						// "authoritative" (in ISC parlance) for this local net
						AddressPool p = DhcpServerConfiguration.findTaAddrPool(clientLink.getLink(),
									iaAddrOpt.getInetAddress());
						if (p == null) {
							onLink = false;
						}
					}
					else {
						if (!clientLink.getSubnet().contains(iaAddrOpt.getInetAddress())) {
							log.info("Setting zero(0) lifetimes for off link address: " +
									iaAddrOpt.getInetAddress().getHostAddress());
							iaAddrOpt.getIaAddrOption().setPreferredLifetime(0);
							iaAddrOpt.getIaAddrOption().setValidLifetime(0);
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
	protected boolean allIaPrefixesOnLink(DhcpIaPdOption dhcpIaPdOption, DhcpLink clientLink)
	{
		boolean onLink = true;	// assume all IPs are on link
		if (dhcpIaPdOption != null) {
			List<DhcpIaPrefixOption> iaPrefixOpts = dhcpIaPdOption.getIaPrefixOptions();
			if (iaPrefixOpts != null) {
				for (DhcpIaPrefixOption iaPrefixOpt : iaPrefixOpts) {
					if (clientLink.getSubnet().getSubnetAddress().isLinkLocalAddress()) {
						// if the Link address is link-local, then check if the
						// address is within one of the pools configured for this
						// local Link, which automatically makes this server
						// "authoritative" (in ISC parlance) for this local net
						PrefixPool p = DhcpServerConfiguration.findPrefixPool(clientLink.getLink(),
									iaPrefixOpt.getInetAddress());
						if (p == null) {
							onLink = false;
						}
					}
					else {
						if (!clientLink.getSubnet().contains(iaPrefixOpt.getInetAddress())) {
							log.info("Setting zero(0) lifetimes for off link address: " +
									iaPrefixOpt.getInetAddress().getHostAddress());
							iaPrefixOpt.getIaPrefixOption().setPreferredLifetime(0);
							iaPrefixOpt.getIaPrefixOption().setValidLifetime(0);
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
	    private DhcpMessage dhcpMsg;
    	
    	/**
	     * Instantiates a new recent msg timer task.
	     * 
	     * @param dhcpMsg the dhcp msg
	     */
	    public RecentMsgTimerTask(DhcpMessage dhcpMsg)
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
