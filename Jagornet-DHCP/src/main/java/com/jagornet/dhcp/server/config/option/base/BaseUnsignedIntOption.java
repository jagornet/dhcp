/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseUnsignedIntOption.java is part of Jagornet DHCP.
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
import com.jagornet.dhcp.xml.UnsignedIntOptionType;

/**
 * Title: BaseUnsignedIntOption
 * Description: The abstract base class for unsigned integer DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseUnsignedIntOption 
				extends com.jagornet.dhcp.option.base.BaseUnsignedIntOption 
				implements DhcpComparableOption
{ 
	private static Logger log = LoggerFactory.getLogger(BaseUnsignedIntOption.class);
    
    /**
     * Instantiates a new dhcp unsigned int option.
     */
    public BaseUnsignedIntOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new dhcp unsigned int option.
     * 
     * @param unsignedIntOption the unsigned int option
     */
    public BaseUnsignedIntOption(UnsignedIntOptionType uIntOption)
    {
        super();
        if (uIntOption != null) {
            super.setUnsignedInt(uIntOption.getUnsignedInt());
        }
    }

    @Override
    public boolean matches(OptionExpression expression)
    {
        if (expression == null)
            return false;
        if (expression.getCode() != this.getCode())
            return false;
        
        UnsignedIntOptionType exprOption = expression.getUIntOption();
        if (exprOption != null) {
        	long exprUint = exprOption.getUnsignedInt();
        	Operator op = expression.getOperator();
        	if (op.equals(Operator.EQUALS)) {
        		return (unsignedInt == exprUint);
        	}
        	else if (op.equals(Operator.LESS_THAN)) {
        		return (unsignedInt < exprUint);
        	}
        	else if (op.equals(Operator.LESS_THAN_OR_EQUAL)) {
        		return (unsignedInt <= exprUint);
        	}
        	else if (op.equals(Operator.GREATER_THAN)) {
        		return (unsignedInt > exprUint);
        	}
        	else if (op.equals(Operator.GREATER_THAN_OR_EQUAL)) {
        		return (unsignedInt >= exprUint);
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
	                	// need a long to handle unsigned int
	                    if (unsignedInt == Long.parseLong(ascii)) {
	                        return true;
	                    }
	                }
	                catch (NumberFormatException ex) {
	                	log.error("Invalid unsigned byte ASCII value for OpaqueData: " + ascii, ex);
	                }
	            }
	            else {
	                byte[] hex = opaque.getHexValue();
	                if ( (hex != null) && 
	                     (hex.length >= 1) && (hex.length <= 4) ) {
	                	long hexLong = Long.valueOf(Util.toHexString(hex), 16);
	                    if (unsignedInt == hexLong) {
	                        return true;
	                    }
	                }
	            }
	        }
        }
        return false;
    }

}
