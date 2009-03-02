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

import java.net.*;

/**
 * The Class DhcpConstants.
 * Defines standard DHCPv6 constants
 * 
 * @author A. Gregory Rabil
 */
public class DhcpConstants
{
	/** The DHCPv6 home. */
	public static String DHCPV6_HOME = System.getProperty("dhcpv6.home");

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

    static {
		try {
            LOCALHOST = InetAddress.getByName("127.0.0.1");
            LOCALHOST_V6 = InetAddress.getByName("::1");
            BROADCAST = InetAddress.getByName("255.255.255.255");
			ALL_DHCP_RELAY_AGENTS_AND_SERVERS = InetAddress.getByName("FF02::1:2");
			ALL_DHCP_SERVERS = InetAddress.getByName("FF05::1:3");
		}
		catch (UnknownHostException ex) { }
	}
}
