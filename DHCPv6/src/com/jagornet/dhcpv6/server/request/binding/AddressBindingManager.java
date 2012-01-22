/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file AddressBindingManager.java is part of DHCPv6.
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
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.db.DhcpOption;
import com.jagornet.dhcpv6.db.IaAddress;
import com.jagornet.dhcpv6.db.IdentityAssoc;
import com.jagornet.dhcpv6.message.DhcpMessageInterface;
import com.jagornet.dhcpv6.option.DhcpClientFqdnOption;
import com.jagornet.dhcpv6.server.config.DhcpLink;
import com.jagornet.dhcpv6.server.config.DhcpServerConfigException;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcpv6.server.request.ddns.DdnsCallback;
import com.jagornet.dhcpv6.server.request.ddns.DdnsUpdater;
import com.jagornet.dhcpv6.server.request.ddns.DhcpDdnsComplete;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.AddressBinding;
import com.jagornet.dhcpv6.xml.AddressBindingsType;
import com.jagornet.dhcpv6.xml.AddressPool;
import com.jagornet.dhcpv6.xml.AddressPoolsType;
import com.jagornet.dhcpv6.xml.DomainNameOptionType;
import com.jagornet.dhcpv6.xml.Link;
import com.jagornet.dhcpv6.xml.LinkFilter;
import com.jagornet.dhcpv6.xml.LinkFiltersType;

/**
 * The Class AddressBindingManager.  
 * Third-level abstract class that extends BaseAddrBindingManager to
 * add behavior specific to DHCPv6 address bindings.
 * 
 * @author A. Gregory Rabil
 */
public abstract class AddressBindingManager extends BaseAddrBindingManager
{
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(AddressBindingManager.class);
    
	public AddressBindingManager()
	{
		super();
	}
	
	/**
	 * Fetch the AddressPoolsType XML object from the given LinkFilter XML object.
	 * The subclasses fetch the address pools for either NA or TA addresses.
	 * 
	 * @param linkFilter the link filter
	 * @return the AddressPoolsType for this link filter, or null if none
	 */
    protected abstract AddressPoolsType getAddressPoolsType(LinkFilter linkFilter);
    
	/**
	 * Fetch the AddressPoolsType XML object from the given Link XML object.
	 * The subclasses fetch the address pools for either NA or TA addresses.
	 * 
	 * @param linkFilter the link
	 * @return the AddressPoolsType for this link, or null if none
	 */
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
     * @throws DhcpServerConfigException if there is a problem parsing a configured range
     */
    protected List<? extends BindingPool> buildBindingPools(Link link) 
    		throws DhcpServerConfigException
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
        	iaMgr.reconcileIaAddresses(ranges);
    	}
    }
    
    /**
     * Builds a binding pool from an AddressPool using the given link.
     * 
     * @param pool the AddressPool to wrap as an AddressBindingPool
     * @param link the link
     * 
     * @return the binding pool
     * 
     * @throws DhcpServerConfigException if there is a problem parsing the configured range
     */
    protected AddressBindingPool buildBindingPool(AddressPool pool, Link link) 
    		throws DhcpServerConfigException
    {
    	return buildBindingPool(pool, link, null);
    }

    /**
     * Builds a binding pool from an AddressPool using the given link and filter.
     * 
     * @param pool the AddressPool to wrap as an AddressBindingPool
     * @param link the link
     * @param linkFilter the link filter
     * 
     * @return the binding pool
     * 
     * @throws DhcpServerConfigException if there is a problem parsing the configured range
     */
    protected AddressBindingPool buildBindingPool(AddressPool pool, Link link, 
    		LinkFilter linkFilter) throws DhcpServerConfigException
    {
		AddressBindingPool bp = new AddressBindingPool(pool);
		long pLifetime = 
			DhcpServerPolicies.effectivePolicyAsLong((AddressPoolInterface) bp, 
					link, Property.PREFERRED_LIFETIME);
		bp.setPreferredLifetime(pLifetime);
		long vLifetime = 
			DhcpServerPolicies.effectivePolicyAsLong((AddressPoolInterface) bp, 
					link, Property.VALID_LIFETIME);
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
    	return bp;
    }
    
    /**
     * Fetch the AddressBindings from the given XML Link object.
     * 
     * @param link
     * @return
     */
    protected abstract AddressBindingsType getAddressBindingsType(Link link);
    
    protected void initBindings(Link link) throws DhcpServerConfigException
    {
    	try {
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
    	catch (UnknownHostException ex) {
    		log.error("Invalid static binding address", ex);
    		throw new DhcpServerConfigException("Invalid statid binding address", ex);
    	}
    }
    
    /**
     * Perform the DDNS delete processing when a lease is released or expired.
     * 
     * @param iaAddr the released or expired IaAddress 
     */
    protected void ddnsDelete(IdentityAssoc ia, IaAddress iaAddr)
    {
    	DhcpClientFqdnOption clientFqdnOption = null;
    	try {
	    	if ((ia != null) && (iaAddr != null)) {
	    		Collection<DhcpOption> opts = iaAddr.getDhcpOptions();
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
				        		if (bindingAddr != null) {
				        			
				        			AddressBindingPool pool = 
				        				(AddressBindingPool) bindingAddr.getBindingPool();
				        			
				        			DdnsCallback ddnsComplete = 
				        				new DhcpDdnsComplete(bindingAddr, clientFqdnOption);

				        			DdnsUpdater ddns =
										new DdnsUpdater(link.getLink(), pool,
												bindingAddr.getIpAddress(), fqdn, ia.getDuid(),
												pool.getValidLifetime(),
												clientFqdnOption.getUpdateAaaaBit(), true,
												ddnsComplete);
				        			
									ddns.processUpdates();
				        		}
				        		else {
				        			log.error("Failed to find binding for address: " +
				        					iaAddr.getIpAddress().getHostAddress());
				        		}
				        	}
				        	else {
				        		log.error("Failed to find link for binding address: " + 
				        				iaAddr.getIpAddress().getHostAddress());
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
    	}
    	catch (Exception ex) {
    		log.error("Failed to perform DDNS delete", ex);
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
			Link clientLink, DhcpMessageInterface requestMsg)
	{
		if (log.isDebugEnabled())
			log.debug("Building binding from IA: " +
					" duid=" + Util.toHexString(ia.getDuid()) +
					" iatype=" + ia.getIatype() +
					" iaid=" + ia.getIaid());
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
		else {
			log.warn("IA has no addresses, binding is empty.");
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
			Link clientLink, DhcpMessageInterface requestMsg)
	{
		if (log.isDebugEnabled())
			log.debug("Building BindingAddress from " + iaAddr.toString());
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
			Link clientLink, DhcpMessageInterface requestMsg)
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
}
