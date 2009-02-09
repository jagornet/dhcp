package com.jagornet.dhcpv6.option;

import com.jagornet.dhcpv6.xml.NisDomainNameOption;

/**
 * <p>Title: DhcpNisDomainNameOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpNisDomainNameOption extends BaseDomainNameOption
{
    private NisDomainNameOption nisDomainNameOption;

    public DhcpNisDomainNameOption()
    {
        super();
        nisDomainNameOption = NisDomainNameOption.Factory.newInstance();
    }
    public DhcpNisDomainNameOption(NisDomainNameOption nisDomainNameOption)
    {
        super();
        if (nisDomainNameOption != null)
            this.nisDomainNameOption = nisDomainNameOption;
        else
            this.nisDomainNameOption = NisDomainNameOption.Factory.newInstance();
    }

    public NisDomainNameOption getNisDomainNameOption()
    {
        return nisDomainNameOption;
    }

    public void setNisDomainNameOption(NisDomainNameOption nisDomainNameOption)
    {
        if (nisDomainNameOption != null)
            this.nisDomainNameOption = nisDomainNameOption;
    }

    public int getCode()
    {
        return nisDomainNameOption.getCode();
    }

    public String getDomainName()
    {
        return nisDomainNameOption.getDomainName();
    }
    
    public void setDomainName(String domainName)
    {
        nisDomainNameOption.setDomainName(domainName);
    }
}
