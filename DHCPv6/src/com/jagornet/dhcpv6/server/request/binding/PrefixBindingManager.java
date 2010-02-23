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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.db.IaAddress;
import com.jagornet.dhcpv6.db.IaPrefix;
import com.jagornet.dhcpv6.db.IdentityAssoc;
import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpClientIdOption;
import com.jagornet.dhcpv6.option.DhcpIaPdOption;
import com.jagornet.dhcpv6.option.DhcpIaPrefixOption;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcpv6.xml.Link;
import com.jagornet.dhcpv6.xml.LinkFilter;
import com.jagornet.dhcpv6.xml.LinkFiltersType;
import com.jagornet.dhcpv6.xml.PrefixBinding;
import com.jagornet.dhcpv6.xml.PrefixBindingsType;
import com.jagornet.dhcpv6.xml.PrefixPool;
import com.jagornet.dhcpv6.xml.PrefixPoolsType;

// TODO: Auto-generated Javadoc
/**
 * The Class BindingManager.
 */
public class PrefixBindingManager 
		extends BaseBindingManager 
		implements PrefixBindingManagerInterface
{
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(PrefixBindingManager.class);
    
	public PrefixBindingManager() throws Exception
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
    
    protected List<? extends BindingPool> buildBindingPools(Link link) throws Exception
    {
		List<PrefixBindingPool> bindingPools = new ArrayList<PrefixBindingPool>();
		// Put the filtered pools first in the list of pools on this link
		LinkFiltersType linkFiltersType = link.getLinkFilters();
		if (linkFiltersType != null) {
			List<LinkFilter> linkFilters = linkFiltersType.getLinkFilterList();
			if ((linkFilters != null) && !linkFilters.isEmpty()) {
				for (LinkFilter linkFilter : linkFilters) {
					PrefixPoolsType poolsType = linkFilter.getPrefixPools();
					if (poolsType != null) {
						// add the filtered pools to the mapped list
		    			List<PrefixPool> pools = poolsType.getPoolList();
		    			if ((pools != null) && !pools.isEmpty()) {
		    				for (PrefixPool pool : pools) {
		    					PrefixBindingPool abp = buildBindingPool(pool, link, linkFilter);
								bindingPools.add(abp);
		    				}
		    			}
		    			else {
		    				log.error("PoolList is null for PoolsType:\n" + poolsType);
		    			}
					}
					else {
						log.warn("PoolsType is null for LinkFilter:\n" + linkFilter);
					}
				}
			}
		}
		PrefixPoolsType poolsType = link.getPrefixPools();
		if (poolsType != null) {
			// add the unfiltered pools to the mapped list
			List<PrefixPool> pools = poolsType.getPoolList();
			if ((pools != null) && !pools.isEmpty()) {
				for (PrefixPool pool : pools) {
					PrefixBindingPool abp = buildBindingPool(pool, link);
					bindingPools.add(abp);
				}
			}
			else {
				log.error("PoolList is null for PoolsType:\n" + poolsType);
			}
		}
		else {
			log.warn("PoolsType is null for Link:\n" + link);
		}
		
		reconcilePools(bindingPools);
		
		return bindingPools;
    }
    
    /**
     * Reconcile pools.
     * 
     * @param bindingPools the binding pools
     */
    protected void reconcilePools(List<PrefixBindingPool> bindingPools)
    {
    	if ((bindingPools != null) && !bindingPools.isEmpty()) {
    		List<Range> ranges = new ArrayList<Range>();
    		for (PrefixBindingPool bp : bindingPools) {
    			Range range = new Range(bp.getStartAddress(), bp.getEndAddress());
				ranges.add(range);
			}
        	iaPrefixDao.deleteNotInRanges(ranges);
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
    protected PrefixBindingPool buildBindingPool(PrefixPool pool, Link link) throws Exception
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
    protected PrefixBindingPool buildBindingPool(PrefixPool pool, Link link, 
    		LinkFilter linkFilter) throws Exception
    {
		PrefixBindingPool bp = new PrefixBindingPool(pool);
		long pLifetime = 
			DhcpServerPolicies.effectivePolicyAsLong(pool, link, Property.PREFERRED_LIFETIME);
		bp.setPreferredLifetime(pLifetime);
		long vLifetime = 
			DhcpServerPolicies.effectivePolicyAsLong(pool, link, Property.VALID_LIFETIME);
		bp.setValidLifetime(vLifetime);
		bp.setLinkFilter(linkFilter);
		
		List<IaPrefix> usedIps = iaPrefixDao.findAllByRange(bp.getStartAddress(), bp.getEndAddress());
		if ((usedIps != null) && !usedIps.isEmpty()) {
			for (IaPrefix ip : usedIps) {
				//TODO: for the quickest startup?...
				// set IP as used without checking if the binding has expired
				// let the reaper thread deal with all binding cleanup activity
				bp.setUsed(ip.getIpAddress());
			}
		}
    	return bp;
    }
    
    protected void initBindings(Link link) throws Exception
    {
		PrefixBindingsType bindingsType = link.getPrefixBindings();
		if (bindingsType != null) {
			List<PrefixBinding> staticBindings = 
				bindingsType.getBindingList();
			if (staticBindings != null) {
				for (PrefixBinding staticBinding : staticBindings) {
//TODO								reconcileStaticBinding(staticBinding);
					setIpAsUsed(link, 
							InetAddress.getByName(staticBinding.getPrefix()));
				}
			}
		}
    }
    
	public Binding findCurrentBinding(Link clientLink, DhcpClientIdOption clientIdOption, 
			DhcpIaPdOption iaPdOption, DhcpMessage requestMsg)
	{
		byte[] duid = clientIdOption.getDuid();;
		long iaid = iaPdOption.getIaPdOption().getIaId();
		
		return super.findCurrentBinding(clientLink, duid, IdentityAssoc.PD_TYPE, 
				iaid, requestMsg);
		
	}
	
	public Binding createSolicitBinding(Link clientLink, DhcpClientIdOption clientIdOption, 
			DhcpIaPdOption iaPdOption, DhcpMessage requestMsg, boolean rapidCommit)
	{	
		byte[] duid = clientIdOption.getDuid();;
		long iaid = iaPdOption.getIaPdOption().getIaId();
		
		List<InetAddress> requestAddrs = getInetAddrs(iaPdOption);
		
		return super.createSolicitBinding(clientLink, duid, IdentityAssoc.PD_TYPE, 
				iaid, requestAddrs, requestMsg, rapidCommit);		
	}
	
	public Binding updateBinding(Binding binding, Link clientLink, 
			DhcpClientIdOption clientIdOption, DhcpIaPdOption iaPdOption,
			DhcpMessage requestMsg, byte state)
	{
		byte[] duid = clientIdOption.getDuid();;
		long iaid = iaPdOption.getIaPdOption().getIaId();
		
		List<InetAddress> requestAddrs = getInetAddrs(iaPdOption);
		
		return super.updateBinding(binding, clientLink, duid, IdentityAssoc.PD_TYPE,
				iaid, requestAddrs, requestMsg, state);		
	}
	
	public void releaseIaPrefix(IaPrefix iaPrefix)
	{
		try {
			if (DhcpServerPolicies.globalPolicyAsBoolean(
					Property.BINDING_MANAGER_DELETE_OLD_BINDINGS)) {
				iaMgr.deleteIaPrefix(iaPrefix);
			}
			else {
				iaPrefix.setStartTime(null);
				iaPrefix.setPreferredEndTime(null);
				iaPrefix.setValidEndTime(null);
				iaPrefix.setState(IaPrefix.RELEASED);
				iaMgr.updateIaPrefix(iaPrefix);
			}
			freeAddress(iaPrefix.getIpAddress());
			//TODO: ddns update release
		}
		catch (Exception ex) {
			log.error("Failed to release address", ex);
		}
	}
	
	public void declineIaPrefix(IaPrefix iaPrefix)
	{
		try {
			iaPrefix.setStartTime(null);
			iaPrefix.setPreferredEndTime(null);
			iaPrefix.setValidEndTime(null);
			iaPrefix.setState(IaPrefix.DECLINED);
			iaMgr.updateIaPrefix(iaPrefix);
		}
		catch (Exception ex) {
			log.error("Failed to decline address", ex);
		}
	}
	
	/**
	 * Callback from the ExpireTimerTask started when the lease was granted.
	 * 
	 * @param iaPrefix the ia prefix
	 */
	public void expireIaPrefix(IaPrefix iaPrefix)
	{
		try {
			if (DhcpServerPolicies.globalPolicyAsBoolean(
					Property.BINDING_MANAGER_DELETE_OLD_BINDINGS)) {
				log.debug("Deleting expired prefix: " + iaPrefix.getIpAddress());
				iaMgr.deleteIaPrefix(iaPrefix);
			}
			else {
				iaPrefix.setStartTime(null);
				iaPrefix.setPreferredEndTime(null);
				iaPrefix.setValidEndTime(null);
				iaPrefix.setState(IaPrefix.EXPIRED);
				log.debug("Updating expired prefix: " + iaPrefix.getIpAddress());
				iaMgr.updateIaPrefix(iaPrefix);
			}
			freeAddress(iaPrefix.getIpAddress());
			//TODO: ddns update release
		}
		catch (Exception ex) {
			log.error("Failed to expire address", ex);
		}
	}
	
	/**
	 * Callback from the RepearTimerTask started when the BindingManager initialized.
	 */
	public void expirePrefixes()
	{
		List<IaPrefix> expiredPrefs = iaPrefixDao.findAllOlderThan(new Date());
		if ((expiredPrefs != null) && !expiredPrefs.isEmpty()) {
			for (IaPrefix iaPrefix : expiredPrefs) {
				expireIaPrefix(iaPrefix);
			}
		}
	}
	
	private List<InetAddress> getInetAddrs(DhcpIaPdOption iaPdOption)
	{
		List<InetAddress> inetAddrs = null;
		List<DhcpIaPrefixOption> iaPrefs = iaPdOption.getIaPrefixOptions();
		if ((iaPrefs != null) && !iaPrefs.isEmpty()) {
			inetAddrs = new ArrayList<InetAddress>();
			for (DhcpIaPrefixOption iaPrefix : iaPrefs) {
				InetAddress inetAddr = iaPrefix.getInetAddress();
				inetAddrs.add(inetAddr);
			}
		}
		return inetAddrs;
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
		Collection<? extends IaAddress> iaPrefs = ia.getIaAddresses();
		if ((iaPrefs != null) && !iaPrefs.isEmpty()) {
			List<BindingPrefix> bindingPrefixes = new ArrayList<BindingPrefix>();
			for (IaAddress iaAddr : iaPrefs) {
				BindingPrefix bindingPrefix = 
					buildBindingAddrFromIaPrefix((IaPrefix)iaAddr, clientLink, requestMsg);
				if (bindingPrefix != null)
					bindingPrefixes.add(bindingPrefix);
			}
			// replace the collection of IaPrefixes with BindingPrefixes
			binding.setIaAddresses(bindingPrefixes);
		}
		return binding;
	}

	/**
	 * Create a BindingPrefix given an IaPrefix loaded from the database.
	 * 
	 * @param iaPrefix the ia prefix
	 * @param clientLink the client link
	 * @param requestMsg the request msg
	 * 
	 * @return the binding address
	 */
	private BindingPrefix buildBindingAddrFromIaPrefix(IaPrefix iaPrefix, 
			Link clientLink, DhcpMessage requestMsg)
	{
		InetAddress inetAddr = iaPrefix.getIpAddress();
		BindingPool bp = findBindingPool(clientLink, inetAddr, requestMsg);
		if (bp != null) {
			bp.setUsed(inetAddr);	// TODO check if this is necessary
			// TODO store the configured options in the persisted binding?
			// ipAddr.setDhcpOptions(bp.getDhcpOptions());
			return new BindingPrefix(iaPrefix, (PrefixBindingPool)bp);
		}
		else {
			log.error("Failed to create BindingPrefix: No BindingPool found for IP=" + 
					inetAddr.getHostAddress());
		}
		// MUST have a BindingPool, otherwise something's broke
		return null;
	}
	
	/**
	 * Build a BindingPrefix for the given InetAddress and Link.
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
		PrefixBindingPool bp = (PrefixBindingPool) findBindingPool(clientLink, inetAddr, requestMsg);
		if (bp != null) {
			bp.setUsed(inetAddr);	// TODO check if this is necessary
			IaPrefix iaPrefix = new IaPrefix();
			iaPrefix.setIpAddress(inetAddr);
			iaPrefix.setPrefixLength((short)bp.getAllocPrefixLen());
			BindingPrefix bindingPrefix = new BindingPrefix(iaPrefix, bp);
			setBindingObjectTimes(bindingPrefix, 
					bp.getPreferredLifetimeMs(), bp.getPreferredLifetimeMs());
			// TODO store the configured options in the persisted binding?
			// bindingPrefix.setDhcpOptions(bp.getDhcpOptions());
			return bindingPrefix;
		}
		else {
			log.error("Failed to create BindingPrefix: No BindingPool found for IP=" + 
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
			expirePrefixes();
		}
	}
}
