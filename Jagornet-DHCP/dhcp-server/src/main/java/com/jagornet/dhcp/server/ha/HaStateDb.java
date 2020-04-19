package com.jagornet.dhcp.server.ha;

import java.util.List;

import com.jagornet.dhcp.server.config.DhcpServerConfiguration;

public class HaStateDb {

	public DhcpServerConfiguration.HaRole haRole;
	public List<HaState> haStates;
}
