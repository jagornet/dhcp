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
    protected ServerIdOptionDTO serverIdOption;
    protected PreferenceOptionDTO preferenceOption;
    protected InfoRefreshTimeOptionDTO infoRefreshTimeOption;
//    protected ServerUnicastOption serverUnicastOption;
//    protected StatusCodeOption statusCodeOption;
//    protected VendorInfoOption vendorInfoOption;
    protected DnsServersOptionDTO dnsServersOption;
    protected DomainSearchListOptionDTO domainSearchListOption;
    protected SipServerAddressesOptionDTO sipServerAddressesOption;
    protected SipServerDomainNamesOptionDTO sipServerDomainNamesOption;
    protected NisServersOptionDTO nisServersOption;
    protected NisDomainNameOptionDTO nisDomainNameOption;
    protected NisPlusServersOptionDTO nisPlusServersOption;
    protected NisPlusDomainNameOptionDTO nisPlusDomainNameOption;
    protected SntpServersOptionDTO sntpServersOption;
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
    public Integer getXyzPolicy()
    {
        return xyzPolicy;
    }
    public void setXyzPolicy(Integer xyzPolicy)
    {
        this.xyzPolicy = xyzPolicy;
    }
    public ServerIdOptionDTO getServerIdOption()
    {
        return serverIdOption;
    }
    public void setServerIdOption(ServerIdOptionDTO serverIdOption)
    {
        this.serverIdOption = serverIdOption;
    }
    public PreferenceOptionDTO getPreferenceOption()
    {
        return preferenceOption;
    }
    public void setPreferenceOption(PreferenceOptionDTO preferenceOption)
    {
        this.preferenceOption = preferenceOption;
    }
    public DnsServersOptionDTO getDnsServersOption()
    {
        return dnsServersOption;
    }
    public void setDnsServersOption(DnsServersOptionDTO dnsServersOption)
    {
        this.dnsServersOption = dnsServersOption;
    }
    public DomainSearchListOptionDTO getDomainSearchListOption()
    {
        return domainSearchListOption;
    }
    public void setDomainSearchListOption(
            DomainSearchListOptionDTO domainSearchListOption)
    {
        this.domainSearchListOption = domainSearchListOption;
    }
    public SipServerAddressesOptionDTO getSipServerAddressesOption()
    {
        return sipServerAddressesOption;
    }
    public void setSipServerAddressesOption(
            SipServerAddressesOptionDTO sipServerAddressesOption)
    {
        this.sipServerAddressesOption = sipServerAddressesOption;
    }
    public SipServerDomainNamesOptionDTO getSipServerDomainNamesOption()
    {
        return sipServerDomainNamesOption;
    }
    public void setSipServerDomainNamesOption(
            SipServerDomainNamesOptionDTO sipServerDomainNamesOption)
    {
        this.sipServerDomainNamesOption = sipServerDomainNamesOption;
    }
    public InfoRefreshTimeOptionDTO getInfoRefreshTimeOption()
    {
        return infoRefreshTimeOption;
    }
    public void setInfoRefreshTimeOption(
            InfoRefreshTimeOptionDTO infoRefreshTimeOption)
    {
        this.infoRefreshTimeOption = infoRefreshTimeOption;
    }
    public NisServersOptionDTO getNisServersOption()
    {
        return nisServersOption;
    }
    public void setNisServersOption(NisServersOptionDTO nisServersOption)
    {
        this.nisServersOption = nisServersOption;
    }
    public NisDomainNameOptionDTO getNisDomainNameOption()
    {
        return nisDomainNameOption;
    }
    public void setNisDomainNameOption(NisDomainNameOptionDTO nisDomainNameOption)
    {
        this.nisDomainNameOption = nisDomainNameOption;
    }
    public NisPlusServersOptionDTO getNisPlusServersOption()
    {
        return nisPlusServersOption;
    }
    public void setNisPlusServersOption(NisPlusServersOptionDTO nisPlusServersOption)
    {
        this.nisPlusServersOption = nisPlusServersOption;
    }
    public NisPlusDomainNameOptionDTO getNisPlusDomainNameOption()
    {
        return nisPlusDomainNameOption;
    }
    public void setNisPlusDomainNameOption(
            NisPlusDomainNameOptionDTO nisPlusDomainNameOption)
    {
        this.nisPlusDomainNameOption = nisPlusDomainNameOption;
    }
    public SntpServersOptionDTO getSntpServersOption()
    {
        return sntpServersOption;
    }
    public void setSntpServersOption(SntpServersOptionDTO sntpServersOption)
    {
        this.sntpServersOption = sntpServersOption;
    }
}
