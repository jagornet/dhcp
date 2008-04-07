package com.agr.dhcpv6.dto;

import java.io.Serializable;


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
        return value;
    }
    public void setValue(Long value)
    {
        this.value = value;
    }
}
