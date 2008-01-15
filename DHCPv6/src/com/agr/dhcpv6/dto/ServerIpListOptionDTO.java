package com.agr.dhcpv6.dto;

import java.util.List;

public class ServerIpListOptionDTO extends BaseOptionDTO
{
//  We can't use Java 5 Generics with GWT yet
//    protected List<String> serverIpAddresses;
    /**
     * NOTE:  Make sure there are two asterixes ** to
     *        ensure that this is JavaDoc, not a comment!
     *        
     * This annotation tells GWT what type of Object is
     * contained in the list - generics would solve this
     * 
     *  @gwt.typeArgs <java.lang.String>
     */
    protected List serverIpAddresses;

//    public List<String> getServerIpAddresses()
    /**
     * Must be JavaDoc-style ** comment
     * 
     *  @gwt.typeArgs <java.lang.String>
     */
    public List getServerIpAddresses()
    {
        return serverIpAddresses;
    }

//    public void setServerIpAddresses(List<String> serverIpAddresses)
    /**
     * Must be JavaDoc-style ** comment
     * 
     *  @gwt.typeArgs serverIpAddresses <java.lang.String>
     */
    public void setServerIpAddresses(List serverIpAddresses)
    {
        this.serverIpAddresses = serverIpAddresses;
    }
}
