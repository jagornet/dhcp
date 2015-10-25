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
package com.jagornet.dhcp.db;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LeaseManager implements IaManager {
	
	private static Logger log = LoggerFactory.getLogger(LeaseManager.class);

	protected abstract void insertDhcpLease(final DhcpLease lease);
	protected abstract void updateDhcpLease(final DhcpLease lease);
	protected abstract void deleteDhcpLease(final DhcpLease lease);
	protected abstract void updateIaOptions(final InetAddress inetAddr, 
			final Collection<DhcpOption> iaOptions);
	protected abstract void updateIpAddrOptions(final InetAddress inetAddr,
			final Collection<DhcpOption> ipAddrOptions);
	protected abstract List<DhcpLease> findDhcpLeasesForIA(final byte[] duid, 
			final byte iatype, final long iaid);
	protected abstract DhcpLease findDhcpLeaseForInetAddr(final InetAddress inetAddr);
	protected abstract List<DhcpLease> findExpiredLeases(final byte iatype);
	

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#createIA(com.jagornet.dhcpv6.db.IdentityAssoc)
	 */
	public void createIA(IdentityAssoc ia) {
		if (ia != null) {
			List<DhcpLease> leases = toDhcpLeases(ia);
			if ((leases != null) && !leases.isEmpty()) {
				for (final DhcpLease lease : leases) {
					insertDhcpLease(lease);
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#updateIA(com.jagornet.dhcpv6.db.IdentityAssoc, java.util.Collection, java.util.Collection, java.util.Collection)
	 */
	public void updateIA(IdentityAssoc ia, Collection<? extends IaAddress> addAddrs,
			Collection<? extends IaAddress> updateAddrs, Collection<? extends IaAddress> delAddrs)
	{
		if ((addAddrs != null) && !addAddrs.isEmpty()) {
			for (IaAddress addAddr : addAddrs) {
				DhcpLease lease = toDhcpLease(ia, addAddr);
				insertDhcpLease(lease);
			}
		}
		if ((updateAddrs != null) && !updateAddrs.isEmpty()) {
			for (IaAddress updateAddr : updateAddrs) {
				DhcpLease lease = toDhcpLease(ia, updateAddr);
				updateDhcpLease(lease);
			}
		}
		if ((delAddrs != null) && !delAddrs.isEmpty()) {
			for (IaAddress delAddr : delAddrs) {
				DhcpLease lease = toDhcpLease(ia, delAddr);
				deleteDhcpLease(lease);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#deleteIA(com.jagornet.dhcpv6.db.IdentityAssoc)
	 */
	public void deleteIA(IdentityAssoc ia)
	{
		if (ia != null) {
			List<DhcpLease> leases = toDhcpLeases(ia);
			if ((leases != null) && !leases.isEmpty()) {
				for (final DhcpLease lease : leases) {
					deleteDhcpLease(lease);
				}
			}
		}
	}
	
	public List<IdentityAssoc> findExpiredIAs(byte iatype)
	{
		List<DhcpLease> leases = findExpiredLeases(iatype);
		return toIdentityAssocs(leases);
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#findExpiredIaAddresses(byte)
	 */
	@Override
	public List<IaAddress> findExpiredIaAddresses(byte iatype) {
		List<DhcpLease> leases = findExpiredLeases(iatype);
		return toIaAddresses(leases);
	}

	public void saveDhcpOption(IaAddress iaAddr, 
							   com.jagornet.dhcp.option.base.BaseDhcpOption baseOption)
	{
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
				dbOption = new com.jagornet.dhcp.db.DhcpOption();
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

	public void deleteDhcpOption(IaAddress iaAddr, 
							   com.jagornet.dhcp.option.base.BaseDhcpOption baseOption)
	{
		DhcpLease lease = findDhcpLeaseForInetAddr(iaAddr.getIpAddress());
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
				}
			}
		}
	}
	
	protected DhcpOption findIaAddressOption(IaAddress iaAddr,
			com.jagornet.dhcp.option.base.BaseDhcpOption baseOption) 
	{
		DhcpOption dbOption = null;
		DhcpLease lease = findDhcpLeaseForInetAddr(iaAddr.getIpAddress());
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
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#findIA(byte[], byte, long)
	 */
	public IdentityAssoc findIA(final byte[] duid, final byte iatype, final long iaid)
	{
        List<DhcpLease> leases = findDhcpLeasesForIA(duid, iatype, iaid);        
        return toIdentityAssoc(leases);
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#findIA(com.jagornet.dhcpv6.db.IaAddress)
	 */
	public IdentityAssoc findIA(IaAddress iaAddress) {
		return findIA(iaAddress.getIpAddress(), true);
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#findIA(java.net.InetAddress)
	 */
	public IdentityAssoc findIA(final InetAddress inetAddr)
	{
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
        DhcpLease lease = findDhcpLeaseForInetAddr(inetAddr);
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
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#updateIaPrefix(com.jagornet.dhcpv6.db.IaPrefix)
	 */
	public void updateIaPrefix(final IaPrefix iaPrefix)
	{
		updateIaAddr(iaPrefix);
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaManager#deleteIaPrefix(com.jagornet.dhcpv6.db.IaPrefix)
	 */
	public void deleteIaPrefix(IaPrefix iaPrefix)
	{
		deleteIaAddr(iaPrefix);
	}
	
	// Conversion methods
	
	/**
	 * To identity assoc.
	 *
	 * @param leases the leases
	 * @return the identity assoc
	 */
	protected IdentityAssoc toIdentityAssoc(Collection<DhcpLease> leases)
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
	
	protected List<IdentityAssoc> toIdentityAssocs(Collection<DhcpLease> leases)
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

	protected List<IaAddress> toIaAddresses(List<DhcpLease> leases) 
	{
		List<IaAddress> addrs = null;
        if (leases != null) {
        	addrs = new ArrayList<IaAddress>();
        	for (DhcpLease dhcpLease : leases) {
				addrs.add(this.toIaAddress(dhcpLease));
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
	protected IaAddress toIaAddress(DhcpLease lease)
	{
		IaAddress iaAddr = new IaAddress();
		iaAddr.setIpAddress(lease.getIpAddress());
		iaAddr.setState(lease.getState());
		iaAddr.setStartTime(lease.getStartTime());
		iaAddr.setPreferredEndTime(lease.getPreferredEndTime());
		iaAddr.setValidEndTime(lease.getValidEndTime());
		iaAddr.setDhcpOptions(lease.getIaAddrDhcpOptions());
		return iaAddr;
	}


	protected List<IaPrefix> toIaPrefixes(List<DhcpLease> leases) 
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
	protected IaPrefix toIaPrefix(DhcpLease lease)
	{
		IaPrefix iaPrefix = new IaPrefix();
		iaPrefix.setIpAddress(lease.getIpAddress());
		iaPrefix.setPrefixLength(lease.getPrefixLength());
		iaPrefix.setState(lease.getState());
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
	 * @return the list
	 */
	protected List<DhcpLease> toDhcpLeases(IdentityAssoc ia)
	{
		if (ia != null) {
			Collection<? extends IaAddress> iaAddrs = ia.getIaAddresses();
			if ((iaAddrs != null) && !iaAddrs.isEmpty()) {
				List<DhcpLease> leases = new ArrayList<DhcpLease>();
				for (IaAddress iaAddr : iaAddrs) {
					DhcpLease lease = toDhcpLease(ia, iaAddr);
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
	 * @return the dhcp lease
	 */
	protected DhcpLease toDhcpLease(IdentityAssoc ia, IaAddress iaAddr)
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
		lease.setStartTime(iaAddr.getStartTime());
		lease.setPreferredEndTime(iaAddr.getPreferredEndTime());
		lease.setValidEndTime(iaAddr.getValidEndTime());
		lease.setIaAddrDhcpOptions(iaAddr.getDhcpOptions());
		lease.setIaDhcpOptions(ia.getDhcpOptions());
		return lease;
	}
	
	/**
	 * Encode options.
	 *
	 * @param dhcpOptions the dhcp options
	 * @return the byte[]
	 */
	protected byte[] encodeOptions(Collection<DhcpOption> dhcpOptions)
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
	protected Collection<DhcpOption> decodeOptions(byte[] buf)
	{
		Collection<DhcpOption> options = null;
        if (buf != null) {
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
