/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpServerPolicies.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.server.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Properties;

import com.jagornet.dhcpv6.xml.AddressPool;
import com.jagornet.dhcpv6.xml.Link;
import com.jagornet.dhcpv6.xml.PoliciesType;
import com.jagornet.dhcpv6.xml.Policy;
import com.jagornet.dhcpv6.xml.PrefixPool;

/**
 * The Class DhcpServerPolicies.
 */
public class DhcpServerPolicies
{
	/**
	 * The Property enum.
	 */
	public enum Property {
		MCAST_SERVER_QUEUE_SIZE ("mcast.server.queueSize", "10000"),
		MCAST_SERVER_POOL_SIZE ("mcast.server.poolSize", "100"),
		UCAST_SERVER_QUEUE_SIZE ("ucast.server.queueSize", "10000"),
		DHCP_PROCESSOR_RECENT_MESSAGE_TIMER("dhcp.processor.recentMessageTimer", "5000"),
		BINDING_MANAGER_REAPER_STARTUP_DELAY("binding.manager.reaper.startupDelay", "10000"),
		BINDING_MANAGER_REAPER_RUN_PERIOD("binding.manager.reaper.runPeriod", "60000"),
		BINDING_MANAGER_DELETE_OLD_BINDINGS("binding.manager.deleteOldBindings", "false"),
		SEND_REQUESTED_OPTIONS_ONLY("sendRequestedOptionsOnly", "false"),
		SUPPORT_RAPID_COMMIT("supportRapidCommit", "false"),
		VERIFY_UNKNOWN_REBIND("verifyUnknownRebind", "false"),
		PREFERRED_LIFETIME("preferredLifetime", "3600"),
		VALID_LIFETIME("validLifetime", "3600"),
		IA_NA_T1("iaNaT1", "0.5"),
		IA_NA_T2("iaNaT2", "0.8"),
		IA_PD_T1("iaPdT1", "0.5"),
		IA_PD_T2("iaPdT2", "0.8"),
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
	 * @throws Exception the exception
	 */
	public static void loadPropertiesFile(String propertiesFilename) throws Exception
	{
		Reader reader = null; 
		try {
			reader = new FileReader(propertiesFilename);
			SERVER_PROPERTIES.load(reader);
		} catch (FileNotFoundException ex) {
			throw ex;
		} catch (IOException ex) {
			throw ex;
		}
		finally {
			if (reader != null) {
				try { reader.close(); } catch (IOException ex) { }
			}
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
    		getPolicy(DhcpServerConfiguration.getInstance().getDhcpV6ServerConfig().getPolicies(),
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
     * @param pool the pool
     * @param link the link
     * @param prop the prop
     * 
     * @return the string
     */
    public static String effectivePolicy(AddressPool pool, Link link, Property prop)
    {
    	String policy = getPolicy(pool.getPolicies(), prop.key());
    	if (policy != null) {
    		return policy;
    	}
    	policy = getPolicy(link.getPolicies(), prop.key());
    	if (policy != null) {
    		return policy;
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
	public static boolean effectivePolicyAsBoolean(AddressPool pool, Link link, Property prop)
    {
    	return Boolean.parseBoolean(effectivePolicy(pool, link, prop));
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
	public static int effectivePolicyAsInt(AddressPool pool, Link link, Property prop)
    {
    	return Integer.parseInt(effectivePolicy(pool, link, prop));
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
	public static long effectivePolicyAsLong(AddressPool pool, Link link, Property prop)
    {
    	return Long.parseLong(effectivePolicy(pool, link, prop));
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
	public static float effectivePolicyAsFloat(AddressPool pool, Link link, Property prop)
    {
    	return Float.parseFloat(effectivePolicy(pool, link, prop));
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
    public static String effectivePolicy(PrefixPool pool, Link link, Property prop)
    {
    	String policy = getPolicy(pool.getPolicies(), prop.key());
    	if (policy != null) {
    		return policy;
    	}
    	policy = getPolicy(link.getPolicies(), prop.key());
    	if (policy != null) {
    		return policy;
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
	public static boolean effectivePolicyAsBoolean(PrefixPool pool, Link link, Property prop)
    {
    	return Boolean.parseBoolean(effectivePolicy(pool, link, prop));
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
	public static int effectivePolicyAsInt(PrefixPool pool, Link link, Property prop)
    {
    	return Integer.parseInt(effectivePolicy(pool, link, prop));
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
	public static long effectivePolicyAsLong(PrefixPool pool, Link link, Property prop)
    {
    	return Long.parseLong(effectivePolicy(pool, link, prop));
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
	public static float effectivePolicyAsFloat(PrefixPool pool, Link link, Property prop)
    {
    	return Float.parseFloat(effectivePolicy(pool, link, prop));
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
