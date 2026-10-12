/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.ha;

import java.util.List;

import com.jagornet.dhcp.server.config.DhcpServerConfiguration;

public class HaStateDb {

	public DhcpServerConfiguration.HaRole haRole;
	public List<HaState> haStates;
}
