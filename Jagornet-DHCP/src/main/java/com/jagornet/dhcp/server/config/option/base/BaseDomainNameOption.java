/*
 * Copyright 2009-2014 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file BaseDomainNameOption.java is part of Jagornet DHCP.
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
import com.jagornet.dhcp.xml.DomainNameOptionType;
import com.jagornet.dhcp.xml.Operator;
import com.jagornet.dhcp.xml.OptionExpression;

/**
 * Title: BaseDomainNameOption
 * Description: The abstract base class for domain name DHCP options.
 * 
 * @author A. Gregory Rabil
 */
public abstract class BaseDomainNameOption  
				extends com.jagornet.dhcp.option.base.BaseDomainNameOption 
				implements DhcpComparableOption

{
	private static Logger log = LoggerFactory.getLogger(BaseDomainNameOption.class);
	
	public BaseDomainNameOption()
	{
		this(null);
	}
	
	public BaseDomainNameOption(DomainNameOptionType domainNameOption)
	{
		super();
		if (domainNameOption != null) {
			super.setDomainName(domainNameOption.getDomainName());
		}
	}
	
	@Override
	public boolean matches(OptionExpression expression)
    {
        if (expression == null)
            return false;
        if (expression.getCode() != this.getCode())
            return false;        
        if (domainName == null)
        	return false;

        DomainNameOptionType exprOption = expression.getDomainNameOption();
        if (exprOption != null) {
        	String exprDomainName = exprOption.getDomainName();
            Operator op = expression.getOperator();
            if (op.equals(Operator.EQUALS)) {
            	return domainName.equals(exprDomainName);
            }
            else if (op.equals(Operator.STARTS_WITH)) {
            	return domainName.startsWith(exprDomainName);
            }
            else if (op.equals(Operator.ENDS_WITH)) {
            	return domainName.endsWith(exprDomainName);
            }
            else if (op.equals(Operator.CONTAINS)) {
            	return domainName.contains(exprDomainName);
            }
            else if (op.equals(Operator.REG_EXP)) {
            	return domainName.matches(exprDomainName);
            }
            else {
            	log.warn("Unsupported expression operator: " + op);
            }
        }
        
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(Util.LINE_SEPARATOR);
        sb.append(super.getName());
        sb.append(": domainName=");
        sb.append(domainName);
        return sb.toString();
    }

}
