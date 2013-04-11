/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file NaAddrBindingManager.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.server.request.binding;

import com.jagornet.dhcpv6.db.IaAddress;
import com.jagornet.dhcpv6.db.IdentityAssoc;
import com.jagornet.dhcpv6.message.DhcpMessageInterface;
import com.jagornet.dhcpv6.option.DhcpClientIdOption;
import com.jagornet.dhcpv6.option.DhcpIaNaOption;
import com.jagornet.dhcpv6.server.config.DhcpLink;
import com.jagornet.dhcpv6.server.config.DhcpServerConfigException;

/**
 * The Interface NaAddrBindingManager.  The interface for IA_NA type
 * addresses, for use by the DhcpXXXProcessor classes.
 * 
 * @author A. Gregory Rabil
 */
public interface NaAddrBindingManager extends StaticBindingManager
{
	/**
	 * Initialize the manager.
	 * 
	 * @throws DhcpServerConfigException
	 */
	public void init() throws DhcpServerConfigException;
	
	/**
	 * Find current binding.
	 * 
	 * @param clientLink the client link
	 * @param clientIdOption the client id option
	 * @param iaNaOption the ia na option
	 * @param requestMsg the request msg
	 * 
	 * @return the binding
	 */
	public Binding findCurrentBinding(DhcpLink clientLink, DhcpClientIdOption clientIdOption, 
			DhcpIaNaOption iaNaOption, DhcpMessageInterface requestMsg);
	
	/**
	 * Creates the solicit binding.
	 * 
	 * @param clientLink the client link
	 * @param clientIdOption the client id option
	 * @param iaNaOption the ia na option
	 * @param requestMsg the request msg
	 * @param rapidCommit the rapid commit
	 * 
	 * @return the binding
	 */
	public Binding createSolicitBinding(DhcpLink clientLink, DhcpClientIdOption clientIdOption, 
			DhcpIaNaOption iaNaOption, DhcpMessageInterface requestMsg, byte state);

	/**
	 * Update binding.
	 * 
	 * @param binding the binding
	 * @param clientLink the client link
	 * @param clientIdOption the client id option
	 * @param iaNaOption the ia na option
	 * @param requestMsg the request msg
	 * @param state the state
	 * 
	 * @return the binding
	 */
	public Binding updateBinding(Binding binding, DhcpLink clientLink, 
			DhcpClientIdOption clientIdOption, DhcpIaNaOption iaNaOption,
			DhcpMessageInterface requestMsg, byte state);

	/**
	 * Release ia address.
	 * 
	 * @param iaAddr the ia addr
	 */
	public void releaseIaAddress(IdentityAssoc ia, IaAddress iaAddr);
	
	/**
	 * Decline ia address.
	 * 
	 * @param iaAddr the ia addr
	 */
	public void declineIaAddress(IdentityAssoc ia, IaAddress iaAddr);
}
