/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseDhcpV4Processor.java is part of DHCPv6.
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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
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
import com.jagornet.dhcpv6.option.v4.DhcpV4ClientFqdnOption;
import com.jagornet.dhcpv6.option.v4.DhcpV4HostnameOption;
import com.jagornet.dhcpv6.option.v4.DhcpV4LeaseTimeOption;
import com.jagornet.dhcpv6.option.v4.DhcpV4RequestedIpAddressOption;
import com.jagornet.dhcpv6.option.v4.DhcpV4ServerIdOption;
import com.jagornet.dhcpv6.server.config.DhcpConfigObject;
import com.jagornet.dhcpv6.server.config.DhcpLink;
import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcpv6.server.config.DhcpV4OptionConfigObject;
import com.jagornet.dhcpv6.server.request.binding.Binding;
import com.jagornet.dhcpv6.server.request.binding.BindingObject;
import com.jagornet.dhcpv6.server.request.binding.V4BindingAddress;
import com.jagornet.dhcpv6.server.request.ddns.DdnsCallback;
import com.jagornet.dhcpv6.server.request.ddns.DdnsUpdater;
import com.jagornet.dhcpv6.server.request.ddns.DhcpV4DdnsComplete;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.util.Util;

/**
 * Title: BaseDhcpV4Processor
 * Description: The base class for processing DHCPv4 client messages.
 * 
 * @author A. Gregory Rabil
 */

public abstract class BaseDhcpV4Processor implements DhcpV4MessageProcessor
{
	private static Logger log = LoggerFactory.getLogger(BaseDhcpV4Processor.class);

    protected static DhcpServerConfiguration dhcpServerConfig = 
                                        DhcpServerConfiguration.getInstance();
    
    // wrap the configured V4ServerId option in a DhcpOption for the wire
    protected static DhcpV4ServerIdOption dhcpV4ServerIdOption = 
    	new DhcpV4ServerIdOption(dhcpServerConfig.getDhcpServerConfig().getV4ServerIdOption());
    
    protected final DhcpV4Message requestMsg;
    protected DhcpV4Message replyMsg;
    protected final InetAddress clientLinkAddress;
    protected DhcpLink clientLink;
    protected List<Binding> bindings = new ArrayList<Binding>();
    protected static Set<DhcpV4Message> recentMsgs = 
    	Collections.synchronizedSet(new HashSet<DhcpV4Message>());
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
     * @param configObj the config object or null if none
     */
    protected void populateV4Reply(DhcpLink dhcpLink, DhcpV4OptionConfigObject configObj)
    {
    	String sname = DhcpServerPolicies.effectivePolicy(requestMsg, configObj, 
    			dhcpLink.getLink(), Property.V4_HEADER_SNAME);
    	if ((sname != null) && !sname.isEmpty()) {
    		replyMsg.setsName(sname);
    	}
    	
    	String filename = DhcpServerPolicies.effectivePolicy(requestMsg, configObj, 
    			dhcpLink.getLink(), Property.V4_HEADER_FILENAME);
    	if ((filename != null) && !filename.isEmpty()) {
    		replyMsg.setFile(filename);
    	}
    	
    	Map<Integer, DhcpOption> optionMap = 
    		dhcpServerConfig.effectiveV4AddrOptions(requestMsg, dhcpLink, configObj);
    	if (DhcpServerPolicies.effectivePolicyAsBoolean(configObj,
    			dhcpLink.getLink(), Property.SEND_REQUESTED_OPTIONS_ONLY)) {
    		optionMap = requestedOptions(optionMap, requestMsg);
    	}
    	replyMsg.putAllDhcpOptions(optionMap);
    	
    	// copy the relay agent info option from request to reply 
    	// in order to echo option back to router as required
    	if (requestMsg.hasOption(DhcpConstants.V4OPTION_RELAY_INFO)) {
    		replyMsg.putDhcpOption(requestMsg.getDhcpOption(DhcpConstants.V4OPTION_RELAY_INFO));
    	}
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
	        replyMsg.setHtype(requestMsg.getHtype());
	        replyMsg.setHlen(requestMsg.getHlen());
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
    	InetSocketAddress localSocketAddr = requestMsg.getLocalAddress();
    	InetSocketAddress remoteSocketAddr = requestMsg.getRemoteAddress();
    	
    	byte chAddr[] = requestMsg.getChAddr();
    	if ((chAddr == null) || (chAddr.length == 0) || isIgnoredMac(chAddr)) {
    		log.warn("Ignorning request message from client: mac=" +
    					Util.toHexString(chAddr));
    		return false;
    	}
    	
        clientLink = dhcpServerConfig.findDhcpLink(
        		(Inet4Address)localSocketAddr.getAddress(),
        		(Inet4Address)remoteSocketAddr.getAddress());
        if (clientLink == null) {
        	log.error("No Link configured for DHCPv4 client request: " +
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
					// must be an DhcpV4OptionConfigObject for v4 binding
					DhcpV4OptionConfigObject configObj = 
						(DhcpV4OptionConfigObject) bindingObj.getConfigObj();
					if (configObj != null) {
						long preferred = configObj.getPreferredLifetime();
						DhcpV4LeaseTimeOption dhcpV4LeaseTimeOption = new DhcpV4LeaseTimeOption();
						dhcpV4LeaseTimeOption.setUnsignedInt(preferred);
						replyMsg.putDhcpOption(dhcpV4LeaseTimeOption);
						populateV4Reply(clientLink, configObj);
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
	
	/**
	 * Process ddns updates.
	 */
	protected void processDdnsUpdates(boolean sendUpdates)
	{
		boolean doForwardUpdate = true;

		DhcpV4ClientFqdnOption clientFqdnOption = 
			(DhcpV4ClientFqdnOption) requestMsg.getDhcpOption(DhcpConstants.V4OPTION_CLIENT_FQDN);
		DhcpV4HostnameOption hostnameOption = 
			(DhcpV4HostnameOption) requestMsg.getDhcpOption(DhcpConstants.V4OPTION_HOSTNAME);
		
		if ((clientFqdnOption == null) && (hostnameOption == null)) {
			//TODO allow name generation?
			log.debug("No Client FQDN nor hostname option in request.  Skipping DDNS update processing.");
			return;
		}

		String fqdn = null;
		String domain = DhcpServerPolicies.effectivePolicy(clientLink.getLink(), Property.DDNS_DOMAIN); 
		DhcpV4ClientFqdnOption replyFqdnOption = null;

		if (clientFqdnOption != null) {
			replyFqdnOption = new DhcpV4ClientFqdnOption();
			replyFqdnOption.setDomainName(clientFqdnOption.getDomainName());
			replyFqdnOption.setUpdateABit(false);
			replyFqdnOption.setOverrideBit(false);
			replyFqdnOption.setNoUpdateBit(false);
			replyFqdnOption.setEncodingBit(clientFqdnOption.getEncodingBit());
			replyFqdnOption.setRcode1((short)0xff);		// RFC 4702 says server should set to 255
			replyFqdnOption.setRcode2((short)0xff);		// RFC 4702 says server should set to 255
			
			fqdn = clientFqdnOption.getDomainName();
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

			if (!clientFqdnOption.getUpdateABit() && policy.equalsIgnoreCase("honorNoA")) {
				log.info("Client FQDN NoA flag set.  Server configured to honor request." +
						"  No FORWARD DDNS updates performed.");
				doForwardUpdate = false;
			}
			else {
				replyFqdnOption.setUpdateABit(true);	// server will do update
				if (!clientFqdnOption.getUpdateABit())
					replyFqdnOption.setOverrideBit(true);	// tell client that we overrode request flag
			}
		
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
			// since the client DID send option 81, return it in the reply
			replyMsg.putDhcpOption(replyFqdnOption);
		}
		else {
			// The client did not send an FQDN option, so we'll try to formulate the FQDN
			// from the hostname option combined with the DDNS_DOMAIN policy setting.
			// A replyFqdnOption is fabricated to be stored with the binding for use
			// with the release/expire binding processing to remove the DDNS entry.
			replyFqdnOption = new DhcpV4ClientFqdnOption();
			fqdn = hostnameOption.getString();
			if ((domain != null) && !domain.isEmpty()) {
				log.info("Server configuration for domain policy: " + domain);
				fqdn = fqdn + "." + domain;
				// since the client did NOT send option 81, do not put
				// the fabricated fqdnOption into the reply packet
				// but set the option so that is can be used below
				// when storing the fqdnOption to the database, so 
				// that it can be used if/when the lease expires
				replyFqdnOption.setDomainName(fqdn);
				// server will do the A record update, so set the flag
				// for the option stored in the database, so server will
				// remove the A record when the lease expires
				replyFqdnOption.setUpdateABit(true);
			}
			else {
				log.error("No DDNS domain configured.  No DDNS udpates performed.");
				replyFqdnOption.setNoUpdateBit(true);	// tell client that server did no updates
				replyMsg.putDhcpOption(replyFqdnOption);
				return;
			}
		}

		if (sendUpdates) {
			for (Binding binding : bindings) {
				if (binding.getState() == Binding.COMMITTED) {
					Collection<BindingObject> bindingObjs = binding.getBindingObjects();
					if (bindingObjs != null) {
						for (BindingObject bindingObj : bindingObjs) {
							
							V4BindingAddress bindingAddr = (V4BindingAddress) bindingObj;
							
		        			DhcpConfigObject configObj = bindingAddr.getConfigObj();
		        			
		        			DdnsCallback ddnsComplete = 
		        				new DhcpV4DdnsComplete(bindingAddr, replyFqdnOption);
		        			
							DdnsUpdater ddns =
								new DdnsUpdater(requestMsg, clientLink.getLink(), configObj,
										bindingAddr.getIpAddress(), fqdn, requestMsg.getChAddr(),
										configObj.getValidLifetime(), doForwardUpdate, false,
										ddnsComplete);
							
							ddns.processUpdates();
						}
					}
				}
			}
		}
	}
	
	protected boolean addrOnLink(DhcpV4RequestedIpAddressOption requestedIpOption, DhcpLink clientLink)
	{
		boolean onLink = true;
		if (requestedIpOption != null) {
			try {
				InetAddress requestedIp = InetAddress.getByName(requestedIpOption.getIpAddress());
				if (!clientLink.getSubnet().contains(requestedIp)) {
					onLink = false;
				}
			} catch (UnknownHostException ex) {
				log.error("Invalid requested IP=" + requestedIpOption.getIpAddress() + ": " + ex);
			}
		}
		return onLink;
	}
    
    protected boolean isIgnoredMac(byte[] chAddr)
    {
    	String ignoredMacPolicy = DhcpServerPolicies.globalPolicy(Property.V4_IGNORED_MACS);
    	if (ignoredMacPolicy != null) {
    		String[] ignoredMacs = ignoredMacPolicy.split(",");
    		if (ignoredMacs != null) {
    			for (String ignoredMac : ignoredMacs) {
					if (ignoredMac.trim().equalsIgnoreCase(Util.toHexString(chAddr))) {
						return true;
					}
				}
    		}
    	}
    	return false;
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
			if (recentMsgs.remove(dhcpMsg)) {
    			if (log.isDebugEnabled())
    				log.debug("Pruned recent message: " + dhcpMsg.toString());
			}
    	}

    } 
}
