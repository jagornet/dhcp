/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.rest.api;

import java.net.UnknownHostException;
import java.util.SortedMap;

import com.jagornet.dhcp.core.util.Subnet;
import com.jagornet.dhcp.server.config.DhcpLink;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;

public class DhcpLinksService {

    protected static DhcpServerConfiguration dhcpServerConfig = 
                                        DhcpServerConfiguration.getInstance();
	private SortedMap<Subnet, DhcpLink> dhcpLinkMap;

    public DhcpLinksService() {
		dhcpLinkMap = dhcpServerConfig.getLinkMap();
	}
    
    public DhcpLink.State getLinkState(String linkAddress, int prefixLen) throws UnknownHostException {
    	return getLinkState(new Subnet(linkAddress, prefixLen));
    }
    
	public DhcpLink.State getLinkState(Subnet subnet) {
		DhcpLink dhcpLink = dhcpLinkMap.get(subnet);
		if (dhcpLink != null) {
			return dhcpLink.getState();
		}
		return null;
	}
}
