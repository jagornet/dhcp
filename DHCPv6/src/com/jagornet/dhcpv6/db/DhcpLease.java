/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file IaAddress.java is part of DHCPv6.
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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

/**
 * The IaAddress POJO class for the DHCPLEASE database table.
 * 
 * @author A. Gregory Rabil
 */

public class DhcpLease
{
	// iatypes
	public static final byte V4_TYPE = 0;
	
	/** The Constant NA_TYPE. */
	public static final byte NA_TYPE = 1;
	
	/** The Constant TA_TYPE. */
	public static final byte TA_TYPE = 2;
	
	/** The Constant PD_TYPE. */
	public static final byte PD_TYPE = 3;

	// states
	/** The Constant ADVERTISED. */
	public static final byte ADVERTISED = 1;
	
	/** The Constant COMMITTED. */
	public static final byte COMMITTED = 2;
	
	/** The Constant EXPIRED. */
	public static final byte EXPIRED = 3;
	
	/** The Constant RELEASED. */
	public static final byte RELEASED = 4;
	
	/** The Constant DECLINED. */
	public static final byte DECLINED = 5;

	/** The ip address. */
	protected InetAddress ipAddress;
	
	/** The duid. */
	protected byte[] duid;
	
	/** The iatype. */
	protected byte iatype;
	
	/** The iaid. */
	protected long iaid;		// need long to hold 32-bit unsigned integer

	protected short prefixLength;
	
	/** The state. */
	protected byte state;
	
	/** The start time. */
	protected Date startTime;
	
	/** The preferred end time. */
	protected Date preferredEndTime;
	
	/** The valid end time. */
	protected Date validEndTime;
	
	/** The dhcp options. */
	protected Collection<DhcpOption> iaDhcpOptions;
	protected Collection<DhcpOption> iaAddrDhcpOptions;
	
	public InetAddress getIpAddress() {
		return ipAddress;
	}


	public void setIpAddress(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}


	public byte[] getDuid() {
		return duid;
	}


	public void setDuid(byte[] duid) {
		this.duid = duid;
	}


	public byte getIatype() {
		return iatype;
	}


	public void setIatype(byte iatype) {
		this.iatype = iatype;
	}


	public long getIaid() {
		return iaid;
	}


	public void setIaid(long iaid) {
		this.iaid = iaid;
	}

	
	public short getPrefixLength() {
		return prefixLength;
	}

	public void setPrefixLength(short prefixLength) {
		this.prefixLength = prefixLength;
	}


	public byte getState() {
		return state;
	}


	public void setState(byte state) {
		this.state = state;
	}


	public Date getStartTime() {
		return startTime;
	}


	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}


	public Date getPreferredEndTime() {
		return preferredEndTime;
	}


	public void setPreferredEndTime(Date preferredEndTime) {
		this.preferredEndTime = preferredEndTime;
	}


	public Date getValidEndTime() {
		return validEndTime;
	}


	public void setValidEndTime(Date validEndTime) {
		this.validEndTime = validEndTime;
	}


	public Collection<DhcpOption> getIaDhcpOptions() {
		return iaDhcpOptions;
	}


	public void setIaDhcpOptions(Collection<DhcpOption> iaDhcpOptions) {
		this.iaDhcpOptions = iaDhcpOptions;
	}
	
	public void addIaDhcpOption(DhcpOption iaDhcpOption) {
		if (iaDhcpOptions == null) {			
			//TODO: consider a Set?
			iaDhcpOptions = new ArrayList<DhcpOption>();
		}
		iaDhcpOptions.add(iaDhcpOption);
	}


	public Collection<DhcpOption> getIaAddrDhcpOptions() {
		return iaAddrDhcpOptions;
	}


	public void setIaAddrDhcpOptions(Collection<DhcpOption> iaAddrDhcpOptions) {
		this.iaAddrDhcpOptions = iaAddrDhcpOptions;
	}

	public void addIaAddrDhcpOption(DhcpOption iaDhcpOption) {
		if (iaAddrDhcpOptions == null) {			
			//TODO: consider a Set?
			iaAddrDhcpOptions = new ArrayList<DhcpOption>();
		}
		iaAddrDhcpOptions.add(iaDhcpOption);
	}

	
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

	@Override
	public String toString() {
		return "DhcpLease [ipAddress=" + ipAddress + ", duid="
				+ Arrays.toString(duid) + ", iatype=" + iatype + ", iaid="
				+ iaid + ", state=" + state + ", startTime=" + startTime
				+ ", preferredEndTime=" + preferredEndTime + ", validEndTime="
				+ validEndTime + ", iaDhcpOptions=" + iaDhcpOptions
				+ ", iaAddrDhcpOptions=" + iaAddrDhcpOptions + "]";
	}
	
	public String stateToString()
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
			default:
				s = "Unknown";
				break;
		}
		return s;
	}
}
