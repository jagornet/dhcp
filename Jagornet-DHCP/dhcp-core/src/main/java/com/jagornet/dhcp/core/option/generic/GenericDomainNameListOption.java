/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.generic;

import java.util.List;

import com.jagornet.dhcp.core.option.base.BaseDomainNameListOption;

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
	public GenericDomainNameListOption(int code, String name, List<String> domainNames)
	{
		super(domainNames);
		setCode(code);
		setName(name);
	}
}
