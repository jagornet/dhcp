package com.jagornet.dhcpv6.dto;



/**
 * <p>Title: IntOptionDTO </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class IntOptionDTO extends BaseOptionDTO
{
    protected Integer value;

    public Integer getValue()
    {
        return getIntValue();
    }
    // getIntValue for mapping via Dozer
    public Integer getIntValue()
    {
    	return value;
    }
    
    public void setValue(Integer value)
    {
        setIntValue(value);
    }
    // setIntValue for mapping via Dozer
    public void setIntValue(Integer value)
    {
    	this.value = value;
    }
}
