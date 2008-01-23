package com.agr.dhcpv6.dto;

import java.io.Serializable;


/**
 * <p>Title: ShortOptionDTO </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class IntOptionDTO extends BaseOptionDTO
{
    protected int value;

    public int getValue()
    {
        return value;
    }
    public void setValue(int value)
    {
        this.value = value;
    }
}
