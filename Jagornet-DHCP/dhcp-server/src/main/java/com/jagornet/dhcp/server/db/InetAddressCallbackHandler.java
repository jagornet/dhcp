package com.jagornet.dhcp.server.db;

import java.io.IOException;
import java.net.InetAddress;

public interface InetAddressCallbackHandler {

	public void processInetAddress(InetAddress inetAddr) throws IOException;
}
