/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.db;

import java.net.InetAddress;
import java.util.Date;
import java.util.List;

import com.jagornet.dhcp.core.util.Subnet;
import com.jagornet.dhcp.server.request.binding.Range;

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
