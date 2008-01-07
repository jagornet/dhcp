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
 * <p>Title: BaseOptionDTO </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public abstract class BaseOptionDTO implements Serializable
{
    protected Short code;
    protected String name;
    
    public Short getCode()
    {
        return code;
    }
    public void setCode(Short code)
    {
        this.code = code;
    }
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
}
