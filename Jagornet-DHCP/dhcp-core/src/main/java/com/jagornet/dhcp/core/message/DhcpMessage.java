/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.message;

import java.util.List;

import com.jagornet.dhcp.core.option.base.DhcpOption;

/**
 * The Interface DhcpMessage.
 * 
 * @author A. Gregory Rabil
 */
public interface DhcpMessage {
	
	/**
	 * Sets the message type.
	 *
	 * @param msgType the new message type
	 */
	public void setMessageType(short msgType);
	
	/**
	 * Gets the message type.
	 *
	 * @return the message type
	 */
	public short getMessageType();
	
	/**
	 * Gets the dhcp option.
	 *
	 * @param optionCode the option code
	 * @return the dhcp option
	 */
	public DhcpOption getDhcpOption(int optionCode);
	
	/**
	 * Gets the list of option codes requested by the client
	 * @return the list of option codes requested by the client
	 */
	public List<Integer> getRequestedOptionCodes();
}
