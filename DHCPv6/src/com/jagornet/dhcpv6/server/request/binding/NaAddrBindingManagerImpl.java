/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file NaAddrBindingManagerImpl.java is part of DHCPv6.
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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.jagornet.dhcpv6.db.IdentityAssoc;
import com.jagornet.dhcpv6.message.DhcpMessageInterface;
import com.jagornet.dhcpv6.option.DhcpClientIdOption;
import com.jagornet.dhcpv6.option.DhcpIaAddrOption;
import com.jagornet.dhcpv6.option.DhcpIaNaOption;
import com.jagornet.dhcpv6.server.config.DhcpLink;
import com.jagornet.dhcpv6.server.config.DhcpServerConfigException;
import com.jagornet.dhcpv6.xml.AddressBindingsType;
import com.jagornet.dhcpv6.xml.AddressPoolsType;
import com.jagornet.dhcpv6.xml.Link;
import com.jagornet.dhcpv6.xml.LinkFilter;

/**
 * The Class NaAddrBindingManagerImpl.
 * 
 * @author A. Gregory Rabil
 */
public class NaAddrBindingManagerImpl 
		extends AddressBindingManager 
		implements NaAddrBindingManager
{
	
	/**
	 * Instantiates a new na addr binding manager impl.
	 * 
	 * @throws DhcpServerConfigException the dhcp server config exception
	 */
	public NaAddrBindingManagerImpl() throws DhcpServerConfigException
	{
		super();
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.AddressBindingManager#getAddressBindingsType(com.jagornet.dhcpv6.xml.Link)
	 */
	@Override
	protected AddressBindingsType getAddressBindingsType(Link link) {
		return link.getNaAddrBindings();
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.AddressBindingManager#getAddressPoolsType(com.jagornet.dhcpv6.xml.LinkFilter)
	 */
	@Override
	protected AddressPoolsType getAddressPoolsType(LinkFilter linkFilter) {
		return linkFilter.getNaAddrPools();
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.AddressBindingManager#getAddressPoolsType(com.jagornet.dhcpv6.xml.Link)
	 */
	@Override
	protected AddressPoolsType getAddressPoolsType(Link link) {
		return link.getNaAddrPools();
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.NaAddrBindingManager#findCurrentBinding(com.jagornet.dhcpv6.xml.Link, com.jagornet.dhcpv6.option.DhcpClientIdOption, com.jagornet.dhcpv6.option.DhcpIaNaOption, com.jagornet.dhcpv6.message.DhcpMessageInterface)
	 */
	@Override
	public Binding findCurrentBinding(DhcpLink clientLink,
			DhcpClientIdOption clientIdOption, DhcpIaNaOption iaNaOption,
			DhcpMessageInterface requestMsg) {
		
		byte[] duid = clientIdOption.getDuid();
		long iaid = iaNaOption.getIaId();
		
		return super.findCurrentBinding(clientLink, duid, IdentityAssoc.NA_TYPE, 
				iaid, requestMsg);
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.NaAddrBindingManager#createSolicitBinding(com.jagornet.dhcpv6.xml.Link, com.jagornet.dhcpv6.option.DhcpClientIdOption, com.jagornet.dhcpv6.option.DhcpIaNaOption, com.jagornet.dhcpv6.message.DhcpMessageInterface, boolean)
	 */
	@Override
	public Binding createSolicitBinding(DhcpLink clientLink,
			DhcpClientIdOption clientIdOption, DhcpIaNaOption iaNaOption,
			DhcpMessageInterface requestMsg, byte state) {
		
		byte[] duid = clientIdOption.getDuid();
		long iaid = iaNaOption.getIaId();

		StaticBinding staticBinding = 
			findStaticBinding(clientLink.getLink(), duid, IdentityAssoc.NA_TYPE, iaid, requestMsg);
		
		if (staticBinding != null) {
			return super.createStaticBinding(clientLink, duid, IdentityAssoc.NA_TYPE, 
					iaid, staticBinding, requestMsg);
		}
		else {
			return super.createBinding(clientLink, duid, IdentityAssoc.NA_TYPE, 
					iaid, getInetAddrs(iaNaOption), requestMsg, state);
		}
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.NaAddrBindingManager#updateBinding(com.jagornet.dhcpv6.server.request.binding.Binding, com.jagornet.dhcpv6.xml.Link, com.jagornet.dhcpv6.option.DhcpClientIdOption, com.jagornet.dhcpv6.option.DhcpIaNaOption, com.jagornet.dhcpv6.message.DhcpMessageInterface, byte)
	 */
	@Override
	public Binding updateBinding(Binding binding, DhcpLink clientLink,
			DhcpClientIdOption clientIdOption, DhcpIaNaOption iaNaOption,
			DhcpMessageInterface requestMsg, byte state) {
		
		byte[] duid = clientIdOption.getDuid();
		long iaid = iaNaOption.getIaId();	

		StaticBinding staticBinding = 
			findStaticBinding(clientLink.getLink(), duid, IdentityAssoc.NA_TYPE, iaid, requestMsg);
		
		if (staticBinding != null) {
			return super.updateStaticBinding(binding, clientLink, duid, IdentityAssoc.NA_TYPE, 
					iaid, staticBinding, requestMsg);
		}
		else {
			return super.updateBinding(binding, clientLink, duid, IdentityAssoc.NA_TYPE,
					iaid, getInetAddrs(iaNaOption), requestMsg, state);
		}
	}
	
	/**
	 * Extract the list of IP addresses from within the given IA_NA option.
	 * 
	 * @param iaNaOption the IA_NA option
	 * 
	 * @return the list of InetAddresses for the IPs in the IA_NA option
	 */
	private List<InetAddress> getInetAddrs(DhcpIaNaOption iaNaOption)
	{
		List<InetAddress> inetAddrs = null;
		List<DhcpIaAddrOption> iaAddrs = iaNaOption.getIaAddrOptions();
		if ((iaAddrs != null) && !iaAddrs.isEmpty()) {
			inetAddrs = new ArrayList<InetAddress>();
			for (DhcpIaAddrOption iaAddr : iaAddrs) {
				InetAddress inetAddr = iaAddr.getInetAddress();
				inetAddrs.add(inetAddr);
			}
		}
		return inetAddrs;
	}

	@Override
	protected byte getIaType() {
		return IdentityAssoc.NA_TYPE;
	}
}
