/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file V6TaAddrBindingManagerImpl.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.server.request.binding;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.message.DhcpMessage;
import com.jagornet.dhcp.core.message.DhcpV6Message;
import com.jagornet.dhcp.core.option.v6.DhcpV6ClientIdOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaAddrOption;
import com.jagornet.dhcp.core.option.v6.DhcpV6IaTaOption;
import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.config.DhcpServerConfigException;
import com.jagornet.dhcp.server.config.DhcpServerPolicies;
import com.jagornet.dhcp.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcp.server.config.xml.Link;
import com.jagornet.dhcp.server.config.xml.LinkFilter;
import com.jagornet.dhcp.server.config.xml.V6AddressBindingsType;
import com.jagornet.dhcp.server.config.xml.V6AddressPoolsType;
import com.jagornet.dhcp.server.db.IdentityAssoc;

/**
 * The Class V6TaAddrBindingManagerImpl.
 * 
 * @author A. Gregory Rabil
 */
public class V6TaAddrBindingManagerImpl 
		extends V6AddrBindingManager 
		implements V6TaAddrBindingManager
{
	private static Logger log = LoggerFactory.getLogger(V6TaAddrBindingManagerImpl.class);
	
	/**
	 * Instantiates a new ta addr binding manager impl.
	 * 
	 * @throws DhcpServerConfigException the dhcp server config exception
	 */
	public V6TaAddrBindingManagerImpl() throws DhcpServerConfigException
	{
		super();
	}
	
	@Override
	protected V6AddressBindingsType getV6AddressBindingsType(Link link) {
		return link.getV6TaAddrBindings();
	}

	@Override
	protected V6AddressPoolsType getV6AddressPoolsType(LinkFilter linkFilter) {
		return linkFilter.getV6TaAddrPools();
	}

	@Override
	protected V6AddressPoolsType getV6AddressPoolsType(Link link) {
		return link.getV6TaAddrPools();
	}

	@Override
	public Binding findCurrentBinding(DhcpLink clientLink,
			DhcpV6ClientIdOption clientIdOption, DhcpV6IaTaOption iaTaOption,
			DhcpMessage requestMsg) {
		
		byte[] duid = clientIdOption.getDuid();
		long iaid = iaTaOption.getIaId();
		
		return super.findCurrentBinding(clientLink, duid, IdentityAssoc.TA_TYPE, 
				iaid, requestMsg);
	}

	@Override
	public Binding createSolicitBinding(DhcpLink clientLink,
			DhcpV6ClientIdOption clientIdOption, DhcpV6IaTaOption iaTaOption,
			DhcpMessage requestMsg, byte state) {
		
		byte[] duid = clientIdOption.getDuid();
		long iaid = iaTaOption.getIaId();

		StaticBinding staticBinding = 
			findStaticBinding(clientLink.getLink(), duid, IdentityAssoc.TA_TYPE, iaid, requestMsg);
		
		if (staticBinding != null) {
			return super.createStaticBinding(clientLink, duid, IdentityAssoc.TA_TYPE, 
					iaid, staticBinding, requestMsg);
		}
		else {
			return super.createBinding(clientLink, duid, IdentityAssoc.TA_TYPE, 
					iaid, getInetAddrs(iaTaOption), requestMsg, state);
		}
	}

	@Override
	public Binding updateBinding(Binding binding, DhcpLink clientLink,
			DhcpV6ClientIdOption clientIdOption, DhcpV6IaTaOption iaTaOption,
			DhcpMessage requestMsg, byte state) {
		
		byte[] duid = clientIdOption.getDuid();
		long iaid = iaTaOption.getIaId();

		StaticBinding staticBinding = 
			findStaticBinding(clientLink.getLink(), duid, IdentityAssoc.TA_TYPE, iaid, requestMsg);
		
		if (staticBinding != null) {
			return super.updateStaticBinding(binding, clientLink, duid, IdentityAssoc.TA_TYPE, 
					iaid, staticBinding, requestMsg);
		}
		else {
			return super.updateBinding(binding, clientLink, duid, IdentityAssoc.TA_TYPE,
					iaid, getInetAddrs(iaTaOption), requestMsg, state);
		}
	}
	
	/**
	 * Extract the list of IP addresses from within the given IA_TA option.
	 * 
	 * @param iaNaOption the IA_TA option
	 * 
	 * @return the list of InetAddresses for the IPs in the IA_TA option
	 */
	private List<InetAddress> getInetAddrs(DhcpV6IaTaOption iaTaOption)
	{
		List<InetAddress> inetAddrs = null;
		List<DhcpV6IaAddrOption> iaAddrs = iaTaOption.getIaAddrOptions();
		if ((iaAddrs != null) && !iaAddrs.isEmpty()) {
			inetAddrs = new ArrayList<>();
			for (DhcpV6IaAddrOption iaAddr : iaAddrs) {
				InetAddress inetAddr = iaAddr.getInetAddress();
				inetAddrs.add(inetAddr);
			}
		}
		return inetAddrs;
	}

	@Override
	protected byte getIaType() {
		return IdentityAssoc.TA_TYPE;
	}
	
	@Override
	protected Map<Integer, com.jagornet.dhcp.core.option.base.DhcpOption> buildDhcpOptions(
			DhcpLink clientLink, DhcpV6Message requestMsg, V6AddressBindingPool bp) {
		
		Map<Integer, com.jagornet.dhcp.core.option.base.DhcpOption> configOptionMap = 
				serverConfig.effectiveMsgOptions((DhcpV6Message)requestMsg, clientLink, bp);
		
    	if (DhcpServerPolicies.effectivePolicyAsBoolean(requestMsg,
    			clientLink.getLink(), Property.DHCP_SEND_REQUESTED_OPTIONS_ONLY)) {
    		log.debug("buildDhcpOptions: configured to include only requested options");
    		configOptionMap = requestedOptions(configOptionMap, requestMsg);
    	}
    	
		return configOptionMap;
	}
	
	protected Map<Integer, com.jagornet.dhcp.core.option.base.DhcpOption> buildIaDhcpOptions(
			DhcpLink clientLink, DhcpV6Message requestMsg, V6AddressBindingPool bp) {
		
		Map<Integer, com.jagornet.dhcp.core.option.base.DhcpOption> configOptionMap = 
				serverConfig.effectiveIaTaOptions(requestMsg, clientLink, bp);
		
    	if (DhcpServerPolicies.effectivePolicyAsBoolean(requestMsg,
    			clientLink.getLink(), Property.DHCP_SEND_REQUESTED_OPTIONS_ONLY)) {
    		log.debug("buildDhcpOptions: configured to include only requested options");
    		configOptionMap = requestedOptions(configOptionMap, requestMsg);
    	}
    	
		return configOptionMap;
	}
	
	protected Map<Integer, com.jagornet.dhcp.core.option.base.DhcpOption> buildIaAddrDhcpOptions(
			DhcpLink clientLink, DhcpV6Message requestMsg, V6AddressBindingPool bp) {
		
		Map<Integer, com.jagornet.dhcp.core.option.base.DhcpOption> configOptionMap = 
				serverConfig.effectiveTaAddrOptions(requestMsg, clientLink, bp);
		
    	if (DhcpServerPolicies.effectivePolicyAsBoolean(requestMsg,
    			clientLink.getLink(), Property.DHCP_SEND_REQUESTED_OPTIONS_ONLY)) {
    		log.debug("buildDhcpOptions: configured to include only requested options");
    		configOptionMap = requestedOptions(configOptionMap, requestMsg);
    	}
    	
		return configOptionMap;
	}

}
