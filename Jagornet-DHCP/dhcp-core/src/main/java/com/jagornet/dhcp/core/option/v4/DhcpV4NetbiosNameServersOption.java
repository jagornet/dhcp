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
 * <p>Title: DhcpV4NetbiosNameServersOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4NetbiosNameServersOption extends BaseIpAddressListOption
{
	/**
	 * Instantiates a new dhcp v4 netbiosName servers option.
	 */
	public DhcpV4NetbiosNameServersOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp v4 netbios name servers option.
	 * 
	 * @param v4NetbiosNameServersOption the v4 netbios name servers option
	 */
	public DhcpV4NetbiosNameServersOption(List<String> v4NetbiosNameServers)
	{
		super(v4NetbiosNameServers);
		setCode(DhcpConstants.V4OPTION_NETBIOS_NAME_SERVERS);
		setV4(true);
	}
}
