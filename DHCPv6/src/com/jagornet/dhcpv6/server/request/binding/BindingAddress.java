/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BindingAddress.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.server.request.binding;

import org.springframework.beans.BeanUtils;

import com.jagornet.dhcpv6.db.IaAddress;

/**
 * The Class BindingAddress.
 */
public class BindingAddress extends IaAddress implements BindingObject
{
	
	/** The orig ia addr. */
	private IaAddress origIaAddr;
	
	/** The binding pool. */
	private AddressBindingPool bindingPool;
	
	/**
	 * Instantiates a new binding address.
	 * 
	 * @param iaAddr the ia addr
	 * @param bindingPool the binding pool
	 */
	public BindingAddress(IaAddress iaAddr, AddressBindingPool bindingPool)
	{
		this.origIaAddr = iaAddr;	// save a reference to the original IaAddress
		BeanUtils.copyProperties(iaAddr, this);
		this.bindingPool = bindingPool;
	}
	
	/**
	 * Gets the binding pool.
	 * 
	 * @return the binding pool
	 */
	public BindingPool getBindingPool() {
		return bindingPool;
	}

	/**
	 * Sets the binding pool.
	 * 
	 * @param bindingPool the new binding pool
	 */
	public void setBindingPool(AddressBindingPool bindingPool) {
		this.bindingPool = bindingPool;
	}
	
	/**
	 * Checks for changed.
	 * 
	 * @return true, if successful
	 */
	public boolean hasChanged() {
		return !this.equals(origIaAddr);
	}
}
