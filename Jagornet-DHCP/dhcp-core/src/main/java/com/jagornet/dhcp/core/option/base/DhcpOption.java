/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.core.option.base;


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
	public void setCode(int code);

    /**
     * Gets the option name.
     * 
     * @return the name of the option
     */
    public String getName();
    public void setName(String name);
    
    /**
     * Gets the length.  Java int for unsigned short.
     * 
     * @return the length of the option in bytes
     */
    public int getLength();
    
    /**
     * Gets the raw option data as a byte array.
     * 
     * @return the option data bytes
     */
    public byte[] getRawData();
    public void setRawData(byte[] value);
    
    /**
     * Return a string representation of this DhcpOption.
     * 
     * @return the string
     */
    public String toString();
    
    /**
     * Return true if this is a DHCPv4 option, false otherwise.
     * 
     * @return true if this is a DHCPv4 option, false otherwise.
     */
    public boolean isV4();
    public void setV4(boolean isV4);
    
}
