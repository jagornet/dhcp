/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.v4;

import java.util.List;

import com.jagornet.dhcp.core.option.base.BaseIpAddressListOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * <p>Title: DhcpV4DomainServersOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4DomainServersOption extends BaseIpAddressListOption
{
	/**
	 * Instantiates a new dhcp v4 domain servers option.
	 */
	public DhcpV4DomainServersOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp v4 domain servers option.
	 * 
	 * @param v4DomainServersOption the v4 domain servers option
	 */
	public DhcpV4DomainServersOption(List<String> v4DomainServers)
	{
		super(v4DomainServers);
		setCode(DhcpConstants.V4OPTION_DOMAIN_SERVERS);
		setV4(true);
	}
}
