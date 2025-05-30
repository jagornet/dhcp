/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpOption.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.server.db;

import java.io.IOException;
import java.util.Arrays;

import com.jagornet.dhcp.core.util.Util;

/**
 * The DhcpOption POJO class for the DHCPOPTION database table.
 * 
 * @author A. Gregory Rabil
 */

public class DbDhcpOption
{
	protected Long id;	// the database-generated object ID
	protected int code;	// int = ushort
	protected byte[] value;	// value includes 2 bytes for length to facilitate encode/decode
	protected Long identityAssocId;
	protected Long iaAddressId;
	protected Long iaPrefixId;

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
	 * Gets the ia address id.
	 * 
	 * @return the ia address id
	 */
	public Long getIaAddressId() {
		return iaAddressId;
	}
	
	/**
	 * Sets the ia address id.
	 * 
	 * @param iaAddressId the new ia address id
	 */
	public void setIaAddressId(Long iaAddressId) {
		this.iaAddressId = iaAddressId;
	}

	/**
	 * Gets the ia prefix id.
	 *
	 * @return the ia prefix id
	 */
	public Long getIaPrefixId() {
		return iaPrefixId;
	}

	/**
	 * Sets the ia prefix id.
	 *
	 * @param iaPrefixId the new ia prefix id
	 */
	public void setIaPrefixId(Long iaPrefixId) {
		this.iaPrefixId = iaPrefixId;
	}
	
	public String toString() {
//		return "DhcpOption: code=" + getCode() + 
//				" value=" + Util.toHexString(getValue());
		return toJson();
	}
	
	public String toJson() {
		return "\"" + getCode() + "\":\"" + Util.toHexString(getValue()) + "\"";
	}
	
	public static DbDhcpOption fromConfigDhcpOption(
			com.jagornet.dhcp.core.option.base.DhcpOption configDhcpOption) throws IOException {
		DbDhcpOption dhcpOption = new DbDhcpOption();
		dhcpOption.setCode(configDhcpOption.getCode());
		dhcpOption.setValue(configDhcpOption.encode().array());
		return dhcpOption;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + code;
		result = prime * result + ((iaAddressId == null) ? 0 : iaAddressId.hashCode());
		result = prime * result + ((iaPrefixId == null) ? 0 : iaPrefixId.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((identityAssocId == null) ? 0 : identityAssocId.hashCode());
		result = prime * result + Arrays.hashCode(value);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DbDhcpOption other = (DbDhcpOption) obj;
		if (code != other.code)
			return false;
		if (iaAddressId == null) {
			if (other.iaAddressId != null)
				return false;
		} else if (!iaAddressId.equals(other.iaAddressId))
			return false;
		if (iaPrefixId == null) {
			if (other.iaPrefixId != null)
				return false;
		} else if (!iaPrefixId.equals(other.iaPrefixId))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (identityAssocId == null) {
			if (other.identityAssocId != null)
				return false;
		} else if (!identityAssocId.equals(other.identityAssocId))
			return false;
		return Arrays.equals(value, other.value);
	}
}
