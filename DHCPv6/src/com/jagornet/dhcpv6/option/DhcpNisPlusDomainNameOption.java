package com.jagornet.dhcpv6.option;

import com.jagornet.dhcpv6.xml.NisPlusDomainNameOption;

/**
 * <p>Title: DhcpNisPlusDomainNameOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpNisPlusDomainNameOption extends BaseDomainNameOption
{
    private NisPlusDomainNameOption nisPlusDomainNameOption;

    public DhcpNisPlusDomainNameOption()
    {
        super();
        nisPlusDomainNameOption = NisPlusDomainNameOption.Factory.newInstance();
    }
    public DhcpNisPlusDomainNameOption(NisPlusDomainNameOption nisPlusDomainNameOption)
    {
        super();
        if (nisPlusDomainNameOption != null)
            this.nisPlusDomainNameOption = nisPlusDomainNameOption;
        else
            this.nisPlusDomainNameOption = NisPlusDomainNameOption.Factory.newInstance();
    }

    public NisPlusDomainNameOption getNisPlusDomainNameOption()
    {
        return nisPlusDomainNameOption;
    }

    public void setNisPlusDomainNameOption(NisPlusDomainNameOption nisPlusDomainNameOption)
    {
        if (nisPlusDomainNameOption != null)
            this.nisPlusDomainNameOption = nisPlusDomainNameOption;
    }

    public int getCode()
    {
        return nisPlusDomainNameOption.getCode();
    }

    public String getDomainName()
    {
        return nisPlusDomainNameOption.getDomainName();
    }
    
    public void setDomainName(String domainName)
    {
        nisPlusDomainNameOption.setDomainName(domainName);
    }
}
