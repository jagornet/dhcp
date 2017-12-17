/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6ConfigOptions.java is part of Jagornet DHCP.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.jagornet.dhcp.core.option.base.DhcpOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6BcmcsAddressesOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6BcmcsDomainNamesOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6DnsServersOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6DomainSearchListOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6GeoconfCivicOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6InfoRefreshTimeOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6LostServerDomainNameOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6NewPosixTimezoneOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6NewTzdbTimezoneOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6NisDomainNameOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6NisPlusDomainNameOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6NisPlusServersOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6NisServersOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6PanaAgentAddressesOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6PreferenceOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6ServerUnicastOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6SipServerAddressesOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6SipServerDomainNamesOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6SntpServersOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6StatusCodeOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6VendorInfoOption;
import com.jagornet.dhcp.server.config.xml.CivicAddressElement;
import com.jagornet.dhcp.server.config.xml.GenericOptionsType;
import com.jagornet.dhcp.server.config.xml.OptionDefType;
import com.jagornet.dhcp.server.config.xml.V6BcmcsAddressesOption;
import com.jagornet.dhcp.server.config.xml.V6BcmcsDomainNamesOption;
import com.jagornet.dhcp.server.config.xml.V6ConfigOptionsType;
import com.jagornet.dhcp.server.config.xml.V6DnsServersOption;
import com.jagornet.dhcp.server.config.xml.V6DomainSearchListOption;
import com.jagornet.dhcp.server.config.xml.V6GeoconfCivicOption;
import com.jagornet.dhcp.server.config.xml.V6InfoRefreshTimeOption;
import com.jagornet.dhcp.server.config.xml.V6LostServerDomainNameOption;
import com.jagornet.dhcp.server.config.xml.V6NewPosixTimezoneOption;
import com.jagornet.dhcp.server.config.xml.V6NewTzdbTimezoneOption;
import com.jagornet.dhcp.server.config.xml.V6NisDomainNameOption;
import com.jagornet.dhcp.server.config.xml.V6NisPlusDomainNameOption;
import com.jagornet.dhcp.server.config.xml.V6NisPlusServersOption;
import com.jagornet.dhcp.server.config.xml.V6NisServersOption;
import com.jagornet.dhcp.server.config.xml.V6PanaAgentAddressesOption;
import com.jagornet.dhcp.server.config.xml.V6PreferenceOption;
import com.jagornet.dhcp.server.config.xml.V6ServerUnicastOption;
import com.jagornet.dhcp.server.config.xml.V6SipServerAddressesOption;
import com.jagornet.dhcp.server.config.xml.V6SipServerDomainNamesOption;
import com.jagornet.dhcp.server.config.xml.V6SntpServersOption;
import com.jagornet.dhcp.server.config.xml.V6StatusCodeOption;
import com.jagornet.dhcp.server.config.xml.V6VendorInfoOption;

/**
 * The Class DhcpV6ConfigOptions.
 */
public class DhcpV6ConfigOptions
{
	/** The config options. */
	protected V6ConfigOptionsType configOptions;
	
	/** The option map. */
	protected Map<Integer, DhcpOption> optionMap = new TreeMap<Integer, DhcpOption>();
	
	
	/**
	 * Instantiates a new dhcp config options.
	 */
	public DhcpV6ConfigOptions()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp config options.
	 * 
	 * @param configOptions the config options
	 */
	public DhcpV6ConfigOptions(V6ConfigOptionsType configOptions)
	{
		if (configOptions != null) 
			this.configOptions = configOptions;
		else
			this.configOptions = new V6ConfigOptionsType();
		
		initDhcpOptionMap();
	}
    
    /**
     * Inits the dhcp option map.
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> initDhcpOptionMap()
    {
		optionMap.clear();
		
		if (configOptions.getV6BcmcsAddressesOption() != null) {
			V6BcmcsAddressesOption bcmcsAddressesOption = configOptions.getV6BcmcsAddressesOption();
			if (bcmcsAddressesOption != null) {
				optionMap.put(bcmcsAddressesOption.getCode(), 
						new DhcpV6BcmcsAddressesOption(bcmcsAddressesOption.getIpAddressList()));
			}
		}
		
		if (configOptions.getV6BcmcsDomainNamesOption() != null) {
			V6BcmcsDomainNamesOption bcmcsDomainNamesOption = configOptions.getV6BcmcsDomainNamesOption();
			if (bcmcsDomainNamesOption != null) {
				optionMap.put(bcmcsDomainNamesOption.getCode(),
						new DhcpV6BcmcsDomainNamesOption(bcmcsDomainNamesOption.getDomainNameList()));
			}
		}
		
		if (configOptions.getV6DnsServersOption() != null) {
			V6DnsServersOption dnsServersOption = configOptions.getV6DnsServersOption();
			if (dnsServersOption != null) {
				optionMap.put(dnsServersOption.getCode(),
						new DhcpV6DnsServersOption(dnsServersOption.getIpAddressList()));
			}
		}
		
		if (configOptions.getV6DomainSearchListOption() != null) {
			V6DomainSearchListOption domainSearchListOption = configOptions.getV6DomainSearchListOption();
			if (domainSearchListOption != null) {
				optionMap.put(domainSearchListOption.getCode(),
						new DhcpV6DomainSearchListOption(domainSearchListOption.getDomainNameList()));
			}
		}
		
		if (configOptions.getV6GeoconfCivicOption() != null) {
			V6GeoconfCivicOption geoconfCivicOption = configOptions.getV6GeoconfCivicOption();
			if (geoconfCivicOption != null) {
				List<CivicAddressElement> civicAddressElements =
						geoconfCivicOption.getCivicAddressElement();
				List<DhcpV6GeoconfCivicOption.CivicAddress> civicAddresses = null;
				if (civicAddressElements != null) {
					civicAddresses = new ArrayList<DhcpV6GeoconfCivicOption.CivicAddress>();
					for (CivicAddressElement cae : civicAddressElements) {
						//civicAddresses.add(new DhcpV6GeoconfCivicOption.CivicAddress(cae.getCaType(), cae.getCaValue()));
						new DhcpV6GeoconfCivicOption.CivicAddress(cae.getCaType(), cae.getCaValue());
					}
				}
				optionMap.put(geoconfCivicOption.getCode(),
						new DhcpV6GeoconfCivicOption(geoconfCivicOption.getWhat(),
								geoconfCivicOption.getCountryCode(), civicAddresses));
			}
		}
		
		if (configOptions.getV6InfoRefreshTimeOption() != null) {
			V6InfoRefreshTimeOption infoRefreshTimeOption = configOptions.getV6InfoRefreshTimeOption();
			if (infoRefreshTimeOption != null) {
				optionMap.put(infoRefreshTimeOption.getCode(),
						new DhcpV6InfoRefreshTimeOption(infoRefreshTimeOption.getUnsignedInt()));
			}
		}
		
		if (configOptions.getV6LostServerDomainNameOption() != null) {
			V6LostServerDomainNameOption lostServerDomainNameOption = 
				configOptions.getV6LostServerDomainNameOption();
			if (lostServerDomainNameOption != null) {
				optionMap.put(lostServerDomainNameOption.getCode(),
						new DhcpV6LostServerDomainNameOption(lostServerDomainNameOption.getDomainName()));
			}
		}
		
		if (configOptions.getV6NewPosixTimezoneOption() != null) {
			V6NewPosixTimezoneOption newPosixTimezoneOption = configOptions.getV6NewPosixTimezoneOption();
			if (newPosixTimezoneOption != null) {
				optionMap.put(newPosixTimezoneOption.getCode(),
						new DhcpV6NewPosixTimezoneOption(newPosixTimezoneOption.getString()));
			}
		}
		
		if (configOptions.getV6NewTzdbTimezoneOption() != null) {
			V6NewTzdbTimezoneOption newTzdbTimezoneOption = configOptions.getV6NewTzdbTimezoneOption();
			if (newTzdbTimezoneOption != null) {
				optionMap.put(newTzdbTimezoneOption.getCode(),
						new DhcpV6NewTzdbTimezoneOption(newTzdbTimezoneOption.getString()));
			}
		}
		
		if (configOptions.getV6NisDomainNameOption() != null) {
			V6NisDomainNameOption nisDomainNameOption = configOptions.getV6NisDomainNameOption();
			if (nisDomainNameOption != null) {
				optionMap.put(nisDomainNameOption.getCode(),
						new DhcpV6NisDomainNameOption(nisDomainNameOption.getDomainName()));
			}
		}
		
		if (configOptions.getV6NisPlusDomainNameOption() != null) {
			V6NisPlusDomainNameOption nisPlusDomainNameOption = configOptions.getV6NisPlusDomainNameOption();
			if (nisPlusDomainNameOption != null) {
				optionMap.put(nisPlusDomainNameOption.getCode(),
						new DhcpV6NisPlusDomainNameOption(nisPlusDomainNameOption.getDomainName()));
			}
		}
		
		if (configOptions.getV6NisPlusServersOption() != null) {
			V6NisPlusServersOption nisPlusServersOption = configOptions.getV6NisPlusServersOption();
			if (nisPlusServersOption != null) {
				optionMap.put(nisPlusServersOption.getCode(),
						new DhcpV6NisPlusServersOption(nisPlusServersOption.getIpAddressList()));
			}
		}
		
		if (configOptions.getV6NisServersOption() != null) {
			V6NisServersOption nisServersOption = configOptions.getV6NisServersOption();
			if (nisServersOption != null) {
				optionMap.put(nisServersOption.getCode(),
						new DhcpV6NisServersOption(nisServersOption.getIpAddressList()));
			}
		}
		
		if (configOptions.getV6PanaAgentAddressesOption() != null) {
			V6PanaAgentAddressesOption panaAgentAddressesOption = configOptions.getV6PanaAgentAddressesOption();
			if (panaAgentAddressesOption != null) {
				optionMap.put(panaAgentAddressesOption.getCode(),
						new DhcpV6PanaAgentAddressesOption(panaAgentAddressesOption.getIpAddressList()));
			}
		}
		
		if (configOptions.getV6PreferenceOption() != null) {
			V6PreferenceOption preferenceOption = configOptions.getV6PreferenceOption();
			if (preferenceOption != null) {
				optionMap.put(preferenceOption.getCode(),
						new DhcpV6PreferenceOption(preferenceOption.getUnsignedByte()));
			}
		}
		
		if (configOptions.getV6ServerUnicastOption() != null) {
			V6ServerUnicastOption serverUnicastOption = configOptions.getV6ServerUnicastOption();
			if (serverUnicastOption != null) {
				optionMap.put(serverUnicastOption.getCode(),
						new DhcpV6ServerUnicastOption(serverUnicastOption.getIpAddress()));
			}
		}
		
		if (configOptions.getV6SipServerAddressesOption() != null) {
			V6SipServerAddressesOption sipServerAddressesOption = configOptions.getV6SipServerAddressesOption();
			if (sipServerAddressesOption != null) {
				optionMap.put(sipServerAddressesOption.getCode(),
						new DhcpV6SipServerAddressesOption(sipServerAddressesOption.getIpAddressList()));
			}
		}
		
		if (configOptions.getV6SipServerDomainNamesOption() != null) {
			V6SipServerDomainNamesOption sipServerDomainNamesOption =
				configOptions.getV6SipServerDomainNamesOption();
			if (sipServerDomainNamesOption != null) {
				optionMap.put(sipServerDomainNamesOption.getCode(),
						new DhcpV6SipServerDomainNamesOption(sipServerDomainNamesOption.getDomainNameList()));
			}
		}
		
		if (configOptions.getV6SntpServersOption() != null) {
			V6SntpServersOption sntpServersOption = configOptions.getV6SntpServersOption();
			if (sntpServersOption != null) {
				optionMap.put(sntpServersOption.getCode(),
						new DhcpV6SntpServersOption(sntpServersOption.getIpAddressList()));
			}
		}
		
		if (configOptions.getV6StatusCodeOption() != null) {
			V6StatusCodeOption statusCodeOption = configOptions.getV6StatusCodeOption();
			if (statusCodeOption != null) {
				optionMap.put(statusCodeOption.getCode(),
						new DhcpV6StatusCodeOption(statusCodeOption.getCode(), 
													statusCodeOption.getMessage()));
			}
		}
		
		if (configOptions.getV6VendorInfoOption() != null) {
			V6VendorInfoOption vendorInfoOption = configOptions.getV6VendorInfoOption();
			if (vendorInfoOption != null) {
	    		long enterpriseNumber = vendorInfoOption.getEnterpriseNumber();
	    		List<DhcpOption> suboptionList = null;
	    		GenericOptionsType genericOptions = vendorInfoOption.getSuboptionList();
	    		if (genericOptions != null) {
	    			suboptionList = new ArrayList<DhcpOption>();
	    			List<OptionDefType> optdefs = genericOptions.getOptionDefs();
	    			if (optdefs != null) {
	    				for (OptionDefType optdef : optdefs) {
							DhcpOption subopt = GenericOptionFactory.getDhcpOption(optdef);
							suboptionList.add(subopt);
						}
	    			}
	    		}
				optionMap.put(vendorInfoOption.getCode(),
						new DhcpV6VendorInfoOption(enterpriseNumber, suboptionList));
			}
		}
		
		if (configOptions.getV6OtherOptions() != null) {
			optionMap.putAll(GenericOptionFactory.genericOptions(configOptions.getV6OtherOptions()));
		}
		
		return optionMap;
    }

	/**
	 * Gets the config options.
	 * 
	 * @return the config options
	 */
	public V6ConfigOptionsType getV6ConfigOptions() {
		return configOptions;
	}

	/**
	 * Sets the config options.
	 * 
	 * @param configOptions the new config options
	 */
	public void setV6ConfigOptions(V6ConfigOptionsType configOptions) {
		this.configOptions = configOptions;
		// reset the option map
		initDhcpOptionMap();
	}
	
	/**
	 * Gets the dhcp option map.
	 * 
	 * @return the dhcp option map
	 */
	public Map<Integer, DhcpOption> getDhcpOptionMap()
	{
		return optionMap;
	}

}
