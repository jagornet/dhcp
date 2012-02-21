/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseBindingManager.java is part of DHCPv6.
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
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.db.IaAddress;
import com.jagornet.dhcpv6.db.IaManager;
import com.jagornet.dhcpv6.db.IdentityAssoc;
import com.jagornet.dhcpv6.message.DhcpMessageInterface;
import com.jagornet.dhcpv6.server.config.DhcpConfigObject;
import com.jagornet.dhcpv6.server.config.DhcpLink;
import com.jagornet.dhcpv6.server.config.DhcpServerConfigException;
import com.jagornet.dhcpv6.server.config.DhcpServerConfiguration;
import com.jagornet.dhcpv6.util.Subnet;
import com.jagornet.dhcpv6.xml.Link;
import com.jagornet.dhcpv6.xml.LinkFilter;

/**
 * The Class BaseBindingManager.
 * Top-level abstract class used by all BindingManagers.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseBindingManager
{
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(BaseBindingManager.class);
	
	/** The server config. */
	protected static DhcpServerConfiguration serverConfig = DhcpServerConfiguration.getInstance();

    /** The IdentityAssoc manager */
    protected IaManager iaMgr;

    /** 
     * The map of binding binding pools for this manager.  The key is the link address
     *  and the value is the list of configured BindingPools for the link. 
     */
    protected Map<String, List<? extends BindingPool>> bindingPoolMap;
    
    /**
     * The map of static bindings for this manager.  The key is the link address
     * and the value is the list of configured StaticBindings for the link. 
     */
    protected Map<String, List<? extends StaticBinding>> staticBindingMap;

	/** The reaper thread for cleaning expired bindings. */
	protected Timer reaper;
	
	/** The lock for access to the reuseAvailableAddress method */
	// the ReentrantLock class is better than synchronized
	private final ReentrantLock lock = new ReentrantLock();
	
	/**
	 * Initialize the manager.  Read the configuration and build
	 * the pool map and static bindings.
	 * @throws DhcpServerConfigException
	 */
	public void init() throws DhcpServerConfigException
	{
		initPoolMap();
		initStaticBindings();
		startReaper();
	}
	
    /**
     * Initialize the pool map.  Read through the link map from the server's
     * configuration and build the pool map keyed by link address with a
     * value of the list of (na/ta/v4 address or prefix) bindings for the link.
     * 
     * @throws DhcpServerConfigException the exception
     */
    protected void initPoolMap() throws DhcpServerConfigException
    {
		SortedMap<Subnet, DhcpLink> linkMap = serverConfig.getLinkMap();
		if ((linkMap != null) && !linkMap.isEmpty()) {
    		bindingPoolMap = new HashMap<String, List<? extends BindingPool>>();
			for (DhcpLink dhcpLink : linkMap.values()) {
				Link link = dhcpLink.getLink();
				List<? extends BindingPool> bindingPools = buildBindingPools(link);
				if ((bindingPools != null) && !bindingPools.isEmpty()) {
					bindingPoolMap.put(link.getAddress(), bindingPools);
				}
			}
		}
		else {
			log.error("LinkMap is null for DhcpServerConfiguration");
		}
    }


    /**
     * Build the list of BindingPools for the given Link.  The BindingPools
     * are either AddressBindingPools (NA and TA) or PrefixBindingPools
     * or V4AddressBindingPools.
     * 
     * @param link the configured Link
     * @return the list of BindingPools for the link
     * @throws DhcpServerConfigException
     */
    protected abstract List<? extends BindingPool> buildBindingPools(Link link) 
    		throws DhcpServerConfigException;
    
	
    /**
     * Initialize the static bindings.  Read through the link map from the server's
     * configuration and build the binding map keyed by link address with a
     * value of the list of (na/ta/v4 address or prefix) bindings for the link.
     * 
     * 
     * @throws DhcpServerConfigException the exception
     */
    protected void initStaticBindings() throws DhcpServerConfigException
    {
		SortedMap<Subnet, DhcpLink> linkMap = serverConfig.getLinkMap();
		if (linkMap != null) {
    		staticBindingMap = new HashMap<String, List<? extends StaticBinding>>();
			for (DhcpLink dhcpLink : linkMap.values()) {
				Link link = dhcpLink.getLink();
				List<? extends StaticBinding> staticBindings = buildStaticBindings(link);
				if ((staticBindings != null) && !staticBindings.isEmpty()) {
					staticBindingMap.put(link.getAddress(), staticBindings);
				}
			}
		}
    }
    
    /**
     * Initialize the static bindings for the given Link.  The StaticBindings
     * are either StaticAddressBindings (NA and TA) or StaticPrefixBindings
     * or StaticV4AddressBindings.
     * 
     * @param link the configured Link
     * @return the list of StaticBindings for the link
     * @throws DhcpServerConfigException
     */
    protected abstract List<? extends StaticBinding> buildStaticBindings(Link link) 
			throws DhcpServerConfigException;


	/**
	 * Start a reaper thread to check for expired bindings.
	 */
	protected abstract void startReaper();

	/**
	 * Find binding pool for address in a message received on the given link.
	 * 
	 * @param link the link on which the client message was received.
	 * @param inetAddr the IP address referenced in the message
	 * @param requestMsg the request message
	 * 
	 * @return the binding pool
	 */
	protected BindingPool findBindingPool(Link link, InetAddress inetAddr, 
			DhcpMessageInterface requestMsg)
	{
		List<? extends BindingPool> bps = bindingPoolMap.get(link.getAddress());
		if ((bps != null) && !bps.isEmpty()) {
			for (BindingPool bindingPool : bps) {
//				if (log.isDebugEnabled()) {
//					if (bindingPool instanceof AddressBindingPool) {
//						AddressBindingPool abp = (AddressBindingPool) bindingPool;
//						log.debug("AddressBindingPool: " + abp.toString());
//						log.debug("FreeList: " + abp.freeListToString());
//					}
//				}
				if (bindingPool.contains(inetAddr)) {
					if ((requestMsg != null) && (bindingPool.getLinkFilter() != null)) {
						if (DhcpServerConfiguration.msgMatchesFilter(requestMsg, 
								bindingPool.getLinkFilter())) {
							log.info("Found filter binding pool: " + bindingPool);
							return bindingPool;
						}
					}
					else {
						log.info("Found binding pool: " + bindingPool);
						return bindingPool;
					}
				}
			}
		}
		return null;
	}
    
	/**
	 * Find binding pool for the given IP address.  Search through all
	 * the pools on all links to find the IP's binding pool.
	 * 
	 * @param inetAddr the IP address
	 * 
	 * @return the binding pool
	 */
	protected BindingPool findBindingPool(InetAddress inetAddr)
	{
		Collection<List<? extends BindingPool>> allPools = bindingPoolMap.values();
		if ((allPools != null) && !allPools.isEmpty()) {
			for (List<? extends BindingPool> bps : allPools) {
				for (BindingPool bindingPool : bps) {
					if (bindingPool.contains(inetAddr))
						log.info("Found binding pool for address=" + 
								inetAddr.getHostAddress() +
								": " + bindingPool);
						return bindingPool;
				}
			}
		}
		return null;
	}
    
    /**
     * Sets an IP address as in-use in it's binding pool.
     * 
     * @param link the link for the message
     * @param inetAddr the IP address to set used
     */
    protected void setIpAsUsed(Link link, InetAddress inetAddr)
    {
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
	 * Sets and IP address as free in it's binding pool.
	 * 
	 * @param inetAddr the IP address to free
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
    
	/**
	 * Find the current binding, if any, for the given client identity association (IA).
	 * 
	 * @param clientLink the link for the client request message
	 * @param duid the DUID of the client
	 * @param iatype the IA type of the client request
	 * @param iaid the IAID of the client request
	 * @param requestMsg the client request message
	 * @return the existing Binding for this client request
	 */
	protected Binding findCurrentBinding(Link clientLink, byte[] duid, byte iatype, long iaid,
			DhcpMessageInterface requestMsg) 
	{
		Binding binding = null;
		try {
			IdentityAssoc ia = iaMgr.findIA(duid, iatype, iaid);
			if (ia != null) {
				log.info("Found current binding for " + 
						IdentityAssoc.keyToString(duid, iatype, iaid));
				binding = buildBindingFromIa(ia, clientLink, requestMsg);
				if (binding != null) {
					log.info("Successfully built Binding object: " + binding);
				}
				else {
					log.error("Failed to build Binding object");
				}
			}
			else {
				log.info("No current binding found for " +
						IdentityAssoc.keyToString(duid, iatype, iaid));
			}
		}
		catch (Exception ex) {
			log.error("Failed to find current binding", ex);
		}
		return binding;
	}
	
	/**
	 * Find a static binding, if any, for the given client identity association (IA).
	 * 
	 * @param clientLink the link for the client request message
	 * @param duid the DUID of the client
	 * @param iatype the IA type of the client request
	 * @param iaid the IAID of the client request
	 * @param requestMsg the client request message
	 * @return the existing StaticBinding for this client request
	 */
	public StaticBinding findStaticBinding(Link clientLink, byte[] duid, byte iatype, long iaid,
			DhcpMessageInterface requestMsg)
	{
		try {
			List<? extends StaticBinding> staticBindings = 
				staticBindingMap.get(clientLink.getAddress());
			if ((staticBindings != null) && !staticBindings.isEmpty()) {
				for (StaticBinding staticBinding : staticBindings) {
					if (staticBinding.matches(duid, iatype, iaid, requestMsg)) {
						log.info("Found static binding: " + staticBinding);
						return staticBinding;
					}
				}
			}
		}
		catch (Exception ex) {
			log.error("Exception in findStaticBinding", ex);
		}		
		return null;
	}

	/**
	 * Create a binding in response to a Solicit/Discover request for the given client IA.
	 * 
	 * @param clientLink the link for the client request message
	 * @param duid the DUID of the client
	 * @param iatype the IA type of the client request
	 * @param iaid the IAID of the client request
	 * @param requestAddrs the list of requested IP addresses, if any
	 * @param requestMsg the client request message
	 * @param state the binding state
	 * @return the created Binding
	 */
	protected Binding createBinding(Link clientLink, byte[] duid, byte iatype, long iaid,
			List<InetAddress> requestAddrs, DhcpMessageInterface requestMsg, byte state)
	{
		Binding binding = null;		
		List<InetAddress> inetAddrs = 
			getInetAddrs(clientLink, duid, iatype, iaid, requestAddrs, requestMsg);
		if ((inetAddrs != null) && !inetAddrs.isEmpty()) {
			binding = buildBinding(clientLink, duid, iatype, iaid, state);
			Set<BindingObject> bindingObjs = 
				buildBindingObjects(clientLink, inetAddrs, requestMsg, state);
			if ((bindingObjs != null) && !bindingObjs.isEmpty()) {
				binding.setBindingObjects(bindingObjs);
				try {
					iaMgr.createIA(binding);
				}
				catch (Exception ex) {
					log.error("Failed to create persistent binding", ex);
					return null;
				}
			}
			else {
				log.error("Failed to build binding object(s)");
				return null;
			}
		}
		
		String bindingType = (iatype == IdentityAssoc.V4_TYPE) ? "discover" : "solicit";
		if (binding != null) {
			log.info("Created " + bindingType + " binding: " + binding.toString());
		}
		else {
			log.warn("Failed to create " + bindingType + " binding");
		}
		return binding;
	}
	
	/**
	 * Create a binding in from a StaticBinding
	 * 
	 * @param clientLink the link for the client request message
	 * @param duid the DUID of the client
	 * @param iatype the IA type of the client request
	 * @param iaid the IAID of the client request
	 * @param staticBinding the static binding
	 * @param requestMsg the client request message
	 * @return the created Binding
	 */
	protected Binding createStaticBinding(Link clientLink, byte[] duid, byte iatype, long iaid,
			StaticBinding staticBinding, DhcpMessageInterface requestMsg)
	{
		Binding binding = null;		
		if (staticBinding != null) {
			binding = buildBinding(clientLink, duid, iatype, iaid, IaAddress.STATIC);
			InetAddress inetAddr = staticBinding.getInetAddress();
			if (inetAddr != null) {
				IaAddress iaAddr = new IaAddress();
				iaAddr.setIpAddress(inetAddr);
				iaAddr.setState(IaAddress.STATIC);
				BindingAddress bindingAddr = 
					new BindingAddress(iaAddr, (DhcpConfigObject)staticBinding);
				setBindingObjectTimes(bindingAddr, 
						staticBinding.getPreferredLifetimeMs(), 
						staticBinding.getPreferredLifetimeMs());
				// TODO store the configured options in the persisted binding?
				// bindingAddr.setDhcpOptions(bp.getDhcpOptions());
				Collection<BindingObject> bindingObjs = new ArrayList<BindingObject>();
				bindingObjs.add(bindingAddr);
				binding.setBindingObjects(bindingObjs);
				try {
					iaMgr.createIA(binding);
				}
				catch (Exception ex) {
					log.error("Failed to create persistent binding", ex);
					return null;
				}
			}
			else {
				log.error("Failed to build binding object(s)");
				return null;
			}
		}
		else {
			log.error("StaticBinding object is null");
		}
		
		String bindingType = (iatype == IdentityAssoc.V4_TYPE) ? "discover" : "solicit";
		if (binding != null) {
			log.info("Created static " + bindingType + " binding: " + binding.toString());
		}
		else {
			log.warn("Failed to create static " + bindingType + " binding");
		}
		return binding;		
	}
	
	
	/**
	 * Update an existing client binding.  Addresses in the current binding that are appropriate
	 * for the client's link are simply updated with new lifetimes.  If no current bindings are
	 * appropriate for the client link, new binding addresses will be created and added to the
	 * existing binding, leaving any other addresses alone.
	 * 
	 * @param binding the existing client binding
	 * @param clientLink the link for the client request message
	 * @param duid the DUID of the client
	 * @param iatype the IA type of the client request
	 * @param iaid the IAID of the client request
	 * @param requestAddrs the list of requested IP addresses, if any
	 * @param requestMsg the client request message
	 * @param state the new state for the binding
	 * @return the updated Binding
	 */
	protected Binding updateBinding(Binding binding, Link clientLink, byte[] duid, byte iatype, long iaid,
			List<InetAddress> requestAddrs, DhcpMessageInterface requestMsg, byte state)
	{
		Collection<? extends IaAddress> addIaAddresses = null;
		Collection<? extends IaAddress> updateIaAddresses = null;
		Collection<? extends IaAddress> delIaAddresses = null;	// not used currently
		
		log.info("Updating dynamic binding: " + binding);
		
		Collection<BindingObject> bindingObjs = binding.getBindingObjects();
		if ((bindingObjs != null) && !bindingObjs.isEmpty()) {
			// current binding has addresses, so update times
			setBindingObjsTimes(bindingObjs);
			// the existing IaAddress binding objects will be updated
			updateIaAddresses = binding.getIaAddresses();
		}
		else {
			log.warn("Existing binding has no on-link addresses, allocating new address(es)");
			// current binding has no addresses, add new one(s)
			List<InetAddress> inetAddrs = 
				getInetAddrs(clientLink, duid, iatype, iaid, requestAddrs, requestMsg);
			if ((inetAddrs == null) || inetAddrs.isEmpty()) {
				log.error("Failed to update binding, no addresses available");
				return null;
			}
			bindingObjs = buildBindingObjects(clientLink, inetAddrs, requestMsg, state);
			binding.setBindingObjects(bindingObjs);
			// these new IaAddress binding objects will be added
			addIaAddresses = binding.getIaAddresses();
		}
		binding.setState(state);
		try {
			iaMgr.updateIA(binding, addIaAddresses, updateIaAddresses, delIaAddresses);
			return binding;	// if we get here, it worked
		}
		catch (Exception ex) {
			log.error("Failed to update binding", ex);
			return null;
		}
	}

	/**
	 * Update an existing static binding.
	 * 
	 * @param binding the existing client binding
	 * @param clientLink the link for the client request message
	 * @param duid the DUID of the client
	 * @param iatype the IA type of the client request
	 * @param iaid the IAID of the client request
	 * @param staticBinding the static binding
	 * @param requestMsg the client request message
	 * @return the updated Binding
	 */
	protected Binding updateStaticBinding(Binding binding, Link clientLink, 
			byte[] duid, byte iatype, long iaid, StaticBinding staticBinding,
			DhcpMessageInterface requestMsg)
	{
		Collection<? extends IaAddress> addIaAddresses = null;
		Collection<? extends IaAddress> updateIaAddresses = null;
		Collection<? extends IaAddress> delIaAddresses = null;	// not used currently

		if (staticBinding != null) {
			log.info("Updating static binding: " + binding);
			Collection<BindingObject> bindingObjs = binding.getBindingObjects();
			if ((bindingObjs != null) && !bindingObjs.isEmpty()) {
				for (BindingObject bindingObj : bindingObjs) {
					if (bindingObj.getIpAddress().equals(staticBinding.getInetAddress())) {
						setBindingObjectTimes(bindingObj, 
								staticBinding.getPreferredLifetimeMs(), 
								staticBinding.getPreferredLifetimeMs());
						break;
					}
					//TODO: what about bindingObjs that do NOT match the static binding?
				}
				// the existing IaAddress binding objects will be updated
				updateIaAddresses = binding.getIaAddresses();
			}
			else {
				InetAddress inetAddr = staticBinding.getInetAddress();
				if (inetAddr != null) {
					BindingObject bindingObj = buildStaticBindingObject(inetAddr, staticBinding);
					bindingObjs = new ArrayList<BindingObject>();
					bindingObjs.add(bindingObj);
					binding.setBindingObjects(bindingObjs);
					// this new IaAddress binding object will be added
					addIaAddresses = binding.getIaAddresses();
				}
			}
		}
		else {
			log.error("StaticBindingObject is null");
		}

		binding.setState(IaAddress.STATIC);
		try {
			iaMgr.updateIA(binding, addIaAddresses, updateIaAddresses, delIaAddresses);
			return binding;	// if we get here, it worked
		}
		catch (Exception ex) {
			log.error("Failed to update binding", ex);
			return null;
		}
	}

	/**
	 * Get list of IP addresses for the given client IA request.
	 * 
	 * @param clientLink the link for the client request message
	 * @param duid the DUID of the client
	 * @param iatype the IA type of the client request
	 * @param iaid the IAID of the client request
	 * @param requestAddrs the list of requested IP addresses, if any
	 * @param requestMsg the client request message
	 * @return the list of InetAddresses
	 */
	protected List<InetAddress> getInetAddrs(Link clientLink, byte[] duid, byte iatype, long iaid,
			List<InetAddress> requestAddrs, DhcpMessageInterface requestMsg)
	{
		List<InetAddress> inetAddrs = new ArrayList<InetAddress>();
		
		if ((requestAddrs != null) && !requestAddrs.isEmpty()) {
			for (InetAddress reqAddr : requestAddrs) {
				BindingPool bp = findBindingPool(clientLink, reqAddr, requestMsg);
				if (bp == null) {
					log.warn("No BindingPool found for requested client address: " +
							reqAddr.getHostAddress());
					if (iatype == IdentityAssoc.PD_TYPE) {
						// TAHI tests want NoPrefixAvail in this case
						log.warn("Requested prefix is not available, returning");
						return inetAddrs;
					}
					// if there is no pool for the requested address, then skip it
					// because that address is either off-link or no longer valid
					continue;
				}
				log.info("Searching existing bindings for requested IP=" +
						reqAddr.getHostAddress());
				IdentityAssoc ia = null;
				try {
					ia = iaMgr.findIA(reqAddr); 
					if (ia != null) {
						// the address is assigned to an IA, which we
						// don't expect to be this IA, because we would
						// have found it using findCurrentBinding...
						// but, perhaps another thread just created it?
						if (isMyIa(duid, iatype, iaid, ia)) {
							log.warn("Requested IP=" + reqAddr.getHostAddress() +
									" is already held by THIS client " +
									IdentityAssoc.keyToString(duid, iatype, iaid) +
									".  Allowing this requested IP.");
						}
						else {
							log.info("Requested IP=" + reqAddr.getHostAddress() +
									" is held by ANOTHER client " +
									IdentityAssoc.keyToString(duid, iatype, iaid));
							// the address is held by another IA, so get a new one
							reqAddr = getNextFreeAddress(clientLink, requestMsg);
						}
					}
					if (reqAddr != null) {
						inetAddrs.add(reqAddr);
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
	 * Checks if the duid-iatype-iaid tuple matches the given IA.
	 * 
	 * @param duid the DUID
	 * @param iatype the IA type
	 * @param iaid the IAID
	 * @param ia the IA
	 * 
	 * @return true, if is my ia
	 */
	protected boolean isMyIa(byte[] duid, byte iatype, long iaid, IdentityAssoc ia)
	{
		boolean rc = false;
		if (duid != null) {
			if (Arrays.equals(ia.getDuid(), duid) &&
				(ia.getIatype() == iatype) &&
				(ia.getIaid() == iaid)) {
				rc = true;
			}
		}		
		return rc;
	}
	
	/**
	 * Gets the next free address from the pool(s) configured for the client link.
	 * 
	 * @param clientLink the client link
	 * @param requestMsg the request message
	 * 
	 * @return the next free address
	 */
	protected InetAddress getNextFreeAddress(Link clientLink, DhcpMessageInterface requestMsg)
	{
		if (clientLink != null) {
    		List<? extends BindingPool> pools = bindingPoolMap.get(clientLink.getAddress());
    		if ((pools != null) && !pools.isEmpty()) {
    			for (BindingPool bp : pools) {
    				LinkFilter filter = bp.getLinkFilter();
    				if ((requestMsg != null) && (filter != null)) {
    					if (!DhcpServerConfiguration.msgMatchesFilter(requestMsg, filter)) {
    						log.info("Client request does not match filter, skipping pool: " +
    								bp.toString());
    						continue;
    					}
    				}
					if (log.isDebugEnabled())
						log.debug("Getting next available address from pool: " +
								bp.toString());
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
								bp.toString());
					}
    			}
    			// if we get this far, then we did not find any free(virgin) addresses
    			// so we must start searching for any that can be used in the pools
    			for (BindingPool bp : pools) {
    				LinkFilter filter = bp.getLinkFilter();
    				if ((requestMsg != null) && (filter != null)) {
    					if (!DhcpServerConfiguration.msgMatchesFilter(requestMsg, filter)) {
    						log.info("Client request does not match filter, skipping pool: " +
    								bp.toString());
    						continue;
    					}
    				}
    				InetAddress reused = reuseAvailableAddress(bp);
    				if (reused != null) {
    					return reused;
    				}
    			}
    		}
    		else {
	    		log.error("No Pools defined in server configuration for Link: " +
	    				clientLink.getAddress());
    		}
		}
		else {
			throw new IllegalStateException("ClientLink is null");
		}
		return null;
	}
	
	/**
	 * Find an address that can be reused.  This method is invoked only
	 * when no "virgin" leases can be found for a new client request.
	 * 
	 * @param bp the binding pool
	 * @return the oldest available address, if any
	 */
	protected InetAddress reuseAvailableAddress(BindingPool bp)
	{
		lock.lock();
		try {
			if (log.isDebugEnabled())
				log.debug("Finding available addresses in pool: " +
						bp.toString());
			List<IaAddress> iaAddrs = 
				iaMgr.findUnusedIaAddresses(bp.getStartAddress(), bp.getEndAddress());
			if ((iaAddrs != null) && !iaAddrs.isEmpty()) {
				if (log.isDebugEnabled()) {
					for (IaAddress iaAddr : iaAddrs) {
						log.debug("Found available address: " + iaAddr.toString());
					}
				}
				// list is ordered by validendtime
				// so the first one is the oldest one
				IaAddress iaAddr = iaAddrs.get(0);
				log.info("Deleting oldest available address: " + iaAddr.toString());
				// delete the oldest one and return the IP
				// allowing that IP to be used again
				iaMgr.deleteIaAddr(iaAddr);
				return iaAddr.getIpAddress();
// TODO: should we clear the rest of unused IPs
// now, or wait for them to expire or be needed
//				for (int i=1; i<iaAddrs.size(); i++) {
//					
//				}
			}
		}
		finally {
			lock.unlock();
		}
		return null;
	}
	
	/**
	 * Create a Binding given an IdentityAssoc loaded from the database.
	 * 
	 * @param ia the IA
	 * @param clientLink the client link
	 * @param requestMsg the request message
	 * 
	 * @return the binding
	 */
	protected abstract Binding buildBindingFromIa(IdentityAssoc ia, 
			Link clientLink, DhcpMessageInterface requestMsg);

	/**
	 * Builds a new Binding.  Create a new IdentityAssoc from the given
	 * tuple and wrap that IdentityAssoc in a Binding.
	 * 
	 * @param clientLink the client link
	 * @param duid the DUID
	 * @param iatype the IA type
	 * @param iaia the IAID
	 * 
	 * @return the binding (a wrapped IdentityAssoc)
	 */
	private Binding buildBinding(Link clientLink, byte[] duid, byte iatype, long iaid,
			byte state)
	{
		IdentityAssoc ia = new IdentityAssoc();
		ia.setDuid(duid);
		ia.setIatype(iatype);
		ia.setIaid(iaid);
		ia.setState(state);
		return new Binding(ia, clientLink);		
	}
	
	/**
	 * Builds the set of binding objects for the client request.
	 * 
	 * @param clientLink the client link
	 * @param inetAddrs the list of IP addresses for the client binding
	 * @param requestMsg the request message
	 * @param state the binding state
	 * 
	 * @return the set<binding object>
	 */
	private Set<BindingObject> buildBindingObjects(Link clientLink, 
			List<InetAddress> inetAddrs, DhcpMessageInterface requestMsg,
			byte state)
	{
		Set<BindingObject> bindingObjs = new HashSet<BindingObject>();
		for (InetAddress inetAddr : inetAddrs) {
			if (log.isDebugEnabled())
				log.debug("Building BindingObject for IP=" + inetAddr.getHostAddress());
			BindingObject bindingObj = buildBindingObject(inetAddr, clientLink, requestMsg);
			if (bindingObj != null) {
				bindingObj.setState(state);
				bindingObjs.add(bindingObj);
			}
			else {
				log.warn("Failed to build BindingObject for IP=" + inetAddr.getHostAddress());
			}
		}
		return bindingObjs;
	}
	
	/**
	 * Build the appropriate type of BindingObject.  This method is implemented by the
	 * subclasses to create an NA/TA BindingAddress object or a BindingPrefix object
	 * or a V4AddressBinding object.
	 * 
	 * @param inetAddr the IP address for this binding object
	 * @param clientLink the client link
	 * @param requestMsg the request message
	 * @return the binding object
	 */
	protected abstract BindingObject buildBindingObject(InetAddress inetAddr, 
			Link clientLink, DhcpMessageInterface requestMsg);


	/**
	 * Build a BindingObject from a static binding.  Because the StaticBinding
	 * implements the DhcpConfigObject interface, it can be used directly in
	 * creating the BindingObject, unlike buildBindingObject above which is
	 * abstract because each implementation must lookup the object in that
	 * binding manager's map of binding pools.
	 * 
	 * @param inetAddr the IP address for this binding object
	 * @param staticBinding the static binding
	 * @return the binding object
	 */
	protected BindingObject buildStaticBindingObject(InetAddress inetAddr,
			StaticBinding staticBinding)
	{
		IaAddress iaAddr = new IaAddress();
		iaAddr.setIpAddress(inetAddr);
		BindingAddress bindingAddr = new BindingAddress(iaAddr, staticBinding);
		setBindingObjectTimes(bindingAddr, 
				staticBinding.getPreferredLifetimeMs(), 
				staticBinding.getPreferredLifetimeMs());
		return bindingAddr;
	}
	
	/**
	 * Sets the lifetimes of the given collection of binding objects.
	 * 
	 * @param bindingObjs the collection of binding objects to set lifetimes in
	 */
	protected void setBindingObjsTimes(Collection<BindingObject> bindingObjs) 
	{
		if ((bindingObjs != null) && !bindingObjs.isEmpty()) {
			for (BindingObject bindingObj : bindingObjs) {
				DhcpConfigObject configObj = bindingObj.getConfigObj();
				setBindingObjectTimes(bindingObj, 
						configObj.getPreferredLifetimeMs(), 
						configObj.getValidLifetimeMs());
				//TODO: if we store the options, and they have changed,
				// 		then we must update those options here somehow
			}
		}
	}

	/**
	 * Sets the lifetimes of the given binding object.
	 * 
	 * @param bindingObj the binding object
	 * @param preferred the preferred lifetime in ms
	 * @param valid the valid lifetime in ms
	 */
	protected void setBindingObjectTimes(BindingObject bindingObj, long preferred, long valid)
	{
		if (log.isDebugEnabled())
			log.debug("Updating binding times for address: " + 
					bindingObj.getIpAddress().getHostAddress() +
					" preferred=" + preferred + ", valid=" + valid);
		Date now = new Date();
		bindingObj.setStartTime(now);
		if (preferred < 0) {
			// infinite lease
			bindingObj.setPreferredEndTime(new Date(-1));
		}
		else {
			long pEndTime = now.getTime() + preferred;
			bindingObj.setPreferredEndTime(new Date(pEndTime));
		}
		if (valid < 0) {
			// infinite lease
			bindingObj.setValidEndTime(new Date(-1));
		}
		else {
			long vEndTime = now.getTime() + valid;
			bindingObj.setValidEndTime(new Date(vEndTime));
		}
	}

	public IaManager getIaMgr() {
		return iaMgr;
	}

	public void setIaMgr(IaManager iaMgr) {
		this.iaMgr = iaMgr;
	}
}
