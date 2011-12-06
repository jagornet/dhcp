/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file NaAddrBindingManagerImpl.java is part of DHCPv6.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.db.IaAddress;
import com.jagornet.dhcpv6.db.IdentityAssoc;
import com.jagornet.dhcpv6.message.DhcpMessageInterface;
import com.jagornet.dhcpv6.option.v4.DhcpV4RequestedIpAddressOption;
import com.jagornet.dhcpv6.server.config.DhcpServerConfigException;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.Link;
import com.jagornet.dhcpv6.xml.LinkFilter;
import com.jagornet.dhcpv6.xml.LinkFiltersType;
import com.jagornet.dhcpv6.xml.PrefixBinding;
import com.jagornet.dhcpv6.xml.PrefixBindingsType;
import com.jagornet.dhcpv6.xml.V4AddressPool;
import com.jagornet.dhcpv6.xml.V4AddressPoolsType;

/**
 * The Class V4AddrBindingManagerImpl.
 * 
 * @author A. Gregory Rabil
 */
public class V4AddrBindingManagerImpl 
		extends BaseAddrBindingManager 
		implements V4AddrBindingManager
{
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(V4AddrBindingManagerImpl.class);
	
	/**
	 * Instantiates a new v4 addr binding manager impl.
	 * 
	 * @throws DhcpServerConfigException the dhcp server config exception
	 */
	public V4AddrBindingManagerImpl() throws DhcpServerConfigException
	{
		super();
	}
    
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
		List<V4AddressBindingPool> bindingPools = new ArrayList<V4AddressBindingPool>();
		// Put the filtered pools first in the list of pools on this link
		LinkFiltersType linkFiltersType = link.getLinkFilters();
		if (linkFiltersType != null) {
			List<LinkFilter> linkFilters = linkFiltersType.getLinkFilterList();
			if ((linkFilters != null) && !linkFilters.isEmpty()) {
				for (LinkFilter linkFilter : linkFilters) {
					V4AddressPoolsType poolsType = linkFilter.getV4AddrPools();
					if (poolsType != null) {
						// add the filtered pools to the mapped list
		    			List<V4AddressPool> pools = poolsType.getPoolList();
		    			if ((pools != null) && !pools.isEmpty()) {
		    				for (V4AddressPool pool : pools) {
		    					V4AddressBindingPool abp = buildBindingPool(pool, link, linkFilter);
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
		V4AddressPoolsType poolsType = link.getV4AddrPools();
		if (poolsType != null) {
			// add the unfiltered pools to the mapped list
			List<V4AddressPool> pools = poolsType.getPoolList();
			if ((pools != null) && !pools.isEmpty()) {
				for (V4AddressPool pool : pools) {
					V4AddressBindingPool abp = buildBindingPool(pool, link);
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
     * within the given list of AddressBindingPools.
     * 
     * @param bindingPools the list of AddressBindingPools
     */
    protected void reconcilePools(List<V4AddressBindingPool> bindingPools)
    {
    	if ((bindingPools != null) && !bindingPools.isEmpty()) {
    		List<Range> ranges = new ArrayList<Range>();
    		for (V4AddressBindingPool bp : bindingPools) {
    			Range range = new Range(bp.getStartAddress(), bp.getEndAddress());
				ranges.add(range);
			}
        	iaMgr.reconcileIaAddresses(ranges);
    	}
    }
    
    /**
     * Builds a binding pool from an V4AddressPool using the given link.
     * 
     * @param pool the V4AddressPool to wrap as an AddressBindingPool
     * @param link the link
     * 
     * @return the binding pool
     * 
     * @throws DhcpServerConfigException if there is a problem parsing the configured range
     */
    protected V4AddressBindingPool buildBindingPool(V4AddressPool pool, Link link) 
    		throws DhcpServerConfigException
    {
    	return buildBindingPool(pool, link, null);
    }

    /**
     * Builds a binding pool from an V4AddressPool using the given link and filter.
     * 
     * @param pool the V4AddressPool to wrap as an AddressBindingPool
     * @param link the link
     * @param linkFilter the link filter
     * 
     * @return the binding pool
     * 
     * @throws DhcpServerConfigException if there is a problem parsing the configured range
     */
    protected V4AddressBindingPool buildBindingPool(V4AddressPool pool, Link link, 
    		LinkFilter linkFilter) throws DhcpServerConfigException
    {
    	V4AddressBindingPool bp = new V4AddressBindingPool(pool);
		long leasetime = 
			DhcpServerPolicies.effectivePolicyAsLong(pool, link, Property.V4_DEFAULT_LEASETIME);
		bp.setLeasetime(leasetime);
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
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.server.request.binding.BaseBindingManager#initBindings(com.jagornet.dhcpv6.xml.Link)
     */
    protected void initBindings(Link link) throws DhcpServerConfigException
    {
    	try {
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
    	catch (UnknownHostException ex) {
    		log.error("Invalid static binding address", ex);
    		throw new DhcpServerConfigException("Invalid static binding address", ex);
    	}
    }
    

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.NaAddrBindingManager#findCurrentBinding(com.jagornet.dhcpv6.xml.Link, com.jagornet.dhcpv6.option.DhcpClientIdOption, com.jagornet.dhcpv6.option.DhcpIaNaOption, com.jagornet.dhcpv6.message.DhcpMessage)
	 */
	@Override
	public Binding findCurrentBinding(Link clientLink, byte[] macAddr, 
			DhcpMessageInterface requestMsg) {
		
		return super.findCurrentBinding(clientLink, macAddr, IdentityAssoc.V4_TYPE, 
				0, requestMsg);
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.V4AddrBindingManager#createSolicitBinding(com.jagornet.dhcpv6.xml.Link, com.jagornet.dhcpv6.option.DhcpClientIdOption, com.jagornet.dhcpv6.option.DhcpIaNaOption, com.jagornet.dhcpv6.message.DhcpMessage, boolean)
	 */
	@Override
	public Binding createDiscoverBinding(Link clientLink, byte[] macAddr, 
			DhcpMessageInterface requestMsg, boolean rapidCommit) {
		
		List<InetAddress> requestAddrs = getInetAddrs(requestMsg);
		
		return super.createBinding(clientLink, macAddr, IdentityAssoc.V4_TYPE, 
				0, requestAddrs, requestMsg, rapidCommit);
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.V4AddrBindingManager#updateBinding(com.jagornet.dhcpv6.server.request.binding.Binding, com.jagornet.dhcpv6.xml.Link, com.jagornet.dhcpv6.option.DhcpClientIdOption, com.jagornet.dhcpv6.option.DhcpIaNaOption, com.jagornet.dhcpv6.message.DhcpMessage, byte)
	 */
	@Override
	public Binding updateBinding(Binding binding, Link clientLink, 
			byte[] macAddr, DhcpMessageInterface requestMsg, byte state) {
				
		List<InetAddress> requestAddrs = getInetAddrs(requestMsg);
		
		return super.updateBinding(binding, clientLink, macAddr, IdentityAssoc.V4_TYPE,
				0, requestAddrs, requestMsg, state);
	}
	
	/**
	 * Get the Requested IP addresses from the client message, if any was provided.
	 * 
	 * @param requestMsg the request msg
	 * 
	 * @return a list of InetAddresses containing the requested IP, or null if none requested or
	 *         if the requested IP is bogus
	 */
	private List<InetAddress> getInetAddrs(DhcpMessageInterface requestMsg)
	{
		List<InetAddress> inetAddrs = null;
		DhcpV4RequestedIpAddressOption reqIpOption = (DhcpV4RequestedIpAddressOption)
			requestMsg.getDhcpOption(DhcpConstants.V4OPTION_REQUESTED_IP);
		if (reqIpOption != null) {
			try {
				InetAddress inetAddr = 
					InetAddress.getByName(reqIpOption.getIpAddressOption().getIpAddress());
				inetAddrs = new ArrayList<InetAddress>();
				inetAddrs.add(inetAddr);
			}
			catch (UnknownHostException ex) {
				// client provided a bogus address, ignore it
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
	protected Binding buildBindingFromIa(IdentityAssoc ia, Link clientLink,
			DhcpMessageInterface requestMsg) {
		if (log.isDebugEnabled())
			log.debug("Building binding from IA: " +
					" duid=" + Util.toHexString(ia.getDuid()) +
					" iatype=" + ia.getIatype() +
					" iaid=" + ia.getIaid());
		Binding binding = new Binding(ia, clientLink);
		Collection<? extends IaAddress> iaAddrs = ia.getIaAddresses();
		if ((iaAddrs != null) && !iaAddrs.isEmpty()) {
			List<V4BindingAddress> bindingAddrs = new ArrayList<V4BindingAddress>();
			for (IaAddress iaAddr : iaAddrs) {
				V4BindingAddress bindingAddr = 
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
	private V4BindingAddress buildBindingAddrFromIaAddr(IaAddress iaAddr, 
			Link clientLink, DhcpMessageInterface requestMsg)
	{
		if (log.isDebugEnabled())
			log.debug("Building BindingObject from " + iaAddr.toString());
		InetAddress inetAddr = iaAddr.getIpAddress();
		BindingPool bp = findBindingPool(clientLink, inetAddr, requestMsg);
		if (bp != null) {
			// TODO determine if we need to set the IP as used in the
			// BindingPool.  Note that this method is also called when
			// releasing addresses.
//			bp.setUsed(inetAddr);
			// TODO store the configured options in the persisted binding?
			// ipAddr.setDhcpOptions(bp.getDhcpOptions());
			return new V4BindingAddress(iaAddr, (V4AddressBindingPool)bp);
		}
		else {
			log.error("Failed to create V4BindingAddress: No V4BindingPool found for IP=" + 
					inetAddr.getHostAddress());
		}
		// MUST have a BindingPool, otherwise something's broke
		return null;
	}

	@Override
	protected BindingObject buildBindingObject(InetAddress inetAddr,
			Link clientLink, DhcpMessageInterface requestMsg)
	{
		V4AddressBindingPool bp = (V4AddressBindingPool) findBindingPool(clientLink, inetAddr, requestMsg);
		if (bp != null) {
			bp.setUsed(inetAddr);	// TODO check if this is necessary
			IaAddress iaAddr = new IaAddress();
			iaAddr.setIpAddress(inetAddr);
			V4BindingAddress bindingAddr = new V4BindingAddress(iaAddr, bp);
			setBindingObjectTimes(bindingAddr, 
					bp.getPreferredLifetimeMs(), bp.getPreferredLifetimeMs());
			// TODO store the configured options in the persisted binding?
			// bindingAddr.setDhcpOptions(bp.getDhcpOptions());
			return bindingAddr;
		}
		else {
			log.error("Failed to create V4BindingAddress: No V4BindingPool found for IP=" + 
					inetAddr.getHostAddress());
		}
		// MUST have a BindingPool, otherwise something's broke
		return null;
	}

	@Override
	protected void ddnsDelete(IaAddress iaAddr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected byte getIaType() {
		return IdentityAssoc.V4_TYPE;
	}
}
