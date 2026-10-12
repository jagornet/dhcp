/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.request.binding;

import java.util.List;
import java.util.Map;

import com.jagornet.dhcp.core.message.DhcpMessage;
import com.jagornet.dhcp.server.config.xml.Link;

public interface BindingManager 
{
	public Map<String, List<? extends BindingPool>> getBindingPoolMap();
	public Map<String, List<? extends StaticBinding>> getStaticBindingMap();
	public StaticBinding findStaticBinding(Link clientLink, byte[] duid, byte iatype, long iaid,
			DhcpMessage requestMsg);
}
