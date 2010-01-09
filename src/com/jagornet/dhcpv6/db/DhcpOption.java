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
package com.jagornet.dhcpv6.db;


// TODO: Auto-generated Javadoc
/**
 * TODO: RENAME this to just Option.java, and the corresponding DB table as well
 * because DhcpOption name conflicts with the interface in option.base package.
 * 
 * @author agrabil
 */

public class DhcpOption
{
	
	/** The id. */
	protected Long id;	// the database-generated object ID

	/** The code. */
	protected int code;	// int = ushort
	
	/** The value. */
	protected byte[] value;
		
	/** The identity assoc id. */
	protected Long identityAssocId;
	
	/** The ip address id. */
	protected Long ipAddressId;

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	
	/**
	 * Sets the id.
	 * 
	 * @param id the new id
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * Gets the code.
	 * 
	 * @return the code
	 */
	public int getCode() {
		return code;
	}
	
	/**
	 * Sets the code.
	 * 
	 * @param code the new code
	 */
	public void setCode(int code) {
		this.code = code;
	}
	
	/**
	 * Gets the value.
	 * 
	 * @return the value
	 */
	public byte[] getValue() {
		return value;
	}
	
	/**
	 * Sets the value.
	 * 
	 * @param value the new value
	 */
	public void setValue(byte[] value) {
		this.value = value;
	}
	
	/**
	 * Gets the identity assoc id.
	 * 
	 * @return the identity assoc id
	 */
	public Long getIdentityAssocId() {
		return identityAssocId;
	}
	
	/**
	 * Sets the identity assoc id.
	 * 
	 * @param identityAssocId the new identity assoc id
	 */
	public void setIdentityAssocId(Long identityAssocId) {
		this.identityAssocId = identityAssocId;
	}
	
	/**
	 * Gets the ip address id.
	 * 
	 * @return the ip address id
	 */
	public Long getIpAddressId() {
		return ipAddressId;
	}
	
	/**
	 * Sets the ip address id.
	 * 
	 * @param ipAddressId the new ip address id
	 */
	public void setIpAddressId(Long ipAddressId) {
		this.ipAddressId = ipAddressId;
	}
}
