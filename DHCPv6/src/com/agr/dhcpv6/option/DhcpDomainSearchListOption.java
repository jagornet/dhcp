package com.agr.dhcpv6.option;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.agr.dhcpv6.server.config.xml.DomainSearchListOption;

/**
 * <p>Title: DhcpDomainSearchListOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpDomainSearchListOption extends BaseDomainNameListOption
{
    private static Log log = LogFactory.getLog(DhcpDomainSearchListOption.class);

    private DomainSearchListOption domainSearchListOption;

    public DhcpDomainSearchListOption()
    {
        super();
        domainSearchListOption = new DomainSearchListOption();
    }
    public DhcpDomainSearchListOption(DomainSearchListOption domainSearchListOption)
    {
        super();
        if (domainSearchListOption != null)
            this.domainSearchListOption = domainSearchListOption;
        else
            this.domainSearchListOption = new DomainSearchListOption();
    }

    public DomainSearchListOption getDomainSearchListOption()
    {
        return domainSearchListOption;
    }

    public void setDomainSearchListOption(DomainSearchListOption domainSearchListOption)
    {
        if (domainSearchListOption != null)
            this.domainSearchListOption = domainSearchListOption;
    }

    public short getCode()
    {
        return domainSearchListOption.getCode();
    }

    public String getName()
    {
        return domainSearchListOption.getName();
    }

    public List<String> getDomainNames()
    {
        return domainSearchListOption.getDomainNames();
    }
}
