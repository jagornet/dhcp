/*
 * Copyright 2011 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpConfigOptions.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.option;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.option.generic.GenericDomainNameListOption;
import com.jagornet.dhcpv6.option.generic.GenericDomainNameOption;
import com.jagornet.dhcpv6.option.generic.GenericIpAddressListOption;
import com.jagornet.dhcpv6.option.generic.GenericIpAddressOption;
import com.jagornet.dhcpv6.option.generic.GenericOpaqueDataListOption;
import com.jagornet.dhcpv6.option.generic.GenericOpaqueDataOption;
import com.jagornet.dhcpv6.option.generic.GenericStringOption;
import com.jagornet.dhcpv6.option.generic.GenericUnsignedByteOption;
import com.jagornet.dhcpv6.option.generic.GenericUnsignedIntOption;
import com.jagornet.dhcpv6.option.generic.GenericUnsignedShortListOption;
import com.jagornet.dhcpv6.option.generic.GenericUnsignedShortOption;
import com.jagornet.dhcpv6.xml.BcmcsAddressesOption;
import com.jagornet.dhcpv6.xml.BcmcsDomainNamesOption;
import com.jagornet.dhcpv6.xml.ConfigOptionsType;
import com.jagornet.dhcpv6.xml.DnsServersOption;
import com.jagornet.dhcpv6.xml.DomainNameListOptionType;
import com.jagornet.dhcpv6.xml.DomainNameOptionType;
import com.jagornet.dhcpv6.xml.DomainSearchListOption;
import com.jagornet.dhcpv6.xml.GenericOptionsType;
import com.jagornet.dhcpv6.xml.GeoconfCivicOption;
import com.jagornet.dhcpv6.xml.InfoRefreshTimeOption;
import com.jagornet.dhcpv6.xml.IpAddressListOptionType;
import com.jagornet.dhcpv6.xml.IpAddressOptionType;
import com.jagornet.dhcpv6.xml.LostServerDomainNameOption;
import com.jagornet.dhcpv6.xml.NewPosixTimezoneOption;
import com.jagornet.dhcpv6.xml.NewTzdbTimezoneOption;
import com.jagornet.dhcpv6.xml.NisDomainNameOption;
import com.jagornet.dhcpv6.xml.NisPlusDomainNameOption;
import com.jagornet.dhcpv6.xml.NisPlusServersOption;
import com.jagornet.dhcpv6.xml.NisServersOption;
import com.jagornet.dhcpv6.xml.OpaqueDataListOptionType;
import com.jagornet.dhcpv6.xml.OpaqueDataOptionType;
import com.jagornet.dhcpv6.xml.OptionDefType;
import com.jagornet.dhcpv6.xml.PanaAgentAddressesOption;
import com.jagornet.dhcpv6.xml.PreferenceOption;
import com.jagornet.dhcpv6.xml.ServerUnicastOption;
import com.jagornet.dhcpv6.xml.SipServerAddressesOption;
import com.jagornet.dhcpv6.xml.SipServerDomainNamesOption;
import com.jagornet.dhcpv6.xml.SntpServersOption;
import com.jagornet.dhcpv6.xml.StatusCodeOption;
import com.jagornet.dhcpv6.xml.StringOptionType;
import com.jagornet.dhcpv6.xml.UnsignedByteOptionType;
import com.jagornet.dhcpv6.xml.UnsignedIntOptionType;
import com.jagornet.dhcpv6.xml.UnsignedShortListOptionType;
import com.jagornet.dhcpv6.xml.UnsignedShortOptionType;
import com.jagornet.dhcpv6.xml.VendorInfoOption;

/**
 * The Class DhcpConfigOptions.
 */
public class DhcpConfigOptions
{
	/** The config options. */
	protected ConfigOptionsType configOptions;
	
	/** The option map. */
	protected Map<Integer, DhcpOption> optionMap = new TreeMap<Integer, DhcpOption>();
	
	
	/**
	 * Instantiates a new dhcp config options.
	 */
	public DhcpConfigOptions()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp config options.
	 * 
	 * @param configOptions the config options
	 */
	public DhcpConfigOptions(ConfigOptionsType configOptions)
	{
		if (configOptions != null) 
			this.configOptions = configOptions;
		else
			this.configOptions = ConfigOptionsType.Factory.newInstance();
		
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
		
		if (configOptions.isSetBcmcsAddressesOption()) {
			BcmcsAddressesOption bcmcsAddressesOption = configOptions.getBcmcsAddressesOption();
			if (bcmcsAddressesOption != null) {
				optionMap.put(bcmcsAddressesOption.getCode(), 
						new DhcpBcmcsAddressesOption(bcmcsAddressesOption));
			}
		}
		
		if (configOptions.isSetBcmcsDomainNamesOption()) {
			BcmcsDomainNamesOption bcmcsDomainNamesOption = configOptions.getBcmcsDomainNamesOption();
			if (bcmcsDomainNamesOption != null) {
				optionMap.put(bcmcsDomainNamesOption.getCode(),
						new DhcpBcmcsDomainNamesOption(bcmcsDomainNamesOption));
			}
		}
		
		if (configOptions.isSetDnsServersOption()) {
			DnsServersOption dnsServersOption = configOptions.getDnsServersOption();
			if (dnsServersOption != null) {
				optionMap.put(dnsServersOption.getCode(),
						new DhcpDnsServersOption(dnsServersOption));
			}
		}
		
		if (configOptions.isSetDomainSearchListOption()) {
			DomainSearchListOption domainSearchListOption = configOptions.getDomainSearchListOption();
			if (domainSearchListOption != null) {
				optionMap.put(domainSearchListOption.getCode(),
						new DhcpDomainSearchListOption(domainSearchListOption));
			}
		}
		
		if (configOptions.isSetGeoconfCivicOption()) {
			GeoconfCivicOption geoconfCivicOption = configOptions.getGeoconfCivicOption();
			if (geoconfCivicOption != null) {
				optionMap.put(geoconfCivicOption.getCode(),
						new DhcpGeoconfCivicOption(geoconfCivicOption));
			}
		}
		
		if (configOptions.isSetInfoRefreshTimeOption()) {
			InfoRefreshTimeOption infoRefreshTimeOption = configOptions.getInfoRefreshTimeOption();
			if (infoRefreshTimeOption != null) {
				optionMap.put(infoRefreshTimeOption.getCode(),
						new DhcpInfoRefreshTimeOption(infoRefreshTimeOption));
			}
		}
		
		if (configOptions.isSetLostServerDomainNameOption()) {
			LostServerDomainNameOption lostServerDomainNameOption = 
				configOptions.getLostServerDomainNameOption();
			if (lostServerDomainNameOption != null) {
				optionMap.put(lostServerDomainNameOption.getCode(),
						new DhcpLostServerDomainNameOption(lostServerDomainNameOption));
			}
		}
		
		if (configOptions.isSetNewPosixTimezoneOption()) {
			NewPosixTimezoneOption newPosixTimezoneOption = configOptions.getNewPosixTimezoneOption();
			if (newPosixTimezoneOption != null) {
				optionMap.put(newPosixTimezoneOption.getCode(),
						new DhcpNewPosixTimezoneOption());
			}
		}
		
		if (configOptions.isSetNewTzdbTimezoneOption()) {
			NewTzdbTimezoneOption newTzdbTimezoneOption = configOptions.getNewTzdbTimezoneOption();
			if (newTzdbTimezoneOption != null) {
				optionMap.put(newTzdbTimezoneOption.getCode(),
						new DhcpNewTzdbTimezoneOption());
			}
		}
		
		if (configOptions.isSetNisDomainNameOption()) {
			NisDomainNameOption nisDomainNameOption = configOptions.getNisDomainNameOption();
			if (nisDomainNameOption != null) {
				optionMap.put(nisDomainNameOption.getCode(),
						new DhcpNisDomainNameOption(nisDomainNameOption));
			}
		}
		
		if (configOptions.isSetNisPlusDomainNameOption()) {
			NisPlusDomainNameOption nisPlusDomainNameOption = configOptions.getNisPlusDomainNameOption();
			if (nisPlusDomainNameOption != null) {
				optionMap.put(nisPlusDomainNameOption.getCode(),
						new DhcpNisPlusDomainNameOption(nisPlusDomainNameOption));
			}
		}
		
		if (configOptions.isSetNisPlusServersOption()) {
			NisPlusServersOption nisPlusServersOption = configOptions.getNisPlusServersOption();
			if (nisPlusServersOption != null) {
				optionMap.put(nisPlusServersOption.getCode(),
						new DhcpNisPlusServersOption(nisPlusServersOption));
			}
		}
		
		if (configOptions.isSetNisServersOption()) {
			NisServersOption nisServersOption = configOptions.getNisServersOption();
			if (nisServersOption != null) {
				optionMap.put(nisServersOption.getCode(),
						new DhcpNisServersOption(nisServersOption));
			}
		}
		
		if (configOptions.isSetPanaAgentAddressesOption()) {
			PanaAgentAddressesOption panaAgentAddressesOption = configOptions.getPanaAgentAddressesOption();
			if (panaAgentAddressesOption != null) {
				optionMap.put(panaAgentAddressesOption.getCode(),
						new DhcpPanaAgentAddressesOption(panaAgentAddressesOption));
			}
		}
		
		if (configOptions.isSetPreferenceOption()) {
			PreferenceOption preferenceOption = configOptions.getPreferenceOption();
			if (preferenceOption != null) {
				optionMap.put(preferenceOption.getCode(),
						new DhcpPreferenceOption(preferenceOption));
			}
		}
		
		if (configOptions.isSetServerUnicastOption()) {
			ServerUnicastOption serverUnicastOption = configOptions.getServerUnicastOption();
			if (serverUnicastOption != null) {
				optionMap.put(serverUnicastOption.getCode(),
						new DhcpServerUnicastOption(serverUnicastOption));
			}
		}
		
		if (configOptions.isSetSipServerAddressesOption()) {
			SipServerAddressesOption sipServerAddressesOption = configOptions.getSipServerAddressesOption();
			if (sipServerAddressesOption != null) {
				optionMap.put(sipServerAddressesOption.getCode(),
						new DhcpSipServerAddressesOption(sipServerAddressesOption));
			}
		}
		
		if (configOptions.isSetSipServerDomainNamesOption()) {
			SipServerDomainNamesOption sipServerDomainNamesOption =
				configOptions.getSipServerDomainNamesOption();
			if (sipServerDomainNamesOption != null) {
				optionMap.put(sipServerDomainNamesOption.getCode(),
						new DhcpSipServerDomainNamesOption(sipServerDomainNamesOption));
			}
		}
		
		if (configOptions.isSetSntpServersOption()) {
			SntpServersOption sntpServersOption = configOptions.getSntpServersOption();
			if (sntpServersOption != null) {
				optionMap.put(sntpServersOption.getCode(),
						new DhcpSntpServersOption(sntpServersOption));
			}
		}
		
		if (configOptions.isSetStatusCodeOption()) {
			StatusCodeOption statusCodeOption = configOptions.getStatusCodeOption();
			if (statusCodeOption != null) {
				optionMap.put(statusCodeOption.getCode(),
						new DhcpStatusCodeOption(statusCodeOption));
			}
		}
		
		if (configOptions.isSetVendorInfoOption()) {
			VendorInfoOption vendorInfoOption = configOptions.getVendorInfoOption();
			if (vendorInfoOption != null) {
				optionMap.put(vendorInfoOption.getCode(),
						new DhcpVendorInfoOption(vendorInfoOption));
			}
		}
		
		if (configOptions.isSetOtherOptions()) {
			GenericOptionsType genericOptions = configOptions.getOtherOptions();
			if (genericOptions != null) {
				List<OptionDefType> optionDefs = genericOptions.getOptionDefList();
				if ((optionDefs != null) && !optionDefs.isEmpty()) {
					for (OptionDefType optionDefType : optionDefs) {
						
						int code = optionDefType.getCode();
						String name = optionDefType.getName();
							
						// the XML schema defines the optionDefType as a choice,
						// so we must determine which generic option is set
						
						if (optionDefType.isSetDomainNameListOption()) {
							DomainNameListOptionType domainNameListOption =
								optionDefType.getDomainNameListOption();
							if (domainNameListOption != null) {
								GenericDomainNameListOption dhcpOption = 
									new GenericDomainNameListOption(code, name, domainNameListOption);
								optionMap.put(code, dhcpOption);
							}
						}
	
						if (optionDefType.isSetDomainNameOption()) {
							DomainNameOptionType domainNameOption =
								optionDefType.getDomainNameOption();
							if (domainNameOption != null) {
								GenericDomainNameOption dhcpOption = 
									new GenericDomainNameOption(code, name, domainNameOption);
								optionMap.put(code, dhcpOption);
							}
						}
	
						if (optionDefType.isSetIpAddressListOption()) {
							IpAddressListOptionType ipAddressListOption =
								optionDefType.getIpAddressListOption();
							if (ipAddressListOption != null) {
								GenericIpAddressListOption dhcpOption = 
									new GenericIpAddressListOption(code, name, ipAddressListOption);
								optionMap.put(code, dhcpOption);
							}
						}
	
						if (optionDefType.isSetIpAddressOption()) {
							IpAddressOptionType ipAddressOption =
								optionDefType.getIpAddressOption();
							if (ipAddressOption != null) {
								GenericIpAddressOption dhcpOption = 
									new GenericIpAddressOption(code, name, ipAddressOption);
								optionMap.put(code, dhcpOption);
							}
						}
	
						if (optionDefType.isSetOpaqueDataListOption()) {
							OpaqueDataListOptionType opaqueDataListOption =
								optionDefType.getOpaqueDataListOption();
							if (opaqueDataListOption != null) {
								GenericOpaqueDataListOption dhcpOption = 
									new GenericOpaqueDataListOption(code, name, opaqueDataListOption);
								optionMap.put(code, dhcpOption);
							}
						}
	
						if (optionDefType.isSetOpaqueDataOption()) {
							OpaqueDataOptionType opaqueDataOption =
								optionDefType.getOpaqueDataOption();
							if (opaqueDataOption != null) {
								GenericOpaqueDataOption dhcpOption = 
									new GenericOpaqueDataOption(code, name, opaqueDataOption);
								optionMap.put(code, dhcpOption);
							}
						}
						
						if (optionDefType.isSetStringOption()) {
							StringOptionType stringOption =
								optionDefType.getStringOption();
							if (stringOption != null) {
								GenericStringOption dhcpOption =
									new GenericStringOption(code, name, stringOption);
								optionMap.put(code, dhcpOption);
							}
						}
						
						if (optionDefType.isSetUByteOption()) {
							UnsignedByteOptionType uByteOption =
								optionDefType.getUByteOption();
							if (uByteOption != null) {
								GenericUnsignedByteOption dhcpOption = 
									new GenericUnsignedByteOption(code, name, uByteOption);
								optionMap.put(code, dhcpOption);
							}
						}
						
						if (optionDefType.isSetUIntOption()) {
							UnsignedIntOptionType uIntOption =
								optionDefType.getUIntOption();
							if (uIntOption != null) {
								GenericUnsignedIntOption dhcpOption = 
									new GenericUnsignedIntOption(code, name, uIntOption);
								optionMap.put(code, dhcpOption);
							}
						}
						
						if (optionDefType.isSetUShortListOption()) {
							UnsignedShortListOptionType uShortListOption =
								optionDefType.getUShortListOption();
							if (uShortListOption != null) {
								GenericUnsignedShortListOption dhcpOption = 
									new GenericUnsignedShortListOption(code, name, uShortListOption);
								optionMap.put(code, dhcpOption);
							}
						}
	
						if (optionDefType.isSetUShortOption()) {
							UnsignedShortOptionType uShortOption =
								optionDefType.getUShortOption();
							if (uShortOption != null) {
								GenericUnsignedShortOption dhcpOption = 
									new GenericUnsignedShortOption(code, name, uShortOption);
								optionMap.put(code, dhcpOption);
							}
						}
											
					}
				}
			}
		}
		
		return optionMap;
    }

	/**
	 * Gets the config options.
	 * 
	 * @return the config options
	 */
	public ConfigOptionsType getConfigOptions() {
		return configOptions;
	}

	/**
	 * Sets the config options.
	 * 
	 * @param configOptions the new config options
	 */
	public void setConfigOptions(ConfigOptionsType configOptions) {
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
