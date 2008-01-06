package com.agr.dhcpv6.dto;

import java.util.List;

public class DomainNameListOptionDTO extends BaseOptionDTO
{
    protected List<String> domainNames;

    public List<String> getDomainNames()
    {
        return domainNames;
    }

    public void setDomainNames(List<String> domainNames)
    {
        this.domainNames = domainNames;
    }
}
