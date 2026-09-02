/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV4RelayAgentInfoOption.java is part of Jagornet DHCP.
 *
 *   Jagornet DHCP is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Jagornet DHCP is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Jagornet DHCP.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcp.core.option.v4;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.core.option.base.BaseOpaqueDataOption;
import com.jagornet.dhcp.core.util.DhcpConstants;

/**
 * DhcpV4RelayAgentInfoOption (Option 82)
 * Represents the DHCPv4 Relay Agent Information Option (RFC 3046)
 * and provides helper methods for decoding suboptions such as Link Selection (RFC 3527)
 * and Server Identifier Override (RFC 5107).
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4RelayAgentInfoOption extends BaseOpaqueDataOption {

    private static Logger log = LoggerFactory.getLogger(DhcpV4RelayAgentInfoOption.class);

    public DhcpV4RelayAgentInfoOption() {
        super();
        this.code = DhcpConstants.V4OPTION_RELAY_INFO;
        this.name = "Relay-Agent-Information";
        this.setV4(true);
    }

    /**
     * Get raw payload bytes of this Relay Agent Info option.
     * 
     * @return payload byte array or null
     */
    public byte[] getRelayData() {
        if (opaqueData != null) {
            if (opaqueData.getHex() != null) {
                return opaqueData.getHex();
            } else if (opaqueData.getAscii() != null) {
                return opaqueData.getAscii().getBytes();
            }
        }
        return null;
    }

    /**
     * Get suboption payload bytes for the given suboption code.
     * 
     * @param subOptCode the suboption code
     * @return the suboption byte array, or null if not present
     */
    public byte[] getSuboptionData(int subOptCode) {
        byte[] relayData = getRelayData();
        if (relayData != null) {
            int offset = 0;
            while (offset < relayData.length) {
                int code = relayData[offset++] & 0xFF;
                if (offset >= relayData.length) break;
                int len = relayData[offset++] & 0xFF;
                if (offset + len > relayData.length) break;

                if (code == subOptCode) {
                    byte[] subData = new byte[len];
                    System.arraycopy(relayData, offset, subData, 0, len);
                    return subData;
                }
                offset += len;
            }
        }
        return null;
    }

    /**
     * Get suboption value as an IPv4 InetAddress (for 4-byte IP suboptions).
     * 
     * @param subOptCode the suboption code
     * @return InetAddress or null
     */
    public InetAddress getSuboptionIpAddress(int subOptCode) {
        byte[] data = getSuboptionData(subOptCode);
        if (data != null && data.length == 4) {
            try {
                return InetAddress.getByAddress(data);
            } catch (UnknownHostException e) {
                log.error("Invalid IP address in suboption " + subOptCode + ": " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Helper for Link Selection Sub-option (Suboption 5, RFC 3527).
     * 
     * @return InetAddress or null
     */
    public InetAddress getLinkSelectionAddress() {
        return getSuboptionIpAddress(DhcpConstants.V4OPTION_RELAY_INFO_LINK_SELECTION);
    }

    /**
     * Helper for Server Identifier Override Sub-option (Suboption 11, RFC 5107).
     * 
     * @return InetAddress or null
     */
    public InetAddress getServerIdOverrideAddress() {
        return getSuboptionIpAddress(DhcpConstants.V4OPTION_RELAY_INFO_SERVER_ID_OVERRIDE);
    }
}
