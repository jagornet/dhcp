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
package com.jagornet.dhcp.option.v6;

import java.util.Map;
import java.util.TreeMap;

import com.jagornet.dhcp.option.base.DhcpOption;
import com.jagornet.dhcp.option.generic.GenericOptionFactory;
import com.jagornet.dhcp.xml.V6BcmcsAddressesOption;
import com.jagornet.dhcp.xml.V6BcmcsDomainNamesOption;
import com.jagornet.dhcp.xml.V6ConfigOptionsType;
import com.jagornet.dhcp.xml.V6DnsServersOption;
import com.jagornet.dhcp.xml.V6DomainSearchListOption;
import com.jagornet.dhcp.xml.V6GeoconfCivicOption;
import com.jagornet.dhcp.xml.V6InfoRefreshTimeOption;
import com.jagornet.dhcp.xml.V6LostServerDomainNameOption;
import com.jagornet.dhcp.xml.V6NewPosixTimezoneOption;
import com.jagornet.dhcp.xml.V6NewTzdbTimezoneOption;
import com.jagornet.dhcp.xml.V6NisDomainNameOption;
import com.jagornet.dhcp.xml.V6NisPlusDomainNameOption;
import com.jagornet.dhcp.xml.V6NisPlusServersOption;
import com.jagornet.dhcp.xml.V6NisServersOption;
import com.jagornet.dhcp.xml.V6PanaAgentAddressesOption;
import com.jagornet.dhcp.xml.V6PreferenceOption;
import com.jagornet.dhcp.xml.V6ServerUnicastOption;
import com.jagornet.dhcp.xml.V6SipServerAddressesOption;
import com.jagornet.dhcp.xml.V6SipServerDomainNamesOption;
import com.jagornet.dhcp.xml.V6SntpServersOption;
import com.jagornet.dhcp.xml.V6StatusCodeOption;
import com.jagornet.dhcp.xml.V6VendorInfoOption;

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
			this.configOptions = V6ConfigOptionsType.Factory.newInstance();
		
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
		
		if (configOptions.isSetV6BcmcsAddressesOption()) {
			V6BcmcsAddressesOption bcmcsAddressesOption = configOptions.getV6BcmcsAddressesOption();
			if (bcmcsAddressesOption != null) {
				optionMap.put(bcmcsAddressesOption.getCode(), 
						new DhcpV6BcmcsAddressesOption(bcmcsAddressesOption));
			}
		}
		
		if (configOptions.isSetV6BcmcsDomainNamesOption()) {
			V6BcmcsDomainNamesOption bcmcsDomainNamesOption = configOptions.getV6BcmcsDomainNamesOption();
			if (bcmcsDomainNamesOption != null) {
				optionMap.put(bcmcsDomainNamesOption.getCode(),
						new DhcpV6BcmcsDomainNamesOption(bcmcsDomainNamesOption));
			}
		}
		
		if (configOptions.isSetV6DnsServersOption()) {
			V6DnsServersOption dnsServersOption = configOptions.getV6DnsServersOption();
			if (dnsServersOption != null) {
				optionMap.put(dnsServersOption.getCode(),
						new DhcpV6DnsServersOption(dnsServersOption));
			}
		}
		
		if (configOptions.isSetV6DomainSearchListOption()) {
			V6DomainSearchListOption domainSearchListOption = configOptions.getV6DomainSearchListOption();
			if (domainSearchListOption != null) {
				optionMap.put(domainSearchListOption.getCode(),
						new DhcpV6DomainSearchListOption(domainSearchListOption));
			}
		}
		
		if (configOptions.isSetV6GeoconfCivicOption()) {
			V6GeoconfCivicOption geoconfCivicOption = configOptions.getV6GeoconfCivicOption();
			if (geoconfCivicOption != null) {
				optionMap.put(geoconfCivicOption.getCode(),
						new DhcpV6GeoconfCivicOption(geoconfCivicOption));
			}
		}
		
		if (configOptions.isSetV6InfoRefreshTimeOption()) {
			V6InfoRefreshTimeOption infoRefreshTimeOption = configOptions.getV6InfoRefreshTimeOption();
			if (infoRefreshTimeOption != null) {
				optionMap.put(infoRefreshTimeOption.getCode(),
						new DhcpV6InfoRefreshTimeOption(infoRefreshTimeOption));
			}
		}
		
		if (configOptions.isSetV6LostServerDomainNameOption()) {
			V6LostServerDomainNameOption lostServerDomainNameOption = 
				configOptions.getV6LostServerDomainNameOption();
			if (lostServerDomainNameOption != null) {
				optionMap.put(lostServerDomainNameOption.getCode(),
						new DhcpV6LostServerDomainNameOption(lostServerDomainNameOption));
			}
		}
		
		if (configOptions.isSetV6NewPosixTimezoneOption()) {
			V6NewPosixTimezoneOption newPosixTimezoneOption = configOptions.getV6NewPosixTimezoneOption();
			if (newPosixTimezoneOption != null) {
				optionMap.put(newPosixTimezoneOption.getCode(),
						new DhcpV6NewPosixTimezoneOption());
			}
		}
		
		if (configOptions.isSetV6NewTzdbTimezoneOption()) {
			V6NewTzdbTimezoneOption newTzdbTimezoneOption = configOptions.getV6NewTzdbTimezoneOption();
			if (newTzdbTimezoneOption != null) {
				optionMap.put(newTzdbTimezoneOption.getCode(),
						new DhcpV6NewTzdbTimezoneOption());
			}
		}
		
		if (configOptions.isSetV6NisDomainNameOption()) {
			V6NisDomainNameOption nisDomainNameOption = configOptions.getV6NisDomainNameOption();
			if (nisDomainNameOption != null) {
				optionMap.put(nisDomainNameOption.getCode(),
						new DhcpV6NisDomainNameOption(nisDomainNameOption));
			}
		}
		
		if (configOptions.isSetV6NisPlusDomainNameOption()) {
			V6NisPlusDomainNameOption nisPlusDomainNameOption = configOptions.getV6NisPlusDomainNameOption();
			if (nisPlusDomainNameOption != null) {
				optionMap.put(nisPlusDomainNameOption.getCode(),
						new DhcpV6NisPlusDomainNameOption(nisPlusDomainNameOption));
			}
		}
		
		if (configOptions.isSetV6NisPlusServersOption()) {
			V6NisPlusServersOption nisPlusServersOption = configOptions.getV6NisPlusServersOption();
			if (nisPlusServersOption != null) {
				optionMap.put(nisPlusServersOption.getCode(),
						new DhcpV6NisPlusServersOption(nisPlusServersOption));
			}
		}
		
		if (configOptions.isSetV6NisServersOption()) {
			V6NisServersOption nisServersOption = configOptions.getV6NisServersOption();
			if (nisServersOption != null) {
				optionMap.put(nisServersOption.getCode(),
						new DhcpV6NisServersOption(nisServersOption));
			}
		}
		
		if (configOptions.isSetV6PanaAgentAddressesOption()) {
			V6PanaAgentAddressesOption panaAgentAddressesOption = configOptions.getV6PanaAgentAddressesOption();
			if (panaAgentAddressesOption != null) {
				optionMap.put(panaAgentAddressesOption.getCode(),
						new DhcpV6PanaAgentAddressesOption(panaAgentAddressesOption));
			}
		}
		
		if (configOptions.isSetV6PreferenceOption()) {
			V6PreferenceOption preferenceOption = configOptions.getV6PreferenceOption();
			if (preferenceOption != null) {
				optionMap.put(preferenceOption.getCode(),
						new DhcpV6PreferenceOption(preferenceOption));
			}
		}
		
		if (configOptions.isSetV6ServerUnicastOption()) {
			V6ServerUnicastOption serverUnicastOption = configOptions.getV6ServerUnicastOption();
			if (serverUnicastOption != null) {
				optionMap.put(serverUnicastOption.getCode(),
						new DhcpV6ServerUnicastOption(serverUnicastOption));
			}
		}
		
		if (configOptions.isSetV6SipServerAddressesOption()) {
			V6SipServerAddressesOption sipServerAddressesOption = configOptions.getV6SipServerAddressesOption();
			if (sipServerAddressesOption != null) {
				optionMap.put(sipServerAddressesOption.getCode(),
						new DhcpV6SipServerAddressesOption(sipServerAddressesOption));
			}
		}
		
		if (configOptions.isSetV6SipServerDomainNamesOption()) {
			V6SipServerDomainNamesOption sipServerDomainNamesOption =
				configOptions.getV6SipServerDomainNamesOption();
			if (sipServerDomainNamesOption != null) {
				optionMap.put(sipServerDomainNamesOption.getCode(),
						new DhcpV6SipServerDomainNamesOption(sipServerDomainNamesOption));
			}
		}
		
		if (configOptions.isSetV6SntpServersOption()) {
			V6SntpServersOption sntpServersOption = configOptions.getV6SntpServersOption();
			if (sntpServersOption != null) {
				optionMap.put(sntpServersOption.getCode(),
						new DhcpV6SntpServersOption(sntpServersOption));
			}
		}
		
		if (configOptions.isSetV6StatusCodeOption()) {
			V6StatusCodeOption statusCodeOption = configOptions.getV6StatusCodeOption();
			if (statusCodeOption != null) {
				optionMap.put(statusCodeOption.getCode(),
						new DhcpV6StatusCodeOption(statusCodeOption));
			}
		}
		
		if (configOptions.isSetV6VendorInfoOption()) {
			V6VendorInfoOption vendorInfoOption = configOptions.getV6VendorInfoOption();
			if (vendorInfoOption != null) {
				optionMap.put(vendorInfoOption.getCode(),
						new DhcpV6VendorInfoOption(vendorInfoOption));
			}
		}
		
		if (configOptions.isSetV6OtherOptions()) {
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
