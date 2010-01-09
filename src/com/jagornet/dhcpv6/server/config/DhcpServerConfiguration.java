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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.xmlbeans.XmlOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpComparableOption;
import com.jagornet.dhcpv6.option.DhcpConfigOptions;
import com.jagornet.dhcpv6.option.OpaqueDataUtil;
import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.server.DhcpV6Server;
import com.jagornet.dhcpv6.server.request.binding.Range;
import com.jagornet.dhcpv6.util.Subnet;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.DhcpV6ServerConfigDocument;
import com.jagornet.dhcpv6.xml.Filter;
import com.jagornet.dhcpv6.xml.FilterExpression;
import com.jagornet.dhcpv6.xml.FilterExpressionsType;
import com.jagornet.dhcpv6.xml.FiltersType;
import com.jagornet.dhcpv6.xml.Link;
import com.jagornet.dhcpv6.xml.LinkFilter;
import com.jagornet.dhcpv6.xml.LinkFiltersType;
import com.jagornet.dhcpv6.xml.LinksType;
import com.jagornet.dhcpv6.xml.OpaqueData;
import com.jagornet.dhcpv6.xml.OptionExpression;
import com.jagornet.dhcpv6.xml.Pool;
import com.jagornet.dhcpv6.xml.PoolsType;
import com.jagornet.dhcpv6.xml.ServerIdOption;
import com.jagornet.dhcpv6.xml.DhcpV6ServerConfigDocument.DhcpV6ServerConfig;

// TODO: Auto-generated Javadoc
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

	/** The INSTANCE. */
	private static DhcpServerConfiguration INSTANCE;
	
    /** The XML object representing the configuration. */
    private DhcpV6ServerConfig xmlServerConfig;
    
    /** The link map. */
    private SortedMap<Subnet, DhcpLink> linkMap;
    
    /** The config filename. */
    public static String configFilename = DhcpV6Server.DEFAULT_CONFIG_FILENAME;
    
    /**
     * Gets the single instance of DhcpServerConfiguration.
     * 
     * @return single instance of DhcpServerConfiguration
     */
    public static synchronized DhcpServerConfiguration getInstance()
    {
    	if (INSTANCE == null) {
    		try {
    			INSTANCE = new DhcpServerConfiguration();
    		}
    		catch (Exception ex) {
    			log.error("Failed to initialize DhcpServerConfiguration", ex);
    		}
    	}
    	return INSTANCE;
    }
    
    /**
     * Private constructor suppresses generation of a (public) default constructor.
     * 
     * @throws Exception the exception
     */
    private DhcpServerConfiguration() throws Exception
    {
    	xmlServerConfig = loadConfig(configFilename);
    	if (xmlServerConfig != null) {
	    	initServerId();
	    	initLinkMap();
    	}
    	else {
    		throw new IllegalStateException("Failed to load configuration file: " + configFilename);
    	}
    }
    
    
    /**
     * Initialize the server id.
     * 
     * @throws Exception the exception
     */
    protected void initServerId() throws Exception
    {
    	ServerIdOption serverId = xmlServerConfig.getServerIdOption();
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
	    		xmlServerConfig.setServerIdOption(serverId);
	    		saveConfig(xmlServerConfig, configFilename);
	    	}
    	}
    }
    
    /**
     * Initialize the link map.
     * 
     * @throws Exception the exception
     */
    protected void initLinkMap() throws Exception
    {
        try {
        	LinksType linksType = xmlServerConfig.getLinks();
        	if (linksType != null) {
	        	List<Link> links = linksType.getLinkList();
	            if ((links != null) && !links.isEmpty()) {
	                linkMap = new TreeMap<Subnet, DhcpLink>();
	                for (Link link : links) {
	                	String ifname = link.getInterface();
	                	if (ifname != null) {
	                		NetworkInterface netIf = NetworkInterface.getByName(ifname);
	                		if (netIf == null) {
	                			throw new DatatypeConfigurationException(
	                					"Network interface not found: " + ifname);
	                		}
	                		if (!netIf.supportsMulticast()) {
	                			throw new DatatypeConfigurationException(
	                					"Network interface does not support multicast: " + ifname);
	                		}
	                		// set this link's address to the IPv6 link-local address of the interface
	                		// when a packet is received on the link-local address, we use it
	                		// to find the client's Link as configured for the server
	                		link.setAddress(
	                				Util.netIfIPv6LinkLocalAddress(netIf).getHostAddress()
	                				+ "/128");	// subnet of one
	                	}
	                    String addr = link.getAddress();
	                    if (addr != null) {
	                        String[] s = addr.split("/");
	                        if ((s != null) && (s.length == 2)) {
	                            Subnet subnet = new Subnet(s[0], s[1]);
	                            linkMap.put(subnet, new DhcpLink(subnet, link));
	                        }
	                        else {
	                        	throw new DatatypeConfigurationException(
	                        			"Unsupported Link address=" + addr +
	                        			": expected prefix/prefixlen format");
	                        }
	                    }
                        else {
                        	throw new DatatypeConfigurationException(
                        			"Link must specify an interface or address element");
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
    public DhcpV6ServerConfig getDhcpV6ServerConfig()
    {
    	return xmlServerConfig;
    }
    
    /**
     * Gets the link map.
     * 
     * @return the link map
     */
    public SortedMap<Subnet, DhcpLink> getLinkMap()
    {
        return linkMap;
    }

    /**
     * Find link for address.
     * 
     * @param inetAddr an InetAddress to find a Link for
     * 
     * @return the link
     */
    public DhcpLink findLinkForAddress(InetAddress inetAddr)
    {
        DhcpLink link = null;
        if ((linkMap != null) && !linkMap.isEmpty()) {
            Subnet s = new Subnet(inetAddr, 128);
            SortedMap<Subnet, DhcpLink> subMap = linkMap.headMap(s);
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
     * Find dhcp link.
     * 
     * @param local the local
     * @param remote the remote
     * 
     * @return the dhcp link
     */
    public DhcpLink findDhcpLink(InetAddress local, InetAddress remote)
    {
        DhcpLink link = null;
        if ((linkMap != null) && !linkMap.isEmpty()) {
        	if (local.isLinkLocalAddress() && remote.isLinkLocalAddress()) {
        		// if the local and remote addresses are link-local, then
        		// we received the request directly from the client on the
        		// local address, which will be the IPv6 link-local address
        		// of the interface where the packet was received, and that
        		// link-local address will be a key in the linkMap
        		log.debug("Looking for local Link by address: " + local);
	            Subnet s = new Subnet(local, 128);
        		link = linkMap.get(s);
        	}
        	else {
        		// either we didn't receive this packet on the link-local address
        		// or if we did, then the packet was not sent from link-local, so
        		// we only need the remote address to search the linkMap
        		log.debug("Looking for remote Link by address: " + remote);
	            Subnet s = new Subnet(remote, 128);
	            SortedMap<Subnet, DhcpLink> subMap = linkMap.headMap(s);
	            if ((subMap != null) && !subMap.isEmpty()) {
	                s = subMap.lastKey();
	                if (s.contains(remote)) {
	                    link = subMap.get(s);
	                }
	            }
        	}
        }
        else {
        	log.error("linkMap is null or empty");
        }
        return link;
    }
    
    /**
     * Find pool.
     * 
     * @param link the link
     * @param addr the addr
     * 
     * @return the pool
     */
    public static Pool findPool(Link link, InetAddress addr)
    {
    	Pool pool = null;
    	if (link != null) {
			PoolsType poolsType = link.getPools();
			if (poolsType != null) {
				List<Pool> pools = poolsType.getPoolList();
				if ((pools != null) && !pools.isEmpty()) {
					for (Pool p : pools) {
						try {
							Range r = new Range(p.getRange());
							if (r.contains(addr)) {
								pool = p;
							}
						}
						catch (Exception ex) {
							// this can't happen because the parsing of the
							// pool Ranges is done at startup which would cause abort
							log.error("Invalid Pool Range: " + p.getRange());
//TODO										throw ex;
						}
					}
				}
			}
    		
    	}
    	return pool;
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
	        config = DhcpV6ServerConfigDocument.Factory.parse(fis).getDhcpV6ServerConfig();
	        log.info("Server configuration loaded.");
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
	        DhcpV6ServerConfigDocument doc = DhcpV6ServerConfigDocument.Factory.newInstance();
	        doc.setDhcpV6ServerConfig(config);
	        doc.save(fos, new XmlOptions().setSavePrettyPrint().setSavePrettyPrintIndent(4));
	        log.info("Server configuration saved.");
    	}
    	finally {
    		if (fos != null) {
    			try { fos.close(); } catch (IOException ex) { }
    		}
    	}
    }
    
    /**
     * Effective msg options.
     * 
     * @param requestMsg the request msg
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectiveMsgOptions(DhcpMessage requestMsg)
    {
    	Map<Integer, DhcpOption> optionMap = new HashMap<Integer, DhcpOption>();
    	DhcpConfigOptions configOptions = 
    		new DhcpConfigOptions(xmlServerConfig.getMsgConfigOptions(),
    				requestMsg.getRequestedOptionCodes());
    	if (configOptions != null) {
    		optionMap.putAll(configOptions.getDhcpOptionMap());
    	}
    	Map<Integer, DhcpOption> filteredOptions = 
    		filteredMsgOptions(requestMsg, xmlServerConfig.getFilters());
    	if (filteredOptions != null) {
    		optionMap.putAll(filteredOptions);
    	}
    	return optionMap;
    }

    /**
     * Effective msg options.
     * 
     * @param requestMsg the request msg
     * @param link the link
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectiveMsgOptions(DhcpMessage requestMsg, Link link)
    {
    	Map<Integer, DhcpOption> optionMap = effectiveMsgOptions(requestMsg);
    	if (link != null) {
	    	DhcpConfigOptions configOptions = 
	    		new DhcpConfigOptions(link.getMsgConfigOptions(),
	    				requestMsg.getRequestedOptionCodes());
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredMsgOptions(requestMsg, link.getLinkFilters());
	    	if (filteredOptions != null) {
	    		optionMap.putAll(filteredOptions);
	    	}
    	}
    	return optionMap;
    }
    
    /**
     * Effective ia options.
     * 
     * @param requestMsg the request msg
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectiveIaOptions(DhcpMessage requestMsg)
    {
    	Map<Integer, DhcpOption> optionMap = new HashMap<Integer, DhcpOption>();
    	DhcpConfigOptions configOptions = 
    		new DhcpConfigOptions(xmlServerConfig.getIaConfigOptions(),
    				requestMsg.getRequestedOptionCodes());
    	if (configOptions != null) {
    		optionMap.putAll(configOptions.getDhcpOptionMap());
    	}
    	
    	Map<Integer, DhcpOption> filteredOptions = 
    		filteredIaOptions(requestMsg, xmlServerConfig.getFilters());
    	if (filteredOptions != null) {
    		optionMap.putAll(filteredOptions);
    	}
    	return optionMap;
    }

    /**
     * Effective ia options.
     * 
     * @param requestMsg the request msg
     * @param link the link
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectiveIaOptions(DhcpMessage requestMsg, Link link)
    {
    	Map<Integer, DhcpOption> optionMap = effectiveIaOptions(requestMsg);
    	if (link != null) {
	    	DhcpConfigOptions configOptions = 
	    		new DhcpConfigOptions(link.getIaConfigOptions(),
	    				requestMsg.getRequestedOptionCodes());
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredIaOptions(requestMsg, link.getLinkFilters());
	    	if (filteredOptions != null) {
	    		optionMap.putAll(filteredOptions);
	    	}
    	}
    	return optionMap;
    }
    
    /**
     * Effective addr options.
     * 
     * @param requestMsg the request msg
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectiveAddrOptions(DhcpMessage requestMsg)
    {
    	Map<Integer, DhcpOption> optionMap = new HashMap<Integer, DhcpOption>();
    	DhcpConfigOptions configOptions = 
    		new DhcpConfigOptions(xmlServerConfig.getAddrConfigOptions(), 
    				requestMsg.getRequestedOptionCodes());
    	if (configOptions != null) {
    		optionMap.putAll(configOptions.getDhcpOptionMap());
    	}
    	
    	Map<Integer, DhcpOption> filteredOptions = 
    		filteredAddrOptions(requestMsg, xmlServerConfig.getFilters());
    	if (filteredOptions != null) {
    		optionMap.putAll(filteredOptions);
    	}
    	return optionMap;
    }

    /**
     * Effective addr options.
     * 
     * @param requestMsg the request msg
     * @param link the link
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectiveAddrOptions(DhcpMessage requestMsg, Link link)
    {
    	Map<Integer, DhcpOption> optionMap = effectiveIaOptions(requestMsg);
    	if (link != null) {
	    	DhcpConfigOptions configOptions = 
	    		new DhcpConfigOptions(link.getAddrConfigOptions(),
	    				requestMsg.getRequestedOptionCodes());
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredAddrOptions(requestMsg, link.getLinkFilters());
	    	if (filteredOptions != null) {
	    		optionMap.putAll(filteredOptions);
	    	}
    	}
    	return optionMap;
    }

    /**
     * Effective addr options.
     * 
     * @param requestMsg the request msg
     * @param link the link
     * @param pool the pool
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectiveAddrOptions(DhcpMessage requestMsg, Link link, Pool pool)
    {
    	Map<Integer, DhcpOption> optionMap = effectiveAddrOptions(requestMsg, link);
    	if (pool != null) {
	    	DhcpConfigOptions configOptions = 
	    		new DhcpConfigOptions(pool.getAddrConfigOptions(),
	    				requestMsg.getRequestedOptionCodes());
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredAddrOptions(requestMsg, pool.getFilters());
	    	if (filteredOptions != null) {
	    		optionMap.putAll(filteredOptions);
	    	}
    	}
    	return optionMap;
    }

    /**
     * Filtered msg options.
     * 
     * @param requestMsg the request msg
     * @param filtersType the filters type
     * 
     * @return the map< integer, dhcp option>
     */
    protected Map<Integer, DhcpOption> filteredMsgOptions(DhcpMessage requestMsg, 
    		FiltersType filtersType)
    {
    	if (filtersType != null) {
    		List<Filter> filters = filtersType.getFilterList();
    		return filteredMsgOptions(requestMsg, filters);
    	}
    	return null;
    }

    /**
     * Filtered msg options.
     * 
     * @param requestMsg the request msg
     * @param linkFiltersType the link filters type
     * 
     * @return the map< integer, dhcp option>
     */
    protected Map<Integer, DhcpOption> filteredMsgOptions(DhcpMessage requestMsg, 
    		LinkFiltersType linkFiltersType)
    {
    	if (linkFiltersType != null) {
    		List<LinkFilter> filters = linkFiltersType.getLinkFilterList();
    		return filteredMsgOptions(requestMsg, filters);
    	}
    	return null;
    }

    /**
     * Filtered msg options.
     * 
     * @param requestMsg the request msg
     * @param filters the filters
     * 
     * @return the map< integer, dhcp option>
     */
    private Map<Integer, DhcpOption> filteredMsgOptions(DhcpMessage requestMsg,
    		List<? extends Filter> filters)
    {
		if ((filters != null) && !filters.isEmpty()) {
            for (Filter filter : filters) {
            	if (msgMatchesFilter(requestMsg, filter)) {
                    log.info("Request matches filter: " + filter.getName());
                	DhcpConfigOptions filterConfigOptions = 
                		new DhcpConfigOptions(filter.getMsgConfigOptions(),
                				requestMsg.getRequestedOptionCodes());
                	if (filterConfigOptions != null) {
                		return filterConfigOptions.getDhcpOptionMap();
                	}
            	}
            }
		}
    	return null;
    }
    
    /**
     * Filtered ia options.
     * 
     * @param requestMsg the request msg
     * @param filtersType the filters type
     * 
     * @return the map< integer, dhcp option>
     */
    protected Map<Integer, DhcpOption> filteredIaOptions(DhcpMessage requestMsg,
    		FiltersType filtersType)
    {
    	if (filtersType != null) {
    		List<Filter> filters = filtersType.getFilterList();
    		return filteredIaOptions(requestMsg, filters);
    	}
    	return null;
    }

    /**
     * Filtered ia options.
     * 
     * @param requestMsg the request msg
     * @param linkFiltersType the link filters type
     * 
     * @return the map< integer, dhcp option>
     */
    protected Map<Integer, DhcpOption> filteredIaOptions(DhcpMessage requestMsg, 
    		LinkFiltersType linkFiltersType)
    {
    	if (linkFiltersType != null) {
    		List<LinkFilter> filters = linkFiltersType.getLinkFilterList();
    		return filteredIaOptions(requestMsg, filters);
    	}
    	return null;
    }

	/**
	 * Filtered ia options.
	 * 
	 * @param requestMsg the request msg
	 * @param filters the filters
	 * 
	 * @return the map< integer, dhcp option>
	 */
	private Map<Integer, DhcpOption> filteredIaOptions(DhcpMessage requestMsg,
			List<? extends Filter> filters)
	{
		if ((filters != null) && !filters.isEmpty()) {
		    for (Filter filter : filters) {
		    	if (msgMatchesFilter(requestMsg, filter)) {
		            log.info("Request matches filter: " + filter.getName());
		        	DhcpConfigOptions filterConfigOptions = 
		        		new DhcpConfigOptions(filter.getIaConfigOptions(),
		        				requestMsg.getRequestedOptionCodes());
		        	if (filterConfigOptions != null) {
		        		return filterConfigOptions.getDhcpOptionMap();
		        	}
		    	}
		    }
		}
		return null;
	}

    /**
     * Filtered addr options.
     * 
     * @param requestMsg the request msg
     * @param filtersType the filters type
     * 
     * @return the map< integer, dhcp option>
     */
    protected Map<Integer, DhcpOption> filteredAddrOptions(DhcpMessage requestMsg, 
    		FiltersType filtersType)
    {
    	if (filtersType != null) {
    		List<Filter> filters = filtersType.getFilterList();
    		return filteredAddrOptions(requestMsg, filters);
    	}
    	return null;
    }

    /**
     * Filtered addr options.
     * 
     * @param requestMsg the request msg
     * @param linkFiltersType the link filters type
     * 
     * @return the map< integer, dhcp option>
     */
    protected Map<Integer, DhcpOption> filteredAddrOptions(DhcpMessage requestMsg, 
    		LinkFiltersType linkFiltersType)
    {
    	if (linkFiltersType != null) {
    		List<LinkFilter> filters = linkFiltersType.getLinkFilterList();
    		return filteredAddrOptions(requestMsg, filters);
    	}
    	return null;
    }

    /**
     * Filtered addr options.
     * 
     * @param requestMsg the request msg
     * @param filters the filters
     * 
     * @return the map< integer, dhcp option>
     */
    private Map<Integer, DhcpOption> filteredAddrOptions(DhcpMessage requestMsg,
    		List<? extends Filter> filters)
    {
		if ((filters != null) && !filters.isEmpty()) {
            for (Filter filter : filters) {
            	if (msgMatchesFilter(requestMsg, filter)) {
                    log.info("Request matches filter: " + filter.getName());
                	DhcpConfigOptions filterConfigOptions = 
                		new DhcpConfigOptions(filter.getAddrConfigOptions(),
                				requestMsg.getRequestedOptionCodes());
                	if (filterConfigOptions != null) {
                		return filterConfigOptions.getDhcpOptionMap();
                	}
            	}
            }
		}
		return null;
    }
    
    /**
     * Msg matches filter.
     * 
     * @param requestMsg the request msg
     * @param filter the filter
     * 
     * @return true, if successful
     */
    public static boolean msgMatchesFilter(DhcpMessage requestMsg, Filter filter)
    {
        boolean matches = true;     // assume match
    	FilterExpressionsType filterExprs = filter.getFilterExpressions();
    	if (filterExprs != null) {
        	List<FilterExpression> expressions = filterExprs.getFilterExpressionList();
            if (expressions != null) {
		        for (FilterExpression expression : expressions) {
		        	OptionExpression optexpr = expression.getOptionExpression();
		        	// TODO: handle CustomExpression filters
		            DhcpOption option = requestMsg.getDhcpOption(optexpr.getCode());
		            if (option != null) {
		                // found the filter option in the request,
		                // so check if the expression matches
		                if (!evaluateExpression(optexpr, option)) {
		                    // it must match all expressions for the filter
		                    // group (i.e. expressions are ANDed), so if
		                    // just one doesn't match, then we're done
		                    matches = false;
		                    break;
		                }
		            }
		            else {
		                // if the expression option wasn't found in the
		                // request message, then it can't match
		                matches = false;
		                break;
		            }
		        }
            }
    	}
        return matches;
    }
    
    /**
     * Evaluate expression.  Determine if an option matches based on an expression.
     * 
     * @param expression the option expression
     * @param option the option to compare
     * 
     * @return true, if successful
     */
    protected static boolean evaluateExpression(OptionExpression expression, DhcpOption option)
    {
        boolean matches = false;
        if (option instanceof DhcpComparableOption) {
            matches = ((DhcpComparableOption)option).matches(expression);
        }
        else {
            log.error("Configured option expression is not comparable:" +
                      " code=" + expression.getCode());
        }
        return matches;
    }
    
}
