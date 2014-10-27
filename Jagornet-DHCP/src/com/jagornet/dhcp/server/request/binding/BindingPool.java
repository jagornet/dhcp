/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BindingPool.java is part of Jagornet DHCP.
 *
 *   Jagornet DHCP is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Jagornet DHCP is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Jagornet DHCP.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcp.server.request.binding;

import java.math.BigInteger;
import java.net.InetAddress;

import com.jagornet.dhcp.server.config.DhcpConfigObject;
import com.jagornet.dhcp.xml.LinkFilter;

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
