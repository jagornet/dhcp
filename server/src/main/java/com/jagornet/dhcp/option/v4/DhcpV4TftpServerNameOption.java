/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV4TftpServerNameOption.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.option.v4;

import com.jagornet.dhcp.option.base.BaseStringOption;
import com.jagornet.dhcp.util.DhcpConstants;
import com.jagornet.dhcp.xml.V4TftpServerNameOption;

/**
 * <p>Title: DhcpV4TftpServerNameOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4TftpServerNameOption extends BaseStringOption
{
	/**
	 * Instantiates a new dhcp v4 tftp server name option.
	 */
	public DhcpV4TftpServerNameOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp v4 tftp server name option.
	 * 
	 * @param v4DomainNameOption the v4 tftp server name option
	 */
	public DhcpV4TftpServerNameOption(V4TftpServerNameOption v4TftpServerNameOption)
	{
		super(v4TftpServerNameOption); 
		setCode(DhcpConstants.V4OPTION_TFTP_SERVER_NAME);
		setV4(true);
	}
}
