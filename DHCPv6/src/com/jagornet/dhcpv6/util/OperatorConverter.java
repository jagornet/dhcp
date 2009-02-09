package com.jagornet.dhcpv6.util;

import net.sf.dozer.util.mapping.MappingException;
import net.sf.dozer.util.mapping.converters.CustomConverter;

import com.jagornet.dhcpv6.xml.Operator;

public class OperatorConverter implements CustomConverter
{
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
