package com.agr.dhcpv6.util;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:
 * @author
 * @version 1.0
 */

import java.net.*;

public class DhcpConstants
{
    /**
     * IPv4 Addresses
     */
    public static InetAddress LOCALHOST = null;
    public static InetAddress BROADCAST = null;

	/**
	 * IPv6 Multicast Addresses
	 */
	public static InetAddress ALL_DHCP_RELAY_AGENTS_AND_SERVERS = null;
	public static InetAddress ALL_DHCP_SERVERS = null;

	/**
	 * UDP Ports
	 */
	public static final int CLIENT_PORT = 546;
	public static final int SERVER_PORT = 547;

	/**
	 * DHCP Message Types
	 */
	public static final byte SOLICIT = (byte)1;
	public static final byte ADVERTISE = (byte)2;
	public static final byte REQUEST = (byte)3;
	public static final byte CONFIRM = (byte)4;
	public static final byte RENEW = (byte)5;
	public static final byte REBIND = (byte)6;
	public static final byte REPLY = (byte)7;
	public static final byte RELEASE = (byte)8;
	public static final byte DECLINE = (byte)9;
	public static final byte RECONIFURE = (byte)10;
	public static final byte INFO_REQUEST = (byte)11;
	public static final byte RELAY_FORW = (byte)12;
	public static final byte RELAY_REPL = (byte)13;

    public static final String[] MESSAGE_STRING = { "Unknown",
                                                    "Solicit",
                                                    "Advertise",
                                                    "Request",
                                                    "Confirm",
                                                    "Renew",
                                                    "Rebind",
                                                    "Reply",
                                                    "Release",
                                                    "Decline",
                                                    "Reconfigure",
                                                    "Info-Request",
                                                    "Relay-Forward",
                                                    "Relay-Reply" };
    
    public static final String getMessageString(byte msg)
    {
        if ( (msg > 0) && (msg <= RELAY_REPL)) {
            return MESSAGE_STRING[msg];
        }
        return "Unknown";
    }
    
	/**
	 * DHCP Options
	 */
    public static final short OPTION_CLIENTID = 1;
    public static final short OPTION_SERVERID = 2;
    public static final short OPTION_IA_NA = 3;
    public static final short OPTION_IA_TA = 4;
    public static final short OPTION_IAADDR = 5;
    public static final short OPTION_ORO = 6;
    public static final short OPTION_PREFERENCE = 7;
    public static final short OPTION_ELAPSED_TIME = 8;
    public static final short OPTION_RELAY_MSG = 9;
    public static final short OPTION_AUTH = 11;
    public static final short OPTION_UNICAST = 12;
    public static final short OPTION_STATUS_CODE = 13;
    public static final short OPTION_RAPID_COMMIT = 14;
    public static final short OPTION_USER_CLASS = 15;
    public static final short OPTION_VENDOR_CLASS = 16;
    public static final short OPTION_VENDOR_OPTS = 17;
    public static final short OPTION_INTERFACE_ID = 18;
    public static final short OPTION_RECONF_MSG = 19;
    public static final short OPTION_RECONF_ACCEPT = 20;
    public static final short OPTION_DNS_SERVERS = 23;
    public static final short OPTION_DOMAIN_SEARCH_LIST = 24;
    public static final short OPTION_NIS_SERVERS = 27;
    public static final short OPTION_NISPLUS_SERVERS = 28;
    public static final short OPTION_NIS_DOMAIN_NAME = 29;
    public static final short OPTION_NISPLUS_DOMAIN_NAME = 30;
    public static final short OPTION_SNTP_SERVERS = 31;
    public static final short OPTION_INFO_REFRESH_TIME = 32;

	static {
		try {
            LOCALHOST = InetAddress.getByName("127.0.0.1");
            BROADCAST = InetAddress.getByName("255.255.255.255");
			ALL_DHCP_RELAY_AGENTS_AND_SERVERS = InetAddress.getByName("FF02::1:2");
			ALL_DHCP_SERVERS = InetAddress.getByName("FF05::1:3");
		}
		catch (UnknownHostException ex) { }
	}
}
