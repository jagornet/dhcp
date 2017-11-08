/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseOpaqueDataListOption.java is part of Jagornet DHCP.
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

import com.jagornet.dhcp.server.config.option.DhcpComparableOption;
import com.jagornet.dhcp.server.config.option.OpaqueDataUtil;
import com.jagornet.dhcp.xml.OpaqueData;
import com.jagornet.dhcp.xml.OpaqueDataListOptionType;
import com.jagornet.dhcp.xml.Operator;
import com.jagornet.dhcp.xml.OptionExpression;

/**
 * Title: BaseOpaqueDataListOption
 * Description: The abstract base class for opaque opaqueDataList DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public class BaseOpaqueDataListOption 
				extends com.jagornet.dhcp.option.base.BaseOpaqueDataListOption
				implements DhcpComparableOption
{
    /**
     * Instantiates a new opaque opaqueData list option.
     */
    public BaseOpaqueDataListOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new opaque opaqueData list option.
     * 
     * @param opaqueDataListOption the opaque opaqueData list option
     */
    public BaseOpaqueDataListOption(OpaqueDataListOptionType opaqueDataListOption)
    {
        super();
        if (opaqueDataListOption != null) {
        	List<OpaqueData> _opaqueDataList = opaqueDataListOption.getOpaqueDataList();
        	if (_opaqueDataList != null) {
        		for (OpaqueData opaqueData : _opaqueDataList) {
					super.addOpaqueData(new BaseOpaqueData(opaqueData));
				}
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

        return matches(expression.getOpaqueDataListOption(), expression.getOperator());
    }

	public boolean matches(OpaqueDataListOptionType that, Operator op) 
	{
		if (that != null) {
        	List<OpaqueData> thatOpaqueDataList = that.getOpaqueDataList();
        	return OpaqueDataUtil.matches(this.getOpaqueDataList(), thatOpaqueDataList, op);
        }		
		return false;
	}

	/*
	public boolean matches(BaseOpaqueDataListOption that, Operator op) 
	{
		if (this.getOpaqueDataList() == null)
        	return false;
	
		if (that != null) {
        	List<com.jagornet.dhcp.option.base.BaseOpaqueData> opaqueList = 
        			that.getOpaqueDataList();
        	if (opaqueList != null) {
        		if (op.equals(Operator.EQUALS)) {
	        		if (opaqueList.size() != this.getOpaqueDataList().size()) {
	        			return false;
	        		}
	        		for (int i=0; i<opaqueList.size(); i++) {
	        			com.jagornet.dhcp.option.base.BaseOpaqueData opaque = 
	        					opaqueList.get(i);
	        			com.jagornet.dhcp.option.base.BaseOpaqueData myOpaque = 
	        					this.getOpaqueDataList().get(i);
	        			if (!OpaqueDataUtil.equals(opaque, myOpaque)) {
	        				return false;
	        			}
	        		}
	        		return true;
        		}
        		else if (op.equals(Operator.CONTAINS)) {
        			if (opaqueList.size() > this.getOpaqueDataList().size()) {
        				return false;
        			}
        			for (int i=0; i<opaqueList.size(); i++) {
        				com.jagornet.dhcp.option.base.BaseOpaqueData opaque = 
        						opaqueList.get(i);
	        			boolean found = false;
        				for (int j=0; j<this.getOpaqueDataList().size(); j++) {
        					com.jagornet.dhcp.option.base.BaseOpaqueData myOpaque = 
        							this.getOpaqueDataList().get(j);
    	        			if (OpaqueDataUtil.equals(opaque, myOpaque)) {
    	        				found = true;
    	        				break;
    	        			}        					
        				}
        				if (!found) {
        					return false;        					
        				}
        			}
        			return true;
        		}
        		else {
                	log.warn("Unsupported expression operator: " + op);
        		}
        	}
        }
		
		return false;
	}
	*/
}
