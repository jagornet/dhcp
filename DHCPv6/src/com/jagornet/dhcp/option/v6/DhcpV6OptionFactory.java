/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV6OptionFactory.java is part of DHCPv6.
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
package com.jagornet.dhcp.option.v6;

import com.jagornet.dhcp.option.DhcpUnknownOption;
import com.jagornet.dhcp.option.base.DhcpOption;
import com.jagornet.dhcp.util.DhcpConstants;

/**
 * A factory for creating DhcpOption objects.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV6OptionFactory
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
                option = new DhcpV6ClientIdOption();
                break;
            case DhcpConstants.OPTION_SERVERID:	// 2
                option = new DhcpV6ServerIdOption();
                break;
            case DhcpConstants.OPTION_IA_NA:	// 3
                option = new DhcpV6IaNaOption();
                break;
            case DhcpConstants.OPTION_IA_TA:	// 4
                option = new DhcpV6IaTaOption();
                break;
            case DhcpConstants.OPTION_IAADDR:	// 5
            	option = new DhcpV6IaAddrOption();
            	break;
            case DhcpConstants.OPTION_ORO:	// 6
                option = new DhcpV6OptionRequestOption();
                break;
            case DhcpConstants.OPTION_PREFERENCE:	// 7
                option = new DhcpV6PreferenceOption();
                break;
            case DhcpConstants.OPTION_ELAPSED_TIME:	// 8
                option = new DhcpV6ElapsedTimeOption();
                break;
            case DhcpConstants.OPTION_RELAY_MSG:	// 9
                option = new DhcpV6RelayOption();
                break;
                
                // option code 10 is unassigned
                
            case DhcpConstants.OPTION_AUTH:		// 11
            	option = new DhcpV6AuthenticationOption();
            	break;
            case DhcpConstants.OPTION_UNICAST:	// 12
            	option = new DhcpV6ServerUnicastOption();
            	break;
            case DhcpConstants.OPTION_STATUS_CODE:	// 13
                option = new DhcpV6StatusCodeOption();
                break;
            case DhcpConstants.OPTION_RAPID_COMMIT:	// 14
            	option = new DhcpV6RapidCommitOption();
            	break;
            case DhcpConstants.OPTION_USER_CLASS:	// 15
                option = new DhcpV6UserClassOption();
                break;
            case DhcpConstants.OPTION_VENDOR_CLASS:	// 16
                option = new DhcpV6VendorClassOption();
                break;
            case DhcpConstants.OPTION_VENDOR_OPTS:	// 17
                option = new DhcpV6VendorInfoOption();
                break;
            case DhcpConstants.OPTION_INTERFACE_ID:	// 18
                option = new DhcpV6InterfaceIdOption();
                break;
            case DhcpConstants.OPTION_RECONF_MSG:	// 19
            	option = new DhcpV6ReconfigureMessageOption();
            	break;
            case DhcpConstants.OPTION_RECONF_ACCEPT:	// 20
            	option = new DhcpV6ReconfigureAcceptOption();
                break;
            case DhcpConstants.OPTION_SIP_SERVERS_DOMAIN_LIST:	// 21
            	option = new DhcpV6SipServerDomainNamesOption();
            	break;
            case DhcpConstants.OPTION_SIP_SERVERS_ADDRESS_LIST:	// 22
            	option = new DhcpV6SipServerAddressesOption();
            	break;
            case DhcpConstants.OPTION_DNS_SERVERS:	// 23
                option = new DhcpV6DnsServersOption();
                break;
            case DhcpConstants.OPTION_DOMAIN_SEARCH_LIST:	// 24
                option = new DhcpV6DomainSearchListOption();
                break;
            case DhcpConstants.OPTION_IA_PD:	// 25
            	option = new DhcpV6IaPdOption();
            	break;
            case DhcpConstants.OPTION_IA_PD_PREFIX:	// 26
            	option = new DhcpV6IaPrefixOption();
            	break;
            case DhcpConstants.OPTION_NIS_SERVERS:	// 27
                option = new DhcpV6NisServersOption();
                break;
            case DhcpConstants.OPTION_NISPLUS_SERVERS:	// 28
                option = new DhcpV6NisPlusServersOption();
                break;
            case DhcpConstants.OPTION_NIS_DOMAIN_NAME:	// 29
                option = new DhcpV6NisDomainNameOption();
                break;
            case DhcpConstants.OPTION_NISPLUS_DOMAIN_NAME:	// 30
                option = new DhcpV6NisPlusDomainNameOption();
                break;
            case DhcpConstants.OPTION_SNTP_SERVERS:	// 31
                option = new DhcpV6SntpServersOption();
                break;
            case DhcpConstants.OPTION_INFO_REFRESH_TIME:	// 32
                option = new DhcpV6InfoRefreshTimeOption();
                break;
            case DhcpConstants.OPTION_BCMCS_DOMAIN_NAMES:	// 33
            	option = new DhcpV6BcmcsDomainNamesOption();
            	break;
            case DhcpConstants.OPTION_BCMCS_ADDRESSES:	// 34
            	option = new DhcpV6BcmcsAddressesOption();
            	break;
            	
            	// option code 35 is unassigned
            	
            case DhcpConstants.OPTION_GEOCONF_CIVIC:	// 36
            	option = new DhcpV6GeoconfCivicOption();
            	break;
            case DhcpConstants.OPTION_REMOTE_ID:	// 37
            	option = new DhcpV6RemoteIdOption();
            	break;
            case DhcpConstants.OPTION_SUBSCRIBER_ID:	// 38
            	option = new DhcpV6SubscriberIdOption();
            	break;
            case DhcpConstants.OPTION_CLIENT_FQDN:	// 39
            	option = new DhcpV6ClientFqdnOption();
            	break;
            case DhcpConstants.OPTION_PANA_AGENT_ADDRESSES:	// 40
            	option = new DhcpV6PanaAgentAddressesOption();
            	break;
            case DhcpConstants.OPTION_NEW_POSIX_TIMEZONE:	// 41
            	option = new DhcpV6NewPosixTimezoneOption();
            	break;
            case DhcpConstants.OPTION_NEW_TZDB_TIMEZONE:	// 42
            	option = new DhcpV6NewTzdbTimezoneOption();
            	break;
            case DhcpConstants.OPTION_ECHO_REQUEST:	// 43
            	option = new DhcpV6EchoRequestOption();
            	break;
            	
            	// option codes 44-48 for Lease Query RFCs
            	// option codes 49-50 for Mobile IPv6 Home Information draft
            	
            case DhcpConstants.OPTION_LOST_SERVER_DOMAIN_NAME:	// 51
            	option = new DhcpV6LostServerDomainNameOption();
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
