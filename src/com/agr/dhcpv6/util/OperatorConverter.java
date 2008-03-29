package com.agr.dhcpv6.util;

import net.sf.dozer.util.mapping.MappingException;
import net.sf.dozer.util.mapping.converters.CustomConverter;

import com.agr.dhcpv6.server.config.xml.Operator;

public class OperatorConverter implements CustomConverter
{

	public Object convert(Object destObj, Object sourceObj, Class destClass, Class sourceClass)
	{
		if (sourceObj == null)
			return null;
		
		if (sourceObj instanceof Operator) {
			return ((Operator)sourceObj).toString();
		}
		else if (sourceObj instanceof String) {
			return Operator.fromValue((String)sourceObj);
		}
		else {
			throw new MappingException("Invalid source object provided for conversion: " + sourceObj);
		}
	}

}
