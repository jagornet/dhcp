/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpOptionDAO.java is part of Jagornet DHCP.
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

import java.util.List;

/**
 * The Interface DhcpOptionDAO.
 * 
 * @author A. Gregory Rabil
 */
public interface DhcpOptionDAO
{
	
	/**
	 * Creates the.
	 * 
	 * @param option the option
	 */
	public void create(DhcpOption option);
	
	/**
	 * Update.
	 * 
	 * @param option the option
	 */
	public void update(DhcpOption option);
	
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
	public DhcpOption getById(Long id);
	
	/**
	 * Find all by identity assoc id.
	 * 
	 * @param identityAssocId the identity assoc id
	 * 
	 * @return the list< dhcp option>
	 */
	public List<DhcpOption> findAllByIdentityAssocId(long identityAssocId);
	
	/**
	 * Find all by ia address id.
	 * 
	 * @param iaAddressId the ia address id
	 * 
	 * @return the list< dhcp option>
	 */
	public List<DhcpOption> findAllByIaAddressId(long iaAddressId);
	
	/**
	 * Find all by ia prefix id.
	 * 
	 * @param iaPrefixId the ia prefix id
	 * 
	 * @return the list< dhcp option>
	 */
	public List<DhcpOption> findAllByIaPrefixId(long iaPrefixId);
}
