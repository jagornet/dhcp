/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseOpaqueDataOption.java is part of Jagornet DHCP.
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
package com.jagornet.dhcp.server.config.option.base;

import com.jagornet.dhcp.server.config.option.DhcpComparableOption;
import com.jagornet.dhcp.server.config.option.OpaqueDataUtil;
import com.jagornet.dhcp.xml.OpaqueData;
import com.jagornet.dhcp.xml.OpaqueDataOptionType;
import com.jagornet.dhcp.xml.OptionExpression;

/**
 * Title: BaseOpaqueDataOption
 * Description: The abstract base class for opaque opaqueData DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public class BaseOpaqueDataOption 
				extends com.jagornet.dhcp.option.base.BaseOpaqueDataOption 
				implements DhcpComparableOption
{
    
    /**
     * Instantiates a new opaque opaqueData option.
     * 
     * @param opaqueDataOption the opaque opaqueData option
     */
    public BaseOpaqueDataOption(OpaqueDataOptionType opaqueDataOption)
    {
    	this(opaqueDataOption.getOpaqueData());
    }
    
    public BaseOpaqueDataOption(OpaqueData opaqueData) {
        super();
    	if (opaqueData != null) {
    		super.setOpaqueData(new BaseOpaqueData(opaqueData));
    	}
    }

    @Override
    public boolean matches(OptionExpression expression)
    {
        if (expression == null)
            return false;
        if (expression.getCode() != this.getCode())
            return false;

        return OpaqueDataUtil.matches(expression, opaqueData);
    }

}
