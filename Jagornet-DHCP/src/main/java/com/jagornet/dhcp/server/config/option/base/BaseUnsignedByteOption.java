/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseUnsignedByteOption.java is part of Jagornet DHCP.
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
import com.jagornet.dhcp.xml.UnsignedByteOptionType;

/**
 * Title: BaseUnsignedByteOption
 * Description: The abstract base class for unsigned byte DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseUnsignedByteOption 
				extends com.jagornet.dhcp.option.base.BaseUnsignedByteOption 
				implements DhcpComparableOption
{
	private static Logger log = LoggerFactory.getLogger(BaseUnsignedByteOption.class);

    /**
     * Instantiates a new unsigned byte option.
     */
    public BaseUnsignedByteOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new unsigned byte option.
     * 
     * @param uByteOption the unsigned byte option
     */
    public BaseUnsignedByteOption(UnsignedByteOptionType uByteOption)
    {
        super();
        if (uByteOption != null) {
        	super.setUnsignedByte(uByteOption.getUnsignedByte());
        }
    }

    @Override
    public boolean matches(OptionExpression expression)
    {
        if (expression == null)
            return false;
        if (expression.getCode() != this.getCode())
            return false;
        
        UnsignedByteOptionType exprOption = expression.getUByteOption();
        if (exprOption != null) {
        	short exprUbyte = exprOption.getUnsignedByte();
        	Operator op = expression.getOperator();
        	if (op.equals(Operator.EQUALS)) {
        		return (unsignedByte == exprUbyte);
        	}
        	else if (op.equals(Operator.LESS_THAN)) {
        		return (unsignedByte < exprUbyte);
        	}
        	else if (op.equals(Operator.LESS_THAN_OR_EQUAL)) {
        		return (unsignedByte <= exprUbyte);
        	}
        	else if (op.equals(Operator.GREATER_THAN)) {
        		return (unsignedByte > exprUbyte);
        	}
        	else if (op.equals(Operator.GREATER_THAN_OR_EQUAL)) {
        		return (unsignedByte >= exprUbyte);
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
	                	// need an Short to handle unsigned byte
	                    if (unsignedByte == Short.parseShort(ascii)) {
	                        return true;
	                    }
	                }
	                catch (NumberFormatException ex) { 
	                	log.error("Invalid unsigned byte ASCII value for OpaqueData: " + ascii, ex);
	                }
	            }
	            else {
	                byte[] hex = opaque.getHexValue();
	                if ((hex != null) && (hex.length == 1)) {
	                	int hexUnsignedByte = Short.valueOf(Util.toHexString(hex), 16);
	                    if (unsignedByte == hexUnsignedByte) {
	                        return true;
	                    }
	                }
	            }
	        }
        }
        return false;
    }
    
}
