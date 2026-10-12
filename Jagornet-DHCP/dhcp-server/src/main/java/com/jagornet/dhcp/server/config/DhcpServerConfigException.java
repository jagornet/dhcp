/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
