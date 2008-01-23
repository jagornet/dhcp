package com.agr.dhcpv6.dto;

/**
 * <p>Title: DomainNameOptionDTO </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DomainNameOptionDTO extends BaseOptionDTO
{
    protected String domainName;

    public String getDomainName()
    {
        return domainName;
    }
    public void setDomainName(String domainName)
    {
        this.domainName = domainName;
    }
}
