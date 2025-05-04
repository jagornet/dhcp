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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaseCache {
	
	private static Logger log = LoggerFactory.getLogger(LeaseCache.class);

	private Map<InetAddress, DhcpLease> cache;
	
	public LeaseCache(int cacheSize) {
		log.info("Creating Lease cache size=" + cacheSize);
		cache = Collections.synchronizedMap(
				new LinkedHashMap<InetAddress, DhcpLease>(16, 0.75f, true) {
					private static final long serialVersionUID = 1L;
					@Override
				     protected boolean removeEldestEntry(
				    		 Map.Entry<InetAddress, DhcpLease> eldest) {
				        return (size() > cacheSize);
				     }
					
				});
	}
	
	public void clear() {
		cache.clear();
	}
	
	public void putLease(DhcpLease lease) {
		cache.put(lease.getIpAddress(), lease);
	}
	
	public DhcpLease getLease(InetAddress inetAddr) {
		return cache.get(inetAddr);
	}
	
	public DhcpLease removeLease(InetAddress inetAddr) {
		return cache.remove(inetAddr);
	}
	
	public Collection<DhcpLease> getAllLeases() {
		return cache.values();
	}
	
	public List<DhcpLease> expiredLeases(byte iatype) {
		long now = new Date().getTime();
		return cache.values().stream()
			.filter(l -> 
				(l.getIatype() == iatype) && 
				(l.getState() != IaAddress.RESERVED) &&
				(l.getValidEndTime().getTime() < now))
			.sorted(Comparator.comparing(DhcpLease::getValidEndTime))
			.collect(Collectors.toList());
	}
}
