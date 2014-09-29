/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file GenericOptionFactory.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.option.generic;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcp.xml.DomainNameListOptionType;
import com.jagornet.dhcp.xml.DomainNameOptionType;
import com.jagornet.dhcp.xml.GenericOptionsType;
import com.jagornet.dhcp.xml.IpAddressListOptionType;
import com.jagornet.dhcp.xml.IpAddressOptionType;
import com.jagornet.dhcp.xml.OpaqueDataListOptionType;
import com.jagornet.dhcp.xml.OpaqueDataOptionType;
import com.jagornet.dhcp.xml.OptionDefType;
import com.jagornet.dhcp.xml.StringOptionType;
import com.jagornet.dhcp.xml.UnsignedByteListOptionType;
import com.jagornet.dhcp.xml.UnsignedByteOptionType;
import com.jagornet.dhcp.xml.UnsignedIntOptionType;
import com.jagornet.dhcp.xml.UnsignedShortListOptionType;
import com.jagornet.dhcp.xml.UnsignedShortOptionType;

/**
 * A factory for creating GenericOption objects.
 * 
 * @author A. Gregory Rabil
 */
public class GenericOptionFactory
{
	private static Logger log = LoggerFactory.getLogger(GenericOptionFactory.class);

	public static DhcpOption getDhcpOption(OptionDefType optionDef)
	{
		int code = optionDef.getCode();
		String name = optionDef.getName();
		if (optionDef.isSetDomainNameListOption()) {
			return new GenericDomainNameListOption(code, name, optionDef.getDomainNameListOption());
		}
		else if (optionDef.isSetDomainNameOption()) {
			return new GenericDomainNameOption(code, name, optionDef.getDomainNameOption());
		}
		else if (optionDef.isSetIpAddressListOption()) {
			return new GenericIpAddressListOption(code, name, optionDef.getIpAddressListOption());
		}
		else if (optionDef.isSetIpAddressOption()) {
			return new GenericIpAddressOption(code, name, optionDef.getIpAddressOption());
		}
		else if (optionDef.isSetOpaqueDataListOption()) {
			return new GenericOpaqueDataListOption(code, name, optionDef.getOpaqueDataListOption());
		}
		else if (optionDef.isSetOpaqueDataOption()) {
			return new GenericOpaqueDataOption(code, name, optionDef.getOpaqueDataOption());
		}
		else if (optionDef.isSetStringOption()) {
			return new GenericStringOption(code, name, optionDef.getStringOption());
		}
		else if (optionDef.isSetUByteOption()) {
			return new GenericUnsignedByteOption(code, name, optionDef.getUByteOption());
		}
		else if (optionDef.isSetUIntOption()) {
			return new GenericUnsignedIntOption(code, name, optionDef.getUIntOption());
		}
		else if (optionDef.isSetUShortListOption()) {
			return new GenericUnsignedShortListOption(code, name, optionDef.getUShortListOption());
		}
		else if (optionDef.isSetUShortOption()) {
			return new GenericUnsignedShortOption(code, name, optionDef.getUShortOption());
		}
		else {
			log.error("Unknown generic option type");
		}
		return null;
	}

	/**
	 * Convert a list of XML Generic options to a map of DhcpOptions
	 * 
	 * @param genericOptions
	 * @return a map of generic options
	 */
	public static Map<Integer, DhcpOption> genericOptions(GenericOptionsType genericOptions) 
	{
		Map<Integer, DhcpOption> optMap = new TreeMap<Integer, DhcpOption>(); 
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
							optMap.put(code, dhcpOption);
							continue;
						}
					}

					if (optionDefType.isSetDomainNameOption()) {
						DomainNameOptionType domainNameOption =
							optionDefType.getDomainNameOption();
						if (domainNameOption != null) {
							GenericDomainNameOption dhcpOption = 
								new GenericDomainNameOption(code, name, domainNameOption);
							optMap.put(code, dhcpOption);
							continue;
						}
					}

					if (optionDefType.isSetIpAddressListOption()) {
						IpAddressListOptionType ipAddressListOption =
							optionDefType.getIpAddressListOption();
						if (ipAddressListOption != null) {
							GenericIpAddressListOption dhcpOption = 
								new GenericIpAddressListOption(code, name, ipAddressListOption);
							optMap.put(code, dhcpOption);
							continue;
						}
					}

					if (optionDefType.isSetIpAddressOption()) {
						IpAddressOptionType ipAddressOption =
							optionDefType.getIpAddressOption();
						if (ipAddressOption != null) {
							GenericIpAddressOption dhcpOption = 
								new GenericIpAddressOption(code, name, ipAddressOption);
							optMap.put(code, dhcpOption);
							continue;
						}
					}

					if (optionDefType.isSetOpaqueDataListOption()) {
						OpaqueDataListOptionType opaqueDataListOption =
							optionDefType.getOpaqueDataListOption();
						if (opaqueDataListOption != null) {
							GenericOpaqueDataListOption dhcpOption = 
								new GenericOpaqueDataListOption(code, name, opaqueDataListOption);
							optMap.put(code, dhcpOption);
							continue;
						}
					}

					if (optionDefType.isSetOpaqueDataOption()) {
						OpaqueDataOptionType opaqueDataOption =
							optionDefType.getOpaqueDataOption();
						if (opaqueDataOption != null) {
							GenericOpaqueDataOption dhcpOption = 
								new GenericOpaqueDataOption(code, name, opaqueDataOption);
							optMap.put(code, dhcpOption);
							continue;
						}
					}
					
					if (optionDefType.isSetStringOption()) {
						StringOptionType stringOption =
							optionDefType.getStringOption();
						if (stringOption != null) {
							GenericStringOption dhcpOption =
								new GenericStringOption(code, name, stringOption);
							optMap.put(code, dhcpOption);
							continue;
						}
					}
					
					if (optionDefType.isSetUByteListOption()) {
						UnsignedByteListOptionType uByteListOption =
							optionDefType.getUByteListOption();
						if (uByteListOption != null) {
							GenericUnsignedByteListOption dhcpOption = 
								new GenericUnsignedByteListOption(code, name, uByteListOption);
							optMap.put(code, dhcpOption);
							continue;
						}
					}
					
					if (optionDefType.isSetUByteOption()) {
						UnsignedByteOptionType uByteOption =
							optionDefType.getUByteOption();
						if (uByteOption != null) {
							GenericUnsignedByteOption dhcpOption = 
								new GenericUnsignedByteOption(code, name, uByteOption);
							optMap.put(code, dhcpOption);
							continue;
						}
					}
					
					if (optionDefType.isSetUIntOption()) {
						UnsignedIntOptionType uIntOption =
							optionDefType.getUIntOption();
						if (uIntOption != null) {
							GenericUnsignedIntOption dhcpOption = 
								new GenericUnsignedIntOption(code, name, uIntOption);
							optMap.put(code, dhcpOption);
							continue;
						}
					}
					
					if (optionDefType.isSetUShortListOption()) {
						UnsignedShortListOptionType uShortListOption =
							optionDefType.getUShortListOption();
						if (uShortListOption != null) {
							GenericUnsignedShortListOption dhcpOption = 
								new GenericUnsignedShortListOption(code, name, uShortListOption);
							optMap.put(code, dhcpOption);
							continue;
						}
					}

					if (optionDefType.isSetUShortOption()) {
						UnsignedShortOptionType uShortOption =
							optionDefType.getUShortOption();
						if (uShortOption != null) {
							GenericUnsignedShortOption dhcpOption = 
								new GenericUnsignedShortOption(code, name, uShortOption);
							optMap.put(code, dhcpOption);
							continue;
						}
					}
										
				}
			}
		}
		return optMap;
	}

}
