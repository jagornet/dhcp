/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.request.binding;

import java.math.BigInteger;
import java.net.InetAddress;

import com.jagornet.dhcp.server.config.DhcpConfigObject;
import com.jagornet.dhcp.server.config.xml.LinkFilter;

/**
 * Interface BindingPool.  
 * Common interface for V6AddressBindingPool, V6PrefixBindingPool and V4AddressBindingPool
 * 
 * @author A. Gregory Rabil
 */
public interface BindingPool extends DhcpConfigObject
{
	public InetAddress getStartAddress();
	public InetAddress getEndAddress();
	public InetAddress getNextAvailableAddress();
	public void setUsed(InetAddress addr);
	public void setFree(InetAddress addr);
	public boolean contains(InetAddress addr);
	public LinkFilter getLinkFilter();
	public BigInteger getSize();
}
