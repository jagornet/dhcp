/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file OpaqueDataOptionDTO.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.dto.option.base;

/**
 * The Class OpaqueDataOptionDTO.
 * 
 * @author A. Gregory Rabil
 */
public class OpaqueDataOptionDTO extends BaseOptionDTO
{
	/**
	 * Default serial version id.
	 */
	private static final long serialVersionUID = 1L;
	
	/** The opaqueData. */
	protected OpaqueDataDTO opaqueData;

	/**
	 * Gets the opaqueData.
	 * 
	 * @return the opaqueData
	 */
	public OpaqueDataDTO getOpaqueData() {
		return opaqueData;
	}

	/**
	 * Sets the opaqueData.
	 * 
	 * @param opaqueData the new opaqueData
	 */
	public void setOpaqueData(OpaqueDataDTO opaqueData) {
		this.opaqueData = opaqueData;
	}
}
