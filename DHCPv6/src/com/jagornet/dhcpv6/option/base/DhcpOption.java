/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpOption.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.option.base;


/**
 * Title: DhcpOption
 * Description: The main Interface for DHCP Options.
 * 
 * @author A. Gregory Rabil
 */
public interface DhcpOption extends Encodable, Decodable
{	
    /**
	 * Gets the option code.  Java int for unsigned short.
	 * 
	 * @return the code for the option
	 */
	public int getCode();

    /**
     * Gets the option name.
     * 
     * @return the name of the option
     */
    public String getName();
    
    /**
     * Gets the length.  Java int for unsigned short.
     * 
     * @return the length of the option in bytes
     */
    public int getLength();
    
    /**
     * Return a string representation of this DhcpOption.
     * 
     * @return the string
     */
    public String toString();
}
