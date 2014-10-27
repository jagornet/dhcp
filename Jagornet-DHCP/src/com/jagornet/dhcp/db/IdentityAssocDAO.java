/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file IdentityAssocDAO.java is part of Jagornet DHCP.
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
 * The Interface IdentityAssocDAO.
 * 
 * @author A. Gregory Rabil
 */
public interface IdentityAssocDAO 
{
	
	/**
	 * Creates an IdentityAssoc
	 * 
	 * @param ia the ia
	 */
	public void create(IdentityAssoc ia);
	
	/**
	 * Update an IdentityAssoc
	 * 
	 * @param ia the ia
	 */
	public void update(IdentityAssoc ia);
	
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
	public IdentityAssoc getById(Long id);
	
	/**
	 * Gets the by key.
	 * 
	 * @param duid the duid
	 * @param iatype the iatype
	 * @param iaid the iaid
	 * 
	 * @return the by key
	 */
	public IdentityAssoc getByKey(byte[] duid, byte iatype, long iaid);
	
	/**
	 * Find all by duid.
	 * 
	 * @param duid the duid
	 * 
	 * @return the list< identity assoc>
	 */
	public List<IdentityAssoc> findAllByDuid(byte[] duid);
	
	/**
	 * Find all by iaid.
	 * 
	 * @param iaid the iaid
	 * 
	 * @return the list< identity assoc>
	 */
	public List<IdentityAssoc> findAllByIaid(long iaid);
	
	/**
	 * For unit tests only
	 */
	public void deleteAll();
}
