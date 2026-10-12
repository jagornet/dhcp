/*
 * Copyright Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.jagornet.dhcp.server.ha;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jagornet.dhcp.server.config.DhcpServerConfiguration;

public class TestHaStateDbManager {

	HaStateDbManager haStateDbManager;
	
	@Before
	public void setUp() throws Exception {
		haStateDbManager = new HaStateDbManager();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testInitPrimary() throws Exception {
		haStateDbManager.init(DhcpServerConfiguration.HaRole.PRIMARY);
	}

}
