/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseUnsignedShortOption.java is part of Jagornet DHCP.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcp.server.config.option.DhcpComparableOption;
import com.jagornet.dhcp.util.Util;
import com.jagornet.dhcp.xml.OpaqueData;
import com.jagornet.dhcp.xml.OpaqueDataOptionType;
import com.jagornet.dhcp.xml.Operator;
import com.jagornet.dhcp.xml.OptionExpression;
import com.jagornet.dhcp.xml.UnsignedShortOptionType;

/**
 * Title: BaseUnsignedShortOption
 * Description: The abstract base class for unsigned short DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseUnsignedShortOption 
				extends com.jagornet.dhcp.option.base.BaseUnsignedShortOption 
				implements DhcpComparableOption
{ 
	private static Logger log = LoggerFactory.getLogger(BaseUnsignedShortOption.class);
    
    /**
     * Instantiates a new unsigned short option.
     */
    public BaseUnsignedShortOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new unsigned short option.
     * 
     * @param uShortOption the elapsed time option
     */
    public BaseUnsignedShortOption(UnsignedShortOptionType uShortOption)
    {
        super();
        if (uShortOption != null) {
            super.setUnsignedShort(uShortOption.getUnsignedShort());
        }
    }

    @Override
    public boolean matches(OptionExpression expression)
    {
        if (expression == null)
            return false;
        if (expression.getCode() != this.getCode())
            return false;
        
        UnsignedShortOptionType exprOption = expression.getUShortOption();
        if (exprOption != null) {
        	int exprUshort = exprOption.getUnsignedShort();
        	Operator op = expression.getOperator();
        	if (op.equals(Operator.EQUALS)) {
        		return (unsignedShort == exprUshort);
        	}
        	else if (op.equals(Operator.LESS_THAN)) {
        		return (unsignedShort < exprUshort);
        	}
        	else if (op.equals(Operator.LESS_THAN_OR_EQUAL)) {
        		return (unsignedShort <= exprUshort);
        	}
        	else if (op.equals(Operator.GREATER_THAN)) {
        		return (unsignedShort > exprUshort);
        	}
        	else if (op.equals(Operator.GREATER_THAN_OR_EQUAL)) {
        		return (unsignedShort >= exprUshort);
        	}
            else {
            	log.warn("Unsupported expression operator: " + op);
            }
        }
        
        // then see if we have an opaque option
        OpaqueDataOptionType opaqueOption = expression.getOpaqueDataOption();
        if (opaqueOption != null) {
	        OpaqueData opaque = opaqueOption.getOpaqueData();
	        if (opaque != null) {
	            String ascii = opaque.getAsciiValue();
	            if (ascii != null) {
	                try {
	                	// need an Integer to handle unsigned short
	                    if (unsignedShort == Integer.parseInt(ascii)) {
	                        return true;
	                    }
	                }
	                catch (NumberFormatException ex) { 
	                	log.error("Invalid unsigned short ASCII value for OpaqueData: " + ascii, ex);
	                }
	            }
	            else {
	                byte[] hex = opaque.getHexValue();
	                if ( (hex != null) && 
	                     (hex.length >= 1) && (hex.length <= 2) ) {
	                	int hexUnsignedShort = Integer.valueOf(Util.toHexString(hex), 16);
	                    if (unsignedShort == hexUnsignedShort) {
	                        return true;
	                    }
	                }
	            }
	        }
        }
        return false;
    }
    
}
