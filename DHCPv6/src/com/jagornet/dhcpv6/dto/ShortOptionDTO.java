package com.jagornet.dhcpv6.dto;

/**
 * <p>Title: ShortOptionDTO </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class ShortOptionDTO extends BaseOptionDTO
{
    protected Short value;

    public Short getValue()
    {
        return getShortValue();
    }
    // getShortValue for mapping via Dozer
    public Short getShortValue()
    {
    	return value;
    }
    
    public void setValue(Short value)
    {
        setShortValue(value);
    }
    // setShortValue for mapping via Dozer
    public void setShortValue(Short value)
    {
    	this.value = value;
    }
}
