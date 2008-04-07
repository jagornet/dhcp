package com.agr.dhcpv6.dto;

import java.io.Serializable;


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
        return value;
    }
    public void setValue(Integer value)
    {
        this.value = value;
    }
}
