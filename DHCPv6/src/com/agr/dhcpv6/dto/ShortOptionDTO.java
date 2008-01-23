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
    protected short value;

    public short getValue()
    {
        return value;
    }
    public void setValue(short value)
    {
        this.value = value;
    }
}
