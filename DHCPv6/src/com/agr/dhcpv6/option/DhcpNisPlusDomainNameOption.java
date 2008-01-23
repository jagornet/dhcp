package com.agr.dhcpv6.option;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.server.config.xml.NisPlusDomainNameOption;

/**
 * <p>Title: DhcpNisPlusDomainNameOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpNisPlusDomainNameOption extends BaseDomainNameOption
{
    private static Log log = LogFactory.getLog(DhcpNisPlusDomainNameOption.class);

    private NisPlusDomainNameOption nisPlusDomainNameOption;

    public DhcpNisPlusDomainNameOption()
    {
        super();
        nisPlusDomainNameOption = new NisPlusDomainNameOption();
    }
    public DhcpNisPlusDomainNameOption(NisPlusDomainNameOption nisPlusDomainNameOption)
    {
        super();
        if (nisPlusDomainNameOption != null)
            this.nisPlusDomainNameOption = nisPlusDomainNameOption;
        else
            this.nisPlusDomainNameOption = new NisPlusDomainNameOption();
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

    public short getCode()
    {
        return nisPlusDomainNameOption.getCode();
    }

    public String getName()
    {
        return nisPlusDomainNameOption.getName();
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
