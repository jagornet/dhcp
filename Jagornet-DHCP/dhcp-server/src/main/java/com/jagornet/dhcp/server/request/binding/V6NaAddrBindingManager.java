/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.request.binding;

import com.jagornet.dhcp.core.message.DhcpMessage;
import com.jagornet.dhcp.core.option.v6.DhcpV6ClientIdOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaNaOption;
import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.config.DhcpServerConfigException;
import com.jagornet.dhcp.server.db.IaAddress;
import com.jagornet.dhcp.server.db.IdentityAssoc;

/**
 * The Interface V6NaAddrBindingManager.  The interface for DHCPv6 IA_NA type
 * addresses, for use by the DhcpV6XXXProcessor classes.
 * 
 * @author A. Gregory Rabil
 */
public interface V6NaAddrBindingManager
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
	public Binding findCurrentBinding(DhcpLink clientLink, DhcpV6ClientIdOption clientIdOption, 
			DhcpV6IaNaOption iaNaOption, DhcpMessage requestMsg);
	
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
	public Binding createSolicitBinding(DhcpLink clientLink, DhcpV6ClientIdOption clientIdOption, 
			DhcpV6IaNaOption iaNaOption, DhcpMessage requestMsg, byte state);

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
			DhcpV6ClientIdOption clientIdOption, DhcpV6IaNaOption iaNaOption,
			DhcpMessage requestMsg, byte state);

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
