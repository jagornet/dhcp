/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file Binding.java is part of DHCPv6.
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

import java.util.Collection;

import org.springframework.beans.BeanUtils;

import com.jagornet.dhcpv6.db.DhcpOption;
import com.jagornet.dhcpv6.db.IaAddress;
import com.jagornet.dhcpv6.db.IdentityAssoc;
import com.jagornet.dhcpv6.util.Util;
import com.jagornet.dhcpv6.xml.Link;

// TODO: Auto-generated Javadoc
/**
 * The Class Binding.
 */
public class Binding extends IdentityAssoc
{
	
	/** The orig ia. */
	private IdentityAssoc origIa;
	
	/** The link. */
	private Link link;
	
	/**
	 * Instantiates a new binding.
	 * 
	 * @param ia the ia
	 * @param link the link
	 */
	public Binding(IdentityAssoc ia, Link link)
	{
		this.origIa = ia;	// save a reference to the original IA
		BeanUtils.copyProperties(ia, this);
		this.link = link;
	}
	
	/**
	 * Gets the link.
	 * 
	 * @return the link
	 */
	public Link getLink() {
		return link;
	}

	/**
	 * Sets the link.
	 * 
	 * @param link the new link
	 */
	public void setLink(Link link) {
		this.link = link;
	}

	/**
	 * Gets the binding objects.
	 * 
	 * @return the binding objects
	 */
	@SuppressWarnings("unchecked")
	public Collection<BindingObject> getBindingObjects() {
		return (Collection<BindingObject>)iaAddresses;
	}
	
	@SuppressWarnings("unchecked")
	public void setBindingObjects(Collection<BindingObject> bindingObjs) {
		this.setIaAddresses((Collection<? extends IaAddress>) bindingObjs);
	}
	
	/**
	 * Checks for changed.
	 * 
	 * @return true, if successful
	 */
	public boolean hasChanged() {
		return !this.equals(origIa);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuilder sb = new StringBuilder(Util.LINE_SEPARATOR);
		sb.append("Binding(IA): ");
		sb.append(" duid=");
		sb.append(Util.toHexString(this.getDuid()));
		sb.append(" iaid=");
		sb.append(this.getIaid());
		sb.append(" state=");
		sb.append(this.getState());
		Collection<? extends IaAddress> ips = this.getIaAddresses();
		if (ips != null) {
			for (IaAddress ipAddress : ips) {
				sb.append(Util.LINE_SEPARATOR);
				sb.append("\tIA_ADDR: ");
				sb.append(" ip=");
				sb.append(ipAddress.getIpAddress().getHostAddress());
				sb.append(" startTime=");
				sb.append(ipAddress.getStartTime());
				sb.append(" preferredEndTime=");
				sb.append(ipAddress.getPreferredEndTime());
				sb.append(" validEndTime=");
				sb.append(ipAddress.getValidEndTime());
				Collection<DhcpOption> opts = ipAddress.getDhcpOptions();
				if (opts != null) {
					for (DhcpOption dhcpOption : opts) {
						sb.append(Util.LINE_SEPARATOR);
						sb.append("\t\tIA_ADDR Option: ");
						sb.append(" code=");
						sb.append(dhcpOption.getCode());
						sb.append(" value=");
						sb.append(Util.toHexString(dhcpOption.getValue()));
					}
				}
			}
		}
		Collection<DhcpOption> opts = this.getDhcpOptions();
		if (opts != null) {
			for (DhcpOption dhcpOption : opts) {
				sb.append(Util.LINE_SEPARATOR);
				sb.append("\tIA Option: ");
				sb.append(" code=");
				sb.append(dhcpOption.getCode());
				sb.append(" value=");
				sb.append(Util.toHexString(dhcpOption.getValue()));
			}
		}
		return sb.toString();
	}
}
