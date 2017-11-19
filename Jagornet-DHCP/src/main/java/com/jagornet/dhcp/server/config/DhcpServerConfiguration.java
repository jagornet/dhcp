/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpServerConfiguration.java is part of Jagornet DHCP.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.message.DhcpMessage;
import com.jagornet.dhcp.core.message.DhcpV4Message;
import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.option.base.BaseDomainNameListOption;
import com.jagornet.dhcp.core.option.base.BaseDomainNameOption;
import com.jagornet.dhcp.core.option.base.BaseEmptyOption;
import com.jagornet.dhcp.core.option.base.BaseIpAddressListOption;
import com.jagornet.dhcp.core.option.base.BaseIpAddressOption;
import com.jagornet.dhcp.core.option.base.BaseOpaqueData;
import com.jagornet.dhcp.core.option.base.BaseOpaqueDataListOption;
import com.jagornet.dhcp.core.option.base.BaseOpaqueDataOption;
import com.jagornet.dhcp.core.option.base.BaseStringOption;
import com.jagornet.dhcp.core.option.base.BaseUnsignedByteListOption;
import com.jagornet.dhcp.core.option.base.BaseUnsignedByteOption;
import com.jagornet.dhcp.core.option.base.BaseUnsignedIntOption;
import com.jagornet.dhcp.core.option.base.BaseUnsignedShortListOption;
import com.jagornet.dhcp.core.option.base.BaseUnsignedShortOption;
import com.jagornet.dhcp.core.option.base.DhcpOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4ServerIdOption;
import com.jagornet.dhcp.core.option.v4.DhcpV4VendorClassOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6ServerIdOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6UserClassOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6VendorClassOption;
import com.jagornet.dhcp.core.util.DhcpConstants;
import com.jagornet.dhcp.core.util.Subnet;
import com.jagornet.dhcp.core.util.Util;
import com.jagornet.dhcp.server.db.IaManager;
import com.jagornet.dhcp.server.request.binding.Range;
import com.jagornet.dhcp.server.request.binding.V4AddrBindingManager;
import com.jagornet.dhcp.server.request.binding.V6NaAddrBindingManager;
import com.jagornet.dhcp.server.request.binding.V6PrefixBindingManager;
import com.jagornet.dhcp.server.request.binding.V6TaAddrBindingManager;
import com.jagornet.dhcp.xml.ClientClassExpression;
import com.jagornet.dhcp.xml.DhcpServerConfig;
import com.jagornet.dhcp.xml.DomainNameListOptionType;
import com.jagornet.dhcp.xml.DomainNameOptionType;
import com.jagornet.dhcp.xml.Filter;
import com.jagornet.dhcp.xml.FilterExpression;
import com.jagornet.dhcp.xml.FilterExpressionsType;
import com.jagornet.dhcp.xml.FiltersType;
import com.jagornet.dhcp.xml.IpAddressListOptionType;
import com.jagornet.dhcp.xml.IpAddressOptionType;
import com.jagornet.dhcp.xml.Link;
import com.jagornet.dhcp.xml.LinkFilter;
import com.jagornet.dhcp.xml.LinkFiltersType;
import com.jagornet.dhcp.xml.LinksType;
import com.jagornet.dhcp.xml.OpaqueData;
import com.jagornet.dhcp.xml.OpaqueDataListOptionType;
import com.jagornet.dhcp.xml.OpaqueDataOptionType;
import com.jagornet.dhcp.xml.Operator;
import com.jagornet.dhcp.xml.OptionExpression;
import com.jagornet.dhcp.xml.PoliciesType;
import com.jagornet.dhcp.xml.StringOptionType;
import com.jagornet.dhcp.xml.UnsignedByteListOptionType;
import com.jagornet.dhcp.xml.UnsignedByteOptionType;
import com.jagornet.dhcp.xml.UnsignedIntOptionType;
import com.jagornet.dhcp.xml.UnsignedShortListOptionType;
import com.jagornet.dhcp.xml.UnsignedShortOptionType;
import com.jagornet.dhcp.xml.V4AddressPool;
import com.jagornet.dhcp.xml.V4AddressPoolsType;
import com.jagornet.dhcp.xml.V4ServerIdOption;
import com.jagornet.dhcp.xml.V4VendorClassOption;
import com.jagornet.dhcp.xml.V6AddressPool;
import com.jagornet.dhcp.xml.V6AddressPoolsType;
import com.jagornet.dhcp.xml.V6PrefixPool;
import com.jagornet.dhcp.xml.V6PrefixPoolsType;
import com.jagornet.dhcp.xml.V6ServerIdOption;
import com.jagornet.dhcp.xml.V6UserClassOption;
import com.jagornet.dhcp.xml.V6VendorClassOption;

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
    
	private DhcpV6ServerIdOption dhcpV6ServerIdOption;
	private DhcpV4ServerIdOption dhcpV4ServerIdOption;

	private PoliciesType globalPolicies;
	
    private DhcpV6ConfigOptions globalV6MsgConfigOptions;
    private DhcpV6ConfigOptions globalV6IaNaConfigOptions;
    private DhcpV6ConfigOptions globalV6NaAddrConfigOptions;
    private DhcpV6ConfigOptions globalV6IaTaConfigOptions;
    private DhcpV6ConfigOptions globalV6TaAddrConfigOptions;
    private DhcpV6ConfigOptions globalV6IaPdConfigOptions;
    private DhcpV6ConfigOptions globalV6PrefixConfigOptions;
    private DhcpV4ConfigOptions globalV4ConfigOptions;
    
    private FiltersType globalFilters;
    
    private SortedMap<Subnet, DhcpLink> linkMap;
    
    private V6NaAddrBindingManager v6NaAddrBindingMgr;
    private V6TaAddrBindingManager v6TaAddrBindingMgr;
    private V6PrefixBindingManager v6PrefixBindingMgr;
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
    		INSTANCE = new DhcpServerConfiguration();
    	}
    	return INSTANCE;
    }
    
    /**
     * Private constructor suppresses generation of a (public) default constructor.
     * 
     * @throws DhcpServerConfigException the exception
     */
    private DhcpServerConfiguration()
    {
    	// empty constructor
    }
    
    public void init(String configFilename) throws DhcpServerConfigException, JAXBException, IOException
    {
    	DhcpServerConfig xmlServerConfig = loadConfig(configFilename);
    	if (xmlServerConfig != null) {
    		boolean needWrite = false;
        	V6ServerIdOption v6ServerId = xmlServerConfig.getV6ServerIdOption();
        	if (v6ServerId == null) {
        		v6ServerId = generateV6ServerId();
        		xmlServerConfig.setV6ServerIdOption(v6ServerId);
        		needWrite = true;
        	}
        	BaseOpaqueData baseOpaqueData = 
        			OpaqueDataUtil.toBaseOpaqueData(v6ServerId.getOpaqueData());
        	dhcpV6ServerIdOption = new DhcpV6ServerIdOption(baseOpaqueData);
        	
        	V4ServerIdOption v4ServerId = xmlServerConfig.getV4ServerIdOption();
        	if (v4ServerId == null) {
        		v4ServerId = generateV4ServerId();
        		xmlServerConfig.setV4ServerIdOption(v4ServerId);
        		needWrite = true;
        	}
        	dhcpV4ServerIdOption = new DhcpV4ServerIdOption(v4ServerId.getIpAddress());
        	
        	globalPolicies = xmlServerConfig.getPolicies();
	    	globalV6MsgConfigOptions = new DhcpV6ConfigOptions(xmlServerConfig.getV6MsgConfigOptions());
	    	globalV6IaNaConfigOptions = new DhcpV6ConfigOptions(xmlServerConfig.getV6IaNaConfigOptions());
	    	globalV6NaAddrConfigOptions = new DhcpV6ConfigOptions(xmlServerConfig.getV6NaAddrConfigOptions());
	    	globalV6IaTaConfigOptions = new DhcpV6ConfigOptions(xmlServerConfig.getV6IaTaConfigOptions());
	    	globalV6TaAddrConfigOptions = new DhcpV6ConfigOptions(xmlServerConfig.getV6TaAddrConfigOptions());
	    	globalV6IaPdConfigOptions = new DhcpV6ConfigOptions(xmlServerConfig.getV6IaPdConfigOptions());
	    	globalV6PrefixConfigOptions = new DhcpV6ConfigOptions(xmlServerConfig.getV6PrefixConfigOptions());
	    	globalV4ConfigOptions = new DhcpV4ConfigOptions(xmlServerConfig.getV4ConfigOptions());
	    	
	    	globalFilters = xmlServerConfig.getFilters();
	    	
	    	initLinkMap(xmlServerConfig.getLinks());
	    	
	    	if (needWrite) {
	    		saveConfig(xmlServerConfig, configFilename);
	    	}
    	}
    	else {
    		throw new IllegalStateException("Failed to load configuration file: " + configFilename);
    	}
    }
    
    public DhcpV6ServerIdOption getDhcpV6ServerIdOption() {
		return dhcpV6ServerIdOption;
	}

	public void setDhcpV6ServerIdOption(DhcpV6ServerIdOption dhcpV6ServerIdOption) {
		this.dhcpV6ServerIdOption = dhcpV6ServerIdOption;
	}

	public DhcpV4ServerIdOption getDhcpV4ServerIdOption() {
		return dhcpV4ServerIdOption;
	}

	public void setDhcpV4ServerIdOption(DhcpV4ServerIdOption dhcpV4ServerIdOption) {
		this.dhcpV4ServerIdOption = dhcpV4ServerIdOption;
	}

	public PoliciesType getGlobalPolicies() {
		return globalPolicies;
	}

	public void setGlobalPolicies(PoliciesType globalPolicies) {
		this.globalPolicies = globalPolicies;
	}

	public DhcpV6ConfigOptions getGlobalV6MsgConfigOptions() {
		return globalV6MsgConfigOptions;
	}

	public void setGlobalV6MsgConfigOptions(DhcpV6ConfigOptions globalV6MsgConfigOptions) {
		this.globalV6MsgConfigOptions = globalV6MsgConfigOptions;
	}

	public DhcpV6ConfigOptions getGlobalV6IaNaConfigOptions() {
		return globalV6IaNaConfigOptions;
	}

	public void setGlobalV6IaNaConfigOptions(DhcpV6ConfigOptions globalV6IaNaConfigOptions) {
		this.globalV6IaNaConfigOptions = globalV6IaNaConfigOptions;
	}

	public DhcpV6ConfigOptions getGlobalV6NaAddrConfigOptions() {
		return globalV6NaAddrConfigOptions;
	}

	public void setGlobalV6NaAddrConfigOptions(DhcpV6ConfigOptions globalV6NaAddrConfigOptions) {
		this.globalV6NaAddrConfigOptions = globalV6NaAddrConfigOptions;
	}

	public DhcpV6ConfigOptions getGlobalV6IaTaConfigOptions() {
		return globalV6IaTaConfigOptions;
	}

	public void setGlobalV6IaTaConfigOptions(DhcpV6ConfigOptions globalV6IaTaConfigOptions) {
		this.globalV6IaTaConfigOptions = globalV6IaTaConfigOptions;
	}

	public DhcpV6ConfigOptions getGlobalV6TaAddrConfigOptions() {
		return globalV6TaAddrConfigOptions;
	}

	public void setGlobalV6TaAddrConfigOptions(DhcpV6ConfigOptions globalV6TaAddrConfigOptions) {
		this.globalV6TaAddrConfigOptions = globalV6TaAddrConfigOptions;
	}

	public DhcpV6ConfigOptions getGlobalV6IaPdConfigOptions() {
		return globalV6IaPdConfigOptions;
	}

	public void setGlobalV6IaPdConfigOptions(DhcpV6ConfigOptions globalV6IaPdConfigOptions) {
		this.globalV6IaPdConfigOptions = globalV6IaPdConfigOptions;
	}

	public DhcpV6ConfigOptions getGlobalV6PrefixConfigOptions() {
		return globalV6PrefixConfigOptions;
	}

	public void setGlobalV6PrefixConfigOptions(DhcpV6ConfigOptions globalV6PrefixConfigOptions) {
		this.globalV6PrefixConfigOptions = globalV6PrefixConfigOptions;
	}

	public DhcpV4ConfigOptions getGlobalV4ConfigOptions() {
		return globalV4ConfigOptions;
	}

	public void setGlobalV4ConfigOptions(DhcpV4ConfigOptions globalV4ConfigOptions) {
		this.globalV4ConfigOptions = globalV4ConfigOptions;
	}

	public FiltersType getGlobalFilters() {
		return globalFilters;
	}

	public void setGlobalFilters(FiltersType globalFilters) {
		this.globalFilters = globalFilters;
	}

	/**
     * Initialize the server ids.
     * 
     * @return true if server ID(s) were generated
     * 
     * @throws IOException the exception
     */
    protected boolean initServerIds(DhcpServerConfig xmlServerConfig) throws IOException
    {
    	boolean generated = false;
    	V6ServerIdOption v6ServerId = xmlServerConfig.getV6ServerIdOption();
    	if (v6ServerId == null) {
    		v6ServerId = new V6ServerIdOption();
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
    		generated = true;
    	}
    	
    	V4ServerIdOption v4ServerId = xmlServerConfig.getV4ServerIdOption();
    	if (v4ServerId == null) {
    		v4ServerId = new V4ServerIdOption();
    	}
		String ip = v4ServerId.getIpAddress();
		if ((ip == null) || (ip.length() <= 0)) {
			v4ServerId.setIpAddress(InetAddress.getLocalHost().getHostAddress());
			xmlServerConfig.setV4ServerIdOption(v4ServerId);
			generated = true;
		}
		return generated;
    }
    
    public static V6ServerIdOption generateV6ServerId() {
    	V6ServerIdOption v6ServerId = new V6ServerIdOption();
    	OpaqueData opaque = v6ServerId.getOpaqueData();
    	if ( ( (opaque == null) ||
    		   ((opaque.getAsciiValue() == null) || (opaque.getAsciiValue().length() <= 0)) &&
    		   ((opaque.getHexValue() == null) || (opaque.getHexValue().length <= 0)) ) ) {
    		OpaqueData duid = OpaqueDataUtil.generateDUID_LLT();
    		if (duid == null) {
    			throw new IllegalStateException("Failed to create ServerID");
    		}
    		v6ServerId.setOpaqueData(duid);
    	}
    	return v6ServerId;
    }
    
    public static V4ServerIdOption generateV4ServerId() throws IOException {
    	V4ServerIdOption v4ServerId = new V4ServerIdOption();
		String ip = v4ServerId.getIpAddress();
		if ((ip == null) || (ip.length() <= 0)) {
			v4ServerId.setIpAddress(InetAddress.getLocalHost().getHostAddress());
		}
		return v4ServerId;
    }
    
    /**
     * Initialize the link map.
     * 
     * @throws DhcpServerConfigException the exception
     */
    protected void initLinkMap(LinksType linksType) throws DhcpServerConfigException
    {
    	if (linksType != null) {
        	List<Link> links = linksType.getLinks();
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
     * Gets the link map.
     * 
     * @return the link map
     */
    public SortedMap<Subnet, DhcpLink> getLinkMap()
    {
        return linkMap;
    }
    
    public V6NaAddrBindingManager getV6NaAddrBindingMgr() {
		return v6NaAddrBindingMgr;
	}

	public void setV6NaAddrBindingMgr(V6NaAddrBindingManager v6NaAddrBindingMgr) {
		this.v6NaAddrBindingMgr = v6NaAddrBindingMgr;
	}

	public V6TaAddrBindingManager getV6TaAddrBindingMgr() {
		return v6TaAddrBindingMgr;
	}

	public void setV6TaAddrBindingMgr(V6TaAddrBindingManager v6TaAddrBindingMgr) {
		this.v6TaAddrBindingMgr = v6TaAddrBindingMgr;
	}

	public V6PrefixBindingManager getV6PrefixBindingMgr() {
		return v6PrefixBindingMgr;
	}

	public void setV6PrefixBindingMgr(V6PrefixBindingManager v6PrefixBindingMgr) {
		this.v6PrefixBindingMgr = v6PrefixBindingMgr;
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
            			List<V6AddressPool> addrPools = addrPoolType.getPool();
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
            			List<V6AddressPool> addrPools = addrPoolType.getPool();
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
            			List<V4AddressPool> addrPools = addrPoolType.getPool();
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
        	if (remote.equals(DhcpConstants.ZEROADDR_V4)) {
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
			List<V6AddressPool> pools = poolsType.getPool();
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
			List<V6PrefixPool> pools = poolsType.getPool();
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
     * @throws JAXBException, IOException
     * @throws  
     */
    public static DhcpServerConfig loadConfig(String filename) 
    		throws DhcpServerConfigException, JAXBException, IOException 
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
    		throws DhcpServerConfigException, JAXBException, IOException
    {
    	DhcpServerConfig config = null;
    	InputStream inputStream = null;
    	try {
    		File file = new File(filename);
    		if (filename.contains(File.separator) && file.exists()) {
    			// if filename is qualified, assume it is on the file
    			// system and relative to the working directory
    			inputStream = new FileInputStream(filename);
    		}
    		else {
    			// if filename is not qualified, assume it is on the
    			// classpath, as would be the case for unit tests
    			filename = File.separator + filename;
    			inputStream = DhcpServerConfig.class.getResourceAsStream(filename);
    		}
	        JAXBContext jc = JAXBContext.newInstance(DhcpServerConfig.class);
	        Unmarshaller unmarshaller = jc.createUnmarshaller();
	        //TODO: consider VEC or ValidationEventHandler implementation
	        //ValidationEventCollector vec = new ValidationEventCollector();
	        unmarshaller.setEventHandler(new DefaultValidationEventHandler());
	        config = (DhcpServerConfig) unmarshaller.unmarshal(inputStream);
    	}
    	finally {
    		if (inputStream != null) {
    			inputStream.close();
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
	        JAXBContext jc = JAXBContext.newInstance(DhcpServerConfig.class);
	        Marshaller marshaller = jc.createMarshaller();
	        marshaller.marshal(config, fos);
	        log.info("Server configuration file saved.");
    	}
    	catch (JAXBException je) {
    		//TODO: better exception handling here
    		throw new IOException(je);
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
    	if (globalV6MsgConfigOptions != null) {
    		optionMap.putAll(globalV6MsgConfigOptions.getDhcpOptionMap());
    	}
    	Map<Integer, DhcpOption> filteredOptions = 
    		filteredMsgOptions(requestMsg, globalFilters);
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
    	if (globalV6IaNaConfigOptions != null) {
    		optionMap.putAll(globalV6IaNaConfigOptions.getDhcpOptionMap());
    	}
    	
    	Map<Integer, DhcpOption> filteredOptions = 
    		filteredIaNaOptions(requestMsg, globalFilters);
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
    	if (globalV6NaAddrConfigOptions != null) {
    		optionMap.putAll(globalV6NaAddrConfigOptions.getDhcpOptionMap());
    	}
    	
    	Map<Integer, DhcpOption> filteredOptions = 
    		filteredNaAddrOptions(requestMsg, globalFilters);
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
    	if (globalV6IaTaConfigOptions != null) {
    		optionMap.putAll(globalV6IaTaConfigOptions.getDhcpOptionMap());
    	}
    	
    	Map<Integer, DhcpOption> filteredOptions = 
    		filteredIaTaOptions(requestMsg, globalFilters);
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
    	if (globalV6TaAddrConfigOptions != null) {
    		optionMap.putAll(globalV6TaAddrConfigOptions.getDhcpOptionMap());
    	}
    	
    	Map<Integer, DhcpOption> filteredOptions = 
    		filteredNaAddrOptions(requestMsg, globalFilters);
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
    	if (globalV6IaPdConfigOptions != null) {
    		optionMap.putAll(globalV6IaPdConfigOptions.getDhcpOptionMap());
    	}
    	
    	Map<Integer, DhcpOption> filteredOptions = 
    		filteredIaPdOptions(requestMsg, globalFilters);
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
    	if (globalV6PrefixConfigOptions != null) {
    		optionMap.putAll(globalV6PrefixConfigOptions.getDhcpOptionMap());
    	}
    	
    	Map<Integer, DhcpOption> filteredOptions = 
    		filteredPrefixOptions(requestMsg, globalFilters);
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
    		filteredV4Options(requestMsg, globalFilters);
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
    		List<Filter> filters = filtersType.getFilters();
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
    		List<LinkFilter> filters = linkFiltersType.getLinkFilter();
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
    		List<Filter> filters = filtersType.getFilters();
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
    		List<LinkFilter> filters = linkFiltersType.getLinkFilter();
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
    		List<Filter> filters = filtersType.getFilters();
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
    		List<LinkFilter> filters = linkFiltersType.getLinkFilter();
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
    		List<Filter> filters = filtersType.getFilters();
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
    		List<LinkFilter> filters = linkFiltersType.getLinkFilter();
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
    		List<Filter> filters = filtersType.getFilters();
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
    		List<LinkFilter> filters = linkFiltersType.getLinkFilter();
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
    		List<Filter> filters = filtersType.getFilters();
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
    		List<LinkFilter> filters = linkFiltersType.getLinkFilter();
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
    		List<Filter> filters = filtersType.getFilters();
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
    		List<LinkFilter> filters = linkFiltersType.getLinkFilter();
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
    		List<Filter> filters = filtersType.getFilters();
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
    		List<LinkFilter> filters = linkFiltersType.getLinkFilter();
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
        	List<FilterExpression> expressions = filterExprs.getFilterExpression();
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
        if (option.isV4() == expression.isV4()) {
        	if (option.getCode() == expression.getCode()) {
        		if (option instanceof BaseEmptyOption) {
        			return true;
        		}
        		else if (option instanceof BaseDomainNameOption) {
        			String domainName = ((BaseDomainNameOption)option).getDomainName();
        	        DomainNameOptionType exprOption = expression.getDomainNameOption();
        	        if (exprOption != null) {
        	            return compareStringOption(domainName, exprOption.getDomainName(),
        	            		expression.getOperator());
        	        }
        		}
        		else if (option instanceof BaseDomainNameListOption) {
        			List<String> domainNameList = ((BaseDomainNameListOption)option).getDomainNameList();
        	        DomainNameListOptionType exprOption = expression.getDomainNameListOption();
        	        if (exprOption != null) {   	
        	            return compareStringListOption(domainNameList, exprOption.getDomainNameList(),
        	            		expression.getOperator());
        	        }
        		}
        		else if (option instanceof BaseIpAddressOption) {
        			String ipAddress = ((BaseIpAddressOption)option).getIpAddress();
        	        IpAddressOptionType exprOption = expression.getIpAddressOption();
        	        if (exprOption != null) {
        	            return compareStringOption(ipAddress, exprOption.getIpAddress(),
        	            		expression.getOperator());
        	        }
        		}
        		else if (option instanceof BaseIpAddressListOption) {
        			List<String> ipAddressList = ((BaseIpAddressListOption)option).getIpAddressList();
        	        IpAddressListOptionType exprOption = expression.getIpAddressListOption();
        	        if (exprOption != null) {
        	            return compareStringListOption(ipAddressList, exprOption.getIpAddressList(),
        	            		expression.getOperator());
        	        }        			
        		}
        		else if (option instanceof BaseOpaqueDataOption) {
        			com.jagornet.dhcp.core.option.base.BaseOpaqueData opaqueData = 
        					((BaseOpaqueDataOption)option).getOpaqueData();
        	        return OpaqueDataUtil.matches(expression, opaqueData);
        		}
        		else if (option instanceof BaseOpaqueDataListOption) {
        			List<com.jagornet.dhcp.core.option.base.BaseOpaqueData> opaqueDataList =
        					((BaseOpaqueDataListOption)option).getOpaqueDataList();
        			OpaqueDataListOptionType exprOpaqueDataListOption = 
        					expression.getOpaqueDataListOption();
        			if (exprOpaqueDataListOption != null) {
        	        	List<OpaqueData> exprOpaqueDataList = 
        	        			exprOpaqueDataListOption.getOpaqueDataList();
                    	return OpaqueDataUtil.matches(opaqueDataList, exprOpaqueDataList,
                    			expression.getOperator());
        				
        			}
        		}
        		else if (option instanceof BaseStringOption) {
        			String string = ((BaseStringOption)option).getString();
        	        StringOptionType exprOption = expression.getStringOption();
        	        if (exprOption != null) {
        	            return compareStringOption(string, exprOption.getString(),
        	            		expression.getOperator());
        	        }
        		}
        		else if (option instanceof BaseUnsignedByteOption) {
        			short unsignedByte = ((BaseUnsignedByteOption)option).getUnsignedByte();
        	        UnsignedByteOptionType exprOption = expression.getUByteOption();
        	        if (exprOption != null) {
        	        	return compareNumberOption(unsignedByte, exprOption.getUnsignedByte(),
        	        			expression.getOperator());
        	        }
        		}
        		else if (option instanceof BaseUnsignedByteListOption) {
        			List<Short> unsignedByteList =
        					((BaseUnsignedByteListOption)option).getUnsignedByteList();
        	        UnsignedByteListOptionType exprOption = expression.getUByteListOption();
        	        if (exprOption != null) {
        	            return compareNumberListOption(unsignedByteList, 
        	            		exprOption.getUnsignedByteList(), expression.getOperator());
        	        }
        		}
        		else if (option instanceof BaseUnsignedIntOption) {
        			long unsignedInt = ((BaseUnsignedIntOption)option).getUnsignedInt();
        	        UnsignedIntOptionType exprOption = expression.getUIntOption();
        	        if (exprOption != null) {
        	        	return compareNumberOption(unsignedInt, exprOption.getUnsignedInt(),
        	        			expression.getOperator());
        	        }
        	        
        	        // then see if we have an opaque option
        	        OpaqueDataOptionType opaqueOption = expression.getOpaqueDataOption();
        	        if (opaqueOption != null) {
        		        OpaqueData opaque = opaqueOption.getOpaqueData();
        		        if (opaque != null) {
        		            String ascii = opaque.getAsciiValue();
        		            if (ascii != null) {
        		                try {
        		                	// need a long to handle unsigned int
        		                    if (unsignedInt == Long.parseLong(ascii)) {
        		                        return true;
        		                    }
        		                }
        		                catch (NumberFormatException ex) {
        		                	log.error("Invalid unsigned byte ASCII value for OpaqueData: " + ascii, ex);
        		                }
        		            }
        		            else {
        		                byte[] hex = opaque.getHexValue();
        		                if ( (hex != null) && 
        		                     (hex.length >= 1) && (hex.length <= 4) ) {
        		                	long hexLong = Long.valueOf(Util.toHexString(hex), 16);
        		                    if (unsignedInt == hexLong) {
        		                        return true;
        		                    }
        		                }
        		            }
        		        }
        	        }
        		}
        		else if (option instanceof BaseUnsignedShortOption) {
        			int unsignedShort = ((BaseUnsignedShortOption)option).getUnsignedShort();
        	        UnsignedShortOptionType exprOption = expression.getUShortOption();
        	        if (exprOption != null) {
        	        	return compareNumberOption(unsignedShort, exprOption.getUnsignedShort(),
        	        			expression.getOperator());
        	        }
        	        
        	        // then see if we have an opaque option
        	        OpaqueDataOptionType opaqueOption = expression.getOpaqueDataOption();
        	        if (opaqueOption != null) {
        		        OpaqueData opaque = opaqueOption.getOpaqueData();
        		        if (opaque != null) {
        		            String ascii = opaque.getAsciiValue();
        		            if (ascii != null) {
        		                try {
        		                	// need an Integer to handle unsigned short
        		                    if (unsignedShort == Integer.parseInt(ascii)) {
        		                        return true;
        		                    }
        		                }
        		                catch (NumberFormatException ex) { 
        		                	log.error("Invalid unsigned short ASCII value for OpaqueData: " + ascii, ex);
        		                }
        		            }
        		            else {
        		                byte[] hex = opaque.getHexValue();
        		                if ( (hex != null) && 
        		                     (hex.length >= 1) && (hex.length <= 2) ) {
        		                	int hexUnsignedShort = Integer.valueOf(Util.toHexString(hex), 16);
        		                    if (unsignedShort == hexUnsignedShort) {
        		                        return true;
        		                    }
        		                }
        		            }
        		        }
        	        }
        		}
        		else if (option instanceof BaseUnsignedShortListOption) {
        			List<Integer> unsignedShortList = 
        					((BaseUnsignedShortListOption)option).getUnsignedShortList();
        	        UnsignedShortListOptionType exprOption = expression.getUShortListOption();
        	        if (exprOption != null) {
        	            return compareNumberListOption(unsignedShortList, 
        	            		exprOption.getUnsignedShortList(), expression.getOperator());
        	        }
        		}
        		else {
        			log.error("Unable to compare unknown option class: " +
        						option.getClass().getName());
        		}
        	}
        	
        }
        return false;
    }

	private static boolean compareStringOption(String optString, String exprString, Operator op) {
		if (op.equals(Operator.EQUALS)) {
			return optString.equals(exprString);
		}
		else if (op.equals(Operator.STARTS_WITH)) {
			return optString.startsWith(exprString);
		}
		else if (op.equals(Operator.ENDS_WITH)) {
			return optString.endsWith(exprString);
		}
		else if (op.equals(Operator.CONTAINS)) {
			return optString.contains(exprString);
		}
		else if (op.equals(Operator.REG_EXP)) {
			return optString.matches(exprString);
		}
		else {
			log.warn("Unsupported expression operator: " + op);
		}
		return false;
	}

	private static boolean compareStringListOption(List<String> optDomainNameList,
			List<String> exprDomainNameList, Operator op) {
		if (op.equals(Operator.EQUALS)) {
			return optDomainNameList.equals(exprDomainNameList);
		}
		else if (op.equals(Operator.CONTAINS)) {
			return optDomainNameList.containsAll(exprDomainNameList);
		}
		else {
			log.warn("Unsupported expression operator: " + op);
		}
		return false;
	}

	private static boolean compareNumberOption(Number optNumber, Number exprNumber, Operator op) {
		if (op.equals(Operator.EQUALS)) {
			return (optNumber.longValue() == exprNumber.longValue());
		}
		else if (op.equals(Operator.LESS_THAN)) {
			return (optNumber.longValue() < exprNumber.longValue());
		}
		else if (op.equals(Operator.LESS_THAN_OR_EQUAL)) {
			return (optNumber.longValue() <= exprNumber.longValue());
		}
		else if (op.equals(Operator.GREATER_THAN)) {
			return (optNumber.longValue() > exprNumber.longValue());
		}
		else if (op.equals(Operator.GREATER_THAN_OR_EQUAL)) {
			return (optNumber.longValue() >= exprNumber.longValue());
		}
		else {
			log.warn("Unsupported expression operator: " + op);
		}
		return false;
	}

	private static boolean compareNumberListOption(List<? extends Number> optNumberList, 
			List<? extends Number> exprNumberList, Operator op) {
		if (op.equals(Operator.EQUALS)) {
			return optNumberList.equals(exprNumberList);
		}
		else if (op.equals(Operator.CONTAINS)) {
			return optNumberList.containsAll(exprNumberList);
		}
		else {
			log.warn("Unsupported expression operator: " + op);
		}
		return false;
	}
    
    protected static boolean msgMatchesClientClass(DhcpMessage requestMsg, 
    		ClientClassExpression ccexpr)
    {
		if (ccexpr.getV6UserClassOption() != null) {
			V6UserClassOption ccexprOption = ccexpr.getV6UserClassOption();
			DhcpV6UserClassOption ucOption = (DhcpV6UserClassOption) 
					requestMsg.getDhcpOption(ccexpr.getV6UserClassOption().getCode());
			if (ucOption != null) {
				return OpaqueDataUtil.matches(ucOption.getOpaqueDataList(), 
						ccexprOption.getOpaqueDataList(), ccexpr.getOperator());
			}
			else {
				return false;
			}
		}
		else if (ccexpr.getV6VendorClassOption() != null) {
			V6VendorClassOption ccexprOption = ccexpr.getV6VendorClassOption();
			DhcpV6VendorClassOption vcOption = (DhcpV6VendorClassOption) 
					requestMsg.getDhcpOption(ccexpr.getV6VendorClassOption().getCode());
			if (vcOption != null) {
				return OpaqueDataUtil.matches(vcOption.getOpaqueDataList(), 
						ccexprOption.getOpaqueDataList(), ccexpr.getOperator());
			}
			else {
				return false;
			}
		}
		else if (ccexpr.getV4VendorClassOption() != null) {
			V4VendorClassOption ccexprOption = ccexpr.getV4VendorClassOption();
			DhcpV4VendorClassOption vcOption = (DhcpV4VendorClassOption) 
					requestMsg.getDhcpOption(ccexpr.getV4VendorClassOption().getCode());
			if (vcOption != null) {
				com.jagornet.dhcp.core.option.base.BaseOpaqueData baseOpaqueData = 
						vcOption.getOpaqueData();
				if (baseOpaqueData != null) {
					return OpaqueDataUtil.matches(baseOpaqueData, 
							ccexprOption.getOpaqueData(), ccexpr.getOperator());
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


/*
 * 
package com.jagornet.dhcp.server.config.option.base;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.xml.OpaqueData;
import com.jagornet.dhcp.xml.Operator;

public class BaseOpaqueData 
		extends com.jagornet.dhcp.core.option.base.BaseOpaqueData
{
	private static Logger log = LoggerFactory.getLogger(BaseOpaqueData.class);

	public BaseOpaqueData() {
		this(null);
	}
	
	public BaseOpaqueData(OpaqueData opaqueData) {
		super();
		if (opaqueData != null) {
			super.setAscii(opaqueData.getAsciiValue());
			super.setHex(opaqueData.getHexValue());
		}
	}

	// for expression matching
    public boolean matches(OpaqueData that, Operator op)
    {
        if (that != null) {
            String expAscii = that.getAsciiValue();
            String myAscii = getAscii();
            if ( (expAscii != null) && (myAscii != null) ) {
                if (op.equals(Operator.EQUALS)) {
                    return myAscii.equalsIgnoreCase(expAscii);
                }
                else if (op.equals(Operator.STARTS_WITH)) {
                    return myAscii.startsWith(expAscii);
                }
                else if (op.equals(Operator.CONTAINS)) {
                    return myAscii.contains(expAscii);
                }
                else if (op.equals(Operator.ENDS_WITH)) {
                    return myAscii.endsWith(expAscii);
                }
                else if (op.equals(Operator.REG_EXP)) {
                    return myAscii.matches(expAscii);
                }
                else {
                    log.error("Unsupported expression operator: " + op);
                    return false;
                }
            }
            else if ( (expAscii == null) && (myAscii == null) ) {
                byte[] expHex = that.getHexValue();
                byte[] myHex = getHex();
                if ( (expHex != null) && (myHex != null) ) {
                    if (op.equals(Operator.EQUALS)) {
                    	return Arrays.equals(myHex, expHex);
                    }
                    else if (op.equals(Operator.STARTS_WITH)) {
                        if (myHex.length >= expHex.length) {
                            for (int i=0; i<expHex.length; i++) {
                                if (myHex[i] != expHex[i]) {
                                    return false;
                                }
                            }
                            return true;    // if we get here, it matches
                        }
                        else {
                            return false;   // exp length too long
                        }
                    }
                    else if (op.equals(Operator.CONTAINS)) {
                        if (myHex.length >= expHex.length) {
                            int j=0;
                            for (int i=0; i<myHex.length; i++) {
                                if (myHex[i] == expHex[j]) {
                                    // found a potential match
                                    j++;
                                    boolean matches = true;
                                    for (int ii=i+1; ii<myHex.length; ii++) {
                                        if (myHex[ii] != expHex[j++]) {
                                            matches = false;
                                            break;
                                        }
                                    }
                                    if (matches) {
                                        return true;
                                    }
                                    j=0;    // reset to start of exp
                                }
                            }
                            return false;    // if we get here, it didn't match
                        }
                        else {
                            return false;   // exp length too long
                        }
                    }
                    else if (op.equals(Operator.ENDS_WITH)) {
                        if (myHex.length >= expHex.length) {
                            for (int i=myHex.length-1; 
                                 i>=myHex.length-expHex.length; 
                                 i--) {
                                if (myHex[i] != expHex[i]) {
                                    return false;
                                }
                            }
                            return true;    // if we get here, it matches
                        }
                        else {
                            return false;   // exp length too long
                        }
                    }
                    else if (op.equals(Operator.REG_EXP)) {
                        log.error("Regular expression operator not valid for hex opaque opaqueData");
                        return false;
                    }
                    else {
                        log.error("Unsupported expression operator: " + op);
                        return false;
                    }
                }
            }
        }
        return false;
    }
}


*/