package com.jagornet.dhcp.server.db;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class IaCache {

	private static IaCache instance = null;
	
	private static int MAX_CACHE_SIZE = 1000;
	private static Map<String, IdentityAssoc> iaCache;
	
	protected IaCache() {
		
	}
	
	public static IaCache getInstance() {
		if (instance == null) {
			iaCache = Collections.synchronizedMap(
					new LinkedHashMap<String, IdentityAssoc>(16, 0.75f, true) {
						private static final long serialVersionUID = 1L;
						@Override
					     protected boolean removeEldestEntry(
					    		 Map.Entry<String, IdentityAssoc> eldest) {
					        return (size() > MAX_CACHE_SIZE);
					     }
						
					});
			instance = new IaCache();
		}
		return instance;
	}
	
	public void clear() {
		iaCache.clear();
	}
	
	public void putIA(IdentityAssoc ia) {
		String key = toKeyString(ia.getDuid(), ia.getIatype(), ia.getIaid());
		iaCache.put(key, ia);
	}
	
	public IdentityAssoc getIA(byte[] duid, byte iatype, long iaid) {
		String key = toKeyString(duid, iatype, iaid);
		return iaCache.get(key);
	}
	
	public void removeIA(IdentityAssoc ia) {
		String key = toKeyString(ia.getDuid(), ia.getIatype(), ia.getIaid());
		iaCache.remove(key);
	}
	
	private static String toKeyString(byte[] duid, byte iatype, long iaid) {
		return String.valueOf(duid) + Byte.toString(iatype) + Long.toString(iaid);
	}
}
