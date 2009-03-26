/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file OptionDefDTO.java is part of DHCPv6.
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
 * The Class OptionDefDTO.
 * 
 * @author A. Gregory Rabil
 */
public class OptionDefDTO extends BaseOptionDTO
{
	/** Default serial version id. */
	private static final long serialVersionUID = 1L;

	// this can only be ONE of the following DTO types
	
	/** The u byte option. */
	protected UByteOptionDTO uByteOption;
	
	/** The u short option. */
	protected UShortOptionDTO uShortOption;

	//	protected UShortListOptionDTO uShortListOption;
	
	/** The u int option. */
	protected UIntOptionDTO uIntOption;
	
	/** The string option. */
	protected StringOptionDTO stringOption;
	
	//	protected IpAddressOptionDTO ipAddressOption;
	
	/** The ip address list option. */
	protected IpAddressListOptionDTO ipAddressListOption;
	
	/** The domain name option. */
	protected DomainNameOptionDTO domainNameOption;
	
	/** The domain name list option. */
	protected DomainNameListOptionDTO domainNameListOption;
	
	/** The opaque data option. */
	protected OpaqueDataOptionDTO opaqueDataOption;
	
	//	protected OpaqueDataListOptionDTO opaqueDataListOption;
	
	
	/**
	 * Gets the u byte option.
	 * 
	 * @return the u byte option
	 */
	public UByteOptionDTO getUByteOption() {
		return uByteOption;
	}
	
	/**
	 * Sets the u byte option.
	 * 
	 * @param byteOption the new u byte option
	 */
	public void setUByteOption(UByteOptionDTO byteOption) {
		uByteOption = byteOption;
	}
	
	/**
	 * Gets the u short option.
	 * 
	 * @return the u short option
	 */
	public UShortOptionDTO getUShortOption() {
		return uShortOption;
	}
	
	/**
	 * Sets the u short option.
	 * 
	 * @param shortOption the new u short option
	 */
	public void setUShortOption(UShortOptionDTO shortOption) {
		uShortOption = shortOption;
	}
	
	/**
	 * Gets the u int option.
	 * 
	 * @return the u int option
	 */
	public UIntOptionDTO getUIntOption() {
		return uIntOption;
	}
	
	/**
	 * Sets the u int option.
	 * 
	 * @param intOption the new u int option
	 */
	public void setUIntOption(UIntOptionDTO intOption) {
		uIntOption = intOption;
	}
	
	/**
	 * Gets the string option.
	 * 
	 * @return the string option
	 */
	public StringOptionDTO getStringOption() {
		return stringOption;
	}
	
	/**
	 * Sets the string option.
	 * 
	 * @param stringOption the new string option
	 */
	public void setStringOption(StringOptionDTO stringOption) {
		this.stringOption = stringOption;
	}
	
	/**
	 * Gets the server ip list option.
	 * 
	 * @return the server ip list option
	 */
	public IpAddressListOptionDTO getServerIpListOption() {
		return ipAddressListOption;
	}
	
	/**
	 * Sets the server ip list option.
	 * 
	 * @param serverIpListOption the new server ip list option
	 */
	public void setServerIpListOption(IpAddressListOptionDTO serverIpListOption) {
		this.ipAddressListOption = serverIpListOption;
	}
	
	/**
	 * Gets the domain name option.
	 * 
	 * @return the domain name option
	 */
	public DomainNameOptionDTO getDomainNameOption() {
		return domainNameOption;
	}
	
	/**
	 * Sets the domain name option.
	 * 
	 * @param domainNameOption the new domain name option
	 */
	public void setDomainNameOption(DomainNameOptionDTO domainNameOption) {
		this.domainNameOption = domainNameOption;
	}
	
	/**
	 * Gets the domain name list option.
	 * 
	 * @return the domain name list option
	 */
	public DomainNameListOptionDTO getDomainNameListOption() {
		return domainNameListOption;
	}
	
	/**
	 * Sets the domain name list option.
	 * 
	 * @param domainNameListOption the new domain name list option
	 */
	public void setDomainNameListOption(DomainNameListOptionDTO domainNameListOption) {
		this.domainNameListOption = domainNameListOption;
	}
	
	/**
	 * Gets the opaque data option.
	 * 
	 * @return the opaque data option
	 */
	public OpaqueDataOptionDTO getOpaqueDataOption() {
		return opaqueDataOption;
	}
	
	/**
	 * Sets the opaque data option.
	 * 
	 * @param opaqueDataOption the new opaque data option
	 */
	public void setOpaqueDataOption(OpaqueDataOptionDTO opaqueDataOption) {
		this.opaqueDataOption = opaqueDataOption;
	}
}
