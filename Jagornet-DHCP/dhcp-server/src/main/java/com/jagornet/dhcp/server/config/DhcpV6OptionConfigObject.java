/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.config;

public interface DhcpV6OptionConfigObject extends DhcpConfigObject 
{
	public DhcpV6ConfigOptions getMsgConfigOptions();
	public DhcpV6ConfigOptions getIaConfigOptions();
	public DhcpV6ConfigOptions getAddrConfigOptions();
}
