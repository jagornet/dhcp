/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.config;

import com.jagornet.dhcp.server.config.xml.FiltersType;
import com.jagornet.dhcp.server.config.xml.PoliciesType;

public interface DhcpConfigObject 
{	
	public FiltersType getFilters();
	public PoliciesType getPolicies();
	public long getPreferredLifetime();
	public long getValidLifetime();
	public long getPreferredLifetimeMs();
	public long getValidLifetimeMs();
}
