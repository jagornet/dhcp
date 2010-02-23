/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpClientIdOption.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.option.base.BaseOpaqueDataOption;
import com.jagornet.dhcpv6.xml.ClientIdOption;

/**
 * <p>Title: DhcpClientIdOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpClientIdOption extends BaseOpaqueDataOption
{
	public DhcpClientIdOption()
	{
		this(null);
	}
	
	public DhcpClientIdOption(ClientIdOption clientIdOption)
	{
		if (clientIdOption != null) {
			this.opaqueDataOption = clientIdOption;
		}
		else {
			this.opaqueDataOption = ClientIdOption.Factory.newInstance();
            // create an OpaqueData element to actually hold the data
            this.opaqueDataOption.addNewOpaqueData();
		}
		
	}
	
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return ((ClientIdOption)opaqueDataOption).getCode();
    }
    
    /**
     * Convenience method to get the DUID bytes.
     */
    public byte[] getDuid()
    {
		if ((this.getOpaqueDataOptionType() != null) &&
				(this.getOpaqueDataOptionType().getOpaqueData() != null)) {
			return this.getOpaqueDataOptionType().getOpaqueData().getHexValue();
		}
		return null;
	}
    
}
