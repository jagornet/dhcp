package com.jagornet.dhcpv6.dto;

import java.util.List;

public class DomainSearchNameListOptionDTO extends BaseOptionDTO
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
