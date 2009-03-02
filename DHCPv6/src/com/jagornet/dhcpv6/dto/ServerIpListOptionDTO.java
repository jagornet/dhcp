/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file ServerIpListOptionDTO.java is part of DHCPv6.
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
 *   This file ServerIpListOptionDTO.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.dto;

import java.util.List;

/**
 * The abstract Class ServerIpListOptionDTO.
 * 
 * @author A. Gregory Rabil
 */
public abstract class ServerIpListOptionDTO extends BaseOptionDTO
{   
    /** The server ip addresses. */
    protected List<String> serverIpAddresses;

    /**
     * Gets the server ip addresses.
     * 
     * @return the server ip addresses
     */
    public List<String> getServerIpAddresses()
    {
        return serverIpAddresses;
    }

    /**
     * Sets the server ip addresses.
     * 
     * @param serverIpAddresses the new server ip addresses
     */
    public void setServerIpAddresses(List<String> serverIpAddresses)
    {
        this.serverIpAddresses = serverIpAddresses;
    }
}
