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
 * <p>Title: DhcpV6SipServerAddressesOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6SipServerAddressesOption extends BaseIpAddressListOption
{
	/**
	 * Instantiates a new dhcp sip server addresses option.
	 */
	public DhcpV6SipServerAddressesOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp sip server addresses option.
	 * 
	 * @param sipServerAddressesOption the sip server addresses option
	 */
	public DhcpV6SipServerAddressesOption(List<String> sipServerAddresses)
	{
		super(sipServerAddresses);
		setCode(DhcpConstants.V6OPTION_SIP_SERVERS_ADDRESS_LIST);
	}
}
