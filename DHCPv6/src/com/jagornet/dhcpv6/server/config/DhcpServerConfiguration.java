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
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.db.IaManager;
import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpComparableOption;
import com.jagornet.dhcpv6.option.DhcpConfigOptions;
import com.jagornet.dhcpv6.option.OpaqueDataUtil;
import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.server.DhcpV6Server;
import com.jagornet.dhcpv6.server.request.binding.NaAddrBindingManager;
import com.jagornet.dhcpv6.server.request.binding.PrefixBindingManager;
import com.jagornet.dhcpv6.server.request.binding.Range;
import com.jagornet.dhcpv6.server.request.binding.TaAddrBindingManager;
import com.jagornet.dhcpv6.util.Subnet;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.AddressPool;
import com.jagornet.dhcpv6.xml.AddressPoolsType;
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
import com.jagornet.dhcpv6.xml.PrefixPool;
import com.jagornet.dhcpv6.xml.PrefixPoolsType;
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
    
    /** The config filename. */
    public static String configFilename = DhcpV6Server.DEFAULT_CONFIG_FILENAME;

	/** The INSTANCE. */
	private static DhcpServerConfiguration INSTANCE;
	
    /** The XML object representing the configuration. */
    private DhcpV6ServerConfig xmlServerConfig;
    
    /** The link map. */
    private SortedMap<Subnet, DhcpLink> linkMap;
    
    private NaAddrBindingManager naAddrBindingMgr;
    private TaAddrBindingManager taAddrBindingMgr;
    private PrefixBindingManager prefixBindingMgr;
    private IaManager iaMgr;
    
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
     * @throws DhcpServerConfigException the exception
     */
    private DhcpServerConfiguration() throws DhcpServerConfigException, XmlException, IOException
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
     * @throws IOException the exception
     */
    protected void initServerId() throws IOException
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
     * @throws DhcpServerConfigException the exception
     */
    protected void initLinkMap() throws DhcpServerConfigException
    {
    	LinksType linksType = xmlServerConfig.getLinks();
    	if (linksType != null) {
        	List<Link> links = linksType.getLinkList();
            if ((links != null) && !links.isEmpty()) {
                linkMap = new TreeMap<Subnet, DhcpLink>();
                for (Link link : links) {
                	String ifname = link.getInterface();
                	if (ifname != null) {
                		try {
	                		NetworkInterface netIf = NetworkInterface.getByName(ifname);
	                		if (netIf == null) {
	                			throw new DhcpServerConfigException(
	                					"Network interface not found: " + ifname);
	                		}
	                		if (!netIf.supportsMulticast()) {
	                			throw new DhcpServerConfigException(
	                					"Network interface does not support multicast: " + ifname);
	                		}
	                		if (!netIf.getInetAddresses().hasMoreElements()) {
	                			throw new DhcpServerConfigException(
	                					"Network interface has no configured addresses: " + ifname);
	                		}
	                		// set this link's address to the IPv6 link-local address of the interface
	                		// when a packet is received on the link-local address, we use it
	                		// to find the client's Link as configured for the server
	                		link.setAddress(
	                				Util.netIfIPv6LinkLocalAddress(netIf).getHostAddress()
	                				+ "/128");	// subnet of one
                		}
                		catch (SocketException ex) {
                			throw new DhcpServerConfigException(
                					"Invalid network interface: " + ifname, ex);
                		}
                	}
                    String addr = link.getAddress();
                    if (addr != null) {
                        String[] s = addr.split("/");
                        if ((s != null) && (s.length == 2)) {
                            try {
								Subnet subnet = new Subnet(s[0], s[1]);
								linkMap.put(subnet, new DhcpLink(subnet, link));
							} 
                            catch (NumberFormatException ex) {
                            	throw new DhcpServerConfigException(
                            			"Invalid link address" + addr, ex);
							} 
                            catch (UnknownHostException ex) {
                            	throw new DhcpServerConfigException(
                            			"Invalid link address" + addr, ex);
							}
                        }
                        else {
                        	throw new DhcpServerConfigException(
                        			"Unsupported Link address=" + addr +
                        			": expected prefix/prefixlen format");
                        }
                    }
                    else {
                    	throw new DhcpServerConfigException(
                    			"Link must specify an interface or address element");
                    }
                }
        	}
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
    
    public NaAddrBindingManager getNaAddrBindingMgr() {
		return naAddrBindingMgr;
	}

	public void setNaAddrBindingMgr(NaAddrBindingManager naAddrBindingMgr) {
		this.naAddrBindingMgr = naAddrBindingMgr;
	}

	public TaAddrBindingManager getTaAddrBindingMgr() {
		return taAddrBindingMgr;
	}

	public void setTaAddrBindingMgr(TaAddrBindingManager taAddrBindingMgr) {
		this.taAddrBindingMgr = taAddrBindingMgr;
	}

	public PrefixBindingManager getPrefixBindingMgr() {
		return prefixBindingMgr;
	}

	public void setPrefixBindingMgr(PrefixBindingManager prefixBindingMgr) {
		this.prefixBindingMgr = prefixBindingMgr;
	}

	public IaManager getIaMgr() {
		return iaMgr;
	}

	public void setIaMgr(IaManager iaMgr) {
		this.iaMgr = iaMgr;
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
        DhcpLink dhcpLink = null;
        if ((linkMap != null) && !linkMap.isEmpty()) {
//            Subnet s = new Subnet(inetAddr, 128);
//            SortedMap<Subnet, DhcpLink> subMap = linkMap.headMap(s);
//            if ((subMap != null) && !subMap.isEmpty()) {
//                s = subMap.lastKey();
//                if (s.contains(inetAddr)) {
//                    dhcpLink = subMap.get(s);
//                }
//            }
        	for (DhcpLink link : linkMap.values()) {
        		AddressPoolsType addrPoolType = link.getLink().getNaAddrPools();
        		if (addrPoolType != null) {
        			List<AddressPool> addrPools = addrPoolType.getPoolList();
        			if (addrPools != null) {
        				for (AddressPool addrPool : addrPools) {
        					try {
	        					Range range = new Range(addrPool.getRange());
	        					if (range.contains(inetAddr)) {
	        						return link;
	        					}
        					}
        					catch (Exception ex) {
        						log.error("Invalid AddressPool range: " + addrPool.getRange());
        					}
        				}
        			}
        		}
        		addrPoolType = link.getLink().getTaAddrPools();
        		if (addrPoolType != null) {
        			List<AddressPool> addrPools = addrPoolType.getPoolList();
        			if (addrPools != null) {
        				for (AddressPool addrPool : addrPools) {
        					try {
	        					Range range = new Range(addrPool.getRange());
	        					if (range.contains(inetAddr)) {
	        						return link;
	        					}
        					}
        					catch (Exception ex) {
        						log.error("Invalid AddressPool range: " + addrPool.getRange());
        					}
        				}
        			}
        		}
        	}
        }
        return dhcpLink;
    }

    /**
     * Find dhcp link.
     * 
     * @param local the local address
     * @param remote the remote address
     * 
     * @return the dhcp link
     */
    public DhcpLink findDhcpLink(InetAddress local, InetAddress remote)
    {
        DhcpLink link = null;
        if ((linkMap != null) && !linkMap.isEmpty()) {
        	if (local.isLinkLocalAddress()) {
        		// if the local address is link-local, then the request
        		// was received directly from the client on the interface
        		// with that link-local address, which is the linkMap key
        		log.debug("Looking for Link by link local address: " + local);
	            Subnet s = new Subnet(local, 128);
        		link = linkMap.get(s);
        	}
        	else if (!remote.isLinkLocalAddress()) { 
        		// if the remote (client) address is not link-local, then the client
        		// already has an address, so use that address to search the linkMap
        		log.debug("Looking for Link by remote global address: " + remote);
        		link = findLink(remote);
        	}
        	else {
        		// if the local address is not link-local, and the remote
        		// address is link-local, then this message was relayed and
        		// the local address is the client link address
        		log.debug("Looking for Link by address: " + local);
        		link = findLink(local);
        	}
        }
        else {
        	log.error("linkMap is null or empty");
        }
        return link;
    }
    
    private DhcpLink findLink(InetAddress addr)
    {
        Subnet s = new Subnet(addr, 128);
        // find links less than the given address
        SortedMap<Subnet, DhcpLink> subMap = linkMap.headMap(s);
        if ((subMap != null) && !subMap.isEmpty()) {
        	// the last one in the sub map should contain the address
            Subnet k = subMap.lastKey();
            if (k.contains(addr)) {
                return subMap.get(k);
            }
        }
        // find links greater or equal to given address
        subMap = linkMap.tailMap(s);
        if ((subMap != null) && !subMap.isEmpty()) {
        	// the first one in the sub map should contain the address
            Subnet k = subMap.firstKey();
            if (k.contains(addr)) {
                return subMap.get(k);
            }
        }
        return null;
    }
    
    /**
     * Find pool.
     * 
     * @param link the link
     * @param addr the addr
     * 
     * @return the pool
     */
    public static AddressPool findNaAddrPool(Link link, InetAddress addr)
    {
    	AddressPool pool = null;
    	if (link != null) {
			pool = findAddrPool(link.getNaAddrPools(), addr);
			if (pool == null) {
				pool = findAddrPool(link.getNaAddrPools(), addr);
			}
    	}
    	return pool;
    }
    
    public static AddressPool findTaAddrPool(Link link, InetAddress addr)
    {
    	AddressPool pool = null;
    	if (link != null) {
			pool = findAddrPool(link.getTaAddrPools(), addr);
			if (pool == null) {
				pool = findAddrPool(link.getTaAddrPools(), addr);
			}
    	}
    	return pool;
    }

    public static AddressPool findAddrPool(AddressPoolsType poolsType, InetAddress addr)
    {
    	AddressPool pool = null;
		if (poolsType != null) {
			List<AddressPool> pools = poolsType.getPoolList();
			if ((pools != null) && !pools.isEmpty()) {
				for (AddressPool p : pools) {
					try {
						Range r = new Range(p.getRange());
						if (r.contains(addr)) {
							pool = p;
							break;
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
		return pool;
    }
    
    public static PrefixPool findPrefixPool(Link link, InetAddress addr)
    {
    	PrefixPool pool = null;
    	if (link != null) {
			pool = findPrefixPool(link.getPrefixPools(), addr);
			if (pool == null) {
				pool = findPrefixPool(link.getPrefixPools(), addr);
			}
    	}
    	return pool;
    }

    public static PrefixPool findPrefixPool(PrefixPoolsType poolsType, InetAddress addr)
    {
    	PrefixPool pool = null;
		if (poolsType != null) {
			List<PrefixPool> pools = poolsType.getPoolList();
			if ((pools != null) && !pools.isEmpty()) {
				for (PrefixPool p : pools) {
					try {
						Range r = new Range(p.getRange());
						if (r.contains(addr)) {
							pool = p;
							break;
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
		return pool;
    }
    
    /**
     * Load the server configuration from a file.
     * 
     * @param filename the full path and filename for the configuration
     * 
     * @return the loaded DhcpV6ServerConfig
     * 
     * @throws XmlException, IOException
     */
    public static DhcpV6ServerConfig loadConfig(String filename) throws XmlException, IOException
    {
    	DhcpV6ServerConfig config = null;
    	FileInputStream fis = null;
    	try {
	        log.info("Loading server configuration file: " + filename);
	        fis = new FileInputStream(filename);
	        config = DhcpV6ServerConfigDocument.Factory.parse(fis).getDhcpV6ServerConfig();
	        log.info("Server configuration file loaded.");
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
     * @throws IOException the exception
     */
    public static void saveConfig(DhcpV6ServerConfig config, String filename) throws IOException
    {
    	FileOutputStream fos = null;
    	try {
	        log.info("Saving server configuration file: " + filename);
	        fos = new FileOutputStream(filename);
	        DhcpV6ServerConfigDocument doc = DhcpV6ServerConfigDocument.Factory.newInstance();
	        doc.setDhcpV6ServerConfig(config);
	        doc.save(fos, new XmlOptions().setSavePrettyPrint().setSavePrettyPrintIndent(4));
	        log.info("Server configuration file saved.");
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
     * Effective ia na options.
     * 
     * @param requestMsg the request msg
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectiveIaNaOptions(DhcpMessage requestMsg)
    {
    	Map<Integer, DhcpOption> optionMap = new HashMap<Integer, DhcpOption>();
    	DhcpConfigOptions configOptions = 
    		new DhcpConfigOptions(xmlServerConfig.getIaNaConfigOptions(),
    				requestMsg.getRequestedOptionCodes());
    	if (configOptions != null) {
    		optionMap.putAll(configOptions.getDhcpOptionMap());
    	}
    	
    	Map<Integer, DhcpOption> filteredOptions = 
    		filteredIaNaOptions(requestMsg, xmlServerConfig.getFilters());
    	if (filteredOptions != null) {
    		optionMap.putAll(filteredOptions);
    	}
    	return optionMap;
    }

    /**
     * Effective ia na options.
     * 
     * @param requestMsg the request msg
     * @param link the link
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectiveIaNaOptions(DhcpMessage requestMsg, Link link)
    {
    	Map<Integer, DhcpOption> optionMap = effectiveIaNaOptions(requestMsg);
    	if (link != null) {
	    	DhcpConfigOptions configOptions = 
	    		new DhcpConfigOptions(link.getIaNaConfigOptions(),
	    				requestMsg.getRequestedOptionCodes());
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredIaNaOptions(requestMsg, link.getLinkFilters());
	    	if (filteredOptions != null) {
	    		optionMap.putAll(filteredOptions);
	    	}
    	}
    	return optionMap;
    }
    
    /**
     * Effective na addr options.
     * 
     * @param requestMsg the request msg
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectiveNaAddrOptions(DhcpMessage requestMsg)
    {
    	Map<Integer, DhcpOption> optionMap = new HashMap<Integer, DhcpOption>();
    	DhcpConfigOptions configOptions = 
    		new DhcpConfigOptions(xmlServerConfig.getNaAddrConfigOptions(), 
    				requestMsg.getRequestedOptionCodes());
    	if (configOptions != null) {
    		optionMap.putAll(configOptions.getDhcpOptionMap());
    	}
    	
    	Map<Integer, DhcpOption> filteredOptions = 
    		filteredNaAddrOptions(requestMsg, xmlServerConfig.getFilters());
    	if (filteredOptions != null) {
    		optionMap.putAll(filteredOptions);
    	}
    	return optionMap;
    }

    /**
     * Effective na addr options.
     * 
     * @param requestMsg the request msg
     * @param link the link
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectiveNaAddrOptions(DhcpMessage requestMsg, Link link)
    {
    	Map<Integer, DhcpOption> optionMap = effectiveNaAddrOptions(requestMsg);
    	if (link != null) {
	    	DhcpConfigOptions configOptions = 
	    		new DhcpConfigOptions(link.getNaAddrConfigOptions(),
	    				requestMsg.getRequestedOptionCodes());
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredNaAddrOptions(requestMsg, link.getLinkFilters());
	    	if (filteredOptions != null) {
	    		optionMap.putAll(filteredOptions);
	    	}
    	}
    	return optionMap;
    }

    /**
     * Effective na addr options.
     * 
     * @param requestMsg the request msg
     * @param link the link
     * @param pool the pool
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectiveNaAddrOptions(DhcpMessage requestMsg, Link link, 
    		AddressPool pool)
    {
    	Map<Integer, DhcpOption> optionMap = effectiveNaAddrOptions(requestMsg, link);
    	if (pool != null) {
	    	DhcpConfigOptions configOptions = 
	    		new DhcpConfigOptions(pool.getAddrConfigOptions(),
	    				requestMsg.getRequestedOptionCodes());
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredNaAddrOptions(requestMsg, pool.getFilters());
	    	if (filteredOptions != null) {
	    		optionMap.putAll(filteredOptions);
	    	}
    	}
    	return optionMap;
    }
    
    /**
     * Effective ia ta options.
     * 
     * @param requestMsg the request msg
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectiveIaTaOptions(DhcpMessage requestMsg)
    {
    	Map<Integer, DhcpOption> optionMap = new HashMap<Integer, DhcpOption>();
    	DhcpConfigOptions configOptions = 
    		new DhcpConfigOptions(xmlServerConfig.getIaTaConfigOptions(),
    				requestMsg.getRequestedOptionCodes());
    	if (configOptions != null) {
    		optionMap.putAll(configOptions.getDhcpOptionMap());
    	}
    	
    	Map<Integer, DhcpOption> filteredOptions = 
    		filteredIaTaOptions(requestMsg, xmlServerConfig.getFilters());
    	if (filteredOptions != null) {
    		optionMap.putAll(filteredOptions);
    	}
    	return optionMap;
    }

    /**
     * Effective ia ta options.
     * 
     * @param requestMsg the request msg
     * @param link the link
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectiveIaTaOptions(DhcpMessage requestMsg, Link link)
    {
    	Map<Integer, DhcpOption> optionMap = effectiveIaTaOptions(requestMsg);
    	if (link != null) {
	    	DhcpConfigOptions configOptions = 
	    		new DhcpConfigOptions(link.getIaTaConfigOptions(),
	    				requestMsg.getRequestedOptionCodes());
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredIaTaOptions(requestMsg, link.getLinkFilters());
	    	if (filteredOptions != null) {
	    		optionMap.putAll(filteredOptions);
	    	}
    	}
    	return optionMap;
    }
    
    /**
     * Effective ta addr options.
     * 
     * @param requestMsg the request msg
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectiveTaAddrOptions(DhcpMessage requestMsg)
    {
    	Map<Integer, DhcpOption> optionMap = new HashMap<Integer, DhcpOption>();
    	DhcpConfigOptions configOptions = 
    		new DhcpConfigOptions(xmlServerConfig.getTaAddrConfigOptions(), 
    				requestMsg.getRequestedOptionCodes());
    	if (configOptions != null) {
    		optionMap.putAll(configOptions.getDhcpOptionMap());
    	}
    	
    	Map<Integer, DhcpOption> filteredOptions = 
    		filteredNaAddrOptions(requestMsg, xmlServerConfig.getFilters());
    	if (filteredOptions != null) {
    		optionMap.putAll(filteredOptions);
    	}
    	return optionMap;
    }

    /**
     * Effective ta addr options.
     * 
     * @param requestMsg the request msg
     * @param link the link
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectiveTaAddrOptions(DhcpMessage requestMsg, Link link)
    {
    	Map<Integer, DhcpOption> optionMap = effectiveTaAddrOptions(requestMsg);
    	if (link != null) {
	    	DhcpConfigOptions configOptions = 
	    		new DhcpConfigOptions(link.getTaAddrConfigOptions(),
	    				requestMsg.getRequestedOptionCodes());
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredTaAddrOptions(requestMsg, link.getLinkFilters());
	    	if (filteredOptions != null) {
	    		optionMap.putAll(filteredOptions);
	    	}
    	}
    	return optionMap;
    }

    /**
     * Effective ta addr options.
     * 
     * @param requestMsg the request msg
     * @param link the link
     * @param pool the pool
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectiveTaAddrOptions(DhcpMessage requestMsg, Link link, 
    		AddressPool pool)
    {
    	Map<Integer, DhcpOption> optionMap = effectiveNaAddrOptions(requestMsg, link);
    	if (pool != null) {
	    	DhcpConfigOptions configOptions = 
	    		new DhcpConfigOptions(pool.getAddrConfigOptions(),
	    				requestMsg.getRequestedOptionCodes());
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredTaAddrOptions(requestMsg, pool.getFilters());
	    	if (filteredOptions != null) {
	    		optionMap.putAll(filteredOptions);
	    	}
    	}
    	return optionMap;
    }
    
    /**
     * Effective ia pd options.
     * 
     * @param requestMsg the request msg
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectiveIaPdOptions(DhcpMessage requestMsg)
    {
    	Map<Integer, DhcpOption> optionMap = new HashMap<Integer, DhcpOption>();
    	DhcpConfigOptions configOptions = 
    		new DhcpConfigOptions(xmlServerConfig.getIaPdConfigOptions(),
    				requestMsg.getRequestedOptionCodes());
    	if (configOptions != null) {
    		optionMap.putAll(configOptions.getDhcpOptionMap());
    	}
    	
    	Map<Integer, DhcpOption> filteredOptions = 
    		filteredIaPdOptions(requestMsg, xmlServerConfig.getFilters());
    	if (filteredOptions != null) {
    		optionMap.putAll(filteredOptions);
    	}
    	return optionMap;
    }

    /**
     * Effective ia pd options.
     * 
     * @param requestMsg the request msg
     * @param link the link
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectiveIaPdOptions(DhcpMessage requestMsg, Link link)
    {
    	Map<Integer, DhcpOption> optionMap = effectiveIaPdOptions(requestMsg);
    	if (link != null) {
	    	DhcpConfigOptions configOptions = 
	    		new DhcpConfigOptions(link.getIaPdConfigOptions(),
	    				requestMsg.getRequestedOptionCodes());
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredIaPdOptions(requestMsg, link.getLinkFilters());
	    	if (filteredOptions != null) {
	    		optionMap.putAll(filteredOptions);
	    	}
    	}
    	return optionMap;
    }
    
    /**
     * Effective prefix options.
     * 
     * @param requestMsg the request msg
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectivePrefixOptions(DhcpMessage requestMsg)
    {
    	Map<Integer, DhcpOption> optionMap = new HashMap<Integer, DhcpOption>();
    	DhcpConfigOptions configOptions = 
    		new DhcpConfigOptions(xmlServerConfig.getPrefixConfigOptions(), 
    				requestMsg.getRequestedOptionCodes());
    	if (configOptions != null) {
    		optionMap.putAll(configOptions.getDhcpOptionMap());
    	}
    	
    	Map<Integer, DhcpOption> filteredOptions = 
    		filteredPrefixOptions(requestMsg, xmlServerConfig.getFilters());
    	if (filteredOptions != null) {
    		optionMap.putAll(filteredOptions);
    	}
    	return optionMap;
    }

    /**
     * Effective prefix options.
     * 
     * @param requestMsg the request msg
     * @param link the link
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectivePrefixOptions(DhcpMessage requestMsg, Link link)
    {
    	Map<Integer, DhcpOption> optionMap = effectivePrefixOptions(requestMsg);
    	if (link != null) {
	    	DhcpConfigOptions configOptions = 
	    		new DhcpConfigOptions(link.getPrefixConfigOptions(),
	    				requestMsg.getRequestedOptionCodes());
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredPrefixOptions(requestMsg, link.getLinkFilters());
	    	if (filteredOptions != null) {
	    		optionMap.putAll(filteredOptions);
	    	}
    	}
    	return optionMap;
    }

    /**
     * Effective prefix options.
     * 
     * @param requestMsg the request msg
     * @param link the link
     * @param pool the pool
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectivePrefixOptions(DhcpMessage requestMsg, Link link, 
    		PrefixPool pool)
    {
    	Map<Integer, DhcpOption> optionMap = effectivePrefixOptions(requestMsg, link);
    	if (pool != null) {
	    	DhcpConfigOptions configOptions = 
	    		new DhcpConfigOptions(pool.getPrefixConfigOptions(),
	    				requestMsg.getRequestedOptionCodes());
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredPrefixOptions(requestMsg, pool.getFilters());
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
     * Filtered ia na options.
     * 
     * @param requestMsg the request msg
     * @param filtersType the filters type
     * 
     * @return the map< integer, dhcp option>
     */
    protected Map<Integer, DhcpOption> filteredIaNaOptions(DhcpMessage requestMsg,
    		FiltersType filtersType)
    {
    	if (filtersType != null) {
    		List<Filter> filters = filtersType.getFilterList();
    		return filteredIaNaOptions(requestMsg, filters);
    	}
    	return null;
    }

    /**
     * Filtered ia na options.
     * 
     * @param requestMsg the request msg
     * @param linkFiltersType the link filters type
     * 
     * @return the map< integer, dhcp option>
     */
    protected Map<Integer, DhcpOption> filteredIaNaOptions(DhcpMessage requestMsg, 
    		LinkFiltersType linkFiltersType)
    {
    	if (linkFiltersType != null) {
    		List<LinkFilter> filters = linkFiltersType.getLinkFilterList();
    		return filteredIaNaOptions(requestMsg, filters);
    	}
    	return null;
    }

	/**
	 * Filtered ia na options.
	 * 
	 * @param requestMsg the request msg
	 * @param filters the filters
	 * 
	 * @return the map< integer, dhcp option>
	 */
	private Map<Integer, DhcpOption> filteredIaNaOptions(DhcpMessage requestMsg,
			List<? extends Filter> filters)
	{
		if ((filters != null) && !filters.isEmpty()) {
		    for (Filter filter : filters) {
		    	if (msgMatchesFilter(requestMsg, filter)) {
		            log.info("Request matches filter: " + filter.getName());
		        	DhcpConfigOptions filterConfigOptions = 
		        		new DhcpConfigOptions(filter.getIaNaConfigOptions(),
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
     * Filtered na addr options.
     * 
     * @param requestMsg the request msg
     * @param filtersType the filters type
     * 
     * @return the map< integer, dhcp option>
     */
    protected Map<Integer, DhcpOption> filteredNaAddrOptions(DhcpMessage requestMsg, 
    		FiltersType filtersType)
    {
    	if (filtersType != null) {
    		List<Filter> filters = filtersType.getFilterList();
    		return filteredNaAddrOptions(requestMsg, filters);
    	}
    	return null;
    }

    /**
     * Filtered na addr options.
     * 
     * @param requestMsg the request msg
     * @param linkFiltersType the link filters type
     * 
     * @return the map< integer, dhcp option>
     */
    protected Map<Integer, DhcpOption> filteredNaAddrOptions(DhcpMessage requestMsg, 
    		LinkFiltersType linkFiltersType)
    {
    	if (linkFiltersType != null) {
    		List<LinkFilter> filters = linkFiltersType.getLinkFilterList();
    		return filteredNaAddrOptions(requestMsg, filters);
    	}
    	return null;
    }

    /**
     * Filtered na addr options.
     * 
     * @param requestMsg the request msg
     * @param filters the filters
     * 
     * @return the map< integer, dhcp option>
     */
    private Map<Integer, DhcpOption> filteredNaAddrOptions(DhcpMessage requestMsg,
    		List<? extends Filter> filters)
    {
		if ((filters != null) && !filters.isEmpty()) {
            for (Filter filter : filters) {
            	if (msgMatchesFilter(requestMsg, filter)) {
                    log.info("Request matches filter: " + filter.getName());
                	DhcpConfigOptions filterConfigOptions = 
                		new DhcpConfigOptions(filter.getNaAddrConfigOptions(),
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
     * Filtered ia ta options.
     * 
     * @param requestMsg the request msg
     * @param filtersType the filters type
     * 
     * @return the map< integer, dhcp option>
     */
    protected Map<Integer, DhcpOption> filteredIaTaOptions(DhcpMessage requestMsg,
    		FiltersType filtersType)
    {
    	if (filtersType != null) {
    		List<Filter> filters = filtersType.getFilterList();
    		return filteredIaTaOptions(requestMsg, filters);
    	}
    	return null;
    }

    /**
     * Filtered ia ta options.
     * 
     * @param requestMsg the request msg
     * @param linkFiltersType the link filters type
     * 
     * @return the map< integer, dhcp option>
     */
    protected Map<Integer, DhcpOption> filteredIaTaOptions(DhcpMessage requestMsg, 
    		LinkFiltersType linkFiltersType)
    {
    	if (linkFiltersType != null) {
    		List<LinkFilter> filters = linkFiltersType.getLinkFilterList();
    		return filteredIaTaOptions(requestMsg, filters);
    	}
    	return null;
    }

	/**
	 * Filtered ia ta options.
	 * 
	 * @param requestMsg the request msg
	 * @param filters the filters
	 * 
	 * @return the map< integer, dhcp option>
	 */
	private Map<Integer, DhcpOption> filteredIaTaOptions(DhcpMessage requestMsg,
			List<? extends Filter> filters)
	{
		if ((filters != null) && !filters.isEmpty()) {
		    for (Filter filter : filters) {
		    	if (msgMatchesFilter(requestMsg, filter)) {
		            log.info("Request matches filter: " + filter.getName());
		        	DhcpConfigOptions filterConfigOptions = 
		        		new DhcpConfigOptions(filter.getIaTaConfigOptions(),
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
     * Filtered ta addr options.
     * 
     * @param requestMsg the request msg
     * @param filtersType the filters type
     * 
     * @return the map< integer, dhcp option>
     */
    protected Map<Integer, DhcpOption> filteredTaAddrOptions(DhcpMessage requestMsg, 
    		FiltersType filtersType)
    {
    	if (filtersType != null) {
    		List<Filter> filters = filtersType.getFilterList();
    		return filteredTaAddrOptions(requestMsg, filters);
    	}
    	return null;
    }

    /**
     * Filtered ta addr options.
     * 
     * @param requestMsg the request msg
     * @param linkFiltersType the link filters type
     * 
     * @return the map< integer, dhcp option>
     */
    protected Map<Integer, DhcpOption> filteredTaAddrOptions(DhcpMessage requestMsg, 
    		LinkFiltersType linkFiltersType)
    {
    	if (linkFiltersType != null) {
    		List<LinkFilter> filters = linkFiltersType.getLinkFilterList();
    		return filteredTaAddrOptions(requestMsg, filters);
    	}
    	return null;
    }

    /**
     * Filtered ta addr options.
     * 
     * @param requestMsg the request msg
     * @param filters the filters
     * 
     * @return the map< integer, dhcp option>
     */
    private Map<Integer, DhcpOption> filteredTaAddrOptions(DhcpMessage requestMsg,
    		List<? extends Filter> filters)
    {
		if ((filters != null) && !filters.isEmpty()) {
            for (Filter filter : filters) {
            	if (msgMatchesFilter(requestMsg, filter)) {
                    log.info("Request matches filter: " + filter.getName());
                	DhcpConfigOptions filterConfigOptions = 
                		new DhcpConfigOptions(filter.getTaAddrConfigOptions(),
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
     * Filtered ia pd options.
     * 
     * @param requestMsg the request msg
     * @param filtersType the filters type
     * 
     * @return the map< integer, dhcp option>
     */
    protected Map<Integer, DhcpOption> filteredIaPdOptions(DhcpMessage requestMsg,
    		FiltersType filtersType)
    {
    	if (filtersType != null) {
    		List<Filter> filters = filtersType.getFilterList();
    		return filteredIaPdOptions(requestMsg, filters);
    	}
    	return null;
    }

    /**
     * Filtered ia pd options.
     * 
     * @param requestMsg the request msg
     * @param linkFiltersType the link filters type
     * 
     * @return the map< integer, dhcp option>
     */
    protected Map<Integer, DhcpOption> filteredIaPdOptions(DhcpMessage requestMsg, 
    		LinkFiltersType linkFiltersType)
    {
    	if (linkFiltersType != null) {
    		List<LinkFilter> filters = linkFiltersType.getLinkFilterList();
    		return filteredIaPdOptions(requestMsg, filters);
    	}
    	return null;
    }

	/**
	 * Filtered ia pd options.
	 * 
	 * @param requestMsg the request msg
	 * @param filters the filters
	 * 
	 * @return the map< integer, dhcp option>
	 */
	private Map<Integer, DhcpOption> filteredIaPdOptions(DhcpMessage requestMsg,
			List<? extends Filter> filters)
	{
		if ((filters != null) && !filters.isEmpty()) {
		    for (Filter filter : filters) {
		    	if (msgMatchesFilter(requestMsg, filter)) {
		            log.info("Request matches filter: " + filter.getName());
		        	DhcpConfigOptions filterConfigOptions = 
		        		new DhcpConfigOptions(filter.getIaPdConfigOptions(),
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
     * Filtered prefix options.
     * 
     * @param requestMsg the request msg
     * @param filtersType the filters type
     * 
     * @return the map< integer, dhcp option>
     */
    protected Map<Integer, DhcpOption> filteredPrefixOptions(DhcpMessage requestMsg, 
    		FiltersType filtersType)
    {
    	if (filtersType != null) {
    		List<Filter> filters = filtersType.getFilterList();
    		return filteredPrefixOptions(requestMsg, filters);
    	}
    	return null;
    }

    /**
     * Filtered prefix options.
     * 
     * @param requestMsg the request msg
     * @param linkFiltersType the link filters type
     * 
     * @return the map< integer, dhcp option>
     */
    protected Map<Integer, DhcpOption> filteredPrefixOptions(DhcpMessage requestMsg, 
    		LinkFiltersType linkFiltersType)
    {
    	if (linkFiltersType != null) {
    		List<LinkFilter> filters = linkFiltersType.getLinkFilterList();
    		return filteredPrefixOptions(requestMsg, filters);
    	}
    	return null;
    }

    /**
     * Filtered prefix options.
     * 
     * @param requestMsg the request msg
     * @param filters the filters
     * 
     * @return the map< integer, dhcp option>
     */
    private Map<Integer, DhcpOption> filteredPrefixOptions(DhcpMessage requestMsg,
    		List<? extends Filter> filters)
    {
		if ((filters != null) && !filters.isEmpty()) {
            for (Filter filter : filters) {
            	if (msgMatchesFilter(requestMsg, filter)) {
                    log.info("Request matches filter: " + filter.getName());
                	DhcpConfigOptions filterConfigOptions = 
                		new DhcpConfigOptions(filter.getPrefixConfigOptions(),
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
