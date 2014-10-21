/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file V4AddrBindingManagerImpl.java is part of Jagornet DHCP.
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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.db.DhcpOption;
import com.jagornet.dhcp.db.IaAddress;
import com.jagornet.dhcp.db.IdentityAssoc;
import com.jagornet.dhcp.message.DhcpMessage;
import com.jagornet.dhcp.option.v4.DhcpV4ClientFqdnOption;
import com.jagornet.dhcp.option.v4.DhcpV4RequestedIpAddressOption;
import com.jagornet.dhcp.server.config.DhcpConfigObject;
import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.config.DhcpServerConfigException;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.request.ddns.DdnsCallback;
import com.jagornet.dhcp.server.request.ddns.DdnsUpdater;
import com.jagornet.dhcp.server.request.ddns.DhcpV4DdnsComplete;
import com.jagornet.dhcp.util.DhcpConstants;
import com.jagornet.dhcp.xml.Link;
import com.jagornet.dhcp.xml.LinkFilter;
import com.jagornet.dhcp.xml.LinkFiltersType;
import com.jagornet.dhcp.xml.V4AddressBinding;
import com.jagornet.dhcp.xml.V4AddressBindingsType;
import com.jagornet.dhcp.xml.V4AddressPool;
import com.jagornet.dhcp.xml.V4AddressPoolsType;

/**
 * The Class V4AddrBindingManagerImpl.
 * 
 * @author A. Gregory Rabil
 */
public class V4AddrBindingManagerImpl 
		extends BaseAddrBindingManager 
		implements V4AddrBindingManager
{
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
     * Build the list of V4AddressBindingPools from the list of configured V4AddressPools
     * for the given configuration Link container object. The list of V4AddressBindingPools
     * starts with the filtered V4AddressPools followed by non-filtered V4AddressPools.
     * 
     * @param link the configuration Link object
     * 
     * @return the list of V4AddressBindingPools (<? extends BindingPool>)
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
		    					V4AddressBindingPool abp = buildV4BindingPool(pool, link, linkFilter);
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
					V4AddressBindingPool abp = buildV4BindingPool(pool, link);
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
     * within the given list of V4AddressBindingPools.
     * 
     * @param bindingPools the list of V4AddressBindingPools
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
     * @param pool the V4AddressPool to wrap as an V4AddressBindingPool
     * @param link the link
     * 
     * @return the binding pool
     * 
     * @throws DhcpServerConfigException if there is a problem parsing the configured range
     */
    protected V4AddressBindingPool buildV4BindingPool(V4AddressPool pool, Link link) 
    		throws DhcpServerConfigException
    {
    	return buildV4BindingPool(pool, link, null);
    }

    /**
     * Builds a binding pool from an V4AddressPool using the given link and filter.
     * 
     * @param pool the V4AddressPool to wrap as an V4AddressBindingPool
     * @param link the link
     * @param linkFilter the link filter
     * 
     * @return the binding pool
     * 
     * @throws DhcpServerConfigException if there is a problem parsing the configured range
     */
    protected V4AddressBindingPool buildV4BindingPool(V4AddressPool pool, Link link, 
    		LinkFilter linkFilter) throws DhcpServerConfigException
    {
    	V4AddressBindingPool bp = new V4AddressBindingPool(pool);
		long leasetime = 
			DhcpServerPolicies.effectivePolicyAsLong(bp, link, Property.V4_DEFAULT_LEASETIME);
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
		log.info("Built v4 address binding pool: " + bp.getStartAddress().getHostAddress() + "-" + 
				bp.getEndAddress().getHostAddress() + " size=" + bp.getSize());
    	return bp;
    }
    
    protected List<? extends StaticBinding> buildStaticBindings(Link link) 
			throws DhcpServerConfigException
	{
		List<V4StaticAddressBinding> staticBindings = new ArrayList<V4StaticAddressBinding>();
		V4AddressBindingsType bindingsType = link.getV4AddrBindings();
		if (bindingsType != null) {
			List<V4AddressBinding> bindings = bindingsType.getBindingList();
			if ((bindings != null) && !bindings.isEmpty()) {
				for (V4AddressBinding binding : bindings) {
					V4StaticAddressBinding sab = buildV4StaticBinding(binding, link);
					staticBindings.add(sab);
				}
			}
		}
		
		return staticBindings;
	}
	
	protected V4StaticAddressBinding buildV4StaticBinding(V4AddressBinding binding, Link link) 
			throws DhcpServerConfigException
	{
		try {
			InetAddress inetAddr = InetAddress.getByName(binding.getIpAddress());
			V4StaticAddressBinding sb = new V4StaticAddressBinding(binding);
			setIpAsUsed(link, inetAddr);
			return sb;
		}
		catch (UnknownHostException ex) {
			log.error("Invalid static binding address", ex);
			throw new DhcpServerConfigException("Invalid static binding address", ex);
		}
	}

	@Override
	public Binding findCurrentBinding(DhcpLink clientLink, byte[] macAddr, 
			DhcpMessage requestMsg) {
		
		return super.findCurrentBinding(clientLink, macAddr, IdentityAssoc.V4_TYPE, 
				0, requestMsg);
	}

	@Override
	public Binding createDiscoverBinding(DhcpLink clientLink, byte[] macAddr, 
			DhcpMessage requestMsg, byte state)
	{
		StaticBinding staticBinding = 
			findStaticBinding(clientLink.getLink(), macAddr, IdentityAssoc.V4_TYPE, 0, requestMsg);
		
		if (staticBinding != null) {
			return super.createStaticBinding(clientLink, macAddr, IdentityAssoc.V4_TYPE, 
					0, staticBinding, requestMsg);
		}
		else {
			return super.createBinding(clientLink, macAddr, IdentityAssoc.V4_TYPE, 
					0, getInetAddrs(requestMsg), requestMsg, state);
		}		
	}

	@Override
	public Binding updateBinding(Binding binding, DhcpLink clientLink, 
			byte[] macAddr, DhcpMessage requestMsg, byte state) {

		StaticBinding staticBinding = 
			findStaticBinding(clientLink.getLink(), macAddr, IdentityAssoc.V4_TYPE, 0, requestMsg);
		
		if (staticBinding != null) {
			return super.updateStaticBinding(binding, clientLink, macAddr, IdentityAssoc.V4_TYPE, 
					0, staticBinding, requestMsg);
		}
		else {
			return super.updateBinding(binding, clientLink, macAddr, IdentityAssoc.V4_TYPE,
					0, getInetAddrs(requestMsg), requestMsg, state);
		}		
	}
	
	/**
	 * Get the Requested IP addresses from the client message, if any was provided.
	 * 
	 * @param requestMsg the request msg
	 * 
	 * @return a list of InetAddresses containing the requested IP, or null if none requested or
	 *         if the requested IP is bogus
	 */
	private List<InetAddress> getInetAddrs(DhcpMessage requestMsg)
	{
		List<InetAddress> inetAddrs = null;
		DhcpV4RequestedIpAddressOption reqIpOption = (DhcpV4RequestedIpAddressOption)
			requestMsg.getDhcpOption(DhcpConstants.V4OPTION_REQUESTED_IP);
		if (reqIpOption != null) {
			try {
				InetAddress inetAddr = 
					InetAddress.getByName(reqIpOption.getIpAddress());
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
	protected Binding buildBindingFromIa(IdentityAssoc ia, DhcpLink clientLink,
			DhcpMessage requestMsg)
	{
		Binding binding = new Binding(ia, clientLink);
		Collection<? extends IaAddress> iaAddrs = ia.getIaAddresses();
		if ((iaAddrs != null) && !iaAddrs.isEmpty()) {
			List<V4BindingAddress> bindingAddrs = new ArrayList<V4BindingAddress>();
			for (IaAddress iaAddr : iaAddrs) {
				if (!clientLink.getSubnet().contains(iaAddr.getIpAddress())) {
					log.info("Ignoring off-link binding address: " + 
							iaAddr.getIpAddress().getHostAddress());
					continue;
				}
				V4BindingAddress bindingAddr = null;
        		StaticBinding staticBinding =
        			findStaticBinding(clientLink.getLink(), ia.getDuid(), 
        					ia.getIatype(), ia.getIaid(), requestMsg);
        		if (staticBinding != null) {
        			bindingAddr = 
        				buildV4StaticBindingFromIaAddr(iaAddr, staticBinding);
        		}
        		else {
        			bindingAddr =
        				buildV4BindingAddressFromIaAddr(iaAddr, clientLink.getLink(), requestMsg);
        		}
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
	 * Create a V4BindingAddress given an IaAddress loaded from the database.
	 * 
	 * @param iaAddr the ia addr
	 * @param clientLink the client link
	 * @param requestMsg the request msg
	 * 
	 * @return the binding address
	 */
	private V4BindingAddress buildV4BindingAddressFromIaAddr(IaAddress iaAddr, 
			Link clientLink, DhcpMessage requestMsg)
	{
		InetAddress inetAddr = iaAddr.getIpAddress();
		BindingPool bp = findBindingPool(clientLink, inetAddr, requestMsg);
		if (bp != null) {
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
	
	private V4BindingAddress buildV4StaticBindingFromIaAddr(IaAddress iaAddr, 
			StaticBinding staticBinding)
	{
		V4BindingAddress bindingAddr = new V4BindingAddress(iaAddr, staticBinding);
		return bindingAddr;
	}

	@Override
	protected BindingObject buildBindingObject(InetAddress inetAddr,
			DhcpLink clientLink, DhcpMessage requestMsg)
	{
		V4AddressBindingPool bp = 
			(V4AddressBindingPool) findBindingPool(clientLink.getLink(), inetAddr, requestMsg);
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
	protected void ddnsDelete(IdentityAssoc ia, IaAddress iaAddr) {
    	DhcpV4ClientFqdnOption clientFqdnOption = null;
    	try {
	    	if ((ia != null) && (iaAddr != null)) {
	    		Collection<DhcpOption> opts = iaAddr.getDhcpOptions();
	    		if (opts != null) {
	    			for (DhcpOption opt : opts) {
	    				if (opt.getCode() == DhcpConstants.V4OPTION_CLIENT_FQDN) {
	    					clientFqdnOption = new DhcpV4ClientFqdnOption();
	    					clientFqdnOption.decode(ByteBuffer.wrap(opt.getValue()));
	    					break;
	    				}
	    			}
	    		}
	    		if (clientFqdnOption != null) {
					String fqdn = clientFqdnOption.getDomainName();
					if ((fqdn != null) && !fqdn.isEmpty()) {
			        	DhcpLink link = serverConfig.findLinkForAddress(iaAddr.getIpAddress());
			        	if (link != null) {
			        		V4BindingAddress bindingAddr = null;
			        		StaticBinding staticBinding =
			        			findStaticBinding(link.getLink(), ia.getDuid(), 
			        					ia.getIatype(), ia.getIaid(), null);
			        		if (staticBinding != null) {
			        			bindingAddr = 
			        				buildV4StaticBindingFromIaAddr(iaAddr, staticBinding);
			        		}
			        		else {
			        			bindingAddr =
			        				buildV4BindingAddressFromIaAddr(iaAddr, link.getLink(), null);	// safe to send null requestMsg
			        		}
			        		if (bindingAddr != null) {
			        			
			        			DdnsCallback ddnsComplete = 
			        				new DhcpV4DdnsComplete(bindingAddr, clientFqdnOption);
			        			
			        			DhcpConfigObject configObj = bindingAddr.getConfigObj();
			        			
								DdnsUpdater ddns =
									new DdnsUpdater(link.getLink(), configObj,
											bindingAddr.getIpAddress(), fqdn, ia.getDuid(),
											configObj.getValidLifetime(),
											clientFqdnOption.getUpdateABit(), true,
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
	    			log.warn("No Client FQDN option in current binding.  No DDNS deletes performed.");
	    		}
	    	}
    	}
    	catch (Exception ex) {
    		log.error("Failed to perform DDNS delete", ex);
    	}
	}

	@Override
	protected byte getIaType() {
		return IdentityAssoc.V4_TYPE;
	}
}
