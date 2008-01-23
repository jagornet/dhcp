package com.agr.dhcpv6.option;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.server.config.xml.NisDomainNameOption;

/**
 * <p>Title: DhcpNisDomainNameOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpNisDomainNameOption extends BaseDomainNameOption
{
    private static Log log = LogFactory.getLog(DhcpNisDomainNameOption.class);

    private NisDomainNameOption nisDomainNameOption;

    public DhcpNisDomainNameOption()
    {
        super();
        nisDomainNameOption = new NisDomainNameOption();
    }
    public DhcpNisDomainNameOption(NisDomainNameOption nisDomainNameOption)
    {
        super();
        if (nisDomainNameOption != null)
            this.nisDomainNameOption = nisDomainNameOption;
        else
            this.nisDomainNameOption = new NisDomainNameOption();
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

    public short getCode()
    {
        return nisDomainNameOption.getCode();
    }

    public String getName()
    {
        return nisDomainNameOption.getName();
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
