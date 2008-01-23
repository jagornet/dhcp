package com.agr.dhcpv6.dto;

import java.util.List;

public class DomainSearchNameListOptionDTO extends BaseOptionDTO
{
//  We can't use Java 5 Generics with GWT yet
//    protected List<String> domainNames;
    /**
     * NOTE:  Make sure there are two asterixes ** to
     *        ensure that this is JavaDoc, not a comment!
     *        
     * This annotation tells GWT what type of Object is
     * contained in the list - generics would solve this
     * 
     *  @gwt.typeArgs <java.lang.String>
     */
    protected List domainNames;

//    public List<String> getDomainNames()
    /**
     * Must be JavaDoc-style ** comment
     * 
     *  @gwt.typeArgs <java.lang.String>
     */
    public List getDomainNames()
    {
        return domainNames;
    }

//    public void setDomainNames(List<String> domainNames)
    /**
     * Must be JavaDoc-style ** comment
     * 
     *  @gwt.typeArgs domainNames <java.lang.String>
     */
    public void setDomainNames(List domainNames)
    {
        this.domainNames = domainNames;
    }
}
