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
import java.util.List;

import com.jagornet.dhcpv6.server.request.binding.Range;

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
	 * Create an IdentityAssoc object, including any contained
	 * IaAddresses, IaPrefixes and DhcpOptions, as well as any DhcpOptions
	 * contained in the IaAddresses or IaPrefixes themselves.
	 * 
	 * @param ia the IdentityAssoc to create
	 */
	public void createIA(IdentityAssoc ia);
	
	/**
	 * Update an IdentityAssoc object, including any contained
	 * IaAddresses, IaPrefixes and DhcpOptions, as well as any DhcpOptions
	 * contained in the IaAddresses or IaPrefixes themselves.
	 * 
	 * @param ia the IdentityAssoc to update
	 * @param addAddrs the IaAddresses/IaPrefixes to add to the IdentityAssoc
	 * @param updateAddrs the IaAddresses/IaPrefixes to update in the IdentityAssoc
	 * @param delAddrs the IaAddresses/IaPrefixes to delete from the IdentityAssoc
	 */
	public void updateIA(IdentityAssoc ia, Collection<? extends IaAddress> addAddrs,
			Collection<? extends IaAddress> updateAddrs, Collection<? extends IaAddress> delAddrs);
	
	/**
	 * Delete an IdentityAssoc object, and allow the database
	 * constraints (cascade delete) to care of deleting any
	 * contained IaAddresses, IaPrefixes and DhcpOptions, and further
	 * cascading to delete any DhcpOptions contained in the
	 * IaAddresses or IaPrefixes themselves.
	 * 
	 * @param ia the IdentityAssoc to delete
	 */
	public void deleteIA(IdentityAssoc ia);
	
	/**
	 * Add dhcp option to an existing IdentityAssoc.
	 * 
	 * @param ia the IdentityAssoc
	 * @param option the DhcpOption to add
	 */
	public void addDhcpOption(IdentityAssoc ia, DhcpOption option);
	
	/**
	 * Update dhcp option.
	 * 
	 * @param option the DhcpOption to update
	 */
	public void updateDhcpOption(DhcpOption option);
		
	/**
	 * Delete dhcp option.
	 * 
	 * @param option the DhcpOption to delete
	 */
	public void deleteDhcpOption(DhcpOption option);
	
	/**
	 * Get an IdentityAssoc by id.
	 * 
	 * @param id the ID of the IdentityAssoc to get
	 * @return the IdentityAssoc for the given ID
	 */
	public IdentityAssoc getIA(long id);
	
	/**
	 * Locate an IdentityAssoc object by the key tuple duid-iaid-iatype.
	 * Populate any contained IaAddresses, IaPrefixes and DhcpOptions, as well as
	 * any DhcpOptions contained in the IaAddresses or IaPrefixes themselves.
	 * 
	 * @param duid the duid
	 * @param iatype the iatype
	 * @param iaid the iaid
	 * 
	 * @return a fully-populated IdentityAssoc, or null if not found
	 */
	public IdentityAssoc findIA(byte[] duid, byte iatype, long iaid);
	
	/**
	 * Find an IdentityAssoc for the given IPv6 address.  That is,
	 * locate the IaAddress or IaPrefix for the address, and then
	 * locate the IdentityAssoc that contains that address object.
	 * 
	 * @param inetAddr the inet addr
	 * 
	 * @return the identity assoc
	 */
	public IdentityAssoc findIA(InetAddress inetAddr);
	
	/**
	 * Update an IaAddress.
	 * 
	 * @param iaAddr the IaAddress to update
	 */
	public void updateIaAddr(IaAddress iaAddr);
	
	/**
	 * Delete an IaAddress.
	 * 
	 * @param iaAddr the IaAddress to delete
	 */
	public void deleteIaAddr(IaAddress iaAddr);
	
	/**
	 * Update an IaPrefix.
	 * 
	 * @param iaPrefix the IaPrefix to update
	 */
	public void updateIaPrefix(IaPrefix iaPrefix);
	
	/**
	 * Update an IaPrefix.
	 * 
	 * @param iaPrefix the IaPrefix to delete
	 */
	public void deleteIaPrefix(IaPrefix iaPrefix);
	
	/**
	 * Find existing IPs within an inclusive address range.
	 * 
	 * @param startAddr the start address of the range
	 * @param endAddr the end address of the range
	 * 
	 * @return the list of InetAddress objects for existing IPs in the range
	 */
	public List<InetAddress> findExistingIPs(final InetAddress startAddr, final InetAddress endAddr);

	/**
	 * Find the unused IA Addresses within an inclusive address range.
	 * 
	 * @param startAddr the start address of the range
	 * @param endAddr the end address of the range
	 * 
	 * @return the list of IaAddress objects that are unused in the range
	 */
	public List<IaAddress> findUnusedIaAddresses(InetAddress startAddr, InetAddress endAddr);
	
	/**
	 * Find the expired IA Addresses for the given IA type.
	 * 
	 * @param iatype the IA type
	 * 
	 * @return the list of IaAddress objects that are expired for the type
	 */
	public List<IaAddress> findExpiredIaAddresses(byte iatype);

	/**
	 * Find the unused IA Prefixes within an inclusive address range.
	 * 
	 * @param startAddr the start address of the range
	 * @param endAddr the end address of the range
	 * 
	 * @return the list of IaPrefix objects that are unused in the range
	 */
	public List<IaPrefix> findUnusedIaPrefixes(InetAddress startAddr, InetAddress endAddr);
	
	/**
	 * Find the expired IA Prefixes.
	 * 
	 * @return the list of IaPrefix objects that are expired
	 */
	public List<IaPrefix> findExpiredIaPrefixes();

	/**
	 * Reconcile IA addresses to a set of ranges.  That is, delete any
	 * lease or binding information for IPs that are outside the list of ranges.
	 *   
	 * @param ranges a list of IP address ranges to reconcile against
	 */
	public void reconcileIaAddresses(List<Range> ranges);	

	/**
	 * Find the DHCP options for a given IdentityAssocId.
	 * 
	 * @param identityAssocId the IdentityAssocId
	 * 
	 * @return the list of DhcpOption objects for the IdentityAssocId
	 */
	public List<DhcpOption> findDhcpOptionsByIdentityAssocId(long identityAssocId);
	
}
