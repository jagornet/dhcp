/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file IaAddress.java is part of Jagornet DHCP.
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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.jagornet.dhcp.util.Util;

/**
 * The IaAddress POJO class for the IAADDRESS database table.
 * 
 * @author A. Gregory Rabil
 */

public class IaAddress
{
	// states
	public static final byte ADVERTISED = 1;
	public static final byte COMMITTED = 2;
	public static final byte EXPIRED = 3;
	public static final byte RELEASED = 4;
	public static final byte DECLINED = 5;
	public static final byte STATIC = 6;

	protected Long id;	// the database-generated object ID
	protected InetAddress ipAddress;
	protected Date startTime;
	protected Date preferredEndTime;
	protected Date validEndTime;
	protected byte state;
	protected Long identityAssocId;
	protected Collection<DhcpOption> dhcpOptions;

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
	 * Gets the ip address.
	 * 
	 * @return the ip address
	 */
	public InetAddress getIpAddress() {
		return ipAddress;
	}
	
	/**
	 * Sets the ip address.
	 * 
	 * @param ipAddress the new ip address
	 */
	public void setIpAddress(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	/**
	 * Gets the start time.
	 * 
	 * @return the start time
	 */
	public Date getStartTime() {
		return startTime;
	}
	
	/**
	 * Sets the start time.
	 * 
	 * @param startTime the new start time
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	/**
	 * Gets the preferred end time.
	 * 
	 * @return the preferred end time
	 */
	public Date getPreferredEndTime() {
		return preferredEndTime;
	}
	
	/**
	 * Sets the preferred end time.
	 * 
	 * @param preferredEndTime the new preferred end time
	 */
	public void setPreferredEndTime(Date preferredEndTime) {
		this.preferredEndTime = preferredEndTime;
	}
	
	/**
	 * Gets the valid end time.
	 * 
	 * @return the valid end time
	 */
	public Date getValidEndTime() {
		return validEndTime;
	}
	
	/**
	 * Sets the valid end time.
	 * 
	 * @param validEndTime the new valid end time
	 */
	public void setValidEndTime(Date validEndTime) {
		this.validEndTime = validEndTime;
	}
	
	/**
	 * Gets the state.
	 * 
	 * @return the state
	 */
	public byte getState() {
		return state;
	}
	
	/**
	 * Sets the state.
	 * 
	 * @param state the new state
	 */
	public void setState(byte state) {
		this.state = state;
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
	 * Get a specific DHCP option.
	 * 
	 * @param code
	 * @return
	 */
	public DhcpOption getDhcpOption(int code)
	{
		if (dhcpOptions != null) {
			for (DhcpOption dhcpOption : dhcpOptions) {
				if (dhcpOption.getCode() == code)
					return dhcpOption;
			}
		}
		return null;
	}
	
	public void setDhcpOption(DhcpOption newOption)
	{
		if (dhcpOptions == null) {
			dhcpOptions = new ArrayList<DhcpOption>();
		}
		// first remove the option, if it exists
		for (DhcpOption dhcpOption : dhcpOptions) {
			if (dhcpOption.getCode() == newOption.getCode()) {
				dhcpOptions.remove(dhcpOption);
				break;
			}
		}
		dhcpOptions.add(newOption);
	}
	
	/**
	 * Gets the dhcp options.
	 * 
	 * @return the dhcp options
	 */
	public Collection<DhcpOption> getDhcpOptions() {
		return dhcpOptions;
	}
	
	/**
	 * Sets the dhcp options.
	 * 
	 * @param dhcpOptions the new dhcp options
	 */
	public void setDhcpOptions(Collection<DhcpOption> dhcpOptions) {
		this.dhcpOptions = dhcpOptions;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((identityAssocId == null) ? 0 : identityAssocId.hashCode());
		result = prime * result
				+ ((ipAddress == null) ? 0 : ipAddress.hashCode());
		result = prime
				* result
				+ ((preferredEndTime == null) ? 0 : preferredEndTime.hashCode());
		result = prime * result
				+ ((startTime == null) ? 0 : startTime.hashCode());
		result = prime * result + state;
		result = prime * result
				+ ((validEndTime == null) ? 0 : validEndTime.hashCode());
		return result;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		if (!(obj instanceof IaAddress))
			return false;
		IaAddress other = (IaAddress) obj;
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
		if (ipAddress == null) {
			if (other.ipAddress != null)
				return false;
		} else if (!ipAddress.equals(other.ipAddress))
			return false;
		if (preferredEndTime == null) {
			if (other.preferredEndTime != null)
				return false;
		} else if (!preferredEndTime.equals(other.preferredEndTime))
			return false;
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
			return false;
		if (state != other.state)
			return false;
		if (validEndTime == null) {
			if (other.validEndTime != null)
				return false;
		} else if (!validEndTime.equals(other.validEndTime))
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("IA_ADDR: ");
		sb.append(" ip=");
		sb.append(this.getIpAddress().getHostAddress());
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
				sb.append("\t\tIA_ADDR Option: ");
				sb.append(dhcpOption.toString());
			}
		}
		return sb.toString();
	}
	
	/**
	 * State to string.
	 *
	 * @return the string
	 */
	public static String stateToString(byte state)
	{
		String s = null;
		switch (state) {
			case ADVERTISED:
				s = "Advertised";
				break;
			case COMMITTED:
				s = "Committed";
				break;
			case EXPIRED:
				s = "Expired";
				break;
			case RELEASED:
				s = "Released";
				break;
			case DECLINED:
				s = "Declined";
				break;
			case STATIC:
				s = "Static";
				break;
			default:
				s = "Unknown";
				break;
		}
		return s;
	}
}
