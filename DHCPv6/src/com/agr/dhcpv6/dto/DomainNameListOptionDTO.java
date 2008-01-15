package com.agr.dhcpv6.dto;

import java.util.List;

public class DomainNameListOptionDTO extends BaseOptionDTO
{
//  We can't use Java 5 Generics with GWT yet
//    protected List<String> domainNames;
    protected List domainNames;

//    public List<String> getDomainNames()
    public List getDomainNames()
    {
        return domainNames;
    }

//    public void setDomainNames(List<String> domainNames)
    public void setDomainNames(List domainNames)
    {
        this.domainNames = domainNames;
    }
}
