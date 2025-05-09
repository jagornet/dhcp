/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file V6AddrBindingManager.java is part of Jagornet DHCP.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.message.DhcpMessage;
import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.option.base.DhcpOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6ClientFqdnOption;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.server.config.DhcpConfigObject;
import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.config.DhcpServerConfigException;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.config.xml.Link;
import com.jagornet.dhcp.server.config.xml.LinkFilter;
import com.jagornet.dhcp.server.config.xml.LinkFiltersType;
import com.jagornet.dhcp.server.config.xml.V6AddressBinding;
import com.jagornet.dhcp.server.config.xml.V6AddressBindingsType;
import com.jagornet.dhcp.server.config.xml.V6AddressPool;
import com.jagornet.dhcp.server.config.xml.V6AddressPoolsType;
import com.jagornet.dhcp.server.db.IaAddress;
import com.jagornet.dhcp.server.db.IdentityAssoc;
import com.jagornet.dhcp.server.request.ddns.DdnsCallback;
import com.jagornet.dhcp.server.request.ddns.DdnsUpdater;
import com.jagornet.dhcp.server.request.ddns.DhcpV6DdnsComplete;

/**
 * The Class V6AddrBindingManager.  
 * Third-level abstract class that extends BaseAddrBindingManager to
 * add behavior specific to DHCPv6 address bindings.
 * 
 * @author A. Gregory Rabil
 */
public abstract class V6AddrBindingManager extends BaseAddrBindingManager
{
	private static Logger log = LoggerFactory.getLogger(V6AddrBindingManager.class);
    
	protected V6AddrBindingManager()
	{
		super();
	}
	
	/**
	 * Fetch the V6AddressPoolsType XML object from the given LinkFilter XML object.
	 * The subclasses fetch the address pools for either NA or TA addresses.
	 * 
	 * @param linkFilter the link filter
	 * @return the V6AddressPoolsType for this link filter, or null if none
	 */
    protected abstract V6AddressPoolsType getV6AddressPoolsType(LinkFilter linkFilter);
    
	/**
	 * Fetch the AddressPoolsType XML object from the given Link XML object.
	 * The subclasses fetch the address pools for either NA or TA addresses.
	 * 
	 * @param linkFilter the link
	 * @return the AddressPoolsType for this link, or null if none
	 */
    protected abstract V6AddressPoolsType getV6AddressPoolsType(Link link);
    
    /**
     * Build the list of V6AddressBindingPools from the list of configured V6AddressPools
     * for the given configuration Link container object. The list of V6AddressBindingPools
     * starts with the filtered AddressPools followed by non-filtered AddressPools.
     * 
     * @param link the configuration Link object
     * 
     * @return the list of V6AddressBindingPools (<? extends BindingPool>)
     * 
     * @throws DhcpServerConfigException if there is a problem parsing a configured range
     */
    protected List<? extends BindingPool> buildBindingPools(Link link) 
    		throws DhcpServerConfigException
    {
		List<V6AddressBindingPool> bindingPools = new ArrayList<>();
		// Put the filtered pools first in the list of pools on this link
		LinkFiltersType linkFiltersType = link.getLinkFilters();
		if (linkFiltersType != null) {
			List<LinkFilter> linkFilters = linkFiltersType.getLinkFilterList();
			if ((linkFilters != null) && !linkFilters.isEmpty()) {
				for (LinkFilter linkFilter : linkFilters) {
					V6AddressPoolsType poolsType = getV6AddressPoolsType(linkFilter);
					if (poolsType != null) {
						// add the filtered pools to the mapped list
		    			List<V6AddressPool> pools = poolsType.getPoolList();
		    			if ((pools != null) && !pools.isEmpty()) {
		    				for (V6AddressPool pool : pools) {
		    					V6AddressBindingPool abp = buildV6BindingPool(pool, link, linkFilter);
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
		V6AddressPoolsType poolsType = getV6AddressPoolsType(link);
		if (poolsType != null) {
			// add the unfiltered pools to the mapped list
			List<V6AddressPool> pools = poolsType.getPoolList();
			if ((pools != null) && !pools.isEmpty()) {
				for (V6AddressPool pool : pools) {
					V6AddressBindingPool abp = buildV6BindingPool(pool, link);
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
		
		return bindingPools;
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
    protected V6AddressBindingPool buildV6BindingPool(V6AddressPool pool, Link link) 
    		throws DhcpServerConfigException
    {
    	return buildV6BindingPool(pool, link, null);
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
    protected V6AddressBindingPool buildV6BindingPool(V6AddressPool pool, Link link, 
    		LinkFilter linkFilter) throws DhcpServerConfigException
    {
		V6AddressBindingPool bp = new V6AddressBindingPool(pool);
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
		log.info("Built address binding pool: " + bp.getStartAddress().getHostAddress() + "-" + 
				bp.getEndAddress().getHostAddress() + " size=" + bp.getSize());
    	return bp;
    }
    
    /**
     * Fetch the AddressBindings from the given XML Link object.
     * 
     * @param link
     * @return
     */
    protected abstract V6AddressBindingsType getV6AddressBindingsType(Link link);
    
    /**
     * Build the list of static bindings for the given link.
     * 
     * @param link the link
     * @return the list of static bindings
     * @throws DhcpServerConfigException if the static binding is invalid
     */
    protected List<? extends StaticBinding> buildStaticBindings(Link link) 
			throws DhcpServerConfigException
	{
		List<V6StaticAddressBinding> staticBindings = new ArrayList<>();
		V6AddressBindingsType bindingsType = getV6AddressBindingsType(link);
		if (bindingsType != null) {
			List<V6AddressBinding> bindings = bindingsType.getBindingList();
			if ((bindings != null) && !bindings.isEmpty()) {
				for (V6AddressBinding binding : bindings) {
					V6StaticAddressBinding sab = buildV6StaticBinding(binding, link);
					staticBindings.add(sab);
				}
			}
		}
		return staticBindings;
	}
    
    /**
     * Build a static address binding from the given address binding.
     * 
     * @param binding the address binding
     * @param link the link
     * @return the static address binding
     * @throws DhcpServerConfigException if the static binding is invalid
     */
    protected V6StaticAddressBinding buildV6StaticBinding(V6AddressBinding binding, Link link) 
    		throws DhcpServerConfigException
    {
    	try {
    		InetAddress inetAddr = InetAddress.getByName(binding.getIpAddress());
    		V6StaticAddressBinding sb = new V6StaticAddressBinding(binding, getIaType());
    		setIpAsUsed(link, inetAddr);
    		return sb;
    	}
    	catch (UnknownHostException ex) {
    		log.error("Invalid static binding address", ex);
    		throw new DhcpServerConfigException("Invalid static binding address", ex);
    	}
    }
    
    /**
     * Perform the DDNS delete processing when a lease is released or expired.
     * 
     * @param ia the IdentityAssoc of the client
     * @param iaAddr the released or expired IaAddress 
     */
    protected void ddnsDelete(IdentityAssoc ia, IaAddress iaAddr)
    {
    	DhcpV6ClientFqdnOption clientFqdnOption = null;
    	try {
	    	if ((ia != null) && (iaAddr != null)) {
	    		Collection<DhcpOption> opts = iaAddr.getDhcpOptions();
	    		if (opts != null) {
	    			for (DhcpOption opt : opts) {
	    				if (opt.getCode() == DhcpConstants.V6OPTION_CLIENT_FQDN) {
//	    					clientFqdnOption = new DhcpV6ClientFqdnOption();
//	    					clientFqdnOption.decode(ByteBuffer.wrap(opt.getValue()));
	    					clientFqdnOption = (DhcpV6ClientFqdnOption)opt;
	    					break;
	    				}
	    			}
	    		}
	    		if (clientFqdnOption != null) {
					String fqdn = clientFqdnOption.getDomainName();
					if ((fqdn != null) && !fqdn.isEmpty()) {
			        	DhcpLink link = serverConfig.findLinkForAddress(iaAddr.getIpAddress());
			        	if (link != null) {
			        		V6BindingAddress bindingAddr = null;
			        		StaticBinding staticBinding =
			        			findStaticBinding(link.getLink(), ia.getDuid(), 
			        					ia.getIatype(), ia.getIaid(), null);
			        		if (staticBinding != null) {
			        			bindingAddr = 
			        				buildV6StaticBindingFromIaAddr(iaAddr, staticBinding);
			        		}
			        		else {
			        			bindingAddr =
			        				buildV6BindingAddressFromIaAddr(iaAddr, link, null);	// safe to send null requestMsg
			        		}
			        		if (bindingAddr != null) {
			        			
			        			DdnsCallback ddnsComplete = 
			        				new DhcpV6DdnsComplete(bindingAddr, clientFqdnOption);
			        			
			        			DhcpConfigObject configObj = bindingAddr.getConfigObj();

			        			DdnsUpdater ddns =
									new DdnsUpdater(link.getLink(), configObj,
											bindingAddr.getIpAddress(), fqdn, ia.getDuid(),
											configObj.getValidLifetime(),
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
	    			log.info("No Client FQDN option in current binding.  No DDNS deletes performed.");
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
			DhcpLink clientLink, DhcpMessage requestMsg)
	{
		Binding binding = new Binding(ia, clientLink);
		Collection<? extends IaAddress> iaAddrs = ia.getIaAddresses();
		if ((iaAddrs != null) && !iaAddrs.isEmpty()) {
			List<V6BindingAddress> bindingAddrs = new ArrayList<>();
			for (IaAddress iaAddr : iaAddrs) {
// off-link check needed only for v4?
//				if (!clientLink.getSubnet().contains(iaAddr.getIpAddress())) {
//					log.info("Ignoring off-link binding address: " + 
//							iaAddr.getIpAddress().getHostAddress());
//					continue;
//				}
				V6BindingAddress bindingAddr = null;
        		StaticBinding staticBinding =
        			findStaticBinding(clientLink.getLink(), ia.getDuid(), 
        					ia.getIatype(), ia.getIaid(), requestMsg);
        		if (staticBinding != null) {
        			bindingAddr = 
        				buildV6StaticBindingFromIaAddr(iaAddr, staticBinding);
        		}
        		else {
        			bindingAddr =
        				buildV6BindingAddressFromIaAddr(iaAddr, clientLink, requestMsg);
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
	 * Build a V6BindingAddress from an IaAddress loaded from the database.
	 * 
	 * @param iaAddr the ia addr
	 * @param clientLink the client link
	 * @param requestMsg the request msg
	 * 
	 * @return the binding address
	 */
	private V6BindingAddress buildV6BindingAddressFromIaAddr(IaAddress iaAddr, 
			DhcpLink clientLink, DhcpMessage requestMsg)
	{
		InetAddress inetAddr = iaAddr.getIpAddress();
		V6AddressBindingPool bp = (V6AddressBindingPool)
				findBindingPool(clientLink.getLink(), inetAddr, requestMsg);
		if (bp != null) {
			// binding loaded from DB will contain the stored options
	    	V6BindingAddress bindingAddr = new V6BindingAddress(iaAddr, bp);
	    	// TODO: setBindingObjectTimes?  see buildBindingObject
			// update the options with whatever may now be configured
	    	setDhcpOptions(bindingAddr, clientLink, (DhcpV6Message)requestMsg, bp);
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
	 * Build a V6BindingAddress from an IaAddress loaded from the database
	 * and a static binding for the client request.
	 * 
	 * @param iaAddr
	 * @param staticBinding
	 * @return
	 */
	private V6BindingAddress buildV6StaticBindingFromIaAddr(IaAddress iaAddr, 
			StaticBinding staticBinding)
	{
		V6BindingAddress bindingAddr = new V6BindingAddress(iaAddr, staticBinding);
		return bindingAddr;
	}
	
	/**
	 * Create a new V6BindingAddress type BindingObject
	 * for the given InetAddress and DhcpLink.
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
		V6AddressBindingPool bp = (V6AddressBindingPool) 
				findBindingPool(clientLink.getLink(), inetAddr, requestMsg);
		if (bp != null) {
			bp.setUsed(inetAddr);	// TODO check if this is necessary
			IaAddress iaAddr = new IaAddress();
			iaAddr.setIpAddress(inetAddr);
			V6BindingAddress bindingAddr = new V6BindingAddress(iaAddr, bp);
			setBindingObjectTimes(bindingAddr, 
					bp.getPreferredLifetimeMs(), bp.getPreferredLifetimeMs());
			setDhcpOptions(bindingAddr, clientLink, (DhcpV6Message)requestMsg, bp);
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
	 * Set both the map of options to be returned to the client and those
	 * same options as a list to be stored with the lease in the database
	 * 
	 * @param bindingAddr
	 * @param clientLink
	 * @param requestMsg
	 * @param bp
	 */
	protected void setDhcpOptions(V6BindingAddress bindingAddr, DhcpLink clientLink,
			DhcpV6Message requestMsg, V6AddressBindingPool bp) {
		// set the options to be returned to the client
		Map<Integer, com.jagornet.dhcp.core.option.base.DhcpOption> dhcpOptionMap =
				buildDhcpOptions(clientLink, requestMsg, bp);
		bindingAddr.setDhcpOptionMap(dhcpOptionMap);
		// these options are rarely used
		Map<Integer, com.jagornet.dhcp.core.option.base.DhcpOption> iaDhcpOptionMap =
				buildIaDhcpOptions(clientLink, requestMsg, bp);
		bindingAddr.setIaDhcpOptionMap(iaDhcpOptionMap);
		// these options are rarely used
		Map<Integer, com.jagornet.dhcp.core.option.base.DhcpOption> iaAddrDhcpOptionMap =
				buildIaAddrDhcpOptions(clientLink, requestMsg, bp);
		bindingAddr.setIaAddrDhcpOptionMap(iaAddrDhcpOptionMap);
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
	protected abstract Map<Integer, com.jagornet.dhcp.core.option.base.DhcpOption> buildDhcpOptions(
			DhcpLink clientLink, DhcpV6Message requestMsg, V6AddressBindingPool bp);
	protected abstract Map<Integer, com.jagornet.dhcp.core.option.base.DhcpOption> buildIaDhcpOptions(
			DhcpLink clientLink, DhcpV6Message requestMsg, V6AddressBindingPool bp);
	protected abstract Map<Integer, com.jagornet.dhcp.core.option.base.DhcpOption> buildIaAddrDhcpOptions(
			DhcpLink clientLink, DhcpV6Message requestMsg, V6AddressBindingPool bp);
}
