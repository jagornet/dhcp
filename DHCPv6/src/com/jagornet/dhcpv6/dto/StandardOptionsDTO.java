/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file StandardOptionsDTO.java is part of DHCPv6.
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

/*
 *   This file StandardOptionsDTO.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.dto;

import java.io.Serializable;

/**
 * The Class StandardOptionsDTO.
 * 
 * @author A. Gregory Rabil
 */
public class StandardOptionsDTO implements Serializable 
{	
	/**
	 * Default serial version id.
	 */
	private static final long serialVersionUID = 1L;
    
    /** The preference option. */
    protected PreferenceOptionDTO preferenceOption;
    
    /** The info refresh time option. */
    protected InfoRefreshTimeOptionDTO infoRefreshTimeOption;
    
    //    protected ServerUnicastOption serverUnicastOption;
    //TODO    protected StatusCodeOption statusCodeOption;
    //TODO    protected VendorInfoOption vendorInfoOption;
    
    /** The dns servers option. */
    protected DnsServersOptionDTO dnsServersOption;
    
    /** The domain search list option. */
    protected DomainSearchListOptionDTO domainSearchListOption;
    
    /** The sip server addresses option. */
    protected SipServerAddressesOptionDTO sipServerAddressesOption;
    
    /** The sip server domain names option. */
    protected SipServerDomainNamesOptionDTO sipServerDomainNamesOption;
    
    /** The nis servers option. */
    protected NisServersOptionDTO nisServersOption;
    
    /** The nis domain name option. */
    protected NisDomainNameOptionDTO nisDomainNameOption;
    
    /** The nis plus servers option. */
    protected NisPlusServersOptionDTO nisPlusServersOption;
    
    /** The nis plus domain name option. */
    protected NisPlusDomainNameOptionDTO nisPlusDomainNameOption;
    
    /** The sntp servers option. */
    protected SntpServersOptionDTO sntpServersOption;

	/**
	 * Gets the preference option.
	 * 
	 * @return the preference option
	 */
	public PreferenceOptionDTO getPreferenceOption() {
		return preferenceOption;
	}

	/**
	 * Sets the preference option.
	 * 
	 * @param preferenceOption the new preference option
	 */
	public void setPreferenceOption(PreferenceOptionDTO preferenceOption) {
		this.preferenceOption = preferenceOption;
	}

	/**
	 * Gets the info refresh time option.
	 * 
	 * @return the info refresh time option
	 */
	public InfoRefreshTimeOptionDTO getInfoRefreshTimeOption() {
		return infoRefreshTimeOption;
	}

	/**
	 * Sets the info refresh time option.
	 * 
	 * @param infoRefreshTimeOption the new info refresh time option
	 */
	public void setInfoRefreshTimeOption(
			InfoRefreshTimeOptionDTO infoRefreshTimeOption) {
		this.infoRefreshTimeOption = infoRefreshTimeOption;
	}

	/**
	 * Gets the dns servers option.
	 * 
	 * @return the dns servers option
	 */
	public DnsServersOptionDTO getDnsServersOption() {
		return dnsServersOption;
	}

	/**
	 * Sets the dns servers option.
	 * 
	 * @param dnsServersOption the new dns servers option
	 */
	public void setDnsServersOption(DnsServersOptionDTO dnsServersOption) {
		this.dnsServersOption = dnsServersOption;
	}

	/**
	 * Gets the domain search list option.
	 * 
	 * @return the domain search list option
	 */
	public DomainSearchListOptionDTO getDomainSearchListOption() {
		return domainSearchListOption;
	}

	/**
	 * Sets the domain search list option.
	 * 
	 * @param domainSearchListOption the new domain search list option
	 */
	public void setDomainSearchListOption(
			DomainSearchListOptionDTO domainSearchListOption) {
		this.domainSearchListOption = domainSearchListOption;
	}

	/**
	 * Gets the sip server addresses option.
	 * 
	 * @return the sip server addresses option
	 */
	public SipServerAddressesOptionDTO getSipServerAddressesOption() {
		return sipServerAddressesOption;
	}

	/**
	 * Sets the sip server addresses option.
	 * 
	 * @param sipServerAddressesOption the new sip server addresses option
	 */
	public void setSipServerAddressesOption(
			SipServerAddressesOptionDTO sipServerAddressesOption) {
		this.sipServerAddressesOption = sipServerAddressesOption;
	}

	/**
	 * Gets the sip server domain names option.
	 * 
	 * @return the sip server domain names option
	 */
	public SipServerDomainNamesOptionDTO getSipServerDomainNamesOption() {
		return sipServerDomainNamesOption;
	}

	/**
	 * Sets the sip server domain names option.
	 * 
	 * @param sipServerDomainNamesOption the new sip server domain names option
	 */
	public void setSipServerDomainNamesOption(
			SipServerDomainNamesOptionDTO sipServerDomainNamesOption) {
		this.sipServerDomainNamesOption = sipServerDomainNamesOption;
	}

	/**
	 * Gets the nis servers option.
	 * 
	 * @return the nis servers option
	 */
	public NisServersOptionDTO getNisServersOption() {
		return nisServersOption;
	}

	/**
	 * Sets the nis servers option.
	 * 
	 * @param nisServersOption the new nis servers option
	 */
	public void setNisServersOption(NisServersOptionDTO nisServersOption) {
		this.nisServersOption = nisServersOption;
	}

	/**
	 * Gets the nis domain name option.
	 * 
	 * @return the nis domain name option
	 */
	public NisDomainNameOptionDTO getNisDomainNameOption() {
		return nisDomainNameOption;
	}

	/**
	 * Sets the nis domain name option.
	 * 
	 * @param nisDomainNameOption the new nis domain name option
	 */
	public void setNisDomainNameOption(NisDomainNameOptionDTO nisDomainNameOption) {
		this.nisDomainNameOption = nisDomainNameOption;
	}

	/**
	 * Gets the nis plus servers option.
	 * 
	 * @return the nis plus servers option
	 */
	public NisPlusServersOptionDTO getNisPlusServersOption() {
		return nisPlusServersOption;
	}

	/**
	 * Sets the nis plus servers option.
	 * 
	 * @param nisPlusServersOption the new nis plus servers option
	 */
	public void setNisPlusServersOption(NisPlusServersOptionDTO nisPlusServersOption) {
		this.nisPlusServersOption = nisPlusServersOption;
	}

	/**
	 * Gets the nis plus domain name option.
	 * 
	 * @return the nis plus domain name option
	 */
	public NisPlusDomainNameOptionDTO getNisPlusDomainNameOption() {
		return nisPlusDomainNameOption;
	}

	/**
	 * Sets the nis plus domain name option.
	 * 
	 * @param nisPlusDomainNameOption the new nis plus domain name option
	 */
	public void setNisPlusDomainNameOption(
			NisPlusDomainNameOptionDTO nisPlusDomainNameOption) {
		this.nisPlusDomainNameOption = nisPlusDomainNameOption;
	}

	/**
	 * Gets the sntp servers option.
	 * 
	 * @return the sntp servers option
	 */
	public SntpServersOptionDTO getSntpServersOption() {
		return sntpServersOption;
	}

	/**
	 * Sets the sntp servers option.
	 * 
	 * @param sntpServersOption the new sntp servers option
	 */
	public void setSntpServersOption(SntpServersOptionDTO sntpServersOption) {
		this.sntpServersOption = sntpServersOption;
	}

}
