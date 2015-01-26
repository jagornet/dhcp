/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file IaPrefix.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.db;

import java.util.Collection;

import com.jagornet.dhcp.util.Util;


/**
 * The IaPrefix POJO class for the IAPREFIX database table.
 * Note that this is modeled as an IaAddress with a prefixLength
 * 
 * @author A. Gregory Rabil
 */

public class IaPrefix extends IaAddress
{
	protected short prefixLength;
	
	/**
	 * Gets the prefix length.
	 *
	 * @return the prefix length
	 */
	public short getPrefixLength() {
		return prefixLength;
	}

	/**
	 * Sets the prefix length.
	 *
	 * @param prefixLength the new prefix length
	 */
	public void setPrefixLength(short prefixLength) {
		this.prefixLength = prefixLength;
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaAddress#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + prefixLength;
		return result;
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaAddress#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		if (!(obj instanceof IaPrefix))
			return false;
		IaPrefix other = (IaPrefix) obj;
		if (prefixLength != other.prefixLength)
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IaAddress#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("\tIA_PREFIX: ");
		sb.append(" ip=");
		sb.append(this.getIpAddress().getHostAddress());
		sb.append(" prefixLength=");
		sb.append(this.getPrefixLength());
		sb.append(" state=");
		sb.append(this.getState() + "(" + stateToString(this.getState()) + ")");
		sb.append(" startTime=");
		if (this.getStartTime() != null)
			sb.append(Util.GMT_DATEFORMAT.format(this.getStartTime()));
		sb.append(" preferredEndTime=");
		if (this.getPreferredEndTime() != null)
			if (this.getPreferredEndTime().getTime() < 0)
				sb.append("infinite");
			else
				sb.append(Util.GMT_DATEFORMAT.format(this.getPreferredEndTime()));
		sb.append(" validEndTime=");
		if (this.getValidEndTime() != null)
			if (this.getValidEndTime().getTime() < 0)
				sb.append("infinite");
			else
				sb.append(Util.GMT_DATEFORMAT.format(this.getValidEndTime()));
		Collection<DhcpOption> opts = this.getDhcpOptions();
		if (opts != null) {
			for (DhcpOption dhcpOption : opts) {
				sb.append(Util.LINE_SEPARATOR);
				sb.append("\t\tIA_PREFIX Option: ");
				sb.append(dhcpOption.toString());
			}
		}
		return sb.toString();
	}
}
