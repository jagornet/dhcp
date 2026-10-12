/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.db;

import java.util.List;

/**
 * The Interface DhcpOptionDAO.
 * 
 * @author A. Gregory Rabil
 */
public interface DbDhcpOptionDAO
{
	
	/**
	 * Creates the.
	 * 
	 * @param option the option
	 */
	public void create(DbDhcpOption option);
	
	/**
	 * Update.
	 * 
	 * @param option the option
	 */
	public void update(DbDhcpOption option);
	
	/**
	 * Delete by id.
	 * 
	 * @param id the id
	 */
	public void deleteById(Long id);
	
	/**
	 * Gets the by id.
	 * 
	 * @param id the id
	 * 
	 * @return the by id
	 */
	public DbDhcpOption getById(Long id);
	
	/**
	 * Find all by identity assoc id.
	 * 
	 * @param identityAssocId the identity assoc id
	 * 
	 * @return the list< dhcp option>
	 */
	public List<DbDhcpOption> findAllByIdentityAssocId(long identityAssocId);
	
	/**
	 * Find all by ia address id.
	 * 
	 * @param iaAddressId the ia address id
	 * 
	 * @return the list< dhcp option>
	 */
	public List<DbDhcpOption> findAllByIaAddressId(long iaAddressId);
	
	/**
	 * Find all by ia prefix id.
	 * 
	 * @param iaPrefixId the ia prefix id
	 * 
	 * @return the list< dhcp option>
	 */
	public List<DbDhcpOption> findAllByIaPrefixId(long iaPrefixId);
}
