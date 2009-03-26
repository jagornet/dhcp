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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.xml.OptionDefType;

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

}
