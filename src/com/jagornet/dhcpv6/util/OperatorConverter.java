/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file OperatorConverter.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.util;

import net.sf.dozer.util.mapping.MappingException;
import net.sf.dozer.util.mapping.converters.CustomConverter;

import com.jagornet.dhcpv6.xml.Operator;

/**
 * Title: OperatorConverter
 * Description: a customer converter used when mapping between domain
 * and opaqueData transfer objects via Dozer.  Converts between Enum and String.
 * 
 * @author A. Gregory Rabil
 */
public class OperatorConverter implements CustomConverter
{
	/* (non-Javadoc)
	 * @see net.sf.dozer.util.mapping.converters.CustomConverter#convert(java.lang.Object, java.lang.Object, java.lang.Class, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public Object convert(Object destObj, Object sourceObj, Class destClass, Class sourceClass)
	{
		if (sourceObj == null)
			return null;
		
		if (sourceObj instanceof Operator.Enum) {
			return ((Operator.Enum)sourceObj).toString();
		}
		else if (sourceObj instanceof String) {
			return Operator.Enum.forString((String)sourceObj);
		}
		else {
			throw new MappingException("Invalid source object provided for conversion: " + sourceObj);
		}
	}
}
