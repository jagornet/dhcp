/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpLink.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.server.config;

import com.jagornet.dhcp.option.v4.DhcpV4ConfigOptions;
import com.jagornet.dhcp.option.v6.DhcpV6ConfigOptions;
import com.jagornet.dhcp.util.Subnet;
import com.jagornet.dhcp.xml.Link;

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
	
    private DhcpV6ConfigOptions msgConfigOptions;
    private DhcpV6ConfigOptions iaNaConfigOptions;
    private DhcpV6ConfigOptions naAddrConfigOptions;
    private DhcpV6ConfigOptions iaTaConfigOptions;
    private DhcpV6ConfigOptions taAddrConfigOptions;
    private DhcpV6ConfigOptions iaPdConfigOptions;
    private DhcpV6ConfigOptions prefixConfigOptions;
    private DhcpV4ConfigOptions v4ConfigOptions;
	
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
		msgConfigOptions = new DhcpV6ConfigOptions(link.getV6MsgConfigOptions());
		iaNaConfigOptions = new DhcpV6ConfigOptions(link.getV6IaNaConfigOptions());
		naAddrConfigOptions = new DhcpV6ConfigOptions(link.getV6NaAddrConfigOptions());
		iaTaConfigOptions = new DhcpV6ConfigOptions(link.getV6IaTaConfigOptions());
		taAddrConfigOptions = new DhcpV6ConfigOptions(link.getV6TaAddrConfigOptions());
		iaPdConfigOptions = new DhcpV6ConfigOptions(link.getV6IaPdConfigOptions());
		prefixConfigOptions = new DhcpV6ConfigOptions(link.getV6PrefixConfigOptions());
		v4ConfigOptions = new DhcpV4ConfigOptions(link.getV4ConfigOptions());
	}
	
	/**
	 * Convenience method to get the XML Link object's address element.
	 */
	public String getLinkAddress() {
		return link.getAddress();
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

	public DhcpV6ConfigOptions getMsgConfigOptions() {
		return msgConfigOptions;
	}

	public void setMsgConfigOptions(DhcpV6ConfigOptions msgConfigOptions) {
		this.msgConfigOptions = msgConfigOptions;
	}

	public DhcpV6ConfigOptions getIaNaConfigOptions() {
		return iaNaConfigOptions;
	}

	public void setIaNaConfigOptions(DhcpV6ConfigOptions iaNaConfigOptions) {
		this.iaNaConfigOptions = iaNaConfigOptions;
	}

	public DhcpV6ConfigOptions getIaTaConfigOptions() {
		return iaTaConfigOptions;
	}

	public void setIaTaConfigOptions(DhcpV6ConfigOptions iaTaConfigOptions) {
		this.iaTaConfigOptions = iaTaConfigOptions;
	}

	public DhcpV6ConfigOptions getIaPdConfigOptions() {
		return iaPdConfigOptions;
	}

	public void setIaPdConfigOptions(DhcpV6ConfigOptions iaPdConfigOptions) {
		this.iaPdConfigOptions = iaPdConfigOptions;
	}

	public DhcpV6ConfigOptions getNaAddrConfigOptions() {
		return naAddrConfigOptions;
	}

	public void setNaAddrConfigOptions(DhcpV6ConfigOptions naAddrConfigOptions) {
		this.naAddrConfigOptions = naAddrConfigOptions;
	}

	public DhcpV6ConfigOptions getTaAddrConfigOptions() {
		return taAddrConfigOptions;
	}

	public void setTaAddrConfigOptions(DhcpV6ConfigOptions taAddrConfigOptions) {
		this.taAddrConfigOptions = taAddrConfigOptions;
	}

	public DhcpV6ConfigOptions getPrefixConfigOptions() {
		return prefixConfigOptions;
	}

	public void setPrefixConfigOptions(DhcpV6ConfigOptions prefixConfigOptions) {
		this.prefixConfigOptions = prefixConfigOptions;
	}

	public DhcpV4ConfigOptions getV4ConfigOptions() {
		return v4ConfigOptions;
	}

	public void setV4ConfigOptions(DhcpV4ConfigOptions v4ConfigOptions) {
		this.v4ConfigOptions = v4ConfigOptions;
	}
	
}
