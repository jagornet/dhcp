/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpServerConfiguration.java is part of DHCPv6.
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.xmlbeans.XmlOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.option.OpaqueDataUtil;
import com.jagornet.dhcpv6.server.DhcpV6Server;
import com.jagornet.dhcpv6.util.Subnet;
import com.jagornet.dhcpv6.xml.DhcpV6ServerConfigDocument;
import com.jagornet.dhcpv6.xml.Link;
import com.jagornet.dhcpv6.xml.LinksType;
import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.PoliciesType;
import com.jagornet.dhcpv6.xml.Policy;
import com.jagornet.dhcpv6.xml.ServerIdOption;
import com.jagornet.dhcpv6.xml.DhcpV6ServerConfigDocument.DhcpV6ServerConfig;

/**
 * Title: DhcpServerConfiguration
 * Description: The class representing the DHCPv6 server configuration.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpServerConfiguration
{	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpServerConfiguration.class);

    /** The CONFIG. */
    private static volatile DhcpV6ServerConfig CONFIG;
    
    /** The link map. */
    private static TreeMap<Subnet, Link> linkMap;
    
    /**
     * Private constructor suppresses generation of a (public) default constructor
     */
    private DhcpServerConfiguration() {}

    /**
     * Initialize the DhcpServerConfiguration.
     * 
     * @param filename the full path and filename of the server configuration
     * 
     * @throws Exception the exception
     */
    public static void init(String filename) 
        throws Exception
    {
        if (CONFIG == null)
            synchronized(DhcpServerConfiguration.class) {
                if (CONFIG == null) {
                    CONFIG = loadConfig(filename);
                    initServerId();
                    initLinkMap();
                }
            }
    }
    
    /**
     * Initialize the server id.
     * 
     * @throws Exception the exception
     */
    public static void initServerId() throws Exception
    {
    	ServerIdOption serverId = CONFIG.getServerIdOption();
    	if (serverId != null) {
	    	OpaqueData opaque = serverId.getOpaqueData();
	    	if ( ( (opaque == null) ||
	    		   ((opaque.getAsciiValue() == null) || (opaque.getAsciiValue().length() <= 0)) &&
	    		   ((opaque.getHexValue() == null) || (opaque.getHexValue().length <= 0)) ) ) {
	    		OpaqueData duid = OpaqueDataUtil.generateDUID_LLT();
	    		if (duid == null) {
	    			throw new IllegalStateException("Failed to create ServerID");
	    		}
	    		serverId.setOpaqueData(duid);
	    		CONFIG.setServerIdOption(serverId);
	    		saveConfig(CONFIG, DhcpV6Server.DEFAULT_CONFIG_FILENAME);
	    	}
    	}
    }
    
    /**
     * Initialize the link map.
     * 
     * @throws Exception the exception
     */
    public static void initLinkMap() throws Exception
    {
        linkMap = 
            new TreeMap<Subnet, Link>(new Comparator<Subnet>() {

                public int compare(Subnet s1, Subnet s2)
                {
                    BigInteger bi1 = new BigInteger(s1.getSubnetAddress().getAddress());
                    BigInteger bi2 = new BigInteger(s2.getSubnetAddress().getAddress());
                    if (bi1.equals(bi2)) {
                        // if we have two subnets with the same starting address
                        // then the "smaller" subnet is the one with the larger
                        // prefix length, which logically places the more specific
                        // subnet _before_ the less specific subnet in the map
                        // this allows us to work from "inside-out"?
                        if (s1.getPrefixLength() > s2.getPrefixLength())
                            return -1;
                        else if (s1.getPrefixLength() < s2.getPrefixLength())
                            return 1;
                        else
                            return 0;
                    }
                    else {
                        // subnet addresses are different, so return
                        // the standard compare for the address
                        return bi1.compareTo(bi2);
                    }
                }
                
            });
        
        try {
        	LinksType linksType = CONFIG.getLinks();
        	if (linksType != null) {
	        	List<Link> links = linksType.getLinkList();
	            if ((links != null) && !links.isEmpty()) {
	                for (Link link : links) {
	                    String addr = link.getAddress();
	                    if (addr != null) {
	                        String[] subnet = addr.split("/");
	                        if ((subnet != null) && (subnet.length == 2)) {
	                            Subnet s = new Subnet(subnet[0], subnet[1]);
	                            linkMap.put(s, link);
	                        }
	                    }
	                }
	        	}
        	}
        }
        catch (Exception ex) {
            log.error("Failed to build linkMap: " + ex);
            throw ex;
        }
    }
    
    /**
     * Gets the server configuration.
     * 
     * @return the DhcpV6ServerConfig object representing the server's configuration
     */
    public static DhcpV6ServerConfig getConfig()
    {
        if (CONFIG == null) {
            throw new IllegalStateException("DhcpServerConfiguration not initialized");
        }
        return CONFIG;
    }
    
    /**
     * Gets the link map.
     * 
     * @return the link map
     */
    public static TreeMap<Subnet, Link> getLinkMap()
    {
        if (CONFIG == null) {
            throw new IllegalStateException("DhcpServerConfiguration not initialized");
        }
        return linkMap;
    }

    /**
     * Find link for address.
     * 
     * @param inetAddr an InetAddress to find a Link for
     * 
     * @return the link
     */
    public static Link findLinkForAddress(InetAddress inetAddr)
    {
        Link link = null;
        if ((linkMap != null) && !linkMap.isEmpty()) {
            Subnet s = new Subnet(inetAddr, 128);
            SortedMap<Subnet, Link> subMap = linkMap.headMap(s);
            if ((subMap != null) && !subMap.isEmpty()) {
                s = subMap.lastKey();
                if (s.contains(inetAddr)) {
                    link = subMap.get(s);
                }
            }
        }
        return link;
    }
    
    /**
     * Load the server configuration from a file.
     * 
     * @param filename the full path and filename for the configuration
     * 
     * @return the loaded DhcpV6ServerConfig 
     * 
     * @throws Exception the exception
     */
    public static DhcpV6ServerConfig loadConfig(String filename) throws Exception
    {
    	DhcpV6ServerConfig config = null;
    	FileInputStream fis = null;
    	try {
	        log.info("Loading server configuration: " + filename);
	        fis = new FileInputStream(filename);
	        long start = System.currentTimeMillis();
	        config = DhcpV6ServerConfigDocument.Factory.parse(fis).getDhcpV6ServerConfig();
	        log.info("Server configuration loaded in " + (System.currentTimeMillis()-start) + "ms");
    	}
    	finally {
    		if (fis != null) {
    			try { fis.close(); } catch (IOException ex) { }
    		}
    	}
    	return config;
    }
    
    /**
     * Save the server configuration to a file.
     * 
     * @param config the DhcpV6ServerConfig to save
     * @param filename the full path and filename for the configuration
     * 
     * @throws Exception the exception
     */
    public static void saveConfig(DhcpV6ServerConfig config, String filename) throws Exception
    {
    	FileOutputStream fos = null;
    	try {
	        log.info("Saving server configuration: " + filename);
	        fos = new FileOutputStream(filename);
	        long start = System.currentTimeMillis();			
	        DhcpV6ServerConfigDocument doc = DhcpV6ServerConfigDocument.Factory.newInstance();
	        doc.setDhcpV6ServerConfig(config);
	        doc.save(fos, new XmlOptions().setSavePrettyPrint().setSavePrettyPrintIndent(4));
	        log.info("Server configuration saved in " + (System.currentTimeMillis()-start) + "ms");
    	}
    	finally {
    		if (fos != null) {
    			try { fos.close(); } catch (IOException ex) { }
    		}
    	}
    }
    
    public static boolean getBooleanPolicy(String name, boolean defaultValue)
    {
    	String val = getStringPolicy(name, String.valueOf(defaultValue));
    	if (val != null) {
    		return Boolean.parseBoolean(val);
    	}
    	return defaultValue;
    }
    
    public static int getIntPolicy(String name, int defaultValue)
    {
    	String val = getStringPolicy(name, String.valueOf(defaultValue));
    	if (val != null) {
    		return Integer.parseInt(val);
    	}
    	return defaultValue;
    }
    
    public static String getStringPolicy(String name, String defaultValue)
    {
    	if (CONFIG != null) {
    		PoliciesType policies = CONFIG.getPolicies();
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
    	}
    	return defaultValue;
    }
}
