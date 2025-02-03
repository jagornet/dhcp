package com.jagornet.dhcp.server.db;

public interface DhcpLeaseCallbackHandler {

	public void processDhcpLease(DhcpLease dhcpLease) throws ProcessLeaseException;
}
