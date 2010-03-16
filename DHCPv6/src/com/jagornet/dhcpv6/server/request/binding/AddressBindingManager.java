/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BindingManager.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.server.request.binding;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.db.DhcpOption;
import com.jagornet.dhcpv6.db.IaAddress;
import com.jagornet.dhcpv6.db.IdentityAssoc;
import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpClientFqdnOption;
import com.jagornet.dhcpv6.server.config.DhcpLink;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcpv6.server.request.ddns.DdnsUpdater;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.xml.AddressBinding;
import com.jagornet.dhcpv6.xml.AddressBindingsType;
import com.jagornet.dhcpv6.xml.AddressPool;
import com.jagornet.dhcpv6.xml.AddressPoolsType;
import com.jagornet.dhcpv6.xml.DomainNameOptionType;
import com.jagornet.dhcpv6.xml.Link;
import com.jagornet.dhcpv6.xml.LinkFilter;
import com.jagornet.dhcpv6.xml.LinkFiltersType;

/**
 * The Class BindingManager.
 */
public abstract class AddressBindingManager extends BaseBindingManager
{
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(AddressBindingManager.class);
    
	public AddressBindingManager() throws Exception
	{
		super();

		//TODO: separate properties for address/prefix binding managers?
		long reaperStartupDelay = 
			DhcpServerPolicies.globalPolicyAsLong(Property.BINDING_MANAGER_REAPER_STARTUP_DELAY);
		long reaperRunPeriod =
			DhcpServerPolicies.globalPolicyAsLong(Property.BINDING_MANAGER_REAPER_RUN_PERIOD);

		reaper = new Timer("BindingReaper");
		reaper.schedule(new ReaperTimerTask(), reaperStartupDelay, reaperRunPeriod);
	}
    
    protected abstract AddressPoolsType getAddressPoolsType(LinkFilter linkFilter);
    protected abstract AddressPoolsType getAddressPoolsType(Link link);
    
    /**
     * Build the list of AddressBindingPools from the list of configured AddressPools
     * for the given configuration Link container object. The list of AddressBindingPools
     * starts with the filtered AddressPools followed by non-filtered AddressPools.
     * 
     * @param link the configuration Link object
     * 
     * @return the list of AddressBindingPools (<? extends BindingPool>)
     * 
     * @throws Exception if any problem occurs
     */
    protected List<? extends BindingPool> buildBindingPools(Link link) throws Exception
    {
		List<AddressBindingPool> bindingPools = new ArrayList<AddressBindingPool>();
		// Put the filtered pools first in the list of pools on this link
		LinkFiltersType linkFiltersType = link.getLinkFilters();
		if (linkFiltersType != null) {
			List<LinkFilter> linkFilters = linkFiltersType.getLinkFilterList();
			if ((linkFilters != null) && !linkFilters.isEmpty()) {
				for (LinkFilter linkFilter : linkFilters) {
					AddressPoolsType poolsType = getAddressPoolsType(linkFilter);
					if (poolsType != null) {
						// add the filtered pools to the mapped list
		    			List<AddressPool> pools = poolsType.getPoolList();
		    			if ((pools != null) && !pools.isEmpty()) {
		    				for (AddressPool pool : pools) {
		    					AddressBindingPool abp = buildBindingPool(pool, link, linkFilter);
								bindingPools.add(abp);
		    				}
		    			}
		    			else {
		    				log.error("PoolList is null for PoolsType: " + poolsType);
		    			}
					}
					else {
						log.info("PoolsType is null for LinkFilter: " + linkFilter.getName());
					}
				}
			}
		}
		AddressPoolsType poolsType = getAddressPoolsType(link);
		if (poolsType != null) {
			// add the unfiltered pools to the mapped list
			List<AddressPool> pools = poolsType.getPoolList();
			if ((pools != null) && !pools.isEmpty()) {
				for (AddressPool pool : pools) {
					AddressBindingPool abp = buildBindingPool(pool, link);
					bindingPools.add(abp);
				}
			}
			else {
				log.error("PoolList is null for PoolsType: " + poolsType);
			}
		}
		else {
			log.info("PoolsType is null for Link: " + link.getName());
		}

//TODO this is very dangerous if the server is managing
//	    both NA and TA address pools because we'd delete
//	    all the addresses in pools of the other type
//		reconcilePools(bindingPools);
		
		return bindingPools;
    }
    
    /**
     * Reconcile pools.  Delete any IaAddress objects not contained
     * within the given list of AddressBindingPools.
     * 
     * @param bindingPools the list of AddressBindingPools
     */
    protected void reconcilePools(List<AddressBindingPool> bindingPools)
    {
    	if ((bindingPools != null) && !bindingPools.isEmpty()) {
    		List<Range> ranges = new ArrayList<Range>();
    		for (AddressBindingPool bp : bindingPools) {
				ranges.add(bp.getRange());
			}
        	iaAddrDao.deleteNotInRanges(ranges);
    	}
    }
    
    /**
     * Inits the binding pool.
     * 
     * @param pool the pool
     * @param link the link
     * 
     * @return the binding pool
     * 
     * @throws Exception the exception
     */
    protected AddressBindingPool buildBindingPool(AddressPool pool, Link link) throws Exception
    {
    	return buildBindingPool(pool, link, null);
    }

    /**
     * Inits the binding pool.
     * 
     * @param pool the pool
     * @param link the link
     * @param linkFilter the link filter
     * 
     * @return the binding pool
     * 
     * @throws Exception the exception
     */
    protected AddressBindingPool buildBindingPool(AddressPool pool, Link link, 
    		LinkFilter linkFilter) throws Exception
    {
		AddressBindingPool bp = new AddressBindingPool(pool);
		long pLifetime = 
			DhcpServerPolicies.effectivePolicyAsLong(pool, link, Property.PREFERRED_LIFETIME);
		bp.setPreferredLifetime(pLifetime);
		long vLifetime = 
			DhcpServerPolicies.effectivePolicyAsLong(pool, link, Property.VALID_LIFETIME);
		bp.setValidLifetime(vLifetime);
		bp.setLinkFilter(linkFilter);
		
		List<IaAddress> usedIps = iaAddrDao.findAllByRange(bp.getStartAddress(), bp.getEndAddress());
		if ((usedIps != null) && !usedIps.isEmpty()) {
			for (IaAddress ip : usedIps) {
				//TODO: for the quickest startup?...
				// set IP as used without checking if the binding has expired
				// let the reaper thread deal with all binding cleanup activity
				bp.setUsed(ip.getIpAddress());
			}
		}
    	return bp;
    }
    
    protected abstract AddressBindingsType getAddressBindingsType(Link link);
    
    protected void initBindings(Link link) throws Exception
    {
		AddressBindingsType bindingsType = getAddressBindingsType(link);
		if (bindingsType != null) {
			List<AddressBinding> staticBindings = 
				bindingsType.getBindingList();
			if (staticBindings != null) {
				for (AddressBinding staticBinding : staticBindings) {
//TODO								reconcileStaticBinding(staticBinding);
					setIpAsUsed(link, 
							InetAddress.getByName(staticBinding.getIpAddress()));
				}
			}
		}
    }
    
    protected void ddnsDelete(IaAddress iaAddr)
    {
    	DhcpClientFqdnOption clientFqdnOption = null;
    	try {
	    	long identityAssocId = iaAddr.getIdentityAssocId();
	    	IdentityAssoc ia = iaMgr.getIA(identityAssocId);
	    	if (ia != null) {
	    		List<DhcpOption> opts = optDao.findAllByIdentityAssocId(identityAssocId);
	    		if (opts != null) {
	    			for (DhcpOption opt : opts) {
	    				if (opt.getCode() == DhcpConstants.OPTION_CLIENT_FQDN) {
	    					clientFqdnOption = new DhcpClientFqdnOption();
	    					clientFqdnOption.decode(ByteBuffer.wrap(opt.getValue()));
	    					break;
	    				}
	    			}
	    		}
	    		if (clientFqdnOption != null) {
					DomainNameOptionType domainNameOption = 
						clientFqdnOption.getDomainNameOption();
					if (domainNameOption != null) {
						String fqdn = domainNameOption.getDomainName();
						if ((fqdn != null) && !fqdn.isEmpty()) {
				        	DhcpLink link = serverConfig.findLinkForAddress(iaAddr.getIpAddress());
				        	if (link != null) {
				        		BindingAddress bindingAddr =
				        			buildBindingAddrFromIaAddr(iaAddr, link.getLink(), null);	// safe to send null requestMsg
								DdnsUpdater ddns =
									new DdnsUpdater(link.getLink(), bindingAddr, fqdn,
											ia.getDuid(), clientFqdnOption.getUpdateAaaaBit(), true);
								ddns.processUpdates();
				        	}
				        	else {
				        		log.error("Failed to find link for binding address: " + 
				        				iaAddr.getIpAddress());
				        	}
						}
						else {
							log.error("FQDN is null or empty.  No DDNS deletes performed.");
						}
					}
					else {
						log.error("Domain name option type null in Client FQDN." +
								"  No DDNS deletes performed.");
					}
	    		}
	    		else {
	    			log.warn("No Client FQDN option in current binding.  No DDNS deletes performed.");
	    		}
	    	}
	    	else {
	    		log.error("Failed to get IdentityAssoc id=" + identityAssocId);
	    	}
    	}
    	catch (Exception ex) {
    		log.error("Failed to perform DDNS delete", ex);
    	}
    }
	
	public void releaseIaAddress(IaAddress iaAddr)
	{
		try {
			ddnsDelete(iaAddr);
			if (DhcpServerPolicies.globalPolicyAsBoolean(
					Property.BINDING_MANAGER_DELETE_OLD_BINDINGS)) {
				iaMgr.deleteIaAddr(iaAddr);
			}
			else {
				iaAddr.setStartTime(null);
				iaAddr.setPreferredEndTime(null);
				iaAddr.setValidEndTime(null);
				iaAddr.setState(IaAddress.RELEASED);
				iaMgr.updateIaAddr(iaAddr);
			}
			freeAddress(iaAddr.getIpAddress());
		}
		catch (Exception ex) {
			log.error("Failed to release address", ex);
		}
	}
	
	public void declineIaAddress(IaAddress iaAddr)
	{
		try {
			ddnsDelete(iaAddr);
			iaAddr.setStartTime(null);
			iaAddr.setPreferredEndTime(null);
			iaAddr.setValidEndTime(null);
			iaAddr.setState(IaAddress.DECLINED);
			iaMgr.updateIaAddr(iaAddr);
		}
		catch (Exception ex) {
			log.error("Failed to decline address", ex);
		}
	}
	
	/**
	 * Callback from the ExpireTimerTask started when the lease was granted.
	 * 
	 * @param iaAddr the ia addr
	 */
	public void expireIaAddress(IaAddress iaAddr)
	{
		try {
			ddnsDelete(iaAddr);
			if (DhcpServerPolicies.globalPolicyAsBoolean(
					Property.BINDING_MANAGER_DELETE_OLD_BINDINGS)) {
				log.debug("Deleting expired address: " + iaAddr.getIpAddress());
				iaMgr.deleteIaAddr(iaAddr);
			}
			else {
				iaAddr.setStartTime(null);
				iaAddr.setPreferredEndTime(null);
				iaAddr.setValidEndTime(null);
				iaAddr.setState(IaAddress.EXPIRED);
				log.debug("Updating expired address: " + iaAddr.getIpAddress());
				iaMgr.updateIaAddr(iaAddr);
			}
			freeAddress(iaAddr.getIpAddress());
		}
		catch (Exception ex) {
			log.error("Failed to expire address", ex);
		}
	}
	
	/**
	 * Callback from the RepearTimerTask started when the BindingManager initialized.
	 */
	public void expireAddresses()
	{
		List<IaAddress> expiredAddrs = iaAddrDao.findAllOlderThan(new Date());
		if ((expiredAddrs != null) && !expiredAddrs.isEmpty()) {
			for (IaAddress iaAddress : expiredAddrs) {
				expireIaAddress(iaAddress);
			}
		}
	}
	
	/**
	 * Create a Binding given an IdentityAssoc loaded from the database.
	 * 
	 * @param ia the ia
	 * @param clientLink the client link
	 * @param requestMsg the request msg
	 * 
	 * @return the binding
	 */
	protected Binding buildBindingFromIa(IdentityAssoc ia, 
			Link clientLink, DhcpMessage requestMsg)
	{
		Binding binding = new Binding(ia, clientLink);
		Collection<? extends IaAddress> iaAddrs = ia.getIaAddresses();
		if ((iaAddrs != null) && !iaAddrs.isEmpty()) {
			List<BindingAddress> bindingAddrs = new ArrayList<BindingAddress>();
			for (IaAddress iaAddr : iaAddrs) {
				BindingAddress bindingAddr = 
					buildBindingAddrFromIaAddr(iaAddr, clientLink, requestMsg);
				if (bindingAddr != null)
					bindingAddrs.add(bindingAddr);
			}
			// replace the collection of IaAddresses with BindingAddresses
			binding.setIaAddresses(bindingAddrs);
		}
		return binding;
	}

	/**
	 * Create a BindingAddress given an IaAddress loaded from the database.
	 * 
	 * @param iaAddr the ia addr
	 * @param clientLink the client link
	 * @param requestMsg the request msg
	 * 
	 * @return the binding address
	 */
	private BindingAddress buildBindingAddrFromIaAddr(IaAddress iaAddr, 
			Link clientLink, DhcpMessage requestMsg)
	{
		InetAddress inetAddr = iaAddr.getIpAddress();
		BindingPool bp = findBindingPool(clientLink, inetAddr, requestMsg);
		if (bp != null) {
			// TODO determine if we need to set the IP as used in the
			// BindingPool.  Note that this method is also called when
			// releasing addresses.
//			bp.setUsed(inetAddr);
			// TODO store the configured options in the persisted binding?
			// ipAddr.setDhcpOptions(bp.getDhcpOptions());
			return new BindingAddress(iaAddr, (AddressBindingPool)bp);
		}
		else {
			log.error("Failed to create BindingAddress: No BindingPool found for IP=" + 
					inetAddr.getHostAddress());
		}
		// MUST have a BindingPool, otherwise something's broke
		return null;
	}
	
	/**
	 * Build a BindingAddress for the given InetAddress and Link.
	 * 
	 * @param inetAddr the inet addr
	 * @param clientLink the client link
	 * @param requestMsg the request msg
	 * 
	 * @return the binding address
	 */
	protected BindingObject buildBindingObject(InetAddress inetAddr, 
			Link clientLink, DhcpMessage requestMsg)
	{
		AddressBindingPool bp = (AddressBindingPool) findBindingPool(clientLink, inetAddr, requestMsg);
		if (bp != null) {
			bp.setUsed(inetAddr);	// TODO check if this is necessary
			IaAddress iaAddr = new IaAddress();
			iaAddr.setIpAddress(inetAddr);
			BindingAddress bindingAddr = new BindingAddress(iaAddr, bp);
			setBindingObjectTimes(bindingAddr, 
					bp.getPreferredLifetimeMs(), bp.getPreferredLifetimeMs());
			// TODO store the configured options in the persisted binding?
			// bindingAddr.setDhcpOptions(bp.getDhcpOptions());
			return bindingAddr;
		}
		else {
			log.error("Failed to create BindingAddress: No BindingPool found for IP=" + 
					inetAddr.getHostAddress());
		}
		// MUST have a BindingPool, otherwise something's broke
		return null;
	}
	
	/**
	 * The Class ReaperTimerTask.
	 */
	class ReaperTimerTask extends TimerTask
	{		
		/* (non-Javadoc)
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run() {
//			log.debug("Expiring addresses...");
			expireAddresses();
		}
	}
}
