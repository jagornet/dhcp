/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file V6PrefixBindingManagerImpl.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.server.request.binding;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.message.DhcpMessage;
import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.option.v6.DhcpV6ClientIdOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaPdOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaPrefixOption;
import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.config.DhcpServerConfigException;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.config.xml.Link;
import com.jagornet.dhcp.server.config.xml.LinkFilter;
import com.jagornet.dhcp.server.config.xml.LinkFiltersType;
import com.jagornet.dhcp.server.config.xml.V6PrefixBinding;
import com.jagornet.dhcp.server.config.xml.V6PrefixBindingsType;
import com.jagornet.dhcp.server.config.xml.V6PrefixPool;
import com.jagornet.dhcp.server.config.xml.V6PrefixPoolsType;
import com.jagornet.dhcp.server.db.DhcpOption;
import com.jagornet.dhcp.server.db.IaAddress;
import com.jagornet.dhcp.server.db.IaPrefix;
import com.jagornet.dhcp.server.db.IdentityAssoc;

/**
 * The Class PrefixBindingManagerImpl.
 * 
 * @author A. Gregory Rabil
 */
public class V6PrefixBindingManagerImpl 
		extends BaseBindingManager 
		implements V6PrefixBindingManager
{
	private static Logger log = LoggerFactory.getLogger(V6PrefixBindingManagerImpl.class);
    
	/**
	 * Instantiates a new prefix binding manager impl.
	 */
	public V6PrefixBindingManagerImpl()
	{
		super();
	}
	
	protected void startReaper()
	{
		//TODO: separate properties for address/prefix binding managers?
		long reaperStartupDelay = 
			DhcpServerPolicies.globalPolicyAsLong(Property.BINDING_MANAGER_REAPER_STARTUP_DELAY);
		long reaperRunPeriod =
			DhcpServerPolicies.globalPolicyAsLong(Property.BINDING_MANAGER_REAPER_RUN_PERIOD);

		reaper = new Timer("BindingReaper");
		reaper.schedule(new ReaperTimerTask(), reaperStartupDelay, reaperRunPeriod);
	}
    
	protected void stopReaper()
	{
		reaper.cancel();
		reaper.purge();
	}
    
    /**
     * Build the list of PrefixBindingPools from the list of configured PrefixPools
     * for the given configuration Link container object. The list of PrefixBindingPools
     * starts with the filtered PrefixPools followed by non-filtered PrefixPools.
     * 
     * @param link the configuration Link object
     * 
     * @return the list of PrefixBindingPools (<? extends BindingPool>)
     * 
     * @throws DhcpServerConfigException if there is a problem parsing a configured range
     */
    protected List<? extends BindingPool> buildBindingPools(Link link) 
    		throws DhcpServerConfigException
    {
		List<V6PrefixBindingPool> bindingPools = new ArrayList<V6PrefixBindingPool>();
		// Put the filtered pools first in the list of pools on this link
		LinkFiltersType linkFiltersType = link.getLinkFilters();
		if (linkFiltersType != null) {
			List<LinkFilter> linkFilters = linkFiltersType.getLinkFilterList();
			if ((linkFilters != null) && !linkFilters.isEmpty()) {
				for (LinkFilter linkFilter : linkFilters) {
					V6PrefixPoolsType poolsType = linkFilter.getV6PrefixPools();
					if (poolsType != null) {
						// add the filtered pools to the mapped list
		    			List<V6PrefixPool> pools = poolsType.getPoolList();
		    			if ((pools != null) && !pools.isEmpty()) {
		    				for (V6PrefixPool pool : pools) {
		    					V6PrefixBindingPool abp = buildV6BindingPool(pool, link, linkFilter);
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
		V6PrefixPoolsType poolsType = link.getV6PrefixPools();
		if (poolsType != null) {
			// add the unfiltered pools to the mapped list
			List<V6PrefixPool> pools = poolsType.getPoolList();
			if ((pools != null) && !pools.isEmpty()) {
				for (V6PrefixPool pool : pools) {
					V6PrefixBindingPool abp = buildV6BindingPool(pool, link);
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
		
		reconcilePools(bindingPools);
		
		return bindingPools;
    }
    
    /**
     * Reconcile pools.  Delete any IaAddress objects not contained
     * within the given list of PrefixBindingPools.
     * 
     * @param bindingPools the list of PrefixBindingPools
     */
    protected void reconcilePools(List<V6PrefixBindingPool> bindingPools)
    {
    	if ((bindingPools != null) && !bindingPools.isEmpty()) {
    		List<Range> ranges = new ArrayList<Range>();
    		for (V6PrefixBindingPool bp : bindingPools) {
    			Range range = new Range(bp.getStartAddress(), bp.getEndAddress());
				ranges.add(range);
			}
        	iaMgr.reconcileIaAddresses(ranges);
    	}
    }
    
    /**
     * Builds a binding pool from an PrefixPool using the given link.
     * 
     * @param pool the PrefixPool to wrap as an PrefixBindingPool
     * @param link the link
     * 
     * @return the binding pool
     * 
     * @throws DhcpServerConfigException if there is a problem parsing the configured range
     */
    protected V6PrefixBindingPool buildV6BindingPool(V6PrefixPool pool, Link link) 
    		throws DhcpServerConfigException
    {
    	return buildV6BindingPool(pool, link, null);
    }

    /**
     * Builds a binding pool from an PrefixPool using the given link and filter.
     * 
     * @param pool the AddressPool to wrap as an PrefixBindingPool
     * @param link the link
     * @param linkFilter the link filter
     * 
     * @return the binding pool
     * 
     * @throws DhcpServerConfigException if there is a problem parsing the configured range
     */
    protected V6PrefixBindingPool buildV6BindingPool(V6PrefixPool pool, Link link, 
    		LinkFilter linkFilter) throws DhcpServerConfigException
    {
		V6PrefixBindingPool bp = new V6PrefixBindingPool(pool);
		long pLifetime = 
			DhcpServerPolicies.effectivePolicyAsLong(bp, link, Property.V6_PREFERRED_LIFETIME);
		bp.setPreferredLifetime(pLifetime);
		long vLifetime = 
			DhcpServerPolicies.effectivePolicyAsLong(bp, link, Property.V6_VALID_LIFETIME);
		bp.setValidLifetime(vLifetime);
		bp.setLinkFilter(linkFilter);
		
		List<InetAddress> usedIps = iaMgr.findExistingIPs(bp.getStartAddress(), bp.getEndAddress());
		if ((usedIps != null) && !usedIps.isEmpty()) {
			for (InetAddress ip : usedIps) {
				//TODO: for the quickest startup?...
				// set IP as used without checking if the binding has expired
				// let the reaper thread deal with all binding cleanup activity
				bp.setUsed(ip);
			}
		}
		log.info("Built prefix binding pool: " + bp.getStartAddress().getHostAddress() + "-" + 
				bp.getEndAddress().getHostAddress() + ", size=" + bp.getSize());
    	return bp;
    }
    
    protected List<? extends StaticBinding> buildStaticBindings(Link link) 
			throws DhcpServerConfigException
	{
		List<V6StaticPrefixBinding> staticBindings = new ArrayList<V6StaticPrefixBinding>();
		V6PrefixBindingsType bindingsType = link.getV6PrefixBindings();
		if (bindingsType != null) {
			List<V6PrefixBinding> bindings = bindingsType.getBindingList();
			if ((bindings != null) && !bindings.isEmpty()) {
				for (V6PrefixBinding binding : bindings) {
					V6StaticPrefixBinding spb = buildStaticBinding(binding, link);
					staticBindings.add(spb);
				}
			}
		}
		
		return staticBindings;
	}
    
    protected V6StaticPrefixBinding buildStaticBinding(V6PrefixBinding binding, Link link) 
    		throws DhcpServerConfigException
    {
    	try {
    		InetAddress inetAddr = InetAddress.getByName(binding.getPrefix());
    		V6StaticPrefixBinding sb = new V6StaticPrefixBinding(binding);
    		setIpAsUsed(link, inetAddr);
    		return sb;
    	}
    	catch (UnknownHostException ex) {
    		log.error("Invalid static binding address", ex);
    		throw new DhcpServerConfigException("Invalid static binding address", ex);
    	}
    }
    
	public Binding findCurrentBinding(DhcpLink clientLink, DhcpV6ClientIdOption clientIdOption, 
			DhcpV6IaPdOption iaPdOption, DhcpMessage requestMsg)
	{
		byte[] duid = clientIdOption.getDuid();
		long iaid = iaPdOption.getIaId();
		
		return super.findCurrentBinding(clientLink, duid, IdentityAssoc.PD_TYPE, 
				iaid, requestMsg);
		
	}
	
	public Binding createSolicitBinding(DhcpLink clientLink, DhcpV6ClientIdOption clientIdOption, 
			DhcpV6IaPdOption iaPdOption, DhcpMessage requestMsg, byte state)
	{	
		byte[] duid = clientIdOption.getDuid();
		long iaid = iaPdOption.getIaId();

		StaticBinding staticBinding = 
			findStaticBinding(clientLink.getLink(), duid, IdentityAssoc.PD_TYPE, iaid, requestMsg);
		
		if (staticBinding != null) {
			return super.createStaticBinding(clientLink, duid, IdentityAssoc.PD_TYPE, 
					iaid, staticBinding, requestMsg);
		}
		else {
			return super.createBinding(clientLink, duid, IdentityAssoc.PD_TYPE, 
					iaid, getInetAddrs(iaPdOption), requestMsg, state);
		}
	}
	
	public Binding updateBinding(Binding binding, DhcpLink clientLink, 
			DhcpV6ClientIdOption clientIdOption, DhcpV6IaPdOption iaPdOption,
			DhcpMessage requestMsg, byte state)
	{
		byte[] duid = clientIdOption.getDuid();
		long iaid = iaPdOption.getIaId();

		StaticBinding staticBinding = 
			findStaticBinding(clientLink.getLink(), duid, IdentityAssoc.PD_TYPE, iaid, requestMsg);
		
		if (staticBinding != null) {
			return super.updateStaticBinding(binding, clientLink, duid, IdentityAssoc.PD_TYPE, 
					iaid, staticBinding, requestMsg);
		}
		else {
			return super.updateBinding(binding, clientLink, duid, IdentityAssoc.PD_TYPE,
					iaid, getInetAddrs(iaPdOption), requestMsg, state);
		}
	}
	
	public void releaseIaPrefix(IaPrefix iaPrefix)
	{
		try {
			if (DhcpServerPolicies.globalPolicyAsBoolean(
					Property.BINDING_MANAGER_DELETE_OLD_BINDINGS)) {
				iaMgr.deleteIaPrefix(iaPrefix);
				// free the prefix only if it is deleted from the db,
				// otherwise, we will get a unique constraint violation
				// if another client obtains this released prefix
				freeAddress(iaPrefix.getIpAddress());
			}
			else {
				iaPrefix.setStartTime(null);
				iaPrefix.setPreferredEndTime(null);
				iaPrefix.setValidEndTime(null);
				iaPrefix.setState(IaPrefix.AVAILABLE);
				iaMgr.updateIaPrefix(iaPrefix);
			}
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
	 * NOT CURRENTLY USED
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
				// free the prefix only if it is deleted from the db,
				// otherwise, we will get a unique constraint violation
				// if another client obtains this released prefix
				freeAddress(iaPrefix.getIpAddress());
			}
			else {
				iaPrefix.setStartTime(null);
				iaPrefix.setPreferredEndTime(null);
				iaPrefix.setValidEndTime(null);
				iaPrefix.setState(IaPrefix.AVAILABLE);
				log.debug("Updating expired prefix: " + iaPrefix.getIpAddress());
				iaMgr.updateIaPrefix(iaPrefix);
			}
		}
		catch (Exception ex) {
			log.error("Failed to expire address", ex);
		}
	}
	
	/**
	 * Callback from the RepearTimerTask started when the BindingManager initialized.
	 * Find any expired prefixes as of now, and expire them already.
	 */
	public void expirePrefixes()
	{
		List<IaPrefix> expiredPrefs = iaMgr.findExpiredIaPrefixes();
		if ((expiredPrefs != null) && !expiredPrefs.isEmpty()) {
			log.info("Found " + expiredPrefs.size() + " expired prefix bindings"); 
			for (IaPrefix iaPrefix : expiredPrefs) {
				expireIaPrefix(iaPrefix);
			}
		}
	}
	
	/**
	 * Extract the list of IP addresses from within the given IA_PD option.
	 * 
	 * @param iaNaOption the IA_PD option
	 * 
	 * @return the list of InetAddresses for the IPs in the IA_PD option
	 */
	private List<InetAddress> getInetAddrs(DhcpV6IaPdOption iaPdOption)
	{
		List<InetAddress> inetAddrs = null;
		List<DhcpV6IaPrefixOption> iaPrefs = iaPdOption.getIaPrefixOptions();
		if ((iaPrefs != null) && !iaPrefs.isEmpty()) {
			inetAddrs = new ArrayList<InetAddress>();
			for (DhcpV6IaPrefixOption iaPrefix : iaPrefs) {
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
			DhcpLink clientLink, DhcpMessage requestMsg)
	{
		Binding binding = new Binding(ia, clientLink);
		Collection<? extends IaAddress> iaPrefs = ia.getIaAddresses();
		if ((iaPrefs != null) && !iaPrefs.isEmpty()) {
			List<V6BindingPrefix> bindingPrefixes = new ArrayList<V6BindingPrefix>();
			for (IaAddress iaAddr : iaPrefs) {
// off-link check needed only for v4?
//				if (!clientLink.getSubnet().contains(iaAddr.getIpAddress())) {
//					log.info("Ignoring off-link binding address: " + 
//							iaAddr.getIpAddress().getHostAddress());
//					continue;
//				}
				V6BindingPrefix bindingPrefix = null;
        		StaticBinding staticBinding =
        			findStaticBinding(clientLink.getLink(), ia.getDuid(), 
        					ia.getIatype(), ia.getIaid(), requestMsg);
        		if (staticBinding != null) {
        			bindingPrefix = 
        				buildV6BindingPrefixFromIaPrefix((IaPrefix)iaAddr, staticBinding);
        		}
        		else {
        			bindingPrefix =
        				buildBindingAddrFromIaPrefix((IaPrefix)iaAddr, clientLink, requestMsg);
        		}
				if (bindingPrefix != null)
					bindingPrefixes.add(bindingPrefix);
			}
			// replace the collection of IaPrefixes with BindingPrefixes
			binding.setIaAddresses(bindingPrefixes);
		}
		else {
			log.warn("IA has no prefixes, binding is empty.");
		}
		return binding;
	}

	/**
	 * Build a V6BindingPrefix from an IaPrefix loaded from the database.
	 * 
	 * @param iaPrefix the ia prefix
	 * @param clientLink the client link
	 * @param requestMsg the request msg
	 * 
	 * @return the binding address
	 */
	private V6BindingPrefix buildBindingAddrFromIaPrefix(IaPrefix iaPrefix, 
			DhcpLink clientLink, DhcpMessage requestMsg)
	{
		InetAddress inetAddr = iaPrefix.getIpAddress();
		V6PrefixBindingPool bp = (V6PrefixBindingPool)
				findBindingPool(clientLink.getLink(), inetAddr, requestMsg);
		if (bp != null) {
			// binding loaded from DB will contain the stored options
			V6BindingPrefix bindingPrefix = new V6BindingPrefix(iaPrefix, (V6PrefixBindingPool)bp);
	    	// TODO: setBindingObjectTimes?  see buildBindingObject
			// update the options with whatever may now be configured
	    	setDhcpOptions(bindingPrefix, clientLink, (DhcpV6Message)requestMsg, bp);
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
	 * Build a BindingPrefix from an IaAddress loaded from the database
	 * and a static binding for the client request.
	 * 
	 * @param iaPrefix
	 * @param staticBinding
	 * @return
	 */
	private V6BindingPrefix buildV6BindingPrefixFromIaPrefix(IaPrefix iaPrefix, 
			StaticBinding staticBinding)
	{
		V6BindingPrefix bindingPrefix = new V6BindingPrefix(iaPrefix, staticBinding);
		return bindingPrefix;
	}
	
	/**
	 * Created a new V6BindingPrefix type BindingObject
	 * for the given InetAddress and Link.
	 * 
	 * @param inetAddr the inet addr
	 * @param clientLink the client link
	 * @param requestMsg the request msg
	 * 
	 * @return the binding address
	 */
	protected BindingObject createBindingObject(InetAddress inetAddr, 
			DhcpLink clientLink, DhcpMessage requestMsg)
	{
		V6PrefixBindingPool bp = 
			(V6PrefixBindingPool) findBindingPool(clientLink.getLink(), inetAddr, requestMsg);
		if (bp != null) {
			bp.setUsed(inetAddr);	// TODO check if this is necessary
			IaPrefix iaPrefix = new IaPrefix();
			iaPrefix.setIpAddress(inetAddr);
			iaPrefix.setPrefixLength((short)bp.getAllocPrefixLen());
			V6BindingPrefix bindingPrefix = new V6BindingPrefix(iaPrefix, bp);
			setBindingObjectTimes(bindingPrefix, 
					bp.getPreferredLifetimeMs(), bp.getPreferredLifetimeMs());
			setDhcpOptions(bindingPrefix, clientLink, (DhcpV6Message)requestMsg, bp);
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
	 * Set both the map of options to be returned to the client and those
	 * same options as a list to be stored with the lease in the database
	 * 
	 * @param bindingAddr
	 * @param clientLink
	 * @param requestMsg
	 * @param bp
	 */
	protected void setDhcpOptions(V6BindingPrefix bindingPrefix, DhcpLink clientLink,
			DhcpV6Message requestMsg, V6PrefixBindingPool bp) {
		// set the options to be returned to the client
		Map<Integer, com.jagornet.dhcp.core.option.base.DhcpOption> effectiveDhcpOptionMap =
				buildDhcpOptions(clientLink, requestMsg, bp);
		bindingPrefix.setDhcpOptionMap(effectiveDhcpOptionMap);
		// convert the "configured" options to options to be stored in lease database
		Collection<DhcpOption> dhcpOptions = convertDhcpOptions(effectiveDhcpOptionMap);
		bindingPrefix.setDhcpOptions(dhcpOptions);
	}
	
	/**
	 * Build the map of DHCP options to be returned to the client, which consists
	 * of the configured options, filtered by any requested options, if applicable
	 * 
	 * @param clientLink
	 * @param requestMsg
	 * @param bp
	 * @return
	 */
	protected Map<Integer, com.jagornet.dhcp.core.option.base.DhcpOption> buildDhcpOptions(
			DhcpLink clientLink, DhcpV6Message requestMsg, V6PrefixBindingPool bp) {
		
		Map<Integer, com.jagornet.dhcp.core.option.base.DhcpOption> configOptionMap = 
				serverConfig.effectiveMsgOptions((DhcpV6Message)requestMsg, clientLink, bp);
		
    	if (DhcpServerPolicies.effectivePolicyAsBoolean(requestMsg,
    			clientLink.getLink(), Property.DHCP_SEND_REQUESTED_OPTIONS_ONLY)) {
    		log.debug("buildDhcpOptions: configured to include only requested options");
    		configOptionMap = requestedOptions(configOptionMap, requestMsg);
    	}
    	
		return configOptionMap;
	}
	
	protected Map<Integer, com.jagornet.dhcp.core.option.base.DhcpOption> buildIaDhcpOptions(
			DhcpLink clientLink, DhcpV6Message requestMsg, V6PrefixBindingPool bp) {
		
		Map<Integer, com.jagornet.dhcp.core.option.base.DhcpOption> configOptionMap = 
				serverConfig.effectiveIaPdOptions((DhcpV6Message)requestMsg, clientLink, bp);
		
    	if (DhcpServerPolicies.effectivePolicyAsBoolean(requestMsg,
    			clientLink.getLink(), Property.DHCP_SEND_REQUESTED_OPTIONS_ONLY)) {
    		log.debug("buildDhcpOptions: configured to include only requested options");
    		configOptionMap = requestedOptions(configOptionMap, requestMsg);
    	}
    	
		return configOptionMap;
	}

	protected Map<Integer, com.jagornet.dhcp.core.option.base.DhcpOption> buildIaAddrDhcpOptions(
			DhcpLink clientLink, DhcpV6Message requestMsg, V6PrefixBindingPool bp) {
		
		Map<Integer, com.jagornet.dhcp.core.option.base.DhcpOption> configOptionMap = 
				serverConfig.effectivePrefixOptions((DhcpV6Message)requestMsg, clientLink, bp);
		
    	if (DhcpServerPolicies.effectivePolicyAsBoolean(requestMsg,
    			clientLink.getLink(), Property.DHCP_SEND_REQUESTED_OPTIONS_ONLY)) {
    		log.debug("buildDhcpOptions: configured to include only requested options");
    		configOptionMap = requestedOptions(configOptionMap, requestMsg);
    	}
    	
		return configOptionMap;
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
			log.debug("Looking for expired prefixes...");
			expirePrefixes();
		}
	}
}
