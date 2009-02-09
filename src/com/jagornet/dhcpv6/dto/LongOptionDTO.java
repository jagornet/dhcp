package com.jagornet.dhcpv6.dto;



/**
 * <p>Title: LongOptionDTO </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class LongOptionDTO extends BaseOptionDTO
{
    protected Long value;

    public Long getValue()
    {
        return getLongValue();
    }
    // getLongValue for mapping via Dozer
    public Long getLongValue()
    {
    	return value;
    }
    
    public void setValue(Long value)
    {
        setLongValue(value);
    }
    // setLongValue for mapping via Dozer
    public void setLongValue(Long value)
    {
    	this.value = value;
    }
}
