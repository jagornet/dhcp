/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file GenericOptionFactory.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.server.config.option;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.option.base.BaseDhcpOption;
import com.jagornet.dhcp.option.base.DhcpOption;
import com.jagornet.dhcp.option.generic.GenericDomainNameListOption;
import com.jagornet.dhcp.option.generic.GenericDomainNameOption;
import com.jagornet.dhcp.option.generic.GenericIpAddressListOption;
import com.jagornet.dhcp.option.generic.GenericIpAddressOption;
import com.jagornet.dhcp.option.generic.GenericOpaqueDataListOption;
import com.jagornet.dhcp.option.generic.GenericOpaqueDataOption;
import com.jagornet.dhcp.option.generic.GenericStringOption;
import com.jagornet.dhcp.option.generic.GenericUnsignedByteOption;
import com.jagornet.dhcp.option.generic.GenericUnsignedIntOption;
import com.jagornet.dhcp.option.generic.GenericUnsignedShortListOption;
import com.jagornet.dhcp.option.generic.GenericUnsignedShortOption;
import com.jagornet.dhcp.server.config.option.base.BaseOpaqueData;
import com.jagornet.dhcp.xml.GenericOptionsType;
import com.jagornet.dhcp.xml.OpaqueData;
import com.jagornet.dhcp.xml.OptionDefType;

/**
 * A factory for creating GenericOption objects.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpConfigOptionFactory
{
	private static Logger log = LoggerFactory.getLogger(DhcpConfigOptionFactory.class);

	public static DhcpOption getDhcpOption(OptionDefType optionDef)
	{
		int code = optionDef.getCode();
		String name = optionDef.getName();
		if (optionDef.getDomainNameListOption() != null) {
			return new GenericDomainNameListOption(code, name, 
							optionDef.getDomainNameListOption().getDomainNameList());
		}
		else if (optionDef.getDomainNameOption() != null) {
			return new GenericDomainNameOption(code, name, 
							optionDef.getDomainNameOption().getDomainName());
		}
		else if (optionDef.getIpAddressListOption() != null) {
			return new GenericIpAddressListOption(code, name, 
							optionDef.getIpAddressListOption().getIpAddressList());
		}
		else if (optionDef.getIpAddressOption() != null) {
			return new GenericIpAddressOption(code, name, 
							optionDef.getIpAddressOption().getIpAddress());
		}
		else if (optionDef.getOpaqueDataListOption() != null) {
			List<OpaqueData> opaqueDataList = optionDef.getOpaqueDataListOption().getOpaqueDataList();
			List<com.jagornet.dhcp.option.base.BaseOpaqueData> baseOpaqueDataList = null;
			if (opaqueDataList != null) {
				baseOpaqueDataList = new ArrayList<com.jagornet.dhcp.option.base.BaseOpaqueData>();
				for (OpaqueData opaqueData : opaqueDataList) {
					baseOpaqueDataList.add(new BaseOpaqueData(opaqueData));
				}
			}
			return new GenericOpaqueDataListOption(code, name, baseOpaqueDataList);
		}
		else if (optionDef.getOpaqueDataOption() != null) {
			OpaqueData opaqueData = optionDef.getOpaqueDataOption().getOpaqueData();
			BaseOpaqueData baseOpaqueData = null;
			if (opaqueData != null) {
				baseOpaqueData = new BaseOpaqueData(opaqueData);
			}
			return new GenericOpaqueDataOption(code, name, baseOpaqueData); 
		}
		else if (optionDef.getStringOption() != null) {
			return new GenericStringOption(code, name, 
							optionDef.getStringOption().getString());
		}
		else if (optionDef.getUByteOption() != null) {
			return new GenericUnsignedByteOption(code, name, 
							optionDef.getUByteOption().getUnsignedByte());
		}
		else if (optionDef.getUIntOption() != null) {
			return new GenericUnsignedIntOption(code, name, 
							optionDef.getUIntOption().getUnsignedInt());
		}
		else if (optionDef.getUShortListOption() != null) {
			return new GenericUnsignedShortListOption(code, name, 
							optionDef.getUShortListOption().getUnsignedShortList());
		}
		else if (optionDef.getUShortOption() != null) {
			return new GenericUnsignedShortOption(code, name, 
							optionDef.getUShortOption().getUnsignedShort());
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
			List<OptionDefType> optionDefs = genericOptions.getOptionDefs();
			if ((optionDefs != null) && !optionDefs.isEmpty()) {
				for (OptionDefType optionDefType : optionDefs) {
					
					int code = optionDefType.getCode();
					String name = optionDefType.getName();
						
					// the XML schema defines the optionDefType as a choice,
					// so we must determine which generic option is set
					
/*
					if (optionDefType.getDomainNameListOption() != null) {
						DomainNameListOptionType domainNameListOption =
							optionDefType.getDomainNameListOption();
						if (domainNameListOption != null) {
							GenericDomainNameListOption dhcpOption = 
								new GenericDomainNameListOption(code, name, domainNameListOption);
							optMap.put(code, dhcpOption);
							continue;
						}
					}

					if (optionDefType.getDomainNameOption() != null) {
						DomainNameOptionType domainNameOption =
							optionDefType.getDomainNameOption();
						if (domainNameOption != null) {
							GenericDomainNameOption dhcpOption = 
								new GenericDomainNameOption(code, name, domainNameOption);
							optMap.put(code, dhcpOption);
							continue;
						}
					}

					if (optionDefType.getIpAddressListOption() != null) {
						IpAddressListOptionType ipAddressListOption =
							optionDefType.getIpAddressListOption();
						if (ipAddressListOption != null) {
							GenericIpAddressListOption dhcpOption = 
								new GenericIpAddressListOption(code, name, ipAddressListOption);
							optMap.put(code, dhcpOption);
							continue;
						}
					}

					if (optionDefType.getIpAddressOption() != null) {
						IpAddressOptionType ipAddressOption =
							optionDefType.getIpAddressOption();
						if (ipAddressOption != null) {
							GenericIpAddressOption dhcpOption = 
								new GenericIpAddressOption(code, name, ipAddressOption);
							optMap.put(code, dhcpOption);
							continue;
						}
					}

					if (optionDefType.getOpaqueDataListOption() != null) {
						OpaqueDataListOptionType opaqueDataListOption =
							optionDefType.getOpaqueDataListOption();
						if (opaqueDataListOption != null) {
							GenericOpaqueDataListOption dhcpOption = 
								new GenericOpaqueDataListOption(code, name, opaqueDataListOption);
							optMap.put(code, dhcpOption);
							continue;
						}
					}

					if (optionDefType.getOpaqueDataOption() != null) {
						OpaqueDataOptionType opaqueDataOption =
							optionDefType.getOpaqueDataOption();
						if (opaqueDataOption != null) {
							GenericOpaqueDataOption dhcpOption = 
								new GenericOpaqueDataOption(code, name, opaqueDataOption);
							optMap.put(code, dhcpOption);
							continue;
						}
					}
					
					if (optionDefType.getStringOption() != null) {
						StringOptionType stringOption =
							optionDefType.getStringOption();
						if (stringOption != null) {
							GenericStringOption dhcpOption =
								new GenericStringOption(code, name, stringOption);
							optMap.put(code, dhcpOption);
							continue;
						}
					}
					
					if (optionDefType.getUByteListOption() != null) {
						UnsignedByteListOptionType uByteListOption =
							optionDefType.getUByteListOption();
						if (uByteListOption != null) {
							GenericUnsignedByteListOption dhcpOption = 
								new GenericUnsignedByteListOption(code, name, uByteListOption);
							optMap.put(code, dhcpOption);
							continue;
						}
					}
					
					if (optionDefType.getUByteOption() != null) {
						UnsignedByteOptionType uByteOption =
							optionDefType.getUByteOption();
						if (uByteOption != null) {
							GenericUnsignedByteOption dhcpOption = 
								new GenericUnsignedByteOption(code, name, uByteOption);
							optMap.put(code, dhcpOption);
							continue;
						}
					}
					
					if (optionDefType.getUIntOption() != null) {
						UnsignedIntOptionType uIntOption =
							optionDefType.getUIntOption();
						if (uIntOption != null) {
							GenericUnsignedIntOption dhcpOption = 
								new GenericUnsignedIntOption(code, name, uIntOption);
							optMap.put(code, dhcpOption);
							continue;
						}
					}
					
					if (optionDefType.getUShortListOption() != null) {
						UnsignedShortListOptionType uShortListOption =
							optionDefType.getUShortListOption();
						if (uShortListOption != null) {
							GenericUnsignedShortListOption dhcpOption = 
								new GenericUnsignedShortListOption(code, name, uShortListOption);
							optMap.put(code, dhcpOption);
							continue;
						}
					}

					if (optionDefType.getUShortOption() != null) {
						UnsignedShortOptionType uShortOption =
							optionDefType.getUShortOption();
						if (uShortOption != null) {
							GenericUnsignedShortOption dhcpOption = 
								new GenericUnsignedShortOption(code, name, uShortOption);
							optMap.put(code, dhcpOption);
							continue;
						}
					}
*/
					DhcpOption dhcpOption = getDhcpOption(optionDefType);
					if (dhcpOption != null) {
						optMap.put(code, dhcpOption);
					}
					
					if(optMap.get(code) instanceof BaseDhcpOption) {
						if (optionDefType.isV4()) {
							((BaseDhcpOption) optMap.get(code)).setV4(true);
						}
					}
				}
			}
		}
		return optMap;
	}

}
