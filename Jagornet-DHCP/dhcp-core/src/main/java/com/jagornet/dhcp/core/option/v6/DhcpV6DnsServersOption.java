/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.v6;

import java.util.List;

import com.jagornet.dhcp.core.option.base.BaseIpAddressListOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * <p>Title: DhcpV6DnsServersOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6DnsServersOption extends BaseIpAddressListOption
{
	/**
	 * Instantiates a new dhcp dns servers option.
	 */
	public DhcpV6DnsServersOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp dns servers option.
	 * 
	 * @param dnsServersOption the dns servers option
	 */
	public DhcpV6DnsServersOption(List<String> dnsServers)
	{
		super(dnsServers);
		setCode(DhcpConstants.V6OPTION_DNS_SERVERS);
	}
}
