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
	public static String DHCPV6_HOME = System.getProperty("dhcpv6.home");

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
	 * DHCP Message Types - use short to support unsigned byte
	 */
	public static final short SOLICIT = 1;
	public static final short ADVERTISE = 2;
	public static final short REQUEST = 3;
	public static final short CONFIRM = 4;
	public static final short RENEW = 5;
	public static final short REBIND = 6;
	public static final short REPLY = 7;
	public static final short RELEASE = 8;
	public static final short DECLINE = 9;
	public static final short RECONIFURE = 10;
	public static final short INFO_REQUEST = 11;
	public static final short RELAY_FORW = 12;
	public static final short RELAY_REPL = 13;

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
    
    /**
     * Get the string representation of a DHCP message
     * given the message type.  Note that the type is
     * an unsigned byte, so we need a short to store
     * it properly. 
     * @param msg  the message type as a short (i.e. unsigned byte)
     * @return
     */
    public static final String getMessageString(short msg)
    {
        if ( (msg > 0) && (msg <= RELAY_REPL)) {
            return MESSAGE_STRING[msg];
        }
        return "Unknown";
    }
    
	/**
	 * DHCP Options
	 */
    public static final int OPTION_CLIENTID = 1;
    public static final int OPTION_SERVERID = 2;
    public static final int OPTION_IA_NA = 3;
    public static final int OPTION_IA_TA = 4;
    public static final int OPTION_IAADDR = 5;
    public static final int OPTION_ORO = 6;
    public static final int OPTION_PREFERENCE = 7;
    public static final int OPTION_ELAPSED_TIME = 8;
    public static final int OPTION_RELAY_MSG = 9;
    public static final int OPTION_AUTH = 11;
    public static final int OPTION_UNICAST = 12;
    public static final int OPTION_STATUS_CODE = 13;
    public static final int OPTION_RAPID_COMMIT = 14;
    public static final int OPTION_USER_CLASS = 15;
    public static final int OPTION_VENDOR_CLASS = 16;
    public static final int OPTION_VENDOR_OPTS = 17;
    public static final int OPTION_INTERFACE_ID = 18;
    public static final int OPTION_RECONF_MSG = 19;
    public static final int OPTION_RECONF_ACCEPT = 20;
    public static final int OPTION_SIP_SERVERS_DOMAIN_LIST = 21;
    public static final int OPTION_SIP_SERVERS_ADDRESS_LIST = 22;
    public static final int OPTION_DNS_SERVERS = 23;
    public static final int OPTION_DOMAIN_SEARCH_LIST = 24;
    public static final int OPTION_IA_PD = 25;
    public static final int OPTION_IA_PD_PREFIX = 26;
    public static final int OPTION_NIS_SERVERS = 27;
    public static final int OPTION_NISPLUS_SERVERS = 28;
    public static final int OPTION_NIS_DOMAIN_NAME = 29;
    public static final int OPTION_NISPLUS_DOMAIN_NAME = 30;
    public static final int OPTION_SNTP_SERVERS = 31;
    public static final int OPTION_INFO_REFRESH_TIME = 32;

    public static final int STATUS_CODE_SUCCESS = 0;
    public static final int STATUS_CODE_UNSPEC_FAIL = 1;
    public static final int STATUS_CODE_NOADDRSAVAIL = 2;
    public static final int STATUS_CODE_NOBINDING = 3;
    public static final int STATUS_CODE_NOTONLINK = 4;
    public static final int STATUS_CODE_USEMULTICAST = 5;

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
