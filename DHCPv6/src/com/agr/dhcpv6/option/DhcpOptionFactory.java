package com.agr.dhcpv6.option;

import java.net.InetAddress;

import com.agr.dhcpv6.util.DhcpConstants;

public class DhcpOptionFactory
{
    public static DhcpOption getDhcpOption(short code, InetAddress peer)
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
                option = new DhcpRelayOption(peer);
                break;
            default:
//                option = new DhcpOption();
//                option.setCode(code);
                break;
        }
        return option;
    }
}
