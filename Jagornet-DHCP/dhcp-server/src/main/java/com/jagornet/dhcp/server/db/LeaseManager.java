/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file LeaseManager.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.server.db;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.request.binding.Range;

public abstract class LeaseManager implements IaManager {
	
	private static Logger log = LoggerFactory.getLogger(LeaseManager.class);

	public abstract int insertDhcpLease(final DhcpLease lease);
	public abstract int updateDhcpLease(final DhcpLease lease);
	public abstract int deleteDhcpLease(final DhcpLease lease);
	public abstract int updateIpAddress(final InetAddress inetAddr, 
			final byte state, final byte haPeerState, final short prefixlen,
			final Date start, final Date preferred, final Date valid);
	public abstract int deleteIpAddress(final InetAddress inetAddr);
	public abstract int updateIaOptions(final InetAddress inetAddr, 
			final Collection<DhcpOption> iaOptions);
	public abstract int updateIpAddrOptions(final InetAddress inetAddr,
			final Collection<DhcpOption> ipAddrOptions);
	public abstract List<DhcpLease> findDhcpLeasesForIA(final byte[] duid, 
			final byte iatype, final long iaid);
	public abstract DhcpLease findDhcpLeaseForInetAddr(final InetAddress inetAddr);
	public abstract List<InetAddress> findExistingLeaseIPs(final InetAddress startAddr, 
			final InetAddress endAddr);
	public void findExistingLeaseIPs(final InetAddress startAddr, 
			final InetAddress endAddr, InetAddressCallbackHandler inetAddressCallbackHandler) {
		//TODO: implement in all subclasses
	}
	public void findExistingLeases(final InetAddress startAddr, 
								   final InetAddress endAddr,
								   DhcpLeaseCallbackHandler dhcpLeaseCallbackHandler) {
		//TODO: implement in all subclasses
	}
	public void findUnsyncedLeases(final InetAddress startAddr, 
			   final InetAddress endAddr,
			   DhcpLeaseCallbackHandler dhcpLeaseCallbackHandler) {
		//TODO: implement in all subclasses
	}
	public int setAllLeasesUnsynced() {
		//TODO: implement in all subclasses
		return 0;
	}
	public abstract List<DhcpLease> findUnusedLeases(final InetAddress startAddr, 
			final InetAddress endAddr);
	public abstract DhcpLease findUnusedLease(final InetAddress startAddr, 
			final InetAddress endAddr);
	public abstract List<DhcpLease> findExpiredLeases(final byte iatype);
	public abstract void reconcileLeases(final List<Range> ranges);
	public abstract int deleteAllLeases();
	
	protected long offerExpireMillis;
	protected IaCache iaCache = null;
	protected LeaseCache leaseCache = null;
	
	public void init() throws Exception {
		
		offerExpireMillis = DhcpServerPolicies.
				globalPolicyAsLong(Property.BINDING_MANAGER_OFFER_EXPIRATION);
		
		int cacheSize = DhcpServerPolicies.
				globalPolicyAsInt(Property.BINDING_MANAGER_IA_CACHE_SIZE);
		if (cacheSize > 0) {
			iaCache = new IaCache(cacheSize);
		}
		
		cacheSize = DhcpServerPolicies.
				globalPolicyAsInt(Property.BINDING_MANAGER_LEASE_CACHE_SIZE);
		if (cacheSize > 0) {
			leaseCache = new LeaseCache(cacheSize);
		}
	}
	
	// for unit testing
	public long getOfferExpireMillis() {
		return offerExpireMillis;
	}
	
	protected boolean useIaCache() {
		return iaCache != null;
	}
	
	protected boolean useLeaseCache() {
		return leaseCache != null;
	}

	@Override
	public void createIA(IdentityAssoc ia, Collection<DhcpOption> dhcpOptions) {
		if (ia != null) {
			List<DhcpLease> leases = toDhcpLeases(ia, dhcpOptions);
			if ((leases != null) && !leases.isEmpty()) {
				for (final DhcpLease lease : leases) {
					insertDhcpLease(lease);
					if (useLeaseCache()) {
						leaseCache.putLease(lease);
					}
				}
			}
			if (useIaCache()) {
				iaCache.putIA(ia);
			}
		}
	}
	
	@Override
	public void updateIA(IdentityAssoc ia, 
						 Collection<? extends IaAddress> addAddrs,
						 Collection<? extends IaAddress> updateAddrs,
						 Collection<? extends IaAddress> delAddrs,
						 Collection<DhcpOption> dhcpOptions)
	{
		if ((addAddrs != null) && !addAddrs.isEmpty()) {
			for (IaAddress addAddr : addAddrs) {
				DhcpLease lease = toDhcpLease(ia, addAddr, dhcpOptions);
				insertDhcpLease(lease);
				if (useLeaseCache()) {
					leaseCache.putLease(lease);
				}
			}
		}
		if ((updateAddrs != null) && !updateAddrs.isEmpty()) {
			for (IaAddress updateAddr : updateAddrs) {
				DhcpLease lease = toDhcpLease(ia, updateAddr, dhcpOptions);
				updateDhcpLease(lease);
				if (useLeaseCache()) {
					leaseCache.putLease(lease);
				}
			}
		}
		if ((delAddrs != null) && !delAddrs.isEmpty()) {
			for (IaAddress delAddr : delAddrs) {
				DhcpLease lease = toDhcpLease(ia, delAddr, dhcpOptions);
				deleteDhcpLease(lease);
				if (useLeaseCache()) {
					leaseCache.removeLease(lease.getIpAddress());
				}
			}
		}
		if (useIaCache()) {
			iaCache.putIA(ia);
		}
	}

	@Override
	public void deleteIA(IdentityAssoc ia)
	{
		if (ia != null) {
			// don't need v4 options or v6 message-level options for delete
			List<DhcpLease> leases = toDhcpLeases(ia, null);
			if ((leases != null) && !leases.isEmpty()) {
				for (final DhcpLease lease : leases) {
					deleteDhcpLease(lease);
					if (useLeaseCache()) {
						leaseCache.removeLease(lease.getIpAddress());
					}
				}
			}
			if (useIaCache()) {
				iaCache.removeIA(ia);
			}
		}
	}

	@Override
	public IdentityAssoc findIA(byte[] duid, byte iatype, long iaid) {
		IdentityAssoc ia = null;
		if (useIaCache()) {
			ia = iaCache.getIA(duid, iatype, iaid);
			if (log.isDebugEnabled()) {
				String iaKey = IdentityAssoc.keyToString(duid, iatype, iaid);
				if (ia != null) {
					log.debug("IA Cache hit: " + iaKey);
				}
				else {
					log.debug("IA Cache miss: " + iaKey);
				}
			}
		}
		if (ia == null) {
			List<DhcpLease> leases = findDhcpLeasesForIA(duid, iatype, iaid);        
        	ia = toIdentityAssoc(leases);
		}
		return ia;
	}

	@Override
	public IdentityAssoc findIA(IaAddress iaAddress) {
		return findIA(iaAddress.getIpAddress(), true);
	}

	@Override
	public IdentityAssoc findIA(InetAddress inetAddr) {
		return findIA(inetAddr, true);
	}
	
	/**
	 * Find ia.
	 *
	 * @param inetAddr the inet addr
	 * @param allBindings the all bindings
	 * @return the identity assoc
	 */
	public IdentityAssoc findIA(final InetAddress inetAddr, boolean allBindings)
	{
		IdentityAssoc ia = null;
        DhcpLease lease = null;
        if (useLeaseCache()) {
        	lease = leaseCache.getLease(inetAddr);
        	if (log.isDebugEnabled()) {
				if (lease != null) {
					log.debug("Lease Cache hit: " + inetAddr.getHostAddress());
				}
				else {
					log.debug("Lease Cache miss: " + inetAddr.getHostAddress());
				}
        	}
        }
        if (lease == null) {
        	lease = findDhcpLeaseForInetAddr(inetAddr);
        }
        if (lease != null) {
	        // use a set here, so that if we are getting all bindings, then we don't
	        // include the lease found above again in the returned collection
	        Set<DhcpLease> leases = new LinkedHashSet<DhcpLease>();
	        leases.add(lease);
	        if (allBindings) {
	        	leases.addAll(findDhcpLeasesForIA(lease.getDuid(), lease.getIatype(), lease.getIaid()));
	        }
        	ia = toIdentityAssoc(leases);
        }
        return ia;
	}

	@Override
	public List<IdentityAssoc> findExpiredIAs(byte iatype) {
		List<IdentityAssoc> ias = null;
		if (useLeaseCache()) {
			// tempting to use parallelStream, but the thread overhead
			// is not worth it with a cache size of only 1000 leases
			ias = leaseCache.getAllLeases().stream()
							.filter(l -> 
									(l.getIatype() == iatype) && 
									(l.getState() == IaAddress.LEASED) &&
									(l.getValidEndTime().getTime() < new Date().getTime()))
							.sorted(Comparator.comparing(DhcpLease::getValidEndTime))
							.map(LeaseManager::toIdentityAssoc)
							.collect(Collectors.toList());
			
			if (log.isDebugEnabled()) {
				if ((ias != null) && !ias.isEmpty()) {
					log.debug("Found " + ias.size() + " expired IAs of type=" +
							  IdentityAssoc.iaTypeToString(iatype) + " in Lease Cache");
				}
				else {
					log.debug("Found no expired IAs of type=" +
							  IdentityAssoc.iaTypeToString(iatype) + " in Lease Cache");
				}
			}
		}
		if ((ias == null) || ias.isEmpty()) {
			ias = toIdentityAssocs(findExpiredLeases(iatype));
		}
		return ias;
	}

	@Override
	public void saveDhcpOption(IaAddress iaAddr, 
							   com.jagornet.dhcp.core.option.base.BaseDhcpOption baseOption) {
		try {
			byte[] newVal = baseOption.encode().array();
			// don't store the option code, start with length to
			// simplify decoding when retrieving from database
			if (baseOption.isV4()) {
				newVal = Arrays.copyOfRange(newVal, 1, newVal.length);
			}
			else {
				newVal = Arrays.copyOfRange(newVal, 2, newVal.length);
			}
//			DhcpOption dbOption = iaAddr.getDhcpOption(baseOption.getCode());
			DhcpOption dbOption = findIaAddressOption(iaAddr, baseOption);
			if (dbOption == null) {
				dbOption = new com.jagornet.dhcp.server.db.DhcpOption();
				dbOption.setCode(baseOption.getCode());
				dbOption.setValue(newVal);
				setDhcpOption(iaAddr, dbOption);
			}
			else {
				if(!Arrays.equals(dbOption.getValue(), newVal)) {
					dbOption.setValue(newVal);
					setDhcpOption(iaAddr, dbOption);
				}
			}
		} 
		catch (IOException ex) {
			log.error("Failed to update binding with option", ex);
		}
		
	}

	@Override
	public void deleteDhcpOption(IaAddress iaAddr, 
							   com.jagornet.dhcp.core.option.base.BaseDhcpOption baseOption)
	{
		DhcpLease lease = null;
		if (useLeaseCache()) {
			lease = leaseCache.getLease(iaAddr.getIpAddress());
		}
		if (lease == null) {
			lease = findDhcpLeaseForInetAddr(iaAddr.getIpAddress());
		}
		if (lease != null) {
			Collection<DhcpOption> iaAddrOptions = lease.getIaAddrDhcpOptions();
			if (iaAddrOptions != null) {
				boolean deleted = false;
				for (DhcpOption iaAddrOption : iaAddrOptions) {
					if (iaAddrOption.getCode() == baseOption.getCode()) {
						iaAddrOptions.remove(iaAddrOption);
						deleted = true;
						break;
					}
				}
				if (deleted) {
					updateIpAddrOptions(iaAddr.getIpAddress(), iaAddrOptions);
					if (useIaCache()) {
						lease.setIaAddrDhcpOptions(iaAddrOptions);
// updated by reference, don't have to put it back
//						leaseCache.putLease(lease);
						IdentityAssoc ia = iaCache.getIA(lease.getDuid(), 
								lease.getIatype(), lease.getIaid());
						if (ia != null) {
							ia.setDhcpOptions(iaAddrOptions);
// updated by reference, don't have to put it back
//							iaCache.putIA(ia);
						}
					}
				}
			}
		}
	}

	@Override
	public void updateIaAddr(IaAddress iaAddr) {
		boolean isPrefix = false;
		short prefixLen = 0;
		if (iaAddr instanceof IaPrefix) {
			isPrefix = true;
			prefixLen = ((IaPrefix)iaAddr).getPrefixLength();
		}
		if (!isPrefix) {
			updateIpAddress(iaAddr.getIpAddress(), 
					iaAddr.getState(), iaAddr.getHaPeerState(), (short)0,
					iaAddr.getStartTime(), iaAddr.getPreferredEndTime(), iaAddr.getValidEndTime());
		}
		else {
			updateIpAddress(iaAddr.getIpAddress(), 
					iaAddr.getState(), iaAddr.getHaPeerState(), prefixLen,
					iaAddr.getStartTime(), iaAddr.getPreferredEndTime(), iaAddr.getValidEndTime());
			
		}
		if (useLeaseCache()) {
			DhcpLease lease = leaseCache.getLease(iaAddr.getIpAddress());
			if (lease != null) {
				lease.setState(iaAddr.getState());
				if (isPrefix) {
					lease.setPrefixLength(prefixLen);
				}
				lease.setStartTime(iaAddr.getStartTime());
				lease.setPreferredEndTime(iaAddr.getPreferredEndTime());
				lease.setValidEndTime(iaAddr.getValidEndTime());
				// no need to put the object back in the cache
				// because it was updated via reference from get
				IdentityAssoc ia = iaCache.getIA(lease.getDuid(),
						lease.getIatype(), lease.getIaid());
				if (ia != null) {
					// we may not need to update the object in the IA cache
					// because the iaAddr in the IA will be updated directly
					// via the reference object obtained from the IA cache,
					// but maybe the iaAddr came from somewhere else?
					Iterator<IaAddress> iaAddrIter = 
							(Iterator<IaAddress>) ia.getIaAddresses().iterator();
					while (iaAddrIter.hasNext()) {
						IaAddress currIaAddr = iaAddrIter.next();
						if (Util.compareInetAddrs(currIaAddr.getIpAddress(),
								iaAddr.getIpAddress()) == 0) {
							currIaAddr.setState(iaAddr.getState());
							if (isPrefix) {
								((IaPrefix)currIaAddr).setPrefixLength(prefixLen);
							}
							currIaAddr.setStartTime(iaAddr.getStartTime());
							currIaAddr.setPreferredEndTime(iaAddr.getPreferredEndTime());
							currIaAddr.setValidEndTime(iaAddr.getValidEndTime());
							break;
						}
					}
				}
			}
		}
	}

	@Override
	public void deleteIaAddr(IaAddress iaAddr) {
		deleteIpAddress(iaAddr.getIpAddress());
		if (useLeaseCache()) {
			DhcpLease oldLease = leaseCache.removeLease(iaAddr.getIpAddress());
			if (oldLease != null) {
				IdentityAssoc ia = iaCache.getIA(oldLease.getDuid(),
						oldLease.getIatype(), oldLease.getIaid());
				if (ia != null) {
					Iterator<IaAddress> iaAddrIter = 
							(Iterator<IaAddress>) ia.getIaAddresses().iterator();
					while (iaAddrIter.hasNext()) {
						IaAddress nextIaAddr = iaAddrIter.next();
						if (Util.compareInetAddrs(nextIaAddr.getIpAddress(),
								iaAddr.getIpAddress()) == 0) {
							iaAddrIter.remove();
							break;
						}
					}
					if (ia.iaAddresses.size() == 0) {
						iaCache.removeIA(ia);
					}
				}
				else {
					// TODO: what??
				}
			}
		}
	}

	@Override
	public void updateIaPrefix(IaPrefix iaPrefix) {
		this.updateIaAddr(iaPrefix);	// see above
	}

	@Override
	public void deleteIaPrefix(IaPrefix iaPrefix) {
		this.deleteIaAddr(iaPrefix);	// see above
	}

	@Override
	public List<InetAddress> findExistingIPs(InetAddress startAddr, InetAddress endAddr) {
		List<InetAddress> ips = null;
		if (useLeaseCache()) {
			ips = leaseCache.getAllLeases().stream()
					.filter(l -> 
							(Util.compareInetAddrs(l.getIpAddress(), startAddr) >= 0) && 
							(Util.compareInetAddrs(l.getIpAddress(), endAddr) <= 0))
//					.sorted(Comparator.comparing(DhcpLease::getIpAddress))
					.map(l -> l.getIpAddress())
					.collect(Collectors.toList());
			
			if (log.isDebugEnabled()) {
				if ((ips != null) && !ips.isEmpty()) {
					log.debug("Found " + ips.size() + " existing IPs in range=" +
							  startAddr.getHostAddress() + "-" + 
							  endAddr.getHostAddress() +
							  " in Lease Cache");
				}
				else {
					log.debug("Found no existing IPs in range=" +
							  startAddr.getHostAddress() + "-" + 
							  endAddr.getHostAddress() +
							  " in Lease Cache");
				}
			}
		}
		if ((ips == null) || ips.isEmpty()) {
			ips = findExistingLeaseIPs(startAddr, endAddr);
		}
		return ips;
	}

	@Override
	public List<IaAddress> findUnusedIaAddresses(InetAddress startAddr, InetAddress endAddr) {
		List<IaAddress> iaAddresses = null;
		if (useLeaseCache()) {
			final long offerExpiration = new Date().getTime() - offerExpireMillis;
			iaAddresses = leaseCache.getAllLeases().stream()
					.filter(l -> 
								((l.getState() == IaAddress.AVAILABLE) ||
								((l.getState() == IaAddress.OFFERED) && 
								 (l.getStartTime().getTime() < offerExpiration))) 
								&&
								((Util.compareInetAddrs(l.getIpAddress(), startAddr) >= 0) &&
								(Util.compareInetAddrs(l.getIpAddress(), endAddr) <= 0))
							)
					.sorted(Comparator.comparing(DhcpLease::getValidEndTime))
					.map(LeaseManager::toIaAddress)
					.collect(Collectors.toList());
			
			if (log.isDebugEnabled()) {
				if ((iaAddresses != null) && !iaAddresses.isEmpty()) {
					log.debug("Found " + iaAddresses.size() + 
							  " ununsed iaAddresses in range=" +
							  startAddr.getHostAddress() + "-" + 
							  endAddr.getHostAddress() +
							  " in Lease Cache");
				}
				else {
					log.debug("Found no unused iaAddresses in range=" +
							  startAddr.getHostAddress() + "-" + 
							  endAddr.getHostAddress() +
							  " in Lease Cache");
				}
			}
		}
		if ((iaAddresses == null) || iaAddresses.isEmpty()) {
			iaAddresses = toIaAddresses(findUnusedLeases(startAddr, endAddr));
		}
		return iaAddresses;
	}
	
	@Override
	public IaAddress findUnusedIaAddress(InetAddress startAddr, InetAddress endAddr) {
		IaAddress iaAddress = null;
		if (useLeaseCache()) {
			final long offerExpiration = new Date().getTime() - offerExpireMillis;
			Optional<IaAddress> cachedIaAddress = leaseCache.getAllLeases().stream()
					.reduce((DhcpLease l1, DhcpLease l2) ->
					((l1.isInRange(startAddr, endAddr) && l1.isAvailable(offerExpiration) &&
					l2.isInRange(startAddr, endAddr) && l2.isAvailable(offerExpiration) &&
					(l1.getValidEndTime().compareTo(l2.getValidEndTime()) <= 0)) ? l1 : l2))
					.map(LeaseManager::toIaAddress);
			if (cachedIaAddress.isPresent()) {
				iaAddress = cachedIaAddress.get();
				if (log.isDebugEnabled()) {
					log.debug("Found an ununsed iaAddress in range=" +
							  startAddr.getHostAddress() + "-" + 
							  endAddr.getHostAddress() +
							  " in Lease Cache");
				}
			}
			else {
				if (log.isDebugEnabled()) {
					log.debug("Found no ununsed iaAddress in range=" +
							  startAddr.getHostAddress() + "-" + 
							  endAddr.getHostAddress() +
							  " in Lease Cache");
				}
			}
		}
		if (iaAddress == null) {
			iaAddress = toIaAddress(findUnusedLease(startAddr, endAddr));
		}
		return iaAddress;
	}

	@Override
	public List<IaAddress> findExpiredIaAddresses(byte iatype) {
		List<IaAddress> iaAddresses = null;
		if (useLeaseCache()) {
			iaAddresses = leaseCache.getAllLeases().stream()
							.filter(l -> 
									(l.getIatype() == iatype) && 
									(l.getState() != IaAddress.RESERVED) &&
									(l.getValidEndTime().getTime() < new Date().getTime()))
							.sorted(Comparator.comparing(DhcpLease::getValidEndTime))
							.map(LeaseManager::toIaAddress)
							.collect(Collectors.toList());
		}
		if (iaAddresses == null) {
			iaAddresses = toIaAddresses(findExpiredLeases(iatype));
		}
		return iaAddresses;
	}

	@Override
	public List<IaPrefix> findUnusedIaPrefixes(InetAddress startAddr, InetAddress endAddr) {
		List<IaPrefix> iaPrefixes = null;
		if (useLeaseCache()) {
			final long offerExpiration = new Date().getTime() - offerExpireMillis;
			iaPrefixes = leaseCache.getAllLeases().stream()
					.filter(l -> 
								((l.getState() == IaAddress.AVAILABLE) ||
								((l.getState() == IaAddress.OFFERED) && 
								 (l.getStartTime().getTime() < offerExpiration))) 
								&&
								((Util.compareInetAddrs(l.getIpAddress(), startAddr) >= 0) &&
								(Util.compareInetAddrs(l.getIpAddress(), endAddr) <= 0))
							)
					.sorted(Comparator.comparing(DhcpLease::getValidEndTime))
					.map(LeaseManager::toIaPrefix)
					.collect(Collectors.toList());
		}
		if (iaPrefixes == null) {
			iaPrefixes = toIaPrefixes(findUnusedLeases(startAddr, endAddr));
		}
		return iaPrefixes;
	}

	@Override
	public List<IaPrefix> findExpiredIaPrefixes() {
		List<IaPrefix> iaPrefixes = null;
		if (useLeaseCache()) {
			iaPrefixes = leaseCache.getAllLeases().stream()
							.filter(l -> 
									(l.getIatype() == IdentityAssoc.PD_TYPE) && 
									(l.getState() != IaAddress.RESERVED) &&
									(l.getValidEndTime().getTime() < new Date().getTime()))
							.sorted(Comparator.comparing(DhcpLease::getValidEndTime))
							.map(LeaseManager::toIaPrefix)
							.collect(Collectors.toList());
		}
		if (iaPrefixes == null) {
			iaPrefixes = toIaPrefixes(findExpiredLeases(IdentityAssoc.PD_TYPE));
		}
		return iaPrefixes;
	}

	@Override
	public void reconcileIaAddresses(List<Range> ranges) {
		reconcileLeases(ranges);
		if (useLeaseCache()) {
// this methond is invoked only during startup, so there is nothing in the cache!
//			leaseCache.getAllLeases().stream()
//				.filter(l -> {
//					for (Range range : ranges) {
//						if ((Util.compareInetAddrs(l.getIpAddress(), range.getStartAddress()) >= 0) &&
//							(Util.compareInetAddrs(l.getIpAddress(), range.getEndAddress()) <= 0)) {
//							return true;
//						}
//					}
//					return false;
//				})
//				.collect(Collectors.toList());
		}
	}

	@Override
	public void deleteAllIAs() {
		deleteAllLeases();
		if (useIaCache()) {
			iaCache.clear();
		}
		if (useLeaseCache()) {
			leaseCache.clear();
		}
	}


	protected DhcpOption findIaAddressOption(IaAddress iaAddr,
			com.jagornet.dhcp.core.option.base.BaseDhcpOption baseOption) 
	{
		DhcpOption dbOption = null;
		DhcpLease lease = null;
		if (useLeaseCache()) {
			lease = leaseCache.getLease(iaAddr.getIpAddress());
		}
		if (lease == null) {
			lease = findDhcpLeaseForInetAddr(iaAddr.getIpAddress());
		}
		if (lease != null) {
			Collection<DhcpOption> iaAddrOptions = lease.getIaAddrDhcpOptions();
			if (iaAddrOptions != null) {
				for (DhcpOption iaAddrOption : iaAddrOptions) {
					if (iaAddrOption.getCode() == baseOption.getCode()) {
						dbOption = iaAddrOption;
						break;
					}
				}
			}
		}
		return dbOption;
	}
	
	protected void setDhcpOption(IaAddress iaAddr, DhcpOption option)
	{
		iaAddr.setDhcpOption(option);
		updateIpAddrOptions(iaAddr.getIpAddress(), iaAddr.getDhcpOptions());
		if (useLeaseCache()) {
			// we only need to update the object in the lease cache
			// because the iaPrefix in the IA will be updated directly
			// via the reference object obtained from the IA cache
			DhcpLease lease = leaseCache.getLease(iaAddr.getIpAddress());
			if (lease != null) {
				lease.setIaAddrDhcpOptions(iaAddr.getDhcpOptions());
				// no need to put the object back in the cache
				// because it was updated via reference from get
				IdentityAssoc ia = iaCache.getIA(lease.getDuid(),
						lease.getIatype(), lease.getIaid());
				if (ia != null) {
					Iterator<IaAddress> iaAddrIter = 
							(Iterator<IaAddress>) ia.getIaAddresses().iterator();
					while (iaAddrIter.hasNext()) {
						IaAddress currIaAddr = iaAddrIter.next();
						if (Util.compareInetAddrs(currIaAddr.getIpAddress(),
								iaAddr.getIpAddress()) == 0) {
							currIaAddr.setDhcpOption(option);
							break;
						}
					}
				}
			}
		}
	}
	
	// Conversion methods
	
	/**
	 * To identity assoc.
	 *
	 * @param leases the leases
	 * @return the identity assoc
	 */
	public static IdentityAssoc toIdentityAssoc(DhcpLease lease) {
		return toIdentityAssoc(Arrays.asList(lease));
	}
	
	public static IdentityAssoc toIdentityAssoc(Collection<DhcpLease> leases)
	{
		IdentityAssoc ia = null;
		if ((leases != null) && !leases.isEmpty()) {
			Iterator<DhcpLease> leaseIter = leases.iterator();
			DhcpLease lease = leaseIter.next();
			ia = new IdentityAssoc();
			ia.setDuid(lease.getDuid());
			ia.setIatype(lease.getIatype());
			ia.setIaid(lease.getIaid());
			ia.setState(lease.getState());
			//TODO? ia.setHaPeerState(lease.getHaPeerState());
			ia.setDhcpOptions(lease.getIaDhcpOptions());
			if (lease.getIatype() == IdentityAssoc.PD_TYPE) {
				List<IaPrefix> iaPrefixes = new ArrayList<IaPrefix>();
				iaPrefixes.add(toIaPrefix(lease));
				while (leaseIter.hasNext()) {
					//TODO: should confirm that the duid/iatype/iaid/state still match
					lease = leaseIter.next();
					iaPrefixes.add(toIaPrefix(lease));
				}
				ia.setIaAddresses(iaPrefixes);
			}
			else {
				List<IaAddress> iaAddrs = new ArrayList<IaAddress>();
				iaAddrs.add(toIaAddress(lease));
				while (leaseIter.hasNext()) {
					//TODO: should confirm that the duid/iatype/iaid/state still match
					lease = leaseIter.next();
					iaAddrs.add(toIaAddress(lease));
				}
				ia.setIaAddresses(iaAddrs);
			}
		}
		return ia;
	}
	
	public static List<IdentityAssoc> toIdentityAssocs(Collection<DhcpLease> leases)
	{
		List<IdentityAssoc> ias = null;
		if ((leases != null) && !leases.isEmpty()) {
			ias = new ArrayList<IdentityAssoc>();
			// for each lease, create a separate IdentityAssoc
			for (DhcpLease lease : leases) {
				Collection<DhcpLease> _leases = new ArrayList<DhcpLease>();
				_leases.add(lease);
				IdentityAssoc ia = toIdentityAssoc(_leases);
				ias.add(ia);
			}
		}
		return ias;
	}

	public static List<IaAddress> toIaAddresses(List<DhcpLease> leases) 
	{
		List<IaAddress> addrs = null;
        if (leases != null) {
        	addrs = new ArrayList<IaAddress>();
        	for (DhcpLease dhcpLease : leases) {
				addrs.add(toIaAddress(dhcpLease));
			}
        }
        return addrs;
	}

	/**
	 * To ia address.
	 *
	 * @param lease the lease
	 * @return the ia address
	 */
	public static IaAddress toIaAddress(DhcpLease lease)
	{
		IaAddress iaAddr = new IaAddress();
		iaAddr.setIpAddress(lease.getIpAddress());
		iaAddr.setState(lease.getState());
		iaAddr.setHaPeerState(lease.getHaPeerState());
		iaAddr.setStartTime(lease.getStartTime());
		iaAddr.setPreferredEndTime(lease.getPreferredEndTime());
		iaAddr.setValidEndTime(lease.getValidEndTime());
		iaAddr.setDhcpOptions(lease.getIaAddrDhcpOptions());
		return iaAddr;
	}


	public static List<IaPrefix> toIaPrefixes(List<DhcpLease> leases) 
	{
		List<IaPrefix> prefixes = null;
        if (leases != null) {
        	prefixes = new ArrayList<IaPrefix>();
        	for (DhcpLease dhcpLease : leases) {
				prefixes.add(toIaPrefix(dhcpLease));
			}
        }
        return prefixes;
	}

	/**
	 * To ia prefix.
	 *
	 * @param lease the lease
	 * @return the ia prefix
	 */
	public static IaPrefix toIaPrefix(DhcpLease lease)
	{
		IaPrefix iaPrefix = new IaPrefix();
		iaPrefix.setIpAddress(lease.getIpAddress());
		iaPrefix.setPrefixLength(lease.getPrefixLength());
		iaPrefix.setState(lease.getState());
		iaPrefix.setHaPeerState(lease.getHaPeerState());
		iaPrefix.setStartTime(lease.getStartTime());
		iaPrefix.setPreferredEndTime(lease.getPreferredEndTime());
		iaPrefix.setValidEndTime(lease.getValidEndTime());
		iaPrefix.setDhcpOptions(lease.getIaAddrDhcpOptions());
		return iaPrefix;
	}
	
	/**
	 * To dhcp leases.
	 *
	 * @param ia the ia
	 * @param dhcpOptions v4 options or v6 message-level options
	 * @return the list
	 */
	public static List<DhcpLease> toDhcpLeases(IdentityAssoc ia,
											   Collection<DhcpOption> dhcpOptions)
	{
		if (ia != null) {
			Collection<? extends IaAddress> iaAddrs = ia.getIaAddresses();
			if ((iaAddrs != null) && !iaAddrs.isEmpty()) {
				List<DhcpLease> leases = new ArrayList<DhcpLease>();
				for (IaAddress iaAddr : iaAddrs) {
					DhcpLease lease = toDhcpLease(ia, iaAddr, dhcpOptions);
					leases.add(lease);
				}
				return leases;
			}
			else {
				log.warn("No addresses in lease");
			}
		}
		return null;
	}

	/**
	 * To dhcp lease.
	 *
	 * @param ia the ia
	 * @param iaAddr the ia addr
	 * @param dhcpOptions v4 options or v6 message-level options
	 * @return the dhcp lease
	 */
	public static DhcpLease toDhcpLease(IdentityAssoc ia, IaAddress iaAddr,
										Collection<DhcpOption> dhcpOptions)
	{
		DhcpLease lease = new DhcpLease();
		lease.setDuid(ia.getDuid());
		lease.setIaid(ia.getIaid());
		lease.setIatype(ia.getIatype());
		lease.setIpAddress(iaAddr.getIpAddress());
		if (iaAddr instanceof IaPrefix) {
			lease.setPrefixLength(((IaPrefix)iaAddr).getPrefixLength());
		}
		lease.setState(iaAddr.getState());
		lease.setHaPeerState(iaAddr.getHaPeerState());
		lease.setStartTime(iaAddr.getStartTime());
		lease.setPreferredEndTime(iaAddr.getPreferredEndTime());
		lease.setValidEndTime(iaAddr.getValidEndTime());
		lease.setIaAddrDhcpOptions(iaAddr.getDhcpOptions());
		lease.setIaDhcpOptions(ia.getDhcpOptions());
		lease.setDhcpOptions(dhcpOptions);
		return lease;
	}
	
	/**
	 * Encode options.
	 *
	 * @param dhcpOptions the dhcp options
	 * @return the byte[]
	 */
	public static byte[] encodeOptions(Collection<DhcpOption> dhcpOptions)
	{
        if (dhcpOptions != null) {
        	ByteBuffer bb = ByteBuffer.allocate(1024);
            for (DhcpOption option : dhcpOptions) {
        		bb.putShort((short)option.getCode());
        		bb.putShort((short)option.getValue().length);
        		bb.put(option.getValue());
            }
            bb.flip();
            byte[] b = new byte[bb.limit()];
            bb.get(b);
            return b;
        }
        return null;
	}
	
	/**
	 * Decode options.
	 *
	 * @param buf the buf
	 * @return the collection
	 */
	public static Collection<DhcpOption> decodeOptions(byte[] buf)
	{
		Collection<DhcpOption> options = null;
        if ((buf != null) && (buf.length > 0)) {
        	ByteBuffer bb = ByteBuffer.wrap(buf);
        	options = new ArrayList<DhcpOption>();
        	while (bb.hasRemaining()) {
        		DhcpOption option = new DhcpOption();
        		option.setCode(bb.getShort());
        		int len = bb.getShort();
        		byte[] val = new byte[len];
        		bb.get(val);
        		option.setValue(val);
        		options.add(option);
        	}
        }
        return options;
	}

}
