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
