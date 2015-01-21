/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file GenericDomainNameListOption.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.option.generic;

import com.jagornet.dhcp.option.base.BaseDomainNameListOption;
import com.jagornet.dhcp.xml.DomainNameListOptionType;

/**
 * <p>Title: GenericDomainNameListOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class GenericDomainNameListOption extends BaseDomainNameListOption
{
	/**
	 * Instantiates a new generic domain name list option.
	 * 
	 * @param code the option code
	 * @param name the option name
	 */
	public GenericDomainNameListOption(int code, String name)
	{
		this(code, name, null);
	}
	
	/**
	 * Instantiates a new generic domain name list option.
	 * 
	 * @param code the option code
	 * @param name the option name
	 * @param domainNameListOption the domain name list option
	 */
	public GenericDomainNameListOption(int code, String name,
									   DomainNameListOptionType domainNameListOption)
	{
		super(domainNameListOption);
		setCode(code);
		setName(name);
	}
}
