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
 * <p>Title: DhcpV6ServerConfigDTO </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpV6ServerConfigDTO implements Serializable
{

    protected boolean abcPolicy;
    protected Integer xyzPolicy;
//    protected List genericPolicies;
//    protected ServerIdOption serverIdOption;
    protected PreferenceOptionDTO preferenceOption;
//    protected ServerUnicastOption serverUnicastOption;
//    protected StatusCodeOption statusCodeOption;
//    protected VendorInfoOption vendorInfoOption;
//    protected DnsServersOption dnsServersOption;
//    protected DomainListOption domainListOption;
//    protected List genericOptions;
//    protected List<DhcpV6ServerConfig.FilterGroups> filterGroups;
//    protected List<DhcpV6ServerConfig.Links> links;

    public boolean isAbcPolicy()
    {
        return abcPolicy;
    }
    public void setAbcPolicy(boolean abcPolicy)
    {
        this.abcPolicy = abcPolicy;
    }
    public PreferenceOptionDTO getPreferenceOption()
    {
        return preferenceOption;
    }
    public void setPreferenceOption(PreferenceOptionDTO preferenceOption)
    {
        this.preferenceOption = preferenceOption;
    }
    public Integer getXyzPolicy()
    {
        return xyzPolicy;
    }
    public void setXyzPolicy(Integer xyzPolicy)
    {
        this.xyzPolicy = xyzPolicy;
    }
}
