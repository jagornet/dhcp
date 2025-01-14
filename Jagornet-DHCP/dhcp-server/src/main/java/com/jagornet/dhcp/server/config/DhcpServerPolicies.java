/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpServerPolicies.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.server.config;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Properties;

import com.jagornet.dhcp.core.message.DhcpMessage;
import com.jagornet.dhcp.server.config.xml.Filter;
import com.jagornet.dhcp.server.config.xml.FiltersType;
import com.jagornet.dhcp.server.config.xml.Link;
import com.jagornet.dhcp.server.config.xml.LinkFilter;
import com.jagornet.dhcp.server.config.xml.LinkFiltersType;
import com.jagornet.dhcp.server.config.xml.PoliciesType;
import com.jagornet.dhcp.server.config.xml.Policy;
import com.jagornet.dhcp.server.rest.JerseyRestServer;

/**
 * The Class DhcpServerPolicies.
 * Description: The class representing the DHCP server policies.  Policy defaults
 * are defined herein by the Property enum.  These defaults can be overridden by loading
 * a standard Java properties file.  At runtime, the value of any given property is taken
 * from the server's XML configuration file via the <policy> element with the corresponding
 * name.  Some policies are supported only globally, e.g. queueSize, whereas others may be
 * defined at various "levels" of the DHCP server's XML configuration, e.g. preferredLifetime,
 * which will allow for lower-level overrides of global or other hierarchical policy values.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpServerPolicies
{
	/**
	 * The Property enum.
	 */
	public enum Property {
		
		BINDING_MANAGER_REAPER_STARTUP_DELAY("binding.manager.reaper.startupDelay", "10000"),
		BINDING_MANAGER_REAPER_RUN_PERIOD("binding.manager.reaper.runPeriod", "60000"),
		BINDING_MANAGER_OFFER_EXPIRATION("binding.manager.offerExpiration", "120000"),
		BINDING_MANAGER_DELETE_OLD_BINDINGS("binding.manager.deleteOldBindings", "false"),
		// caching is buggy and not really faster, so turn it off for now
		BINDING_MANAGER_IA_CACHE_SIZE("binding.manager.iaCacheSize", "0"),
		// caching is buggy and not really faster, so turn it off for now
		BINDING_MANAGER_LEASE_CACHE_SIZE("binding.manager.leaseCacheSize", "0"),
		BINDING_MANAGER_RECONCILE_POOLS_ON_STARTUP("binding.manager.reconcilePoolsOnStartup", "true"),

		CHANNEL_THREADPOOL_SIZE("channel.threadPoolSize", "16"),
		CHANNEL_READ_BUFFER_SIZE("channel.readBufferSize", "307200"),		// 300 bytes x 1K clients
		CHANNEL_WRITE_BUFFER_SIZE("channel.writeBufferSize", "307200"),		// 300 bytes x 1K clients
		
		DATABASE_SCHEMA_TYTPE("database.schemaType", "jdbc-h2"),	// h2 performs best
		DATABASE_SCHEMA_VERSION("database.schemaVersion", "2"),
		
		DDNS_UPDATE("ddns.update", "none"),	// acceptable values: none, server, client, etc...
		DDNS_SYNCHRONIZE("ddns.synchronize", "false"),
		DDNS_DOMAIN("ddns.domain", ""),
		DDNS_TTL("ddns.ttl", "0.3"),	// 1/3 of the lifetime
		DDNS_SERVER("ddns.server", ""),
		DDNS_TSIG_KEYNAME("ddns.tsig.keyName", ""),
		DDNS_TSIG_ALGORITHM("ddns.tsig.algorithm", ""),
		DDNS_TSIG_KEYDATA("ddns.tsig.keyData", ""),
		DDNS_FORWARD_ZONE_NAME("ddns.forward.zone.name", ""),
		DDNS_FORWARD_ZONE_TTL("ddns.forward.zone.ttl", "0.3"),
		DDNS_FORWARD_ZONE_SERVER("ddns.forward.zone.server", ""),
		DDNS_FORWARD_ZONE_TSIG_KEYNAME("ddns.forward.zone.tsig.keyName", ""),
		DDNS_FORWARD_ZONE_TSIG_ALGORITHM("ddns.forward.zone.tsig.algorithm", ""),
		DDNS_FORWARD_ZONE_TSIG_KEYDATA("ddns.forward.zone.tsig.keyData", ""),
		DDNS_REVERSE_ZONE_NAME("ddns.reverse.zone.name", ""),
		DDNS_REVERSE_ZONE_BITLENGTH("ddns.reverse.zone.bitLength", "64"),
		DDNS_REVERSE_ZONE_TTL("ddns.reverse.zone.ttl", "0.3"),
		DDNS_REVERSE_ZONE_SERVER("ddns.reverse.zone.server", ""),
		DDNS_REVERSE_ZONE_TSIG_KEYNAME("ddns.reverse.zone.tsig.keyName", ""),
		DDNS_REVERSE_ZONE_TSIG_ALGORITHM("ddns.reverse.zone.tsig.algorithm", ""),
		DDNS_REVERSE_ZONE_TSIG_KEYDATA("ddns.reverse.zone.tsig.keyData", ""),
		
		DHCP_PROCESSOR_RECENT_MESSAGE_TIMER("dhcp.processor.recentMessageTimer", "5000"),
		DHCP_IGNORE_LOOPBACK("dhcp.ignoreLoopback", "true"),
		DHCP_IGNORE_LINKLOCAL("dhcp.ignoreLinkLocal", "true"),
		DHCP_IGNORE_SELF_PACKETS("dhcp.ignoreSelfPackets", "true"),
		
		DHCP_SEND_REQUESTED_OPTIONS_ONLY("dhcp.sendRequestedOptionsOnly", "false"),
		DHCP_SUPPORT_RAPID_COMMIT("dhcp.supportRapidCommit", "false"),
		
		V4_HEADER_SNAME("v4.header.sname", ""),
		V4_HEADER_FILENAME("v4.header.filename", ""),
		V4_IGNORED_MACS("v4.ignoredMacAddrs", "000000000000, FFFFFFFFFFFF"),
		V4_DEFAULT_LEASETIME("v4.defaultLeasetime", "3600"),
		V4_PINGCHECK_TIMEOUT("v4.pingCheckTimeout", "0"),

		V6_IA_NA_T1("v6.iaNaT1", "0.5"),
		V6_IA_NA_T2("v6.iaNaT2", "0.8"),
		V6_IA_PD_T1("v6.iaPdT1", "0.5"),
		V6_IA_PD_T2("v6.iaPdT2", "0.8"),
		V6_PREFERRED_LIFETIME("v6.preferredLifetime", "3600"),
		V6_VALID_LIFETIME("v6.validLifetime", "3600"),
		V6_VERIFY_UNKNOWN_REBIND("v6.verifyUnknownRebind", "false"),
		
		HA_ROLE("ha.role", ""),
		HA_PROTOCOL("ha.protocol", "grpc"),	// rest or grpc
		HA_USERNAME("ha.username", "hapeer"),		// for rest
		HA_PASSWORD("ha.password", "jagornet"),	// for rest
		HA_PEER_USERNAME("ha.peerUsername", "hapeer"),	// for rest
		HA_PEER_PASSWORD("ha.peerPassword", "jagornet"),	// for rest
		HA_DATABASE_FILE("ha.stateDbFile", "db/ha/jagornet-ha-state.db"),
		HA_DATABASE_MAX_STORED_STATES("ha.maxStoredStates", "10"),
		HA_BINDING_UPDATE_MODE("ha.bindingUpdateMode", "sync"),	// sync, async, database 
		HA_PEER_SERVER("ha.peerServer", ""),
		HA_PEER_PORT("ha.peerPort", String.valueOf(JerseyRestServer.HTTPS_SERVER_PORT)),
		HA_POLL_SECONDS("ha.pollSeconds", "30"),
		HA_POLL_REPLY_TIMEOUT("ha.pollReplyTimeout", "1000"),	// milliseconds
		HA_POLL_REPLY_FAILURE_COUNT("ha.pollReplyFailureCount", "5"),
		HA_CONTROL_REQUEST_ALL_LEASES_ON_RESTART("ha.requestAllLeasesOnRestart", "true"),
		
		REST_API_USERNAME("rest.api.username", "jagornet"),
		REST_API_PASSWORD("rest.api.password", "jagornet"),
		;
		
	    /** The key. */
    	private final String key;
	    
    	/** The value. */
    	private final String value;
	    
    	/**
    	 * Instantiates a new property.
    	 * 
    	 * @param key the key
    	 * @param value the value
    	 */
    	Property(String key, String value) {
	        this.key = key;
	        this.value = value;
	    }
	    
    	/**
    	 * Key.
    	 * 
    	 * @return the string
    	 */
    	public String key()   { return key; }
	    
    	/**
    	 * Value.
    	 * 
    	 * @return the string
    	 */
    	public String value() { return value; }
	}
	
	/** The Constant DEFAULT_PROPERTIES. */
	protected static final Properties DEFAULT_PROPERTIES = new Properties();
	static {
		Property[] defaultProperties = Property.values();
		for (Property prop : defaultProperties) {
			DEFAULT_PROPERTIES.put(prop.key(), prop.value());
		}
	}
	
	/** The SERVER properties. */
	protected static Properties SERVER_PROPERTIES = new Properties(DEFAULT_PROPERTIES);
	
	/**
	 * Load properties file.
	 * 
	 * @param propertiesFilename the properties filename
	 * 
	 * @throws IOException the exception
	 */
	public static void loadPropertiesFile(String propertiesFilename) throws IOException
	{
		Reader reader = null; 
		try {
			reader = new FileReader(propertiesFilename);
			SERVER_PROPERTIES.load(reader);
		}
		finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
	
	/**
	 * For testing, allow direct manipulation of policy values.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public static void setProperty(Property prop, String value)
	{
		if (SERVER_PROPERTIES != null) {
			SERVER_PROPERTIES.setProperty(prop.key(), value);
		}
	}
	
	/**
	 * Gets the properties.
	 * 
	 * @return the properties
	 */
	public static Properties getProperties()
	{
		return SERVER_PROPERTIES;
	}
	
    /**
     * Global policy.
     * 
     * @param prop the prop
     * 
     * @return the string
     */
    public static String globalPolicy(Property prop)
    {
    	String policy = 
    		getPolicy(DhcpServerConfiguration.getInstance().getGlobalPolicies(),
    				prop.key());
    	if (policy != null) {
    		return policy;
    	}
    	return SERVER_PROPERTIES.getProperty(prop.key());
    }

    /**
	 * Global policy as boolean.
	 * 
	 * @param prop the prop
	 * 
	 * @return true, if successful
	 */
	public static boolean globalPolicyAsBoolean(Property prop)
    {
    	return Boolean.parseBoolean(globalPolicy(prop));
    }
    
    /**
	 * Global policy as int.
	 * 
	 * @param prop the prop
	 * 
	 * @return the int
	 */
	public static int globalPolicyAsInt(Property prop)
    {
    	return Integer.parseInt(globalPolicy(prop));
    }
    
    /**
	 * Global policy as long.
	 * 
	 * @param prop the prop
	 * 
	 * @return the long
	 */
	public static long globalPolicyAsLong(Property prop)
    {
    	return Long.parseLong(globalPolicy(prop));
    }
    
    
    /**
	 * Global policy as float.
	 * 
	 * @param prop the prop
	 * 
	 * @return the float
	 */
	public static float globalPolicyAsFloat(Property prop)
    {
    	return Float.parseFloat(globalPolicy(prop));
    }
	
	/**
	 * Global policy as string.
	 *
	 * @param requestMsg the request msg
	 * @param prop the prop
	 * @return the string
	 */
    public static String globalPolicy(DhcpMessage requestMsg, Property prop)
    {
    	DhcpServerConfiguration config = DhcpServerConfiguration.getInstance();
    	String policy = null;
    	if (requestMsg != null) {
    		FiltersType filtersType = config.getGlobalFilters();
    		if (filtersType != null) {
    			List<Filter> filters = filtersType.getFilterList();
    			for (Filter filter : filters) {
					if (DhcpServerConfiguration.msgMatchesFilter(requestMsg, filter)) {
						policy = getPolicy(filter.getPolicies(), prop.key());
					}
				}
				// if the client request matches at least one global filter,
				// and that filter has configured a value for the policy, then 
				// return that value from the last filter that the client matches
    			if (policy != null) {
    				return policy;
    			}
    		}
    	}
		// client does not match a global filter 
		// get the value of the global policy, if any
    	policy = getPolicy(config.getGlobalPolicies(), prop.key());
    	if (policy != null) {
    		return policy;
    	}
    	// fall back to the configured default value
    	return SERVER_PROPERTIES.getProperty(prop.key());
    }


    /**
     * Global policy as boolean.
     *
     * @param requestMsg the request msg
     * @param prop the prop
     * @return true, if successful
     */
	public static boolean globalPolicyAsBoolean(DhcpMessage requestMsg, Property prop)
    {
    	return Boolean.parseBoolean(globalPolicy(requestMsg, prop));
    }
    
    /**
     * Global policy as int.
     *
     * @param requestMsg the request msg
     * @param prop the prop
     * @return the int
     */
	public static int globalPolicyAsInt(DhcpMessage requestMsg, Property prop)
    {
    	return Integer.parseInt(globalPolicy(requestMsg, prop));
    }
    
    /**
     * Global policy as long.
     *
     * @param requestMsg the request msg
     * @param prop the prop
     * @return the long
     */
	public static long globalPolicyAsLong(DhcpMessage requestMsg, Property prop)
    {
    	return Long.parseLong(globalPolicy(requestMsg, prop));
    }
    
    
    /**
     * Global policy as float.
     *
     * @param requestMsg the request msg
     * @param prop the prop
     * @return the float
     */
	public static float globalPolicyAsFloat(DhcpMessage requestMsg, Property prop)
    {
    	return Float.parseFloat(globalPolicy(requestMsg, prop));
    }
    
    /**
     * Effective policy.
     * 
     * @param link the link
     * @param prop the prop
     * 
     * @return the string
     */
    public static String effectivePolicy(Link link, Property prop)
    {
    	String policy = getPolicy(link.getPolicies(), prop.key());
    	if (policy != null) {
    		return policy;
    	}
    	return globalPolicy(prop);
    }
    
    /**
	 * Effective policy as boolean.
	 * 
	 * @param link the link
	 * @param prop the prop
	 * 
	 * @return true, if successful
	 */
	public static boolean effectivePolicyAsBoolean(Link link, Property prop)
    {
    	return Boolean.parseBoolean(effectivePolicy(link, prop));
    }
    
    /**
	 * Effective policy as int.
	 * 
	 * @param link the link
	 * @param prop the prop
	 * 
	 * @return the int
	 */
	public static int effectivePolicyAsInt(Link link, Property prop)
    {
    	return Integer.parseInt(effectivePolicy(link, prop));
    }
    
    /**
	 * Effective policy as long.
	 * 
	 * @param link the link
	 * @param prop the prop
	 * 
	 * @return the long
	 */
	public static long effectivePolicyAsLong(Link link, Property prop)
    {
    	return Long.parseLong(effectivePolicy(link, prop));
    }
    
    /**
	 * Effective policy as float.
	 * 
	 * @param link the link
	 * @param prop the prop
	 * 
	 * @return the float
	 */
	public static float effectivePolicyAsFloat(Link link, Property prop)
    {
    	return Float.parseFloat(effectivePolicy(link, prop));
    }
	
	/**
	 * Effective policy.
	 *
	 * @param requestMsg the request msg
	 * @param link the link
	 * @param prop the prop
	 * @return the string
	 */
	public static String effectivePolicy(DhcpMessage requestMsg, 
			Link link, Property prop)
	{
		String policy = null;
		if ((requestMsg != null) && (link != null)) {
			LinkFiltersType linkFiltersType = link.getLinkFilters();
			if (linkFiltersType != null) {
				List<LinkFilter> linkFilters = linkFiltersType.getLinkFilterList();
				if (linkFilters != null) {
					for (LinkFilter linkFilter : linkFilters) {
						if (DhcpServerConfiguration.msgMatchesFilter(requestMsg, linkFilter)) {
							policy = getPolicy(linkFilter.getPolicies(), prop.key());
						}
					}
					// if the client request matches at least one filter on the link,
					// and that filter has configured a value for the policy, then return
					// that value from the last filter that the client matches
			    	if (policy != null) {
			    		return policy;
			    	}
				}
			}
		}
		if (link != null) {
			// client does not match a link filter 
			// get the value of the policy on the link, if any
			policy = getPolicy(link.getPolicies(), prop.key());
			if (policy != null) {
				return policy;
			}
		}
    	return globalPolicy(prop);
	}
    
    /**
     * Effective policy as boolean.
     *
     * @param requestMsg the request msg
     * @param link the link
     * @param prop the prop
     * @return true, if successful
     */
	public static boolean effectivePolicyAsBoolean(DhcpMessage requestMsg, 
			Link link, Property prop)
    {
    	return Boolean.parseBoolean(effectivePolicy(requestMsg, link, prop));
    }
    
    /**
     * Effective policy as int.
     *
     * @param requestMsg the request msg
     * @param link the link
     * @param prop the prop
     * @return the int
     */
	public static int effectivePolicyAsInt(DhcpMessage requestMsg, 
			Link link, Property prop)
    {
    	return Integer.parseInt(effectivePolicy(requestMsg, link, prop));
    }
    
    /**
     * Effective policy as long.
     *
     * @param requestMsg the request msg
     * @param link the link
     * @param prop the prop
     * @return the long
     */
	public static long effectivePolicyAsLong(DhcpMessage requestMsg, 
			Link link, Property prop)
    {
    	return Long.parseLong(effectivePolicy(requestMsg, link, prop));
    }
    
    /**
     * Effective policy as float.
     *
     * @param requestMsg the request msg
     * @param link the link
     * @param prop the prop
     * @return the float
     */
	public static float effectivePolicyAsFloat(DhcpMessage requestMsg, 
			Link link, Property prop)
    {
    	return Float.parseFloat(effectivePolicy(requestMsg, link, prop));
    }
    
    /**
     * Effective policy.
     * 
     * @param pool the pool
     * @param link the link
     * @param prop the prop
     * 
     * @return the string
     */
    public static String effectivePolicy(DhcpConfigObject configObj, Link link, Property prop)
    {
    	String policy = null;
    	if (configObj != null) {
	    	policy = getPolicy(configObj.getPolicies(), prop.key());
	    	if (policy != null) {
	    		return policy;
	    	}
    	}
    	if (link != null) {
	    	policy = getPolicy(link.getPolicies(), prop.key());
	    	if (policy != null) {
	    		return policy;
	    	}
    	}
    	return globalPolicy(prop);
    }
    
    /**
	 * Effective policy as boolean.
	 * 
	 * @param pool the pool
	 * @param link the link
	 * @param prop the prop
	 * 
	 * @return true, if successful
	 */
	public static boolean effectivePolicyAsBoolean(DhcpConfigObject configObj, 
			Link link, Property prop)
    {
    	return Boolean.parseBoolean(effectivePolicy(configObj, link, prop));
    }
    
    /**
	 * Effective policy as int.
	 * 
	 * @param pool the pool
	 * @param link the link
	 * @param prop the prop
	 * 
	 * @return the int
	 */
	public static int effectivePolicyAsInt(DhcpConfigObject configObj, 
			Link link, Property prop)
    {
    	return Integer.parseInt(effectivePolicy(configObj, link, prop));
    }
    
    /**
	 * Effective policy as long.
	 * 
	 * @param pool the pool
	 * @param link the link
	 * @param prop the prop
	 * 
	 * @return the long
	 */
	public static long effectivePolicyAsLong(DhcpConfigObject configObj, 
			Link link, Property prop)
    {
    	return Long.parseLong(effectivePolicy(configObj, link, prop));
    }
    
    /**
	 * Effective policy as float.
	 * 
	 * @param pool the pool
	 * @param link the link
	 * @param prop the prop
	 * 
	 * @return the float
	 */
	public static float effectivePolicyAsFloat(DhcpConfigObject configObj, 
			Link link, Property prop)
    {
    	return Float.parseFloat(effectivePolicy(configObj, link, prop));
    }
	
    /**
     * Effective policy.
     *
     * @param requestMsg the request msg
     * @param filtersType the filters type
     * @param policiesType the policies type
     * @param link the link
     * @param prop the prop
     * @return the string
     */
    public static String effectivePolicy(DhcpMessage requestMsg, 
    		DhcpConfigObject configObj, Link link, Property prop)
    {
		String policy = null;
		if ((requestMsg != null) && (configObj != null)) {
			if (configObj.getFilters() != null) {
				List<Filter> filters = configObj.getFilters().getFilterList();
				if (filters != null) {
					for (Filter filter : filters) {
						if (DhcpServerConfiguration.msgMatchesFilter(requestMsg, filter)) {
							policy = getPolicy(filter.getPolicies(), prop.key());
						}
					}
					// if the client request matches at least one filter on the pool,
					// and that filter has configured a value for the policy, then return
					// that value from the last filter that the client matches
			    	if (policy != null) {
			    		return policy;
			    	}
				}
			}
		}
		if (configObj != null) {
			// client does not match a pool filter 
			// get the value of the policy on the pool, if any
			policy = getPolicy(configObj.getPolicies(), prop.key());
			if (policy != null) {
				return policy;
			}
		}
    	return effectivePolicy(requestMsg, link, prop);
    }
    
    /**
     * Effective policy as boolean.
     *
     * @param requestMsg the request msg
     * @param pool the pool
     * @param link the link
     * @param prop the prop
     * @return true, if successful
     */
	public static boolean effectivePolicyAsBoolean(DhcpMessage requestMsg, 
			DhcpConfigObject configObj, Link link, Property prop)
    {
    	return Boolean.parseBoolean(effectivePolicy(requestMsg, configObj, link, prop));
    }
    
    /**
     * Effective policy as int.
     *
     * @param requestMsg the request msg
     * @param pool the pool
     * @param link the link
     * @param prop the prop
     * @return the int
     */
	public static int effectivePolicyAsInt(DhcpMessage requestMsg, 
			DhcpConfigObject configObj, Link link, Property prop)
    {
    	return Integer.parseInt(effectivePolicy(requestMsg, configObj, link, prop));
    }
    
    /**
     * Effective policy as long.
     *
     * @param requestMsg the request msg
     * @param pool the pool
     * @param link the link
     * @param prop the prop
     * @return the long
     */
	public static long effectivePolicyAsLong(DhcpMessage requestMsg, 
			DhcpConfigObject configObj, Link link, Property prop)
    {
    	return Long.parseLong(effectivePolicy(requestMsg, configObj, link, prop));
    }
    
    /**
     * Effective policy as float.
     *
     * @param requestMsg the request msg
     * @param pool the pool
     * @param link the link
     * @param prop the prop
     * @return the float
     */
	public static float effectivePolicyAsFloat(DhcpMessage requestMsg, 
			DhcpConfigObject configObj, Link link, Property prop)
    {
    	return Float.parseFloat(effectivePolicy(requestMsg, configObj, link, prop));
    }    

    /**
     * Gets the policy.
     * 
     * @param policies the policies
     * @param name the name
     * 
     * @return the policy
     */
    protected static String getPolicy(PoliciesType policies, String name)
    {
		if (policies != null) {
			List<Policy> policyList = policies.getPolicyList();
			if (policyList != null) {
				for (Policy policy : policyList) {
					if (policy.getName().equalsIgnoreCase(name)) {
						return policy.getValue();
					}
				}
			}
		}
    	return null;
    }

}
