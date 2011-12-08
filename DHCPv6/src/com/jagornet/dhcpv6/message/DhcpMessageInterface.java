/*
 * Copyright 2011 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpMessageInterface.java is part of DHCPv6.
 *
 *   DHCPv6 is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   DHCPv6 is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with DHCPv6.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcpv6.message;

import com.jagornet.dhcpv6.option.base.DhcpOption;

/**
 * The Interface DhcpMessageInterface.
 * 
 * @author A. Gregory Rabil
 */
public interface DhcpMessageInterface {
	
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
}
