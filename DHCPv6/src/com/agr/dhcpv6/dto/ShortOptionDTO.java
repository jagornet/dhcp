package com.agr.dhcpv6.dto;

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
        return value;
    }
    public void setValue(Short value)
    {
        this.value = value;
    }
}
