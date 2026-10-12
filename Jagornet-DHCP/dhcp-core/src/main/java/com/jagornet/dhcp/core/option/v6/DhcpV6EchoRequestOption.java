/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.v6;

import java.util.List;

import com.jagornet.dhcp.core.option.base.BaseUnsignedShortListOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * <p>Title: DhcpV6EchoRequestOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6EchoRequestOption extends BaseUnsignedShortListOption
{
	/**
	 * Instantiates a new dhcp echo request option.
	 */
	public DhcpV6EchoRequestOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp echo request option.
	 * 
	 * @param echoRequestOption the echo request option
	 */
	public DhcpV6EchoRequestOption(List<Integer> echoRequest)
	{
		super(echoRequest);
		setCode(DhcpConstants.V6OPTION_ECHO_REQUEST);
	}
}
