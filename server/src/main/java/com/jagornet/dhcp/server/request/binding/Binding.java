/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file Binding.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.server.request.binding;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.BeanUtils;

import com.jagornet.dhcp.db.IaAddress;
import com.jagornet.dhcp.db.IdentityAssoc;
import com.jagornet.dhcp.server.config.DhcpLink;

/**
 * The Class Binding.  A wrapper class for a database IdentityAssoc object
 * with a reference to the associated configuration Link object.
 * 
 * @author A. Gregory Rabil
 */
public class Binding extends IdentityAssoc
{
	private IdentityAssoc origIa;
	private DhcpLink dhcpLink;
	
	/**
	 * Instantiates a new binding.
	 * 
	 * @param ia the ia
	 * @param link the link
	 */
	public Binding(IdentityAssoc ia, DhcpLink dhcpLink)
	{
		this.origIa = ia;	// save a reference to the original IA
		BeanUtils.copyProperties(ia, this);
		this.dhcpLink = dhcpLink;
	}
	
	/**
	 * Gets the link.
	 * 
	 * @return the link
	 */
	public DhcpLink getDhcpLink() {
		return dhcpLink;
	}

	/**
	 * Sets the link.
	 * 
	 * @param link the new link
	 */
	public void setDhcpLink(DhcpLink dhcpLink) {
		this.dhcpLink = dhcpLink;
	}

	/**
	 * Gets the binding objects.
	 * 
	 * @return the binding objects
	 */
//	@SuppressWarnings("unchecked")
	public Collection<BindingObject> getBindingObjects() {
//		return (Collection<BindingObject>)iaAddresses;
		// Manually convert from IaAddresses to BindingObjects
		// which is safe because *this* Binding holds either
		// BindingAddresses or BindingPrefixes, both of which
		// extend from IaAddress and implement BindingObject
		if (iaAddresses != null) {
			Collection<BindingObject> bindingObjs = new ArrayList<BindingObject>();
			for (IaAddress iaAddr : iaAddresses) {
				if (iaAddr instanceof BindingObject) {
					bindingObjs.add((BindingObject)iaAddr);
				}
			}
			return bindingObjs;
		}
		return null;
	}
	
//	@SuppressWarnings("unchecked")
	public void setBindingObjects(Collection<BindingObject> bindingObjs) {
//		this.setIaAddresses((Collection<? extends IaAddress>) bindingObjs);
		// Manually convert from BindingObjects to IaAddresses
		// which is safe because *this* Binding holds either
		// BindingAddresses or BindingPrefixes, both of which
		// extend from IaAddress and implement BindingObject
		if (bindingObjs != null) {
			Collection<IaAddress> iaAddrs = new ArrayList<IaAddress>();
			for (BindingObject bindingObj : bindingObjs) {
				if (bindingObj instanceof IaAddress) {
					iaAddrs.add((IaAddress)bindingObj);
				}
			}
			this.setIaAddresses(iaAddrs);
		}
	}
	
	/**
	 * Checks for changed.
	 * 
	 * @return true, if successful
	 */
	public boolean hasChanged() {
		return !this.equals(origIa);
	}
}
