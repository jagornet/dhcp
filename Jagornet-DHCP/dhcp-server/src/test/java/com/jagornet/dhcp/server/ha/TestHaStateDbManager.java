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
