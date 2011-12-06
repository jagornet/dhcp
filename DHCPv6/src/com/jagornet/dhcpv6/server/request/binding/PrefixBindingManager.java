/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file PrefixBindingManager.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.db.IaPrefix;
import com.jagornet.dhcpv6.message.DhcpMessageInterface;
import com.jagornet.dhcpv6.option.DhcpClientIdOption;
import com.jagornet.dhcpv6.option.DhcpIaPdOption;
import com.jagornet.dhcpv6.server.config.DhcpServerConfigException;
import com.jagornet.dhcpv6.xml.Link;

/**
 * The Interface PrefixBindingManager.  The interface for IA_PD type
 * prefixes, for use by the DhcpXXXProcessor classes.
 * 
 * @author A. Gregory Rabil
 */
public interface PrefixBindingManager
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
	 * @param iaPdOption the ia pd option
	 * @param requestMsg the request msg
	 * 
	 * @return the binding
	 */
	public Binding findCurrentBinding(Link clientLink, DhcpClientIdOption clientIdOption, 
			DhcpIaPdOption iaPdOption, DhcpMessageInterface requestMsg);
	
	/**
	 * Creates the solicit binding.
	 * 
	 * @param clientLink the client link
	 * @param clientIdOption the client id option
	 * @param iaPdOption the ia pd option
	 * @param requestMsg the request msg
	 * @param rapidCommit the rapid commit
	 * 
	 * @return the binding
	 */
	public Binding createSolicitBinding(Link clientLink, DhcpClientIdOption clientIdOption, 
			DhcpIaPdOption iaPdOption, DhcpMessageInterface requestMsg, boolean rapidCommit);

	/**
	 * Update binding.
	 * 
	 * @param binding the binding
	 * @param clientLink the client link
	 * @param clientIdOption the client id option
	 * @param iaPdOption the ia pd option
	 * @param requestMsg the request msg
	 * @param state the state
	 * 
	 * @return the binding
	 */
	public Binding updateBinding(Binding binding, Link clientLink, 
			DhcpClientIdOption clientIdOption, DhcpIaPdOption iaPdOption,
			DhcpMessageInterface requestMsg, byte state);

	/**
	 * Release ia prefix.
	 * 
	 * @param iaPrefix the ia prefix
	 */
	public void releaseIaPrefix(IaPrefix iaPrefix);
	
	/**
	 * Decline ia prefix.
	 * 
	 * @param iaPrefix the ia prefix
	 */
	public void declineIaPrefix(IaPrefix iaPrefix);
}
