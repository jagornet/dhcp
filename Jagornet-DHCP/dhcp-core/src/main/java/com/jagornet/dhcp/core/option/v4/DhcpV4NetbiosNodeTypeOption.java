/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.v4;

import com.jagornet.dhcp.core.option.base.BaseUnsignedByteOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * <p>Title: DhcpV4NetbiosNodeTypeOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4NetbiosNodeTypeOption extends BaseUnsignedByteOption
{
	/**
	 * Instantiates a new dhcpv4 netbios node type option.
	 */
	public DhcpV4NetbiosNodeTypeOption()
	{
		this((short)0);
	}
	
	/**
	 * Instantiates a new dhcpv4 netbios node type option.
	 * 
	 * @param v4NetbiosNodeTypeOption the v4 netbios node type option
	 */
	public DhcpV4NetbiosNodeTypeOption(short v4NetbiosNodeType)
	{
		super(v4NetbiosNodeType);
		setCode(DhcpConstants.V4OPTION_NETBIOS_NODE_TYPE);
		setV4(true);
	}
}
