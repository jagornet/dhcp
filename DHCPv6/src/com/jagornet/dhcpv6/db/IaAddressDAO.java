/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file IaAddressDAO.java is part of DHCPv6.
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
import java.util.Date;
import java.util.List;

import com.jagornet.dhcpv6.server.request.binding.Range;
import com.jagornet.dhcpv6.util.Subnet;

// TODO: Auto-generated Javadoc
/**
 * The Interface IaAddressDAO.
 */
public interface IaAddressDAO
{
	
	/**
	 * Creates the.
	 * 
	 * @param iaAddr the ia addr
	 */
	public void create(IaAddress iaAddr);
	
	/**
	 * Update.
	 * 
	 * @param iaAddr the ia addr
	 */
	public void update(IaAddress iaAddr);
	
	/**
	 * Gets the by id.
	 * 
	 * @param id the id
	 * 
	 * @return the by id
	 */
	public IaAddress getById(Long id);
	
	/**
	 * Gets the by inet address.
	 * 
	 * @param inetAddr the inet addr
	 * 
	 * @return the by inet address
	 */
	public IaAddress getByInetAddress(InetAddress inetAddr);
	
	/**
	 * Delete by id.
	 * 
	 * @param id the id
	 */
	public void deleteById(Long id);
	
	/**
	 * Delete by inet address.
	 * 
	 * @param inetAddr the inet addr
	 */
	public void deleteByInetAddress(InetAddress inetAddr);
	
	/**
	 * Delete not in ranges.
	 * 
	 * @param ranges the ranges
	 */
	public void deleteNotInRanges(List<Range> ranges);
	
	/**
	 * Find all by identity assoc id.
	 * 
	 * @param identityAssocId the identity assoc id
	 * 
	 * @return the list< ia address>
	 */
	public List<IaAddress> findAllByIdentityAssocId(long identityAssocId);
	
	/**
	 * Find all by subnet.
	 * 
	 * @param subnet the subnet
	 * 
	 * @return the list< ia address>
	 */
	public List<IaAddress> findAllBySubnet(Subnet subnet);
	
	/**
	 * Find all by range.
	 * 
	 * @param startAddr the start addr
	 * @param endAddr the end addr
	 * 
	 * @return the list< ia address>
	 */
	public List<IaAddress> findAllByRange(InetAddress startAddr, InetAddress endAddr);
	
	/**
	 * Find all older than.
	 * 
	 * @param date the date
	 * 
	 * @return the list< ia address>
	 */
	public List<IaAddress> findAllOlderThan(Date date);
}
