/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpLease.java is part of Jagornet DHCP.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import com.jagornet.dhcp.util.Util;

/**
 * The IaAddress POJO class for the DHCPLEASE database table.
 * 
 * @author A. Gregory Rabil
 */

public class DhcpLease
{
	protected InetAddress ipAddress;
	protected byte[] duid;
	protected byte iatype;
	protected long iaid;		// need long to hold 32-bit unsigned integer
	protected short prefixLength;
	protected byte state;
	protected Date startTime;
	protected Date preferredEndTime;
	protected Date validEndTime;
	protected Collection<DhcpOption> iaDhcpOptions;
	protected Collection<DhcpOption> iaAddrDhcpOptions;
	
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
	 * Gets the duid.
	 *
	 * @return the duid
	 */
	public byte[] getDuid() {
		return duid;
	}


	/**
	 * Sets the duid.
	 *
	 * @param duid the new duid
	 */
	public void setDuid(byte[] duid) {
		this.duid = duid;
	}


	/**
	 * Gets the iatype.
	 *
	 * @return the iatype
	 */
	public byte getIatype() {
		return iatype;
	}


	/**
	 * Sets the iatype.
	 *
	 * @param iatype the new iatype
	 */
	public void setIatype(byte iatype) {
		this.iatype = iatype;
	}


	/**
	 * Gets the iaid.
	 *
	 * @return the iaid
	 */
	public long getIaid() {
		return iaid;
	}


	/**
	 * Sets the iaid.
	 *
	 * @param iaid the new iaid
	 */
	public void setIaid(long iaid) {
		this.iaid = iaid;
	}

	
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
	 * Gets the ia dhcp options.
	 *
	 * @return the ia dhcp options
	 */
	public Collection<DhcpOption> getIaDhcpOptions() {
		return iaDhcpOptions;
	}


	/**
	 * Sets the ia dhcp options.
	 *
	 * @param iaDhcpOptions the new ia dhcp options
	 */
	public void setIaDhcpOptions(Collection<DhcpOption> iaDhcpOptions) {
		this.iaDhcpOptions = iaDhcpOptions;
	}
	
	/**
	 * Adds the ia dhcp option.
	 *
	 * @param iaDhcpOption the ia dhcp option
	 */
	public void addIaDhcpOption(DhcpOption iaDhcpOption) {
		if (iaDhcpOptions == null) {			
			//TODO: consider a Set?
			iaDhcpOptions = new ArrayList<DhcpOption>();
		}
		iaDhcpOptions.add(iaDhcpOption);
	}


	/**
	 * Gets the ia addr dhcp options.
	 *
	 * @return the ia addr dhcp options
	 */
	public Collection<DhcpOption> getIaAddrDhcpOptions() {
		return iaAddrDhcpOptions;
	}


	/**
	 * Sets the ia addr dhcp options.
	 *
	 * @param iaAddrDhcpOptions the new ia addr dhcp options
	 */
	public void setIaAddrDhcpOptions(Collection<DhcpOption> iaAddrDhcpOptions) {
		this.iaAddrDhcpOptions = iaAddrDhcpOptions;
	}

	/**
	 * Adds the ia addr dhcp option.
	 *
	 * @param iaDhcpOption the ia dhcp option
	 */
	public void addIaAddrDhcpOption(DhcpOption iaDhcpOption) {
		if (iaAddrDhcpOptions == null) {			
			//TODO: consider a Set?
			iaAddrDhcpOptions = new ArrayList<DhcpOption>();
		}
		iaAddrDhcpOptions.add(iaDhcpOption);
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(duid);
		result = prime
				* result
				+ ((iaAddrDhcpOptions == null) ? 0 : iaAddrDhcpOptions
						.hashCode());
		result = prime * result
				+ ((iaDhcpOptions == null) ? 0 : iaDhcpOptions.hashCode());
		result = prime * result + (int) (iaid ^ (iaid >>> 32));
		result = prime * result + iatype;
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
		DhcpLease other = (DhcpLease) obj;
		if (!Arrays.equals(duid, other.duid))
			return false;
		if (iaAddrDhcpOptions == null) {
			if (other.iaAddrDhcpOptions != null)
				return false;
		} else if (!iaAddrDhcpOptions.equals(other.iaAddrDhcpOptions))
			return false;
		if (iaDhcpOptions == null) {
			if (other.iaDhcpOptions != null)
				return false;
		} else if (!iaDhcpOptions.equals(other.iaDhcpOptions))
			return false;
		if (iaid != other.iaid)
			return false;
		if (iatype != other.iatype)
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
	public String toString() {
		return "DhcpLease [ipAddress=" + ipAddress + 
				", duid=" + Arrays.toString(duid) + 
				", iatype=" + iatype + ", iaid=" + iaid + ", state=" + state + 
				", startTime=" + Util.GMT_DATEFORMAT.format(startTime) +
				", preferredEndTime=" + Util.GMT_DATEFORMAT.format(preferredEndTime) + 
				", validEndTime=" + Util.GMT_DATEFORMAT.format(validEndTime) + 
				", iaDhcpOptions=" + iaDhcpOptions + 
				", iaAddrDhcpOptions=" + iaAddrDhcpOptions + "]";
	}
}
