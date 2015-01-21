/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV4OptionFactory.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.option.v4;

import com.jagornet.dhcp.option.DhcpUnknownOption;
import com.jagornet.dhcp.option.base.DhcpOption;
import com.jagornet.dhcp.util.DhcpConstants;

/**
 * A factory for creating DhcpOption objects.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpV4OptionFactory
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
        	case DhcpConstants.V4OPTION_SUBNET_MASK:	// 1
        		option = new DhcpV4SubnetMaskOption();
        		break;
	        case DhcpConstants.V4OPTION_TIME_OFFSET:	// 2
	        	option = new DhcpV4TimeOffsetOption();
	        	break;
	        case DhcpConstants.V4OPTION_ROUTERS:		// 3
	        	option = new DhcpV4RoutersOption();
	        	break;
	        case DhcpConstants.V4OPTION_TIME_SERVERS: 	// 4
	        	option = new DhcpV4TimeServersOption();
	        	break;
	        case DhcpConstants.V4OPTION_DOMAIN_SERVERS:	// 6
	        	option = new DhcpV4DomainServersOption();
	        	break;
	        case DhcpConstants.V4OPTION_HOSTNAME:		// 12
	        	option = new DhcpV4HostnameOption();
	        	break;
	        case DhcpConstants.V4OPTION_DOMAIN_NAME:	// 15
	        	option = new DhcpV4DomainNameOption();
	        	break;
	        case DhcpConstants.V4OPTION_VENDOR_INFO:	// 43
	        	option = new DhcpV4VendorSpecificOption();
	        	break;
	        case DhcpConstants.V4OPTION_NETBIOS_NAME_SERVERS:	// 44
	        	option = new DhcpV4NetbiosNameServersOption();
	        	break;
	        case DhcpConstants.V4OPTION_NETBIOS_NODE_TYPE:	// 46
	        	option = new DhcpV4NetbiosNodeTypeOption();
	        	break;
	        case DhcpConstants.V4OPTION_REQUESTED_IP:	// 50
	            option = new DhcpV4RequestedIpAddressOption();
	            break;
	        case DhcpConstants.V4OPTION_LEASE_TIME:		// 51
	            option = new DhcpV4LeaseTimeOption();
	            break;
            case DhcpConstants.V4OPTION_MESSAGE_TYPE:	// 53
                option = new DhcpV4MsgTypeOption();
                break;
            case DhcpConstants.V4OPTION_SERVERID:		// 54
                option = new DhcpV4ServerIdOption();
                break;
            case DhcpConstants.V4OPTION_PARAM_REQUEST_LIST:		// 55
            	option = new DhcpV4ParamRequestOption();
            	break;
            case DhcpConstants.V4OPTION_VENDOR_CLASS:	// 60
                option = new DhcpV4VendorClassOption();
                break;
            case DhcpConstants.V4OPTION_CLIENT_ID:		// 61
                option = new DhcpV4ClientIdOption();
                break;
	        case DhcpConstants.V4OPTION_TFTP_SERVER_NAME:	// 66
	        	option = new DhcpV4TftpServerNameOption();
	        	break;
	        case DhcpConstants.V4OPTION_BOOT_FILE_NAME:	// 67
	        	option = new DhcpV4BootFileNameOption();
	        	break;
            case DhcpConstants.V4OPTION_CLIENT_FQDN:	// 81
                option = new DhcpV4ClientFqdnOption();
                break;
            case DhcpConstants.V4OPTION_EOF:			// 255
            	break;
            default:
            	// Unknown option code, build an opaque option to hold it
                DhcpUnknownOption unknownOption = new DhcpUnknownOption();
                unknownOption.setCode(code);
                unknownOption.setV4(true);
                option = unknownOption;
                break;
        }
        return option;
    }
}
