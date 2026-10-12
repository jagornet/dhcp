/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.v6;

import java.util.List;

import com.jagornet.dhcp.core.option.base.BaseDomainNameListOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * <p>Title: DhcpV6SipServerDomainNamesOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6SipServerDomainNamesOption extends BaseDomainNameListOption
{
	/**
	 * Instantiates a new dhcp sip server domain names option.
	 */
	public DhcpV6SipServerDomainNamesOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp sip server domain names option.
	 * 
	 * @param sipServerDomainNamesOption the sip server domain names option
	 */
	public DhcpV6SipServerDomainNamesOption(List<String> sipServerDomainNames)
	{
		super(sipServerDomainNames);
		setCode(DhcpConstants.V6OPTION_SIP_SERVERS_DOMAIN_LIST);
	}
}
