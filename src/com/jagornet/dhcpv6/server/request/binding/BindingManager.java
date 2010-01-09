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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jagornet.dhcpv6.db.DhcpOptionDAO;
import com.jagornet.dhcpv6.db.IaAddress;
import com.jagornet.dhcpv6.db.IaAddressDAO;
import com.jagornet.dhcpv6.db.IaManager;
import com.jagornet.dhcpv6.db.IdentityAssoc;
import com.jagornet.dhcpv6.db.IdentityAssocDAO;
import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpClientIdOption;
import com.jagornet.dhcpv6.option.DhcpIaAddrOption;
import com.jagornet.dhcpv6.option.DhcpIaNaOption;
import com.jagornet.dhcpv6.server.config.DhcpLink;
import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcpv6.util.Subnet;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.BindingsType;
import com.jagornet.dhcpv6.xml.IaAddrOption;
import com.jagornet.dhcpv6.xml.IaAddrOptionListType;
import com.jagornet.dhcpv6.xml.IaNaOption;
import com.jagornet.dhcpv6.xml.Link;
import com.jagornet.dhcpv6.xml.LinkFilter;
import com.jagornet.dhcpv6.xml.LinkFiltersType;
import com.jagornet.dhcpv6.xml.Pool;
import com.jagornet.dhcpv6.xml.PoolsType;

// TODO: Auto-generated Javadoc
/**
 * The Class BindingManager.
 */
public class BindingManager implements BindingManagerInterface
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(BindingManager.class);

    /** The INSTANCE. */
    private static BindingManager INSTANCE;
    
    /** The application context. */
    protected static ApplicationContext context;

    /** The ia mgr. */
    protected IaManager iaMgr;
	
	/** The ia dao. */
	protected IdentityAssocDAO iaDao;
	
	/** The ia addr dao. */
	protected IaAddressDAO iaAddrDao;
	
	/** The opt dao. */
	protected DhcpOptionDAO optDao;
	
	/** The server config. */
	protected DhcpServerConfiguration serverConfig;

    /** The pool map. */
    protected Map<String, List<BindingPool>> poolMap;

	/** The reaper. */
	protected Timer reaper;
    
    /**
     * Gets the single instance of BindingManager.
     * 
     * @return single instance of BindingManager
     */
    public static synchronized BindingManager getInstance()
    {
    	if (INSTANCE == null) {
    		try {
    			INSTANCE = new BindingManager();
    		}
    		catch (Exception ex) {
    			log.error("Failed to initialize BindingManager", ex);
    			return null;
    		}
    	}
    	return INSTANCE;
    }
    
    // for testing
    /**
     * Sets the context.
     * 
     * @param ctx the new context
     */
    public static void setContext(ApplicationContext ctx)
    {
    	context = ctx;
    }
    
	/**
	 * Private constructor suppresses generation of a (public) default constructor.
	 * 
	 * @throws Exception the exception
	 */
    private BindingManager() throws Exception
    {
    	// we may have a static context set if testing
    	if (context == null) {
    		context = new ClassPathXmlApplicationContext("com/jagornet/dhcpv6/context.xml");
    	}
		iaMgr = (IaManager)context.getBean("iaManager");
		iaDao = (IdentityAssocDAO)context.getBean("identityAssocDAO");
		iaAddrDao = (IaAddressDAO)context.getBean("iaAddressDAO");
		optDao = (DhcpOptionDAO)context.getBean("dhcpOptionDAO");
		serverConfig = DhcpServerConfiguration.getInstance();
		initPoolMap();
		initStaticBindings();

		long reaperStartupDelay = 
			DhcpServerPolicies.globalPolicyAsLong(Property.BINDING_MANAGER_REAPER_STARTUP_DELAY);
		long reaperRunPeriod =
			DhcpServerPolicies.globalPolicyAsLong(Property.BINDING_MANAGER_REAPER_RUN_PERIOD);

		reaper = new Timer("BindingReaper");
		reaper.schedule(new ReaperTimerTask(), reaperStartupDelay, reaperRunPeriod);
    }
    
    
    /**
     * Inits the pool map.
     * 
     * @throws Exception the exception
     */
    protected void initPoolMap() throws Exception
    {
		try {
    		SortedMap<Subnet, DhcpLink> linkMap = serverConfig.getLinkMap();
    		if ((linkMap != null) && !linkMap.isEmpty()) {
        		poolMap = new HashMap<String, List<BindingPool>>();
    			for (DhcpLink dhcpLink : linkMap.values()) {
    				Link link = dhcpLink.getLink();
    				List<BindingPool> bindingPools = null;
    				// Put the filtered pools first in the list of pools on this link
					LinkFiltersType linkFiltersType = link.getLinkFilters();
					if (linkFiltersType != null) {
						List<LinkFilter> linkFilters = linkFiltersType.getLinkFilterList();
						if ((linkFilters != null) && !linkFilters.isEmpty()) {
							for (LinkFilter linkFilter : linkFilters) {
								PoolsType poolsType = linkFilter.getPools();
								if (poolsType != null) {
									// add the filtered pools to the mapped list
					    			List<Pool> pools = poolsType.getPoolList();
					    			if ((pools != null) && !pools.isEmpty()) {
					    				if (bindingPools == null) {
					    					bindingPools = new ArrayList<BindingPool>();
					    				}
					    				for (Pool pool : pools) {
					    					BindingPool bp = initBindingPool(pool, link, linkFilter);
											bindingPools.add(bp);
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
					PoolsType poolsType = link.getPools();
					if (poolsType != null) {
						// add the unfiltered pools to the mapped list
		    			List<Pool> pools = poolsType.getPoolList();
		    			if ((pools != null) && !pools.isEmpty()) {
		    				if (bindingPools == null) {
		    					bindingPools = new ArrayList<BindingPool>();
		    				}
		    				for (Pool pool : pools) {
		    					BindingPool bp = initBindingPool(pool, link);
								bindingPools.add(bp);
		    				}
		    			}
		    			else {
		    				log.error("PoolList is null for PoolsType:\n" + poolsType);
		    			}
					}
					else {
						log.warn("PoolsType is null for Link:\n" + link);
					}
					if (bindingPools != null) {
	    				reconcilePools(bindingPools);
						poolMap.put(link.getAddress(), bindingPools);
					}
				}
    		}
    		else {
    			log.error("LinkMap is null for DhcpServerConfiguration");
    		}
		}
		catch (Exception ex) {
			log.error("Failed to build poolMap: " + ex);
			throw ex;
		}
    }
    
    /**
     * Reconcile pools.
     * 
     * @param bindingPools the binding pools
     */
    protected void reconcilePools(List<BindingPool> bindingPools)
    {
    	if ((bindingPools != null) && !bindingPools.isEmpty()) {
    		List<Range> ranges = new ArrayList<Range>();
    		for (BindingPool bp : bindingPools) {
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
    protected BindingPool initBindingPool(Pool pool, Link link) throws Exception
    {
    	return initBindingPool(pool, link, null);
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
    protected BindingPool initBindingPool(Pool pool, Link link, LinkFilter linkFilter) throws Exception
    {
		BindingPool bp = new BindingPool(pool);
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
/*	
	protected BindingPool findBindingPool(Link link, InetAddress inetAddr)
	{
		List<BindingPool> bps = poolMap.get(link.getAddress());
		if ((bps != null) && !bps.isEmpty()) {
			for (BindingPool bindingPool : bps) {
				if (bindingPool.contains(inetAddr))
					return bindingPool;
			}
		}
		return null;
	}
*/	
	/**
 * Find binding pool.
 * 
 * @param link the link
 * @param inetAddr the inet addr
 * @param requestMsg the request msg
 * 
 * @return the binding pool
 */
protected BindingPool findBindingPool(Link link, InetAddress inetAddr, DhcpMessage requestMsg)
	{
		List<BindingPool> bps = poolMap.get(link.getAddress());
		if ((bps != null) && !bps.isEmpty()) {
			for (BindingPool bindingPool : bps) {
				if (bindingPool.contains(inetAddr)) {
					if ((requestMsg != null) && (bindingPool.getLinkFilter() != null)) {
						if (DhcpServerConfiguration.msgMatchesFilter(requestMsg, 
								bindingPool.getLinkFilter())) {
							return bindingPool;
						}
					}
					else {
						return bindingPool;
					}
				}
			}
		}
		return null;
	}
    
	/**
	 * Find binding pool.
	 * 
	 * @param inetAddr the inet addr
	 * 
	 * @return the binding pool
	 */
	protected BindingPool findBindingPool(InetAddress inetAddr)
	{
		//TODO this kind of sucks - looping thru all pools, on all links
		Collection<List<BindingPool>> allPools = poolMap.values();
		if ((allPools != null) && !allPools.isEmpty()) {
			for (List<BindingPool> bps : allPools) {
				for (BindingPool bindingPool : bps) {
					if (bindingPool.contains(inetAddr))
						return bindingPool;
				}
			}
		}
		return null;
	}
	
    /**
     * Inits the static bindings.
     * 
     * @throws Exception the exception
     */
    protected void initStaticBindings() throws Exception
    {
		try {
    		SortedMap<Subnet, DhcpLink> linkMap = serverConfig.getLinkMap();
    		if (linkMap != null) {
    			for (DhcpLink dhcpLink : linkMap.values()) {
    				Link link = dhcpLink.getLink();
					BindingsType bindingsType = link.getBindings();
					if (bindingsType != null) {
						List<com.jagornet.dhcpv6.xml.Binding> staticBindings = 
							bindingsType.getBindingList();
						if (staticBindings != null) {
							for (com.jagornet.dhcpv6.xml.Binding staticBinding : staticBindings) {
//TODO								reconcileStaticBinding(staticBinding);
								IaNaOption iaNa = staticBinding.getIaNaOption();
								if (iaNa != null) {
									IaAddrOptionListType iaAddrOptionList = iaNa.getIaAddrOptionList();
									List<IaAddrOption> iaAddrOptions = 
										iaAddrOptionList.getIaAddrOptionList();
									for (IaAddrOption iaAddrOption : iaAddrOptions) {
										setIpAsUsed(link, iaAddrOption);
									}
								}
								//TODO iaTa and iaPd bindings
							}
						}
					}
					else {
//						log.warn("BindingsType is null for Link:\n" + link);
					}
    			}
    		}
		}
		catch (Exception ex) {
			log.error("Failed to build staticBindings: " + ex);
			throw ex;
		}
    }
    
    /**
     * Sets the ip as used.
     * 
     * @param link the link
     * @param iaAddrOption the ia addr option
     * 
     * @throws Exception the exception
     */
    protected void setIpAsUsed(Link link, IaAddrOption iaAddrOption) throws Exception
    {
		InetAddress inetAddr = InetAddress.getByName(iaAddrOption.getIpv6Address());
		BindingPool bindingPool = findBindingPool(link, inetAddr, null);
		if (bindingPool != null) {
			bindingPool.setUsed(inetAddr);
		}
		else {
			log.error("Failed to set address used: No BindingPool found for IP=" + 
					inetAddr.getHostAddress());
		}
    }
    
    /**
     * Gets the duid.
     * 
     * @param clientIdOption the client id option
     * 
     * @return the duid
     */
    private byte[] getDuid(DhcpClientIdOption clientIdOption)
    {
		if ((clientIdOption != null) &&
				(clientIdOption.getOpaqueDataOptionType() != null) &&
				(clientIdOption.getOpaqueDataOptionType().getOpaqueData() != null)) {
			return clientIdOption.getOpaqueDataOptionType().getOpaqueData().getHexValue();
		}
		return null;
	}
	
	/**
	 * Gets the iaid.
	 * 
	 * @param iaNaOption the ia na option
	 * 
	 * @return the iaid
	 */
	private Long getIaid(DhcpIaNaOption iaNaOption)
	{
		if ((iaNaOption != null) &&
				(iaNaOption.getIaNaOption() != null)) {
			return iaNaOption.getIaNaOption().getIaId();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.BindingManagerInterface#releaseIaAddress(com.jagornet.dhcpv6.db.IaAddress)
	 */
	public void releaseIaAddress(IaAddress iaAddr)
	{
		try {
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
			//TODO: ddns update release
		}
		catch (Exception ex) {
			log.error("Failed to release address", ex);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.BindingManagerInterface#declineIaAddress(com.jagornet.dhcpv6.db.IaAddress)
	 */
	public void declineIaAddress(IaAddress iaAddr)
	{
		try {
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
			//TODO: ddns update release
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
	 * Free address.
	 * 
	 * @param inetAddr the inet addr
	 */
	protected void freeAddress(InetAddress inetAddr)
	{
		BindingPool bp = findBindingPool(inetAddr);
		if (bp != null) {
			bp.setFree(inetAddr);
		}
		else {
			log.error("Failed to free address: no BindingPool found for address: " +
					inetAddr.getHostAddress());
		}
	}
        
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.BindingManagerInterface#findCurrentBinding(com.jagornet.dhcpv6.xml.Link, com.jagornet.dhcpv6.option.DhcpClientIdOption, com.jagornet.dhcpv6.option.DhcpIaNaOption, com.jagornet.dhcpv6.message.DhcpMessage)
	 */
	public Binding findCurrentBinding(Link clientLink, DhcpClientIdOption clientIdOption, 
			DhcpIaNaOption iaNaOption, DhcpMessage requestMsg) 
	{
		Binding binding = null;
		try {
			if ((clientIdOption != null) && (iaNaOption != null)) {
				byte[] duid = getDuid(clientIdOption);
				if (duid != null) {
					Long iaid = getIaid(iaNaOption);
					if (iaid != null) {
						IdentityAssoc ia = iaMgr.findIA(duid, IdentityAssoc.NA_TYPE, iaid);
						if (ia != null) {
							binding = buildBindingFromIa(ia, clientLink, requestMsg);
						}
					}
					else {
						log.error("Iaid is null in findCurrentBinding:" + 
								" duid=" + Util.toHexString(duid));
					}
				}
				else {
					log.error("Duid is null in findCurrentBinding"); 
				}
			}
			if (binding != null) {
				log.info("Found current binding: " + binding.toString());
			}
			else {
				if (log.isDebugEnabled())
					log.debug("No current binding found for IA_NA: " +
							" duid=" + Util.toHexString(getDuid(clientIdOption)) +
							" iaid=" + getIaid(iaNaOption));
			}
		}
		catch (Exception ex) {
			log.error("Failed to find current binding", ex);
		}
		return binding;
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.BindingManagerInterface#createSolicitBinding(com.jagornet.dhcpv6.xml.Link, com.jagornet.dhcpv6.option.DhcpClientIdOption, com.jagornet.dhcpv6.option.DhcpIaNaOption, com.jagornet.dhcpv6.message.DhcpMessage, boolean)
	 */
	public Binding createSolicitBinding(Link clientLink, DhcpClientIdOption clientIdOption, 
			DhcpIaNaOption iaNaOption, DhcpMessage requestMsg, boolean rapidCommit)
	{
		Binding binding = null;
		
		List<InetAddress> inetAddrs = 
			getInetAddrsForIaNa(clientLink, clientIdOption, iaNaOption, requestMsg);
		if ((inetAddrs != null) && !inetAddrs.isEmpty()) {
			binding = buildBinding(clientLink, clientIdOption, iaNaOption);
			Set<BindingAddress> bindingAddrs = buildBindingAddrs(clientLink, inetAddrs, requestMsg);
			if (rapidCommit) {
				binding.setState(IdentityAssoc.COMMITTED);
				for (BindingAddress bindingAddr : bindingAddrs) {
					bindingAddr.setState(IaAddress.COMMITTED);
					//TODO ddns update
				}
			}
			else {
				binding.setState(IdentityAssoc.ADVERTISED);
				for (BindingAddress bindingAddr : bindingAddrs) {
					bindingAddr.setState(IaAddress.ADVERTISED);
				}
			}
			binding.setIaAddresses(bindingAddrs);
			try {
				iaMgr.createIA(binding);
				return binding;
			}
			catch (Exception ex) {
				log.error("Failed to create binding", ex);
				return null;
			}
		}
		
		if (binding != null) {
			log.info("Created solicit binding: " + binding.toString());
		}
		else {
			log.warn("Failed to created solicit binding");
		}
		return binding;
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.BindingManagerInterface#updateBinding(com.jagornet.dhcpv6.server.request.binding.Binding, com.jagornet.dhcpv6.xml.Link, com.jagornet.dhcpv6.option.DhcpClientIdOption, com.jagornet.dhcpv6.option.DhcpIaNaOption, com.jagornet.dhcpv6.message.DhcpMessage, byte)
	 */
	public Binding updateBinding(Binding binding, Link clientLink, 
			DhcpClientIdOption clientIdOption, DhcpIaNaOption iaNaOption,
			DhcpMessage requestMsg, byte state)
	{
		Collection<BindingAddress> bindingAddrs = binding.getBindingAddresses();
		if ((bindingAddrs != null) && !bindingAddrs.isEmpty()) {
			log.info("Updating times for existing binding addresses");
			// current binding has addresses, so update times
			setBindingAddrsTimes(bindingAddrs);
		}
		else {
			log.info("Existing binding has no addresses, allocating new address(es)");
			// current binding has no addresses, add new one(s)
			List<InetAddress> inetAddrs = 
				getInetAddrsForIaNa(clientLink, clientIdOption, iaNaOption, requestMsg);
			if ((inetAddrs == null) || inetAddrs.isEmpty()) {
				log.error("Failed to update binding, no addresses available");
				return null;
			}
			bindingAddrs = buildBindingAddrs(clientLink, inetAddrs, requestMsg);
			binding.setIaAddresses(bindingAddrs);
		}
		binding.setState(state);
		for (BindingAddress bindingAddr : bindingAddrs) {
			bindingAddr.setState(state);
		}
		try {
			iaMgr.updateIA(binding, null, binding.getIaAddresses(), null);
			return binding;	// if we get here, it worked
		}
		catch (Exception ex) {
			log.error("Failed to update binding", ex);
			return null;
		}
	}
	
	/**
	 * Gets the inet addrs for ia na.
	 * 
	 * @param clientLink the client link
	 * @param clientIdOption the client id option
	 * @param iaNaOption the ia na option
	 * @param requestMsg the request msg
	 * 
	 * @return the inet addrs for ia na
	 */
	protected List<InetAddress> getInetAddrsForIaNa(Link clientLink,
			DhcpClientIdOption clientIdOption, DhcpIaNaOption iaNaOption, DhcpMessage requestMsg)
	{
		List<InetAddress> inetAddrs = new ArrayList<InetAddress>();
		
		List<DhcpIaAddrOption> iaAddrs = iaNaOption.getIaAddrOptions();
		if ((iaAddrs != null) && !iaAddrs.isEmpty()) {
			// see what addresses the client is requesting
			for (DhcpIaAddrOption iaAddr : iaAddrs) {
				InetAddress inetAddr = iaAddr.getInetAddress();
				BindingPool bp = findBindingPool(clientLink, inetAddr, requestMsg);
				if (bp == null) {
					log.warn("No BindingPool found for requested client address: " +
							inetAddr.getHostAddress());
					// if there is no pool for the requested address, then skip it
					// because that address is either off-link or no longer valid
					continue;
				}
				log.info("Searching existing bindings for requested IP=" +
						inetAddr.getHostAddress());
				IdentityAssoc ia = null;
				try {
					ia = iaMgr.findIA(inetAddr); 
					if (ia != null) {
						// the address is assigned to an IA, which we
						// don't expect to be this IA, because we would
						// have found it using findCurrentBinding...
						// but, perhaps another thread just created it?
						if (isMyIa(clientIdOption, iaNaOption, ia)) {
							log.warn("Requested IP=" + inetAddr.getHostAddress() +
									" is already held by THIS client IA_NA: " +
									" duid=" + Util.toHexString(getDuid(clientIdOption)) +
									" iaid=" + getIaid(iaNaOption) +
									".  Allowing this requested IP.");
						}
						else {
							log.info("Requested IP=" + inetAddr.getHostAddress() +
									" is held by ANOTHER client IA_NA: " +
									" duid=" + Util.toHexString(ia.getDuid()) +
									" iaid=" + ia.getIaid());
							// the address is held by another IA, so get a new one
							inetAddr = getNextFreeAddress(clientLink, requestMsg);
						}
					}
					if (inetAddr != null) {
						inetAddrs.add(inetAddr);
					}
				}
				catch (Exception ex) {
					log.error("Failure finding IA for address", ex);
				}
			}
		}
		
		if (inetAddrs.isEmpty()) {
			// the client did not request any valid addresses, so get the next one
			InetAddress inetAddr = getNextFreeAddress(clientLink, requestMsg);
			if (inetAddr != null) {
				inetAddrs.add(inetAddr);
			}
		}
		return inetAddrs;
	}
	
	/**
	 * Checks if is my ia.
	 * 
	 * @param clientIdOption the client id option
	 * @param iaNaOption the ia na option
	 * @param ia the ia
	 * 
	 * @return true, if is my ia
	 */
	private boolean isMyIa(DhcpClientIdOption clientIdOption, 
			DhcpIaNaOption iaNaOption, IdentityAssoc ia)
	{
		boolean rc = false;
		if ((iaNaOption != null) && (ia != null)) {
			byte[] duid = getDuid(clientIdOption);
			if (duid != null) {
				if (Arrays.equals(ia.getDuid(), duid) &&
					(ia.getIatype() == IdentityAssoc.NA_TYPE) &&
					(ia.getIaid() == iaNaOption.getIaNaOption().getIaId())) {
					rc = true;
				}
			}
		}		
		return rc;
	}
	
	/**
	 * Gets the next free address.
	 * 
	 * @param clientLink the client link
	 * @param requestMsg the request msg
	 * 
	 * @return the next free address
	 */
	private InetAddress getNextFreeAddress(Link clientLink, DhcpMessage requestMsg)
	{
		if (clientLink != null) {
    		List<BindingPool> pools = poolMap.get(clientLink.getAddress());
    		if (pools != null) {
    			for (BindingPool bp : pools) {
    				LinkFilter filter = bp.getLinkFilter();
    				if ((requestMsg != null) && (filter != null)) {
    					if (!DhcpServerConfiguration.msgMatchesFilter(requestMsg, filter)) {
    						log.info("Client request does not match filter, skipping pool: " +
    								bp.getStartAddress().getHostAddress() + "-" +
    								bp.getEndAddress().getHostAddress());
    						continue;
    					}
    				}
					if (log.isDebugEnabled())
						log.debug("Getting next available address from pool: " + 
								bp.getStartAddress().getHostAddress() + "-" +
								bp.getEndAddress().getHostAddress());
					InetAddress free = bp.getNextAvailableAddress();
					if (free != null) {
						if (log.isDebugEnabled())
							log.debug("Found next available address: " +
									free.getHostAddress());
						return free;
					}
					else {
						// warning here, b/c there may be more pools
						log.warn("No free addresses available in pool: " + 
								bp.getStartAddress().getHostAddress() + "-" +
								bp.getEndAddress().getHostAddress());
					}
    			}
    		}
    		log.error("No Pools defined in server configuration for Link: " +
    				clientLink.getAddress());
		}
		else {
			throw new IllegalStateException("ClientLink is null");
		}
		return null;
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
	private Binding buildBindingFromIa(IdentityAssoc ia, 
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
			bp.setUsed(inetAddr);	// TODO check if this is necessary
			// TODO store the configured options in the persisted binding?
			// ipAddr.setDhcpOptions(bp.getDhcpOptions());
			return new BindingAddress(iaAddr, bp);
		}
		else {
			log.error("Failed to create BindingAddress: No BindingPool found for IP=" + 
					inetAddr.getHostAddress());
		}
		// MUST have a BindingPool, otherwise something's broke
		return null;
	}

	/**
	 * Builds the binding.
	 * 
	 * @param clientLink the client link
	 * @param clientIdOption the client id option
	 * @param iaNaOption the ia na option
	 * 
	 * @return the binding
	 */
	private Binding buildBinding(Link clientLink, DhcpClientIdOption clientIdOption,
			DhcpIaNaOption iaNaOption)
	{
		IdentityAssoc ia = new IdentityAssoc();
		ia.setDuid(getDuid(clientIdOption));
		ia.setIatype(IdentityAssoc.NA_TYPE);
		ia.setIaid(iaNaOption.getIaNaOption().getIaId());
		return new Binding(ia, clientLink);		
	}
	
	/**
	 * Builds the binding addrs.
	 * 
	 * @param clientLink the client link
	 * @param inetAddrs the inet addrs
	 * @param requestMsg the request msg
	 * 
	 * @return the set< binding address>
	 */
	private Set<BindingAddress> buildBindingAddrs(Link clientLink, 
			List<InetAddress> inetAddrs, DhcpMessage requestMsg)
	{
		Set<BindingAddress> bindingAddrs = new HashSet<BindingAddress>();
		for (InetAddress inetAddr : inetAddrs) {
			BindingAddress bindingAddr = buildBindingAddress(inetAddr, clientLink, requestMsg);
			if (bindingAddr != null)
				bindingAddrs.add(bindingAddr);
		}
		return bindingAddrs;
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
	private BindingAddress buildBindingAddress(InetAddress inetAddr, 
			Link clientLink, DhcpMessage requestMsg)
	{
		BindingPool bp = findBindingPool(clientLink, inetAddr, requestMsg);
		if (bp != null) {
			bp.setUsed(inetAddr);	// TODO check if this is necessary
			IaAddress iaAddr = new IaAddress();
			iaAddr.setIpAddress(inetAddr);
			setIaAddrTimes(bp, iaAddr);
			// TODO store the configured options in the persisted binding?
			// ipAddr.setDhcpOptions(bp.getDhcpOptions());
			return new BindingAddress(iaAddr, bp);
		}
		else {
			log.error("Failed to create BindingAddress: No BindingPool found for IP=" + 
					inetAddr.getHostAddress());
		}
		// MUST have a BindingPool, otherwise something's broke
		return null;
	}

	/**
	 * Sets the binding addrs times.
	 * 
	 * @param bindingAddrs the new binding addrs times
	 */
	private void setBindingAddrsTimes(Collection<BindingAddress> bindingAddrs) 
	{
		if ((bindingAddrs != null) && !bindingAddrs.isEmpty()) {
			for (BindingAddress bindingAddr : bindingAddrs) {
				setIaAddrTimes(bindingAddr.getBindingPool(), bindingAddr);
				//TODO: if we store the options, and they have changed,
				// 		then we must update those options here somehow
			}
		}
	}

	/**
	 * Sets the ia addr times.
	 * 
	 * @param bp the bp
	 * @param iaAddr the ia addr
	 */
	private void setIaAddrTimes(BindingPool bp, IaAddress iaAddr)
	{
		log.debug("Updating binding times for address: " + 
				iaAddr.getIpAddress().getHostAddress());
		Date now = new Date();
		iaAddr.setStartTime(now);
		long pEndTime = now.getTime() + bp.getPreferredLifetimeMs();
		iaAddr.setPreferredEndTime(new Date(pEndTime));
		long vEndTime = now.getTime() + bp.getValidLifetimeMs();
		iaAddr.setValidEndTime(new Date(vEndTime));
	}

	//TODO: expose this in the interface?  is it needed at all?
	/**
	 * All ia addrs in pools.
	 * 
	 * @param dhcpIaNaOption the dhcp ia na option
	 * 
	 * @return true, if successful
	 */
	public boolean allIaAddrsInPools(DhcpIaNaOption dhcpIaNaOption)
	{
		if (dhcpIaNaOption != null) {
			List<DhcpIaAddrOption> iaAddrOpts = dhcpIaNaOption.getIaAddrOptions();
			if (iaAddrOpts != null) {
				for (DhcpIaAddrOption iaAddrOpt : iaAddrOpts) {
					if (findBindingPool(iaAddrOpt.getInetAddress()) == null) {
						return false;
					}
				}
			}
		}
		return true;
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
			INSTANCE.expireAddresses();
		}
	}
}
