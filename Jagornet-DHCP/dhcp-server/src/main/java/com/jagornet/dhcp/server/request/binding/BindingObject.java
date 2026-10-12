/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.request.binding;

import java.net.InetAddress;
import java.util.Date;
import java.util.Map;

import com.jagornet.dhcp.core.option.base.DhcpOption;
import com.jagornet.dhcp.server.config.DhcpConfigObject;

/**
 * Interface BindingObject.  
 * Common interface for V6BindingAddress, V6BindingPrefix and V4BindingAddress.
 * 
 * @author A. Gregory Rabil
 */
public interface BindingObject
{
	public void setState(byte state);
	public void setHaPeerState(byte state);
	public DhcpConfigObject getConfigObj();
	public void setStartTime(Date startDate);
	public void setPreferredEndTime(Date preferredDate);
	public void setValidEndTime(Date validDate);
	public InetAddress getIpAddress();
	
	// the resultant message-level DHCP option map from effective
	// configured options filtered by any requested options from the client
	// this includes DHCPv4 options or DHCPv6 message-level options
	public Map<Integer, DhcpOption> getDhcpOptionMap();
	public void setDhcpOptionMap(Map<Integer, DhcpOption> dhcpOptionMap);
}
