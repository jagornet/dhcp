/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file IaManager.java is part of DHCPv6.
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
import java.util.Collection;

/**
 * Interface for handling the client bindings for the server.
 * 
 * The following definition of a binding is copied from
 * RFC 3315 section 4.2:
 * 
 * A binding (or, client binding) is a group of server data records containing
 * the information the server has about the addresses in an IA or configuration
 * information explicitly assigned to the client.  Configuration information that
 * has been returned to a client through a policy - for example, the information
 * returned to all clients on the same link - does not require a binding.  A
 * binding containing information about an IA is indexed by the tuple <DUID,
 * IA-type, IAID> (where IA-type is the type of address in the IA; for example,
 * temporary).  A binding containing configuration information for a client
 * is indexed by <DUID>.
 * 
 * @author A. Gregory Rabil
 */
public interface IaManager
{
	/**
	 * Creates the ia.
	 * 
	 * @param ia the ia
	 */
	public void createIA(IdentityAssoc ia);
	
	/**
	 * Update ia.
	 * 
	 * @param ia the ia
	 * @param addAddrs the add addrs
	 * @param updateAddrs the update addrs
	 * @param delAddrs the del addrs
	 */
	public void updateIA(IdentityAssoc ia, Collection<? extends IaAddress> addAddrs,
			Collection<? extends IaAddress> updateAddrs, Collection<? extends IaAddress> delAddrs);
	
	/**
	 * Delete ia.
	 * 
	 * @param ia the ia
	 */
	public void deleteIA(IdentityAssoc ia);
	
	/**
	 * Add dhcp option.
	 * 
	 * @param ia the ia
	 * @param option the option
	 */
	public void addDhcpOption(IdentityAssoc ia, DhcpOption option);
	
	/**
	 * Update dhcp option.
	 * 
	 * @param option the option
	 */
	public void updateDhcpOption(DhcpOption option);
		
	/**
	 * Delete dhcp option.
	 * 
	 * @param option the option
	 */
	public void deleteDhcpOption(DhcpOption option);
	
	public IdentityAssoc getIA(long id);
	
	/**
	 * Find ia.
	 * 
	 * @param duid the duid
	 * @param iatype the iatype
	 * @param iaid the iaid
	 * 
	 * @return the identity assoc
	 */
	public IdentityAssoc findIA(byte[] duid, byte iatype, long iaid);
	
	/**
	 * Find ia.
	 * 
	 * @param inetAddr the inet addr
	 * 
	 * @return the identity assoc
	 */
	public IdentityAssoc findIA(InetAddress inetAddr);
	
	/**
	 * Update ia addr.
	 * 
	 * @param iaAddr the ia addr
	 */
	public void updateIaAddr(IaAddress iaAddr);
	
	/**
	 * Delete ia addr.
	 * 
	 * @param iaAddr the ia addr
	 */
	public void deleteIaAddr(IaAddress iaAddr);
	
	/**
	 * Update ia prefix.
	 * 
	 * @param iaPrefix the ia prefix
	 */
	public void updateIaPrefix(IaPrefix iaPrefix);
	
	/**
	 * Delete ia prefix.
	 * 
	 * @param iaPrefix the ia prefix
	 */
	public void deleteIaPrefix(IaPrefix iaPrefix);
}
