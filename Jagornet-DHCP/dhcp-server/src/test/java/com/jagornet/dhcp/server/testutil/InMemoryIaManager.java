/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.testutil;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.jagornet.dhcp.core.option.base.BaseDhcpOption;
import com.jagornet.dhcp.core.option.base.DhcpOption;
import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.db.DhcpLease;
import com.jagornet.dhcp.server.db.DhcpLeaseCallbackHandler;
import com.jagornet.dhcp.server.db.IaAddress;
import com.jagornet.dhcp.server.db.IaPrefix;
import com.jagornet.dhcp.server.db.IdentityAssoc;
import com.jagornet.dhcp.server.db.InetAddressCallbackHandler;
import com.jagornet.dhcp.server.db.LeaseManager;
import com.jagornet.dhcp.server.request.binding.Range;

/**
 * High-performance, lightweight, in-memory implementation of {@link LeaseManager}
 * for end-to-end request processor lifecycle testing (DORA & SARR) and HA testing
 * without database dependencies.
 * 
 * @author A. Gregory Rabil
 */
public class InMemoryIaManager extends LeaseManager {

	private final AtomicLong idGen = new AtomicLong(1);
	private final Map<String, IdentityAssoc> iasByKey = new ConcurrentHashMap<>();
	private final Map<InetAddress, IdentityAssoc> iasByIp = new ConcurrentHashMap<>();
	private final Map<InetAddress, IaAddress> addrsByIp = new ConcurrentHashMap<>();
	private final Map<InetAddress, DhcpLease> leasesByIp = new ConcurrentHashMap<>();

	private String key(byte[] duid, byte iatype, long iaid) {
		return (duid != null ? Util.toHexString(duid) : "null") + ":" + iatype + ":" + iaid;
	}

	@Override
	public void init() throws Exception {
		// No-op for in-memory implementation
	}

	@Override
	public synchronized void createIA(IdentityAssoc ia, Collection<DhcpOption> dhcpOptions) {
		if (ia.getId() == null) {
			ia.setId(idGen.getAndIncrement());
		}
		String k = key(ia.getDuid(), ia.getIatype(), ia.getIaid());
		iasByKey.put(k, ia);
		if (dhcpOptions != null) {
			ia.setDhcpOptions(new ArrayList<>(dhcpOptions));
		}
		if (ia.getIaAddresses() != null) {
			for (IaAddress addr : ia.getIaAddresses()) {
				if (addr.getIpAddress() != null) {
					addrsByIp.put(addr.getIpAddress(), addr);
					iasByIp.put(addr.getIpAddress(), ia);
				}
			}
		}
		List<DhcpLease> leases = toDhcpLeases(ia, dhcpOptions);
		if (leases != null) {
			for (DhcpLease l : leases) {
				leasesByIp.put(l.getIpAddress(), l);
			}
		}
	}

	@Override
	public synchronized void updateIA(IdentityAssoc ia,
			Collection<? extends IaAddress> addAddrs,
			Collection<? extends IaAddress> updateAddrs,
			Collection<? extends IaAddress> delAddrs,
			Collection<DhcpOption> dhcpOptions) {
		String k = key(ia.getDuid(), ia.getIatype(), ia.getIaid());
		iasByKey.put(k, ia);
		if (dhcpOptions != null) {
			ia.setDhcpOptions(new ArrayList<>(dhcpOptions));
		}
		if (addAddrs != null) {
			for (IaAddress addr : addAddrs) {
				if (addr.getIpAddress() != null) {
					addrsByIp.put(addr.getIpAddress(), addr);
					iasByIp.put(addr.getIpAddress(), ia);
				}
			}
		}
		if (updateAddrs != null) {
			for (IaAddress addr : updateAddrs) {
				if (addr.getIpAddress() != null) {
					addrsByIp.put(addr.getIpAddress(), addr);
					iasByIp.put(addr.getIpAddress(), ia);
				}
			}
		}
		if (delAddrs != null) {
			for (IaAddress addr : delAddrs) {
				if (addr.getIpAddress() != null) {
					addrsByIp.remove(addr.getIpAddress());
					iasByIp.remove(addr.getIpAddress());
					leasesByIp.remove(addr.getIpAddress());
				}
			}
		}
		List<DhcpLease> leases = toDhcpLeases(ia, dhcpOptions);
		if (leases != null) {
			for (DhcpLease l : leases) {
				leasesByIp.put(l.getIpAddress(), l);
			}
		}
	}

	@Override
	public synchronized void deleteIA(IdentityAssoc ia) {
		String k = key(ia.getDuid(), ia.getIatype(), ia.getIaid());
		iasByKey.remove(k);
		if (ia.getIaAddresses() != null) {
			for (IaAddress addr : ia.getIaAddresses()) {
				if (addr.getIpAddress() != null) {
					addrsByIp.remove(addr.getIpAddress());
					iasByIp.remove(addr.getIpAddress());
					leasesByIp.remove(addr.getIpAddress());
				}
			}
		}
	}

	@Override
	public IdentityAssoc findIA(byte[] duid, byte iatype, long iaid) {
		return iasByKey.get(key(duid, iatype, iaid));
	}

	@Override
	public IdentityAssoc findIA(IaAddress iaAddress) {
		if (iaAddress != null && iaAddress.getIpAddress() != null) {
			return iasByIp.get(iaAddress.getIpAddress());
		}
		return null;
	}

	@Override
	public IdentityAssoc findIA(InetAddress inetAddr) {
		if (inetAddr != null) {
			return iasByIp.get(inetAddr);
		}
		return null;
	}

	@Override
	public List<IdentityAssoc> findExpiredIAs(byte iatype) {
		return Collections.emptyList();
	}

	private final Map<String, List<BaseDhcpOption>> optionsByIp = new ConcurrentHashMap<>();

	private String ipKey(IaAddress iaAddr) {
		if (iaAddr == null || iaAddr.getIpAddress() == null) return null;
		return iaAddr.getIpAddress().getHostAddress();
	}

	@Override
	public synchronized void saveDhcpOption(IaAddress iaAddr, BaseDhcpOption option) {
		if (iaAddr != null && option != null) {
			iaAddr.setDhcpOption(option);
			if (iaAddr.getIpAddress() != null) {
				IaAddress stored = addrsByIp.get(iaAddr.getIpAddress());
				if (stored != null && stored != iaAddr) {
					stored.setDhcpOption(option);
				}
			}
			String key = ipKey(iaAddr);
			if (key != null) {
				List<BaseDhcpOption> list = optionsByIp.computeIfAbsent(key, k -> new ArrayList<>());
				list.removeIf(o -> o.getCode() == option.getCode());
				list.add(option);
			}
		}
	}

	@Override
	public synchronized void deleteDhcpOption(IaAddress iaAddr, BaseDhcpOption option) {
		if (iaAddr != null && option != null) {
			if (iaAddr.getDhcpOptions() != null) {
				iaAddr.getDhcpOptions().removeIf(o -> o.getCode() == option.getCode());
			}
			if (iaAddr.getIpAddress() != null) {
				IaAddress stored = addrsByIp.get(iaAddr.getIpAddress());
				if (stored != null && stored.getDhcpOptions() != null) {
					stored.getDhcpOptions().removeIf(o -> o.getCode() == option.getCode());
				}
			}
			String key = ipKey(iaAddr);
			if (key != null) {
				List<BaseDhcpOption> list = optionsByIp.get(key);
				if (list != null) {
					list.removeIf(o -> o.getCode() == option.getCode());
				}
			}
		}
	}

	public synchronized List<BaseDhcpOption> findDhcpOptionsForAddr(IaAddress iaAddr) {
		String key = ipKey(iaAddr);
		if (key != null) {
			List<BaseDhcpOption> list = optionsByIp.get(key);
			if (list != null && !list.isEmpty()) {
				return new ArrayList<>(list);
			}
		}
		if (iaAddr != null && iaAddr.getDhcpOptions() != null) {
			List<BaseDhcpOption> list = new ArrayList<>();
			for (DhcpOption o : iaAddr.getDhcpOptions()) {
				if (o instanceof BaseDhcpOption) {
					list.add((BaseDhcpOption) o);
				}
			}
			return list;
		}
		return Collections.emptyList();
	}

	@Override
	public synchronized void updateIaAddr(IaAddress iaAddr) {
		if (iaAddr != null && iaAddr.getIpAddress() != null) {
			addrsByIp.put(iaAddr.getIpAddress(), iaAddr);
		}
	}

	@Override
	public synchronized void deleteIaAddr(IaAddress iaAddr) {
		if (iaAddr != null && iaAddr.getIpAddress() != null) {
			addrsByIp.remove(iaAddr.getIpAddress());
			iasByIp.remove(iaAddr.getIpAddress());
			leasesByIp.remove(iaAddr.getIpAddress());
		}
	}

	@Override
	public void updateIaPrefix(IaPrefix iaPrefix) {
	}

	@Override
	public void deleteIaPrefix(IaPrefix iaPrefix) {
	}

	@Override
	public synchronized List<InetAddress> findExistingIPs(InetAddress startAddr, InetAddress endAddr) {
		List<InetAddress> result = new ArrayList<>();
		for (Map.Entry<InetAddress, IaAddress> entry : addrsByIp.entrySet()) {
			IaAddress addr = entry.getValue();
			if (addr.getState() != IaAddress.AVAILABLE) {
				InetAddress ip = entry.getKey();
				if (Util.compareInetAddrs(startAddr, ip) <= 0 && Util.compareInetAddrs(ip, endAddr) <= 0) {
					result.add(ip);
				}
			}
		}
		return result;
	}

	@Override
	public synchronized List<IaAddress> findUnusedIaAddresses(InetAddress startAddr, InetAddress endAddr) {
		List<IaAddress> result = new ArrayList<>();
		for (Map.Entry<InetAddress, IaAddress> entry : addrsByIp.entrySet()) {
			IaAddress addr = entry.getValue();
			if (addr.getState() == IaAddress.AVAILABLE) {
				InetAddress ip = entry.getKey();
				if (Util.compareInetAddrs(startAddr, ip) <= 0 && Util.compareInetAddrs(ip, endAddr) <= 0) {
					result.add(addr);
				}
			}
		}
		return result;
	}

	@Override
	public synchronized IaAddress findUnusedIaAddress(InetAddress startAddr, InetAddress endAddr) {
		List<IaAddress> unused = findUnusedIaAddresses(startAddr, endAddr);
		return unused.isEmpty() ? null : unused.get(0);
	}

	@Override
	public List<IaAddress> findExpiredIaAddresses(byte iatype) {
		return Collections.emptyList();
	}

	@Override
	public List<IaPrefix> findUnusedIaPrefixes(InetAddress startAddr, InetAddress endAddr) {
		return Collections.emptyList();
	}

	@Override
	public List<IaPrefix> findExpiredIaPrefixes() {
		return Collections.emptyList();
	}

	@Override
	public void reconcileIaAddresses(List<Range> ranges) {
	}

	@Override
	public synchronized void deleteAllIAs() {
		iasByKey.clear();
		iasByIp.clear();
		addrsByIp.clear();
		leasesByIp.clear();
	}

	// ==================== LeaseManager implementations ====================

	@Override
	public synchronized int insertDhcpLease(final DhcpLease lease) {
		if (lease != null && lease.getIpAddress() != null) {
			leasesByIp.put(lease.getIpAddress(), lease);
			return 1;
		}
		return 0;
	}

	@Override
	public synchronized int updateDhcpLease(final DhcpLease lease) {
		if (lease != null && lease.getIpAddress() != null) {
			leasesByIp.put(lease.getIpAddress(), lease);
			return 1;
		}
		return 0;
	}

	@Override
	public synchronized int deleteDhcpLease(final DhcpLease lease) {
		if (lease != null && lease.getIpAddress() != null) {
			leasesByIp.remove(lease.getIpAddress());
			return 1;
		}
		return 0;
	}

	@Override
	public synchronized int updateIpAddress(final InetAddress inetAddr, 
			final byte state, final byte haPeerState, final short prefixlen,
			final Date start, final Date preferred, final Date valid,
			final Collection<DhcpOption> iaAddrOptions) {
		DhcpLease lease = leasesByIp.get(inetAddr);
		if (lease != null) {
			lease.setState(state);
			lease.setHaPeerState(haPeerState);
			lease.setPrefixLength(prefixlen);
			lease.setStartTime(start);
			lease.setPreferredEndTime(preferred);
			lease.setValidEndTime(valid);
			return 1;
		}
		return 0;
	}

	@Override
	public synchronized int deleteIpAddress(final InetAddress inetAddr) {
		return leasesByIp.remove(inetAddr) != null ? 1 : 0;
	}

	@Override
	public int updateIaOptions(final InetAddress inetAddr, final Collection<DhcpOption> iaOptions) {
		return 1;
	}

	@Override
	public int updateIpAddrOptions(final InetAddress inetAddr, final Collection<DhcpOption> ipAddrOptions) {
		return 1;
	}

	@Override
	public synchronized List<DhcpLease> findDhcpLeasesForIA(final byte[] duid, final byte iatype, final long iaid) {
		List<DhcpLease> list = new ArrayList<>();
		for (DhcpLease l : leasesByIp.values()) {
			if (Arrays.equals(l.getDuid(), duid) && l.getIatype() == iatype && l.getIaid() == iaid) {
				list.add(l);
			}
		}
		return list;
	}

	@Override
	public DhcpLease findDhcpLeaseForInetAddr(final InetAddress inetAddr) {
		return inetAddr != null ? leasesByIp.get(inetAddr) : null;
	}

	@Override
	public synchronized List<InetAddress> findExistingLeaseIPs(final InetAddress startAddr, final InetAddress endAddr) {
		List<InetAddress> ips = new ArrayList<>();
		for (InetAddress ip : leasesByIp.keySet()) {
			if (Util.compareInetAddrs(startAddr, ip) <= 0 && Util.compareInetAddrs(ip, endAddr) <= 0) {
				ips.add(ip);
			}
		}
		return ips;
	}

	@Override
	public synchronized void findExistingLeaseIPs(final InetAddress startAddr, final InetAddress endAddr,
			InetAddressCallbackHandler inetAddressCallbackHandler) {
		for (InetAddress ip : findExistingLeaseIPs(startAddr, endAddr)) {
			try {
				inetAddressCallbackHandler.processInetAddress(ip);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	@Override
	public synchronized void findExistingLeases(final InetAddress startAddr, final InetAddress endAddr,
			DhcpLeaseCallbackHandler dhcpLeaseCallbackHandler) {
		for (DhcpLease lease : leasesByIp.values()) {
			InetAddress ip = lease.getIpAddress();
			if (Util.compareInetAddrs(startAddr, ip) <= 0 && Util.compareInetAddrs(ip, endAddr) <= 0) {
				try {
					dhcpLeaseCallbackHandler.processDhcpLease(lease);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		}
	}

	@Override
	public synchronized void findUnsyncedLeases(final InetAddress startAddr, final InetAddress endAddr,
			DhcpLeaseCallbackHandler dhcpLeaseCallbackHandler) {
		for (DhcpLease lease : leasesByIp.values()) {
			InetAddress ip = lease.getIpAddress();
			if (Util.compareInetAddrs(startAddr, ip) <= 0 && Util.compareInetAddrs(ip, endAddr) <= 0) {
				if (lease.getHaPeerState() != lease.getState()) {
					try {
						dhcpLeaseCallbackHandler.processDhcpLease(lease);
					} catch (Exception ex) {
						throw new RuntimeException(ex);
					}
				}
			}
		}
	}

	@Override
	public synchronized int setAllLeasesUnsynced() {
		int count = 0;
		for (DhcpLease lease : leasesByIp.values()) {
			lease.setHaPeerState((byte) -1);
			count++;
		}
		return count;
	}

	@Override
	public synchronized List<DhcpLease> findUnusedLeases(final InetAddress startAddr, final InetAddress endAddr) {
		List<DhcpLease> list = new ArrayList<>();
		for (DhcpLease l : leasesByIp.values()) {
			if (l.getState() == IaAddress.AVAILABLE) {
				InetAddress ip = l.getIpAddress();
				if (Util.compareInetAddrs(startAddr, ip) <= 0 && Util.compareInetAddrs(ip, endAddr) <= 0) {
					list.add(l);
				}
			}
		}
		return list;
	}

	@Override
	public synchronized DhcpLease findUnusedLease(final InetAddress startAddr, final InetAddress endAddr) {
		List<DhcpLease> list = findUnusedLeases(startAddr, endAddr);
		return list.isEmpty() ? null : list.get(0);
	}

	@Override
	public List<DhcpLease> findExpiredLeases(final byte iatype) {
		return Collections.emptyList();
	}

	@Override
	public void reconcileLeases(final List<Range> ranges) {
	}

	@Override
	public synchronized int deleteAllLeases() {
		int count = leasesByIp.size();
		leasesByIp.clear();
		return count;
	}

	public synchronized void clear() {
		deleteAllIAs();
		deleteAllLeases();
	}
}
