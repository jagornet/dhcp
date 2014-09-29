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
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.xml.ClientClassExpression;
import com.jagornet.dhcp.xml.DhcpServerConfigDocument;
import com.jagornet.dhcp.xml.DhcpServerConfigDocument.DhcpServerConfig;
import com.jagornet.dhcp.xml.Filter;
import com.jagornet.dhcp.xml.FilterExpression;
import com.jagornet.dhcp.xml.FilterExpressionsType;
import com.jagornet.dhcp.xml.FiltersType;
import com.jagornet.dhcp.xml.Link;
import com.jagornet.dhcp.xml.LinkFilter;
import com.jagornet.dhcp.xml.LinkFiltersType;
import com.jagornet.dhcp.xml.LinksType;
import com.jagornet.dhcp.xml.OpaqueData;
import com.jagornet.dhcp.xml.OptionExpression;
import com.jagornet.dhcp.xml.V4AddressPool;
import com.jagornet.dhcp.xml.V4AddressPoolsType;
import com.jagornet.dhcp.xml.V4ServerIdOption;
import com.jagornet.dhcp.xml.V6AddressPool;
import com.jagornet.dhcp.xml.V6AddressPoolsType;
import com.jagornet.dhcp.xml.V6PrefixPool;
import com.jagornet.dhcp.xml.V6PrefixPoolsType;
import com.jagornet.dhcp.xml.V6ServerIdOption;
import com.jagornet.dhcpv6.db.IaManager;
import com.jagornet.dhcpv6.message.DhcpV6Message;
import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.message.DhcpV4Message;
import com.jagornet.dhcpv6.option.DhcpComparableOption;
import com.jagornet.dhcpv6.option.OpaqueDataUtil;
import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.option.v4.DhcpV4ConfigOptions;
import com.jagornet.dhcpv6.option.v4.DhcpV4VendorClassOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6ConfigOptions;
import com.jagornet.dhcpv6.option.v6.DhcpV6UserClassOption;
import com.jagornet.dhcpv6.option.v6.DhcpV6VendorClassOption;
import com.jagornet.dhcpv6.server.JagornetDhcpServer;
import com.jagornet.dhcpv6.server.request.binding.V6NaAddrBindingManager;
import com.jagornet.dhcpv6.server.request.binding.V6PrefixBindingManager;
import com.jagornet.dhcpv6.server.request.binding.Range;
import com.jagornet.dhcpv6.server.request.binding.V6TaAddrBindingManager;
import com.jagornet.dhcpv6.server.request.binding.V4AddrBindingManager;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.util.Subnet;
import com.jagornet.dhcpv6.util.Util;

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
    public static String configFilename = JagornetDhcpServer.DEFAULT_CONFIG_FILENAME;

	/** The INSTANCE. */
	private static DhcpServerConfiguration INSTANCE;
	
    /** The XML object representing the configuration. */
    private DhcpServerConfig xmlServerConfig;
    
    private DhcpV6ConfigOptions globalMsgConfigOptions;
    private DhcpV6ConfigOptions globalIaNaConfigOptions;
    private DhcpV6ConfigOptions globalNaAddrConfigOptions;
    private DhcpV6ConfigOptions globalIaTaConfigOptions;
    private DhcpV6ConfigOptions globalTaAddrConfigOptions;
    private DhcpV6ConfigOptions globalIaPdConfigOptions;
    private DhcpV6ConfigOptions globalPrefixConfigOptions;
    private DhcpV4ConfigOptions globalV4ConfigOptions;
    
    /** The link map. */
    private SortedMap<Subnet, DhcpLink> linkMap;
    
    private V6NaAddrBindingManager naAddrBindingMgr;
    private V6TaAddrBindingManager taAddrBindingMgr;
    private V6PrefixBindingManager prefixBindingMgr;
    private V4AddrBindingManager v4AddrBindingMgr;
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
	    	initServerIds();
	    	globalMsgConfigOptions = new DhcpV6ConfigOptions(xmlServerConfig.getV6MsgConfigOptions());
	    	globalIaNaConfigOptions = new DhcpV6ConfigOptions(xmlServerConfig.getV6IaNaConfigOptions());
	    	globalNaAddrConfigOptions = new DhcpV6ConfigOptions(xmlServerConfig.getV6NaAddrConfigOptions());
	    	globalIaTaConfigOptions = new DhcpV6ConfigOptions(xmlServerConfig.getV6IaTaConfigOptions());
	    	globalTaAddrConfigOptions = new DhcpV6ConfigOptions(xmlServerConfig.getV6TaAddrConfigOptions());
	    	globalIaPdConfigOptions = new DhcpV6ConfigOptions(xmlServerConfig.getV6IaPdConfigOptions());
	    	globalPrefixConfigOptions = new DhcpV6ConfigOptions(xmlServerConfig.getV6PrefixConfigOptions());
	    	globalV4ConfigOptions = new DhcpV4ConfigOptions(xmlServerConfig.getV4ConfigOptions());
	    	initLinkMap();
    	}
    	else {
    		throw new IllegalStateException("Failed to load configuration file: " + configFilename);
    	}
    }
    
    
    /**
     * Initialize the server ids.
     * 
     * @throws IOException the exception
     */
    protected void initServerIds() throws IOException
    {
    	V6ServerIdOption v6ServerId = xmlServerConfig.getV6ServerIdOption();
    	if (v6ServerId == null) {
    		v6ServerId = V6ServerIdOption.Factory.newInstance();
    	}
    	OpaqueData opaque = v6ServerId.getOpaqueData();
    	if ( ( (opaque == null) ||
    		   ((opaque.getAsciiValue() == null) || (opaque.getAsciiValue().length() <= 0)) &&
    		   ((opaque.getHexValue() == null) || (opaque.getHexValue().length <= 0)) ) ) {
    		OpaqueData duid = OpaqueDataUtil.generateDUID_LLT();
    		if (duid == null) {
    			throw new IllegalStateException("Failed to create ServerID");
    		}
    		v6ServerId.setOpaqueData(duid);
    		xmlServerConfig.setV6ServerIdOption(v6ServerId);
    		saveConfig(xmlServerConfig, configFilename);
    	}
    	
    	V4ServerIdOption v4ServerId = xmlServerConfig.getV4ServerIdOption();
    	if (v4ServerId == null) {
    		v4ServerId = V4ServerIdOption.Factory.newInstance();
    	}
		String ip = v4ServerId.getIpAddress();
		if ((ip == null) || (ip.length() <= 0)) {
			v4ServerId.setIpAddress(InetAddress.getLocalHost().getHostAddress());
			xmlServerConfig.setV4ServerIdOption(v4ServerId);
			saveConfig(xmlServerConfig, configFilename);
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
	            				// if not found by name, see if the name is actually an address
	            				try {
	            					InetAddress ipaddr = InetAddress.getByName(ifname);
	            					netIf = NetworkInterface.getByInetAddress(ipaddr);
	            				}
	            				catch (UnknownHostException ex) {
	            					log.warn("Unknown interface: " + ifname + ": " + ex);
	            				}
	            			}
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
								log.info("Adding subnet=" + subnet.toString() +
										" to link map for link:" +
										" name='" + link.getName() + 
										((link.getInterface() != null) ?  
												"' interface=" + link.getInterface() :
												"' address=" + link.getAddress())); 
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
    public DhcpServerConfig getDhcpServerConfig()
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
    
    public V6NaAddrBindingManager getNaAddrBindingMgr() {
		return naAddrBindingMgr;
	}

	public void setNaAddrBindingMgr(V6NaAddrBindingManager naAddrBindingMgr) {
		this.naAddrBindingMgr = naAddrBindingMgr;
	}

	public V6TaAddrBindingManager getTaAddrBindingMgr() {
		return taAddrBindingMgr;
	}

	public void setTaAddrBindingMgr(V6TaAddrBindingManager taAddrBindingMgr) {
		this.taAddrBindingMgr = taAddrBindingMgr;
	}

	public V6PrefixBindingManager getPrefixBindingMgr() {
		return prefixBindingMgr;
	}

	public void setPrefixBindingMgr(V6PrefixBindingManager prefixBindingMgr) {
		this.prefixBindingMgr = prefixBindingMgr;
	}
    
    public V4AddrBindingManager getV4AddrBindingMgr() {
		return v4AddrBindingMgr;
	}

	public void setV4AddrBindingMgr(V4AddrBindingManager v4AddrBindingMgr) {
		this.v4AddrBindingMgr = v4AddrBindingMgr;
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
     * @param inetAddr an InetAddress (v4/v6) to find a Link for
     * 
     * @return the link
     */
    public DhcpLink findLinkForAddress(InetAddress inetAddr)
    {
    	if (inetAddr instanceof Inet6Address) {
            if ((linkMap != null) && !linkMap.isEmpty()) {
            	for (DhcpLink link : linkMap.values()) {
            		V6AddressPoolsType addrPoolType = link.getLink().getV6NaAddrPools();
            		if (addrPoolType != null) {
            			List<V6AddressPool> addrPools = addrPoolType.getPoolList();
            			if (addrPools != null) {
            				for (V6AddressPool addrPool : addrPools) {
            					try {
    	        					Range range = new Range(addrPool.getRange());
    	        					if (range.contains(inetAddr)) {
    	        						return link;
    	        					}
            					}
            					catch (Exception ex) {
            						log.error("Invalid AddressPool range: " + addrPool.getRange() +
            								": " + ex);
            					}
            				}
            			}
            		}
            		addrPoolType = link.getLink().getV6TaAddrPools();
            		if (addrPoolType != null) {
            			List<V6AddressPool> addrPools = addrPoolType.getPoolList();
            			if (addrPools != null) {
            				for (V6AddressPool addrPool : addrPools) {
            					try {
    	        					Range range = new Range(addrPool.getRange());
    	        					if (range.contains(inetAddr)) {
    	        						return link;
    	        					}
            					}
            					catch (Exception ex) {
            						log.error("Invalid AddressPool range: " + addrPool.getRange() +
            								": " + ex);
            					}
            				}
            			}
            		}
            	}
            }
    	}
    	else {
            if ((linkMap != null) && !linkMap.isEmpty()) {
            	for (DhcpLink link : linkMap.values()) {
            		V4AddressPoolsType addrPoolType = link.getLink().getV4AddrPools();
            		if (addrPoolType != null) {
            			List<V4AddressPool> addrPools = addrPoolType.getPoolList();
            			if (addrPools != null) {
            				for (V4AddressPool addrPool : addrPools) {
            					try {
    	        					Range range = new Range(addrPool.getRange());
    	        					if (range.contains(inetAddr)) {
    	        						return link;
    	        					}
            					}
            					catch (Exception ex) {
            						log.error("Invalid V4AddressPool range: " + addrPool.getRange() +
            								": " + ex);
            					}
            				}
            			}
            		}
            	}
            }
    	}
        return null;
    }

    /**
     * Find dhcp link.
     * 
     * @param local the local v6 address
     * @param remote the remote v6 address
     * 
     * @return the dhcp link
     */
    public DhcpLink findDhcpLink(Inet6Address local, Inet6Address remote)
    {
        DhcpLink link = null;
        if ((linkMap != null) && !linkMap.isEmpty()) {
        	if (local.isLinkLocalAddress()) {
        		// if the local address is link-local, then the request
        		// was received directly from the client on the interface
        		// with that link-local address, which is the linkMap key
        		log.debug("Looking for Link by link local address: " + local.getHostAddress());
	            Subnet s = new Subnet(local, 128);
        		link = linkMap.get(s);
        	}
        	else if (!remote.isLinkLocalAddress()) { 
        		// if the remote (client) address is not link-local, then the client
        		// already has an address, so use that address to search the linkMap
        		log.debug("Looking for Link by remote global address: " + remote.getHostAddress());
        		link = findLink(remote);
        	}
        	else {
        		// if the local address is not link-local, and the remote
        		// address is link-local, then this message was relayed and
        		// the local address is the client link address
        		log.debug("Looking for Link by address: " + local.getHostAddress());
        		link = findLink(local);
        	}
        }
        else {
        	log.error("linkMap is null or empty");
        }
        if (link != null) {
        	log.info("Found configured Link for client request: " + 
        				link.getLink().getName());
        }
        return link;
    }

    /**
     * Find dhcp link.
     * 
     * @param local the local v4 address
     * @param remote the remote v4 address
     * 
     * @return the dhcp link
     */
    public DhcpLink findDhcpLink(Inet4Address local, Inet4Address remote)
    {
        DhcpLink link = null;
        if ((linkMap != null) && !linkMap.isEmpty()) {
        	if (remote.equals(DhcpConstants.ZEROADDR)) {
        		// if the remote address is zero, then the request was received
        		// from a client without an address on the broadcast channel, so
        		// use the local address to search the linkMap
        		log.debug("Looking for Link by local address: " + local.getHostAddress());
        		link = findLink(local);
        	}
        	else {
        		// if the remote address is non-zero, then the request was received
        		// from a client or relay with that address, so use the remote address
        		// to search the linkMap
        		log.debug("Looking for Link by remote address: " + remote.getHostAddress());
        		link = findLink(remote);
        	}
        }
        else {
        	log.error("linkMap is null or empty");
        }
        if (link != null) {
        	log.info("Found configured Link for client request: " + 
        				link.getLink().getName());
        }
        return link;
    }
    
    private DhcpLink findLink(InetAddress addr)
    {
        Subnet s = null;
        if (addr instanceof Inet4Address) {
        	s = new Subnet(addr, 32);
        }
        else {
        	s = new Subnet(addr, 128);
        }
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
     * Find the NA address pool for an address on a link
     * 
     * @param link the link
     * @param addr the addr
     * 
     * @return the pool
     */
    public static V6AddressPool findNaAddrPool(Link link, InetAddress addr)
    {
    	V6AddressPool pool = null;
    	if (link != null) {
			pool = findAddrPool(link.getV6NaAddrPools(), addr);
			if (pool == null) {
				pool = findAddrPool(link.getV6NaAddrPools(), addr);
			}
    	}
    	return pool;
    }
    
    /**
     * Find the TA address pool for an address on a link
     * 
     * @param link the link
     * @param addr the addr
     * 
     * @return the pool
     */
    public static V6AddressPool findTaAddrPool(Link link, InetAddress addr)
    {
    	V6AddressPool pool = null;
    	if (link != null) {
			pool = findAddrPool(link.getV6TaAddrPools(), addr);
			if (pool == null) {
				pool = findAddrPool(link.getV6TaAddrPools(), addr);
			}
    	}
    	return pool;
    }

    /**
     * Find the address pool for an address by type
     * 
     * @param poolsType the pool type
     * @param addr the addr
     * @return the pool
     */
    public static V6AddressPool findAddrPool(V6AddressPoolsType poolsType, InetAddress addr)
    {
    	V6AddressPool pool = null;
		if (poolsType != null) {
			List<V6AddressPool> pools = poolsType.getPoolList();
			if ((pools != null) && !pools.isEmpty()) {
				for (V6AddressPool p : pools) {
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
						log.error("Invalid Pool Range: " + p.getRange() + ": " + ex);
//TODO										throw ex;
					}
				}
			}
		}
		return pool;
    }
    
    /**
     * Find the prefix pool for an address on a link
     * 
     * @param link the link
     * @param addr the addr
     * 
     * @return the pool
     */
    public static V6PrefixPool findPrefixPool(Link link, InetAddress addr)
    {
    	V6PrefixPool pool = null;
    	if (link != null) {
			pool = findPrefixPool(link.getV6PrefixPools(), addr);
			if (pool == null) {
				pool = findPrefixPool(link.getV6PrefixPools(), addr);
			}
    	}
    	return pool;
    }

    /**
     * Find the prefix pool for an address by type
     * 
     * @param poolsType the pool type
     * @param addr the addr
     * @return the pool
     */
    public static V6PrefixPool findPrefixPool(V6PrefixPoolsType poolsType, InetAddress addr)
    {
    	V6PrefixPool pool = null;
		if (poolsType != null) {
			List<V6PrefixPool> pools = poolsType.getPoolList();
			if ((pools != null) && !pools.isEmpty()) {
				for (V6PrefixPool p : pools) {
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
						log.error("Invalid Pool Range: " + p.getRange() + ": " + ex);
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
     * @throws  
     */
    public static DhcpServerConfig loadConfig(String filename) 
    		throws DhcpServerConfigException, XmlException, IOException 
    {
        log.info("Loading server configuration file: " + filename);
    	DhcpServerConfig config = parseConfig(filename);
    	if (config != null) {
        	log.info("Server configuration file loaded.");
    	}
    	else {
    		log.error("No server configuration loaded.");
    	}
    	return config;
    }
    
    public static DhcpServerConfig parseConfig(String filename) 
    		throws DhcpServerConfigException, XmlException, IOException
    {
    	DhcpServerConfig config = null;
    	FileInputStream fis = null;
    	try {
	        fis = new FileInputStream(filename);
	        config = DhcpServerConfigDocument.Factory.parse(fis).getDhcpServerConfig();
	        
	        ArrayList<XmlValidationError> validationErrors = new ArrayList<XmlValidationError>();
	        XmlOptions validationOptions = new XmlOptions();
	        validationOptions.setErrorListener(validationErrors);

	        // During validation, errors are added to the ArrayList
	        boolean isValid = config.validate(validationOptions);
	        if (!isValid) {
	        	StringBuilder sb = new StringBuilder();
	            Iterator<XmlValidationError> iter = validationErrors.iterator();
	            while (iter.hasNext())
	            {
	                sb.append(iter.next());
	                sb.append('\n');
	            }
	            throw new DhcpServerConfigException(sb.toString());
	        }
    	}
    	finally {
    		if (fis != null) {
    			fis.close();
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
    public static void saveConfig(DhcpServerConfig config, String filename) throws IOException
    {
    	FileOutputStream fos = null;
    	try {
	        log.info("Saving server configuration file: " + filename);
	        fos = new FileOutputStream(filename);
	        DhcpServerConfigDocument doc = DhcpServerConfigDocument.Factory.newInstance();
	        doc.setDhcpServerConfig(config);
	        doc.save(fos, new XmlOptions().setSavePrettyPrint().setSavePrettyPrintIndent(4));
	        log.info("Server configuration file saved.");
    	}
    	finally {
    		if (fos != null) {
    			fos.close();
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
    public Map<Integer, DhcpOption> effectiveMsgOptions(DhcpV6Message requestMsg)
    {
    	Map<Integer, DhcpOption> optionMap = new HashMap<Integer, DhcpOption>();
    	if (globalMsgConfigOptions != null) {
    		optionMap.putAll(globalMsgConfigOptions.getDhcpOptionMap());
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
    public Map<Integer, DhcpOption> effectiveMsgOptions(DhcpV6Message requestMsg, DhcpLink dhcpLink)
    {
    	Map<Integer, DhcpOption> optionMap = effectiveMsgOptions(requestMsg);
    	if ((dhcpLink != null) && (dhcpLink.getLink() != null)) {
	    	DhcpV6ConfigOptions configOptions = dhcpLink.getMsgConfigOptions();
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredMsgOptions(requestMsg, dhcpLink.getLink().getLinkFilters());
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
    public Map<Integer, DhcpOption> effectiveIaNaOptions(DhcpV6Message requestMsg)
    {
    	Map<Integer, DhcpOption> optionMap = new HashMap<Integer, DhcpOption>();
    	if (globalIaNaConfigOptions != null) {
    		optionMap.putAll(globalIaNaConfigOptions.getDhcpOptionMap());
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
    public Map<Integer, DhcpOption> effectiveIaNaOptions(DhcpV6Message requestMsg, DhcpLink dhcpLink)
    {
    	Map<Integer, DhcpOption> optionMap = effectiveIaNaOptions(requestMsg);
    	if ((dhcpLink != null) && (dhcpLink.getLink() != null)) {
	    	DhcpV6ConfigOptions configOptions = dhcpLink.getIaNaConfigOptions();
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredIaNaOptions(requestMsg, dhcpLink.getLink().getLinkFilters());
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
    public Map<Integer, DhcpOption> effectiveNaAddrOptions(DhcpV6Message requestMsg)
    {
    	Map<Integer, DhcpOption> optionMap = new HashMap<Integer, DhcpOption>();
    	if (globalNaAddrConfigOptions != null) {
    		optionMap.putAll(globalNaAddrConfigOptions.getDhcpOptionMap());
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
    public Map<Integer, DhcpOption> effectiveNaAddrOptions(DhcpV6Message requestMsg, DhcpLink dhcpLink)
    {
    	Map<Integer, DhcpOption> optionMap = effectiveNaAddrOptions(requestMsg);
    	if ((dhcpLink != null) && (dhcpLink.getLink() != null)) {
	    	DhcpV6ConfigOptions configOptions = dhcpLink.getNaAddrConfigOptions();
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredNaAddrOptions(requestMsg, dhcpLink.getLink().getLinkFilters());
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
    public Map<Integer, DhcpOption> effectiveNaAddrOptions(DhcpV6Message requestMsg, DhcpLink dhcpLink, 
    		DhcpV6OptionConfigObject configObj)
    {
    	Map<Integer, DhcpOption> optionMap = effectiveNaAddrOptions(requestMsg, dhcpLink);
    	if (configObj != null) {
	    	DhcpV6ConfigOptions configOptions = configObj.getDhcpConfigOptions();
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredNaAddrOptions(requestMsg, configObj.getFilters());
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
    public Map<Integer, DhcpOption> effectiveIaTaOptions(DhcpV6Message requestMsg)
    {
    	Map<Integer, DhcpOption> optionMap = new HashMap<Integer, DhcpOption>();
    	if (globalIaTaConfigOptions != null) {
    		optionMap.putAll(globalIaTaConfigOptions.getDhcpOptionMap());
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
    public Map<Integer, DhcpOption> effectiveIaTaOptions(DhcpV6Message requestMsg, DhcpLink dhcpLink)
    {
    	Map<Integer, DhcpOption> optionMap = effectiveIaTaOptions(requestMsg);
    	if ((dhcpLink != null) && (dhcpLink.getLink() != null)) {
	    	DhcpV6ConfigOptions configOptions = dhcpLink.getIaTaConfigOptions();
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredIaTaOptions(requestMsg, dhcpLink.getLink().getLinkFilters());
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
    public Map<Integer, DhcpOption> effectiveTaAddrOptions(DhcpV6Message requestMsg)
    {
    	Map<Integer, DhcpOption> optionMap = new HashMap<Integer, DhcpOption>();
    	if (globalTaAddrConfigOptions != null) {
    		optionMap.putAll(globalTaAddrConfigOptions.getDhcpOptionMap());
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
    public Map<Integer, DhcpOption> effectiveTaAddrOptions(DhcpV6Message requestMsg, DhcpLink dhcpLink)
    {
    	Map<Integer, DhcpOption> optionMap = effectiveTaAddrOptions(requestMsg);
    	if ((dhcpLink != null) && (dhcpLink != null)) {
	    	DhcpV6ConfigOptions configOptions = dhcpLink.getTaAddrConfigOptions();
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredTaAddrOptions(requestMsg, dhcpLink.getLink().getLinkFilters());
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
    public Map<Integer, DhcpOption> effectiveTaAddrOptions(DhcpV6Message requestMsg, DhcpLink dhcpLink, 
    		DhcpV6OptionConfigObject configObj)
    {
    	Map<Integer, DhcpOption> optionMap = effectiveNaAddrOptions(requestMsg, dhcpLink);
    	if (configObj != null)  {
	    	DhcpV6ConfigOptions configOptions = configObj.getDhcpConfigOptions();
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredTaAddrOptions(requestMsg, configObj.getFilters());
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
    public Map<Integer, DhcpOption> effectiveIaPdOptions(DhcpV6Message requestMsg)
    {
    	Map<Integer, DhcpOption> optionMap = new HashMap<Integer, DhcpOption>();
    	if (globalIaPdConfigOptions != null) {
    		optionMap.putAll(globalIaPdConfigOptions.getDhcpOptionMap());
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
    public Map<Integer, DhcpOption> effectiveIaPdOptions(DhcpV6Message requestMsg, DhcpLink dhcpLink)
    {
    	Map<Integer, DhcpOption> optionMap = effectiveIaPdOptions(requestMsg);
    	if ((dhcpLink != null) && (dhcpLink.getLink() != null)) {
	    	DhcpV6ConfigOptions configOptions = dhcpLink.getIaPdConfigOptions();
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredIaPdOptions(requestMsg, dhcpLink.getLink().getLinkFilters());
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
    public Map<Integer, DhcpOption> effectivePrefixOptions(DhcpV6Message requestMsg)
    {
    	Map<Integer, DhcpOption> optionMap = new HashMap<Integer, DhcpOption>();
    	if (globalPrefixConfigOptions != null) {
    		optionMap.putAll(globalPrefixConfigOptions.getDhcpOptionMap());
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
    public Map<Integer, DhcpOption> effectivePrefixOptions(DhcpV6Message requestMsg, DhcpLink dhcpLink)
    {
    	Map<Integer, DhcpOption> optionMap = effectivePrefixOptions(requestMsg);
    	if ((dhcpLink != null) && (dhcpLink != null)) {
	    	DhcpV6ConfigOptions configOptions = dhcpLink.getPrefixConfigOptions();
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredPrefixOptions(requestMsg, dhcpLink.getLink().getLinkFilters());
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
    public Map<Integer, DhcpOption> effectivePrefixOptions(DhcpV6Message requestMsg, DhcpLink dhcpLink, 
    		DhcpV6OptionConfigObject configObj)
    {
    	Map<Integer, DhcpOption> optionMap = effectivePrefixOptions(requestMsg, dhcpLink);
    	if (configObj != null) {
	    	DhcpV6ConfigOptions configOptions = configObj.getDhcpConfigOptions();
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredPrefixOptions(requestMsg, configObj.getFilters());
	    	if (filteredOptions != null) {
	    		optionMap.putAll(filteredOptions);
	    	}
    	}
    	return optionMap;
    }
    
    /**
     * Effective v4 addr options.
     * 
     * @param requestMsg the request msg
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectiveV4AddrOptions(DhcpV4Message requestMsg)
    {
    	Map<Integer, DhcpOption> optionMap = new HashMap<Integer, DhcpOption>();
    	if (globalV4ConfigOptions != null) {
    		optionMap.putAll(globalV4ConfigOptions.getDhcpOptionMap());
    	}
    	
    	Map<Integer, DhcpOption> filteredOptions = 
    		filteredV4Options(requestMsg, xmlServerConfig.getFilters());
    	if (filteredOptions != null) {
    		optionMap.putAll(filteredOptions);
    	}
    	return optionMap;
    }

    /**
     * Effective v4 addr options.
     * 
     * @param requestMsg the request msg
     * @param link the link
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectiveV4AddrOptions(DhcpV4Message requestMsg, DhcpLink dhcpLink)
    {
    	Map<Integer, DhcpOption> optionMap = effectiveV4AddrOptions(requestMsg);
    	if ((dhcpLink != null) && (dhcpLink.getLink() != null)) {
	    	DhcpV4ConfigOptions configOptions = dhcpLink.getV4ConfigOptions();
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredV4Options(requestMsg, dhcpLink.getLink().getLinkFilters());
	    	if (filteredOptions != null) {
	    		optionMap.putAll(filteredOptions);
	    	}
    	}
    	return optionMap;
    }

    /**
     * Effective v4 addr options.
     * 
     * @param requestMsg the request msg
     * @param link the link
     * @param pool the pool
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> effectiveV4AddrOptions(DhcpV4Message requestMsg, DhcpLink dhcpLink, 
    		DhcpV4OptionConfigObject configObj)
    {
    	Map<Integer, DhcpOption> optionMap = effectiveV4AddrOptions(requestMsg, dhcpLink);
    	if (configObj != null) {
	    	DhcpV4ConfigOptions configOptions = configObj.getV4ConfigOptions();
	    	if (configOptions != null) {
	    		optionMap.putAll(configOptions.getDhcpOptionMap());
	    	}
	    	
	    	Map<Integer, DhcpOption> filteredOptions = 
	    		filteredV4Options(requestMsg, configObj.getFilters());
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
    protected Map<Integer, DhcpOption> filteredMsgOptions(DhcpV6Message requestMsg, 
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
    protected Map<Integer, DhcpOption> filteredMsgOptions(DhcpV6Message requestMsg, 
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
    private Map<Integer, DhcpOption> filteredMsgOptions(DhcpV6Message requestMsg,
    		List<? extends Filter> filters)
    {
		if ((filters != null) && !filters.isEmpty()) {
            for (Filter filter : filters) {
            	if (msgMatchesFilter(requestMsg, filter)) {
                    log.info("Request matches filter: " + filter.getName());
                	DhcpV6ConfigOptions filterConfigOptions = 
                		new DhcpV6ConfigOptions(filter.getV6MsgConfigOptions());
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
    protected Map<Integer, DhcpOption> filteredIaNaOptions(DhcpV6Message requestMsg,
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
    protected Map<Integer, DhcpOption> filteredIaNaOptions(DhcpV6Message requestMsg, 
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
	private Map<Integer, DhcpOption> filteredIaNaOptions(DhcpV6Message requestMsg,
			List<? extends Filter> filters)
	{
		if ((filters != null) && !filters.isEmpty()) {
		    for (Filter filter : filters) {
		    	if (msgMatchesFilter(requestMsg, filter)) {
		            log.info("Request matches filter: " + filter.getName());
		        	DhcpV6ConfigOptions filterConfigOptions = 
		        		new DhcpV6ConfigOptions(filter.getV6IaNaConfigOptions());
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
    protected Map<Integer, DhcpOption> filteredNaAddrOptions(DhcpV6Message requestMsg, 
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
    protected Map<Integer, DhcpOption> filteredNaAddrOptions(DhcpV6Message requestMsg, 
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
    private Map<Integer, DhcpOption> filteredNaAddrOptions(DhcpV6Message requestMsg,
    		List<? extends Filter> filters)
    {
		if ((filters != null) && !filters.isEmpty()) {
            for (Filter filter : filters) {
            	if (msgMatchesFilter(requestMsg, filter)) {
                    log.info("Request matches filter: " + filter.getName());
                	DhcpV6ConfigOptions filterConfigOptions = 
                		new DhcpV6ConfigOptions(filter.getV6NaAddrConfigOptions());
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
    protected Map<Integer, DhcpOption> filteredIaTaOptions(DhcpV6Message requestMsg,
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
    protected Map<Integer, DhcpOption> filteredIaTaOptions(DhcpV6Message requestMsg, 
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
	private Map<Integer, DhcpOption> filteredIaTaOptions(DhcpV6Message requestMsg,
			List<? extends Filter> filters)
	{
		if ((filters != null) && !filters.isEmpty()) {
		    for (Filter filter : filters) {
		    	if (msgMatchesFilter(requestMsg, filter)) {
		            log.info("Request matches filter: " + filter.getName());
		        	DhcpV6ConfigOptions filterConfigOptions = 
		        		new DhcpV6ConfigOptions(filter.getV6IaTaConfigOptions());
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
    protected Map<Integer, DhcpOption> filteredTaAddrOptions(DhcpV6Message requestMsg, 
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
    protected Map<Integer, DhcpOption> filteredTaAddrOptions(DhcpV6Message requestMsg, 
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
    private Map<Integer, DhcpOption> filteredTaAddrOptions(DhcpV6Message requestMsg,
    		List<? extends Filter> filters)
    {
		if ((filters != null) && !filters.isEmpty()) {
            for (Filter filter : filters) {
            	if (msgMatchesFilter(requestMsg, filter)) {
                    log.info("Request matches filter: " + filter.getName());
                	DhcpV6ConfigOptions filterConfigOptions = 
                		new DhcpV6ConfigOptions(filter.getV6TaAddrConfigOptions());
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
    protected Map<Integer, DhcpOption> filteredIaPdOptions(DhcpV6Message requestMsg,
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
    protected Map<Integer, DhcpOption> filteredIaPdOptions(DhcpV6Message requestMsg, 
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
	private Map<Integer, DhcpOption> filteredIaPdOptions(DhcpV6Message requestMsg,
			List<? extends Filter> filters)
	{
		if ((filters != null) && !filters.isEmpty()) {
		    for (Filter filter : filters) {
		    	if (msgMatchesFilter(requestMsg, filter)) {
		            log.info("Request matches filter: " + filter.getName());
		        	DhcpV6ConfigOptions filterConfigOptions = 
		        		new DhcpV6ConfigOptions(filter.getV6IaPdConfigOptions());
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
    protected Map<Integer, DhcpOption> filteredPrefixOptions(DhcpV6Message requestMsg, 
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
    protected Map<Integer, DhcpOption> filteredPrefixOptions(DhcpV6Message requestMsg, 
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
    private Map<Integer, DhcpOption> filteredPrefixOptions(DhcpV6Message requestMsg,
    		List<? extends Filter> filters)
    {
		if ((filters != null) && !filters.isEmpty()) {
            for (Filter filter : filters) {
            	if (msgMatchesFilter(requestMsg, filter)) {
                    log.info("Request matches filter: " + filter.getName());
                	DhcpV6ConfigOptions filterConfigOptions = 
                		new DhcpV6ConfigOptions(filter.getV6PrefixConfigOptions());
                	if (filterConfigOptions != null) {
		        		return filterConfigOptions.getDhcpOptionMap();
                	}
            	}
            }
		}
		return null;
    }

    /**
     * Filtered v4 options.
     * 
     * @param requestMsg the request msg
     * @param filtersType the filters type
     * 
     * @return the map< integer, dhcp option>
     */
    protected Map<Integer, DhcpOption> filteredV4Options(DhcpV4Message requestMsg, 
    		FiltersType filtersType)
    {
    	if (filtersType != null) {
    		List<Filter> filters = filtersType.getFilterList();
    		return filteredV4Options(requestMsg, filters);
    	}
    	return null;
    }

    /**
     * Filtered v4 options.
     * 
     * @param requestMsg the request msg
     * @param linkFiltersType the link filters type
     * 
     * @return the map< integer, dhcp option>
     */
    protected Map<Integer, DhcpOption> filteredV4Options(DhcpV4Message requestMsg, 
    		LinkFiltersType linkFiltersType)
    {
    	if (linkFiltersType != null) {
    		List<LinkFilter> filters = linkFiltersType.getLinkFilterList();
    		return filteredV4Options(requestMsg, filters);
    	}
    	return null;
    }

    /**
     * Filtered v4 options.
     * 
     * @param requestMsg the request msg
     * @param filters the filters
     * 
     * @return the map< integer, dhcp option>
     */
    private Map<Integer, DhcpOption> filteredV4Options(DhcpV4Message requestMsg,
    		List<? extends Filter> filters)
    {
		if ((filters != null) && !filters.isEmpty()) {
            for (Filter filter : filters) {
            	if (msgMatchesFilter(requestMsg, filter)) {
                    log.info("Request matches filter: " + filter.getName());
                	DhcpV4ConfigOptions filterConfigOptions = 
                		new DhcpV4ConfigOptions(filter.getV4ConfigOptions());
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
		        	if (expression.getClientClassExpression() != null) {
		        		matches = msgMatchesClientClass(requestMsg, expression.getClientClassExpression());
		        		if (!matches)
		        			break;
		        	}
		        	else if (expression.getOptionExpression() != null) {
		        		OptionExpression optexpr = expression.getOptionExpression();
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
		        	else {
		        		log.warn("Unsupported filter expression: " + expression);
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
        	if (option.isV4() == expression.getV4()) {
        		matches = ((DhcpComparableOption)option).matches(expression);
        	}
        }
        else {
            log.error("Configured option expression is not comparable:" +
                      " code=" + expression.getCode());
        }
        return matches;
    }
    
    protected static boolean msgMatchesClientClass(DhcpMessage requestMsg, 
    		ClientClassExpression ccexpr)
    {
		if (ccexpr.getV6UserClassOption() != null) {
			DhcpV6UserClassOption ucOption = (DhcpV6UserClassOption) 
					requestMsg.getDhcpOption(ccexpr.getV6UserClassOption().getCode());
			if (ucOption != null) {
				DhcpV6UserClassOption exprOption = new DhcpV6UserClassOption(ccexpr.getV6UserClassOption());
				if (!ucOption.matches(exprOption, ccexpr.getOperator())) {
					return false;
				}
			}
			else {
				return false;
			}
		}
		else if (ccexpr.getV6VendorClassOption() != null) {
			DhcpV6VendorClassOption vcOption = (DhcpV6VendorClassOption) 
					requestMsg.getDhcpOption(ccexpr.getV6VendorClassOption().getCode());
			if (vcOption != null) {
				DhcpV6VendorClassOption exprOption = new DhcpV6VendorClassOption(ccexpr.getV6VendorClassOption());
				if (!vcOption.matches(exprOption, ccexpr.getOperator())) {
					return false;
				}
			}
			else {
				return false;
			}
		}
		else if (ccexpr.getV4VendorClassOption() != null) {
			DhcpV4VendorClassOption vcOption = (DhcpV4VendorClassOption) 
					requestMsg.getDhcpOption(ccexpr.getV4VendorClassOption().getCode());
			if (vcOption != null) {
				if (!vcOption.matches(ccexpr.getV4VendorClassOption(), ccexpr.getOperator())) {
					return false;
				}
			}
			else {
				return false;
			}
		}
		else {
			log.warn("Unsupported client class expression: " + ccexpr);
		}
		return true;
    }
}
