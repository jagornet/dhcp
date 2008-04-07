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
    protected Integer code;
    protected String name;
    
    public Integer getCode()
    {
        return code;
    }
    public void setCode(Integer code)
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
