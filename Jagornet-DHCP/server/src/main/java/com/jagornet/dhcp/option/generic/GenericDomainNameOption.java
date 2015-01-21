/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file GenericDomainNameOption.java is part of Jagornet DHCP.
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

import com.jagornet.dhcp.option.base.BaseDomainNameOption;
import com.jagornet.dhcp.xml.DomainNameOptionType;

/**
 * <p>Title: GenericDomainNameOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class GenericDomainNameOption extends BaseDomainNameOption
{	
	/**
	 * Instantiates a new generic domain name option.
	 * 
	 * @param code the option code
	 * @param name the option name
	 */
	public GenericDomainNameOption(int code, String name)
	{
		this(code, name, null);
	}
	
	/**
	 * Instantiates a new generic domain name option.
	 * 
	 * @param code the option code
	 * @param name the option name
	 * @param domainNameOption the domain name option
	 */
	public GenericDomainNameOption(int code, String name,
								   DomainNameOptionType domainNameOption)
	{
		super(domainNameOption);
		setCode(code);
		setName(name);
	}
}
