/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseIpAddressListOption.java is part of Jagornet DHCP.
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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.config.option.DhcpComparableOption;
import com.jagornet.dhcp.xml.IpAddressListOptionType;
import com.jagornet.dhcp.xml.Operator;
import com.jagornet.dhcp.xml.OptionExpression;

/**
 * Title: BaseIpAddressListOption
 * Description: The abstract base class for server address list DHCP options.
 * 
 * @author A. Gregory Rabil
 */

public abstract class BaseIpAddressListOption 
				extends com.jagornet.dhcp.option.base.BaseIpAddressListOption 
				implements DhcpComparableOption
{
	private static Logger log = LoggerFactory.getLogger(BaseIpAddressListOption.class);
	
	public BaseIpAddressListOption()
	{
		this(null);
	}
	
	public BaseIpAddressListOption(IpAddressListOptionType ipAddressListOption)
	{
		super();
		if (ipAddressListOption != null) {
			if (ipAddressListOption.getIpAddressList() != null) {
				super.setIpAddressList(ipAddressListOption.getIpAddressList());
			}
		}
	}
	
	@Override
    public boolean matches(OptionExpression expression)
    {
        if (expression == null)
            return false;
        if (expression.getCode() != this.getCode())
            return false;
        if (ipAddressList == null)
        	return false;

        // first see if we have a ip address list option to compare to
        IpAddressListOptionType exprOption = expression.getIpAddressListOption();
        if (exprOption != null) {
        	List<String> exprIpAddresses = exprOption.getIpAddressList();
            Operator op = expression.getOperator();
            if (op.equals(Operator.EQUALS)) {
            	return ipAddressList.equals(exprIpAddresses);
            }
            else if (op.equals(Operator.CONTAINS)) {
            	return ipAddressList.containsAll(exprIpAddresses);
            }
            else {
            	log.warn("Unsupported expression operator: " + op);
            }
        }
        
        return false;
    }
}
