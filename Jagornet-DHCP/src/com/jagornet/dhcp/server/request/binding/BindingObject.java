/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BindingObject.java is part of Jagornet DHCP.
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

import java.net.InetAddress;
import java.util.Date;

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
	public DhcpConfigObject getConfigObj();
	public void setStartTime(Date startDate);
	public void setPreferredEndTime(Date preferredDate);
	public void setValidEndTime(Date validDate);
	public InetAddress getIpAddress();
}
