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
            case DhcpConstants.OPTION_CLIENTID:
                option = new DhcpClientIdOption();
                break;
            case DhcpConstants.OPTION_SERVERID:
                option = new DhcpServerIdOption();
                break;
            case DhcpConstants.OPTION_IA_NA:
                option = new DhcpIaNaOption();
                break;
            case DhcpConstants.OPTION_IA_TA:
                option = new DhcpIaNaOption();
                break;
            case DhcpConstants.OPTION_ORO:
                option = new DhcpOptionRequestOption();
                break;
            case DhcpConstants.OPTION_PREFERENCE:
                option = new DhcpPreferenceOption();
                break;
            case DhcpConstants.OPTION_ELAPSED_TIME:
                option = new DhcpElapsedTimeOption();
                break;
            case DhcpConstants.OPTION_STATUS_CODE:
                option = new DhcpStatusCodeOption();
                break;
            case DhcpConstants.OPTION_USER_CLASS:
                option = new DhcpUserClassOption();
                break;
            case DhcpConstants.OPTION_VENDOR_CLASS:
                option = new DhcpVendorClassOption();
                break;
            case DhcpConstants.OPTION_VENDOR_OPTS:
                option = new DhcpVendorInfoOption();
                break;
            case DhcpConstants.OPTION_INTERFACE_ID:
                option = new DhcpInterfaceIdOption();
                break;
            case DhcpConstants.OPTION_SIP_SERVERS_DOMAIN_LIST:
            	option = new DhcpSipServerDomainNamesOption();
            	break;
            case DhcpConstants.OPTION_SIP_SERVERS_ADDRESS_LIST:
            	option = new DhcpSipServerAddressesOption();
            	break;
            case DhcpConstants.OPTION_DNS_SERVERS:
                option = new DhcpDnsServersOption();
                break;
            case DhcpConstants.OPTION_DOMAIN_SEARCH_LIST:
                option = new DhcpDomainSearchListOption();
                break;
            case DhcpConstants.OPTION_NIS_SERVERS:
                option = new DhcpNisServersOption();
                break;
            case DhcpConstants.OPTION_NISPLUS_SERVERS:
                option = new DhcpNisPlusServersOption();
                break;
            case DhcpConstants.OPTION_NIS_DOMAIN_NAME:
                option = new DhcpNisDomainNameOption();
                break;
            case DhcpConstants.OPTION_NISPLUS_DOMAIN_NAME:
                option = new DhcpNisPlusDomainNameOption();
                break;
            case DhcpConstants.OPTION_SNTP_SERVERS:
                option = new DhcpSntpServersOption();
                break;
            case DhcpConstants.OPTION_INFO_REFRESH_TIME:
                option = new DhcpInfoRefreshTimeOption();
                break;
            case DhcpConstants.OPTION_RELAY_MSG:
                option = new DhcpRelayOption();
                break;
            default:
//                option = new DhcpOption();
//                option.setCode(code);
                break;
        }
        return option;
    }
}
