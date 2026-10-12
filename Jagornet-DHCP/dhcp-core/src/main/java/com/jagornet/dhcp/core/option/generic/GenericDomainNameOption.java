/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.generic;

import com.jagornet.dhcp.core.option.base.BaseDomainNameOption;

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
	public GenericDomainNameOption(int code, String name, String domainName)
	{
		super(domainName);
		setCode(code);
		setName(name);
	}
}
