/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file IdentityAssoc.java is part of Jagornet DHCP.
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

import java.util.Arrays;
import java.util.Collection;

import com.jagornet.dhcp.util.Util;

/**
 * The IdentityAssoc POJO class for the IDENTITYASSOC database table.
 * 
 * @author A. Gregory Rabil
 */

public class IdentityAssoc
{
	// iatypes
	public static final byte V4_TYPE = 0;
	public static final byte NA_TYPE = 1;
	public static final byte TA_TYPE = 2;
	public static final byte PD_TYPE = 3;
	
	// states
	public static final byte ADVERTISED = IaAddress.ADVERTISED;
	public static final byte COMMITTED = IaAddress.COMMITTED;
	public static final byte EXPIRED = IaAddress.EXPIRED;
	
	protected Long id;	// the database-generated object ID
	protected byte[] duid;
	protected byte iatype;
	protected long iaid;		// need long to hold 32-bit unsigned integer
	protected byte state;
	protected Collection<? extends IaAddress> iaAddresses;
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
	 * Gets the ia addresses.
	 * 
	 * @return the ia addresses
	 */
	public Collection<? extends IaAddress> getIaAddresses() {
		return iaAddresses;
	}
	
	/**
	 * Sets the ia addresses.
	 * 
	 * @param iaAddresses the new ia addresses
	 */
	public void setIaAddresses(Collection<? extends IaAddress> iaAddresses) {
		this.iaAddresses = iaAddresses;
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
		result = prime * result + Arrays.hashCode(duid);
		result = prime * result + (int) (iaid ^ (iaid >>> 32));
		result = prime * result + iatype;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + state;
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
		if (!(obj instanceof IdentityAssoc))
			return false;
		IdentityAssoc other = (IdentityAssoc) obj;
		if (!Arrays.equals(duid, other.duid))
			return false;
		if (iaid != other.iaid)
			return false;
		if (iatype != other.iatype)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (state != other.state)
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(Util.LINE_SEPARATOR);
		sb.append(keyToString(this.getDuid(), this.getIatype(), this.getIaid()));
		sb.append(" state=");
		sb.append(this.getState());
		sb.append('(');
		sb.append(IaAddress.stateToString(this.getState()));
		sb.append(')');
		Collection<? extends IaAddress> iaAddrs = this.getIaAddresses();
		if (iaAddrs != null) {
			for (IaAddress iaAddr : iaAddrs) {
				sb.append(Util.LINE_SEPARATOR);
				sb.append('\t');
				sb.append(iaAddr.toString());
			}
		}
		Collection<DhcpOption> opts = this.getDhcpOptions();
		if (opts != null) {
			for (DhcpOption dhcpOption : opts) {
				sb.append(Util.LINE_SEPARATOR);
				sb.append("\tIA Option: ");
				sb.append(dhcpOption.toString());
			}
		}
		return sb.toString();
	}
	
	public static String keyToString(byte[] _duid, byte _iatype, long _iaid)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("IA: duid=");
		sb.append(Util.toHexString(_duid));
		sb.append(" iatype=");
		sb.append(_iatype);
		sb.append('(');
		sb.append(iaTypeToString(_iatype));
		sb.append(')');
		sb.append(" iaid=");
		sb.append(_iaid);
		return sb.toString();
	}
	
	public static String iaTypeToString(byte _iatype) {
		String s = null;
		switch (_iatype) {
			case V4_TYPE:
				s = "V4";
				break;
			case NA_TYPE:
				s = "NA";
				break;
			case TA_TYPE:
				s = "TA";
				break;
			case PD_TYPE:
				s = "PD";
				break;
			default:
				s = "??";
				break;
		}
		return s;
	}
}
