package com.agr.dhcpv6.dto;

import java.io.Serializable;
import java.util.List;

/**
 * <p>Title: DhcpV6ServerConfigDTO </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpV6ServerConfigDTO implements Serializable
{
    protected Boolean sendRequestedOptionsOnlyPolicy;
//TODO    protected List genericPolicies;
    protected ServerIdOptionDTO serverIdOption;
    protected PreferenceOptionDTO preferenceOption;
    protected InfoRefreshTimeOptionDTO infoRefreshTimeOption;
//    protected ServerUnicastOption serverUnicastOption;
//TODO    protected StatusCodeOption statusCodeOption;
//TODO    protected VendorInfoOption vendorInfoOption;
    protected DnsServersOptionDTO dnsServersOption;
    protected DomainSearchListOptionDTO domainSearchListOption;
    protected SipServerAddressesOptionDTO sipServerAddressesOption;
    protected SipServerDomainNamesOptionDTO sipServerDomainNamesOption;
    protected NisServersOptionDTO nisServersOption;
    protected NisDomainNameOptionDTO nisDomainNameOption;
    protected NisPlusServersOptionDTO nisPlusServersOption;
    protected NisPlusDomainNameOptionDTO nisPlusDomainNameOption;
    protected SntpServersOptionDTO sntpServersOption;
// TODO		protected List genericOptions;
    
//  We can't use Java 5 Generics with GWT yet
//  protected List<DhcpV6ServerConfigDTO.Filters> filters;
   /**
    * NOTE:  Make sure there are two asterixes ** to
    *        ensure that this is JavaDoc, not a comment!
    *        
    * This annotation tells GWT what type of Object is
    * contained in the list - generics would solve this
    * 
    *  @gwt.typeArgs <com.agr.dhcpv6.dto.DhcpV6ServerConfigDTO.Filters>
    */
    protected List filters;

//  We can't use Java 5 Generics with GWT yet
//	protected List<DhcpV6ServerConfigDTO.Links> links;
   /**
    * NOTE:  Make sure there are two asterixes ** to
    *        ensure that this is JavaDoc, not a comment!
    *        
    * This annotation tells GWT what type of Object is
    * contained in the list - generics would solve this
    * 
    *  @gwt.typeArgs <com.agr.dhcpv6.dto.DhcpV6ServerConfigDTO.Links>
    */
    protected List links;
    

	public Boolean getSendRequestedOptionsOnlyPolicy() {
		return sendRequestedOptionsOnlyPolicy;
	}

	public void setSendRequestedOptionsOnlyPolicy(
			Boolean sendRequestedOptionsOnlyPolicy) {
		this.sendRequestedOptionsOnlyPolicy = sendRequestedOptionsOnlyPolicy;
	}

	public ServerIdOptionDTO getServerIdOption() {
		return serverIdOption;
	}

	public void setServerIdOption(ServerIdOptionDTO serverIdOption) {
		this.serverIdOption = serverIdOption;
	}

	public PreferenceOptionDTO getPreferenceOption() {
		return preferenceOption;
	}

	public void setPreferenceOption(PreferenceOptionDTO preferenceOption) {
		this.preferenceOption = preferenceOption;
	}

	public InfoRefreshTimeOptionDTO getInfoRefreshTimeOption() {
		return infoRefreshTimeOption;
	}

	public void setInfoRefreshTimeOption(
			InfoRefreshTimeOptionDTO infoRefreshTimeOption) {
		this.infoRefreshTimeOption = infoRefreshTimeOption;
	}

	public DnsServersOptionDTO getDnsServersOption() {
		return dnsServersOption;
	}

	public void setDnsServersOption(DnsServersOptionDTO dnsServersOption) {
		this.dnsServersOption = dnsServersOption;
	}

	public DomainSearchListOptionDTO getDomainSearchListOption() {
		return domainSearchListOption;
	}

	public void setDomainSearchListOption(
			DomainSearchListOptionDTO domainSearchListOption) {
		this.domainSearchListOption = domainSearchListOption;
	}

	public SipServerAddressesOptionDTO getSipServerAddressesOption() {
		return sipServerAddressesOption;
	}

	public void setSipServerAddressesOption(
			SipServerAddressesOptionDTO sipServerAddressesOption) {
		this.sipServerAddressesOption = sipServerAddressesOption;
	}

	public SipServerDomainNamesOptionDTO getSipServerDomainNamesOption() {
		return sipServerDomainNamesOption;
	}

	public void setSipServerDomainNamesOption(
			SipServerDomainNamesOptionDTO sipServerDomainNamesOption) {
		this.sipServerDomainNamesOption = sipServerDomainNamesOption;
	}

	public NisServersOptionDTO getNisServersOption() {
		return nisServersOption;
	}

	public void setNisServersOption(NisServersOptionDTO nisServersOption) {
		this.nisServersOption = nisServersOption;
	}

	public NisDomainNameOptionDTO getNisDomainNameOption() {
		return nisDomainNameOption;
	}

	public void setNisDomainNameOption(NisDomainNameOptionDTO nisDomainNameOption) {
		this.nisDomainNameOption = nisDomainNameOption;
	}

	public NisPlusServersOptionDTO getNisPlusServersOption() {
		return nisPlusServersOption;
	}

	public void setNisPlusServersOption(NisPlusServersOptionDTO nisPlusServersOption) {
		this.nisPlusServersOption = nisPlusServersOption;
	}

	public NisPlusDomainNameOptionDTO getNisPlusDomainNameOption() {
		return nisPlusDomainNameOption;
	}

	public void setNisPlusDomainNameOption(
			NisPlusDomainNameOptionDTO nisPlusDomainNameOption) {
		this.nisPlusDomainNameOption = nisPlusDomainNameOption;
	}

	public SntpServersOptionDTO getSntpServersOption() {
		return sntpServersOption;
	}

	public void setSntpServersOption(SntpServersOptionDTO sntpServersOption) {
		this.sntpServersOption = sntpServersOption;
	}

	public List getFilters() {
		return filters;
	}

	public void setFilters(List filters) {
		this.filters = filters;
	}

	public List getLinks() {
		return links;
	}

	public void setLinks(List links) {
		this.links = links;
	}
	
    public static class Filters implements Serializable
	{
		protected String name;
//  We can't use Java 5 Generics with GWT yet
//		protected List<OptionExpression> optionExpressions;
	   /**
	    * NOTE:  Make sure there are two asterixes ** to
	    *        ensure that this is JavaDoc, not a comment!
	    *        
	    * This annotation tells GWT what type of Object is
	    * contained in the list - generics would solve this
	    * 
	    *  @gwt.typeArgs <com.agr.dhcpv6.dto.OptionExpressionDTO>
	    */		
	    protected List optionExpressions;
	    
		protected Boolean sendRequestedOptionsOnlyPolicy;
//TODO		protected List<Policy> genericPolicies;
		protected PreferenceOptionDTO preferenceOption;
//		protected ServerUnicastOptionDTO serverUnicastOption;
//TODO		protected StatusCodeOptionDTO statusCodeOption;
//TODO		protected VendorInfoOptionDTO vendorInfoOption;
		protected DnsServersOptionDTO dnsServersOption;
		protected DomainSearchListOptionDTO domainSearchListOption;
		protected SipServerAddressesOptionDTO sipServerAddressesOption;
		protected SipServerDomainNamesOptionDTO sipServerDomainNamesOption;
		protected NisServersOptionDTO nisServersOption;
		protected NisDomainNameOptionDTO nisDomainNameOption;
		protected NisPlusServersOptionDTO nisPlusServersOption;
		protected NisPlusDomainNameOptionDTO nisPlusDomainNameOption;
	    protected SntpServersOptionDTO sntpServersOption;
	    protected InfoRefreshTimeOptionDTO infoRefreshTimeOption;
//TODO	    protected List<Option> genericOptions;
	    
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public List getOptionExpressions() {
			return optionExpressions;
		}
		public void setOptionExpressions(List optionExpressions) {
			this.optionExpressions = optionExpressions;
		}
		public Boolean getSendRequestedOptionsOnlyPolicy() {
			return sendRequestedOptionsOnlyPolicy;
		}
		public void setSendRequestedOptionsOnlyPolicy(
				Boolean sendRequestedOptionsOnlyPolicy) {
			this.sendRequestedOptionsOnlyPolicy = sendRequestedOptionsOnlyPolicy;
		}
		public PreferenceOptionDTO getPreferenceOption() {
			return preferenceOption;
		}
		public void setPreferenceOption(PreferenceOptionDTO preferenceOption) {
			this.preferenceOption = preferenceOption;
		}
		public DnsServersOptionDTO getDnsServersOption() {
			return dnsServersOption;
		}
		public void setDnsServersOption(DnsServersOptionDTO dnsServersOption) {
			this.dnsServersOption = dnsServersOption;
		}
		public DomainSearchListOptionDTO getDomainSearchListOption() {
			return domainSearchListOption;
		}
		public void setDomainSearchListOption(
				DomainSearchListOptionDTO domainSearchListOption) {
			this.domainSearchListOption = domainSearchListOption;
		}
		public SipServerAddressesOptionDTO getSipServerAddressesOption() {
			return sipServerAddressesOption;
		}
		public void setSipServerAddressesOption(
				SipServerAddressesOptionDTO sipServerAddressesOption) {
			this.sipServerAddressesOption = sipServerAddressesOption;
		}
		public SipServerDomainNamesOptionDTO getSipServerDomainNamesOption() {
			return sipServerDomainNamesOption;
		}
		public void setSipServerDomainNamesOption(
				SipServerDomainNamesOptionDTO sipServerDomainNamesOption) {
			this.sipServerDomainNamesOption = sipServerDomainNamesOption;
		}
		public NisServersOptionDTO getNisServersOption() {
			return nisServersOption;
		}
		public void setNisServersOption(NisServersOptionDTO nisServersOption) {
			this.nisServersOption = nisServersOption;
		}
		public NisDomainNameOptionDTO getNisDomainNameOption() {
			return nisDomainNameOption;
		}
		public void setNisDomainNameOption(NisDomainNameOptionDTO nisDomainNameOption) {
			this.nisDomainNameOption = nisDomainNameOption;
		}
		public NisPlusServersOptionDTO getNisPlusServersOption() {
			return nisPlusServersOption;
		}
		public void setNisPlusServersOption(NisPlusServersOptionDTO nisPlusServersOption) {
			this.nisPlusServersOption = nisPlusServersOption;
		}
		public NisPlusDomainNameOptionDTO getNisPlusDomainNameOption() {
			return nisPlusDomainNameOption;
		}
		public void setNisPlusDomainNameOption(
				NisPlusDomainNameOptionDTO nisPlusDomainNameOption) {
			this.nisPlusDomainNameOption = nisPlusDomainNameOption;
		}
		public SntpServersOptionDTO getSntpServersOption() {
			return sntpServersOption;
		}
		public void setSntpServersOption(SntpServersOptionDTO sntpServersOption) {
			this.sntpServersOption = sntpServersOption;
		}
		public InfoRefreshTimeOptionDTO getInfoRefreshTimeOption() {
			return infoRefreshTimeOption;
		}
		public void setInfoRefreshTimeOption(
				InfoRefreshTimeOptionDTO infoRefreshTimeOption) {
			this.infoRefreshTimeOption = infoRefreshTimeOption;
		}
	}
    
    
    public static class Links implements Serializable
    {
    	protected String name;
        protected String address;
        protected Boolean sendRequestedOptionsOnlyPolicy;
//TODO        protected List<Policy> genericPolicies;
        protected ServerIdOptionDTO serverIdOption;
        protected PreferenceOptionDTO preferenceOption;
//        protected ServerUnicastOptionDTO serverUnicastOption;
//TODO        protected StatusCodeOptionDTO statusCodeOption;
//TODO        protected VendorInfoOptionDTO vendorInfoOption;
        protected DnsServersOptionDTO dnsServersOption;
        protected DomainSearchListOptionDTO domainSearchListOption;
        protected SipServerAddressesOptionDTO sipServerAddressesOption;
        protected SipServerDomainNamesOptionDTO sipServerDomainNamesOption;
        protected NisServersOptionDTO nisServersOption;
        protected NisDomainNameOptionDTO nisDomainNameOption;
        protected NisPlusServersOptionDTO nisPlusServersOption;
        protected NisPlusDomainNameOptionDTO nisPlusDomainNameOption;
        protected SntpServersOptionDTO sntpServersOption;
        protected InfoRefreshTimeOptionDTO infoRefreshTimeOption;
//TODO        protected List<Option> genericOptions;

        //  We can't use Java 5 Generics with GWT yet
        // NOTE: for this DTO we don't require a Links.Filters class, instead
        //		 we can just reuse the DhcpV6ServerConfigDTO.Filters class
        // protected List<DhcpV6ServerConfigDTO.Filters> filters;
       /**
        * NOTE:  Make sure there are two asterixes ** to
        *        ensure that this is JavaDoc, not a comment!
        *        
        * This annotation tells GWT what type of Object is
        * contained in the list - generics would solve this
        * 
        * DON'T CREATE ANOTHER INNER-CLASS for Links.Filters, instead just
        * reuse the DhcpV6ServerConfigDTO.Filters class
        *  @gwt.typeArgs <com.agr.dhcpv6.dto.DhcpV6ServerConfigDTO.Filters>
        */
    	protected List filters;
	
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getAddress() {
			return address;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		public Boolean getSendRequestedOptionsOnlyPolicy() {
			return sendRequestedOptionsOnlyPolicy;
		}
		public void setSendRequestedOptionsOnlyPolicy(
				Boolean sendRequestedOptionsOnlyPolicy) {
			this.sendRequestedOptionsOnlyPolicy = sendRequestedOptionsOnlyPolicy;
		}
		public ServerIdOptionDTO getServerIdOption() {
			return serverIdOption;
		}
		public void setServerIdOption(ServerIdOptionDTO serverIdOption) {
			this.serverIdOption = serverIdOption;
		}
		public PreferenceOptionDTO getPreferenceOption() {
			return preferenceOption;
		}
		public void setPreferenceOption(PreferenceOptionDTO preferenceOption) {
			this.preferenceOption = preferenceOption;
		}
		public DnsServersOptionDTO getDnsServersOption() {
			return dnsServersOption;
		}
		public void setDnsServersOption(DnsServersOptionDTO dnsServersOption) {
			this.dnsServersOption = dnsServersOption;
		}
		public DomainSearchListOptionDTO getDomainSearchListOption() {
			return domainSearchListOption;
		}
		public void setDomainSearchListOption(
				DomainSearchListOptionDTO domainSearchListOption) {
			this.domainSearchListOption = domainSearchListOption;
		}
		public SipServerAddressesOptionDTO getSipServerAddressesOption() {
			return sipServerAddressesOption;
		}
		public void setSipServerAddressesOption(
				SipServerAddressesOptionDTO sipServerAddressesOption) {
			this.sipServerAddressesOption = sipServerAddressesOption;
		}
		public SipServerDomainNamesOptionDTO getSipServerDomainNamesOption() {
			return sipServerDomainNamesOption;
		}
		public void setSipServerDomainNamesOption(
				SipServerDomainNamesOptionDTO sipServerDomainNamesOption) {
			this.sipServerDomainNamesOption = sipServerDomainNamesOption;
		}
		public NisServersOptionDTO getNisServersOption() {
			return nisServersOption;
		}
		public void setNisServersOption(NisServersOptionDTO nisServersOption) {
			this.nisServersOption = nisServersOption;
		}
		public NisDomainNameOptionDTO getNisDomainNameOption() {
			return nisDomainNameOption;
		}
		public void setNisDomainNameOption(NisDomainNameOptionDTO nisDomainNameOption) {
			this.nisDomainNameOption = nisDomainNameOption;
		}
		public NisPlusServersOptionDTO getNisPlusServersOption() {
			return nisPlusServersOption;
		}
		public void setNisPlusServersOption(NisPlusServersOptionDTO nisPlusServersOption) {
			this.nisPlusServersOption = nisPlusServersOption;
		}
		public NisPlusDomainNameOptionDTO getNisPlusDomainNameOption() {
			return nisPlusDomainNameOption;
		}
		public void setNisPlusDomainNameOption(
				NisPlusDomainNameOptionDTO nisPlusDomainNameOption) {
			this.nisPlusDomainNameOption = nisPlusDomainNameOption;
		}
		public SntpServersOptionDTO getSntpServersOption() {
			return sntpServersOption;
		}
		public void setSntpServersOption(SntpServersOptionDTO sntpServersOption) {
			this.sntpServersOption = sntpServersOption;
		}
		public InfoRefreshTimeOptionDTO getInfoRefreshTimeOption() {
			return infoRefreshTimeOption;
		}
		public void setInfoRefreshTimeOption(
				InfoRefreshTimeOptionDTO infoRefreshTimeOption) {
			this.infoRefreshTimeOption = infoRefreshTimeOption;
		}
		public List getFilters() {
			return filters;
		}
		public void setFilters(List filters) {
			this.filters = filters;
		}
    }

}
