/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.config;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.option.base.BaseDhcpOption;
import com.jagornet.dhcp.core.option.base.DhcpOption;
import com.jagornet.dhcp.core.option.generic.GenericDomainNameListOption;
import com.jagornet.dhcp.core.option.generic.GenericDomainNameOption;
import com.jagornet.dhcp.core.option.generic.GenericIpAddressListOption;
import com.jagornet.dhcp.core.option.generic.GenericIpAddressOption;
import com.jagornet.dhcp.core.option.generic.GenericOpaqueDataListOption;
import com.jagornet.dhcp.core.option.generic.GenericOpaqueDataOption;
import com.jagornet.dhcp.core.option.generic.GenericStringOption;
import com.jagornet.dhcp.core.option.generic.GenericUnsignedByteOption;
import com.jagornet.dhcp.core.option.generic.GenericUnsignedIntOption;
import com.jagornet.dhcp.core.option.generic.GenericUnsignedShortListOption;
import com.jagornet.dhcp.core.option.generic.GenericUnsignedShortOption;
import com.jagornet.dhcp.server.config.xml.DomainNameListOptionType;
import com.jagornet.dhcp.server.config.xml.DomainNameOptionType;
import com.jagornet.dhcp.server.config.xml.GenericOptionsType;
import com.jagornet.dhcp.server.config.xml.IpAddressListOptionType;
import com.jagornet.dhcp.server.config.xml.IpAddressOptionType;
import com.jagornet.dhcp.server.config.xml.OpaqueData;
import com.jagornet.dhcp.server.config.xml.OpaqueDataListOptionType;
import com.jagornet.dhcp.server.config.xml.OpaqueDataOptionType;
import com.jagornet.dhcp.server.config.xml.OptionDefType;

/**
 * A factory for creating GenericOption objects.
 * 
 * @author A. Gregory Rabil
 */
public class GenericOptionFactory
{
	private static Logger log = LoggerFactory.getLogger(GenericOptionFactory.class);

	// Private constructor to hide the implicit public one
	private GenericOptionFactory() {
		// Prevent instantiation
	}
	
	/**
	 * Convert a list of XML Generic options to a map of DhcpOptions
	 * 
	 * @param genericOptions
	 * @return a map of generic options
	 */
	public static Map<Integer, DhcpOption> genericOptions(GenericOptionsType genericOptions) 
	{
		Map<Integer, DhcpOption> optMap = new TreeMap<>(); 
		if (genericOptions != null) {
			List<OptionDefType> optionDefs = genericOptions.getOptionDefList();
			if ((optionDefs != null) && !optionDefs.isEmpty()) {
				for (OptionDefType optionDefType : optionDefs) {
					
					int code = optionDefType.getCode();
					//String name = optionDefType.getName();

					DhcpOption dhcpOption = getDhcpOption(optionDefType);
					optMap.put(code, dhcpOption);
					
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

	public static DhcpOption getDhcpOption(OptionDefType optionDef)
	{
		int code = optionDef.getCode();
		String name = optionDef.getName();
		if (optionDef.getDomainNameListOption() != null) {
			DomainNameListOptionType domainNameListOption = optionDef.getDomainNameListOption();
			if (domainNameListOption.getDomainNameList() != null) {
				GenericDomainNameListOption genericDomainNameListOption = 
						new GenericDomainNameListOption(code, name);
				genericDomainNameListOption.setDomainNameList(domainNameListOption.getDomainNameList());
				return genericDomainNameListOption;
			}
		}
		else if (optionDef.getDomainNameOption() != null) {
			DomainNameOptionType domainNameOption = optionDef.getDomainNameOption();
			if (domainNameOption.getDomainName() != null) {
				GenericDomainNameOption genericDomainNameOption = 
						new GenericDomainNameOption(code, name);
				genericDomainNameOption.setDomainName(domainNameOption.getDomainName());
				return genericDomainNameOption;
			}
		}
		else if (optionDef.getIpAddressListOption() != null) {
			IpAddressListOptionType ipAddressListOption = optionDef.getIpAddressListOption();
			if (ipAddressListOption.getIpAddressList() != null) {
				GenericIpAddressListOption genericIpAddressListOption =
						new GenericIpAddressListOption(code, name);
				genericIpAddressListOption.setIpAddressList(ipAddressListOption.getIpAddressList());
				return genericIpAddressListOption;
			}
		}
		else if (optionDef.getIpAddressOption() != null) {
			IpAddressOptionType ipAddressOption = optionDef.getIpAddressOption();
			if (ipAddressOption.getIpAddress() != null) {
				GenericIpAddressOption genericIpAddressOption = 
						new GenericIpAddressOption(code, name);
				genericIpAddressOption.setIpAddress(ipAddressOption.getIpAddress());
				return genericIpAddressOption;
			}
		}
		else if (optionDef.getOpaqueDataListOption() != null) {
			OpaqueDataListOptionType opaqueDataListOption = optionDef.getOpaqueDataListOption();
			if (opaqueDataListOption.getOpaqueDataList() != null) {
				GenericOpaqueDataListOption genericOpaqueDataListOption = 
						new GenericOpaqueDataListOption(code, name);
        		for (OpaqueData opaqueData : opaqueDataListOption.getOpaqueDataList()) {
        			genericOpaqueDataListOption.addOpaqueData(
        					OpaqueDataUtil.toBaseOpaqueData(opaqueData));
				}
    			return genericOpaqueDataListOption;
			}
		}
		else if (optionDef.getOpaqueDataOption() != null) {
			OpaqueDataOptionType opaqueDataOption = optionDef.getOpaqueDataOption();
			if (opaqueDataOption.getOpaqueData() != null) {
				GenericOpaqueDataOption genericOpaqueDataOption = 
						new GenericOpaqueDataOption(code, name);
				OpaqueData opaqueData = opaqueDataOption.getOpaqueData();
				genericOpaqueDataOption.setOpaqueData(
						OpaqueDataUtil.toBaseOpaqueData(opaqueData));
				return genericOpaqueDataOption;
			}
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
	
}
