/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.rest.api;

import java.io.IOException;

import jakarta.xml.bind.JAXBException;

import com.jagornet.dhcp.server.config.DhcpServerConfigException;
import com.jagornet.dhcp.server.config.DhcpServerConfiguration;
import com.jagornet.dhcp.server.config.xml.DhcpServerConfig;
import com.jagornet.dhcp.server.config.xml.PoliciesType;

public class DhcpServerConfigService {

    protected static DhcpServerConfiguration dhcpServerConfiguration = 
                                        DhcpServerConfiguration.getInstance();

    public DhcpServerConfig getDhcpServerConfig() {
    	return dhcpServerConfiguration.getJaxbServerConfig();
    }
	
	public DhcpServerConfig updateDhcpServerConfig(DhcpServerConfig jaxbServerConfig) throws DhcpServerConfigException, JAXBException, IOException {
		return dhcpServerConfiguration.reload(jaxbServerConfig);
	}
	
	public PoliciesType getGlobalPolicies() {
		return dhcpServerConfiguration.getGlobalPolicies();
	}
	
	public PoliciesType updateGlobalPolicies(PoliciesType globalPolicies) throws DhcpServerConfigException, JAXBException, IOException {
		DhcpServerConfig currentConfig = getDhcpServerConfig();
		// this will validate the policies
		dhcpServerConfiguration.setGlobalPolicies(globalPolicies);
		// validation passed, so set the config object
		currentConfig.setPolicies(globalPolicies);
		// now re-save the config object
		dhcpServerConfiguration.saveConfig(currentConfig);
		return dhcpServerConfiguration.getGlobalPolicies();
	}
}
