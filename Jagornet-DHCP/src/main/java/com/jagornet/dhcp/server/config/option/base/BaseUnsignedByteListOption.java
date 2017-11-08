/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseUnsignedByteListOption.java is part of Jagornet DHCP.
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
import com.jagornet.dhcp.xml.UnsignedByteListOptionType;

/**
 * Title: BaseUnsignedByteListOption
 * Description: The abstract base class for unsigned short list DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseUnsignedByteListOption 
				extends com.jagornet.dhcp.option.base.BaseUnsignedByteListOption 
				implements DhcpComparableOption
{
	private static Logger log = LoggerFactory.getLogger(BaseUnsignedByteListOption.class);

    /**
     * Instantiates a new unsigned short list option.
     */
    public BaseUnsignedByteListOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new unsigned short list option.
     * 
     * @param uByteListOption the option request option
     */
    public BaseUnsignedByteListOption(UnsignedByteListOptionType uByteListOption)
    {
        super();
        if (uByteListOption != null) {
            if (uByteListOption.getUnsignedByteList() != null) {
            	super.setUnsignedByteList(uByteListOption.getUnsignedByteList());
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
        if (unsignedByteList == null)
        	return false;
        
        UnsignedByteListOptionType exprOption = expression.getUByteListOption();
        if (exprOption != null) {
        	List<Short> exprUbytes = exprOption.getUnsignedByteList();
            Operator op = expression.getOperator();
            if (op.equals(Operator.EQUALS)) {
            	return unsignedByteList.equals(exprUbytes);
            }
            else if (op.equals(Operator.CONTAINS)) {
            	return unsignedByteList.containsAll(exprUbytes);
            }
            else {
            	log.warn("Unsupported expression operator: " + op);
            }
        }
        
        return false;
    }
    
}
