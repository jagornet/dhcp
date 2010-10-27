/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpLink.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.server.config;

import com.jagornet.dhcpv6.option.DhcpConfigOptions;
import com.jagornet.dhcpv6.util.Subnet;
import com.jagornet.dhcpv6.xml.Link;

/**
 * The Class DhcpLink.  A wrapper class for the configured XML Link object
 * and the logical Subnet which represents that Link.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpLink
{
	
	/** The subnet. */
	private Subnet subnet;
	
	/** The link. */
	private Link link;
	
    private DhcpConfigOptions msgConfigOptions;
    private DhcpConfigOptions iaNaConfigOptions;
    private DhcpConfigOptions naAddrConfigOptions;
    private DhcpConfigOptions iaTaConfigOptions;
    private DhcpConfigOptions taAddrConfigOptions;
    private DhcpConfigOptions iaPdConfigOptions;
    private DhcpConfigOptions prefixConfigOptions;
	
	/**
	 * Instantiates a new dhcp link.
	 * 
	 * @param subnet the subnet
	 * @param link the link
	 */
	public DhcpLink(Subnet subnet, Link link)
	{
		this.subnet = subnet;
		this.link = link;
		msgConfigOptions = new DhcpConfigOptions(link.getMsgConfigOptions());
		iaNaConfigOptions = new DhcpConfigOptions(link.getIaNaConfigOptions());
		naAddrConfigOptions = new DhcpConfigOptions(link.getNaAddrConfigOptions());
		iaTaConfigOptions = new DhcpConfigOptions(link.getIaTaConfigOptions());
		taAddrConfigOptions = new DhcpConfigOptions(link.getTaAddrConfigOptions());
		iaPdConfigOptions = new DhcpConfigOptions(link.getIaPdConfigOptions());
		prefixConfigOptions = new DhcpConfigOptions(link.getPrefixConfigOptions());
	}

	/**
	 * Gets the subnet.
	 * 
	 * @return the subnet
	 */
	public Subnet getSubnet() {
		return subnet;
	}

	/**
	 * Sets the subnet.
	 * 
	 * @param subnet the new subnet
	 */
	public void setSubnet(Subnet subnet) {
		this.subnet = subnet;
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

	public DhcpConfigOptions getMsgConfigOptions() {
		return msgConfigOptions;
	}

	public void setMsgConfigOptions(DhcpConfigOptions msgConfigOptions) {
		this.msgConfigOptions = msgConfigOptions;
	}

	public DhcpConfigOptions getIaNaConfigOptions() {
		return iaNaConfigOptions;
	}

	public void setIaNaConfigOptions(DhcpConfigOptions iaNaConfigOptions) {
		this.iaNaConfigOptions = iaNaConfigOptions;
	}

	public DhcpConfigOptions getIaTaConfigOptions() {
		return iaTaConfigOptions;
	}

	public void setIaTaConfigOptions(DhcpConfigOptions iaTaConfigOptions) {
		this.iaTaConfigOptions = iaTaConfigOptions;
	}

	public DhcpConfigOptions getIaPdConfigOptions() {
		return iaPdConfigOptions;
	}

	public void setIaPdConfigOptions(DhcpConfigOptions iaPdConfigOptions) {
		this.iaPdConfigOptions = iaPdConfigOptions;
	}

	public DhcpConfigOptions getNaAddrConfigOptions() {
		return naAddrConfigOptions;
	}

	public void setNaAddrConfigOptions(DhcpConfigOptions naAddrConfigOptions) {
		this.naAddrConfigOptions = naAddrConfigOptions;
	}

	public DhcpConfigOptions getTaAddrConfigOptions() {
		return taAddrConfigOptions;
	}

	public void setTaAddrConfigOptions(DhcpConfigOptions taAddrConfigOptions) {
		this.taAddrConfigOptions = taAddrConfigOptions;
	}

	public DhcpConfigOptions getPrefixConfigOptions() {
		return prefixConfigOptions;
	}

	public void setPrefixConfigOptions(DhcpConfigOptions prefixConfigOptions) {
		this.prefixConfigOptions = prefixConfigOptions;
	}
	
}
