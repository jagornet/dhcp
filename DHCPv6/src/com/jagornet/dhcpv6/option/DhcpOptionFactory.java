/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpOptionFactory.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.option;

import com.jagornet.dhcpv6.option.base.DhcpOption;
import com.jagornet.dhcpv6.util.DhcpConstants;

/**
 * A factory for creating DhcpOption objects.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpOptionFactory
{    
    /**
     * Gets the DhcpOption for the given option code.
     * 
     * @param code the DHCP option code
     * 
     * @return the DhcpOption object for the option code.
     */
    public static DhcpOption getDhcpOption(int code)
    {
        DhcpOption option = null;
        switch(code) {
            case DhcpConstants.OPTION_CLIENTID:	// 1
                option = new DhcpClientIdOption();
                break;
            case DhcpConstants.OPTION_SERVERID:	// 2
                option = new DhcpServerIdOption();
                break;
            case DhcpConstants.OPTION_IA_NA:	// 3
                option = new DhcpIaNaOption();
                break;
            case DhcpConstants.OPTION_IA_TA:	// 4
                option = new DhcpIaTaOption();
                break;
            case DhcpConstants.OPTION_IAADDR:	// 5
            	option = new DhcpIaAddrOption();
            	break;
            case DhcpConstants.OPTION_ORO:	// 6
                option = new DhcpOptionRequestOption();
                break;
            case DhcpConstants.OPTION_PREFERENCE:	// 7
                option = new DhcpPreferenceOption();
                break;
            case DhcpConstants.OPTION_ELAPSED_TIME:	// 8
                option = new DhcpElapsedTimeOption();
                break;
            case DhcpConstants.OPTION_RELAY_MSG:	// 9
                option = new DhcpRelayOption();
                break;
                
                // option code 10 is unassigned
                
            case DhcpConstants.OPTION_AUTH:		// 11
            	option = new DhcpAuthenticationOption();
            	break;
            case DhcpConstants.OPTION_UNICAST:	// 12
            	option = new DhcpServerUnicastOption();
            	break;
            case DhcpConstants.OPTION_STATUS_CODE:	// 13
                option = new DhcpStatusCodeOption();
                break;
            case DhcpConstants.OPTION_RAPID_COMMIT:	// 14
            	option = new DhcpRapidCommitOption();
            	break;
            case DhcpConstants.OPTION_USER_CLASS:	// 15
                option = new DhcpUserClassOption();
                break;
            case DhcpConstants.OPTION_VENDOR_CLASS:	// 16
                option = new DhcpVendorClassOption();
                break;
            case DhcpConstants.OPTION_VENDOR_OPTS:	// 17
                option = new DhcpVendorInfoOption();
                break;
            case DhcpConstants.OPTION_INTERFACE_ID:	// 18
                option = new DhcpInterfaceIdOption();
                break;
            case DhcpConstants.OPTION_RECONF_MSG:	// 19
            	option = new DhcpReconfigureMessageOption();
            	break;
            case DhcpConstants.OPTION_RECONF_ACCEPT:	// 20
            	option = new DhcpReconfigureAcceptOption();
                break;
            case DhcpConstants.OPTION_SIP_SERVERS_DOMAIN_LIST:	// 21
            	option = new DhcpSipServerDomainNamesOption();
            	break;
            case DhcpConstants.OPTION_SIP_SERVERS_ADDRESS_LIST:	// 22
            	option = new DhcpSipServerAddressesOption();
            	break;
            case DhcpConstants.OPTION_DNS_SERVERS:	// 23
                option = new DhcpDnsServersOption();
                break;
            case DhcpConstants.OPTION_DOMAIN_SEARCH_LIST:	// 24
                option = new DhcpDomainSearchListOption();
                break;
            case DhcpConstants.OPTION_IA_PD:	// 25
            	option = new DhcpIaPdOption();
            	break;
            case DhcpConstants.OPTION_IA_PD_PREFIX:	// 26
            	option = new DhcpIaPrefixOption();
            	break;
            case DhcpConstants.OPTION_NIS_SERVERS:	// 27
                option = new DhcpNisServersOption();
                break;
            case DhcpConstants.OPTION_NISPLUS_SERVERS:	// 28
                option = new DhcpNisPlusServersOption();
                break;
            case DhcpConstants.OPTION_NIS_DOMAIN_NAME:	// 29
                option = new DhcpNisDomainNameOption();
                break;
            case DhcpConstants.OPTION_NISPLUS_DOMAIN_NAME:	// 30
                option = new DhcpNisPlusDomainNameOption();
                break;
            case DhcpConstants.OPTION_SNTP_SERVERS:	// 31
                option = new DhcpSntpServersOption();
                break;
            case DhcpConstants.OPTION_INFO_REFRESH_TIME:	// 32
                option = new DhcpInfoRefreshTimeOption();
                break;
            case DhcpConstants.OPTION_BCMCS_DOMAIN_NAMES:	// 33
            	option = new DhcpBcmcsDomainNamesOption();
            	break;
            case DhcpConstants.OPTION_BCMCS_ADDRESSES:	// 34
            	option = new DhcpBcmcsAddressesOption();
            	break;
            	
            	// option code 35 is unassigned
            	
            case DhcpConstants.OPTION_GEOCONF_CIVIC:	// 36
            	option = new DhcpGeoconfCivicOption();
            	break;
            case DhcpConstants.OPTION_REMOTE_ID:	// 37
            	option = new DhcpRemoteIdOption();
            	break;
            case DhcpConstants.OPTION_SUBSCRIBER_ID:	// 38
            	option = new DhcpSubscriberIdOption();
            	break;
            case DhcpConstants.OPTION_CLIENT_FQDN:	// 39
            	option = new DhcpClientFqdnOption();
            	break;
            case DhcpConstants.OPTION_PANA_AGENT_ADDRESSES:	// 40
            	option = new DhcpPanaAgentAddressesOption();
            	break;
            case DhcpConstants.OPTION_NEW_POSIX_TIMEZONE:	// 41
            	option = new DhcpNewPosixTimezoneOption();
            	break;
            case DhcpConstants.OPTION_NEW_TZDB_TIMEZONE:	// 42
            	option = new DhcpNewTzdbTimezoneOption();
            	break;
            case DhcpConstants.OPTION_ECHO_REQUEST:	// 43
            	option = new DhcpEchoRequestOption();
            	break;
            	
            	// option codes 44-48 for Lease Query RFCs
            	// option codes 49-50 for Mobile IPv6 Home Information draft
            	
            case DhcpConstants.OPTION_LOST_SERVER_DOMAIN_NAME:	// 51
            	option = new DhcpLostServerDomainNameOption();
            	break; 
            default:
            	// Unknown option code, build an opaque option to hold it
                DhcpUnknownOption unknownOption = new DhcpUnknownOption();
                unknownOption.setCode(code);
                option = unknownOption;
                break;
        }
        return option;
    }
}
