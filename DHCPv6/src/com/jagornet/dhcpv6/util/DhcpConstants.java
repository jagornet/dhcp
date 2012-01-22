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
package com.jagornet.dhcpv6.util;

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
	
	/** The DHCPv6 home. */
	public static String DHCPV6_HOME = System.getProperty("dhcpv6.home");

	public static boolean IS_WINDOWS =
		System.getProperty("os.name", "").startsWith("Windows") ? true : false;
	
	public static InetAddress ZEROADDR = null;
	
    /** IPv4 Addresses. */
    public static InetAddress LOCALHOST = null;
    
    /** IPv6 Addresses. */
    public static InetAddress LOCALHOST_V6 = null;
    
    /** The BROADCAST. */
    public static InetAddress BROADCAST = null;

	/** IPv6 Multicast Addresses. */
	public static InetAddress ALL_DHCP_RELAY_AGENTS_AND_SERVERS = null;
	
	/** The All DHCP servers. */
	public static InetAddress ALL_DHCP_SERVERS = null;

	/** UDP Ports. */
	public static final int CLIENT_PORT = 546;
	
	/** The Constant SERVER_PORT. */
	public static final int SERVER_PORT = 547;

	/** DHCP Message Types - use short to support unsigned byte. */
	public static final short SOLICIT = 1;
	
	/** The Constant ADVERTISE. */
	public static final short ADVERTISE = 2;
	
	/** The Constant REQUEST. */
	public static final short REQUEST = 3;
	
	/** The Constant CONFIRM. */
	public static final short CONFIRM = 4;
	
	/** The Constant RENEW. */
	public static final short RENEW = 5;
	
	/** The Constant REBIND. */
	public static final short REBIND = 6;
	
	/** The Constant REPLY. */
	public static final short REPLY = 7;
	
	/** The Constant RELEASE. */
	public static final short RELEASE = 8;
	
	/** The Constant DECLINE. */
	public static final short DECLINE = 9;
	
	/** The Constant RECONIFURE. */
	public static final short RECONFIGURE = 10;
	
	/** The Constant INFO_REQUEST. */
	public static final short INFO_REQUEST = 11;
	
	/** The Constant RELAY_FORW. */
	public static final short RELAY_FORW = 12;
	
	/** The Constant RELAY_REPL. */
	public static final short RELAY_REPL = 13;

    /** The Constant MESSAGE_STRING. */
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
     * 
     * @param msg  the message type as a short (i.e. unsigned byte)
     * 
     * @return the message string
     */
    public static final String getMessageString(short msg)
    {
        if ( (msg > 0) && (msg <= RELAY_REPL)) {
            return MESSAGE_STRING[msg];
        }
        return "Unknown";
    }
    
	/** DHCP Options. */
    public static final int OPTION_CLIENTID = 1;
    
    /** The Constant OPTION_SERVERID. */
    public static final int OPTION_SERVERID = 2;
    
    /** The Constant OPTION_IA_NA. */
    public static final int OPTION_IA_NA = 3;
    
    /** The Constant OPTION_IA_TA. */
    public static final int OPTION_IA_TA = 4;
    
    /** The Constant OPTION_IAADDR. */
    public static final int OPTION_IAADDR = 5;
    
    /** The Constant OPTION_ORO. */
    public static final int OPTION_ORO = 6;
    
    /** The Constant OPTION_PREFERENCE. */
    public static final int OPTION_PREFERENCE = 7;
    
    /** The Constant OPTION_ELAPSED_TIME. */
    public static final int OPTION_ELAPSED_TIME = 8;
    
    /** The Constant OPTION_RELAY_MSG. */
    public static final int OPTION_RELAY_MSG = 9;
    
    // option code 10 is unassigned
    
    /** The Constant OPTION_AUTH. */
    public static final int OPTION_AUTH = 11;
    
    /** The Constant OPTION_UNICAST. */
    public static final int OPTION_UNICAST = 12;
    
    /** The Constant OPTION_STATUS_CODE. */
    public static final int OPTION_STATUS_CODE = 13;
    
    /** The Constant OPTION_RAPID_COMMIT. */
    public static final int OPTION_RAPID_COMMIT = 14;
    
    /** The Constant OPTION_USER_CLASS. */
    public static final int OPTION_USER_CLASS = 15;
    
    /** The Constant OPTION_VENDOR_CLASS. */
    public static final int OPTION_VENDOR_CLASS = 16;
    
    /** The Constant OPTION_VENDOR_OPTS. */
    public static final int OPTION_VENDOR_OPTS = 17;
    
    /** The Constant OPTION_INTERFACE_ID. */
    public static final int OPTION_INTERFACE_ID = 18;
    
    /** The Constant OPTION_RECONF_MSG. */
    public static final int OPTION_RECONF_MSG = 19;
    
    /** The Constant OPTION_RECONF_ACCEPT. */
    public static final int OPTION_RECONF_ACCEPT = 20;
    
    /** The Constant OPTION_SIP_SERVERS_DOMAIN_LIST. */
    public static final int OPTION_SIP_SERVERS_DOMAIN_LIST = 21;
    
    /** The Constant OPTION_SIP_SERVERS_ADDRESS_LIST. */
    public static final int OPTION_SIP_SERVERS_ADDRESS_LIST = 22;
    
    /** The Constant OPTION_DNS_SERVERS. */
    public static final int OPTION_DNS_SERVERS = 23;
    
    /** The Constant OPTION_DOMAIN_SEARCH_LIST. */
    public static final int OPTION_DOMAIN_SEARCH_LIST = 24;
    
    /** The Constant OPTION_IA_PD. */
    public static final int OPTION_IA_PD = 25;
    
    /** The Constant OPTION_IA_PD_PREFIX. */
    public static final int OPTION_IA_PD_PREFIX = 26;
    
    /** The Constant OPTION_NIS_SERVERS. */
    public static final int OPTION_NIS_SERVERS = 27;
    
    /** The Constant OPTION_NISPLUS_SERVERS. */
    public static final int OPTION_NISPLUS_SERVERS = 28;
    
    /** The Constant OPTION_NIS_DOMAIN_NAME. */
    public static final int OPTION_NIS_DOMAIN_NAME = 29;
    
    /** The Constant OPTION_NISPLUS_DOMAIN_NAME. */
    public static final int OPTION_NISPLUS_DOMAIN_NAME = 30;
    
    /** The Constant OPTION_SNTP_SERVERS. */
    public static final int OPTION_SNTP_SERVERS = 31;
    
    /** The Constant OPTION_INFO_REFRESH_TIME. */
    public static final int OPTION_INFO_REFRESH_TIME = 32;

    /** The Constant OPTION_BCMCS_DOMAIN_NAMES. */
    public static final int OPTION_BCMCS_DOMAIN_NAMES = 33;
    
    /** The Constant OPTION_BCMCS_ADDRESSES. */
    public static final int OPTION_BCMCS_ADDRESSES = 34;
    
    /** The Constant OPTION_GEOCONF_CIVIC. */
    public static final int OPTION_GEOCONF_CIVIC = 36;
    
    /** The Constant OPTION_REMOTE_ID. */
    public static final int OPTION_REMOTE_ID = 37;
    
    /** The Constant OPTION_SUBSCRIBER_ID. */
    public static final int OPTION_SUBSCRIBER_ID = 38;
    
    /** The Constant OPTION_CLIENT_FQDN. */
    public static final int OPTION_CLIENT_FQDN = 39;
    
    /** The Constant OPTION_PANA_AGENT_ADDRESSES. */
    public static final int OPTION_PANA_AGENT_ADDRESSES = 40;
    
    /** The Constant OPTION_NEW_POSIX_TIMEZONE. */
    public static final int OPTION_NEW_POSIX_TIMEZONE = 41;
    
    /** The Constant OPTION_NEW_TZDB_TIMEZONE. */
    public static final int OPTION_NEW_TZDB_TIMEZONE = 42;
    
    /** The Constant OPTION_ECHO_REQUEST. */
    public static final int OPTION_ECHO_REQUEST = 43;
    
    /** The Constant OPTION_LOST_SERVER_DOMAIN_NAME. */
    public static final int OPTION_LOST_SERVER_DOMAIN_NAME = 51;
    
    /** The Constant STATUS_CODE_SUCCESS. */
    public static final int STATUS_CODE_SUCCESS = 0;
    
    /** The Constant STATUS_CODE_UNSPEC_FAIL. */
    public static final int STATUS_CODE_UNSPEC_FAIL = 1;
    
    /** The Constant STATUS_CODE_NOADDRSAVAIL. */
    public static final int STATUS_CODE_NOADDRSAVAIL = 2;
    
    /** The Constant STATUS_CODE_NOBINDING. */
    public static final int STATUS_CODE_NOBINDING = 3;
    
    /** The Constant STATUS_CODE_NOTONLINK. */
    public static final int STATUS_CODE_NOTONLINK = 4;
    
    /** The Constant STATUS_CODE_USEMULTICAST. */
    public static final int STATUS_CODE_USEMULTICAST = 5;
    
    /** The Constant STATUS_CODE_NOPREFIXAVAIL. */
    public static final int STATUS_CODE_NOPREFIXAVAIL = 6;

    static {
		try {
            LOCALHOST = InetAddress.getByName("127.0.0.1");
            LOCALHOST_V6 = InetAddress.getByName("::1");
            BROADCAST = InetAddress.getByName("255.255.255.255");
			ALL_DHCP_RELAY_AGENTS_AND_SERVERS = InetAddress.getByName("FF02::1:2");
			ALL_DHCP_SERVERS = InetAddress.getByName("FF05::1:3");
			ZEROADDR = InetAddress.getByName("0.0.0.0");
		}
		catch (UnknownHostException ex) { 
			log.error("Failed to initialize IP constants: " + ex);
		}
	}
    
    // V4 Constants
	public static final int V4_CLIENT_PORT = 68;
	public static final int V4_SERVER_PORT = 67;

	public static final int OP_REQUEST = 1;
    public static final int OP_REPLY = 2;
    
    public static final int V4OPTION_SUBNET_MASK = 1;
    public static final int V4OPTION_TIME_OFFSET = 2;
    public static final int V4OPTION_ROUTERS = 3;
    public static final int V4OPTION_TIME_SERVERS = 4;
    public static final int V4OPTION_DOMAIN_SERVERS = 6;
    public static final int V4OPTION_HOSTNAME = 12;
    public static final int V4OPTION_DOMAIN_NAME = 15;
    public static final int V4OPTION_VENDOR_INFO = 43;
    public static final int V4OPTION_REQUESTED_IP = 50;
    public static final int V4OPTION_LEASE_TIME = 51;
    public static final int V4OPTION_MESSAGE_TYPE = 53;
    public static final int V4OPTION_SERVERID = 54;
    public static final int V4OPTION_PARAM_REQUEST_LIST = 55;
    public static final int V4OPTION_VENDOR_CLASS = 60;
    public static final int V4OPTION_RAPID_COMMIT = 80;
    public static final int V4OPTION_CLIENT_FQDN = 81;
    public static final int V4OPTION_EOF = 255;

    public static final int V4MESSAGE_TYPE_DISCOVER = 1;
    public static final int V4MESSAGE_TYPE_OFFER = 2;
    public static final int V4MESSAGE_TYPE_REQUEST = 3;
    public static final int V4MESSAGE_TYPE_DECLINE = 4;
    public static final int V4MESSAGE_TYPE_ACK = 5;
    public static final int V4MESSAGE_TYPE_NACK = 6;
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

}
