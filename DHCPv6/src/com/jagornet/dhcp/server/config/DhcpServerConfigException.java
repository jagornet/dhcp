/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpServerConfigException.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.server.config;

/**
 * The Class DhcpServerConfigException.
 */
public class DhcpServerConfigException extends Exception 
{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2214032364910282642L;

	/**
	 * Instantiates a new dhcp server config exception.
	 */
	public DhcpServerConfigException()
	{
		super();
	}

	/**
	 * Instantiates a new dhcp server config exception.
	 * 
	 * @param message the message
	 */
	public DhcpServerConfigException(String message)
	{
		super(message);
	}
	
	/**
	 * Instantiates a new dhcp server config exception.
	 * 
	 * @param message the message
	 * @param throwable the throwable
	 */
	public DhcpServerConfigException(String message, Throwable throwable)
	{
		super(message, throwable);
	}
	
	/**
	 * Instantiates a new dhcp server config exception.
	 * 
	 * @param throwable the throwable
	 */
	public DhcpServerConfigException(Throwable throwable)
	{
		super(throwable);
	}
}
