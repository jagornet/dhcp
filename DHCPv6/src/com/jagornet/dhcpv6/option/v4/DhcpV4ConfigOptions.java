/*
 * Copyright 2011 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV4ConfigOptions.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.option.v4;

import java.util.Map;
import java.util.TreeMap;

import com.jagornet.dhcpv6.option.DhcpConfigOptions;
import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.xml.V4BootFileNameOption;
import com.jagornet.dhcpv6.xml.V4ConfigOptionsType;
import com.jagornet.dhcpv6.xml.V4DomainNameOption;
import com.jagornet.dhcpv6.xml.V4DomainServersOption;
import com.jagornet.dhcpv6.xml.V4NetbiosNameServersOption;
import com.jagornet.dhcpv6.xml.V4NetbiosNodeTypeOption;
import com.jagornet.dhcpv6.xml.V4RoutersOption;
import com.jagornet.dhcpv6.xml.V4SubnetMaskOption;
import com.jagornet.dhcpv6.xml.V4TftpServerNameOption;
import com.jagornet.dhcpv6.xml.V4TimeOffsetOption;
import com.jagornet.dhcpv6.xml.V4TimeServersOption;
import com.jagornet.dhcpv6.xml.V4VendorSpecificOption;

/**
 * The Class DhcpV4ConfigOptions.
 */
public class DhcpV4ConfigOptions
{
	/** The config options. */
	protected V4ConfigOptionsType configOptions;
	
	/** The option map. */
	protected Map<Integer, DhcpOption> optionMap = new TreeMap<Integer, DhcpOption>();
	
	
	/**
	 * Instantiates a new dhcp config options.
	 */
	public DhcpV4ConfigOptions()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp config options.
	 * 
	 * @param configOptions the config options
	 */
	public DhcpV4ConfigOptions(V4ConfigOptionsType configOptions)
	{
		if (configOptions != null) 
			this.configOptions = configOptions;
		else
			this.configOptions = V4ConfigOptionsType.Factory.newInstance();
		
		initDhcpV4OptionMap();
	}
    
    /**
     * Inits the dhcp option map.
     * 
     * @return the map< integer, dhcp option>
     */
    public Map<Integer, DhcpOption> initDhcpV4OptionMap()
    {
		optionMap.clear();
		
		if (configOptions.isSetV4DomainNameOption()) {
			V4DomainNameOption domainNameOption = configOptions.getV4DomainNameOption();
			if (domainNameOption != null) {
				optionMap.put((int)domainNameOption.getCode(),
						new DhcpV4DomainNameOption(domainNameOption));
			}
		}
		
		if (configOptions.isSetV4DomainServersOption()) {
			V4DomainServersOption domainServersOption = configOptions.getV4DomainServersOption();
			if (domainServersOption != null) {
				optionMap.put((int)domainServersOption.getCode(),
						new DhcpV4DomainServersOption(domainServersOption));
			}
		}
		
		if (configOptions.isSetV4RoutersOption()) {
			V4RoutersOption routersOption = configOptions.getV4RoutersOption();
			if (routersOption != null) {
				optionMap.put((int)routersOption.getCode(),
						new DhcpV4RoutersOption(routersOption));
			}
		}
		
		if (configOptions.isSetV4SubnetMaskOption()) {
			V4SubnetMaskOption subnetMaskOption = configOptions.getV4SubnetMaskOption();
			if (subnetMaskOption != null) {
				optionMap.put((int)subnetMaskOption.getCode(),
						new DhcpV4SubnetMaskOption(subnetMaskOption));
			}
		}
		
		if (configOptions.isSetV4TimeOffsetOption()) {
			V4TimeOffsetOption timeOffsetOption = configOptions.getV4TimeOffsetOption();
			if (timeOffsetOption != null) {
				optionMap.put((int)timeOffsetOption.getCode(),
						new DhcpV4TimeOffsetOption(timeOffsetOption));
			}
		}
		
		if (configOptions.isSetV4TimeServersOption()) {
			V4TimeServersOption timeServersOption = configOptions.getV4TimeServersOption();
			if (timeServersOption != null) {
				optionMap.put((int)timeServersOption.getCode(),
						new DhcpV4TimeServersOption(timeServersOption));
			}
		}
		
		if (configOptions.isSetV4VendorSpecificOption()) {
			V4VendorSpecificOption vendorSpecificOption = configOptions.getV4VendorSpecificOption();
			if (vendorSpecificOption != null) {
				optionMap.put((int)vendorSpecificOption.getCode(),
						new DhcpV4VendorSpecificOption(vendorSpecificOption));
			}
		}
		
		if (configOptions.isSetV4NetbiosNameServersOption()) {
			V4NetbiosNameServersOption netbiosNameServersOption =
				configOptions.getV4NetbiosNameServersOption();
			if (netbiosNameServersOption != null) {
				optionMap.put((int)netbiosNameServersOption.getCode(),
						new DhcpV4NetbiosNameServersOption(netbiosNameServersOption));
			}
		}
		
		if (configOptions.isSetV4NetbiosNodeTypeOption()) {
			V4NetbiosNodeTypeOption netbiosNodeTypeOption =
				configOptions.getV4NetbiosNodeTypeOption();
			if (netbiosNodeTypeOption != null) {
				optionMap.put((int)netbiosNodeTypeOption.getCode(),
						new DhcpV4NetbiosNodeTypeOption(netbiosNodeTypeOption));
			}
		}
		
		if (configOptions.isSetV4TftpServerNameOption()) {
			V4TftpServerNameOption tftpServerNameOption =
				configOptions.getV4TftpServerNameOption();
			if (tftpServerNameOption != null) {
				optionMap.put((int)tftpServerNameOption.getCode(),
						new DhcpV4TftpServerNameOption(tftpServerNameOption));
			}
		}
		
		if (configOptions.isSetV4BootFileNameOption()) {
			V4BootFileNameOption bootFileNameOption =
				configOptions.getV4BootFileNameOption();
			if (bootFileNameOption != null) {
				optionMap.put((int)bootFileNameOption.getCode(),
						new DhcpV4BootFileNameOption(bootFileNameOption));
			}
		}
		
		if (configOptions.isSetOtherOptions()) {
			optionMap.putAll(DhcpConfigOptions.genericOptions(configOptions.getOtherOptions()));
		}
		
		return optionMap;
    }

	/**
	 * Gets the config options.
	 * 
	 * @return the config options
	 */
	public V4ConfigOptionsType getV4ConfigOptions() {
		return configOptions;
	}

	/**
	 * Sets the config options.
	 * 
	 * @param configOptions the new config options
	 */
	public void setV4ConfigOptions(V4ConfigOptionsType configOptions) {
		this.configOptions = configOptions;
		// reset the option map
		initDhcpV4OptionMap();
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
