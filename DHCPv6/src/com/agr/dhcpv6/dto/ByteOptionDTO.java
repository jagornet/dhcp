package com.agr.dhcpv6.dto;

import java.io.Serializable;


/**
 * <p>Title: ByteOptionDTO </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class ByteOptionDTO extends BaseOptionDTO
{
    protected byte value;

    public byte getValue()
    {
        return value;
    }
    public void setValue(byte value)
    {
        this.value = value;
    }
}
