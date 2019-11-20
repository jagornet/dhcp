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

import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;

public class LeaseCache {
	
	private static Logger log = LoggerFactory.getLogger(LeaseCache.class);

	private Map<InetAddress, DhcpLease> leaseCache;
	
	public LeaseCache(int cacheSize) {
		log.info("Creating Lease cache size=" + cacheSize);
		leaseCache = Collections.synchronizedMap(
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
		long now = new Date().getTime();
		return leaseCache.values().stream()
			.filter(l -> 
				(l.getIatype() == iatype) && 
				(l.getState() != IaAddress.STATIC) &&
				(l.getValidEndTime().getTime() < now))
			.sorted(Comparator.comparing(DhcpLease::getValidEndTime))
			.collect(Collectors.toList());
	}
}
