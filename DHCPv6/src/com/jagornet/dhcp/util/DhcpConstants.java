/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpConstants.java is part of DHCPv6.
 *
 *   DHCPv6 is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   DHCPv6 is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with DHCPv6.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/*
 *   This file DhcpConstants.java is part of DHCPv6.
 *
 *   DHCPv6 is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   DHCPv6 is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with DHCPv6.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcp.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class DhcpConstants.
 * Defines standard DHCPv6 constants
 * 
 * @author A. Gregory Rabil
 */
public class DhcpConstants
{
    private static Logger log = LoggerFactory.getLogger(DhcpConstants.class);
	
	public static String JAGORNET_DHCP_HOME = System.getProperty("jagornet.dhcp.home");
	
	public static boolean IS_WINDOWS = System.getProperty("os.name").startsWith("Windows");
	
	public static InetAddress ZEROADDR_V4 = null;
	public static InetAddress ZEROADDR_V6 = null;
	
    public static InetAddress LOCALHOST_V4 = null;
    public static InetAddress LOCALHOST_V6 = null;
    
    public static InetAddress BROADCAST = null;

	public static InetAddress ALL_DHCP_RELAY_AGENTS_AND_SERVERS = null;	
	public static InetAddress ALL_DHCP_SERVERS = null;

    static {
		try {
			ZEROADDR_V4 = InetAddress.getByName("0.0.0.0");
			ZEROADDR_V6 = InetAddress.getByName("::");
            LOCALHOST_V4 = InetAddress.getByName("127.0.0.1");
            LOCALHOST_V6 = InetAddress.getByName("::1");
            BROADCAST = InetAddress.getByName("255.255.255.255");
			ALL_DHCP_RELAY_AGENTS_AND_SERVERS = InetAddress.getByName("FF02::1:2");
			ALL_DHCP_SERVERS = InetAddress.getByName("FF05::1:3");
		}
		catch (UnknownHostException ex) { 
			log.error("Failed to initialize IP constants: " + ex);
		}
	}

	public static final int V6_CLIENT_PORT = 546;
	public static final int V6_SERVER_PORT = 547;

	/** DHCPv6 Message Types - use short to support unsigned byte. */
	public static final short V6MESSAGE_TYPE_SOLICIT = 1;
	public static final short V6MESSAGE_TYPE_ADVERTISE = 2;
	public static final short V6MESSAGE_TYPE_REQUEST = 3;
	public static final short V6MESSAGE_TYPE_CONFIRM = 4;
	public static final short V6MESSAGE_TYPE_RENEW = 5;
	public static final short V6MESSAGE_TYPE_REBIND = 6;
	public static final short V6MESSAGE_TYPE_REPLY = 7;
	public static final short V6MESSAGE_TYPE_RELEASE = 8;
	public static final short V6MESSAGE_TYPE_DECLINE = 9;
	public static final short V6MESSAGE_TYPE_RECONFIGURE = 10;
	public static final short V6MESSAGE_TYPE_INFO_REQUEST = 11;
	public static final short V6MESSAGE_TYPE_RELAY_FORW = 12;
	public static final short V6MESSAGE_TYPE_RELAY_REPL = 13;

    public static final String[] V6MESSAGE_STRING = { "Unknown",
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
     * Get the string representation of a DHCPv6 message
     * given the message type.  Note that the type is
     * an unsigned byte, so we need a short to store
     * it properly.
     * 
     * @param msg  the message type as a short (i.e. unsigned byte)
     * 
     * @return the message string
     */
    public static final String getV6MessageString(short msg)
    {
        if ( (msg > 0) && (msg <= V6MESSAGE_TYPE_RELAY_REPL)) {
            return V6MESSAGE_STRING[msg];
        }
        return "Unknown";
    }
    
	/** DHCPv6 Options. */
    public static final int V6OPTION_CLIENTID = 1;
    public static final int V6OPTION_SERVERID = 2;
    public static final int V6OPTION_IA_NA = 3;
    public static final int V6OPTION_IA_TA = 4;
    public static final int V6OPTION_IAADDR = 5;
    public static final int V6OPTION_ORO = 6;
    public static final int V6OPTION_PREFERENCE = 7;
    public static final int V6OPTION_ELAPSED_TIME = 8;
    public static final int V6OPTION_RELAY_MSG = 9;
    // option code 10 is unassigned
    public static final int V6OPTION_AUTH = 11;
    public static final int V6OPTION_UNICAST = 12;
    public static final int V6OPTION_STATUS_CODE = 13;
    public static final int V6OPTION_RAPID_COMMIT = 14;
    public static final int V6OPTION_USER_CLASS = 15;
    public static final int V6OPTION_VENDOR_CLASS = 16;
    public static final int V6OPTION_VENDOR_OPTS = 17;
    public static final int V6OPTION_INTERFACE_ID = 18;
    public static final int V6OPTION_RECONF_MSG = 19;
    public static final int V6OPTION_RECONF_ACCEPT = 20;
    public static final int V6OPTION_SIP_SERVERS_DOMAIN_LIST = 21;
    public static final int V6OPTION_SIP_SERVERS_ADDRESS_LIST = 22;
    public static final int V6OPTION_DNS_SERVERS = 23;
    public static final int V6OPTION_DOMAIN_SEARCH_LIST = 24;
    public static final int V6OPTION_IA_PD = 25;
    public static final int V6OPTION_IA_PD_PREFIX = 26;
    public static final int V6OPTION_NIS_SERVERS = 27;
    public static final int V6OPTION_NISPLUS_SERVERS = 28;
    public static final int V6OPTION_NIS_DOMAIN_NAME = 29;
    public static final int V6OPTION_NISPLUS_DOMAIN_NAME = 30;
    public static final int V6OPTION_SNTP_SERVERS = 31;
    public static final int V6OPTION_INFO_REFRESH_TIME = 32;
    public static final int V6OPTION_BCMCS_DOMAIN_NAMES = 33;
    public static final int V6OPTION_BCMCS_ADDRESSES = 34;
    public static final int V6OPTION_GEOCONF_CIVIC = 36;
    public static final int V6OPTION_REMOTE_ID = 37;
    public static final int V6OPTION_SUBSCRIBER_ID = 38;
    public static final int V6OPTION_CLIENT_FQDN = 39;
    public static final int V6OPTION_PANA_AGENT_ADDRESSES = 40;
    public static final int V6OPTION_NEW_POSIX_TIMEZONE = 41;
    public static final int V6OPTION_NEW_TZDB_TIMEZONE = 42;
    public static final int V6OPTION_ECHO_REQUEST = 43;
    public static final int V6OPTION_LOST_SERVER_DOMAIN_NAME = 51;
    
    public static final int V6STATUS_CODE_SUCCESS = 0;
    public static final int V6STATUS_CODE_UNSPEC_FAIL = 1;
    public static final int V6STATUS_CODE_NOADDRSAVAIL = 2;
    public static final int V6STATUS_CODE_NOBINDING = 3;
    public static final int V6STATUS_CODE_NOTONLINK = 4;
    public static final int V6STATUS_CODE_USEMULTICAST = 5;
    public static final int V6STATUS_CODE_NOPREFIXAVAIL = 6;

    
    // V4 Constants
	public static final int V4_CLIENT_PORT = 68;
	public static final int V4_SERVER_PORT = 67;

	public static final int V4_OP_REQUEST = 1;
    public static final int V4_OP_REPLY = 2;

    public static final int V4MESSAGE_TYPE_DISCOVER = 1;
    public static final int V4MESSAGE_TYPE_OFFER = 2;
    public static final int V4MESSAGE_TYPE_REQUEST = 3;
    public static final int V4MESSAGE_TYPE_DECLINE = 4;
    public static final int V4MESSAGE_TYPE_ACK = 5;
    public static final int V4MESSAGE_TYPE_NAK = 6;
    public static final int V4MESSAGE_TYPE_RELEASE = 7;
    public static final int V4MESSAGE_TYPE_INFORM = 8;
    
    public static final String[] V4MESSAGE_STRING = { "Unknown",
											        "Discover",
											        "Offer",
											        "Request",
											        "Decline",
											        "Ack",
											        "Nack",
											        "Release",
											        "Inform" };
    /**
     * Get the string representation of a DHCPv4 message
     * given the message type.  Note that the type is
     * an unsigned byte, so we need a short to store
     * it properly.
     * 
     * @param msg  the message type as a short (i.e. unsigned byte)
     * 
     * @return the message string
     */
    public static final String getV4MessageString(short msg)
    {
        if ( (msg > 0) && (msg <= V4MESSAGE_TYPE_INFORM)) {
            return V4MESSAGE_STRING[msg];
        }
        return "Unknown";
    }

    
    public static final int V4OPTION_SUBNET_MASK = 1;
    public static final int V4OPTION_TIME_OFFSET = 2;
    public static final int V4OPTION_ROUTERS = 3;
    public static final int V4OPTION_TIME_SERVERS = 4;
    public static final int V4OPTION_DOMAIN_SERVERS = 6;
    public static final int V4OPTION_HOSTNAME = 12;
    public static final int V4OPTION_DOMAIN_NAME = 15;
    public static final int V4OPTION_VENDOR_INFO = 43;
    public static final int V4OPTION_NETBIOS_NAME_SERVERS = 44;
    public static final int V4OPTION_NETBIOS_NODE_TYPE = 46;
    public static final int V4OPTION_REQUESTED_IP = 50;
    public static final int V4OPTION_LEASE_TIME = 51;
    public static final int V4OPTION_MESSAGE_TYPE = 53;
    public static final int V4OPTION_SERVERID = 54;
    public static final int V4OPTION_PARAM_REQUEST_LIST = 55;
    public static final int V4OPTION_VENDOR_CLASS = 60;
    public static final int V4OPTION_CLIENT_ID = 61;
    public static final int V4OPTION_TFTP_SERVER_NAME = 66;
    public static final int V4OPTION_BOOT_FILE_NAME = 67;
    public static final int V4OPTION_RAPID_COMMIT = 80;
    public static final int V4OPTION_CLIENT_FQDN = 81;
    public static final int V4OPTION_RELAY_INFO = 82;
    public static final int V4OPTION_EOF = 255;
}
