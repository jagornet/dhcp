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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpIaAddrOption;
import com.jagornet.dhcpv6.option.DhcpIaNaOption;
import com.jagornet.dhcpv6.option.DhcpServerIdOption;
import com.jagornet.dhcpv6.option.DhcpStatusCodeOption;
import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.server.config.DhcpLink;
import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcpv6.server.request.binding.Binding;
import com.jagornet.dhcpv6.server.request.binding.BindingAddress;
import com.jagornet.dhcpv6.server.request.binding.BindingManager;
import com.jagornet.dhcpv6.server.request.binding.BindingManagerInterface;
import com.jagornet.dhcpv6.server.request.binding.BindingPool;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.xml.IaAddrOption;
import com.jagornet.dhcpv6.xml.IaNaOption;
import com.jagornet.dhcpv6.xml.Link;
import com.jagornet.dhcpv6.xml.Pool;

// TODO: Auto-generated Javadoc
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
    
    /** The recent msgs. */
    protected static Set<DhcpMessage> recentMsgs = 
    	Collections.synchronizedSet(new HashSet<DhcpMessage>());
    
    /** The recent msg pruner. */
    protected static Timer recentMsgPruner = new Timer("RecentMsgPruner");
    
    /** The binding mgr. */
    protected static BindingManagerInterface bindingMgr = BindingManager.getInstance();
    
    /**
     * Construct an BaseDhcpRequest processor.  Since this class is
     * abstract, this constructor is provided for implementing classes.
     * 
     * @param requestMsg the DhcpMessage received from the client
     * @param clientLinkAddress the client link address
     */
    public BaseDhcpProcessor(DhcpMessage requestMsg, InetAddress clientLinkAddress)
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
     * Populate ia options.
     * 
     * @param iaNaOption the ia na option
     */
    protected void populateIaOptions(DhcpIaNaOption iaNaOption)
    {
    	iaNaOption.putAllDhcpOptions(dhcpServerConfig.effectiveIaOptions(requestMsg));
    }

    /**
     * Populate ia options.
     * 
     * @param iaNaOption the ia na option
     * @param link the link
     */
    protected void populateIaOptions(DhcpIaNaOption iaNaOption, Link link)
    {
    	iaNaOption.putAllDhcpOptions(dhcpServerConfig.effectiveIaOptions(requestMsg, link));
    }
    
    /**
     * Populate addr options.
     * 
     * @param iaAddrOption the ia addr option
     */
    protected void populateAddrOptions(DhcpIaAddrOption iaAddrOption)
    {
    	iaAddrOption.putAllDhcpOptions(dhcpServerConfig.effectiveAddrOptions(requestMsg));
    }
    
    /**
     * Populate addr options.
     * 
     * @param iaAddrOption the ia addr option
     * @param link the link
     */
    protected void populateAddrOptions(DhcpIaAddrOption iaAddrOption, Link link)
    {
    	iaAddrOption.putAllDhcpOptions(dhcpServerConfig.effectiveAddrOptions(requestMsg, link));
    }
	
    /**
     * Populate addr options.
     * 
     * @param iaAddrOption the ia addr option
     * @param link the link
     * @param pool the pool
     */
    protected void populateAddrOptions(DhcpIaAddrOption iaAddrOption, Link link, Pool pool)
    {
	    iaAddrOption.putAllDhcpOptions(dhcpServerConfig.effectiveAddrOptions(requestMsg, link, pool));
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
     * @return true, if successful
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
     * @return true, if successful
     */
    public abstract boolean process();
    
    /**
     * Post process.
     * 
     * @return true, if successful
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
		
		long minPreferredLifetime = 0xffffffff;
		Collection<BindingAddress> bindingAddrs = binding.getBindingAddresses();
		if ((bindingAddrs != null) && !bindingAddrs.isEmpty()) {
			List<DhcpIaAddrOption> dhcpIaAddrOptions = new ArrayList<DhcpIaAddrOption>(); 
			for (BindingAddress bindingAddr : bindingAddrs) {
				DhcpIaAddrOption dhcpIaAddrOption = new DhcpIaAddrOption();
				IaAddrOption iaAddrOption = dhcpIaAddrOption.getIaAddrOption();
				InetAddress inetAddr = bindingAddr.getIpAddress();
				if (inetAddr != null) {
					iaAddrOption.setIpv6Address(inetAddr.getHostAddress());
					BindingPool bp = bindingAddr.getBindingPool();
					if (bp != null) {
						long preferred = bp.getPreferredLifetime();
						if ((minPreferredLifetime == 0xffffffff) ||
								(preferred < minPreferredLifetime))  {
							minPreferredLifetime = preferred; 
						}
						iaAddrOption.setPreferredLifetime(preferred);
						iaAddrOption.setValidLifetime(bp.getValidLifetime());
						populateAddrOptions(dhcpIaAddrOption, clientLink, bp.getPool());
						dhcpIaAddrOptions.add(dhcpIaAddrOption);
						//TODO when do actually start the timer?  currently, two get
						//     created - one during advertise, one during reply
						//     policy to allow real-time expiration?
//						bp.startExpireTimerTask(bindingAddr, iaAddrOption.getValidLifetime());
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
		
		populateIaOptions(dhcpIaNaOption, clientLink);
		replyMsg.addIaNaOption(dhcpIaNaOption);
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
			long minPreferredLifetime) {
		float t1 = DhcpServerPolicies.effectivePolicyAsFloat(clientLink, Property.IA_NA_T1);
		if (t1 > 1) {
			log.debug("Setting T1 to configured number of seconds: " + t1);
			// if T1 is greater than one, then treat it as an
			// absolute value which specifies the number of seconds
			iaNaOption.setT1((long)t1);
		}
		else {
			// if T1 is less than one and greater than or equal to zero,
			// then treat is as a percentage of the minimum preferred lifetime
			// unless the minimum preferred lifetime is infinity (0xffffffff)
			if (minPreferredLifetime == 0xffffffff) {
				log.debug("Setting T1 to minPreferredLifetime of infinity: " +
						minPreferredLifetime);
				iaNaOption.setT1(minPreferredLifetime);
			} 
			else {
				 if (t1 >= 0) {	// zero means let the client decide
					 log.debug("Setting T1 to configured ratio=" + t1 +
							 " of minPreferredLifetime=" + minPreferredLifetime);
					 iaNaOption.setT1((long)(t1 * minPreferredLifetime));
				 }
				 else {
					 log.debug("Setting T1 to standard ratio=0.5" + 
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
			long minPreferredLifetime) {
		float t2 = DhcpServerPolicies.effectivePolicyAsFloat(clientLink, Property.IA_NA_T2);
		if (t2 > 1) {
			log.debug("Setting T2 to configured number of seconds: " + t2);
			iaNaOption.setT2((long)t2);
		}
		else {
			// if T2 is less than one and greater than or equal to zero,
			// then treat is as a percentage of the minimum preferred lifetime
			// unless the minimum preferred lifetime is infinity (0xffffffff)
			if (minPreferredLifetime == 0xffffffff) {
				log.debug("Setting T2 to minPreferredLifetime of infinity: " +
						minPreferredLifetime);
				iaNaOption.setT2(minPreferredLifetime);
			} 
			else {
				 if (t2 >= 0) {	// zero means let the client decide
					 log.debug("Setting T2 to configured ratio=" + t2 +
							 " of minPreferredLifetime=" + minPreferredLifetime);
					 iaNaOption.setT2((long)(t2 * minPreferredLifetime));
				 }
				 else {
					 log.debug("Setting T2 to standard ratio=0.8" + 
							 " of minPreferredLifetime=" + minPreferredLifetime);
					 iaNaOption.setT2((long)(0.8f * minPreferredLifetime));
				 }
			}
		}
		// ensure that T2 >= T1
		if (iaNaOption.getT2() < iaNaOption.getT1()) {
			log.warn("T2(" + iaNaOption.getT2() + ")" +
					" < T1(" + iaNaOption.getT1() + "): setting T2=T1");
			iaNaOption.setT2(iaNaOption.getT1());
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
						Pool p = DhcpServerConfiguration.findPool(clientLink.getLink(),
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
