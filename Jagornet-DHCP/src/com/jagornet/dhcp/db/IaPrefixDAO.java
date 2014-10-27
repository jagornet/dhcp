/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file IaPrefixDAO.java is part of Jagornet DHCP.
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
import java.util.Date;
import java.util.List;

import com.jagornet.dhcp.server.request.binding.Range;
import com.jagornet.dhcp.util.Subnet;

/**
 * The Interface IaPrefixDAO.
 * 
 * @author A. Gregory Rabil
 */
public interface IaPrefixDAO
{
	
	/**
	 * Creates an IaPrefix
	 * 
	 * @param iaPrefix the ia prefix
	 */
	public void create(IaPrefix iaPrefix);
	
	/**
	 * Update an IaPrefix
	 * 
	 * @param iaPrefix the ia prefix
	 */
	public void update(IaPrefix iaPrefix);
	
	/**
	 * Gets the by id.
	 * 
	 * @param id the id
	 * 
	 * @return the by id
	 */
	public IaPrefix getById(Long id);
	
	/**
	 * Gets the by inet address.
	 * 
	 * @param inetAddr the inet addr
	 * 
	 * @return the by inet address
	 */
	public IaPrefix getByInetAddress(InetAddress inetAddr);
	
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
	 * @return the list< ia prefix>
	 */
	public List<IaPrefix> findAllByIdentityAssocId(long identityAssocId);
	
	/**
	 * Find all by subnet.
	 * 
	 * @param subnet the subnet
	 * 
	 * @return the list< ia prefix>
	 */
	public List<IaPrefix> findAllBySubnet(Subnet subnet);
	
	/**
	 * Find all by range.
	 * 
	 * @param startAddr the start addr
	 * @param endAddr the end addr
	 * 
	 * @return the list< ia prefix>
	 */
	public List<IaPrefix> findAllByRange(InetAddress startAddr, InetAddress endAddr);
	
	/**
	 * Find all older than.
	 * 
	 * @param date the date
	 * 
	 * @return the list< ia prefix>
	 */
	public List<IaPrefix> findAllOlderThan(Date date);
	
	/**
	 * Find unused by range.
	 * 
	 * @param startAddr the start addr
	 * @param endAddr the end addr
	 * 
	 * @return the list< ia prefix>
	 */
	public List<IaPrefix> findUnusedByRange(InetAddress startAddr, InetAddress endAddr);

	/**
	 * Find existing IPs by range.
	 * 
	 * @param startAddr the start addr
	 * @param endAddr the end addr
	 * 
	 * @return the list< inet prefix>
	 */
	public List<InetAddress> findExistingIPs(final InetAddress startAddr, final InetAddress endAddr);
}
