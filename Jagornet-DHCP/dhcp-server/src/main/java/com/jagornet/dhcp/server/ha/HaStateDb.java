package com.jagornet.dhcp.server.ha;

import java.util.List;

import com.jagornet.dhcp.core.util.DhcpConstants;

public class HaStateDb {

	public DhcpConstants.HaRole haRole;
	public List<HaState> haStates;
}
