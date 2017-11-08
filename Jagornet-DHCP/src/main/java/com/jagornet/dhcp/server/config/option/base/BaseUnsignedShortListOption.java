/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseUnsignedShortListOption.java is part of Jagornet DHCP.
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
import com.jagornet.dhcp.xml.Operator;
import com.jagornet.dhcp.xml.OptionExpression;
import com.jagornet.dhcp.xml.UnsignedShortListOptionType;

/**
 * Title: BaseUnsignedShortListOption
 * Description: The abstract base class for unsigned short list DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseUnsignedShortListOption 
				extends com.jagornet.dhcp.option.base.BaseUnsignedShortListOption 
				implements DhcpComparableOption
{
	private static Logger log = LoggerFactory.getLogger(BaseUnsignedShortListOption.class);

    /**
     * Instantiates a new unsigned short list option.
     */
    public BaseUnsignedShortListOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new unsigned short list option.
     * 
     * @param uShortListOption the option request option
     */
    public BaseUnsignedShortListOption(UnsignedShortListOptionType uShortListOption)
    {
        super();
        if (uShortListOption != null) {
            if (uShortListOption.getUnsignedShortList() != null) {
            	super.setUnsignedShortList(uShortListOption.getUnsignedShortList());
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
        if (unsignedShortList == null)
        	return false;
        
        UnsignedShortListOptionType exprOption = expression.getUShortListOption();
        if (exprOption != null) {
        	List<Integer> exprUshorts = exprOption.getUnsignedShortList();
            Operator op = expression.getOperator();
            if (op.equals(Operator.EQUALS)) {
            	return unsignedShortList.equals(exprUshorts);
            }
            else if (op.equals(Operator.CONTAINS)) {
            	return unsignedShortList.containsAll(exprUshorts);
            }
            else {
            	log.warn("Unsupported expression operator: " + op);
            }
        }
        
        return false;
    }
    
}
