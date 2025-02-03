package com.jagornet.dhcp.server.db;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IaCache {
	
	private static Logger log = LoggerFactory.getLogger(IaCache.class);

	private Map<String, IdentityAssoc> cache;
	
	public IaCache(int cacheSize) {
		log.info("Creating IA cache size=" + cacheSize);
		cache = Collections.synchronizedMap(
				new LinkedHashMap<String, IdentityAssoc>(16, 0.75f, true) {
					private static final long serialVersionUID = 1L;
					@Override
				     protected boolean removeEldestEntry(
				    		 Map.Entry<String, IdentityAssoc> eldest) {
				        return (size() > cacheSize);
				     }
					
				});
	}
	
	public void clear() {
		cache.clear();
	}
	
	public void putIA(IdentityAssoc ia) {
		String key = IdentityAssoc.keyToString(ia.getDuid(), ia.getIatype(), ia.getIaid());
		cache.put(key, ia);
	}
	
	public IdentityAssoc getIA(byte[] duid, byte iatype, long iaid) {
		String key = IdentityAssoc.keyToString(duid, iatype, iaid);
		return cache.get(key);
	}
	
	public void removeIA(IdentityAssoc ia) {
		String key = IdentityAssoc.keyToString(ia.getDuid(), ia.getIatype(), ia.getIaid());
		cache.remove(key);
	}
}
