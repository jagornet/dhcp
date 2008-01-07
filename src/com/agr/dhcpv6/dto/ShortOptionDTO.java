/*
 * Copyright (c) 2004 Diamond IP Technologies, Inc.
 * All international rights reserved. Unauthorized use prohibited.
 *
 * $Log: $
 * 
 */

package com.agr.dhcpv6.dto;

import java.io.Serializable;


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
