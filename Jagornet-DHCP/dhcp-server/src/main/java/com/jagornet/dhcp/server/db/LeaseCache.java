package com.jagornet.dhcp.server.db;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LeaseCache {

	private static LeaseCache instance = null;
	
	private static int MAX_CACHE_SIZE = 1000;
	private static Map<InetAddress, DhcpLease> leaseCache;
	
	protected LeaseCache() {
		
	}
	
	public static LeaseCache getInstance() {
		if (instance == null) {
			leaseCache = Collections.synchronizedMap(
					new LinkedHashMap<InetAddress, DhcpLease>(16, 0.75f, true) {
						private static final long serialVersionUID = 1L;
						@Override
					     protected boolean removeEldestEntry(
					    		 Map.Entry<InetAddress, DhcpLease> eldest) {
					        return (size() > MAX_CACHE_SIZE);
					     }
						
					});
			instance = new LeaseCache();
		}
		return instance;
	}
	
	public void clear() {
		leaseCache.clear();
	}
	
	public void putLease(DhcpLease lease) {
		leaseCache.put(lease.getIpAddress(), lease);
	}
	
	public DhcpLease getLease(InetAddress inetAddr) {
		return leaseCache.get(inetAddr);
	}
	
	public DhcpLease removeLease(InetAddress inetAddr) {
		return leaseCache.remove(inetAddr);
	}
	
	public Collection<DhcpLease> getAllLeases() {
		return leaseCache.values();
	}
	
	public List<DhcpLease> expiredLeases(byte iatype) {
		return leaseCache.values().stream()
			.filter(l -> 
				(l.getIatype() == iatype) && 
				(l.getState() != IaAddress.STATIC) &&
				(l.getValidEndTime().getTime() < new Date().getTime()))
			.sorted(Comparator.comparing(DhcpLease::getValidEndTime))
			.collect(Collectors.toList());
	}
}
